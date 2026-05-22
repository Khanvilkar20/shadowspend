import type { SubscriptionEmail } from "@/lib/types";
import { cn } from "@/lib/utils";

function formatEmailDate(date: string): string {
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

interface EmailTimelineCardProps {
  email: SubscriptionEmail;
  isExpanded: boolean;
  onToggleExpand: () => void;
}

export function EmailTimelineCard({ email, isExpanded, onToggleExpand }: EmailTimelineCardProps) {
  return (
    <article
      className={cn(
        "rounded-xl border border-slate-800 bg-slate-900 p-4 transition-all duration-200",
        "hover:-translate-y-0.5 hover:border-indigo-500/40 hover:shadow-lg hover:shadow-indigo-500/10",
        isExpanded && "border-indigo-500/50 ring-1 ring-indigo-500/30",
      )}
    >
      <div className="space-y-2">
        <div>
          <p className="text-[10px] font-medium uppercase tracking-wide text-gray-500">Subject</p>
          <p className="font-medium text-white">{email.subject || "—"}</p>
        </div>
        <div>
          <p className="text-[10px] font-medium uppercase tracking-wide text-gray-500">Sender</p>
          <p className="text-sm text-gray-300">{email.sender || "—"}</p>
        </div>
        <div className="flex flex-wrap gap-4 text-sm">
          <div>
            <p className="text-[10px] font-medium uppercase tracking-wide text-gray-500">Date</p>
            <p className="text-gray-200">{formatEmailDate(email.receivedAt)}</p>
          </div>
          <div>
            <p className="text-[10px] font-medium uppercase tracking-wide text-gray-500">Amount</p>
            <p className="font-semibold text-indigo-400">
              ₹{email.amount.toLocaleString("en-IN")}
            </p>
          </div>
        </div>
        <div>
          <p className="text-[10px] font-medium uppercase tracking-wide text-gray-500">Snippet</p>
          <p className="line-clamp-3 text-sm text-gray-400">{email.snippet || "—"}</p>
        </div>
      </div>

      <button
        type="button"
        onClick={onToggleExpand}
        className="mt-4 rounded-lg border border-slate-700 bg-slate-800/80 px-3 py-1.5 text-xs font-medium text-gray-200 transition-colors hover:border-indigo-500/50 hover:bg-indigo-500/10 hover:text-white"
      >
        {isExpanded ? "Hide Full Email" : "View Full Email"}
      </button>

      <div
        className={cn(
          "grid transition-all duration-300 ease-in-out",
          isExpanded ? "mt-4 grid-rows-[1fr] opacity-100" : "grid-rows-[0fr] opacity-0",
        )}
      >
        <div className="overflow-hidden">
          <div className="rounded-lg border border-slate-800 bg-slate-950/80 p-4">
            <pre className="whitespace-pre-wrap font-mono text-xs leading-relaxed text-gray-300">
              {email.rawBody || email.snippet || "No email body available."}
            </pre>
          </div>
        </div>
      </div>
    </article>
  );
}
