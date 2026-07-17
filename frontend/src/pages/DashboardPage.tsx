import { useEffect, useState } from "react";
import { useKeycloak } from "@react-keycloak/web";
import { fetchAccounts, fetchTransactions } from "../api/banking";
import { Account, Transaction } from "../types";
import Loading from "../components/Loading";

function DashboardPage() {
  const { keycloak } = useKeycloak();
  const token = keycloak?.token;
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function load() {
      try {
        const [accountData, transactionData] = await Promise.all([
          fetchAccounts(token),
          fetchTransactions(token),
        ]);
        setAccounts(accountData);
        setTransactions(transactionData.slice(0, 6));
      } catch (err) {
        setError("Unable to load accounts and transactions. Check backend connection.");
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [token]);

  if (loading) {
    return <Loading message="Loading your banking dashboard…" />;
  }

  if (error) {
    return <div className="app-card"><p>{error}</p></div>;
  }

  return (
    <div className="app-content">
      <section className="app-card">
        <h2>Accounts</h2>
        {accounts.length === 0 ? (
          <p>No accounts found. Please create an account in the backend or log in with an account that has access.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Account</th>
                <th>Type</th>
                <th>Status</th>
                <th>Balance</th>
                <th>Currency</th>
              </tr>
            </thead>
            <tbody>
              {accounts.map((account) => (
                <tr key={account.id}>
                  <td>{account.accountNumber}</td>
                  <td>{account.accountType}</td>
                  <td>{account.status}</td>
                  <td>{account.balance.toFixed(2)}</td>
                  <td>{account.currency}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>

      <section className="app-card">
        <h2>Recent transactions</h2>
        {transactions.length === 0 ? (
          <p>No recent transactions available.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Type</th>
                <th>Amount</th>
                <th>Currency</th>
                <th>Reference</th>
                <th>Status</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((transaction) => (
                <tr key={transaction.id}>
                  <td>{transaction.transactionType}</td>
                  <td>{transaction.amount.toFixed(2)}</td>
                  <td>{transaction.currency}</td>
                  <td>{transaction.reference || "—"}</td>
                  <td>{transaction.status}</td>
                  <td>{new Date(transaction.executedAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}

export default DashboardPage;
