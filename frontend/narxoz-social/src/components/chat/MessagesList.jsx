import React from "react";
import { Link, useLocation } from "react-router-dom";
import { useSelector }       from "react-redux";
import "../../assets/css/MessagesList.css";

export default function MessagesList({ chats, userMap }) {
  const currentUser = useSelector((s) => s.auth.user);
  const locPath     = useLocation().pathname;

  if (!chats?.length) {
    return <div className="messages-empty">Нет чатов</div>;
  }

  return (
    <div className="messages-list">
      {chats.map((chat) => {
        const isGroup = chat.type === "group";
        const otherId = chat.members.find((id) => id !== currentUser.id);
        const other   = userMap[otherId] || {};

        const title = isGroup
          ? chat.name
          : other.full_name || other.nickname || "Диалог";

        const last  = chat.last_message?.text || "Нет сообщений";

        // Берём поле `unread` из ответа
        const rawUnread = chat.unread ?? 0;
        // Скрываем бейдж, если уже на странице этого чата
        const unread = rawUnread > 0 && !locPath.includes(chat.id) ? rawUnread : 0;

        return (
          <Link
            to={isGroup ? `/groups/${chat.id}` : `/messages/${chat.id}`}
            key={chat.id}
            className="chat-item"
          >
            <img
              src={
                isGroup
                  ? chat.avatar_url || "/group.png"
                  : other.avatar_path || "/avatar.jpg"
              }
              alt=""
              className="chat-avatar"
            />

            <div className="chat-info">
              <div className="chat-title">{title}</div>
              <div className="chat-last">{last}</div>
            </div>

            {unread > 0 && <span className="chat-badge">{unread}</span>}
          </Link>
        );
      })}
    </div>
  );
}
