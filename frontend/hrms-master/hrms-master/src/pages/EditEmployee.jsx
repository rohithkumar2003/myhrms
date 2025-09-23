
import { useParams, useNavigate } from 'react-router-dom';
import { useContext, useState, useEffect } from 'react';
import { EmployeeContext } from '../context/EmployeeContext';
import { FaUser, FaEnvelope, FaBuilding, FaPhone, FaAddressCard, FaCalendarAlt, FaExclamationTriangle, FaMoneyBill, FaFlag, FaTransgender, FaCreditCard, FaUniversity, FaCodeBranch } from 'react-icons/fa';

const EditEmployee = () => {
  const { id } = useParams();
  const { employees, editEmployee } = useContext(EmployeeContext);
  const navigate = useNavigate();

  const employee = employees.find((e) => e.employeeId === id);
  // Get current experience
  const currentExp = employee && Array.isArray(employee.experienceDetails)
    ? employee.experienceDetails.find(exp => exp.lastWorkingDate === "Present")
    : {};

  // Pre-populate form data from nested structure
  const [formData, setFormData] = useState({
    name: employee?.name || "",
    email: employee?.email || "",
    phone: employee?.phone || "",
    address: employee?.address || "",
    emergency: employee?.emergency || "",
    personalDetails: {
      dob: employee?.personalDetails?.dob || "",
      gender: employee?.personalDetails?.gender || "Male",
      maritalStatus: employee?.personalDetails?.maritalStatus || "Single",
      nationality: employee?.personalDetails?.nationality || "",
      panNumber: employee?.personalDetails?.panNumber || "",
      aadharNumber: employee?.personalDetails?.aadharNumber || "",
      aadharFileUrl: employee?.personalDetails?.aadharFileUrl || "",
      panFileUrl: employee?.personalDetails?.panFileUrl || "",
    },
    bankDetails: {
      accountNumber: employee?.bankDetails?.accountNumber || "",
      bankName: employee?.bankDetails?.bankName || "",
      ifsc: employee?.bankDetails?.ifsc || "",
      branch: employee?.bankDetails?.branch || "",
    },
    experienceDetails: employee?.experienceDetails || [],
    currentDepartment: currentExp?.department || "",
    currentRole: currentExp?.role || "",
    currentSalary: currentExp?.salary || "",
    joiningDate: currentExp?.joiningDate || "",
    // For document links
    experienceLetterUrl: (() => {
      if (Array.isArray(employee?.experienceDetails)) {
        const current = employee.experienceDetails.find(exp => exp.lastWorkingDate === "Present");
        return current?.experienceLetterUrl || "";
      }
      return "";
    })(),
  });

  const [snackbar, setSnackbar] = useState("");

  useEffect(() => {
    if (employee) {
      const currentExp = Array.isArray(employee.experienceDetails)
        ? employee.experienceDetails.find(exp => exp.lastWorkingDate === "Present")
        : {};
      setFormData({
        name: employee.name || "",
        email: employee.email || "",
        phone: employee.phone || "",
        address: employee.address || "",
        emergency: employee.emergency || "",
        personalDetails: {
          dob: employee.personalDetails?.dob || "",
          gender: employee.personalDetails?.gender || "Male",
          maritalStatus: employee.personalDetails?.maritalStatus || "Single",
          nationality: employee.personalDetails?.nationality || "",
          panNumber: employee.personalDetails?.panNumber || "",
          aadharNumber: employee.personalDetails?.aadharNumber || "",
          aadharFileUrl: employee.personalDetails?.aadharFileUrl || "",
          panFileUrl: employee.personalDetails?.panFileUrl || "",
        },
        bankDetails: {
          accountNumber: employee.bankDetails?.accountNumber || "",
          bankName: employee.bankDetails?.bankName || "",
          ifsc: employee.bankDetails?.ifsc || "",
          branch: employee.bankDetails?.branch || "",
        },
        experienceDetails: employee.experienceDetails || [],
        currentDepartment: currentExp?.department || "",
        currentRole: currentExp?.role || "",
        currentSalary: currentExp?.salary || "",
        joiningDate: currentExp?.joiningDate || "",
        experienceLetterUrl: currentExp?.experienceLetterUrl || "",
      });
    }
  }, [employee]);

  // Handle changes for nested fields
  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name.startsWith("bankDetails.")) {
      const bankField = name.split(".")[1];
      setFormData(prev => ({
        ...prev,
        bankDetails: {
          ...prev.bankDetails,
          [bankField]: value,
        },
      }));
    } else if (name.startsWith("personalDetails.")) {
      const personalField = name.split(".")[1];
      setFormData(prev => ({
        ...prev,
        personalDetails: {
          ...prev.personalDetails,
          [personalField]: value,
        },
      }));
    } else {
      setFormData(prev => ({
        ...prev,
        [name]: value,
      }));
    }
  };

  // Submission logic
  const handleSubmit = (e) => {
    e.preventDefault();
    // Validate mandatory fields
    const validate = () => {
      if (!formData.name.trim()) return "Full Name is required.";
      if (!formData.email.trim()) return "Email is required.";
      if (!formData.currentDepartment.trim()) return "Department is required.";
      if (!formData.currentRole.trim()) return "Role is required.";
      if (!formData.joiningDate) return "Joining Date is required.";
      if (!formData.currentSalary || isNaN(formData.currentSalary)) return "Salary is required.";
      return "";
    };
    const err = validate();
    if (err) {
      setSnackbar(err);
      setTimeout(() => setSnackbar("") , 2500);
      return;
    }
    // Update only the existing 'current' experience object
    const updatedExperienceDetails = Array.isArray(formData.experienceDetails)
      ? formData.experienceDetails.map(exp => {
          if (exp.lastWorkingDate === "Present") {
            return {
              ...exp,
              department: formData.currentDepartment,
              role: formData.currentRole,
              salary: parseFloat(formData.currentSalary) || 0,
              joiningDate: formData.joiningDate,
              experienceLetterUrl: formData.experienceLetterUrl || exp.experienceLetterUrl,
            };
          }
          return exp;
        })
      : [];
    // Build updated employee object
    const updatedEmployee = {
      ...employee,
      name: formData.name,
      email: formData.email,
      phone: formData.phone,
      address: formData.address,
      emergency: formData.emergency,
      personalDetails: { ...formData.personalDetails },
      bankDetails: { ...formData.bankDetails },
      experienceDetails: updatedExperienceDetails,
    };
    editEmployee(id, updatedEmployee);
    setSnackbar("Employee updated successfully.");
    setTimeout(() => {
      navigate(-1);
    }, 1800);
  };

  if (!employee) return <div className="p-6 text-red-600 font-semibold">Employee not found</div>;

  return (
    <div className="p-6 min-h-screen bg-gray-50 flex flex-col items-center justify-center">
      <div className="bg-white rounded-2xl shadow-xl p-8 w-full max-w-2xl">
        <button
          type="button"
          onClick={() => navigate(-1)}
          className="mb-4 px-4 py-2 rounded-lg bg-gray-200 text-gray-700 font-semibold hover:bg-gray-300 transition shadow flex items-center gap-2"
        >
          &#8592; Go Back
        </button>
        <h2 className="text-2xl font-bold text-blue-700 mb-6 text-center">Edit Employee</h2>
        <form onSubmit={handleSubmit} className="space-y-8">
          {/* Basic Information */}
          <div className="border rounded-xl p-6 bg-blue-50/50 shadow-inner">
            <h3 className="text-xl font-bold text-blue-700 mb-4">Basic Information</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <InputField
                icon={<FaUser />}
                name="name"
                label="Full Name"
                value={formData.name}
                onChange={handleChange}
                required
              />
              <InputField
                icon={<FaEnvelope />}
                name="email"
                label="Email Address"
                type="email"
                value={formData.email}
                onChange={handleChange}
                required
              />
              <InputField
                icon={<FaPhone />}
                name="phone"
                label="Phone Number"
                value={formData.phone}
                onChange={handleChange}
              />
              <InputField
                icon={<FaAddressCard />}
                name="address"
                label="Address"
                value={formData.address}
                onChange={handleChange}
              />
              <InputField
                icon={<FaExclamationTriangle />}
                name="emergency"
                label="Emergency Contact"
                value={formData.emergency}
                onChange={handleChange}
              />
            </div>
          </div>
          {/* Job Details */}
          <div className="border rounded-xl p-6 bg-green-50/50 shadow-inner">
            <h3 className="text-xl font-bold text-green-700 mb-4">Job Details</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              
              <InputField
                icon={<FaBuilding />}
                name="currentDepartment"
                label="Department"
                value={formData.currentDepartment}
                onChange={handleChange}
                required
              />
              <InputField
                icon={<FaMoneyBill />}
                name="currentSalary"
                label="Current Salary (INR)"
                type="number"
                value={formData.currentSalary}
                onChange={handleChange}
                required
              />
              <InputField
                icon={<FaCalendarAlt />}
                name="joiningDate"
                label="Joining Date"
                type="date"
                value={formData.joiningDate}
                onChange={handleChange}
                required
              />
              <InputField
                icon={<FaBuilding />}
                name="currentRole"
                label="Job Role"
                value={formData.currentRole}
                onChange={handleChange}
                required
              />
            </div>
          </div>
          {/* Personal Details */}
          <div className="border rounded-xl p-6 bg-purple-50/50 shadow-inner">
            <h3 className="text-xl font-bold text-purple-700 mb-4">Personal Details</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <InputField
                icon={<FaCalendarAlt />}
                name="personalDetails.dob"
                label="Date of Birth"
                type="date"
                value={formData.personalDetails.dob}
                onChange={handleChange}
              />
              <InputField
                icon={<FaFlag />}
                name="personalDetails.nationality"
                label="Nationality"
                value={formData.personalDetails.nationality}
                onChange={handleChange}
              />
              <InputField
                icon={<FaAddressCard />}
                name="personalDetails.panNumber"
                label="PAN Number"
                value={formData.personalDetails.panNumber}
                onChange={handleChange}
              />
              <InputField
                icon={<FaAddressCard />}
                name="personalDetails.aadharNumber"
                label="Aadhaar Number"
                value={formData.personalDetails.aadharNumber}
                onChange={handleChange}
              />
              <div className="relative">
                <FaTransgender className="absolute left-3 top-4 text-gray-400" />
                <label htmlFor="gender-select" className="absolute left-10 text-xs text-gray-500 font-medium top-1.5">Gender</label>
                <select
                  id="gender-select"
                  name="personalDetails.gender"
                  value={formData.personalDetails.gender}
                  onChange={handleChange}
                  className="w-full pl-10 pr-4 pt-5 pb-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-400 outline-none bg-gray-50 appearance-none"
                >
                  <option value="Male">Male</option>
                  <option value="Female">Female</option>
                  <option value="Other">Other</option>
                </select>
              </div>
              <div className="relative">
                <FaExclamationTriangle className="absolute left-3 top-4 text-gray-400" />
                <label htmlFor="marital-select" className="absolute left-10 text-xs text-gray-500 font-medium top-1.5">Marital Status</label>
                <select
                  id="marital-select"
                  name="personalDetails.maritalStatus"
                  value={formData.personalDetails.maritalStatus}
                  onChange={handleChange}
                  className="w-full pl-10 pr-4 pt-5 pb-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-400 outline-none bg-gray-50 appearance-none"
                >
                  <option value="Single">Single</option>
                  <option value="Married">Married</option>
                  <option value="Divorced">Divorced</option>
                  <option value="Widowed">Widowed</option>
                </select>
              </div>
            </div>
          </div>
          {/* Document Details */}
          <div className="border rounded-xl p-6 bg-blue-50/50 shadow-inner">
            <h3 className="text-xl font-bold text-blue-700 mb-4">Document Details</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <FileLink url={formData.personalDetails.aadharFileUrl} label="Aadhaar File" />
              <FileLink url={formData.personalDetails.panFileUrl} label="PAN File" />
              <FileLink url={formData.experienceLetterUrl} label="Experience Letter" />
            </div>
          </div>
          {/* Bank Details */}
          <div className="border rounded-xl p-6 bg-yellow-50/50 shadow-inner">
            <h3 className="text-xl font-bold text-yellow-700 mb-4">Bank Details</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <InputField
                icon={<FaCreditCard />}
                name="bankDetails.accountNumber"
                label="Account Number"
                value={formData.bankDetails.accountNumber}
                onChange={handleChange}
              />
              <InputField
                icon={<FaUniversity />}
                name="bankDetails.bankName"
                label="Bank Name"
                value={formData.bankDetails.bankName}
                onChange={handleChange}
              />
              <InputField
                icon={<FaCodeBranch />}
                name="bankDetails.ifsc"
                label="IFSC Code"
                value={formData.bankDetails.ifsc}
                onChange={handleChange}
              />
              <InputField
                icon={<FaAddressCard />}
                name="bankDetails.branch"
                label="Bank Branch"
                value={formData.bankDetails.branch}
                onChange={handleChange}
              />
            </div>
          </div>
          <div className="flex gap-3 mt-6">
            <button type="submit" className="bg-green-600 text-white px-4 py-2 rounded-lg hover:bg-green-700 w-full font-semibold shadow">
              Update
            </button>
            <button type="button" onClick={() => navigate(-1)} className="bg-gray-200 text-gray-700 px-4 py-2 rounded-lg hover:bg-gray-300 w-full font-semibold shadow">
              Cancel
            </button>
          </div>
        </form>
        {snackbar && (
          <div className="fixed bottom-6 left-1/2 transform -translate-x-1/2 bg-green-600 text-white px-6 py-3 rounded shadow-lg z-50 animate-fadein">
            {snackbar}
          </div>
        )}
      </div>
    </div>
  );
};

// Reusable Input Field Component
const InputField = ({ icon, label, ...props }) => (
  <div className="relative">
    <div className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">{icon}</div>
    <label htmlFor={props.name} className="absolute left-10 text-xs text-gray-500 font-medium top-1.5">
      {label}
    </label>
    <input
      id={props.name}
      {...props}
      className="w-full pl-10 pr-4 pt-5 pb-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-400 outline-none bg-gray-50"
    />
  </div>
);

// Reusable FileLink Component
import { FaDownload } from 'react-icons/fa';
const FileLink = ({ url, label }) => {
  if (!url) return (
    <div className="flex items-center gap-2 text-gray-400">
      <FaDownload />
      <span className="text-sm">{label}: Not Available</span>
    </div>
  );
  return (
    <a
      href={url}
      target="_blank"
      rel="noopener noreferrer"
      className="flex items-center gap-2 text-blue-700 hover:underline font-semibold"
    >
      <FaDownload />
      <span className="text-sm">{label}</span>
    </a>
  );
};

export default EditEmployee;
