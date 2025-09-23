import React, { useState, useContext,useEffect } from "react";
import { CurrentEmployeeContext } from "../EmployeeContext/CurrentEmployeeContext";
import { CurrentEmployeeSettingsContext } from "../EmployeeContext/CurrentEmployeeSettingsContext";

function getTodayStr() {
  return new Date().toISOString().slice(0, 10);
}

function formatTime(time) {
  if (!time) return "--";
  const [h, m] = time.split(":");
  const hour = parseInt(h, 10);
  const ampm = hour >= 12 ? "PM" : "AM";
  const hour12 = hour % 12 === 0 ? 12 : hour % 12;
  return `${hour12}:${m} ${ampm}`;
}

const EmployeeDashboard = () => {
  // Get data from CurrentEmployeeProvider
 const {
  
  currentEmployee,
  editCurrentEmployee,
  idle_time,
  employeeStats,
  editEmployeeStats,
  leaveStats,
  officeTimings,
  monthlyStats,
  job,
  editJob,
  bank,
  editBank,
  experienceStats,
  editExperience,
  
  birthdaysToday,
  punchIn,
  punchOut,
  fetchCurrentEmployee,
   notices: initialNotices,
} = useContext(CurrentEmployeeContext);



  
  // Get settings context
  const { playSound } = useContext(CurrentEmployeeSettingsContext);
 useEffect(() => {
    const token = localStorage.getItem("token");
    if (token && !currentEmployee) {
      fetchCurrentEmployee(token);
    }
  }, [currentEmployee, fetchCurrentEmployee]);
  // Attendance Tracker State
  const [punchedIn, setPunchedIn] = useState(false);
  const [punchInTime, setPunchInTime] = useState("");
  const [punchOutTime, setPunchOutTime] = useState("");

  const todayStr = getTodayStr();

  // Punch In Handler
  const handlePunchIn = async () => {
    if (!punchedIn) {
      const now = new Date();
      const timeStr = now.toTimeString().slice(0, 5);
      setPunchInTime(timeStr);
      setPunchedIn(true);
      
      // Play sound using settings context
      playSound('punchIn');
      
      await punchIn(); // Call context method
    }
  };

  // Punch Out Handler
  const handlePunchOut = async () => {
    if (punchedIn && !punchOutTime) {
      const now = new Date();
      const timeStr = now.toTimeString().slice(0, 5);
      setPunchOutTime(timeStr);
      
      // Play sound using settings context
      playSound('punchOut');
      
      await punchOut(); // Call context method
    }
  };

  // Edit Profile State
  const [editMode, setEditMode] = useState(false);
  const [editForm, setEditForm] = useState({
    name: currentEmployee?.personal?.name || "",
    email: currentEmployee?.personal?.email || "",
    phone: currentEmployee?.personal?.phone || "",
    employeeId: currentEmployee?.personal?.employeeId || "",
    department: job?.department || "",
    designation: job?.designation || "",
    experiences: [],
  });
  const [showAddExperience, setShowAddExperience] = useState(false);
  const [newExperience, setNewExperience] = useState({ company: '', role: '', years: '' });
  const [editError, setEditError] = useState("");

  // Update editForm when context data changes
  React.useEffect(() => {
   
    setEditForm({
      name: currentEmployee?.personal?.name || "",
      email: currentEmployee?.personal?.email || "",
      phone: currentEmployee?.personal?.phone || "",
      employeeId: currentEmployee?.personal?.employeeId || "",
      department: job?.department || "",
      designation: job?.designation || "",
      experiences: [],
    });
  }, [currentEmployee, employeeStats, job]);

  const handleEditChange = (e) => {
    const { name, value, type, files, checked } = e.target;
    if (type === 'file') {
      const file = files[0];
      if (file) {
        const reader = new FileReader();
        reader.onloadend = () => {
          setEditForm((prev) => ({ ...prev, [name]: reader.result }));
        };
        reader.readAsDataURL(file);
      }
    } else if (type === 'checkbox') {
      setEditForm((prev) => ({ ...prev, [name]: checked }));
    } else {
      setEditForm((prev) => ({ ...prev, [name]: value }));
    }
  };

  const handleEditSave = () => {
    const { name, email: trimmedEmail, phone: trimmedPhone, employeeId, department, designation } = editForm;

    const trimmedName = (name || "").trim();
    const trimmedEmployeeId = (employeeId || "").trim();
    const trimmedDepartment = (department || "").trim();
    const trimmedDesignation = (designation || "").trim();

    if (!trimmedName || !trimmedEmail || !trimmedPhone || !trimmedEmployeeId || !trimmedDepartment || !trimmedDesignation) {
      setEditError("All fields are mandatory.");
      return;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(trimmedEmail)) {
      setEditError("Please enter a valid email address.");
      return;
    }
    const phoneRegex = /^[0-9]{10}$/;
    if (!phoneRegex.test(trimmedPhone)) {
      setEditError("Phone number must be 10 digits.");
      return;
    }

    // Update context data
    editCurrentEmployee({
      personal: {
        name: trimmedName,
        employeeId: trimmedEmployeeId
      }
    });

    editEmployeeStats({
      email: trimmedEmail,
      phone: trimmedPhone
    });

    editJob({
      department: trimmedDepartment,
      designation: trimmedDesignation
    });

    setEditMode(false);
    setEditError("");
    console.log("Profile updated:", editForm);
  };

  // Simple Bar Chart Component
  const SimpleBarChart = ({ data, labels, title }) => {
    const maxValue = Math.max(...data);
    const colors = ["#22c55e", "#facc15", "#3b82f6"];
    
    return (
      <div className="w-full">
        <h3 className="text-center font-semibold mb-4">{title}</h3>
        <div className="flex items-end justify-center gap-4 h-40">
          {data.map((value, index) => (
            <div key={index} className="flex flex-col items-center gap-2">
              <div className="text-xs text-center text-gray-600 font-medium">{value}</div>
              <div 
                className="w-12 rounded-t-md transition-all duration-300"
                style={{ 
                  height: `${(value / maxValue) * 100 + 20}px`,
                  backgroundColor: colors[index % colors.length]
                }}
              />
              <div className="text-xs text-center text-gray-600 max-w-16">{labels[index]}</div>
            </div>
          ))}
        </div>
      </div>
    );
  };

  // Simple Pie Chart Component
  const SimplePieChart = ({ data, labels, title }) => {
    const total = data.reduce((sum, val) => sum + val, 0);
    const colors = ["#3b82f6", "#f87171"];
    let cumulativePercentage = 0;
    
    return (
      <div className="w-full">
        <h3 className="text-center font-semibold mb-4">{title}</h3>
        <div className="flex flex-col items-center">
          <svg width="150" height="150" className="mb-4">
            {data.map((value, index) => {
              const percentage = (value / total) * 100;
              const strokeDasharray = `${percentage} ${100 - percentage}`;
              const strokeDashoffset = -cumulativePercentage;
              cumulativePercentage += percentage;
              
              return (
                <circle
                  key={index}
                  cx="75"
                  cy="75"
                  r="40"
                  fill="transparent"
                  stroke={colors[index]}
                  strokeWidth="20"
                  strokeDasharray={strokeDasharray}
                  strokeDashoffset={strokeDashoffset}
                  transform="rotate(-90 75 75)"
                />
              );
            })}
          </svg>
          <div className="flex gap-4 text-sm">
            {labels.map((label, index) => (
              <div key={index} className="flex items-center gap-1">
                <div 
                  className="w-3 h-3 rounded" 
                  style={{ backgroundColor: colors[index] }}
                />
                <span>{label}: {data[index]}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    );
  };

  // Show loading or error state if context data is not available
  if (!currentEmployee || !employeeStats || !job) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600 mb-4"></div>
          <p className="text-gray-600">Loading employee data...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="p-4 md:p-8 bg-gradient-to-br from-gray-50 to-blue-50 min-h-screen">
      {/* Employee Profile Card */}
      <div className="flex flex-col md:flex-row items-center bg-gradient-to-r from-blue-100 to-blue-50 rounded-2xl shadow-lg p-6 mb-8 gap-6">
        <div className="flex-shrink-0">
          <img
            alt="Employee"
            src={
              currentEmployee.personal.profile_photo ||
              `https://ui-avatars.com/api/?name=${encodeURIComponent(
                currentEmployee.personal.name
              )}&background=0D8ABC&color=fff&size=128`
            }
            className="w-28 h-28 rounded-full border-4 border-white shadow object-cover"
          />
        </div>
        <div className="flex-1">
          <h3 className="text-2xl font-bold text-blue-900 mb-1 flex items-center gap-2">
            <span className="text-blue-400">👤</span>{" "}
            {currentEmployee.personal.name}
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-1 text-gray-700">
            <div>
              <span className="font-semibold">Employee ID:</span>{" "}
              {currentEmployee.personal.employeeId}
            </div>
            <div>
              <span className="font-semibold">Designation:</span> {job.designation}
            </div>
            <div>
              <span className="font-semibold">Department:</span> {job.department}
            </div>
            <div>
              <span className="font-semibold">Email:</span> {currentEmployee.personal?.email}
            </div>
            <div>
              <span className="font-semibold">Phone:</span> {currentEmployee.contact?.phone}
            </div>
          </div>
        </div>
      </div>

      {/* Daily Check-in */}
      <div className="bg-white rounded-2xl shadow-lg p-6 mb-8">
        <div className="flex items-center mb-4 gap-2">
          <span className="text-blue-600 text-xl">🕐</span>
          <h2 className="text-xl font-bold tracking-tight">Daily Check-in</h2>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full text-sm">
            <thead>
              <tr className="bg-blue-50 text-blue-900">
                <th className="px-4 py-2 font-semibold">Date</th>
                <th className="px-4 py-2 font-semibold">Punch In</th>
                <th className="px-4 py-2 font-semibold">Punch Out</th>
                <th className="px-4 py-2 font-semibold">Working Hours</th>
                <th className="px-4 py-2 font-semibold">Idle Time</th>
                <th className="px-4 py-2 font-semibold">Status</th>
                <th className="px-4 py-2 font-semibold">Action</th>
              </tr>
            </thead>
            <tbody>
              <tr className="text-center">
                <td className="border px-4 py-2">{todayStr}</td>
                <td className="border px-4 py-2">{formatTime(punchInTime)}</td>
                <td className="border px-4 py-2">{formatTime(punchOutTime)}</td>
                <td className="border px-4 py-2">{officeTimings.full_day_threshold} hrs</td>
                <td className="border px-4 py-2">
                  {punchOutTime ? (
                    <span className="text-yellow-600 font-semibold">
                      {idle_time} hrs
                    </span>
                  ) : (
                    "--"
                  )}
                </td>
                <td className="border px-4 py-2">
                  {!punchedIn
                    ? <span className="text-gray-500">Not Punched In</span>
                    : !punchOutTime
                      ? <span className="text-blue-600">Working</span>
                      : <span className="text-green-600">Completed</span>
                  }
                </td>
                <td className="border px-4 py-2">
                  {!punchedIn ? (
                    <button
                      className="bg-green-600 text-white px-4 py-1 rounded-lg shadow hover:bg-green-700 transition"
                      onClick={handlePunchIn}
                    >
                      Punch In
                    </button>
                  ) : !punchOutTime ? (
                    <button
                      className="bg-red-600 text-white px-4 py-1 rounded-lg shadow hover:bg-red-700 transition"
                      onClick={handlePunchOut}
                    >
                      Punch Out
                    </button>
                  ) : (
                    <span className="text-gray-400">Done</span>
                  )}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <p className="text-xs text-gray-500 mt-2">
          🕐 Working hours: <span className="font-semibold">{officeTimings.office_start} - {officeTimings.office_end}</span>. Idle time is calculated for late punch in or early punch out.
        </p>
      </div>

      {/* Dashboard Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        <div className="bg-white rounded-xl shadow p-4 flex items-center gap-4">
          <span className="text-blue-500 text-2xl">📅</span>
          <div>
            <div className="text-sm text-gray-500">Leaves (This Month)</div>
            <div className="font-bold text-lg text-blue-900">{monthlyStats.monthlyLeaves}</div>
          </div>
        </div>
        <div className="bg-white rounded-xl shadow p-4 flex items-center gap-4">
          <span className="text-green-500 text-2xl">🕐</span>
          <div>
            <div className="text-sm text-gray-500">Worked Hours (This Month)</div>
            <div className="font-bold text-lg text-green-900">{monthlyStats.monthlyWorkHours}</div>
          </div>
        </div>
        <div className="bg-white rounded-xl shadow p-4 flex items-center gap-4">
          <span className="text-yellow-500 text-2xl">📊</span>
          <div>
            <div className="text-sm text-gray-500">Idle Time (This Month)</div>
            <div className="font-bold text-lg text-yellow-900">{monthlyStats.monthlyIdleHours}</div>
          </div>
        </div>
      </div>

      {/* Analytics Row: Leave Bar Chart & Work Hours Pie Chart */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
        <div className="bg-white rounded-2xl shadow-lg p-4 flex flex-col items-center">
          <div className="flex items-center mb-2 gap-2">
            <span className="text-blue-500 text-lg">📅</span>
            <h2 className="text-lg font-bold tracking-tight">Leave Summary</h2>
          </div>
          <SimpleBarChart 
            data={[
              leaveStats.full_day_leaves_approved,
              leaveStats.half_day_leaves_approved,
              leaveStats.sandwich_leave_count
            ]}
            labels={["Full Day", "Half Day", "Sandwich"]}
          />
          <p className="text-center mt-2 text-sm text-gray-600">
            Paid: {leaveStats.paid_leave_count}, Unpaid: {leaveStats.unpaid_leave_count}
          </p>
        </div>

        <div className="bg-white rounded-2xl shadow-lg p-4 flex flex-col items-center">
          <div className="flex items-center mb-2 gap-2">
            <span className="text-yellow-500 text-lg">📊</span>
            <h2 className="text-lg font-bold tracking-tight">Work Hours Summary</h2>
          </div>
          <SimplePieChart 
            data={[monthlyStats.monthlyWorkHours, monthlyStats.monthlyIdleHours]}
            labels={["Worked Hours", "Idle Time"]}
          />
        </div>
      </div>

      {/* Notice Board */}
      <div className="bg-white rounded-2xl shadow-lg p-4 mb-8">
        <div className="flex items-center mb-4 gap-2">
          <span className="text-red-500 text-lg">🔔</span>
          <h2 className="text-lg font-bold tracking-tight">Notice Board</h2>
        </div>
        {initialNotices && initialNotices.length > 0 ? (
          <ul className="space-y-2 max-h-64 overflow-y-auto">
            {initialNotices.map((notice, idx) => (
              <li key={notice.id || idx} className="border-b pb-2 last:border-b-0">
                <p className="text-gray-700 font-semibold">{notice.title}</p>
                <p className="text-gray-500 text-sm">{notice.message || notice.description}</p>
                <p className="text-gray-400 text-xs">{new Date(notice.date).toLocaleDateString()}</p>
                {notice.author && <p className="text-gray-400 text-xs">By: {notice.author}</p>}
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-gray-500">No notices available.</p>
        )}
      </div>

      {/* Birthday Card */}
      <div className="bg-gradient-to-r from-pink-50 to-purple-50 rounded-2xl shadow-lg p-4 mb-8 border-l-4 border-pink-400">
        <div className="flex items-center mb-4 gap-2">
          <span className="text-2xl">🎂</span>
          <h2 className="text-lg font-bold tracking-tight text-pink-800">Today's Birthdays</h2>
        </div>
        {birthdaysToday && birthdaysToday.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {birthdaysToday.map((birthday, idx) => (
              <div key={birthday.employee_id || idx} className="bg-white rounded-lg p-4 shadow-sm border border-pink-100 flex items-center gap-3">
                <div className="flex-shrink-0">
                  <img
                    alt={birthday.name}
                    src={`https://ui-avatars.com/api/?name=${encodeURIComponent(birthday.name)}&background=EC4899&color=fff&size=64`}
                    className="w-12 h-12 rounded-full border-2 border-pink-200"
                  />
                </div>
                <div className="flex-1">
                  <p className="font-semibold text-pink-800">{birthday.name}</p>
                  <p className="text-sm text-pink-600">Employee ID: {birthday.employee_id}</p>
                  <p className="text-xs text-pink-500">Born: {new Date(birthday.dob).toLocaleDateString()}</p>
                </div>
                <div className="text-2xl">🎉</div>
              </div>
            ))}
          </div>
        ) : (
          <div className="text-center py-8">
            <span className="text-4xl">🎈</span>
            <p className="text-pink-600 mt-2">No birthdays today</p>
            <p className="text-pink-500 text-sm">Check back tomorrow!</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default EmployeeDashboard;