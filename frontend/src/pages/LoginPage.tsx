import { useKeycloak } from "@react-keycloak/web";
import toast from "react-hot-toast";

function LoginPage() {
  const { keycloak } = useKeycloak();

  const handleLogin = async () => {
    try {
      await keycloak?.login();
    } catch (error) {
      toast.error("Login failed. Ensure Keycloak is running on http://localhost:8180.");
    }
  };

  return (
    <div className="app-card">
      <h2>Welcome to Aurum Core Banking</h2>
      <p>Sign in with your Keycloak account to view accounts, initiate transfers, and manage loan applications.</p>
      <button type="button" className="app-button" onClick={handleLogin}>
        Login with Keycloak
      </button>
      <p style={{ marginTop: "16px", color: "#6b7280" }}>
        If you do not have Keycloak running locally, use the backend dev profile or run the setup script.
      </p>
    </div>
  );
}

export default LoginPage;
