export interface Subscription {
  id: string;
  merchantName: string;
  category: string;
  amount: number;
  currency: string;
  billingCycle: string;
  classification: string;
  confidenceScore: number;
  nextBillingDate: string | null;
  isActive: boolean;
}

export interface SubscriptionEmail {
  id: string;
  subject: string;
  sender: string;
  snippet: string;
  rawBody: string;
  receivedAt: string;
  amount: number;
}