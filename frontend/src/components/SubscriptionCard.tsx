import type { Subscription } from "@/lib/types";

function classificationStyle(classification: string): string {
  const c = classification.toUpperCase();
  if (c === "CONFIRMED") return "bg-green-500/20 text-green-400";
  if (c === "LIKELY") return "bg-blue-500/20 text-blue-400";
  if (c === "POTENTIAL" || c === "POSSIBLE") return "bg-yellow-500/20 text-yellow-400";
  return "bg-gray-500/20 text-gray-300";
}

function confidenceColor(score: number): string {
  if (score > 0.8) return "bg-green-500";
  if (score > 0.5) return "bg-yellow-500";
  return "bg-red-500";
}

function formatDate(date: string | null): string {
  if (!date) return "—";
  try {
    return new Date(date).toLocaleDateString("en-IN", {
      year: "numeric",
      month: "short",
      day: "numeric",
    });
  } catch {
    return date;
  }
}

export function SubscriptionCard({ sub }: { sub: Subscription }) {
  const initial = (sub.merchantName?.[0] ?? "?").toUpperCase();
  const score = sub.confidenceScore ?? 0;
  return (
    <div
      className="rounded-xl p-5 shadow-md ring-1 ring-white/10 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lg"
      style={{ backgroundColor: "#111827" }}
    >
      <div className="flex items-center gap-4">
        <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-indigo-500 text-base font-semibold text-white">
          {initial}
        </div>
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-2">
            <h3 className="truncate font-semibold text-white">{sub.merchantName}</h3>
            <span className="rounded-md bg-white/10 px-2 py-0.5 text-[10px] font-medium uppercase tracking-wide text-gray-300">
              {sub.billingCycle}
            </span>
          </div>
          <p className="truncate text-sm text-gray-400">{sub.category}</p>
        </div>
        <div className="flex flex-col items-end gap-1">
          <p className="text-lg font-bold text-indigo-400">
            ₹{sub.amount.toLocaleString("en-IN")}
          </p>
          <span
            className={`rounded-full px-2 py-0.5 text-[10px] font-medium ${classificationStyle(sub.classification)}`}
          >
            {sub.classification}
          </span>
        </div>
      </div>

      <div className="mt-4 flex items-center justify-between text-xs text-gray-400">
        <span>Next billing</span>
        <span className="text-gray-200">{formatDate(sub.nextBillingDate)}</span>
      </div>

      <div className="mt-3">
        <div className="mb-1 flex justify-between text-[10px] text-gray-500">
          <span>Confidence</span>
          <span>{Math.round(score * 100)}%</span>
        </div>
        <div className="h-1.5 w-full overflow-hidden rounded-full bg-white/10">
          <div
            className={`h-full ${confidenceColor(score)} transition-all duration-300`}
            style={{ width: `${Math.max(0, Math.min(1, score)) * 100}%` }}
          />
        </div>
      </div>
    </div>
  );
}