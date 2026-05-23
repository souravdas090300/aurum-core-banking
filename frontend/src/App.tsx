import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ReactKeycloakProvider } from '@react-keycloak/web';
import { Toaster } from 'react-hot-toast';
import keycloak from './config/keycloak';
import { AuthProvider } from './context/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import { Layout } from './components/Layout';
import { Dashboard } from './pages/Dashboard';
import { Accounts } from './pages/Accounts';
import { Transfer } from './pages/Transfer';
import { Transactions } from './pages/Transactions';
import { Loans } from './pages/Loans';
import { Profile } from './pages/Profile';
import { Login } from './pages/Login';
import { Unauthorized } from './pages/Unauthorized';
import './App.css';

const initOptions = {
  onLoad: 'check-sso' as const,
  checkLoginIframe: false,
  pkceMethod: 'S256' as const,
  enableLogging: true,
};

function App() {
  return (
    <ReactKeycloakProvider 
      authClient={keycloak} 
      initOptions={initOptions}
      LoadingComponent={<div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>Loading...</div>}
    >
      <AuthProvider>
        <Router>
          <Toaster position="top-right" />
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/unauthorized" element={<Unauthorized />} />
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <Layout>
                    <Dashboard />
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/accounts"
              element={
                <ProtectedRoute>
                  <Layout>
                    <Accounts />
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/transfer"
              element={
                <ProtectedRoute>
                  <Layout>
                    <Transfer />
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/transactions"
              element={
                <ProtectedRoute>
                  <Layout>
                    <Transactions />
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/loans"
              element={
                <ProtectedRoute>
                  <Layout>
                    <Loans />
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route
              path="/profile"
              element={
                <ProtectedRoute>
                  <Layout>
                    <Profile />
                  </Layout>
                </ProtectedRoute>
              }
            />
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </Router>
      </AuthProvider>
    </ReactKeycloakProvider>
  );
}

export default App;
