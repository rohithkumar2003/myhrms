import React, { useContext } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { EmployeeContext } from '../context/EmployeeContext';

const EmployeeProfile = () => {

  const { id } = useParams();
  const navigate = useNavigate();
  const { employees } = useContext(EmployeeContext);
  const [activeTab, setActiveTab] = React.useState('personal');
  const employee = employees.find((emp) => emp.employeeId === id);

  if (!employee) return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center p-6">
      <div className="bg-white rounded-lg shadow-lg p-8 text-center max-w-md w-full border">
        <div className="w-16 h-16 bg-red-50 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg className="w-8 h-8 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L4.082 16.5c-.77.833.192 2.5 1.732 2.5z" />
          </svg>
        </div>
        <h2 className="text-xl font-semibold text-gray-900 mb-2">Employee Not Found</h2>
        <p className="text-gray-600 mb-6">The employee you're looking for doesn't exist or has been removed.</p>
        <button
          onClick={() => navigate(-1)}
          className="w-full px-6 py-3 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors duration-200 font-medium"
        >
          Go Back
        </button>
      </div>
    </div>
  );

  // Get initials for avatar
  const initials = employee.name?.split(' ').map(n => n[0]).join('').toUpperCase();

  // Get current experience (lastWorkingDate === "Present")
  const currentExp = Array.isArray(employee.experienceDetails)
    ? employee.experienceDetails.find(exp => exp.lastWorkingDate === "Present")
    : null;

  // Helper for safe access
  const safe = (val, fallback = "N/A") => (val !== undefined && val !== null && val !== "") ? val : fallback;

  // Tab content
  const renderTabContent = () => {
    if (activeTab === 'personal') {
      return (
        <div className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <InfoCard label="Full Name" value={safe(employee.name)} />
            <InfoCard label="Email Address" value={safe(employee.email)} />
            <InfoCard label="Phone Number" value={safe(employee.phone)} />
            <InfoCard label="Address" value={safe(employee.address)} />
            <InfoCard label="Emergency Contact" value={safe(employee.emergency)} />
            <InfoCard label="Date of Birth" value={safe(employee.personalDetails?.dob)} />
            <InfoCard label="Gender" value={safe(employee.personalDetails?.gender)} />
            <InfoCard label="Marital Status" value={safe(employee.personalDetails?.maritalStatus)} />
            <InfoCard label="Nationality" value={safe(employee.personalDetails?.nationality)} />
            <InfoCard label="PAN Number" value={safe(employee.personalDetails?.panNumber)} />
            <InfoCard label="Aadhaar Number" value={safe(employee.personalDetails?.aadharNumber)} />
            <InfoCard label="PAN File" value={employee.personalDetails?.panFileUrl ? (
              <a href={employee.personalDetails.panFileUrl} target="_blank" rel="noopener noreferrer" className="text-blue-600 underline">View PAN File</a>
            ) : safe(employee.personalDetails?.panFileUrl)} />
            <InfoCard label="Aadhaar File" value={employee.personalDetails?.aadharFileUrl ? (
              <a href={employee.personalDetails.aadharFileUrl} target="_blank" rel="noopener noreferrer" className="text-blue-600 underline">View Aadhaar File</a>
            ) : safe(employee.personalDetails?.aadharFileUrl)} />
          </div>
        </div>
      );
    }
    if (activeTab === 'bank') {
      return (
        <div className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <InfoCard label="Account Number" value={safe(employee.bankDetails?.accountNumber)} />
            <InfoCard label="Bank Name" value={safe(employee.bankDetails?.bankName)} />
            <InfoCard label="IFSC Code" value={safe(employee.bankDetails?.ifsc)} />
            <InfoCard label="Branch" value={safe(employee.bankDetails?.branch)} />
          </div>
        </div>
      );
    }
    if (activeTab === 'experience') {
      return (
        <div className="space-y-6">
          {/* Current Experience */}
          {currentExp && (
            <div className="bg-white border border-blue-200 rounded-lg p-6 shadow-sm mb-4">
              <h3 className="text-lg font-bold text-blue-800 mb-2">Current Employment</h3>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <InfoCard label="Employee ID" value={safe(currentExp.employeeId)} />
                <InfoCard label="Company" value={safe(currentExp.company)} />
                <InfoCard label="Department" value={safe(currentExp.department)} />
                <InfoCard label="Role/Position" value={safe(currentExp.role)} />
                <InfoCard label="Salary" value={currentExp.salary ? `₹${Number(currentExp.salary).toLocaleString()}` : "N/A"} />
                <InfoCard label="Joining Date" value={safe(currentExp.joiningDate)} />
                <InfoCard label="Status" value={safe(currentExp.lastWorkingDate)} />
                <InfoCard label="Years" value={safe(currentExp.years)} />
                <InfoCard label="Employment Type" value={safe(currentExp.employmentType)} />
                <InfoCard label="Reason for Leaving" value={safe(currentExp.reason)} />
                <InfoCard label="Experience Letter" value={currentExp.experienceLetterUrl ? (
                  <a href={currentExp.experienceLetterUrl} target="_blank" rel="noopener noreferrer" className="text-blue-600 underline">View Experience Letter</a>
                ) : safe(currentExp.experienceLetterUrl)} />
              </div>
            </div>
          )}
          {/* Past Experiences */}
          <div className="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
            <h3 className="text-lg font-bold text-gray-800 mb-2">Past Experience</h3>
            {Array.isArray(employee.experienceDetails) && employee.experienceDetails.filter(exp => exp.lastWorkingDate !== "Present").length > 0 ? (
              employee.experienceDetails
                .filter((exp, idx, arr) => exp.lastWorkingDate !== "Present" && arr.findIndex(e => e.company === exp.company && e.joiningDate === exp.joiningDate) === idx)
                .map((exp, idx) => (
                  <div key={idx} className="mb-4 pb-4 border-b last:border-b-0 last:mb-0 last:pb-0">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <InfoCard label="Employee ID" value={safe(exp.employeeId)} />
                      <InfoCard label="Company" value={safe(exp.company)} />
                      <InfoCard label="Department" value={safe(exp.department)} />
                      <InfoCard label="Role/Position" value={safe(exp.role)} />
                      <InfoCard label="Salary" value={exp.salary ? `₹${Number(exp.salary).toLocaleString()}` : "N/A"} />
                      <InfoCard label="Joining Date" value={safe(exp.joiningDate)} />
                      <InfoCard label="End Date" value={safe(exp.lastWorkingDate)} />
                      <InfoCard label="Years" value={safe(exp.years)} />
                      <InfoCard label="Employment Type" value={safe(exp.employmentType)} />
                      <InfoCard label="Reason for Leaving" value={safe(exp.reason)} />
                      <InfoCard label="Experience Letter" value={exp.experienceLetterUrl ? (
                        <a href={exp.experienceLetterUrl} target="_blank" rel="noopener noreferrer" className="text-blue-600 underline">View Experience Letter</a>
                      ) : safe(exp.experienceLetterUrl)} />
                    </div>
                  </div>
                ))
            ) : (
              <div className="text-gray-500 text-center py-4">No past experience found.</div>
            )}
          </div>
        </div>
      );
    }
    return null;
  };

  // Info Card
  const InfoCard = ({ label, value }) => (
    <div className="bg-gray-50 border border-gray-200 rounded-lg p-4">
      <div className="text-sm font-medium text-gray-500 mb-1">{label}</div>
      <div className="text-base font-semibold text-gray-900">{value}</div>
    </div>
  );

  // Tab Button
  function TabButton({ active, onClick, label }) {
    return (
      <button
        className={`py-4 px-1 border-b-2 font-medium text-sm transition-colors duration-200 focus:outline-none ${
          active
            ? 'border-blue-800 text-blue-900'
            : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
        }`}
        onClick={onClick}
      >
        {label}
      </button>
    );
  }

  // Main Layout
  return (
    <div className="min-h-screen bg-gray-50 p-4 sm:p-6">
      <div className="max-w-5xl mx-auto">
        {/* Back Button */}
        <button
          onClick={() => navigate(-1)}
          className="mb-6 flex items-center gap-2 px-4 py-2 text-gray-600 hover:text-gray-900 transition-colors duration-200 group"
        >
          <svg className="w-5 h-5 group-hover:-translate-x-1 transition-transform duration-200" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          <span className="font-medium">Back to Employee List</span>
        </button>

        <div className="bg-white rounded-lg shadow-lg overflow-hidden border border-gray-200">
          {/* Header */}
          <div className="bg-blue-800 px-8 py-12">
            <div className="flex flex-col items-center text-center">
              {/* Avatar */}
              <div className="w-24 h-24 sm:w-28 sm:h-28 rounded-full bg-blue-600 flex items-center justify-center text-3xl sm:text-4xl text-white font-bold mb-6 shadow-lg">
                {initials}
              </div>
              {/* Employee Name */}
              <h1 className="text-3xl sm:text-4xl font-bold text-white mb-3">
                {safe(employee.name)}
              </h1>
              <div className="flex flex-col sm:flex-row items-center gap-3">
                <span className="px-4 py-2 rounded-md bg-blue-700 text-white font-medium">
                  Employee ID: {safe(employee.employeeId)}
                </span>
                <span className="px-4 py-2 rounded-md bg-blue-600 text-white font-medium">
                  Department: {safe(currentExp?.department)}
                </span>
                <span className="px-4 py-2 rounded-md bg-blue-600 text-white font-medium">
                  Role: {safe(currentExp?.role)}
                </span>
                <span className="px-4 py-2 rounded-md bg-blue-600 text-white font-medium">
                  Salary: {currentExp?.salary ? `₹${Number(currentExp.salary).toLocaleString()}` : "N/A"}
                </span>
              </div>
            </div>
          </div>

          {/* Navigation Tabs */}
          <div className="border-b border-gray-200 bg-white">
            <nav className="flex justify-center">
              <div className="flex space-x-8 px-6">
                <TabButton
                  active={activeTab === 'personal'}
                  onClick={() => setActiveTab('personal')}
                  label="Personal Information"
                />
                <TabButton
                  active={activeTab === 'bank'}
                  onClick={() => setActiveTab('bank')}
                  label="Banking Details"
                />
                <TabButton
                  active={activeTab === 'experience'}
                  onClick={() => setActiveTab('experience')}
                  label="Work Experience"
                />
              </div>
            </nav>
          </div>

          {/* Tab Content */}
          <div className="p-8 bg-gray-50 min-h-[500px]">
            {renderTabContent()}
          </div>
        </div>
      </div>
    </div>
  );
};

export default EmployeeProfile;
