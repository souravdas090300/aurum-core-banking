import { useEffect, useState } from 'react';
import { transactionService } from '../services/bankingService';
import type { Transaction, PaginatedResponse } from '../types';
import { Filter, Download } from 'lucide-react';
import toast from 'react-hot-toast';

export const Transactions = () => {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [pagination, setPagination] = useState({ page: 0, size: 10, totalPages: 0 });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadTransactions();
  }, [pagination.page]);

  const loadTransactions = async () => {
    try {
      const response = await transactionService.getAll(pagination.page, pagination.size);
      setTransactions(response.data.content);
      setPagination((prev) => ({ ...prev, totalPages: response.data.totalPages }));
    } catch (error) {
      toast.error('Failed to load transactions');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'COMPLETED':
        return 'status-completed';
      case 'PENDING':
        return 'status-pending';
      case 'FAILED':
        return 'status-failed';
      case 'REVERSED':
        return 'status-reversed';
      default:
        return '';
    }
  };

  const getTypeColor = (type: string) => {
    switch (type) {
      case 'DEPOSIT':
        return 'type-deposit';
      case 'WITHDRAWAL':
        return 'type-withdrawal';
      case 'TRANSFER':
        return 'type-transfer';
      case 'PAYMENT':
        return 'type-payment';
      default:
        return '';
    }
  };

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  return (
    <div className="transactions-page">
      <div className="page-header">
        <h2 className="page-title">Transaction History</h2>
        <div className="page-actions">
          <button className="btn btn-secondary">
            <Filter size={20} />
            <span>Filter</span>
          </button>
          <button className="btn btn-secondary">
            <Download size={20} />
            <span>Export</span>
          </button>
        </div>
      </div>

      <div className="card">
        <div className="table-container">
          <table className="table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Type</th>
                <th>Reference</th>
                <th>Description</th>
                <th>Amount</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((transaction) => (
                <tr key={transaction.id}>
                  <td>{new Date(transaction.timestamp).toLocaleString()}</td>
                  <td>
                    <span className={`type-badge ${getTypeColor(transaction.transactionType)}`}>
                      {transaction.transactionType}
                    </span>
                  </td>
                  <td className="text-mono">{transaction.reference}</td>
                  <td>{transaction.description}</td>
                  <td className={`amount ${
                    transaction.transactionType === 'DEPOSIT' ? 'positive' : 'negative'
                  }`}>
                    {transaction.transactionType === 'DEPOSIT' ? '+' : '-'}
                    {transaction.currency} ${transaction.amount.toLocaleString()}
                  </td>
                  <td>
                    <span className={`status-badge ${getStatusColor(transaction.status)}`}>
                      {transaction.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {transactions.length === 0 && (
          <div className="empty-state">
            <p>No transactions found</p>
          </div>
        )}

        {pagination.totalPages > 1 && (
          <div className="pagination">
            <button
              className="btn btn-secondary btn-sm"
              onClick={() => setPagination((prev) => ({ ...prev, page: prev.page - 1 }))}
              disabled={pagination.page === 0}
            >
              Previous
            </button>
            <span className="pagination-info">
              Page {pagination.page + 1} of {pagination.totalPages}
            </span>
            <button
              className="btn btn-secondary btn-sm"
              onClick={() => setPagination((prev) => ({ ...prev, page: prev.page + 1 }))}
              disabled={pagination.page >= pagination.totalPages - 1}
            >
              Next
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
