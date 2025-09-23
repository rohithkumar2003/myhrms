import React, { useState,useEffect, useContext, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { FaUsers, FaCalendarAlt, FaClipboardList, FaBuilding, FaChevronLeft, FaChevronRight, FaSyncAlt } from "react-icons/fa";
import AttendanceChart from "../components/AttendanceChart";
import DepartmentPieChart from "../components/DepartmentPieChart";
import { EmployeeContext } from "../context/EmployeeContext";
import { AttendanceContext } from "../context/AttendanceContext";
import { LeaveRequestContext } from "../context/LeaveRequestContext";

// --- Main AdminDashboard Component ---
const AdminDashboard = () => {
  
  // 1. GET RAW DATA AND THE NEW "BACKEND" FUNCTION FROM CONTEXTS
  const { employees , loading, error, fetchEmployees} = useContext(EmployeeContext);
  const { attendanceRecords, getDashboardData } = useContext(AttendanceContext);
  const { leaveRequests } = useContext(LeaveRequestContext);
  const navigate = useNavigate();

  // 2. MANAGE UI-ONLY STATE
  const [selectedDept, setSelectedDept] = useState("All");
  const [currentWeek, setCurrentWeek] = useState(0); // 0 = current week

  useEffect(() => {
    fetchEmployees();
  }, []);
  // 3. FETCH ALL PROCESSED DATA IN ONE GO
  // This single call gets all the calculated stats and lists needed for the UI.
  // useMemo ensures this only runs when the base data from the context changes.
  const { statCards, activeEmployees, departmentList } = useMemo(
    () => {
  if (!employees || employees.length === 0) return {
    statCards: { totalEmployees: 0, onLeaveToday: 0, pendingLeaves: 0, totalDepartments: 0 },
    activeEmployees: [],
    departmentList: []
  };
  return getDashboardData(employees, leaveRequests);
}, [employees, leaveRequests, getDashboardData]);

  // 4. CALCULATE WEEK DATES AND FILTER THE FINAL ATTENDANCE DATA FOR THE CHART
  // This part remains in the component as it's directly tied to the week navigation UI state.
  const weekDates = useMemo(() => {
    const today = new Date();
    today.setDate(today.getDate() + currentWeek * 7);
    const dayOfWeek = today.getDay();
    const monday = new Date(today);
    monday.setDate(today.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1)); // Handle Sunday case
    const sunday = new Date(monday);
    sunday.setDate(monday.getDate() + 6);
    return {
      start: monday.toISOString().slice(0, 10),
      end: sunday.toISOString().slice(0, 10),
    };
  }, [currentWeek]);

  // Filter attendance records by the selected week and department for the chart
  const filteredAttendance = useMemo(() => {
    const activeEmployeeMap = new Map(activeEmployees.map(e => [e.employeeId, e.department]));

    return attendanceRecords.filter(record => {
      // Check if the record's date is within the selected week
      const dateMatch = record.date >= weekDates.start && record.date <= weekDates.end;
      if (!dateMatch) return false;

      // Check if the employee is active and matches the department filter
      const department = activeEmployeeMap.get(record.employeeId);
      if (!department) return false; // Employee is not active

      const deptMatch = selectedDept === "All" || department === selectedDept;
      return deptMatch;
    });
  }, [attendanceRecords, activeEmployees, selectedDept, weekDates]);
  
  // --- Helper Functions for UI ---
  const formatWeekRange = (start, end) => {
    const startDate = new Date(`${start}T00:00:00`);
    const endDate = new Date(`${end}T00:00:00`);
    const options = { month: 'short', day: 'numeric', year: 'numeric' };
    return `${startDate.toLocaleDateString('en-US', options)} - ${endDate.toLocaleDateString('en-US', options)}`;
  };

 

  if (loading) return <div>Loading...</div>;
  if (error) return <div>{error}</div>;
  return (
    <div className="min-h-screen bg-gray-50 p-4 md:p-6">
      {/* Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6">
        <div className="bg-white p-6 rounded-xl shadow-lg cursor-pointer hover:shadow-xl transition flex flex-col items-center" onClick={() => navigate("/employees")}>
          <FaUsers className="text-3xl text-blue-600 mb-2" />
          <h3 className="text-gray-600 font-semibold">Total Employees</h3>
          <p className="text-3xl font-extrabold text-gray-800">{statCards.totalEmployees}</p>
        </div>
        <div className="bg-white p-6 rounded-xl shadow-lg cursor-pointer hover:shadow-xl transition flex flex-col items-center">
          <FaCalendarAlt className="text-3xl text-yellow-500 mb-2" />
          <h3 className="text-gray-600 font-semibold">On Leave Today</h3>
          <p className="text-3xl font-extrabold text-gray-800">{statCards.onLeaveToday}</p>
        </div>
        <div className="bg-white p-6 rounded-xl shadow-lg cursor-pointer hover:shadow-xl transition flex flex-col items-center" onClick={() => navigate("/leave-management", { state: { defaultStatus: "Pending" } })}>
          <FaClipboardList className="text-3xl text-purple-600 mb-2" />
          <h3 className="text-gray-600 font-semibold">Pending Leaves</h3>
          <p className="text-3xl font-extrabold text-gray-800">{statCards.pendingLeaves}</p>
        </div>
        <div className="bg-white p-6 rounded-xl shadow-lg flex flex-col items-center">
          <FaBuilding className="text-3xl text-green-600 mb-2" />
          <h3 className="text-gray-600 font-semibold">Departments</h3>
          <p className="text-3xl font-extrabold text-gray-800">{statCards.totalDepartments}</p>
        </div>
      </div>

      {/* Week Navigation & Filters */}
      <div className="mt-8 bg-white p-4 rounded-xl shadow-lg flex flex-col lg:flex-row justify-between items-center gap-4">
        <div className="flex items-center gap-2">
          <button onClick={() => setCurrentWeek(currentWeek - 1)} className="p-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition shadow"><FaChevronLeft /></button>
          <span className="font-semibold text-gray-700 w-64 text-center">{formatWeekRange(weekDates.start, weekDates.end)}</span>
          <button onClick={() => setCurrentWeek(currentWeek + 1)} className="p-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition shadow"><FaChevronRight /></button>
          {currentWeek !== 0 && <button onClick={() => setCurrentWeek(0)} className="p-2 bg-green-500 text-white rounded-lg hover:bg-green-600 transition shadow" title="Go to Current Week"><FaSyncAlt /></button>}
        </div>
        <div className="w-full lg:w-auto">
            <select value={selectedDept} onChange={(e) => setSelectedDept(e.target.value)} className="border border-gray-300 px-4 py-2 rounded-lg w-full lg:w-64 font-semibold text-gray-700 focus:ring-2 focus:ring-blue-500 outline-none">
              <option value="All">All Departments</option>
              {departmentList.map((dept) => <option key={dept} value={dept}>{dept}</option>)}
            </select>
        </div>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6 mt-6">
        <div className="col-span-1 xl:col-span-2 bg-white p-6 rounded-xl shadow-lg">
          <h3 className="text-blue-700 font-bold text-lg mb-4">Weekly Attendance Overview</h3>
          <div className="w-full h-80">
            {/* The AttendanceChart component receives the accurately filtered data */}
            <AttendanceChart data={filteredAttendance} />
          </div>
        </div>
        <div className="col-span-1 bg-white p-6 rounded-xl shadow-lg">
          <h3 className="text-blue-700 font-bold text-lg mb-4">Employee Distribution</h3>
          <div className="w-full h-80">
            {/* The DepartmentPieChart receives the accurately processed active employee data */}
            <DepartmentPieChart data={activeEmployees} />
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;