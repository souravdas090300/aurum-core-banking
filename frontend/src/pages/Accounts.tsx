import { useEffect, useState } from 'react';
import { accountService } from '../services/bankingService';
import type { Account } from '../types';
import { Plus, Eye } from 'lucide-react';
import toast from 'react-hot-toast';

export const Accounts = () => {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadAccounts();
  }, []);

  const loadAccounts = async () => {
    try {
      const response = await accountService.getAll();
      setAccounts(response.data);
    } catch (error) {
      toast.error('Failed to load accounts');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'status-active';
      case 'INACTIVE':
        return 'status-inactive';
      case 'FROZEN':
        return 'status-frozen';
      case 'CLOSED':
        return 'status-closed';
      default:
        return '';
    }
  };

  const getAccountTypeColor = (type: string) => {
    switch (type) {
      case 'SAVINGS':
        return 'type-savings';
      case 'CHECKING':
        return 'type-checking';
      case 'LOAN':
        return 'type-loan';
      case 'CREDIT':
        return 'type-credit';
      default:
        return '';
    }
  };

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  return (
    <div className="accounts-page">
      <div className="page-header">
        <h2 className="page-title">My Accounts</h2>
        <button className="btn btn-primary">
          <Plus size={20} />
          <span>New Account</span>
        </button>
      </div>

      <div className="accounts-grid">
        {accounts.map((account) => (
          <div key={account.id} className="account-card">
            <div className="account-card-header">
              <span className={`account-type-badge ${getAccountTypeColor(account.accountType)}`}>
                {account.accountType}
              </span>
              <span className={`status-badge ${getStatusColor(account.status)}`}>
                {account.status}
              </span>
            </div>
            <div className="account-card-body">
              <p className="account-number-label">Account Number</p>
              <h3 className="account-number-value">{account.accountNumber}</h3>
              <div className="account-balance-section">
                <p className="balance-label">Available Balance</p>
                <h2 className="balance-value">
                  {account.currency} ${account.balance.toLocaleString()}
                </h2>
              </div>
            </div>
            <div className="account-card-footer">
              <button className="btn btn-secondary btn-sm">
                <Eye size={16} />
                <span>View Details</span>
              </button>
            </div>
          </div>
        ))}
      </div>

      {accounts.length === 0 && (
        <div className="empty-state">
          <p>No accounts found</p>
          <button className="btn btn-primary">Create your first account</button>
        </div>
      )}
    </div>
  );
};
