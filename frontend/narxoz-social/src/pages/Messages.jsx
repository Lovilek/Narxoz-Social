import React, { useEffect, useState } from "react";
import { useSelector }        from "react-redux";
import { useNavigate }        from "react-router-dom";
import apiClient              from "../utils/apiClient";
import LocalSearchBar         from "../components/LocalSearchBar";
import MessagesList           from "../components/chat/MessagesList";
import "../assets/css/MessagesList.css";

const TABS = ["All", "Personals", "Groups"];

export default function Messages() {
  const [chats, setChats]     = useState([]);
  const [userMap, setUserMap] = useState({});
  const [tab,    setTab]      = useState("All");
  const currentUser           = useSelector((s) => s.auth.user);
  const navigate              = useNavigate();

  // Загрузить чаты и подгрузить профили собеседников
  const loadChats = async () => {
    const { data } = await apiClient.get("/chats/allchats/");
    console.log("loaded chats:", data);
    setChats(data);

    const otherIds = data
      .filter((c) => c.type === "direct")
      .map((c) => c.members.find((id) => id !== currentUser.id))
      .filter((id) => id && !userMap[id]);

    if (otherIds.length) {
      const map = { ...userMap };
      await Promise.all(
        otherIds.map(async (id) => {
          const { data } = await apiClient.get(`/users/profile/${id}/`);
          map[id] = data;
        })
      );
      setUserMap(map);
    }
  };

  useEffect(() => {
    loadChats();
    const onRead = () => loadChats();
    window.addEventListener("chat-read", onRead);
    return () => window.removeEventListener("chat-read", onRead);
  }, []);

  // Отфильтровать по табу
  const visible = chats.filter((c) => {
    if (tab === "Personals") return c.type === "direct";
    if (tab === "Groups")    return c.type === "group";
    return true;
  });

  // Mapper для LocalSearchBar (если нужен)
  const mapper = (c) => {
    const isGroup = c.type === "group";
    const title = isGroup
      ? c.name
      : (() => {
          const otherId = c.members.find((i) => i !== currentUser.id);
          const o = userMap[otherId] || {};
          return o.full_name || o.nickname || "Диалог";
        })();
    return {
      id:    c.id,
      label: isGroup ? `# ${title}` : title,
      onClick: () =>
        navigate(isGroup ? `/groups/${c.id}` : `/messages/${c.id}`),
    };
  };

  return (
    <div>
      <h2 className="text-xl font-bold text-center mt-6 mb-4">Мои чаты</h2>

      <LocalSearchBar
        endpoint="/search/chats/"
        mapResult={mapper}
        placeholder="Поиск чатов…"
      />

      <div className="chat-tabs">
        {TABS.map((t) => (
          <button
            key={t}
            onClick={() => setTab(t)}
            className={`chat-tab ${tab === t ? "active" : ""}`}
          >
            {t}
          </button>
        ))}
      </div>

      <MessagesList chats={visible} userMap={userMap} />
    </div>
  );
}
