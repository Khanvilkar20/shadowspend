import type { Subscription } from "@/lib/types";
import type { Insights } from "@/lib/api";
import { Wallet, CalendarClock, ListChecks, CalendarDays } from "lucide-react";

function isWithinNextDays(date: string | null, days: number): boolean {
  if (!date) return false;
  const d = new Date(date).getTime();
  if (Number.isNaN(d)) return false;
  const now = Date.now();
  return d >= now && d <= now + days * 24 * 60 * 60 * 1000;
}

function monthlyAmount(sub: Subscription): number {
  const cycle = (sub.billingCycle || "").toLowerCase();
  if (cycle === "yearly" || cycle === "annual") return sub.amount / 12;
  if (cycle === "weekly") return sub.amount * 4;
  return sub.amount;
}

export function SummaryCards({
  subs,
  insights,
}: {
  subs: Subscription[] | null;
  insights?: Insights | null;
}) {
  const list = (subs ?? []).filter(
    (s) => (s.classification || "").toUpperCase() !== "UNKNOWN",
  );
  const totalMonthly =
    insights?.totalMonthlySpend ??
    list.reduce((acc, s) => acc + monthlyAmount(s), 0);
  const upcoming =
    insights?.upcomingThisWeek ??
    list.filter((s) => isWithinNextDays(s.nextBillingDate, 7)).length;
  const active =
    insights?.activeSubscriptions ??
    list.filter((s) => s.isActive).length;
  const upcomingMonth = list.filter((s) =>
    isWithinNextDays(s.nextBillingDate, 30),
  ).length;

  const cards = [
    {
      label: "Total Monthly Spend",
      value: `₹${Math.round(totalMonthly ?? 0).toLocaleString("en-IN")}`,
      icon: Wallet,
    },
    { label: "Upcoming This Week", value: `${upcoming ?? 0} items`, icon: CalendarClock },
    { label: "Active Subscriptions", value: `${active ?? 0} items`, icon: ListChecks },
    { label: "Upcoming This Month", value: `${upcomingMonth} items`, icon: CalendarDays },
  ];

  return (
    <div className="mb-8 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
      {cards.map((c, i) => (
        <div
          key={c.label}
          className="animate-fade-rise rounded-xl p-5 shadow-md ring-1 ring-white/5 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lg"
          style={{ backgroundColor: "#111827", animationDelay: `${i * 60}ms` }}
        >
          <div className="mb-2 flex items-center justify-between">
            <span className="text-xs font-medium uppercase tracking-wide text-gray-400">
              {c.label}
            </span>
            <c.icon className="h-4 w-4 text-indigo-400" />
          </div>
          <p className="text-2xl font-bold text-white">{c.value}</p>
        </div>
      ))}
    </div>
  );
}