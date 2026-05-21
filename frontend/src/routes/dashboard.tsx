import { createFileRoute } from "@tanstack/react-router";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Sidebar } from "@/components/Sidebar";
import { SubscriptionCard } from "@/components/SubscriptionCard";
import { SummaryCards } from "@/components/SummaryCards";
import { FilterTabs, type FilterKey } from "@/components/FilterTabs";
import { EmptyState } from "@/components/EmptyState";
import {
  getInsights,
  getScanStatus,
  getSubscriptions,
  startScan,
  type Insights,
  type ScanStatus,
} from "@/lib/api";
import type { Subscription } from "@/lib/types";
import { InsightsError, InsightsSection, InsightsSkeleton } from "@/components/InsightsSection";

function getUserId(): string | null {
  if (typeof window === "undefined") return null;
  return window.localStorage.getItem("userId");
}

export const Route = createFileRoute("/dashboard")({
  component: DashboardPage,
  head: () => ({
    meta: [
      { title: "Dashboard — ShadowSpend" },
      { name: "description", content: "Track your subscriptions with ShadowSpend." },
    ],
  }),
});

function SkeletonCard() {
  return (
    <div className="rounded-xl bg-white/5 p-6 shadow-lg ring-1 ring-white/10">
      <div className="mb-3 h-5 w-32 animate-pulse rounded bg-white/10" />
      <div className="mb-4 h-4 w-20 animate-pulse rounded bg-white/10" />
      <div className="mb-4 h-7 w-24 animate-pulse rounded bg-white/10" />
      <div className="space-y-2">
        <div className="h-3 w-full animate-pulse rounded bg-white/10" />
        <div className="h-3 w-full animate-pulse rounded bg-white/10" />
      </div>
    </div>
  );
}

