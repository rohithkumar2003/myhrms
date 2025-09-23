import { createContext, useState, useEffect } from "react";

export const CurrentEmployeeContext = createContext();

export const CurrentEmployeeProvider = ({ children }) => {
  const [currentEmployee, setCurrentEmployee] = useState(null);
  const [job, setJob] = useState(null);
  const [bank, setBank] = useState(null);
  const [experienceStats, setExperienceStats] = useState([]);

  // --- Fetch employee data ---
  const fetchCurrentEmployee = async (token, employeeId) => {
     console.log("🔍 fetchCurrentEmployee called with:", token, employeeId);
    if (!token || !employeeId) return;

    try {
      const res = await fetch(`http://localhost:8181/api/employees/${employeeId}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
        console.log("📡 Response status:", res.status);

      if (!res.ok) throw new Error("Failed to fetch employee");
      const data = await res.json();
  console.log("📦 Data from backend:", data);
      setCurrentEmployee({ ...data, personal: data.personal || {} });
      setJob(data.job || {});
      setBank(data.bank || {});
      setExperienceStats(Array.isArray(data.experience) ? data.experience : []);
    } catch (err) {
      console.error("❌ Error fetching employee:", err);
      setCurrentEmployee(null);
      setJob(null);
      setBank(null);
      setExperienceStats([]);
    }
  };

  // --- Edit handlers ---
  const editCurrentEmployee = (updatedData) => {
    setCurrentEmployee((prev) => ({ ...prev, ...updatedData }));
  };
  const editJob = (jobData) => setJob(jobData);
  const editBank = (bankData) => setBank(bankData);
  const editExperience = (experience) => setExperienceStats(experience);

  // --- Auto-fetch on refresh ---
  useEffect(() => {
   
    const saved = localStorage.getItem("hrmsUser");
    if (saved) {
      const { token, employeeId } = JSON.parse(saved);
      fetchCurrentEmployee(token, employeeId);
    }
  }, []);

  useEffect(() => {
  if (currentEmployee) {
    console.log("✅ Fetched Employee:", currentEmployee);
    console.log("Name:", currentEmployee.personal?.name);
    console.log("Email:", currentEmployee.personal?.email);
  }
}, [currentEmployee]);


  return (
    <CurrentEmployeeContext.Provider
      value={{
        currentEmployee,
        fetchCurrentEmployee,
        editCurrentEmployee,
        job,
        editJob,
        bank,
        editBank,
        experienceStats,
        editExperience,
      }}
    >
      {children}
    </CurrentEmployeeContext.Provider>
  );
};
