// import React, { useState,useEffect } from "react";
// import { NoticeContext } from "./NoticeContext";

// const API_URL = "http://localhost:8181/api/notices";

// export const NoticeProvider = ({ children }) => {
//   const [notices, setNotices] = useState([]);

//    useEffect(() => {
//     fetchNotices();
//     const interval = setInterval(fetchNotices, 5000);
//     return () => clearInterval(interval);
//   }, []);
  

//    const fetchNotices = async () => {
//     try {
//       const res = await fetch(API_URL);
//       const data = await res.json();
//       // backend returns oldest → newest, reverse for UI
//       setNotices(data.reverse());
//     } catch (err) {
//       console.error("Error fetching notices:", err);
//     }
//   };
//   // Add a new notice
//  const addNotice = async (title, message, author = "Admin") => {
//     try {
//       const res = await fetch(API_URL, {
//         method: "POST",
//         headers: { "Content-Type": "application/json" },
//         body: JSON.stringify({ title, message, author }),
//       });
//       if (res.ok) {
//         fetchNotices(); // refresh list
//       }
//     } catch (err) {
//       console.error("Error adding notice:", err);
//     }
//   };

//   // Update an existing notice
//   const updateNotice = async (id, title, message, author = "Admin") => {
//     try {
//       const res = await fetch(`${API_URL}/${id}`, {
//         method: "PUT",
//         headers: { "Content-Type": "application/json" },
//         body: JSON.stringify({
//           title,
//           message,
//           author,
//           date: new Date().toISOString().split("T")[0], // maintain backend date
//         }),
//       });
//       if (res.ok) {
//         fetchNotices();
//       }
//     } catch (err) {
//       console.error("Error updating notice:", err);
//     }
//   };
//   // Delete a notice
//   const deleteNotice = async (id) => {
//     try {
//       const res = await fetch(`${API_URL}/${id}`, { method: "DELETE" });
//       if (res.ok) {
//         fetchNotices();
//       }
//     } catch (err) {
//       console.error("Error deleting notice:", err);
//     }
//   };

//   return (
//     <NoticeContext.Provider value={{ notices, addNotice, updateNotice, deleteNotice }}>
//       {children}
//     </NoticeContext.Provider>
//   );
// };
