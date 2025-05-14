// ✅ AnotherUserProfile.jsx (обновлён с учётом явного статуса)

import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { format, parseISO, isToday, isYesterday } from "date-fns";
import { ru } from "date-fns/locale";
import apiClient from "../utils/apiClient";
import LikeButton from "../components/LikeButton";
import CommentSection from "../components/CommentSection";
import FriendCard from "../components/FriendCard";

const smartDate = (iso) => {
  const d = parseISO(iso);
  if (isToday(d)) return `Сегодня, ${format(d, "HH:mm", { locale: ru })}`;
  if (isYesterday(d)) return `Вчера, ${format(d, "HH:mm", { locale: ru })}`;
  return format(d, "d MMMM yyyy, HH:mm", { locale: ru });
};

const STATUS_MAP = {
  None: { text: "👤 Не в друзьях", color: "#ccc" },
  self: { text: "🙋 Это вы", color: "#aaa" },
  friends: { text: "👥 Друзья", color: "#44dd88" },
  outgoing_request: { text: "⏳ Заявка отправлена", color: "#ffaa00" },
  incoming_request: { text: "📩 Входящая заявка", color: "#00ccff" },
  outgoing_declined_request: { text: "❌ Заявка отклонена", color: "#e66" },
  incoming_declined_request: { text: "❌ Вы отклонили", color: "#888" },
};

const AnotherUserProfile = () => {
  const { id } = useParams();
  const [user, setUser] = useState(null);
  const [posts, setPosts] = useState([]);
  const [imagesByPost, setImagesByPost] = useState({});
  const [friendStatus, setFriendStatus] = useState("None");
  const [friendsFull, setFriendsFull] = useState([]);
  const [error, setError] = useState("");

  const loadAll = async () => {
    try {
      const [uRes, pRes, sRes] = await Promise.all([
        apiClient.get(`/users/profile/${id}/`),
        apiClient.get(`/posts/user/${id}/`),
        apiClient.get(`/friends/status/${id}/`),
      ]);

      setUser(uRes.data);
      setFriendStatus(sRes.data.status || "None");

      const postsData = Array.isArray(pRes.data)
        ? pRes.data
        : pRes.data.results || [];
      setPosts(postsData);

      for (const p of postsData) {
        const { data: imgs } = await apiClient.get(`/posts/image-list/${p.id}/`);
        setImagesByPost((prev) => ({ ...prev, [p.id]: imgs }));
      }

      if (uRes.data.friends?.length) {
        const f = await Promise.all(
          uRes.data.friends.map((fr) =>
            apiClient.get(`/users/profile/${fr.id}/`).then((r) => r.data)
          )
        );
        setFriendsFull(f);
      }
    } catch (e) {
      console.error(e);
      setError("Ошибка при загрузке профиля.");
    }
  };

  useEffect(() => {
    loadAll();
  }, [id]);

  const refreshStatus = async () => {
    try {
      const { data } = await apiClient.get(`/friends/status/${id}/`);
      const status = data.status || "None";
      setFriendStatus(status);
      return status;
    } catch {
      return null;
    }
  };

  const resendAfterDecline = async () => {
    try {
      const { data: outgoing } = await apiClient.get("/friends/outgoing/");
      const found = outgoing.find(
        (r) => r.status === "declined" && r.to_user.id === Number(id)
      );
      if (!found) return false;

      await apiClient.delete(`/friends/cancel/${found.id}/`);
      await apiClient.post(`/friends/send/${id}/`);
      return true;
    } catch {
      return false;
    }
  };

  const handleSendRequest = async () => {
    try {
      await apiClient.post(`/friends/send/${id}/`);
      setFriendStatus("outgoing_request");
      setError("");
    } catch (err) {
      const errMsg = err.response?.data?.error || "Ошибка при отправке запроса.";
      if (errMsg.includes("Уже существует запрос")) {
        const revived = await resendAfterDecline();
        if (revived) {
          setFriendStatus("outgoing_request");
          setError("");
          return;
        }
      }
      const actual = await refreshStatus();
      if (actual && actual !== "None" && actual !== "self") setError("");
      else setError(errMsg);
    }
  };

  const handleRemoveFriend = async () => {
    try {
      await apiClient.delete(`/friends/remove/${id}/`);
      await refreshStatus();
    } catch (e) {
      console.error(e);
    }
  };

  const renderFriendActions = () => {
    const info = STATUS_MAP[friendStatus];
    return (
      <div>
        <span style={{ ...styles.statusBar, backgroundColor: info?.color }}>
          {info?.text || "Статус неизвестен"}
        </span>
        {friendStatus === "None" || friendStatus?.includes("declined") ? (
          <button onClick={handleSendRequest} style={styles.btnAdd}>
            ➕ Добавить в друзья
          </button>
        ) : friendStatus === "friends" ? (
          <button onClick={handleRemoveFriend} style={styles.btnRemove}>
            👥 Удалить из друзей
          </button>
        ) : null}
      </div>
    );
  };

  if (!user) return <p>Загрузка профиля...</p>;

  return (
    <div style={styles.wrapper}>
      <div style={styles.header}>
        <img
          src={user.avatar_path || "/avatar.jpg"}
          alt="avatar"
          style={styles.avatar}
        />
        <div>
          <h2>{user.full_name}</h2>
          <p>@{user.nickname}</p>
          {renderFriendActions()}
          <p style={{ marginTop: 6 }}>👥 Друзей: {friendsFull.length}</p>
        </div>
      </div>

      {error && <p style={styles.errorBar}>⚠️ {error}</p>}

      <h3 style={{ marginTop: 24 }}>Друзья пользователя</h3>
      {friendsFull.length ? (
        <div style={styles.friendList}>
          {friendsFull.slice(0, 6).map((f) => (
            <FriendCard key={f.id} user={f} />
          ))}
        </div>
      ) : (
        <p style={{ fontSize: 14 }}>Нет друзей</p>
      )}

      <hr style={{ margin: "24px 0", borderColor: "#fff" }} />

      <h3>Посты пользователя</h3>
      {posts.length ? (
        posts.map((p) => (
          <div key={p.id} style={styles.postCard}>
            <div style={styles.postHeader}>
              <img
                src={p.author_avatar_path || "/avatar.jpg"}
                alt="avatar"
                style={styles.postAvatar}
              />
              <div>
                <strong>{p.author}</strong>
                <div style={{ fontSize: 13, color: "#ddd" }}>
                  {smartDate(p.created_at)}
                </div>
              </div>
            </div>
            <p style={{ marginTop: 12 }}>{p.content}</p>
            {imagesByPost[p.id]?.length > 0 && (
              <div style={styles.imageGrid}>
                {imagesByPost[p.id].map((img) => (
                  <img
                    key={img.id}
                    src={img.image_path}
                    alt="post"
                    style={styles.postImg}
                  />
                ))}
              </div>
            )}
            <div style={{ marginTop: 10 }}>
              <LikeButton postId={p.id} />
            </div>
            <CommentSection postId={p.id} />
          </div>
        ))
      ) : (
        <p>Нет постов</p>
      )}
    </div>
  );
};

