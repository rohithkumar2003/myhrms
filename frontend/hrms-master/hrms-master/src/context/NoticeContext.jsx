// src/context/NoticeContext.jsx
import React, { createContext, useContext, useState, useEffect } from "react";

export const NoticeContext = createContext();
const API_URL = "http://localhost:8181/api/notices";

export const NoticeProvider = ({ children }) => {
  const [notices, setNotices] = useState([]);

  const token = localStorage.getItem("token"); // JWT from login

  // -------------------- Fetch Notices (GET) --------------------
  const fetchNotices = async () => {
    try {
      const res = await fetch(API_URL, {
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      if (!res.ok) {
        console.error("Failed to fetch notices:", res.status);
        return;
      }
      const data = await res.json();
      setNotices(data.reverse()); // newest first
    } catch (err) {
      console.error("Error fetching notices:", err);
    }
  };

  useEffect(() => {
    fetchNotices();
    const interval = setInterval(fetchNotices, 5000); // refresh every 5s
    return () => clearInterval(interval);
  }, []);

  // -------------------- Add Notice (POST) --------------------
  const addNotice = async (title, message, author) => {
    try {
      const res = await fetch(API_URL, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ title, message, author }),
      });
      if (!res.ok) throw new Error("Failed to add notice");
      const newNotice = await res.json();
      setNotices(prev => [newNotice, ...prev]);
    } catch (err) {
      console.error(err);
      throw err;
    }
  };

  // -------------------- Update Notice (PUT) --------------------
  const updateNotice = async (id, title, message) => {
    try {
      const res = await fetch(`${API_URL}/${id}`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ title, message }),
      });
      if (!res.ok) throw new Error("Failed to update notice");
      const updatedNotice = await res.json();
      setNotices(prev => prev.map(n => (n.id === id ? updatedNotice : n)));
    } catch (err) {
      console.error(err);
      throw err;
    }
  };

  // -------------------- Delete Notice (DELETE) --------------------
  const deleteNotice = async (id) => {
    try {
      const res = await fetch(`${API_URL}/${id}`, {
        method: "DELETE",
        headers: { Authorization: `Bearer ${token}` },
      });
      if (!res.ok) throw new Error("Failed to delete notice");
      setNotices(prev => prev.filter(n => n.id !== id));
    } catch (err) {
      console.error(err);
      throw err;
    }
  };

  return (
    <NoticeContext.Provider value={{
      notices,
      setNotices,
      addNotice,
      updateNotice,
      deleteNotice
    }}>
      {children}
    </NoticeContext.Provider>
  );
};

// ✅ Custom hook
export const useNotice = () => useContext(NoticeContext);
