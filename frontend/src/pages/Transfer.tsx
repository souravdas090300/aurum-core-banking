import { useState, useEffect } from 'react';
import { accountService, transactionService } from '../services/bankingService';
import type { Account, TransferRequest } from '../types';
import { ArrowRightLeft } from 'lucide-react';
import toast from 'react-hot-toast';

export const Transfer = () => {
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState<TransferRequest>({
    fromAccountId: '',
    toAccountId: '',
    amount: 0,
    currency: 'USD',
    description: '',
  });

  useEffect(() => {
    loadAccounts();
  }, []);

  const loadAccounts = async () => {
    try {
      const response = await accountService.getAll();
      setAccounts(response.data);
    } catch (error) {
      toast.error('Failed to load accounts');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!formData.fromAccountId || !formData.toAccountId) {
      toast.error('Please select both accounts');
      return;
    }

    if (formData.fromAccountId === formData.toAccountId) {
      toast.error('Cannot transfer to the same account');
      return;
    }

    if (formData.amount <= 0) {
      toast.error('Amount must be greater than 0');
      return;
    }

    setLoading(true);
    try {
      await transactionService.transfer(formData);
      toast.success('Transfer completed successfully!');
      setFormData({
        fromAccountId: '',
        toAccountId: '',
        amount: 0,
        currency: 'USD',
        description: '',
      });
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Transfer failed');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'amount' ? parseFloat(value) || 0 : value,
    }));
  };

  return (
    <div className="transfer-page">
      <h2 className="page-title">Transfer Money</h2>

      <div className="transfer-container">
        <div className="card">
          <div className="card-icon">
            <ArrowRightLeft size={32} />
          </div>
          <h3 className="card-title">New Transfer</h3>

          <form onSubmit={handleSubmit} className="transfer-form">
            <div className="form-group">
              <label htmlFor="fromAccountId">From Account</label>
              <select
                id="fromAccountId"
                name="fromAccountId"
                value={formData.fromAccountId}
                onChange={handleChange}
                className="form-control"
                required
              >
                <option value="">Select Account</option>
                {accounts.map((account) => (
                  <option key={account.id} value={account.id}>
                    {account.accountNumber} - {account.accountType} (${account.balance.toLocaleString()})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="toAccountId">To Account</label>
              <select
                id="toAccountId"
                name="toAccountId"
                value={formData.toAccountId}
                onChange={handleChange}
                className="form-control"
                required
              >
                <option value="">Select Account</option>
                {accounts.map((account) => (
                  <option key={account.id} value={account.id}>
                    {account.accountNumber} - {account.accountType}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="amount">Amount</label>
              <input
                type="number"
                id="amount"
                name="amount"
                value={formData.amount || ''}
                onChange={handleChange}
                className="form-control"
                placeholder="0.00"
                step="0.01"
                min="0"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="currency">Currency</label>
              <select
                id="currency"
                name="currency"
                value={formData.currency}
                onChange={handleChange}
                className="form-control"
                required
              >
                <option value="USD">USD</option>
                <option value="EUR">EUR</option>
                <option value="GBP">GBP</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="description">Description</label>
              <textarea
                id="description"
                name="description"
                value={formData.description}
                onChange={handleChange}
                className="form-control"
                placeholder="Transfer description..."
                rows={3}
              />
            </div>

            <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
              {loading ? 'Processing...' : 'Transfer Money'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
