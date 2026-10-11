import axios from "axios";

export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";

export const http = axios.create({ baseURL: API_BASE_URL });

const AUTH_STORAGE_KEY = "mmt.basicAuth";

http.interceptors.request.use((config) => {
  if (typeof window !== "undefined") {
    const credential = window.sessionStorage.getItem(AUTH_STORAGE_KEY);
    if (credential) config.headers.Authorization = credential;
  }
  return config;
});

// The backend currently uses HTTP Basic. Keep credentials in this tab's session
// only, never in the persisted Redux user record/localStorage.
export function setApiCredentials(email, password) {
  if (typeof window === "undefined") return;
  const value = window.btoa(unescape(encodeURIComponent(`${email}:${password}`)));
  window.sessionStorage.setItem(AUTH_STORAGE_KEY, `Basic ${value}`);
}

export function clearApiCredentials() {
  if (typeof window !== "undefined") {
    window.sessionStorage.removeItem(AUTH_STORAGE_KEY);
  }
}
