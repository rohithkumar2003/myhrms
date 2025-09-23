import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';
import { BrowserRouter } from 'react-router-dom';

// Import All Application Providers
import { EmployeeProvider } from './context/EmployeeContext';
import { AttendanceProvider } from './context/AttendanceProvider';
import { LeaveRequestProvider } from './context/LeaveRequestProvider';
import { SettingsProvider } from './context/SettingsProvider'; // <-- NEW: Import the new provider
import AdminProvider from './context/AdminProvider';
import { AuthProvider } from './context/AuthProvider';
import { NotificationProvider } from "./context/NotificationProvider";
import HolidayCalendarProvider from './context/HolidayCalendarProvider';
import AddEmployee from "./pages/AddEmployee";
import { NoticeProvider } from "./context/NoticeContext";
// Import Employee-Specific Providers
import CurrentEmployeeAttendanceProvider from './EmployeeContext/CurrentEmployeeAttendanceProvider';
import CurrentEmployeeLeaveRequestProvider from './EmployeeContext/CurrentEmployeeLeaveRequestProvider';
import { CurrentEmployeeProvider } from './EmployeeContext/CurrentEmployeeProvider';
import CurrentEmployeeNotificationProvider from './EmployeeContext/CurrentEmployeeNotificationProvider';
import { CurrentEmployeeSettingsProvider } from './EmployeeContext/CurrentEmployeeSettingsProvider'; // <-- NEW: Import the new provider
ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
    <AuthProvider>
      {/* 
        The provider stack wraps your entire application. 
        The order ensures that any provider can potentially access data from a provider that wraps it.
      */}
      <EmployeeProvider>
        <LeaveRequestProvider>
          <AttendanceProvider>
            {/* The new SettingsProvider is added here, making it available to all child components */}
            <SettingsProvider>
              <AdminProvider>
                <NoticeProvider>
                
                  <NotificationProvider>
                    <CurrentEmployeeAttendanceProvider>
                      <CurrentEmployeeLeaveRequestProvider>
                        <CurrentEmployeeProvider>
                          <CurrentEmployeeNotificationProvider>
                            <HolidayCalendarProvider>
                              <CurrentEmployeeSettingsProvider>
                              <App />
                              </CurrentEmployeeSettingsProvider>
                            </HolidayCalendarProvider>
                          </CurrentEmployeeNotificationProvider>
                        </CurrentEmployeeProvider>
                      </CurrentEmployeeLeaveRequestProvider>
                    </CurrentEmployeeAttendanceProvider>
                  </NotificationProvider>
                </NoticeProvider>
              </AdminProvider>
            </SettingsProvider>
          </AttendanceProvider>
        </LeaveRequestProvider>
      </EmployeeProvider>
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>
);