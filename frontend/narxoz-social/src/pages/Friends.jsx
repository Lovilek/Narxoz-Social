import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import FriendCard from "../components/FriendCard";
import LocalSearchBar from "../components/LocalSearchBar";
import apiClient from "../utils/apiClient";

const TABS = ["All", "Incoming", "Outgoing", "Declined"];

const Friends = () => {
  const [activeTab, setActiveTab] = useState("All");

  const [friends,  setFriends]  = useState([]);
  const [incoming, setIncoming] = useState([]);
  const [outgoing, setOutgoing] = useState([]);
  const [declined, setDeclined] = useState([]);

  const [loading,  setLoading]  = useState(true);
  const nav = useNavigate();

  /* ---------- загрузить все списки ---------- */
  const loadAll = async () => {
    setLoading(true);
    try {
      const [fRes, incRes, outRes] = await Promise.all([
        apiClient.get("/friends/list/"),
        apiClient.get("/friends/incoming/"),
        apiClient.get("/friends/outgoing/"),
      ]);

      setFriends(fRes.data);
      setIncoming(incRes.data.filter((r) => r.status === "pending"));
      setOutgoing(outRes.data.filter((r) => r.status === "pending"));
      setDeclined([
        ...incRes.data.filter((r) => r.status === "declined"),
        ...outRes.data.filter((r) => r.status === "declined"),
      ]);
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => { loadAll(); }, []);

  /* ---------- действия ---------- */
  const accept  = (id)  => apiClient.post(`/friends/respond/${id}/`, { action: "accept"  }).then(loadAll);
  const decline = (id)  => apiClient.post(`/friends/respond/${id}/`, { action: "decline" }).then(loadAll);
  const cancel  = (id)  => apiClient.delete(`/friends/cancel/${id}/`).then(loadAll);
  const remove  = (id)  => apiClient.delete(`/friends/remove/${id}/`).then(loadAll);

  /* «Повторить» отклонённый — просто пытаемся снова отправить */
  const resend = async (userId) => {
    try { await apiClient.post(`/friends/send/${userId}/`); } catch {/* 400 = уже висит pending */}
    loadAll();
  };

  /* ---------- mapper для LocalSearchBar ---------- */
  const userResult = (u) => ({
    id: u.id,
    label: `${u.full_name} (@${u.nickname})`,
    onClick: () => nav(`/profile/${u.id}`),
  });

  /* ---------- рендер вкладок ---------- */
  const renderContent = () => {
    if (loading) return <p>Загрузка...</p>;

    if (activeTab === "All") {
      return friends.length
        ? friends.map((u) => (
            <FriendCard
              key={u.id}
              user={u}
              onSecondary={() => remove(u.id)}
              secondaryLabel="🗑 Удалить"
            />
          ))
        : <p>Нет друзей</p>;
    }

    if (activeTab === "Incoming") {
      return incoming.length
        ? incoming.map((r) => (
            <FriendCard
              key={r.id}
              user={r.from_user}
              extraInfo={<div style={{ fontSize: 12 }}>хочет добавить вас</div>}
              onPrimary={() => accept(r.id)}
              primaryLabel="✅ Принять"
              onSecondary={() => decline(r.id)}
              secondaryLabel="❌ Отклонить"
            />
          ))
        : <p>Нет входящих заявок</p>;
    }

    if (activeTab === "Outgoing") {
      return outgoing.length
        ? outgoing.map((r) => (
            <FriendCard
              key={r.id}
              user={r.to_user}
              extraInfo={<div style={{ fontSize: 12 }}>ожидает подтверждения</div>}
              onSecondary={() => cancel(r.id)}
              secondaryLabel="🚫 Отменить"
            />
          ))
        : <p>Нет исходящих заявок</p>;
    }

    if (activeTab === "Declined") {
      return declined.length
        ? declined.map((r) => {
            const userObj =
              r.from_user.id === r.to_user.id
                ? r.to_user
                : r.from_user.id === r.id
                ? r.to_user
                : r.from_user;

            return (
              <FriendCard
                key={`${r.id}_decl`}
                user={userObj}
                extraInfo={<div style={{ fontSize: 12 }}>❌ Запрос отклонён</div>}
                onPrimary={() => resend(userObj.id)}
                primaryLabel="🔁 Повторить"
              />
            );
          })
        : <p>Нет отклонённых заявок</p>;
    }

    return null;
  };

  /* ---------- UI ---------- */
  return (
    <div style={styles.wrapper}>
      <h2>Друзья</h2>

      {/* поиск по любым пользователям */}
      <LocalSearchBar
        endpoint="/search/users/"
        mapResult={userResult}
        placeholder="Найти пользователя…"
      />

      {/* вкладки */}
      <div style={styles.tabs}>
        {TABS.map((t) => (
          <button
            key={t}
            onClick={() => setActiveTab(t)}
            style={{ ...styles.tab, ...(activeTab === t ? styles.activeTab : {}) }}
          >
            {t}
          </button>
        ))}
      </div>

      <div style={{ marginTop: 20 }}>{renderContent()}</div>
    </div>
  );
};

/* ---------- inline-стили ---------- */
const styles = {
  wrapper: {
    maxWidth: 700,
    margin: "0 auto",
    padding: 28,
    background: "#D50032",
    color: "white",
    borderRadius: 12,
  },
  tabs: {
    display: "flex",
    gap: 10,
    marginTop: 16,
  },
  tab: {
    background: "#ffffff22",
    border: "none",
    padding: "8px 14px",
    borderRadius: 6,
    cursor: "pointer",
    color: "white",
    fontWeight: 600,
  },
  activeTab: {
    background: "white",
    color: "#D50032",
  },
};

export default Friends;
