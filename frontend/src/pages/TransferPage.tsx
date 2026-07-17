import { useEffect, useState } from "react";
import { useKeycloak } from "@react-keycloak/web";
import toast from "react-hot-toast";
import { fetchAccounts, submitTransfer } from "../api/banking";
import { Account, TransferRequest } from "../types";
import Loading from "../components/Loading";

function TransferPage() {
  const { keycloak } = useKeycloak();
  const token = keycloak?.token;
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({
    fromAccountId: "",
    toAccountId: "",
    amount: "",
    currency: "EUR",
    reference: "Transfer funds",
  });

  useEffect(() => {
    async function loadAccounts() {
      try {
        const accountData = await fetchAccounts(token);
        setAccounts(accountData);
        if (accountData.length > 0) {
          setForm((current) => ({ ...current, fromAccountId: current.fromAccountId || accountData[0].id }));
        }
      } catch (error) {
        toast.error("Unable to load accounts for transfers.");
      } finally {
        setLoading(false);
      }
    }
    loadAccounts();
  }, [token]);

  const handleChange = (event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!form.fromAccountId || !form.toAccountId) {
      toast.error("Please choose both debit and destination accounts.");
      return;
    }

    const request: TransferRequest = {
      idempotencyKey: crypto.randomUUID(),
      fromAccountId: form.fromAccountId,
      toAccountId: form.toAccountId,
      amount: Number(form.amount),
      currency: form.currency,
      reference: form.reference,
    };

    try {
      await submitTransfer(request, token);
      toast.success("Transfer submitted successfully.");
      setForm((current) => ({ ...current, amount: "", reference: "Transfer funds" }));
    } catch (error) {
      toast.error("Failed to submit transfer. Check account status and balance.");
    }
  };

  if (loading) {
    return <Loading message="Loading transfer accounts…" />;
  }

  return (
    <div className="app-card">
      <h2>New Transfer</h2>
      <form className="form-grid" onSubmit={handleSubmit}>
        <label>
          From account
          <select name="fromAccountId" value={form.fromAccountId} onChange={handleChange}>
            <option value="">Choose account</option>
            {accounts.map((account) => (
              <option key={account.id} value={account.id}>
                {account.accountNumber} · {account.balance.toFixed(2)} {account.currency}
              </option>
            ))}
          </select>
        </label>
        <label>
          To account
          <input
            name="toAccountId"
            value={form.toAccountId}
            onChange={handleChange}
            placeholder="Enter destination account ID"
          />
        </label>
        <label>
          Amount
          <input
            name="amount"
            value={form.amount}
            onChange={handleChange}
            type="number"
            min="1"
            step="0.01"
          />
        </label>
        <label>
          Currency
          <select name="currency" value={form.currency} onChange={handleChange}>
            <option value="EUR">EUR</option>
            <option value="USD">USD</option>
          </select>
        </label>
        <label>
          Reference
          <input name="reference" value={form.reference} onChange={handleChange} />
        </label>
        <button type="submit">Send transfer</button>
      </form>
    </div>
  );
}

export default TransferPage;
