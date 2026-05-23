import { Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { 
  Home, 
  CreditCard, 
  ArrowRightLeft, 
  History, 
  FileText, 
  User,
  LogOut 
} from 'lucide-react';

export const Sidebar = () => {
  const location = useLocation();
  const { logout } = useAuth();

  const menuItems = [
    { path: '/dashboard', icon: Home, label: 'Dashboard' },
    { path: '/accounts', icon: CreditCard, label: 'Accounts' },
    { path: '/transfer', icon: ArrowRightLeft, label: 'Transfer' },
    { path: '/transactions', icon: History, label: 'Transactions' },
    { path: '/loans', icon: FileText, label: 'Loans' },
    { path: '/profile', icon: User, label: 'Profile' },
  ];

  const isActive = (path: string) => location.pathname === path;

  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <h2>Aurum Bank</h2>
      </div>
      <nav className="sidebar-nav">
        {menuItems.map((item) => (
          <Link
            key={item.path}
            to={item.path}
            className={`sidebar-item ${isActive(item.path) ? 'active' : ''}`}
          >
            <item.icon size={20} />
            <span>{item.label}</span>
          </Link>
        ))}
      </nav>
      <div className="sidebar-footer">
        <button className="logout-btn" onClick={logout}>
          <LogOut size={20} />
          <span>Logout</span>
        </button>
      </div>
    </aside>
  );
};
