import { useState, useEffect, useCallback } from 'react';
import { useKeycloak } from '@react-keycloak/web';
import { apiClient, authHeader } from '../api/client';
import { AuditLogEntry, PageResponse } from '../types';
import toast from 'react-hot-toast';

export function useAuditLog(entityType?: string) {
  const { keycloak } = useKeycloak();
  const [entries,    setEntries]    = useState<AuditLogEntry[]>([]);
  const [loading,    setLoading]    = useState(true);
  const [totalPages, setTotalPages] = useState(0);
  const [page,       setPage]       = useState(0);

  const fetch = useCallback(async (p = 0) => {
    setLoading(true);
    try {
      const params = new URLSearchParams({ page: String(p), size: '50' });
      if (entityType) params.append('entityType', entityType);
      const res = await apiClient.get<PageResponse<AuditLogEntry>>(
        `/api/v1/audit?${params.toString()}`,
        { headers: authHeader(keycloak?.token) }
      );
      setEntries(res.data.content);
      setTotalPages(res.data.totalPages);
      setPage(p);
    } catch (e: any) {
      toast.error('Failed to load audit log');
    } finally {
      setLoading(false);
    }
  }, [entityType, keycloak?.token]);

  useEffect(() => { fetch(); }, [fetch]);

  return { entries, loading, totalPages, page, fetch };
}