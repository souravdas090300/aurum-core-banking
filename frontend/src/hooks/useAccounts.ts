import { useState, useEffect, useCallback } from 'react';
import { useKeycloak } from '@react-keycloak/web';
import { fetchAccounts } from '../api/banking';
import { Account } from '../types';
import toast from 'react-hot-toast';

export function useAccounts() {
  const { keycloak } = useKeycloak();
  const [accounts, setAccounts] = useState<Account[]>([]);
  const [loading, setLoading]  = useState(true);
  const [error,    setError]    = useState<string | null>(null);

  const refetch = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchAccounts(keycloak?.token);
      setAccounts(data);
    } catch (e: any) {
      setError(e.message);
      toast.error('Failed to load accounts');
    } finally {
      setLoading(false);
    }
  }, [keycloak?.token]);

  useEffect(() => { refetch(); }, [refetch]);

  const totalBalance = accounts
    .filter(a => a.status === 'ACTIVE' && a.accountType !== 'LOAN')
    .reduce((sum, a) => sum + a.balance, 0);

  return { accounts, loading, error, refetch, totalBalance };
}