import { createContext, useState, useEffect } from "react";
import axios from "axios";

export const EmployeeContext = createContext({
  employees: [],
  fetchEmployees: () => {},
});

export const EmployeeProvider = ({ children }) => {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Axios instance
  const axiosInstance = axios.create({
    baseURL: "http://localhost:8181/api/employees",
  });

  // Attach token
  axiosInstance.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem("token");
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  // Fetch employees
  const fetchEmployees = async () => {
    try {
      setLoading(true);
      const res = await axiosInstance.get("");
      console.log("Fetched employees:", res.data);
      setEmployees(res.data);
      setError(null);
    } catch (err) {
      console.error("Failed to fetch employees:", err.response || err);
      setEmployees([]);
      setError("Failed to load employees");
    } finally {
      setLoading(false);
    }
  };

  const addEmployee = async (employee) => {
    try {
      const res = await axiosInstance.post("/", employee);
      setEmployees((prev) => [...prev, res.data]);
    } catch (err) {
      console.error("Failed to add employee:", err.response || err);
    }
  };

  const editEmployee = async (employeeId, updatedData) => {
    try {
      const res = await axiosInstance.put(`/${employeeId}`, updatedData);
      setEmployees((prev) =>
        prev.map((emp) => (emp.employeeId === employeeId ? res.data : emp))
      );
    } catch (err) {
      console.error("Failed to edit employee:", err.response || err);
    }
  };

  const deactivateEmployment = async (employeeId) => {
    try {
      await axiosInstance.patch(`/${employeeId}/deactivate`);
      setEmployees((prev) =>
        prev.map((emp) =>
          emp.employeeId === employeeId ? { ...emp, isActive: false } : emp
        )
      );
    } catch (err) {
      console.error("Failed to deactivate employee:", err.response || err);
    }
  };

  const activateEmployee = async (employeeId) => {
    try {
      const today = new Date().toISOString().slice(0, 10);
      await axiosInstance.patch(`/${employeeId}/reactivate`, null, {
        params: { joiningDate: today },
      });
      setEmployees((prev) =>
        prev.map((emp) =>
          emp.employeeId === employeeId ? { ...emp, isActive: true } : emp
        )
      );
    } catch (err) {
      console.error("Failed to activate employee:", err.response || err);
    }
  };

  const getEmployeeById = async (employeeId) => {
    try {
      const res = await axiosInstance.get(`/${employeeId}`);
      return res.data;
    } catch (err) {
      console.error("Failed to get employee:", err.response || err);
      return null;
    }
  };

  useEffect(() => {
    fetchEmployees();
  }, []);

  return (
    <EmployeeContext.Provider
      value={{
        employees,
        loading,
        error,
        fetchEmployees,
        addEmployee,
        editEmployee,
        deactivateEmployment,
        activateEmployee,
        getEmployeeById,
      }}
    >
      {children}
    </EmployeeContext.Provider>
  );
};
