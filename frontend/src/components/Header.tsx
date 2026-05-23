import { useAuth } from '../context/AuthContext';
import { Bell, User } from 'lucide-react';

export const Header = () => {
  const { username } = useAuth();

  return (
    <header className="header">
      <div className="header-content">
        <h1 className="header-title">Core Banking System</h1>
        <div className="header-actions">
          <button className="icon-btn">
            <Bell size={20} />
          </button>
          <div className="user-info">
            <User size={20} />
            <span>{username}</span>
          </div>
        </div>
      </div>
    </header>
  );
};
