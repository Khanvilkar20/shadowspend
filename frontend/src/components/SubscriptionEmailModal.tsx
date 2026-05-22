import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Skeleton } from "@/components/ui/skeleton";
import { EmailTimelineCard } from "@/components/EmailTimelineCard";
import { useSubscriptionEmails } from "@/hooks/useSubscriptionEmails";
import type { Subscription } from "@/lib/types";
import { cn } from "@/lib/utils";

function formatBillingCycle(cycle: string): string {
  const c = (cycle || "").toLowerCase();
  if (c === "monthly") return "month";
  if (c === "yearly" || c === "annual") return "year";
  if (c === "weekly") return "week";
  return cycle || "period";
}

function EmailSkeleton() {
  return (
    <div className="rounded-xl border border-slate-800 bg-slate-900 p-4">
      <Skeleton className="mb-2 h-4 w-3/4 bg-white/10" />
      <Skeleton className="mb-3 h-3 w-1/2 bg-white/10" />
      <Skeleton className="mb-2 h-3 w-full bg-white/10" />
      <Skeleton className="h-3 w-4/5 bg-white/10" />
    </div>
  );
}

interface SubscriptionEmailModalProps {
  subscription: Subscription | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
  expandedEmailId: string | null;
  onExpandedEmailIdChange: (id: string | null) => void;
}

export function SubscriptionEmailModal({
  subscription,
  open,
  onOpenChange,
  expandedEmailId,
  onExpandedEmailIdChange,
}: SubscriptionEmailModalProps) {
  const { emails, isLoading, error, refetch } = useSubscriptionEmails(
    subscription?.id ?? null,
    open && subscription != null,
  );

  const initial = (subscription?.merchantName?.[0] ?? "?").toUpperCase();
  const billingLabel = subscription
    ? `₹${subscription.amount.toLocaleString("en-IN")}/${formatBillingCycle(subscription.billingCycle)}`
    : "";

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent
        overlayClassName="bg-black/60 backdrop-blur-md"
        className={cn(
          "max-h-[90vh] max-w-2xl overflow-y-auto border-slate-800 bg-slate-950/95 p-0 text-white backdrop-blur-xl sm:rounded-xl lg:max-w-3xl",
          "[&>button]:text-gray-400 [&>button]:hover:text-white",
        )}
        style={{ backgroundColor: "rgba(2, 6, 23, 0.95)" }}
      >
        <div className="border-b border-slate-800 bg-slate-900/50 p-6 backdrop-blur-sm">
          <DialogHeader className="space-y-4 text-left">
            <div className="flex items-start gap-4">
              <div className="flex h-14 w-14 shrink-0 items-center justify-center rounded-full bg-indigo-500 text-xl font-semibold text-white shadow-lg shadow-indigo-500/20">
                {initial}
              </div>
              <div className="min-w-0 flex-1">
                <DialogTitle className="text-xl font-semibold text-white">
                  {subscription?.merchantName ?? "Subscription"}
                </DialogTitle>
                <p className="mt-1 text-lg font-bold text-indigo-400">{billingLabel}</p>
                <DialogDescription className="mt-1 text-sm text-gray-400">
                  Detected from Gmail receipts
                </DialogDescription>
              </div>
            </div>
          </DialogHeader>
        </div>

        <div className="p-6">
          {isLoading && (
            <div className="space-y-4">
              <EmailSkeleton />
              <EmailSkeleton />
              <EmailSkeleton />
            </div>
          )}

          {!isLoading && error && (
            <div className="rounded-xl border border-red-500/30 bg-red-500/10 p-4 text-center">
              <p className="text-sm text-red-300">{error}</p>
              <button
                type="button"
                onClick={() => void refetch()}
                className="mt-3 rounded-lg bg-indigo-500 px-4 py-1.5 text-xs font-medium text-white transition-colors hover:bg-indigo-600"
              >
                Retry
              </button>
            </div>
          )}

          {!isLoading && !error && emails.length === 0 && (
            <p className="py-12 text-center text-sm text-gray-400">
              No supporting Gmail receipts found.
            </p>
          )}

          {!isLoading && !error && emails.length > 0 && (
            <div className="relative space-y-4 pl-6">
              <div
                className="absolute bottom-2 left-[7px] top-2 w-px bg-gradient-to-b from-indigo-500/50 via-slate-700 to-transparent"
                aria-hidden
              />
              {emails.map((email) => (
                <div key={email.id} className="relative">
                  <div
                    className="absolute -left-6 top-5 h-2.5 w-2.5 rounded-full border-2 border-indigo-500 bg-slate-950"
                    aria-hidden
                  />
                  <EmailTimelineCard
                    email={email}
                    isExpanded={expandedEmailId === email.id}
                    onToggleExpand={() =>
                      onExpandedEmailIdChange(expandedEmailId === email.id ? null : email.id)
                    }
                  />
                </div>
              ))}
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}
