// Banking domain types
export interface Account {
  id: string;
  accountNumber: string;
  accountType: 'SAVINGS' | 'CHECKING' | 'LOAN' | 'CREDIT';
  balance: number;
  currency: string;
  status: 'ACTIVE' | 'INACTIVE' | 'FROZEN' | 'CLOSED';
  customerId: string;
  createdAt: string;
  updatedAt: string;
}

export interface Customer {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  address: string;
  dateOfBirth: string;
  kycStatus: 'PENDING' | 'VERIFIED' | 'REJECTED';
  createdAt: string;
}

export interface Transaction {
  id: string;
  transactionType: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER' | 'PAYMENT';
  amount: number;
  currency: string;
  fromAccountId?: string;
  toAccountId?: string;
  description: string;
  status: 'PENDING' | 'COMPLETED' | 'FAILED' | 'REVERSED';
  timestamp: string;
  reference: string;
}

export interface TransferRequest {
  fromAccountId: string;
  toAccountId: string;
  amount: number;
  currency: string;
  description: string;
}

export interface Loan {
  id: string;
  accountId: string;
  principalAmount: number;
  interestRate: number;
  termMonths: number;
  remainingBalance: number;
  status: 'PENDING' | 'APPROVED' | 'ACTIVE' | 'PAID_OFF' | 'DEFAULTED';
  nextPaymentDate: string;
  monthlyPayment: number;
}

export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface DashboardStats {
  totalBalance: number;
  totalAccounts: number;
  recentTransactionsCount: number;
  pendingTransactions: number;
  monthlySpending: number;
  monthlyIncome: number;
}
