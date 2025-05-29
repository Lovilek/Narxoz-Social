import React, { useEffect, useRef, useState } from "react";
import apiClient from "../utils/apiClient";
import "../assets/css/LocalSearchBar.css";   // создай при желании

/**
 * props:
 *   endpoint   — REST url, напр. "/search/users/"
 *   mapResult  — (item) => ({ id, label, onClick })
 *   placeholder
 */
const LocalSearchBar = ({ endpoint, mapResult, placeholder = "Search…" }) => {
  const [q, setQ]         = useState("");
  const [items, setItems] = useState([]);
  const [loading, setL]   = useState(false);
  const [open, setOpen]   = useState(false);
  const timer = useRef(null);
  const box   = useRef(null);

  /* debounce fetch */
  const onChange = (e) => {
    const v = e.target.value;
    setQ(v);
    clearTimeout(timer.current);

    if (!v.trim()) { setItems([]); return; }

    timer.current = setTimeout(async () => {
      setL(true);
      try {
        const { data } = await apiClient.get(endpoint, { params: { search: v } });
        const arr = Array.isArray(data) ? data : data.results || [];
        setItems(arr.map(mapResult));
      } finally { setL(false); setOpen(true); }
    }, 300);
  };

  /* click outside => close */
  useEffect(() => {
    const h = (e) => { if (box.current && !box.current.contains(e.target)) setOpen(false); };
    document.addEventListener("mousedown", h);
    return () => document.removeEventListener("mousedown", h);
  }, []);

  return (
    <div className="lsb-box" ref={box}>
      <input
        className="lsb-input"
        placeholder={placeholder}
        value={q}
        onChange={onChange}
        onFocus={() => items.length && setOpen(true)}
      />

      {open && (
        <div className="lsb-dd">
          {loading && <div className="lsb-loading">поиск…</div>}

          {!loading && items.map((it) => (
            <div key={it.id} className="lsb-item" onClick={it.onClick}>
              {it.label}
            </div>
          ))}

          {!loading && !items.length && (
            <div className="lsb-empty">ничего не найдено</div>
          )}
        </div>
      )}
    </div>
  );
};

export default LocalSearchBar;
