import { useState, useEffect } from "react";
import { CurrentEmployeeContext } from "./CurrentEmployeeContext";

const idle_time = 1.25;

export const CurrentEmployeeProvider = ({ children }) => {
  
  const hrmsUser = JSON.parse(localStorage.getItem("hrmsUser"));
  const employeeId = hrmsUser?.employeeId;
  const token = hrmsUser?.token;

  const headers = token
    ? { Authorization: `Bearer ${token}`, "Content-Type": "application/json" }
    : { "Content-Type": "application/json" };

  const [currentEmployee, setCurrentEmployee] = useState({
    personal: {
      name: "",
      fatherName: "",
      dob: "",
      gender: "",
      marital_status: "",
      nationality: "",
      aadhaar_number: "",
      pan_number: "",
      profile_photo: null,
      aadhaar: null,
      pan: null,
      resume: null,
      employeeId: employeeId || "",
    },
  });

  const [employeeStats, setEmployeeStats] = useState({});
  const [leaveStats, setLeaveStats] = useState({});
  const [officeTimings, setOfficeTimings] = useState({});
  const [monthlyStats, setMonthlyStats] = useState({});
  const [job, setJob] = useState({});
  const [bank, setBank] = useState({});
  const [experienceStats, setExperienceStats] = useState({});
  const [birthdaysToday, setBirthdaysToday] = useState([]);
  const [notices, setNotices] = useState([]);

  // ---------------- Helper ----------------
  const getCurrentMonthYear = () => {
  const today = new Date();
  return {
    month: today.getMonth() + 1, // plain number (1–12)
    year: today.getFullYear(),
  };
};


  // ---------------- Fetchers ----------------
  const fetchCurrentEmployee = async () => {
    if (!employeeId) return;

    try {
      // Personal Details
       const empRes = await fetch(
      `http://localhost:8181/api/employees/${employeeId}`,
      { headers }
    );
    const empData = await empRes.json();
    console.log("📦 empData:", empData);

      const personalRes = await fetch(
        `http://localhost:8181/api/employees/${employeeId}/personal-details`,
        { headers }
      );
      const personalData = await personalRes.json();
console.log("📦 personalData:", personalData);
      // Job Details
      const jobRes = await fetch(
        `http://localhost:8181/api/employees/${employeeId}/job-details`,
        { headers }
      );
      const jobData = await jobRes.json();

      // Bank Details
      const bankRes = await fetch(
        `http://localhost:8181/api/employees/${employeeId}/bank-details`,
        { headers }
      );
      const bankData = await bankRes.json();

      // Experience Details
      const expRes = await fetch(
        `http://localhost:8181/api/employees/${employeeId}/experience`,
        { headers }
      );
      const expData = await expRes.json();

      setCurrentEmployee({
        ...empData,
        personal: {
          ...personalData,
           personalId: personalData.personalId || "",
          name: empData.name || "",
           email: empData.email || "",   
          employeeId,
          profile_photo: null,
          aadhaar: null,
          pan: null,
          resume: null,
        },
        contact: {
    phone: empData.phone || "",
    address: empData.address || "",
    emergencyContactName: empData.emergencyContactName || "",
    emergencyContactPhone: empData.emergencyContactPhone || "",
    emergencyContactRelation: empData.emergencyContactRelation || "",
  },
  bank: {
    ...empData.bankDetails,
    bankId: empData.bankDetails?.bankId || "",   // ✅ keep bankId
  },
  experience: (empData.experienceDetails || []).map(exp => ({
    ...exp,
    experienceId: exp.experienceId || "",       // ✅ keep experienceId
  })),
});
      setJob(jobData || {});
      setBank(bankData[0] || {});
      setExperienceStats(expData[0] || {}); // Assuming first experience entry
    } catch (err) {
      console.warn("Failed to fetch current employee data", err);
    }
  };

  // const fetchEmployeeStats = async () => {
  //   try {
  //     const res = await fetch(
  //       `http://localhost:8181/api/employees/${employeeId}/stats`,
  //       { headers }
  //     );
  //     if (!res.ok) throw new Error("Server error");
  //     const data = await res.json();
  //     setEmployeeStats(data);
  //   } catch (err) {
  //     console.warn("Failed to fetch employee stats", err);
  //   }
  // };

const fetchLeaveStats = async () => {
  try {
    const today = new Date();
    const year = today.getFullYear();
    const month = today.getMonth() + 1;

    const res = await fetch(
      `http://localhost:8181/api/leaves/employee/${employeeId}/stats?year=${year}&month=${month}`,
      { headers }
    );
     if (res.status === 404) {
      console.warn("No leave stats found for this employee");
      setLeaveStats({}); // 👈 set empty stats
      return;
    }
    if (!res.ok) throw new Error("Server error");
    const data = await res.json();
    setLeaveStats(data || {});
  } catch (err) {
    console.warn("❌ Failed to fetch leave stats", err);
    setLeaveStats({});
  }
};


  // const fetchOfficeTimings = async () => {
  //   try {
  //     const res = await fetch(
  //       "http://localhost:8181/api/employees/office-timings",
  //       { headers }
  //     );
  //     if (!res.ok) throw new Error("Server error");
  //     const data = await res.json();
  //     setOfficeTimings(data);
  //   } catch (err) {
  //     console.warn("Failed to fetch office timings", err);
  //   }
  // };

  const fetchMonthlyStats = async () => {
    const { month, year } = getCurrentMonthYear();
    try {
      const res = await fetch(
        `http://localhost:8181/api/attendance/employee/${employeeId}/hours/summary/${month}/${year}`,
        { headers }
      );
      if (!res.ok) throw new Error("Server error");
      const data = await res.json();
      setMonthlyStats(data);
    } catch (err) {
      console.warn("Failed to fetch monthly stats", err);
    }
  };

  const fetchNotices = async () => {
    try {
      const res = await fetch(
        `http://localhost:8181/api/notices`,
        { headers }
      );
      if (!res.ok) throw new Error("Server error");
      const data = await res.json();
      setNotices(data.notices || []);
    } catch (err) {
      console.warn("Failed to fetch notices", err);
    }
  };

  // ---------------- Updaters ----------------
  const editCurrentEmployee = async (updatedData) => {
    setCurrentEmployee((prev) => ({
      ...prev,
      personal: { ...prev.personal, ...(updatedData.personal || {}) },
    }));
    try {
      await fetch(`http://localhost:8181/api/employees/${employeeId}/personal-details`, {
        method: "PUT",
        headers,
        body: JSON.stringify({
        ...updatedData.personal,
        personalId: currentEmployee.personal.personalId, // ✅ always include personalId
      }),
      });
    } catch (err) {
      console.warn("Failed to update personal details", err);
    }
  };

  const editJobWithBackend = async (updatedJob) => {
    setJob((prev) => ({ ...prev, ...updatedJob }));
    try {
      await fetch(`http://localhost:8181/api/employees/${employeeId}/job-details`, {
        method: "PUT",
        headers,
        body: JSON.stringify(updatedJob),
      });
    } catch (err) {
      console.warn("Failed to update job details", err);
    }
  };

  const editBank = async (updatedBank) => {
    setBank((prev) => ({ ...prev, ...updatedBank }));
    try {
      await fetch(`http://localhost:8181/api/employees/${employeeId}/bank-details`, {
        method: "PUT",
        headers,
        body: JSON.stringify({
        ...updatedBank,
        bankId: bank.bankId,  // ✅ always include bankId when updating
      }),
      });
    } catch (err) {
      console.warn("Failed to update bank details", err);
    }
  };

  const editExperience = async (updatedExperience) => {
  setExperienceStats((prev) => ({
    ...prev,
    ...updatedExperience,
  }));

  try {
    await fetch(`http://localhost:8181/api/employees/${employeeId}/experience`, {
      method: "PUT",
      headers,
      body: JSON.stringify({
        ...updatedExperience,
        experienceId: updatedExperience.experienceId,  // ✅ must include this
      }),
    });
  } catch (err) {
    console.warn("Failed to update experience details", err);
  }
};


  // const editEmployeeStats = async (updatedStats) => {
  //   setEmployeeStats((prev) => ({ ...prev, ...updatedStats }));
  //   try {
  //     await fetch(`http://localhost:8181/api/employees/${employeeId}/stats`, {
  //       method: "PUT",
  //       headers,
  //       body: JSON.stringify(updatedStats),
  //     });
  //   } catch (err) {
  //     console.warn("Failed to update employee stats", err);
  //   }
  // };

  // ---------------- Effects ----------------
  useEffect(() => {
    if (!employeeId) return;
    fetchCurrentEmployee();
    // fetchEmployeeStats();
    fetchLeaveStats();
    // fetchOfficeTimings();
    fetchMonthlyStats();
    fetchNotices();
  }, [employeeId]);

  useEffect(() => {
  if (currentEmployee?.personal) {
    console.log("🔥 CurrentEmployeeProvider mounted");

    console.log("✅ Current Employee fetched:", currentEmployee);
    console.log("Name:", currentEmployee.personal.name);
    console.log("Email:", currentEmployee.personal.email);
  } else {
    console.log("⚠️ No employee data yet");
  }
}, [currentEmployee]);

  return (
    <CurrentEmployeeContext.Provider
      value={{
        currentEmployee,
        editCurrentEmployee,
        idle_time,
        employeeStats: employeeStats || {},
    editEmployeeStats: () => {},
        leaveStats,
        fetchLeaveStats,
       officeTimings: officeTimings || { office_start: "09:00", office_end: "18:00", full_day_threshold: 8 },
        // fetchOfficeTimings,
        monthlyStats,
        fetchMonthlyStats,
        job,
        editJob: editJobWithBackend,
        bank,
        editBank,
        editExperience,
        experienceStats,
        setExperienceStats,
        birthdaysToday,
        notices,
        fetchNotices,
        fetchCurrentEmployee,
      }}
    >
      {children}
    </CurrentEmployeeContext.Provider>
  );
};
