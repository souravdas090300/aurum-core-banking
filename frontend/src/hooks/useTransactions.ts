import { useState, useEffect, useCallback } from 'react';
import { useKeycloak } from '@react-keycloak/web';
import { fetchTransactions } from '../api/banking';
import { Transaction } from '../types';
import toast from 'react-hot-toast';

export function useTransactions() {
  const { keycloak } = useKeycloak();
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [loading,      setLoading]      = useState(true);
  const [error,        setError]        = useState<string | null>(null);

  const refetch = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchTransactions(keycloak?.token);
      setTransactions(data);
    } catch (e: any) {
      setError(e.message);
      toast.error('Failed to load transactions');
    } finally {
      setLoading(false);
    }
  }, [keycloak?.token]);

  useEffect(() => { refetch(); }, [refetch]);

  return { transactions, loading, error, refetch };
}