import { useAuth } from '../context/AuthContext';
import { LogIn } from 'lucide-react';

export const Login = () => {
  const { login } = useAuth();

  return (
    <div className="login-page">
      <div className="login-container">
        <div className="login-card">
          <h1 className="login-title">Aurum Core Banking</h1>
          <p className="login-subtitle">Secure Banking Portal</p>
          <button onClick={login} className="btn btn-primary btn-lg">
            <LogIn size={24} />
            <span>Login with Keycloak</span>
          </button>
        </div>
      </div>
    </div>
  );
};