const styles = {
  wrapper: {
    maxWidth: 700,
    margin: "0 auto",
    padding: 30,
    background: "#D50032",
    color: "white",
    borderRadius: 12,
  },
  header: {
    display: "flex",
    alignItems: "center",
    gap: 20,
  },
  avatar: {
    width: 90,
    height: 90,
    borderRadius: "50%",
    objectFit: "cover",
    border: "2px solid white",
  },
  statusBar: {
    display: "inline-block",
    padding: "4px 8px",
    borderRadius: 6,
    color: "white",
    fontSize: 13,
    fontWeight: "bold",
    marginTop: 8,
    marginBottom: 6,
  },
  btnAdd: {
    background: "white",
    color: "#D50032",
    padding: "6px 10px",
    borderRadius: 6,
    fontWeight: "bold",
    border: "none",
    cursor: "pointer",
    marginTop: 6,
  },
  btnRemove: {
    background: "#8B0000",
    color: "white",
    padding: "6px 10px",
    borderRadius: 6,
    fontWeight: "bold",
    border: "none",
    cursor: "pointer",
    marginTop: 6,
  },
  errorBar: {
    background: "#fff",
    color: "#D50032",
    padding: 8,
    borderRadius: 6,
    marginTop: 12,
  },
  friendList: {
    display: "flex",
    flexDirection: "column",
    gap: 10,
    marginTop: 10,
  },
  postCard: {
    background: "#ffffff22",
    padding: 12,
    borderRadius: 8,
    marginBottom: 24,
  },
  postHeader: {
    display: "flex",
    gap: 12,
    alignItems: "center",
  },
  postAvatar: {
    width: 48,
    height: 48,
    borderRadius: "50%",
    objectFit: "cover",
    border: "1px solid white",
  },
  imageGrid: {
    display: "flex",
    flexWrap: "wrap",
    gap: 10,
    marginTop: 8,
  },
  postImg: {
    width: 140,
    borderRadius: 8,
    objectFit: "cover",
  },
};

export default AnotherUserProfile;
