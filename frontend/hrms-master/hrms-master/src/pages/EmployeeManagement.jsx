import { useNavigate } from "react-router-dom";
import { useContext, useState, useMemo } from "react";
import { EmployeeContext } from "../context/EmployeeContext";
import { FaUser, FaEdit, FaTrash, FaRedo, FaDownload } from "react-icons/fa";
import * as XLSX from "xlsx";
import { saveAs } from "file-saver";




// Centralized department color mapping
const DEPARTMENT_COLORS = {
  HR: { bg: "bg-pink-100", text: "text-pink-700" },
  Engineering: { bg: "bg-blue-100", text: "text-blue-700" },
  Sales: { bg: "bg-green-100", text: "text-green-700" },
  Marketing: { bg: "bg-yellow-100", text: "text-yellow-700" },
  Finance: { bg: "bg-purple-100", text: "text-purple-700" },
  IT: { bg: "bg-blue-100", text: "text-blue-700" },
  Admin: { bg: "bg-gray-100", text: "text-gray-700" },
  Operations: { bg: "bg-orange-100", text: "text-orange-700" },
};

// Helper: Get current department from last experience entry with lastWorkingDate === "Present"
const getCurrentDepartment = (employee) => {
  if (employee && Array.isArray(employee.experienceDetails)) {
    const currentExp = employee.experienceDetails.find(
      (exp) => exp.lastWorkingDate === "Present"
    );
    return currentExp?.department || "";
  }
  return "";
};

const EmployeeRow = ({ emp, idx, navigate, onDeactivateClick }) => {
  const isEven = idx % 2 === 0;
  const currentDepartment = getCurrentDepartment(emp);
  return (
    <tr
      className={`border-t transition duration-150 ${
        isEven ? "bg-gray-50" : "bg-white"
      } hover:bg-blue-50`}
    >
      <td className="p-4 font-mono font-semibold text-blue-700">
        {emp.employeeId}
      </td>
      <td className="p-4 flex items-center gap-2">
        <div className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center text-blue-700 font-bold border border-gray-300">
          {emp.name?.split(" ").map((n) => n[0]).join("")}
        </div>
        <span className="font-semibold text-gray-800">{emp.name}</span>
      </td>
      <td className="p-4">
        <span
          className={`px-2 py-1 rounded text-xs font-bold ${
            DEPARTMENT_COLORS[currentDepartment]?.bg || "bg-gray-100"
          } ${DEPARTMENT_COLORS[currentDepartment]?.text || "text-gray-700"}`}
        >
          {currentDepartment || "Unknown"}
        </span>
      </td>
      <td className="p-4 text-gray-700">{emp.email}</td>
      <td className="p-4">
        <div className="flex flex-row items-center gap-2">
          <button
            onClick={() => navigate(`/employee/${emp.employeeId}/profile`)}
            className="bg-gray-100 text-blue-700 px-2 py-1 rounded hover:bg-blue-100 flex items-center gap-1 font-semibold shadow"
            title="View Profile"
          >
            <FaUser /> Profile
          </button>
          <button
            onClick={() => navigate(`/employees/edit/${emp.employeeId}`)}
            className="bg-gray-100 text-green-700 px-2 py-1 rounded hover:bg-green-100 flex items-center gap-1 font-semibold shadow"
            title="Edit Employee"
          >
            <FaEdit /> Edit
          </button>
          <button
            onClick={() => onDeactivateClick(emp)}
            className="bg-gray-100 text-orange-700 px-2 py-1 rounded hover:bg-orange-100 flex items-center gap-1 font-semibold shadow"
            title="Deactivate Employee"
          >
            <FaTrash /> Deactivate
          </button>
        </div>
      </td>
    </tr>
  );
};

const InactiveEmployeeRow = ({ emp, navigate }) => {
  const currentDepartment = getCurrentDepartment(emp);
  return (
    <tr className="border-t transition duration-150 bg-gray-300 opacity-60 hover:bg-gray-400">
      <td className="p-4 font-mono font-semibold text-gray-600">
        {emp.employeeId}
      </td>
      <td className="p-4 flex items-center gap-2">
        <div className="w-8 h-8 rounded-full bg-gray-400 flex items-center justify-center text-gray-600 font-bold border border-gray-400">
          {emp.name?.split(" ").map((n) => n[0]).join("")}
        </div>
        <span className="font-semibold text-gray-600">{emp.name} (Inactive)</span>
      </td>
      <td className="p-4">
        <span className={`px-2 py-1 rounded text-xs font-bold ${DEPARTMENT_COLORS[currentDepartment]?.bg || "bg-gray-400"} ${DEPARTMENT_COLORS[currentDepartment]?.text || "text-gray-700"}`}>
          {currentDepartment || "Unknown"}
        </span>
      </td>
      <td className="p-4 text-gray-600">{emp.email}</td>
      <td className="p-4">
        <div className="flex flex-row items-center gap-2">
          <button
            onClick={() => navigate(`/employee/${emp.employeeId}/profile`)}
            className="bg-gray-200 text-gray-600 px-2 py-1 rounded hover:bg-gray-300 flex items-center gap-1 font-semibold shadow"
            title="View Profile"
          >
            <FaUser /> Profile
          </button>
          <button
            onClick={() => navigate(`/employees/reactivate/${emp.employeeId}`)}
            className="bg-green-100 text-green-700 px-2 py-1 rounded hover:bg-green-200 flex items-center gap-1 font-semibold shadow"
            title="Reactivate Employee"
          >
            <FaRedo /> Reactivate
          </button>
        </div>
      </td>
    </tr>
  );
};


