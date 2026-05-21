package com.shadowspend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
public class NormalizationService {

    private static final Map<String, String> ALIASES = new LinkedHashMap<>();

    static {
        ALIASES.put("netflix", "Netflix");
        ALIASES.put("spotify", "Spotify");
        ALIASES.put("amazon prime", "Amazon Prime");
        ALIASES.put("prime video", "Amazon Prime");
        ALIASES.put("amazon.in", "Amazon Prime");
        ALIASES.put("google one", "Google One");
        ALIASES.put("google storage", "Google One");
        ALIASES.put("jio", "Jio");
        ALIASES.put("jiosaavn", "Jio");
        ALIASES.put("jiofiber", "Jio");
        ALIASES.put("reliance jio", "Jio");
        ALIASES.put("hotstar", "Hotstar");
        ALIASES.put("disney+ hotstar", "Hotstar");
        ALIASES.put("youtube premium", "YouTube Premium");
        ALIASES.put("microsoft", "Microsoft");
        ALIASES.put("microsoft 365", "Microsoft");
        ALIASES.put("adobe", "Adobe");
        ALIASES.put("swiggy", "Swiggy");
        ALIASES.put("zomato", "Zomato");
        ALIASES.put("phonepe", "PhonePe");
        ALIASES.put("paytm", "Paytm");
    }

    public String normalizeMerchant(String raw) {
        log.info("normalizeMerchant called");
        if (raw == null) {
            return "";
        }
        String normalizedInput = raw.toLowerCase().trim();
        for (Map.Entry<String, String> entry : ALIASES.entrySet()) {
            if (normalizedInput.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return raw.trim();
    }
}
