package com.shadowspend.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.shadowspend.config.GmailConfig;
import com.shadowspend.model.Email;
import com.shadowspend.model.User;
import com.shadowspend.repository.EmailRepository;
import com.shadowspend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailService {

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    // private static final String USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
    private static final String USERINFO_URL = "https://openidconnect.googleapis.com/v1/userinfo";
    
    private final MailService mailService;
    private final GmailConfig gmailConfig;
    private final UserRepository userRepository;
    private final EmailRepository emailRepository;
    private final RestClient restClient = RestClient.create();

    public User exchangeCodeForTokens(String code) {
        log.info("exchangeCodeForTokens called");
        try {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("code", code);
            form.add("client_id", gmailConfig.getClientId());
            form.add("client_secret", gmailConfig.getClientSecret());
            form.add("redirect_uri", gmailConfig.getRedirectUri());
            form.add("grant_type", "authorization_code");
            Map<String, Object> tokenResponse = postForm(TOKEN_URL, form);
            String accessToken = stringVal(tokenResponse.get("access_token"));
            String refreshToken = stringVal(tokenResponse.get("refresh_token"));
            Integer expiresIn = intVal(tokenResponse.get("expires_in"));
            log.info("Access Token: {}", accessToken);
            log.info("Token Response: {}", tokenResponse);
            Map<String, Object> userInfo = restClient.get()
                .uri(USERINFO_URL)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .retrieve()
                .body(Map.class);
            // Map<String, Object> userInfo = restClient.get().uri(USERINFO_URL)
            //         .header("Authorization", "Bearer " + accessToken)
            //         .retrieve().body(Map.class);
            String email = stringVal(userInfo == null ? null : userInfo.get("email"));
            if (email == null || email.isBlank()) {
                throw new IllegalStateException("Could not resolve user email from Google");
            }
            // User user = userRepository.findByEmail(email).orElseGet(User::new);
            Optional<User> existingUser = userRepository.findByEmail(email);

            User user;
            boolean isNewUser = false;

            if (existingUser.isPresent()) {
                user = existingUser.get();
            } else {
                user = new User();
                isNewUser = true;  // ⭐ track new user
            }
            user.setEmail(email);
            user.setName(stringVal(userInfo.get("name")));
            user.setPictureUrl(stringVal(userInfo.get("picture")));
            user.setGoogleAccessToken(accessToken);
            if (refreshToken != null && !refreshToken.isBlank()) {
                user.setGoogleRefreshToken(refreshToken);
            }
            user.setTokenExpiry(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(expiresIn == null ? 3600 : expiresIn));
            if (user.getCreatedAt() == null) {
                user.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            }
            user.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            // return userRepository.save(user);
            User savedUser = userRepository.save(user);

// 👇 SEND EMAIL ONLY IF NEW USER
            if (isNewUser) {
               mailService.sendWelcomeEmail(savedUser.getEmail());
            }
            return savedUser;
        } catch (Exception ex) {
            log.error("exchangeCodeForTokens failed", ex);
            throw new IllegalStateException("Google auth failed", ex);
        }
    }

    public User refreshTokenIfNeeded(User user) {
        log.info("refreshTokenIfNeeded called for user {}", user.getId());
        try {
            if (user.getTokenExpiry() != null && user.getTokenExpiry().isAfter(OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(5))) {
                return user;
            }
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("client_id", gmailConfig.getClientId());
            form.add("client_secret", gmailConfig.getClientSecret());
            form.add("refresh_token", user.getGoogleRefreshToken());
            form.add("grant_type", "refresh_token");
            Map<String, Object> tokenResponse = postForm(TOKEN_URL, form);
            String accessToken = stringVal(tokenResponse.get("access_token"));
            Integer expiresIn = intVal(tokenResponse.get("expires_in"));
            if (accessToken != null && !accessToken.isBlank()) {
                user.setGoogleAccessToken(accessToken);
            }
            user.setTokenExpiry(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(expiresIn == null ? 3600 : expiresIn));
            user.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
            return userRepository.save(user);
        } catch (Exception ex) {
            log.error("refreshTokenIfNeeded failed for user {}", user.getId(), ex);
            return user;
        }
    }

    public int fetchEmails(User user) {
        log.info("fetchEmails called for user {}", user.getId());
        int fetched = 0;
        try {
            HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            HttpRequestInitializer requestInitializer = request -> request.getHeaders().setAuthorization("Bearer " + user.getGoogleAccessToken());
            Gmail gmail = new Gmail.Builder(httpTransport, GsonFactory.getDefaultInstance(), requestInitializer)
                    .setApplicationName("ShadowSpend")
                    .build();
            // ListMessagesResponse response = gmail.users().messages().list("me").setMaxResults(100L).setQ("in:anywhere subject:(receipt OR invoice OR payment OR subscription OR bill OR charged OR renewed)").execute();
            ListMessagesResponse response = gmail.users().messages().list("me")
            .setMaxResults(50L)
            // .setQ("in:anywhere from:(netflix OR spotify OR amazon OR google OR microsoft OR adobe OR canva OR chatgpt OR openai OR github OR notion OR figma OR zoom OR slack OR dropbox OR apple OR jio OR airtel OR hotstar OR youtube OR xbox OR grammarly OR linkedin OR coursera OR udemy OR midjourney OR paytm OR phonepe) (receipt OR payment OR charged OR renewed OR subscription OR invoice OR \"thank you for your payment\" OR \"payment successful\")")
            .setQ("in:anywhere (receipt OR invoice OR payment OR subscription OR charged OR renewed)")
            .execute();
            List<Message> messageRefs = response.getMessages();
            if (messageRefs == null) {
                return 0;
            }
            for (Message messageRef : messageRefs) {
                try {
                    if (emailRepository.existsByGmailId(messageRef.getId())) {
                        continue;
                    }
                    Message fullMessage = gmail.users().messages().get("me", messageRef.getId()).setFormat("full").execute();
                    Email email = new Email();
                    email.setUser(user);
                    email.setGmailId(fullMessage.getId());
                    email.setSnippet(fullMessage.getSnippet());
                    email.setSubject(getHeader(fullMessage, "Subject"));
                    String from = getHeader(fullMessage, "From");
                    email.setSender(from);
                    email.setSenderDomain(extractDomain(from));
                    email.setRawBody(extractBody(fullMessage.getPayload()));
                    long internalDate = Optional.ofNullable(fullMessage.getInternalDate()).orElse(Instant.now().toEpochMilli());
                    email.setReceivedAt(OffsetDateTime.ofInstant(Instant.ofEpochMilli(internalDate), ZoneOffset.UTC));
                    email.setProcessed(Boolean.FALSE);
                    email.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
                    emailRepository.save(email);
                    fetched++;
                } catch (Exception itemEx) {
                    log.error("Failed to process Gmail message {}", messageRef.getId(), itemEx);
                }
            }
        } catch (Exception ex) {
            log.error("fetchEmails failed for user {}", user.getId(), ex);
        }
        return fetched;
    }

    private Map<String, Object> postForm(String url, MultiValueMap<String, String> form) {
        return restClient.post().uri(url).contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form).retrieve().body(Map.class);
    }

    private String getHeader(Message message, String name) {
        if (message == null || message.getPayload() == null || message.getPayload().getHeaders() == null) {
            return null;
        }
        return message.getPayload().getHeaders().stream()
                .filter(h -> name.equalsIgnoreCase(h.getName()))
                .map(com.google.api.services.gmail.model.MessagePartHeader::getValue)
                .findFirst().orElse(null);
    }

    private String extractBody(MessagePart payload) {
        if (payload == null) {
            return "";
        }
        try {
            if (payload.getBody() != null && payload.getBody().getData() != null) {
                return new String(Base64.getUrlDecoder().decode(payload.getBody().getData()), StandardCharsets.UTF_8);
            }
            if (payload.getParts() != null) {
                StringBuilder out = new StringBuilder();
                for (MessagePart part : payload.getParts()) {
                    out.append(extractBody(part));
                }
                return out.toString();
            }
        } catch (Exception ex) {
            log.error("Body extraction failed", ex);
        }
        return "";
    }

    private String extractDomain(String from) {
        if (from == null) {
            return null;
        }
        int at = from.lastIndexOf("@");
        if (at == -1) {
            return null;
        }
        String domain = from.substring(at + 1).replace(">", "").trim();
        return domain.isBlank() ? null : domain.toLowerCase();
    }

    private String stringVal(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer intVal(Object value) {
        try {
            return value == null ? null : Integer.parseInt(String.valueOf(value));
        } catch (Exception ex) {
            log.error("Failed to parse int value {}", value, ex);
            return null;
        }
    }
}
