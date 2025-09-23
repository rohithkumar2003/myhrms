import { useContext, useState, useRef, useEffect } from "react";
import { AuthContext } from "../../context/AuthContext";
import { useNavigate } from "react-router-dom";
import { NotificationContext } from "../../context/NotificationContext";
import { FaBell, FaUserCircle, FaChevronDown, FaSignOutAlt, FaUser, FaKey, FaCog } from "react-icons/fa"; // Import FaCog

const Navbar = () => {
  const { logout } = useContext(AuthContext);
  const navigate = useNavigate();
  const [showMenu, setShowMenu] = useState(false);
  const menuRef = useRef(null);
  const { unreadCount } = useContext(NotificationContext);

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setShowMenu(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <nav className="h-16 bg-gradient-to-r from-blue-600 via-blue-500 to-blue-700 flex items-center justify-between px-6 shadow-lg relative z-10">
      <div className="flex items-center gap-3">
        <h1 className="text-2xl font-bold text-white tracking-wide drop-shadow">HRMS Admin</h1>
      </div>

      <div className="flex items-center gap-6">
        <div
          className="relative cursor-pointer group"
          onClick={() => navigate("/admin/notifications")}
        >
          <FaBell className="text-2xl text-white group-hover:text-yellow-300 transition" />
          {unreadCount > 0 && (
            <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center animate-bounce shadow-lg">
              {unreadCount}
            </span>
          )}
        </div>

        <div
          ref={menuRef}
          className="relative flex items-center gap-2 cursor-pointer select-none"
          onClick={() => setShowMenu((prev) => !prev)}
        >
          <FaUserCircle className="text-3xl text-white shadow" />
          <span className="text-white font-semibold hidden md:inline">Admin</span>
          <FaChevronDown className={`text-white ml-1 transition-transform duration-200 ${showMenu ? 'rotate-180' : ''}`} />

          {showMenu && (
            <div className="absolute top-12 right-0 bg-white border rounded-lg shadow-lg w-48 z-50 text-base animate-fade-in">
              <div
                onClick={() => { navigate("/admin/profile"); setShowMenu(false); }}
                className="flex items-center gap-3 px-4 py-3 hover:bg-blue-50 text-gray-700 cursor-pointer transition-all"
              >
                <FaUser className="text-blue-600" /> View Profile
              </div>
              <div
                onClick={() => { navigate("/admin/change-password"); setShowMenu(false); }}
                className="flex items-center gap-3 px-4 py-3 hover:bg-blue-50 text-gray-700 cursor-pointer transition-all"
              >
                <FaKey className="text-blue-600" /> Change Password
              </div>
              {/* --- NEW SETTINGS LINK --- */}
              <div
                onClick={() => { navigate("/admin/settings"); setShowMenu(false); }}
                className="flex items-center gap-3 px-4 py-3 hover:bg-blue-50 text-gray-700 cursor-pointer transition-all border-t"
              >
                <FaCog className="text-blue-600" /> Application Settings
              </div>
              <div
                onClick={handleLogout}
                className="flex items-center gap-3 px-4 py-3 text-red-500 hover:bg-blue-50 cursor-pointer transition-all border-t"
              >
                <FaSignOutAlt /> Logout
              </div>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;