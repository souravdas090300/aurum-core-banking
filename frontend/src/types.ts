export type AccountStatus = "ACTIVE" | "FROZEN" | "CLOSED";
export type AccountType = "CURRENT" | "SAVINGS" | "LOAN";

export interface Account {
  id: string;
  accountNumber: string;
  customerId: string;
  accountType: AccountType;
  status: AccountStatus;
  balance: number;
  currency: string;
  creditLimit: number;
}

export interface Transaction {
  id: string;
  transactionType: string;
  accountId: string;
  amount: number;
  currency: string;
  reference: string;
  status: string;
  executedAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface TransferRequest {
  idempotencyKey: string;
  fromAccountId: string;
  toAccountId: string;
  amount: number;
  currency: string;
  reference: string;
}

export interface LoanApplicationRequest {
  customerId: string;
  fullName: string;
  amount: number;
  termMonths: number;
  creditScore: number;
  debtToIncomeRatio: number;
  monthlyIncome: number;
  isPep: boolean;
}

export interface LoanReviewRequest {
  approved: boolean;
  notes: string;
}

export interface LoanTask {
  id: number;
  name: string;
  status: string;
  actualOwner: string;
}

export interface AuditLogEntry {
  id: string;
  entityType: string;
  entityId: string | null;
  action: string;
  performedBy: string;
  oldValue: string | null;
  newValue: string | null;
  ipAddress: string | null;
  traceId: string | null;
  createdAt: string;
}
