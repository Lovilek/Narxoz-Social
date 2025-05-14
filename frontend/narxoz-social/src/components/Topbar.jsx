import React, { useEffect, useRef, useState } from "react";
import { useSelector } from "react-redux";
import { useNavigate } from "react-router-dom";
import apiClient from "../utils/apiClient";

import calendarIcon from "../assets/icons/calendar.svg";
import bellIcon from "../assets/icons/notification.svg";
import searchIcon from "../assets/icons/search.svg";

import "../assets/css/Topbar.css";
import SearchDropdown from "./SearchDropdown";

const Topbar = () => {
  const currentUser = useSelector((state) => state.auth.user);
  const navigate = useNavigate();

  const [query, setQuery] = useState("");
  const [results, setResults] = useState(null);
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const timerRef = useRef(null);
  const boxRef = useRef(null);

  const handleChange = (e) => {
    const q = e.target.value;
    setQuery(q);
    clearTimeout(timerRef.current);

    if (!q.trim()) {
      setResults(null);
      return;
    }

    timerRef.current = setTimeout(async () => {
      setLoading(true);
      try {
        const [glob, users] = await Promise.all([
          apiClient.get("/search/global/", { params: { q } }),
          apiClient.get("/search/users/", { params: { search: q } }),
        ]);

        setResults({
          ...glob.data,
          users: (users.data.results || []).filter((u) => u.role !== "organization"),
        });
      } catch (e) {
        console.error("Ошибка при поиске:", e);
      } finally {
        setLoading(false);
        setOpen(true);
      }
    }, 400);
  };

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (boxRef.current && !boxRef.current.contains(e.target)) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleAddUser = () => navigate("/admin/register");

  return (
    <div className="topbar" ref={boxRef}>
      <div className="search-wrapper">
        <input
          className="search-input"
          type="text"
          placeholder="Search"
          value={query}
          onChange={handleChange}
          onFocus={() => results && setOpen(true)}
        />
        <img src={searchIcon} alt="search" className="search-icon" />
      </div>

      {open && query && (
        <SearchDropdown
          query={query}
          loading={loading}
          data={results}
          onClose={() => setOpen(false)}
        />
      )}

      <div className="topbar-icons">
        <img src={calendarIcon} alt="Calendar" className="topbar-icon" />
        <img src={bellIcon} alt="Notifications" className="topbar-icon" />
        {currentUser?.role === "admin" && (
          <button onClick={handleAddUser} className="add-user-btn">
            ADD USER
          </button>
        )}
      </div>
    </div>
  );
};

export default Topbar;
