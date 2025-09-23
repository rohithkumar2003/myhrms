import { createContext, useState, useEffect, useContext } from "react";
import { AuthContext } from "./AuthContext"; // use the same api instance
import axios from "axios";

export const EmployeeContext = createContext();

export const EmployeeProvider = ({ children }) => {
  const { api } = useContext(AuthContext); // use AuthProvider's axios
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchEmployees = async () => {
    try {
      setLoading(true);
      const res = await api.get("/employees");
      setEmployees(res.data);
      setError(null);
    } catch (err) {
      console.error("❌ Error fetching employees:", err.response?.data || err.message);
      setError("Failed to fetch employees");
      setEmployees([]);
    } finally {
      setLoading(false);
    }
  };

  const addEmployee = async (employee) => {
    try {
      const res = await api.post("/employees", employee);
      setEmployees((prev) => [...prev, res.data]);
      return res.data;
    } catch (err) {
      console.error("❌ Error adding employee:", err.response?.data || err.message);
      throw err;
    }
  };

  useEffect(() => {
    fetchEmployees();
  }, []);

  return (
    <EmployeeContext.Provider value={{ employees, loading, error, fetchEmployees, addEmployee }}>
      {children}
    </EmployeeContext.Provider>
  );
};
