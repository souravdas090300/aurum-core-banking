import { createContext, useContext, ReactNode } from 'react';
import { useKeycloak } from '@react-keycloak/web';

interface AuthContextType {
  isAuthenticated: boolean;
  username: string | undefined;
  token: string | undefined;
  login: () => void;
  logout: () => void;
  hasRole: (role: string) => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const { keycloak, initialized } = useKeycloak();

  const login = () => {
    keycloak.login();
  };

  const logout = () => {
    keycloak.logout();
  };

  const hasRole = (role: string): boolean => {
    return keycloak.hasRealmRole(role) || keycloak.hasResourceRole(role);
  };

  const value: AuthContextType = {
    isAuthenticated: initialized && keycloak.authenticated || false,
    username: keycloak.tokenParsed?.preferred_username,
    token: keycloak.token,
    login,
    logout,
    hasRole,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};
