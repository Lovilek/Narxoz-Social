import React, { useEffect, useRef, useState } from "react";
import { useSelector } from "react-redux";
import apiClient       from "../../utils/apiClient";
import MessageBubble   from "./MessageBubble";
import "../../assets/css/ChatRoom.css";

const LIMIT = 20;

export default function ChatRoom({ chatId }) {
  const user  = useSelector((s) => s.auth.user);
  const token = useSelector((s) => s.auth.token);

  const [msgs, setMsgs]     = useState([]);
  const [userMap, setUmap]  = useState({});
  const [info, setInfo]     = useState(null);
  const [hasMore, setMore]  = useState(true);
  const [text, setText]     = useState("");
  const [editing, setEdit]  = useState(null);

  const wsRef    = useRef(null);
  const retryRef = useRef(null);
  const listRef  = useRef(null);

  // подгрузка истории
  const loadHistory = async (before) => {
    const url = `chats/${chatId}/messages/?limit=${LIMIT}` + (before ? `&before=${before}` : "");
    const { data } = await apiClient.get(url);
    if (!data.length) return setMore(false);
    setMsgs((p) => [...data, ...p]);

    const newIds = Array.from(new Set(data.map((m) => m.sender))).filter((i) => !userMap[i]);
    if (newIds.length) {
      const map = { ...userMap };
      await Promise.all(newIds.map(async (id) => {
        const { data } = await apiClient.get(`users/profile/${id}/`);
        map[id] = data;
      }));
      setUmap(map);
    }
  };

  // инфо чата
  const loadChatInfo = async () => {
    const { data } = await apiClient.get(`chats/${chatId}/detail/`);
    setInfo(data);
    if (data.type === "direct") {
      const other = data.members.find((id) => id !== user.id);
      if (other && !userMap[other]) {
        const { data } = await apiClient.get(`users/profile/${other}/`);
        setUmap((m) => ({ ...m, [data.id]: data }));
      }
    }
  };

  // отметка read
  const markRead = async () => {
    try {
      await apiClient.post(`chats/${chatId}/read/`);
    } catch (e) {
      if (e.response?.status !== 403) console.error(e);
    }
    window.dispatchEvent(new Event("chat-read"));
  };

  // websocket
  const connectWS = () => {
    const ws = new WebSocket(`ws://127.0.0.1:8000/ws/chat/${chatId}/?token=${token}`);
    wsRef.current = ws;
    ws.onmessage = ({ data }) => {
      const m = JSON.parse(data);
      setMsgs((p) => (p.some((x) => x.id === m.id) ? p : [...p, m]));
      if (!userMap[m.sender]) {
        apiClient.get(`users/profile/${m.sender}/`).then(({ data }) =>
          setUmap((u) => ({ ...u, [data.id]: data }))
        );
      }
      setTimeout(() => (listRef.current.scrollTop = listRef.current.scrollHeight), 50);
    };
    ws.onerror = () => ws.close();
    ws.onclose = () => (retryRef.current = setTimeout(connectWS, 2000));
  };

  // init
  useEffect(() => {
    (async () => {
      await Promise.all([loadHistory(), loadChatInfo()]);
      await markRead();
      connectWS();
    })();
    return () => {
      wsRef.current?.close();
      clearTimeout(retryRef.current);
    };
  }, [chatId]);

  // отправка / редактирование
  const handleSend = async () => {
    if (!text.trim()) return;

    if (editing) {
      if (editing.sender !== user.id) {
        alert("Редактировать можно только свои сообщения");
        return;
      }
      try {
        // убираем leading slash
        await apiClient.patch(
          `chats/${chatId}/message/${editing.id}/edit/`,
          { text }
        );
        setMsgs((p) => p.map((m) => (m.id === editing.id ? { ...m, text } : m)));
        setEdit(null);
        setText("");
      } catch (e) {
        if (e.response?.status === 403) alert("Нет прав редактировать");
        else alert("Ошибка редактирования");
        console.error(e);
      }
      return;
    }

    // новое сообщение
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify({ type: "send", text }));
      setText("");
    }
  };

  // скролл
  const handleScroll = () => {
    if (listRef.current.scrollTop < 50 && hasMore && msgs.length) {
      loadHistory(msgs[0].id);
    }
  };

  // рендер
  const otherId = info?.type === "direct" && info.members.find((i) => i !== user.id);
  const other   = otherId ? userMap[otherId] : null;

  return (
    <div className="chat-container">
      <div className="chat-header">
        {info?.type === "group" && <span># {info.name}</span>}
        {info?.type === "direct" && other && (
          <>
            <img src={other.avatar_path || "/avatar.jpg"} className="chat-header-avatar" />
            <div className="chat-header-info">
              <div className="chat-header-name">{other.full_name}</div>
              <div className="chat-header-nick">@{other.nickname}</div>
            </div>
          </>
        )}
      </div>

      <div ref={listRef} className="chat-messages" onScroll={handleScroll}>
        {msgs.map((msg) => (
          <MessageBubble
            key={msg.id}
            msg={msg}
            currentUserId={user.id}
            sender={userMap[msg.sender]}
            userRole={user.role}
            onEdit={(m) => { setEdit(m); setText(m.text); }}
            onDelete={async (m) => {
              if (m.sender !== user.id) {
                alert("Удалять можно только свои сообщения");
                return;
              }
              if (!window.confirm("Удалить?")) return;
              try {
                await apiClient.delete(`chats/${chatId}/message/${m.id}/delete/`);
                setMsgs((p) => p.filter((x) => x.id !== m.id));
              } catch (e) {
                if (e.response?.status === 403) alert("Нет прав удалять");
                else alert("Ошибка удаления");
                console.error(e);
              }
            }}
          />
        ))}
      </div>

      <div className="chat-input">
        <textarea
          rows={1}
          placeholder={editing ? "Редактировать..." : "Написать..."}
          value={text}
          onChange={(e) => setText(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && !e.shiftKey && (e.preventDefault(), handleSend())}
        />
        <button onClick={handleSend}>{editing ? "Обновить" : "Отправить"}</button>
      </div>
    </div>
  );
}
