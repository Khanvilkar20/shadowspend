import type { Insights } from "@/lib/api";

const CATEGORY_COLORS: Record<string, string> = {
  subscription: "bg-indigo-500",
  bill: "bg-blue-500",
  financial: "bg-emerald-500",
};

const CATEGORIES = ["subscription", "bill", "financial"] as const;

function formatINR(n: number): string {
  return `₹${Math.round(n).toLocaleString("en-IN")}`;
}

export function InsightsSkeleton() {
  return (
    <div className="mt-8 grid grid-cols-1 gap-4 lg:grid-cols-3">
      {[0, 1, 2].map((i) => (
        <div
          key={i}
          className="rounded-xl p-5 ring-1 ring-white/5"
          style={{ backgroundColor: "#111827" }}
        >
          <div className="mb-4 h-4 w-32 animate-pulse rounded bg-white/10" />
          <div className="space-y-3">
            <div className="h-3 w-full animate-pulse rounded bg-white/10" />
            <div className="h-3 w-5/6 animate-pulse rounded bg-white/10" />
            <div className="h-3 w-2/3 animate-pulse rounded bg-white/10" />
          </div>
        </div>
      ))}
    </div>
  );
}

export function InsightsError({ message, onRetry }: { message: string; onRetry: () => void }) {
  return (
    <div className="mt-8 rounded-xl bg-red-500/10 p-6 ring-1 ring-red-500/30">
      <div className="flex items-center justify-between gap-4">
        <div>
          <p className="font-semibold text-red-400">Couldn't load insights</p>
          <p className="mt-1 text-sm text-red-300/80">{message}</p>
        </div>
        <button
          onClick={onRetry}
          className="rounded-lg px-3 py-1.5 text-xs text-gray-200 ring-1 ring-white/10 hover:bg-white/5"
        >
          Retry
        </button>
      </div>
    </div>
  );
}

export function InsightsSection({ insights }: { insights: Insights }) {
  const topServicesRaw = Array.isArray(insights?.topServices) ? insights.topServices : [];
  const top = [...topServicesRaw].sort((a, b) => b.amount - a.amount).slice(0, 5);
  const maxTop = Math.max(1, ...top.map((s) => s.amount));

  const rawBreakdown = insights?.categoryBreakdown;
  const breakdownEntries: { category: string; amount: number }[] = Array.isArray(rawBreakdown)
    ? rawBreakdown
    : rawBreakdown && typeof rawBreakdown === "object"
      ? Object.entries(rawBreakdown).map(([category, amount]) => ({
          category,
          amount: Number(amount) || 0,
        }))
      : [];
  const breakdownMap = new Map(
    breakdownEntries.map((c) => [c.category.toLowerCase(), c.amount]),
  );
  const breakdown = CATEGORIES.map((c) => ({ category: c, amount: breakdownMap.get(c) ?? 0 }));
  const maxCat = Math.max(1, ...breakdown.map((c) => c.amount));

  return (
    <section className="mt-10">
      <h3 className="mb-4 text-xl font-bold text-white">Insights</h3>
      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        {/* Monthly Spend Display */}
        <div
          className="animate-fade-rise rounded-xl p-6 shadow-md ring-1 ring-white/5"
          style={{ backgroundColor: "#111827", animationDelay: "0ms" }}
        >
          <p className="text-xs font-medium uppercase tracking-wide text-gray-400">
            Monthly Spend
          </p>
          <p className="mt-3 text-4xl font-bold text-indigo-400">
            {formatINR(insights.totalMonthlySpend)}
          </p>
          <p className="mt-1 text-sm text-gray-400">per month</p>
        </div>

        {/* Top Services */}
        <div
          className="animate-fade-rise rounded-xl p-6 shadow-md ring-1 ring-white/5"
          style={{ backgroundColor: "#111827", animationDelay: "80ms" }}
        >
          <p className="mb-4 text-xs font-medium uppercase tracking-wide text-gray-400">
            Top Services
          </p>
          {top.length === 0 ? (
            <p className="text-sm text-gray-500">No services yet</p>
          ) : (
            <div className="space-y-3">
              {top.map((s) => {
                const pct = Math.max(4, (s.amount / maxTop) * 100);
                return (
                  <div key={s.name}>
                    <div className="mb-1 flex justify-between text-xs">
                      <span className="truncate text-gray-300">{s.name}</span>
                      <span className="font-mono text-gray-400">{formatINR(s.amount)}</span>
                    </div>
                    <div className="h-2 w-full overflow-hidden rounded-full bg-white/5">
                      <div
                        className="h-full rounded-full bg-gradient-to-r from-indigo-500 to-indigo-400 transition-all duration-500"
                        style={{ width: `${pct}%` }}
                      />
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Category Breakdown */}
        <div
          className="animate-fade-rise rounded-xl p-6 shadow-md ring-1 ring-white/5"
          style={{ backgroundColor: "#111827", animationDelay: "160ms" }}
        >
          <p className="mb-4 text-xs font-medium uppercase tracking-wide text-gray-400">
            Category Breakdown
          </p>
          <div className="space-y-3">
            {breakdown.map((c) => {
              const pct = Math.max(4, (c.amount / maxCat) * 100);
              return (
                <div key={c.category}>
                  <div className="mb-1 flex justify-between text-xs">
                    <span className="capitalize text-gray-300">{c.category}</span>
                    <span className="font-mono text-gray-400">{formatINR(c.amount)}</span>
                  </div>
                  <div className="h-2 w-full overflow-hidden rounded-full bg-white/5">
                    <div
                      className={`h-full rounded-full transition-all duration-500 ${CATEGORY_COLORS[c.category]}`}
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </section>
  );
}
