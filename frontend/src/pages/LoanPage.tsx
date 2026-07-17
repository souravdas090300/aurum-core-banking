import { useState } from "react";
import { useKeycloak } from "@react-keycloak/web";
import toast from "react-hot-toast";
import { applyLoan } from "../api/banking";
import { LoanApplicationRequest } from "../types";

function LoanPage() {
  const { keycloak } = useKeycloak();
  const token = keycloak?.token;
  const [form, setForm] = useState({
    customerId: "",
    fullName: "",
    amount: "",
    termMonths: "12",
    creditScore: "650",
    debtToIncomeRatio: "0.35",
    monthlyIncome: "5000",
    isPep: false,
  });

  const handleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = event.target;
    setForm((current) => ({
      ...current,
      [name]: type === "checkbox" ? checked : value,
    }));
  };

  const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const request: LoanApplicationRequest = {
      customerId: form.customerId,
      fullName: form.fullName,
      amount: Number(form.amount),
      termMonths: Number(form.termMonths),
      creditScore: Number(form.creditScore),
      debtToIncomeRatio: Number(form.debtToIncomeRatio),
      monthlyIncome: Number(form.monthlyIncome),
      isPep: form.isPep,
    };

    try {
      await applyLoan(request, token);
      toast.success("Loan application submitted.");
      setForm((current) => ({ ...current, amount: "", fullName: "" }));
    } catch (error) {
      toast.error("Unable to submit loan application.");
    }
  };

  return (
    <div className="app-card">
      <h2>Apply for a Loan</h2>
      <form className="form-grid" onSubmit={handleSubmit}>
        <label>
          Customer ID
          <input name="customerId" value={form.customerId} onChange={handleChange} required />
        </label>
        <label>
          Full name
          <input name="fullName" value={form.fullName} onChange={handleChange} required />
        </label>
        <label>
          Loan amount
          <input
            name="amount"
            value={form.amount}
            onChange={handleChange}
            type="number"
            step="0.01"
            required
          />
        </label>
        <label>
          Term (months)
          <input
            name="termMonths"
            value={form.termMonths}
            onChange={handleChange}
            type="number"
            min="6"
            max="360"
            required
          />
        </label>
        <label>
          Credit score
          <input
            name="creditScore"
            value={form.creditScore}
            onChange={handleChange}
            type="number"
            min="300"
            max="850"
            required
          />
        </label>
        <label>
          Debt-to-income ratio
          <input
            name="debtToIncomeRatio"
            value={form.debtToIncomeRatio}
            onChange={handleChange}
            type="number"
            step="0.01"
            min="0"
            max="2"
            required
          />
        </label>
        <label>
          Monthly income
          <input
            name="monthlyIncome"
            value={form.monthlyIncome}
            onChange={handleChange}
            type="number"
            step="0.01"
            required
          />
        </label>
        <label>
          <span>Politically exposed person</span>
          <input
            name="isPep"
            checked={form.isPep}
            onChange={handleChange}
            type="checkbox"
          />
        </label>
        <button type="submit">Submit loan application</button>
      </form>
    </div>
  );
}

export default LoanPage;
