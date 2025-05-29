import React, { useState, useRef, useEffect } from "react";

const MessageBubble = ({ msg, currentUserId, sender, onEdit, onDelete, userRole }) => {
  const isOwn = msg.sender === currentUserId;

  /* ------ контекст-меню ------ */
  const [visible, setVis] = useState(false);
  const [pos, setPos]     = useState({ x: 0, y: 0 });
  const menuRef = useRef();

  useEffect(() => {
    const h = (e) => {
      if (menuRef.current && !menuRef.current.contains(e.target)) {
        setVis(false);
      }
    };
    document.addEventListener("click", h);
    return () => document.removeEventListener("click", h);
  }, []);

  const showMenu = (e) => {
    // Только модератор или админ могут вызывать меню
    if (userRole !== "moderator" && userRole !== "admin") return;
    e.preventDefault();
    setPos({ x: e.pageX, y: e.pageY });
    setVis(true);
  };

  return (
    <div
      className={`message-bubble ${isOwn ? "message-own" : "message-other"}`}
      onContextMenu={showMenu}
    >
      {!isOwn && sender && (
        <div className="sender-info">
          <img src={sender.avatar_path || "/avatar.jpg"} alt="ava" className="sender-avatar" />
          <span className="sender-name">@{sender.nickname}</span>
        </div>
      )}

      <div className="message-text">{msg.text}</div>
      <div className="message-meta">
        {new Date(msg.created_at).toLocaleTimeString()}
      </div>

      {visible && (
        <div
          ref={menuRef}
          className="context-menu"
          style={{ top: pos.y, left: pos.x }}
        >
          <div onClick={() => { setVis(false); onEdit?.(msg); }}>✏️ Редактировать</div>
          <div onClick={() => { setVis(false); onDelete?.(msg); }}>🗑️ Удалить</div>
        </div>
      )}
    </div>
  );
};

export default MessageBubble;