// Modal form for deactivation
function DeactivateModal({ open, employee, onClose, onSubmit }) {
  const [endDate, setEndDate] = useState("");
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");

  if (!open || !employee) return null;

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!endDate) {
      setError("End Date is required.");
      return;
    }
    if (!reason.trim()) {
      setError("Reason is required.");
      return;
    }
    setError("");
    onSubmit({ endDate, reason });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
      <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-md">
        <h3 className="text-xl font-bold mb-4">Deactivate {employee.name}</h3>
        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          <div>
            <label className="block font-semibold mb-1">End Date</label>
            <input
              type="date"
              value={endDate}
              onChange={e => setEndDate(e.target.value)}
              className="border border-gray-300 px-3 py-2 rounded w-full"
              required
            />
          </div>
          <div>
            <label className="block font-semibold mb-1">Reason</label>
            <textarea
              value={reason}
              onChange={e => setReason(e.target.value)}
              className="border border-gray-300 px-3 py-2 rounded w-full"
              required
              rows={3}
            />
          </div>
          {error && <div className="text-red-600 text-sm">{error}</div>}
          <div className="flex gap-2 justify-end mt-2">
            <button type="button" onClick={onClose} className="px-4 py-2 rounded bg-gray-200 hover:bg-gray-300 font-semibold">Cancel</button>
            <button type="submit" className="px-4 py-2 rounded bg-orange-600 text-white hover:bg-orange-700 font-semibold">Deactivate</button>
          </div>
        </form>
      </div>
    </div>
  );
}

