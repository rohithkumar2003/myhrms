import { useState, useEffect } from "react";
import { AuthContext } from "./AuthContext";
import axios from "axios";

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);

  // Load user from localStorage on refresh
  useEffect(() => {
    const saved = localStorage.getItem("hrmsUser");
    if (saved) setUser(JSON.parse(saved));
  }, []);

  // Axios instance
  const api = axios.create({
    baseURL: "http://localhost:8181/api",
    headers: { "Content-Type": "application/json" },
  });

  // Attach JWT to all requests automatically
  api.interceptors.request.use((config) => {
    const saved = localStorage.getItem("hrmsUser");
    if (saved) {
      const parsed = JSON.parse(saved);
      if (parsed.token) {
        config.headers.Authorization = `Bearer ${parsed.token}`;
      }
    }
    return config;
  });

  // Global response interceptor for expired token
  api.interceptors.response.use(
    (res) => res,
    (err) => {
      if (err.response?.data === "Token expired") {
        logout();
        window.location.href = "/"; // redirect to login
      }
      return Promise.reject(err);
    }
  );

  // LOGIN function
  const login = async (email, password) => {
    try {
      let res, userData;

      if (email === "admin@example.com") {
        res = await api.post("/auth/login", { email, password });
        const { token, role } = res.data;
        userData = { email, role: "admin", token };
      } else {
        res = await api.post("/employees/login", { email, password });
        const { employeeId, role, token } = res.data; // make sure backend returns token for employees
        userData = { email, role: "employee", employeeId, token };
      }

      localStorage.setItem("hrmsUser", JSON.stringify(userData));
      setUser(userData);
      return userData;
    } catch (err) {
      console.error("Login failed:", err.response?.data || err.message);
      return null;
    }
  };

  const logout = () => {
    localStorage.removeItem("hrmsUser");
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, api }}>
      {children}
    </AuthContext.Provider>
  );
};
