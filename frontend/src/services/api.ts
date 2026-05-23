import axios from 'axios';
import keycloak from '../config/keycloak';

// Create axios instance
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    if (keycloak.token) {
      config.headers.Authorization = `Bearer ${keycloak.token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (error.response?.status === 401) {
      // Token expired, try to refresh
      try {
        await keycloak.updateToken(30);
        // Retry the original request
        error.config.headers.Authorization = `Bearer ${keycloak.token}`;
        return axios.request(error.config);
      } catch {
        keycloak.login();
      }
    }
    return Promise.reject(error);
  }
);

export default api;