const EmployeeManagement = () => {
  const navigate = useNavigate();
  const { employees, deactivateEmployment } = useContext(EmployeeContext);
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedDept, setSelectedDept] = useState("All");
  const [modalOpen, setModalOpen] = useState(false);
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  



  // Excel Report Generation
  const getCurrentDetails = (emp) => {
    if (emp && Array.isArray(emp.experienceDetails)) {
      const currentExp = emp.experienceDetails.find(
        (exp) => exp.lastWorkingDate === "Present"
      );
      return currentExp || {};
    }
    return {};
  };

  const prepareReportData = (emps) => {
    return emps.map((emp) => {
      const current = getCurrentDetails(emp);
      return {
        "Employee ID": emp.employeeId,
        "Full Name": emp.name,
        "Email Address": emp.email,
        "Phone Number": emp.phone,
        "Current Department": current.department || "",
        "Current Role": current.role || "",
        "Joining Date": current.joiningDate || "",
        "Current Salary": current.salary !== undefined ? current.salary : ""
      };
    });
  };

  const downloadExcelReport = (emps, filename) => {
    const data = prepareReportData(emps);
    const worksheet = XLSX.utils.json_to_sheet(data);
    const workbook = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(workbook, worksheet, "Employees");
    const excelBuffer = XLSX.write(workbook, { bookType: "xlsx", type: "array" });
    const blob = new Blob([excelBuffer], { type: "application/octet-stream" });
    saveAs(blob, filename);
  };

  // Dynamic department set from current experience (lastWorkingDate === "Present")
  const departmentSet = useMemo(() => {
    const depts = employees
      .map(emp => getCurrentDepartment(emp))
      .filter((dept, idx, arr) => dept && dept !== "N/A" && arr.indexOf(dept) === idx);
    return depts.sort();
  }, [employees]);

  // Filtered employees
  const { activeEmployees, inactiveEmployees } = useMemo(() => {
    const filtered = employees.filter((emp) => {
      const currentDepartment = getCurrentDepartment(emp);
      const matchesSearch = [emp.employeeId, emp.name, currentDepartment, emp.email]
        .some((field) =>
          (field ?? "").toString().toLowerCase().includes(searchQuery.toLowerCase())
        );
      const matchesDept = selectedDept === "All" || currentDepartment === selectedDept;
      return matchesSearch && matchesDept;
    });
    return {
      activeEmployees: filtered.filter((emp) => emp.isActive !== false),
      inactiveEmployees: filtered.filter((emp) => emp.isActive === false),
    };
  }, [employees, searchQuery, selectedDept]);

  // Modal open/close logic
  const openDeactivateModal = (emp) => {
    setSelectedEmployee(emp);
    setModalOpen(true);
  };
  const closeDeactivateModal = () => {
    setSelectedEmployee(null);
    setModalOpen(false);
  };

  // Handle modal submit
  const handleDeactivateSubmit = ({ endDate, reason }) => {
    if (selectedEmployee) {
      deactivateEmployment(selectedEmployee.employeeId, endDate, reason);
      closeDeactivateModal();
    }
  };

  return (
    <div className="min-h-screen w-full bg-gray-50 flex flex-col items-center justify-center py-0">
      <div className="w-full max-w-7xl mx-auto py-12">
        {/* Header */}
        <div className="flex flex-col md:flex-row justify-between items-center mb-10 gap-4">
          <div className="flex flex-col gap-2 md:gap-4">
            <h2 className="text-3xl font-bold text-gray-800 tracking-tight">
              Employee Management
            </h2>
            <div className="flex flex-row gap-2 mt-2">
              <button
                onClick={() => downloadExcelReport(activeEmployees, "Active_Employees_Report.xlsx")}
                className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 flex items-center gap-2 shadow font-semibold"
                title="Download Active Employees Report"
              >
                <FaDownload /> Download Active Employees Report
              </button>
              <button
                onClick={() => downloadExcelReport(inactiveEmployees, "Inactive_Employees_Report.xlsx")}
                className="bg-red-600 text-white px-4 py-2 rounded-lg hover:bg-red-700 flex items-center gap-2 shadow font-semibold"
                title="Download Inactive Employees Report"
              >
                <FaDownload /> Download Inactive Employees Report
              </button>
            </div>
          </div>
          <button
            onClick={() => navigate("/employees/add")}
            className="bg-blue-700 text-white px-6 py-3 rounded-lg hover:bg-blue-800 flex items-center gap-2 shadow font-semibold"
          >
            <FaUser /> Add Employee
          </button>
        </div>

        {/* Filter/Search */}
        <div className="flex flex-col md:flex-row gap-4 mb-10 items-center">
          <input
            type="text"
            placeholder="Search by ID, Name, Department, or Email"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full max-w-md border border-gray-300 px-4 py-2 rounded-lg shadow focus:outline-none focus:ring focus:ring-blue-200"
          />
          <select
            value={selectedDept}
            onChange={(e) => setSelectedDept(e.target.value)}
            className="border border-gray-300 px-4 py-2 rounded-lg w-full max-w-xs shadow focus:outline-none focus:ring focus:ring-blue-200 font-semibold"
          >
            <option value="All">All Departments</option>
            {departmentSet.filter(dept => dept !== "N/A" && dept !== "").map((dept) => (
              <option key={dept} value={dept}>
                {dept}
              </option>
            ))}
          </select>
        </div>

        {/* Employee Table */}
        <div className="overflow-x-auto rounded-xl shadow bg-white">
          <table className="min-w-full rounded-xl">
            <thead className="sticky top-0 z-10">
              <tr className="bg-gray-100 text-left">
                <th className="p-4">ID</th>
                <th className="p-4">Name</th>
                <th className="p-4">Department</th>
                <th className="p-4">Email</th>
                <th className="p-4">Actions</th>
              </tr>
            </thead>
            <tbody>
              {activeEmployees.length > 0 || inactiveEmployees.length > 0 ? (
                <>
                  {activeEmployees.map((emp, idx) => (
                    <EmployeeRow
                      key={`${emp.employeeId}-${emp.email}`}
                      emp={emp}
                      idx={idx}
                      navigate={navigate}
                      onDeactivateClick={openDeactivateModal}
                    />
                  ))}
                  {inactiveEmployees.map((emp) => (
                    <InactiveEmployeeRow
                      key={`${emp.employeeId}-${emp.email}-inactive`}
                      emp={emp}
                      navigate={navigate}
                    />
                  ))}
                </>
              ) : (
                <tr>
                  <td colSpan="5" className="p-4 text-center text-gray-500">
                    No matching employees found. Try clearing your search or changing filters.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Deactivate Modal */}
        <DeactivateModal
          open={modalOpen}
          employee={selectedEmployee}
          onClose={closeDeactivateModal}
          onSubmit={handleDeactivateSubmit}
        />
      </div>
    </div>
  );
};




export default EmployeeManagement;