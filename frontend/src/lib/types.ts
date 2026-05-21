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