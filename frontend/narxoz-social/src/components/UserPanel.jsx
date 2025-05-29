// src/components/UserPanel.jsx
import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { fetchProfile } from "../store/authSlice";
import { useNavigate } from "react-router-dom";
import apiClient from "../utils/apiClient";
import MyPostCard from "./MyPostCard";
import "../assets/css/UserPanel.css";

import moreIcon from "../assets/icons/more.svg";
import chatIcon from "../assets/icons/Chats.svg";

const UserPanel = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { token, user } = useSelector((s) => s.auth);

  const [tab, setTab] = useState("posts");
  const [posts, setPosts] = useState([]);
  const [friends, setFriends] = useState([]);
  const [organizations, setOrganizations] = useState([]);
  const [imagesByPost, setImagesByPost] = useState({});
  const [likesByPost, setLikesByPost] = useState({});

  useEffect(() => {
    if (!user && token) dispatch(fetchProfile());
  }, [user, token, dispatch]);

  useEffect(() => {
    if (!token) return;

    const fetchData = async () => {
      try {
        const { data } = await apiClient.get("/posts/user/");
        const postsArr = Array.isArray(data) ? data : data.results || [];
        setPosts(postsArr);

        const imgs = {}, likes = {};
        for (const p of postsArr) {
          const [imgRes, likeRes] = await Promise.all([
            apiClient.get(`/posts/image-list/${p.id}/`),
            apiClient.get(`/posts/${p.id}/likes/`)
          ]);
          imgs[p.id] = imgRes.data?.results || imgRes.data || [];
          likes[p.id] = likeRes.data?.length || 0;
        }

        setImagesByPost(imgs);
        setLikesByPost(likes);

        const friendsRes = await apiClient.get("/friends/list/");
        setFriends(friendsRes.data || []);

        const orgRes = await apiClient.get("/users/organizations/");
        setOrganizations(orgRes.data || []);
      } catch (e) {
        console.error("Ошибка при загрузке данных:", e);
      }
    };

    fetchData();
  }, [token]);

  const avatarUrl = user?.avatar_path
    ? `${user.avatar_path}?${Date.now()}`
    : "/avatar.jpg";

  const handleChat = async (userId) => {
    try {
      const res = await apiClient.post(`/chats/direct/${userId}/`);
      navigate(`/messages/${res.data.chat_id}`);
    } catch (err) {
      console.error("Ошибка при создании чата:", err);
    }
  };

  return (
    <div className="user-panel">
      <div className="user-info">
        <img src={avatarUrl} alt="User" className="user-avatar" />
        <div className="user-text">
          <h3 className="user-name">{user?.full_name || "Имя Фамилия"}</h3>
          <p className="user-nick">{user?.nickname || "@nickname"}</p>
          <p className="user-login">{user?.login || "SXXXXXXX"}</p>
        </div>
        <button onClick={() => navigate("/profile/edit")} className="user-edit-btn">⋮</button>
      </div>

      <hr style={{ margin: "16px 0", borderColor: "#fff" }} />

      <div className="user-tabs">
        <div className={`tab ${tab === "posts" ? "active" : ""}`} onClick={() => setTab("posts")}>Posts</div>
        <div className={`tab ${tab === "friends" ? "active" : ""}`} onClick={() => setTab("friends")}>Friends</div>
        <div className={`tab ${tab === "orgs" ? "active" : ""}`} onClick={() => setTab("orgs")}>Groups</div>
      </div>

      <div className="tab-content posts-tab custom-scroll">
        {tab === "posts" && (
          posts.length ? posts.map((p) => (
            <MyPostCard
              key={p.id}
              post={p}
              images={imagesByPost[p.id] || []}
              likeCount={likesByPost[p.id] || 0}
              onClick={() => navigate(`/posts/${p.id}/edit`)}
            />
          )) : <p className="up-empty">Нет постов.</p>
        )}

        {tab === "friends" && (
          friends.length ? friends.map((f) => (
            <div key={f.id} className="org-card">
              <img src={f.avatar_path || "/avatar.jpg"} alt={f.nickname} className="org-avatar" />
              <div className="org-text">
                <strong>{f.full_name}</strong>
                <div className="org-sub">@{f.nickname}</div>
              </div>
              <div className="org-actions">
                <img src={chatIcon} alt="chat" onClick={() => handleChat(f.id)} />
                <img src={moreIcon} alt="more" onClick={() => navigate(`/profile/${f.id}`)} />
              </div>
            </div>
          )) : <p className="up-empty">Нет друзей.</p>
        )}

        {tab === "orgs" && (
          organizations.length ? organizations.map((o) => (
            <div key={o.id} className="org-card">
              <img src={`http://127.0.0.1:8000${o.avatar_path}`} alt={o.nickname} className="org-avatar" />
              <div className="org-text">
                <strong>{o.full_name}</strong>
                <div className="org-sub">Программирование<br />561K подписчиков</div>
              </div>
              <div className="org-actions">
                <img src={moreIcon} alt="more" onClick={() => navigate(`/profile/${o.id}`)} />
              </div>
            </div>
          )) : <p className="up-empty">Организации не найдены.</p>
        )}
      </div>
    </div>
  );
};

export default UserPanel;
