import axios from "axios";

const baseURL = process.env.REACT_APP_API_BASE_URL || "http://localhost:8080";

export const apiClient = axios.create({
  baseURL,
  headers: {
    "Content-Type": "application/json",
  },
});

export function authHeader(token?: string) {
  return token ? { Authorization: `Bearer ${token}` } : undefined;
}
