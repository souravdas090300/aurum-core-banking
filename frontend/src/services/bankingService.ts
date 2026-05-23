import api from './api';
import type { 
  Account, 
  Transaction, 
  TransferRequest, 
  Customer, 
  Loan,
  DashboardStats,
  PaginatedResponse 
} from '../types';

// Account services
export const accountService = {
  getAll: () => api.get<Account[]>('/accounts'),
  getById: (id: string) => api.get<Account>(`/accounts/${id}`),
  create: (account: Partial<Account>) => api.post<Account>('/accounts', account),
  update: (id: string, account: Partial<Account>) => api.put<Account>(`/accounts/${id}`, account),
  delete: (id: string) => api.delete(`/accounts/${id}`),
  getBalance: (id: string) => api.get<number>(`/accounts/${id}/balance`),
};

// Transaction services
export const transactionService = {
  getAll: (page = 0, size = 10) => 
    api.get<PaginatedResponse<Transaction>>(`/transactions?page=${page}&size=${size}`),
  getById: (id: string) => api.get<Transaction>(`/transactions/${id}`),
  getByAccountId: (accountId: string, page = 0, size = 10) => 
    api.get<PaginatedResponse<Transaction>>(`/transactions/account/${accountId}?page=${page}&size=${size}`),
  transfer: (transfer: TransferRequest) => api.post<Transaction>('/transactions/transfer', transfer),
  deposit: (accountId: string, amount: number) => 
    api.post<Transaction>('/transactions/deposit', { accountId, amount }),
  withdraw: (accountId: string, amount: number) => 
    api.post<Transaction>('/transactions/withdraw', { accountId, amount }),
};

// Customer services
export const customerService = {
  getProfile: () => api.get<Customer>('/customers/me'),
  update: (customer: Partial<Customer>) => api.put<Customer>('/customers/me', customer),
  getAll: (page = 0, size = 10) => 
    api.get<PaginatedResponse<Customer>>(`/customers?page=${page}&size=${size}`),
};

// Loan services
export const loanService = {
  getAll: () => api.get<Loan[]>('/loans'),
  getById: (id: string) => api.get<Loan>(`/loans/${id}`),
  apply: (loan: Partial<Loan>) => api.post<Loan>('/loans/apply', loan),
  makePayment: (loanId: string, amount: number) => 
    api.post(`/loans/${loanId}/payment`, { amount }),
};

// Dashboard services
export const dashboardService = {
  getStats: () => api.get<DashboardStats>('/dashboard/stats'),
  getRecentTransactions: (limit = 5) => 
    api.get<Transaction[]>(`/dashboard/recent-transactions?limit=${limit}`),
};
