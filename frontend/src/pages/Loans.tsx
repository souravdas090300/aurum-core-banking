import { useEffect, useState } from 'react';
import { loanService } from '../services/bankingService';
import type { Loan } from '../types';
import { FileText, Plus } from 'lucide-react';
import toast from 'react-hot-toast';

export const Loans = () => {
  const [loans, setLoans] = useState<Loan[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadLoans();
  }, []);

  const loadLoans = async () => {
    try {
      const response = await loanService.getAll();
      setLoans(response.data);
    } catch (error) {
      toast.error('Failed to load loans');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE':
        return 'status-active';
      case 'APPROVED':
        return 'status-approved';
      case 'PENDING':
        return 'status-pending';
      case 'PAID_OFF':
        return 'status-paid';
      case 'DEFAULTED':
        return 'status-defaulted';
      default:
        return '';
    }
  };

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  return (
    <div className="loans-page">
      <div className="page-header">
        <h2 className="page-title">My Loans</h2>
        <button className="btn btn-primary">
          <Plus size={20} />
          <span>Apply for Loan</span>
        </button>
      </div>

      <div className="loans-grid">
        {loans.map((loan) => (
          <div key={loan.id} className="loan-card">
            <div className="loan-card-header">
              <FileText size={24} />
              <span className={`status-badge ${getStatusColor(loan.status)}`}>
                {loan.status}
              </span>
            </div>
            <div className="loan-card-body">
              <div className="loan-info-row">
                <span className="loan-label">Principal Amount</span>
                <span className="loan-value">${loan.principalAmount.toLocaleString()}</span>
              </div>
              <div className="loan-info-row">
                <span className="loan-label">Remaining Balance</span>
                <span className="loan-value">${loan.remainingBalance.toLocaleString()}</span>
              </div>
              <div className="loan-info-row">
                <span className="loan-label">Interest Rate</span>
                <span className="loan-value">{loan.interestRate}%</span>
              </div>
              <div className="loan-info-row">
                <span className="loan-label">Monthly Payment</span>
                <span className="loan-value">${loan.monthlyPayment.toLocaleString()}</span>
              </div>
              <div className="loan-info-row">
                <span className="loan-label">Next Payment</span>
                <span className="loan-value">
                  {new Date(loan.nextPaymentDate).toLocaleDateString()}
                </span>
              </div>
              <div className="loan-info-row">
                <span className="loan-label">Term</span>
                <span className="loan-value">{loan.termMonths} months</span>
              </div>
            </div>
            <div className="loan-card-footer">
              <button className="btn btn-secondary btn-sm">Make Payment</button>
              <button className="btn btn-secondary btn-sm">View Details</button>
            </div>
          </div>
        ))}
      </div>

      {loans.length === 0 && (
        <div className="empty-state">
          <FileText size={48} />
          <p>No loans found</p>
          <button className="btn btn-primary">Apply for your first loan</button>
        </div>
      )}
    </div>
  );
};
