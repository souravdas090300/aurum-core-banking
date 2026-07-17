import { apiClient, authHeader } from "./client";
import {
  Account,
  Transaction,
  TransferRequest,
  LoanApplicationRequest,
  LoanReviewRequest,
  LoanTask,
  PageResponse,
} from "../types";

export async function fetchAccounts(token?: string): Promise<Account[]> {
  const response = await apiClient.get<Account[]>("/api/v1/accounts", {
    headers: authHeader(token),
  });
  return response.data;
}

export async function fetchTransactions(token?: string): Promise<Transaction[]> {
  const response = await apiClient.get<PageResponse<Transaction>>("/api/v1/transactions", {
    headers: authHeader(token),
  });
  return response.data.content;
}

export async function submitTransfer(request: TransferRequest, token?: string) {
  const response = await apiClient.post("/api/v1/transfers", request, {
    headers: authHeader(token),
  });
  return response.data;
}

export async function applyLoan(request: LoanApplicationRequest, token?: string) {
  const response = await apiClient.post("/api/v1/loans/applications", request, {
    headers: authHeader(token),
  });
  return response.data;
}

export async function fetchLoanTasks(token?: string): Promise<LoanTask[]> {
  const response = await apiClient.get<LoanTask[]>("/api/v1/loans/tasks/queue", {
    headers: authHeader(token),
  });
  return response.data;
}

export async function reviewLoanTask(
  taskId: number,
  request: LoanReviewRequest,
  token?: string
) {
  await apiClient.post(`/api/v1/loans/tasks/${taskId}/review`, request, {
    headers: authHeader(token),
  });
}
