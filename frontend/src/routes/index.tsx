import { createFileRoute } from "@tanstack/react-router";

const GOOGLE_AUTH_URL = `https://accounts.google.com/o/oauth2/v2/auth?client_id=4562707758-lah3hl8cagn8gllp1mn6br2gfvfj04u6.apps.googleusercontent.com&redirect_uri=http://localhost:8081/auth/callback&response_type=code&scope=${[
  "https://www.googleapis.com/auth/gmail.readonly",
  "https://www.googleapis.com/auth/userinfo.email",
  "https://www.googleapis.com/auth/userinfo.profile",
].join(" ")}&access_type=offline&prompt=consent`;

export const Route = createFileRoute("/")({
  component: LoginPage,
  head: () => ({
    meta: [
      { title: "ShadowSpend — See every subscription" },
      {
        name: "description",
        content:
          "Connect your Gmail to discover hidden recurring charges and stop surprise subscription fees.",
      },
    ],
  }),
});

function LoginPage() {
  const handleConnect = () => {
    window.location.href = GOOGLE_AUTH_URL;
  };

  return (
    <div
      className="flex min-h-screen flex-col items-center justify-center px-6 text-white"
      style={{ backgroundColor: "#0f172a" }}
    >
      <div className="w-full max-w-xl text-center">
        <h1 className="bg-gradient-to-r from-indigo-400 via-indigo-500 to-purple-500 bg-clip-text text-6xl font-bold tracking-tight text-transparent md:text-7xl">
          ShadowSpend
        </h1>
        <p className="mt-6 text-xl font-medium text-gray-200 md:text-2xl">
          See every subscription. Stop every surprise charge.
        </p>
        <p className="mt-3 text-sm text-gray-400">
          Connect your Gmail to discover hidden recurring charges
        </p>
        <button
          onClick={handleConnect}
          className="mt-10 rounded-xl bg-gradient-to-r from-indigo-500 to-indigo-600 px-8 py-4 text-base font-semibold text-white shadow-lg shadow-indigo-500/30 transition-transform duration-150 hover:scale-[1.02] active:scale-[0.97]"
        >
          Connect Gmail
        </button>
      </div>
    </div>
  );
}
