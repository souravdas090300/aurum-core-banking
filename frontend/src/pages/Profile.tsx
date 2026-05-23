import { useEffect, useState } from 'react';
import { customerService } from '../services/bankingService';
import type { Customer } from '../types';
import { User, Mail, Phone, MapPin, Calendar } from 'lucide-react';
import toast from 'react-hot-toast';

export const Profile = () => {
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      const response = await customerService.getProfile();
      setCustomer(response.data);
    } catch (error) {
      toast.error('Failed to load profile');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!customer) return;

    try {
      await customerService.update(customer);
      toast.success('Profile updated successfully');
      setEditing(false);
    } catch (error) {
      toast.error('Failed to update profile');
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setCustomer((prev) => prev ? { ...prev, [name]: value } : null);
  };

  if (loading) {
    return <div className="loading">Loading...</div>;
  }

  if (!customer) {
    return <div className="error-state">Failed to load profile</div>;
  }

  return (
    <div className="profile-page">
      <h2 className="page-title">My Profile</h2>

      <div className="profile-container">
        <div className="card">
          <div className="profile-header">
            <div className="profile-avatar">
              <User size={64} />
            </div>
            <div>
              <h3 className="profile-name">{customer.firstName} {customer.lastName}</h3>
              <span className={`kyc-badge ${customer.kycStatus.toLowerCase()}`}>
                KYC: {customer.kycStatus}
              </span>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="profile-form">
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="firstName">First Name</label>
                <div className="input-with-icon">
                  <User size={20} />
                  <input
                    type="text"
                    id="firstName"
                    name="firstName"
                    value={customer.firstName}
                    onChange={handleChange}
                    className="form-control"
                    disabled={!editing}
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="lastName">Last Name</label>
                <div className="input-with-icon">
                  <User size={20} />
                  <input
                    type="text"
                    id="lastName"
                    name="lastName"
                    value={customer.lastName}
                    onChange={handleChange}
                    className="form-control"
                    disabled={!editing}
                  />
                </div>
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="email">Email</label>
              <div className="input-with-icon">
                <Mail size={20} />
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={customer.email}
                  onChange={handleChange}
                  className="form-control"
                  disabled={!editing}
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="phone">Phone</label>
              <div className="input-with-icon">
                <Phone size={20} />
                <input
                  type="tel"
                  id="phone"
                  name="phone"
                  value={customer.phone}
                  onChange={handleChange}
                  className="form-control"
                  disabled={!editing}
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="address">Address</label>
              <div className="input-with-icon">
                <MapPin size={20} />
                <input
                  type="text"
                  id="address"
                  name="address"
                  value={customer.address}
                  onChange={handleChange}
                  className="form-control"
                  disabled={!editing}
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="dateOfBirth">Date of Birth</label>
              <div className="input-with-icon">
                <Calendar size={20} />
                <input
                  type="date"
                  id="dateOfBirth"
                  name="dateOfBirth"
                  value={customer.dateOfBirth.split('T')[0]}
                  onChange={handleChange}
                  className="form-control"
                  disabled={!editing}
                />
              </div>
            </div>

            <div className="form-actions">
              {!editing ? (
                <button
                  type="button"
                  onClick={() => setEditing(true)}
                  className="btn btn-primary"
                >
                  Edit Profile
                </button>
              ) : (
                <>
                  <button type="submit" className="btn btn-primary">
                    Save Changes
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setEditing(false);
                      loadProfile();
                    }}
                    className="btn btn-secondary"
                  >
                    Cancel
                  </button>
                </>
              )}
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};
