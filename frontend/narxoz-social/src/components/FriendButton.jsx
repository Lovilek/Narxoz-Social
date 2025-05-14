// src/components/FriendButton.jsx
import React from "react";

const FriendButton = ({ status, requestId, userId, token, onStatusChange }) => {
  const apiBase = "http://127.0.0.1:8000/api/friends";

  const handleSend = async () => {
    await fetch(`${apiBase}/send/${userId}/`, {
      method: "POST",
      headers: { Authorization: `Bearer ${token}` },
    });
    onStatusChange();
  };

  const handleCancel = async () => {
    await fetch(`${apiBase}/cancel/${requestId}/`, {
      method: "DELETE",
      headers: { Authorization: `Bearer ${token}` },
    });
    onStatusChange();
  };

  const handleRespond = async (response) => {
    await fetch(`${apiBase}/respond/${requestId}/`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ action: response }),
    });
    onStatusChange();
  };

  const handleRemove = async () => {
    await fetch(`${apiBase}/remove/${userId}/`, {
      method: "DELETE",
      headers: { Authorization: `Bearer ${token}` },
    });
    onStatusChange();
  };

  if (status === "no_relation") {
    return (
      <button onClick={handleSend} className="friend-btn">
        ➕ Добавить в друзья
      </button>
    );
  }

  if (status === "outgoing") {
    return (
      <button onClick={handleCancel} className="friend-btn gray">
        ⏳ Заявка отправлена
      </button>
    );
  }

  if (status === "incoming") {
    return (
      <>
        <button onClick={() => handleRespond("accept")} className="friend-btn">
          ✅ Принять
        </button>
        <button onClick={() => handleRespond("decline")} className="friend-btn gray">
          ❌ Отклонить
        </button>
      </>
    );
  }

  if (status === "friends") {
    return (
      <button onClick={handleRemove} className="friend-btn red">
        👥 Удалить из друзей
      </button>
    );
  }

  return null;
};

export default FriendButton;
