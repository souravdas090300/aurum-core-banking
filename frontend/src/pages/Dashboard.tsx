import { useEffect, useState } from 'react';
import { dashboardService, accountService } from '../services/bankingService';
import type { DashboardStats, Transaction, Account } from '../types';
import { TrendingUp, TrendingDown, CreditCard, Activity } from 'lucide-react';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import toast from 'react-hot-toast';

export const Dashboard = () => {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [recentTransactions, setRecentTransactions] = useState<Transaction[]>([]);
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      const [statsRes, transactionsRes, accountsRes] = await Promise.all([
        dashboardService.getStats(),
        dashboardService.getRecentTransactions(),
        accountService.getAll(),
      ]);
      setStats(statsRes.data);
      setRecentTransactions(transactionsRes.data);
      setAccounts(accountsRes.data);
    } catch (error) {
      toast.error('Failed to load dashboard data');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  // Mock data for chart
  const chartData = [
    { month: 'Jan', balance: 15000 },
    { month: 'Feb', balance: 18000 },
    { month: 'Mar', balance: 16500 },
    { month: 'Apr', balance: 21000 },
    { month: 'May', balance: 24000 },
    { month: 'Jun', balance: 26500 },
  ];

  return (
    <div className="dashboard">
      <h2 className="page-title">Dashboard</h2>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon">
            <CreditCard size={24} />
          </div>
          <div className="stat-content">
            <p className="stat-label">Total Balance</p>
            <h3 className="stat-value">${stats?.totalBalance.toLocaleString() || 0}</h3>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">
            <Activity size={24} />
          </div>
          <div className="stat-content">
            <p className="stat-label">Total Accounts</p>
            <h3 className="stat-value">{stats?.totalAccounts || 0}</h3>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon green">
            <TrendingUp size={24} />
          </div>
          <div className="stat-content">
            <p className="stat-label">Monthly Income</p>
            <h3 className="stat-value">${stats?.monthlyIncome.toLocaleString() || 0}</h3>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon red">
            <TrendingDown size={24} />
          </div>
          <div className="stat-content">
            <p className="stat-label">Monthly Spending</p>
            <h3 className="stat-value">${stats?.monthlySpending.toLocaleString() || 0}</h3>
          </div>
        </div>
      </div>

      <div className="dashboard-grid">
        <div className="card">
          <h3 className="card-title">Balance Overview</h3>
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip />
              <Area type="monotone" dataKey="balance" stroke="#3b82f6" fill="#3b82f680" />
            </AreaChart>
          </ResponsiveContainer>
        </div>

        <div className="card">
          <h3 className="card-title">Your Accounts</h3>
          <div className="account-list">
            {accounts.map((account) => (
              <div key={account.id} className="account-item">
                <div>
                  <p className="account-type">{account.accountType}</p>
                  <p className="account-number">{account.accountNumber}</p>
                </div>
                <div className="account-balance">
                  ${account.balance.toLocaleString()}
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="card">
          <h3 className="card-title">Recent Transactions</h3>
          <div className="transaction-list">
            {recentTransactions.map((transaction) => (
              <div key={transaction.id} className="transaction-item">
                <div>
                  <p className="transaction-type">{transaction.transactionType}</p>
                  <p className="transaction-date">
                    {new Date(transaction.timestamp).toLocaleDateString()}
                  </p>
                </div>
                <div className={`transaction-amount ${
                  transaction.transactionType === 'DEPOSIT' ? 'positive' : 'negative'
                }`}>
                  {transaction.transactionType === 'DEPOSIT' ? '+' : '-'}
                  ${transaction.amount.toLocaleString()}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
