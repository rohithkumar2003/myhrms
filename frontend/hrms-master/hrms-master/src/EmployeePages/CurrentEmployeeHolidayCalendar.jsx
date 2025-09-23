import React, { useContext, useMemo, useState } from "react";
import { HolidayCalendarContext } from "../context/HolidayCalendarProvider";
import Calendar from "react-calendar";
import "react-calendar/dist/Calendar.css";

const CurrentEmployeeHolidayCalendar = () => {
  const { holidays } = useContext(HolidayCalendarContext);

  const currentYear = new Date().getFullYear();
  const today = new Date();
  const [selectedDate, setSelectedDate] = useState(today);
  const [filter, setFilter] = useState("upcoming");
  const [searchTerm, setSearchTerm] = useState("");
  const [tooltipData, setTooltipData] = useState(null);
  const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 });

  // Normalize date to midnight for reliable comparison
  const normalizeDateKey = (dateLike) => {
    const d = new Date(dateLike);
    d.setHours(0, 0, 0, 0);
    return d.getTime();
  };

  const todayKey = normalizeDateKey(today);

  // Filter upcoming holidays (today & future), skip Sundays
  const upcomingHolidays = useMemo(
    () =>
      holidays
        .filter(
          (h) =>
            normalizeDateKey(h.date) >= todayKey &&
            new Date(h.date).getDay() !== 0 // Exclude Sundays
        )
        .sort((a, b) => new Date(a.date) - new Date(b.date)),
    [holidays, todayKey]
  );

  // Filter previous holidays (before today), skip Sundays
  const previousHolidays = useMemo(
    () =>
      holidays
        .filter(
          (h) =>
            normalizeDateKey(h.date) < todayKey &&
            new Date(h.date).getDay() !== 0 // Exclude Sundays
        )
        .sort((a, b) => new Date(b.date) - new Date(a.date)),
    [holidays, todayKey]
  );

  // Helper: group holiday objects by date, merging names/descriptions
  const groupByDate = (arr) => {
    const map = new Map();
    arr.forEach((h) => {
      const key = h.date;
      const existing = map.get(key);
      if (existing) {
        existing.names.push(h.name);
        if (h.description) existing.descriptions.push(h.description);
      } else {
        map.set(key, {
          date: h.date,
          names: [h.name],
          descriptions: h.description ? [h.description] : [],
        });
      }
    });

    return Array.from(map.values())
      .map((item) => ({
        date: item.date,
        name: item.names.join(", "),
        description: item.descriptions.length ? item.descriptions.join(" | ") : "",
      }))
      .sort((a, b) => new Date(a.date) - new Date(b.date));
  };

  // Get filtered and searched holidays
  const getFilteredHolidays = () => {
    const baseList = filter === "upcoming" ? upcomingHolidays : previousHolidays;
    const grouped = groupByDate(baseList);
    
    if (!searchTerm) return grouped;
    
    return grouped.filter(h => 
      h.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      h.description.toLowerCase().includes(searchTerm.toLowerCase())
    );
  };

  const filteredHolidays = useMemo(() => getFilteredHolidays(), [filter, upcomingHolidays, previousHolidays, searchTerm]);
  const upcomingGroupedCount = useMemo(() => groupByDate(upcomingHolidays).length, [upcomingHolidays]);
  const previousGroupedCount = useMemo(() => groupByDate(previousHolidays).length, [previousHolidays]);

  // Get holiday for a specific date
  const getHolidayForDate = (date) => {
    const dateStr = date.toISOString().slice(0, 10);
    return holidays.find(h => h.date === dateStr && new Date(h.date).getDay() !== 0);
  };

  // Handle mouse move for tooltip positioning
  const handleMouseMove = (e, date) => {
    const holiday = getHolidayForDate(date);
    if (holiday) {
      setMousePosition({ x: e.clientX, y: e.clientY });
      setTooltipData({
        holiday,
        date
      });
    }
  };

  const handleMouseLeave = () => {
    setTooltipData(null);
  };

  // Calculate days until next holiday
  const nextHoliday = upcomingHolidays[0];
  const daysUntilNext = nextHoliday ? Math.ceil((new Date(nextHoliday.date) - today) / (1000 * 60 * 60 * 24)) : null;

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-100 to-slate-200">
      {/* Header */}
      <div className="bg-white border-b border-slate-300 sticky top-0 z-10 shadow-sm">
        <div className="max-w-7xl mx-auto px-6 lg:px-8 py-6">
          <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-4">
            <div>
              <h1 className="text-3xl font-bold text-slate-900">
                Holiday Calendar {currentYear}
              </h1>
              <p className="text-slate-600 text-sm mt-1">Plan your time off and stay updated with company holidays</p>
            </div>
            
            <div className="flex items-center">
              <div className="relative">
                <svg className="absolute left-3 top-1/2 transform -translate-y-1/2 text-slate-400 w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
                <input
                  type="text"
                  placeholder="Search holidays..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10 pr-4 py-2.5 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm bg-white shadow-sm w-64"
                />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-6 lg:px-8 py-8">
        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div className="bg-white rounded-lg border border-slate-200 shadow-sm p-6 hover:shadow-md transition-shadow duration-200">
            <div className="flex items-center">
              <div className="flex-1">
                <p className="text-sm font-semibold text-slate-600 uppercase tracking-wide">Next Holiday</p>
                <p className="text-2xl font-bold text-slate-900 mt-1">
                  {daysUntilNext !== null ? `${daysUntilNext} days` : "None"}
                </p>
                {nextHoliday && (
                  <p className="text-sm text-slate-500 mt-1 truncate">{nextHoliday.name}</p>
                )}
              </div>
              <div className="w-12 h-12 bg-blue-600 rounded-lg flex items-center justify-center">
                <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg border border-slate-200 shadow-sm p-6 hover:shadow-md transition-shadow duration-200">
            <div className="flex items-center">
              <div className="flex-1">
                <p className="text-sm font-semibold text-slate-600 uppercase tracking-wide">Upcoming</p>
                <p className="text-2xl font-bold text-slate-900 mt-1">{upcomingGroupedCount}</p>
                <p className="text-sm text-emerald-600 mt-1">This year</p>
              </div>
              <div className="w-12 h-12 bg-emerald-600 rounded-lg flex items-center justify-center">
                <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
                </svg>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg border border-slate-200 shadow-sm p-6 hover:shadow-md transition-shadow duration-200">
            <div className="flex items-center">
              <div className="flex-1">
                <p className="text-sm font-semibold text-slate-600 uppercase tracking-wide">Completed</p>
                <p className="text-2xl font-bold text-slate-900 mt-1">{previousGroupedCount}</p>
                <p className="text-sm text-slate-500 mt-1">This year</p>
              </div>
              <div className="w-12 h-12 bg-slate-600 rounded-lg flex items-center justify-center">
                <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
            </div>
          </div>
        </div>

        {/* Main Content */}
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* Calendar */}
          <div className="lg:col-span-3">
            <div className="bg-white rounded-lg border border-slate-200 shadow-sm p-6 h-full">
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-xl font-semibold text-slate-900">Calendar View</h3>
                <div className="flex items-center gap-6 text-sm">
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 bg-blue-600 rounded-full"></div>
                    <span className="text-slate-600 font-medium">Today</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 bg-emerald-600 rounded-full"></div>
                    <span className="text-slate-600 font-medium">Holiday</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-3 h-3 bg-slate-400 rounded-full"></div>
                    <span className="text-slate-600 font-medium">Weekend</span>
                  </div>
                </div>
              </div>
              
              <div className="relative h-[600px]">
                <Calendar
                  value={selectedDate}
                  onChange={setSelectedDate}
                  className="corporate-calendar w-full h-full"
                  tileClassName={({ date }) => {
                    const dateStr = date.toISOString().slice(0, 10);
                    const isHoliday = holidays.some(h => h.date === dateStr && new Date(h.date).getDay() !== 0);
                    const isToday = date.toDateString() === today.toDateString();
                    const isWeekend = date.getDay() === 0 || date.getDay() === 6;
                    
                    let classes = "calendar-tile";
                    if (isToday) classes += " today-tile";
                    else if (isHoliday) classes += " holiday-tile";
                    else if (isWeekend) classes += " weekend-tile";
                    
                    return classes;
                  }}
                  onMouseMove={(e) => {
                    const target = e.target.closest('.react-calendar__tile');
                    if (target) {
                      const dateAttr = target.getAttribute('aria-label');
                      if (dateAttr) {
                        const date = new Date(dateAttr);
                        handleMouseMove(e, date);
                      }
                    }
                  }}
                  onMouseLeave={handleMouseLeave}
                  prev2Label={null}
                  next2Label={null}
                />
              </div>
            </div>
          </div>

          {/* Sidebar */}
          <div className="lg:col-span-1">
            {/* Filter Card */}
            <div className="bg-white rounded-lg border border-slate-200 shadow-sm p-6 mb-6">
              <h4 className="text-lg font-semibold text-slate-900 mb-4">Filter Holidays</h4>
              <div className="space-y-3">
                <label className={`flex items-center justify-between p-3 rounded-lg border cursor-pointer transition-all duration-200 ${
                  filter === "upcoming" 
                    ? "border-blue-300 bg-blue-50" 
                    : "border-slate-200 hover:border-slate-300 hover:bg-slate-50"
                }`}>
                  <div className="flex items-center gap-3">
                    <input
                      type="radio"
                      name="holidayFilter"
                      value="upcoming"
                      checked={filter === "upcoming"}
                      onChange={() => setFilter("upcoming")}
                      className="w-4 h-4 text-blue-600 focus:ring-blue-500"
                    />
                    <span className="font-medium text-slate-700">Upcoming</span>
                  </div>
                  <span className="px-2 py-1 bg-blue-100 text-blue-700 rounded-full text-sm font-medium">
                    {upcomingGroupedCount}
                  </span>
                </label>

                <label className={`flex items-center justify-between p-3 rounded-lg border cursor-pointer transition-all duration-200 ${
                  filter === "previous" 
                    ? "border-slate-300 bg-slate-50" 
                    : "border-slate-200 hover:border-slate-300 hover:bg-slate-50"
                }`}>
                  <div className="flex items-center gap-3">
                    <input
                      type="radio"
                      name="holidayFilter"
                      value="previous"
                      checked={filter === "previous"}
                      onChange={() => setFilter("previous")}
                      className="w-4 h-4 text-slate-600 focus:ring-slate-500"
                    />
                    <span className="font-medium text-slate-700">Previous</span>
                  </div>
                  <span className="px-2 py-1 bg-slate-100 text-slate-700 rounded-full text-sm font-medium">
                    {previousGroupedCount}
                  </span>
                </label>
              </div>
            </div>

            {/* Holiday List */}
            <div className="bg-white rounded-lg border border-slate-200 shadow-sm overflow-hidden h-[600px]">
              <div className="p-6 border-b border-slate-200">
                <div className="flex items-center justify-between">
                  <h3 className="text-lg font-semibold text-slate-900">
                    {filter === "upcoming" ? "Upcoming" : "Previous"} Holidays
                  </h3>
                  <span className="text-sm text-slate-500 bg-slate-100 px-2 py-1 rounded-full font-medium">
                    {filteredHolidays.length}
                  </span>
                </div>
              </div>

              <div className="h-full overflow-y-auto custom-scrollbar">
                {filteredHolidays.length === 0 ? (
                  <div className="p-12 text-center">
                    <div className="w-16 h-16 bg-slate-100 rounded-lg flex items-center justify-center mx-auto mb-4">
                      <svg className="w-8 h-8 text-slate-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                    </div>
                    <p className="text-slate-500 font-medium">No holidays found</p>
                    {searchTerm && (
                      <p className="text-slate-400 text-sm mt-1">Try adjusting your search</p>
                    )}
                  </div>
                ) : (
                  <div className="pb-6">
                    {filteredHolidays.map((holiday, index) => {
                      const holidayDate = new Date(holiday.date);
                      const isUpcoming = filter === "upcoming";
                      const daysAway = isUpcoming 
                        ? Math.ceil((holidayDate - today) / (1000 * 60 * 60 * 24))
                        : Math.ceil((today - holidayDate) / (1000 * 60 * 60 * 24));
                      
                      return (
                        <div
                          key={holiday.date}
                          className="p-4 hover:bg-slate-50 cursor-pointer transition-colors duration-200 border-b border-slate-100 last:border-b-0"
                          onClick={() => setSelectedDate(holidayDate)}
                        >
                          <div className="flex items-start justify-between">
                            <div className="flex-1 min-w-0">
                              <h5 className="font-semibold text-slate-900 truncate hover:text-blue-600 transition-colors">
                                {holiday.name}
                              </h5>
                              <p className="text-sm text-slate-600 mt-1">
                                {holidayDate.toLocaleDateString('en-US', { 
                                  weekday: 'long',
                                  month: 'long', 
                                  day: 'numeric' 
                                })}
                              </p>
                              {holiday.description && (
                                <p className="text-xs text-slate-500 mt-2 line-clamp-2">
                                  {holiday.description}
                                </p>
                              )}
                            </div>
                            <span className={`ml-3 text-xs px-2 py-1 rounded-full flex-shrink-0 font-medium ${
                              isUpcoming
                                ? daysAway <= 7 
                                  ? "bg-red-100 text-red-700" 
                                  : "bg-blue-100 text-blue-700"
                                : "bg-slate-100 text-slate-600"
                            }`}>
                              {isUpcoming 
                                ? daysAway === 0 ? "Today" : daysAway === 1 ? "Tomorrow" : `${daysAway}d`
                                : `${daysAway}d ago`
                              }
                            </span>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Floating Tooltip */}
      {tooltipData && (
        <div 
          className="fixed z-50 bg-slate-900 text-white p-3 rounded-lg shadow-xl text-sm max-w-xs pointer-events-none border border-slate-700"
          style={{ 
            left: mousePosition.x + 10, 
            top: mousePosition.y - 10,
            transform: 'translateY(-100%)'
          }}
        >
          <div className="font-semibold text-white">{tooltipData.holiday.name}</div>
          {tooltipData.holiday.description && (
            <div className="text-slate-300 text-xs mt-1">{tooltipData.holiday.description}</div>
          )}
          <div className="text-slate-400 text-xs mt-2">
            {tooltipData.date.toLocaleDateString('en-US', { 
              weekday: 'long', 
              month: 'long', 
              day: 'numeric' 
            })}
          </div>
        </div>
      )}

      <style>{`
        .corporate-calendar {
          font-family: inherit;
          background: transparent;
          border: none;
          width: 100%;
          height: 100%;
        }
        
        .corporate-calendar .react-calendar__navigation {
          background: transparent;
          margin-bottom: 1.5rem;
          height: auto;
        }
        
        .corporate-calendar .react-calendar__navigation button {
          background: white;
          border: 1px solid rgb(226, 232, 240);
          border-radius: 6px;
          color: rgb(15, 23, 42);
          font-weight: 600;
          padding: 10px 16px;
          transition: all 0.2s;
          min-width: 44px;
          height: 40px;
        }
        
        .corporate-calendar .react-calendar__navigation button:hover {
          background: rgb(248, 250, 252);
          border-color: rgb(100, 116, 139);
        }
        
        .corporate-calendar .react-calendar__navigation button:disabled {
          background: rgb(248, 250, 252);
          color: rgb(148, 163, 184);
        }
        
        .corporate-calendar .react-calendar_month-view_weekdays {
          background: rgb(248, 250, 252);
          border-radius: 6px;
          margin-bottom: 1rem;
          padding: 10px 0;
          border: 1px solid rgb(226, 232, 240);
        }
        
        .corporate-calendar .react-calendar_month-viewweekdays_weekday {
          color: rgb(51, 65, 85);
          font-weight: 600;
          text-align: center;
          font-size: 0.75rem;
          text-transform: uppercase;
          letter-spacing: 0.05em;
          padding: 4px;
        }
        
        .corporate-calendar .react-calendar_month-view_days {
          display: grid !important;
          grid-template-columns: repeat(7, 1fr) !important;
          gap: 3px !important;
          height: calc(100% - 140px) !important;
        }
        
        .corporate-calendar .calendar-tile {
          background: white !important;
          border: 1px solid rgb(226, 232, 240) !important;
          border-radius: 6px !important;
          margin: 0 !important;
          height: 100% !important;
          min-height: 60px !important;
          padding: 8px !important;
          font-size: 0.9rem !important;
          font-weight: 500 !important;
          color: rgb(15, 23, 42) !important;
          transition: all 0.2s !important;
          position: relative !important;
          display: flex !important;
          align-items: center !important;
          justify-content: center !important;
          cursor: pointer !important;
        }
        
        .corporate-calendar .calendar-tile:hover {
          background: rgb(219, 234, 254) !important;
          border-color: rgb(59, 130, 246) !important;
          transform: translateY(-1px) !important;
          box-shadow: 0 4px 8px rgba(59, 130, 246, 0.15) !important;
        }
        
        .corporate-calendar .today-tile,
        .corporate-calendar .react-calendar__tile.today-tile {
          background: rgb(37, 99, 235) !important;
          color: white !important;
          border-color: rgb(29, 78, 216) !important;
          font-weight: 600 !important;
          box-shadow: 0 2px 4px rgba(37, 99, 235, 0.3) !important;
        }
        
        .corporate-calendar .today-tile:hover,
        .corporate-calendar .react-calendar__tile.today-tile:hover {
          background: rgb(29, 78, 216) !important;
          transform: translateY(-1px) !important;
          box-shadow: 0 4px 8px rgba(37, 99, 235, 0.4) !important;
        }
        
        .corporate-calendar .holiday-tile,
        .corporate-calendar .react-calendar__tile.holiday-tile {
          background: rgb(5, 150, 105) !important;
          color: white !important;
          border-color: rgb(4, 120, 87) !important;
          font-weight: 600 !important;
          box-shadow: 0 2px 4px rgba(5, 150, 105, 0.3) !important;
        }
        
        .corporate-calendar .holiday-tile:hover,
        .corporate-calendar .react-calendar__tile.holiday-tile:hover {
          background: rgb(16, 185, 129) !important;
          transform: translateY(-1px) !important;
          box-shadow: 0 4px 8px rgba(5, 150, 105, 0.4) !important;
        }
        
        .corporate-calendar .weekend-tile,
        .corporate-calendar .react-calendar__tile.weekend-tile {
          color: rgb(100, 116, 139) !important;
          background: rgb(248, 250, 252) !important;
          border-color: rgb(203, 213, 225) !important;
        }
        
        .corporate-calendar .weekend-tile:hover,
        .corporate-calendar .react-calendar__tile.weekend-tile:hover {
          background: rgb(219, 234, 254) !important;
          border-color: rgb(59, 130, 246) !important;
        }
        
        .corporate-calendar .react-calendar__tile--active {
          background: rgb(51, 65, 85) !important;
          color: white !important;
          border-color: rgb(30, 41, 59) !important;
        }
        
        .corporate-calendar .react-calendar__tile--neighboringMonth {
          color: rgb(156, 163, 175) !important;
          background: white !important;
        }
        
        .corporate-calendar .react-calendar__tile--neighboringMonth:hover {
          background: rgb(219, 234, 254) !important;
          border-color: rgb(59, 130, 246) !important;
          color: rgb(100, 116, 139) !important;
        }
        
        .line-clamp-2 {
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
        
        .custom-scrollbar::-webkit-scrollbar {
          width: 6px;
        }
        
        .custom-scrollbar::-webkit-scrollbar-track {
          background: rgb(248, 250, 252);
          border-radius: 3px;
        }
        
        .custom-scrollbar::-webkit-scrollbar-thumb {
          background: rgb(203, 213, 225);
          border-radius: 3px;
        }
        
        .custom-scrollbar::-webkit-scrollbar-thumb:hover {
          background: rgb(148, 163, 184);
        }
      `}</style>
    </div>
  );
};

export default CurrentEmployeeHolidayCalendar;