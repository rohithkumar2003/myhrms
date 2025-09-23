import React, { useState, useCallback, useEffect, createContext } from "react";
import axios from "axios";

export const HolidayCalendarContext = createContext();

const API_URL = "http://localhost:8181/holidays"; // your Spring Boot API

const HolidayCalendarProvider = ({ children }) => {
  const [holidays, setHolidays] = useState([]);

  // Fetch holidays from backend on mount
  useEffect(() => {
    const fetchHolidays = async () => {
      try {
        const response = await axios.get(API_URL);
        // Sort by date ascending
        const sorted = response.data.sort((a, b) => a.date.localeCompare(b.date));
        setHolidays(sorted);
      } catch (error) {
        console.error("Failed to fetch holidays:", error);
      }
    };

    fetchHolidays();
  }, []);

  // CRUD functions
  const addHoliday = useCallback(async (holiday) => {
    try {
      const response = await axios.post(API_URL, holiday);
      setHolidays(prev => [...prev, response.data].sort((a, b) => a.date.localeCompare(b.date)));
    } catch (error) {
      console.error("Failed to add holiday:", error);
    }
  }, []);

  const editHoliday = useCallback(async (id, updated) => {
    try {
      const response = await axios.put(`${API_URL}/${id}`, updated);
      setHolidays(prev => prev.map(h => h.id === id ? response.data : h).sort((a, b) => a.date.localeCompare(b.date)));
    } catch (error) {
      console.error("Failed to edit holiday:", error);
    }
  }, []);

  const deleteHoliday = useCallback(async (id) => {
    try {
      await axios.delete(`${API_URL}/${id}`);
      setHolidays(prev => prev.filter(h => h.id !== id));
    } catch (error) {
      console.error("Failed to delete holiday:", error);
    }
  }, []);

  const getHolidayByDate = useCallback((dateStr) => {
    return holidays.find(h => h.date === dateStr);
  }, [holidays]);

  const getHolidayDates = useCallback(() => {
    return holidays.map(h => h.date).sort();
  }, [holidays]);

  return (
    <HolidayCalendarContext.Provider
      value={{
        holidays,
        addHoliday,
        editHoliday,
        deleteHoliday,
        getHolidayByDate,
        getHolidayDates,
      }}
    >
      {children}
    </HolidayCalendarContext.Provider>
  );
};

export default HolidayCalendarProvider;
