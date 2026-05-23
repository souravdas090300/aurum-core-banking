import { Link } from 'react-router-dom';
import { ShieldAlert } from 'lucide-react';

export const Unauthorized = () => {
  return (
    <div className="error-page">
      <div className="error-container">
        <ShieldAlert size={64} />
        <h1>403 - Unauthorized</h1>
        <p>You don't have permission to access this resource.</p>
        <Link to="/dashboard" className="btn btn-primary">
          Go to Dashboard
        </Link>
      </div>
    </div>
  );
};
