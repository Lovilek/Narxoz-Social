// src/components/FriendCard.jsx
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import apiClient from "../utils/apiClient";

const FriendCard = ({
  user,
  extraInfo,
  onPrimary,
  primaryLabel,
  onSecondary,
  secondaryLabel,
}) => {
  const navigate = useNavigate();

  const handleChat = async () => {
    try {
      const res = await apiClient.post(`/chats/direct/${user.id}/`);
      const chatId = res.data.chat_id;
      navigate(`/messages/${chatId}`);
    } catch (err) {
      console.error("Ошибка при создании чата:", err);
    }
  };

  return (
    <div style={styles.card}>
      <Link to={`/profile/${user.id}`} style={styles.link}>
        <img
          src={user.avatar_path || "/avatar.jpg"}
          alt={user.nickname}
          style={styles.avatar}
        />
        <div>
          <strong>{user.full_name}</strong>
          <div style={{ fontSize: 13, color: "#ddd" }}>@{user.nickname}</div>
          {extraInfo && <div style={styles.extra}>{extraInfo}</div>}
        </div>
      </Link>

      {/* Кнопки действий */}
      <div style={styles.actions}>
        {onPrimary && (
          <button onClick={onPrimary} style={styles.primaryBtn}>
            {primaryLabel}
          </button>
        )}
        {onSecondary && (
          <button onClick={onSecondary} style={styles.secondaryBtn}>
            {secondaryLabel}
          </button>
        )}
        <button onClick={handleChat} style={styles.chatBtn}>
          💬
        </button>
      </div>
    </div>
  );
};

const styles = {
  card: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    background: "#ffffff22",
    padding: "10px 14px",
    borderRadius: 8,
    color: "white",
    marginBottom: 10,
  },
  link: {
    display: "flex",
    alignItems: "center",
    gap: 14,
    textDecoration: "none",
    color: "white",
  },
  avatar: {
    width: 48,
    height: 48,
    borderRadius: "50%",
    objectFit: "cover",
    border: "2px solid white",
  },
  extra: {
    marginTop: 4,
    fontSize: 12,
    color: "#aaa",
  },
  actions: {
    display: "flex",
    gap: 6,
    alignItems: "center",
  },
  primaryBtn: {
    background: "white",
    color: "#D50032",
    padding: "6px 10px",
    borderRadius: 6,
    border: "none",
    fontWeight: 600,
    cursor: "pointer",
  },
  secondaryBtn: {
    background: "#444",
    color: "#fff",
    padding: "6px 10px",
    borderRadius: 6,
    border: "none",
    fontWeight: 600,
    cursor: "pointer",
  },
  chatBtn: {
    background: "#1976d2",
    color: "white",
    padding: "6px 10px",
    borderRadius: 6,
    border: "none",
    fontWeight: 600,
    cursor: "pointer",
  },
};

export default FriendCard;
