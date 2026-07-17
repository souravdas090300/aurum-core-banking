import { NavLink, Route, Routes } from "react-router-dom";
import { useKeycloak } from "@react-keycloak/web";
import { Toaster } from "react-hot-toast";
import DashboardPage from "./pages/DashboardPage";
import TransferPage from "./pages/TransferPage";
import LoanPage from "./pages/LoanPage";
import LoanReviewPage from "./pages/LoanReviewPage";
import NotFoundPage from "./pages/NotFoundPage";
import LoginPage from "./pages/LoginPage";
import "./index.css";

function App() {
  const { keycloak, initialized } = useKeycloak();

  if (!initialized) {
    return <div className="app-shell">Loading Aurum Core Banking…</div>;
  }

  const isLoggedIn = keycloak?.authenticated;

  return (
    <div className="app-shell">
      <header className="app-header">
        <div>
          <h1>Aurum Core Banking</h1>
          <p>Modern digital banking for customers, tellers, and managers.</p>
        </div>
        <nav className="app-nav">
          <NavLink to="/" end>Dashboard</NavLink>
          <NavLink to="/transfer">Transfer</NavLink>
          <NavLink to="/loan">Loan Application</NavLink>
          <NavLink to="/review">Loan Review</NavLink>
          <button
            type="button"
            onClick={() => (isLoggedIn ? keycloak.logout() : keycloak.login())}
            className="app-button"
          >
            {isLoggedIn ? "Logout" : "Login"}
          </button>
        </nav>
      </header>

      <main className="app-content">
        {!isLoggedIn ? (
          <LoginPage />
        ) : (
            <Routes>
              <Route path="/" element={<DashboardPage />} />
              <Route path="/transfer" element={<TransferPage />} />
              <Route path="/loan" element={<LoanPage />} />
              <Route path="/review" element={<LoanReviewPage />} />
              <Route path="*" element={<NotFoundPage />} />
            </Routes>
        )}
      </main>
      <Toaster position="top-right" toastOptions={{ duration: 4000 }} />
    </div>
  );
}

export default App;
