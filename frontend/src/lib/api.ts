import type { Subscription } from "./types";

const API_URL = "http://localhost:8080";

export async function getSubscriptions(userId: string): Promise<Subscription[]> {
  const response = await fetch(`${API_URL}/api/subscriptions/${userId}`);
  if (!response.ok) throw new Error("Failed to fetch subscriptions");
  return response.json();
}

export interface ScanStatus {
  status: "running" | "completed" | "failed";
  emailsFetched: number;
  emailsParsed: number;
  subscriptionsFound: number;
}

export async function startScan(userId: string): Promise<{ scanId: string }> {
  const response = await fetch(`${API_URL}/api/scan`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ userId }),
  });
  if (!response.ok) throw new Error("Failed to start scan");
  return response.json();
}

export async function getScanStatus(scanId: string): Promise<ScanStatus> {
  const response = await fetch(`${API_URL}/api/scan/status/${scanId}`);
  if (!response.ok) throw new Error("Failed to fetch scan status");
  return response.json();
}

export interface Insights {
  totalMonthlySpend: number;
  upcomingThisWeek: number;
  activeSubscriptions: number;
  unknownCharges: number;
  topServices: { name: string; amount: number }[];
  categoryBreakdown:
    | { category: string; amount: number }[]
    | Record<string, number>;
}

export async function getInsights(userId: string): Promise<Insights> {
  const response = await fetch(`${API_URL}/api/insights/${userId}`);
  if (!response.ok) throw new Error("Failed to fetch insights");
  return response.json();
}