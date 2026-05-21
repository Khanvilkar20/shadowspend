import { createFileRoute } from "@tanstack/react-router";
import { useEffect, useState } from "react";

export const Route = createFileRoute("/auth/callback")({
  component: AuthCallbackPage,
  head: () => ({
    meta: [{ title: "Connecting Gmail — ShadowSpend" }],
  }),
});

function AuthCallbackPage() {
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const code = new URLSearchParams(window.location.search).get("code");
    if (!code) {
      setError("Authentication failed. No code received.");
      return;
    }

    let cancelled = false;
    (async () => {
      try {
        const res = await fetch("http://localhost:8080/api/auth/google", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ code }),
        });
        if (!res.ok) throw new Error(`Request failed (${res.status})`);
        const data = await res.json();
        if (cancelled) return;
        localStorage.setItem("userId", data.userId);
        localStorage.setItem("email", data.email);
        localStorage.setItem("name", data.name);
        window.location.href = "/dashboard";
      } catch (e: unknown) {
        if (cancelled) return;
        setError(e instanceof Error ? e.message : "Authentication failed.");
      }
    })();

    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <div
      className="flex min-h-screen flex-col items-center justify-center px-6 text-white"
      style={{ backgroundColor: "#0f172a" }}
    >
      {!error ? (
        <div className="flex flex-col items-center gap-4">
          <div className="h-10 w-10 animate-spin rounded-full border-2 border-indigo-400/30 border-t-indigo-400" />
          <p className="text-sm text-gray-300">Connecting to Gmail...</p>
        </div>
      ) : (
        <div className="w-full max-w-md rounded-xl bg-red-500/10 p-6 text-center ring-1 ring-red-500/30">
          <p className="font-semibold text-red-400">Authentication failed</p>
          <p className="mt-2 text-sm text-red-300/80">{error}</p>
          <button
            onClick={() => {
              window.location.href = "/";
            }}
            className="mt-5 rounded-xl bg-gradient-to-r from-indigo-500 to-indigo-600 px-5 py-2.5 text-sm font-semibold text-white shadow-md transition-transform duration-150 hover:scale-[1.02] active:scale-[0.97]"
          >
            Try Again
          </button>
        </div>
      )}
    </div>
  );
}