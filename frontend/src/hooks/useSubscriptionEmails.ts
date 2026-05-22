import { useCallback, useEffect, useState } from "react";
import { getSubscriptionEmails } from "@/lib/api";
import type { SubscriptionEmail } from "@/lib/types";

export function useSubscriptionEmails(subscriptionId: string | null, enabled: boolean) {
  const [emails, setEmails] = useState<SubscriptionEmail[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchEmails = useCallback(async () => {
    if (!subscriptionId) return;
    setIsLoading(true);
    setError(null);
    try {
      const data = await getSubscriptionEmails(subscriptionId);
      setEmails(data);
    } catch (e: unknown) {
      setEmails([]);
      setError(e instanceof Error ? e.message : "Failed to load emails");
    } finally {
      setIsLoading(false);
    }
  }, [subscriptionId]);

  useEffect(() => {
    if (!enabled || !subscriptionId) {
      setEmails([]);
      setError(null);
      setIsLoading(false);
      return;
    }
    void fetchEmails();
  }, [enabled, subscriptionId, fetchEmails]);

  return { emails, isLoading, error, refetch: fetchEmails };
}