function DashboardPage() {
  const [authChecked, setAuthChecked] = useState(false);
  const [subs, setSubs] = useState<Subscription[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filter, setFilter] = useState<FilterKey>("ALL");

  const [insights, setInsights] = useState<Insights | null>(null);
  const [insightsLoading, setInsightsLoading] = useState(true);
  const [insightsError, setInsightsError] = useState<string | null>(null);

  const [scanId, setScanId] = useState<string | null>(null);
  const [scanStatus, setScanStatus] = useState<"idle" | "running" | "completed" | "failed">("idle");
  const [emailsFetched, setEmailsFetched] = useState(0);
  const [emailsParsed, setEmailsParsed] = useState(0);
  const [subscriptionsFound, setSubscriptionsFound] = useState(0);
  const [scanError, setScanError] = useState<string | null>(null);
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const loadSubs = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const uid = getUserId();
      if (!uid) return;
      const data = await getSubscriptions(uid);
      setSubs(data);
    } catch (e: unknown) {
      setError(e instanceof Error ? e.message : "Unknown error");
    } finally {
      setLoading(false);
    }
  }, []);

  const loadInsights = useCallback(async () => {
    setInsightsLoading(true);
    setInsightsError(null);
    try {
      const uid = getUserId();
      if (!uid) return;
      const data = await getInsights(uid);
      setInsights(data);
    } catch (e: unknown) {
      setInsightsError(e instanceof Error ? e.message : "Unknown error");
    } finally {
      setInsightsLoading(false);
    }
  }, []);

  useEffect(() => {
    if (typeof window === "undefined") return;
    const uid = window.localStorage.getItem("userId");
    if (!uid) {
      window.location.href = "/";
      return;
    }
    setAuthChecked(true);
    loadSubs();
    loadInsights();
  }, [loadSubs, loadInsights]);

  const stopPolling = useCallback(() => {
    if (pollRef.current) {
      clearInterval(pollRef.current);
      pollRef.current = null;
    }
  }, []);

  useEffect(() => {
    return () => stopPolling();
  }, [stopPolling]);

  const applyStatus = useCallback((s: ScanStatus) => {
    setEmailsFetched(s.emailsFetched);
    setEmailsParsed(s.emailsParsed);
    setSubscriptionsFound(s.subscriptionsFound);
    setScanStatus(s.status);
  }, []);

  const startPolling = useCallback(
    (id: string) => {
      stopPolling();
      pollRef.current = setInterval(async () => {
        try {
          const s = await getScanStatus(id);
          applyStatus(s);
          if (s.status === "completed") {
            stopPolling();
            window.location.reload();
          } else if (s.status === "failed") {
            stopPolling();
            setScanStatus("failed");
            setScanError("Scan failed. Please try again.");
            if (typeof window !== "undefined") {
              window.alert("Scan failed");
            }
          }
        } catch (e: unknown) {
          stopPolling();
          setScanStatus("failed");
          setScanError(e instanceof Error ? e.message : "Polling failed");
          console.error(e);
        }
      }, 2000);
    },
    [applyStatus, stopPolling],
  );

  const handleScan = useCallback(async () => {
    setScanError(null);
    setEmailsFetched(0);
    setEmailsParsed(0);
    setSubscriptionsFound(0);
    setScanStatus("running");
    try {
      const uid = getUserId();
      if (!uid) throw new Error("Not authenticated");
      const { scanId: id } = await startScan(uid);
      setScanId(id);
      startPolling(id);
    } catch (e: unknown) {
      setScanStatus("failed");
      setScanError(e instanceof Error ? e.message : "Failed to start scan");
    }
  }, [startPolling]);

  const filtered = useMemo(() => {
    if (!subs) return [];
    const visible = subs.filter(
      (s) => (s.classification || "").toUpperCase() !== "UNKNOWN",
    );
    if (filter === "ALL") return visible;
    return visible.filter((s) => (s.classification || "").toUpperCase() === filter);
  }, [subs, filter]);

  const isScanning = scanStatus === "running";

  if (!authChecked) return null;

  return (
    <div className="min-h-screen text-white" style={{ backgroundColor: "#0f172a" }}>
      <Sidebar />
      <main className="ml-60 h-screen overflow-y-auto p-8">
        <h2 className="mb-2 text-3xl font-bold">Overview</h2>
        <p className="mb-8 text-sm text-gray-400">Your detected subscriptions.</p>

        <SummaryCards subs={subs} insights={insights} />

        {(isScanning || scanStatus === "completed" || scanStatus === "failed") && (
          <div
            className="mb-6 rounded-xl p-5 ring-1 ring-white/5"
            style={{ backgroundColor: "#111827" }}
          >
            {isScanning && (
              <div className="space-y-1">
                <p className="font-semibold text-white">Scanning your Gmail...</p>
                <p className="text-sm text-gray-400">Fetched {emailsFetched} emails</p>
                <p className="text-sm text-gray-400">Parsed {emailsParsed} emails</p>
                {subscriptionsFound > 0 && (
                  <p className="text-sm text-gray-400">
                    Found {subscriptionsFound} subscriptions
                  </p>
                )}
                {scanId && (
                  <p className="mt-2 text-xs text-gray-500">Scan ID: {scanId}</p>
                )}
              </div>
            )}
            {scanStatus === "completed" && (
              <div className="flex items-center justify-between gap-4">
                <div>
                  <p className="font-semibold text-emerald-400">Scan complete!</p>
                  <p className="mt-1 text-sm text-gray-400">
                    Parsed {emailsParsed} emails · Found {subscriptionsFound} subscriptions
                  </p>
                </div>
                <button
                  onClick={() => setScanStatus("idle")}
                  className="rounded-lg px-3 py-1.5 text-xs text-gray-300 ring-1 ring-white/10 hover:bg-white/5"
                >
                  Dismiss
                </button>
              </div>
            )}
            {scanStatus === "failed" && (
              <div className="flex items-center justify-between gap-4">
                <div>
                  <p className="font-semibold text-red-400">Scan failed</p>
                  {scanError && (
                    <p className="mt-1 text-sm text-red-300/80">{scanError}</p>
                  )}
                </div>
                <button
                  onClick={handleScan}
                  className="rounded-xl bg-gradient-to-r from-indigo-500 to-indigo-600 px-4 py-2 text-sm font-semibold text-white shadow-md hover:scale-[1.02] active:scale-[0.97]"
                >
                  Retry
                </button>
              </div>
            )}
          </div>
        )}

        <FilterTabs active={filter} onChange={setFilter} />

        {loading && (
          <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
            <SkeletonCard />
            <SkeletonCard />
            <SkeletonCard />
          </div>
        )}

        {!loading && error && (
          <div className="rounded-xl bg-red-500/10 p-6 ring-1 ring-red-500/30">
            <p className="font-semibold text-red-400">Failed to load subscriptions</p>
            <p className="mt-1 text-sm text-red-300/80">{error}</p>
          </div>
        )}

        {!loading && !error && subs && filtered.length === 0 && (
          <EmptyState onScan={handleScan} scanning={isScanning} />
        )}

        {!loading && !error && subs && filtered.length > 0 && (
          <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
            {filtered.map((s) => (
              <SubscriptionCard key={s.id} sub={s} />
            ))}
          </div>
        )}

        {insightsLoading && <InsightsSkeleton />}
        {!insightsLoading && insightsError && (
          <InsightsError message={insightsError} onRetry={loadInsights} />
        )}
        {!insightsLoading && !insightsError && insights && (
          <InsightsSection insights={insights} />
        )}
      </main>
    </div>
  );
}