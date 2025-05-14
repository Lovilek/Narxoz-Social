import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useSelector } from "react-redux";
import apiClient from "../utils/apiClient";
import GroupCreateModal from "../components/groups/GroupCreateModal";
import "../assets/css/MessagesList.css"; // Переиспользуем стили

const Groups = () => {
  const [groups, setGroups] = useState([]);
  const [showModal, setShowModal] = useState(false);
  const currentUser = useSelector((state) => state.auth.user);

  const loadGroups = async () => {
    try {
      const res = await apiClient.get("/chats/groups/");
      setGroups(res.data);
    } catch (err) {
      console.error("Ошибка загрузки групп:", err);
    }
  };

  useEffect(() => {
    loadGroups();
  }, []);

  const canCreateGroup =
    currentUser && ["admin", "moderator", "teacher"].includes(currentUser.role);

  return (
    <div className="messages-list">
      <div className="flex justify-between items-center px-4 mb-4">
        <h2 className="text-xl font-bold">Групповые чаты</h2>
        {canCreateGroup && (
          <button
            onClick={() => setShowModal(true)}
            className="bg-blue-500 text-white px-4 py-2 rounded"
          >
            ➕ Создать группу
          </button>
        )}
      </div>

      {groups.length === 0 ? (
        <div className="messages-empty">Пока нет групп</div>
      ) : (
        groups.map((group) => (
          <Link to={`/groups/${group.id}`} key={group.id} className="chat-item">
            <img
              src={group.avatar_url || "/avatar.jpg"}
              className="chat-avatar"
              alt="avatar"
            />
            <div className="chat-info">
              <div className="chat-title">{group.name}</div>
              <div className="chat-last">ID: {group.id}</div>
            </div>
          </Link>
        ))
      )}

      {showModal && (
        <GroupCreateModal
          onClose={() => {
            setShowModal(false);
            loadGroups(); // Обновим список
          }}
        />
      )}
    </div>
  );
};

export default Groups;
