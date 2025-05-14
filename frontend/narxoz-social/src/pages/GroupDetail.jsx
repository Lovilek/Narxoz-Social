import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useSelector } from "react-redux";
import apiClient from "../utils/apiClient";

const GroupDetail = () => {
  const { chatId } = useParams();
  const navigate = useNavigate();
  const currentUser = useSelector((state) => state.auth.user);

  const [group, setGroup] = useState(null);
  const [members, setMembers] = useState([]);
  const [editMode, setEditMode] = useState(false);
  const [name, setName] = useState("");
  const [avatarFile, setAvatarFile] = useState(null);
  const [newUserId, setNewUserId] = useState("");
  const [showAddUser, setShowAddUser] = useState(false);

  const isPrivileged = ["admin", "moderator", "organization", "teacher"].includes(currentUser.role);
  const isOwner = group?.owner_id === currentUser.id;

  const loadGroup = async () => {
    try {
      const res = await apiClient.get(`/chats/${chatId}/detail/`);
      setGroup(res.data);
      setName(res.data.name);

      const memberProfiles = await Promise.all(
        res.data.members.map(async (id) => {
          try {
            const profile = await apiClient.get(`/users/profile/${id}/`);
            return profile.data;
          } catch {
            return { id, nickname: "Неизвестно", avatar_path: null };
          }
        })
      );
      setMembers(memberProfiles);
    } catch (err) {
      console.error("Ошибка загрузки группы:", err);
    }
  };

  useEffect(() => {
    loadGroup();
  }, [chatId]);

  const handleLeave = async () => {
    try {
      await apiClient.post(`/chats/${chatId}/leave/`);
      navigate("/groups");
    } catch {
      alert("Ошибка при выходе");
    }
  };

  const handleDelete = async () => {
    try {
      await apiClient.delete(`/chats/${chatId}/delete/`);
      navigate("/groups");
    } catch {
      alert("Ошибка при удалении");
    }
  };

  const handleAddUser = async () => {
    if (!newUserId.trim()) return;
    try {
      await apiClient.post(`/chats/${chatId}/add/`, { user_id: newUserId });
      setNewUserId("");
      setShowAddUser(false);
      await loadGroup();
    } catch (err) {
      alert(err.response?.data?.error || "Ошибка при добавлении");
    }
  };

  const handleRemoveUser = async (id) => {
    if (id === group.owner_id) {
      alert("Нельзя удалить владельца");
      return;
    }
    try {
      await apiClient.post(`/chats/${chatId}/remove/`, { user_id: id });
      await loadGroup();
    } catch (err) {
      alert("Ошибка при удалении");
    }
  };

  const handleUpdateGroup = async () => {
    const formData = new FormData();
    formData.append("name", name);
    if (avatarFile) formData.append("avatar", avatarFile);

    try {
      await apiClient.patch(`/chats/${chatId}/update/`, formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      setEditMode(false);
      setAvatarFile(null);
      await loadGroup();
    } catch {
      alert("Ошибка при обновлении");
    }
  };

  if (!group) return <div className="p-4">Загрузка...</div>;

  return (
    <div className="max-w-2xl mx-auto p-4">
      <div className="flex items-center gap-4 mb-4">
        <img
          src={group.avatar_url || "/avatar.jpg"}
          alt="avatar"
          className="w-20 h-20 rounded-full border object-cover"
        />
        {editMode ? (
          <div className="flex flex-col gap-2">
            <input
              className="border px-2 py-1"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
            <input type="file" onChange={(e) => setAvatarFile(e.target.files[0])} />
            <button className="bg-green-600 text-white px-3 py-1 rounded" onClick={handleUpdateGroup}>
              Сохранить
            </button>
          </div>
        ) : (
          <div>
            <h2 className="text-2xl font-bold">{group.name}</h2>
            <p className="text-sm text-gray-500">ID: {chatId}</p>
          </div>
        )}
      </div>

      {(isPrivileged || isOwner) && !editMode && (
        <button
          className="bg-yellow-500 text-white px-4 py-2 rounded mb-4"
          onClick={() => setEditMode(true)}
        >
          ✏️ Редактировать группу
        </button>
      )}

      <h3 className="text-lg font-semibold mb-2">Участники:</h3>
      <ul className="space-y-2 mb-6">
        {members.map((m) => (
          <li key={m.id} className="flex items-center gap-2">
            <img
              src={m.avatar_path || "/avatar.jpg"}
              alt="avatar"
              className="w-8 h-8 rounded-full object-cover"
            />
            <span className="text-sm">{m.full_name || m.nickname}</span>
            {(isPrivileged || isOwner) && m.id !== group.owner_id && (
              <button
                className="ml-auto text-sm text-red-600 hover:underline"
                onClick={() => handleRemoveUser(m.id)}
              >
                Удалить
              </button>
            )}
          </li>
        ))}
      </ul>

      {(isPrivileged || isOwner) && (
        <>
          <button
            onClick={() => setShowAddUser((prev) => !prev)}
            className="bg-blue-500 text-white px-4 py-2 rounded mb-4"
          >
            ➕ Добавить участника
          </button>

          {showAddUser && (
            <div className="mb-4 flex gap-2">
              <input
                type="number"
                placeholder="ID пользователя"
                value={newUserId}
                onChange={(e) => setNewUserId(e.target.value)}
                className="border px-2 py-1 rounded"
              />
              <button onClick={handleAddUser} className="bg-green-600 text-white px-3 rounded">
                Добавить
              </button>
            </div>
          )}
        </>
      )}

      {(isPrivileged || isOwner) ? (
        <button
          onClick={handleDelete}
          className="bg-red-500 text-white px-4 py-2 rounded"
        >
          Удалить группу
        </button>
      ) : (
        <button
          onClick={handleLeave}
          className="bg-gray-800 text-white px-4 py-2 rounded"
        >
          Выйти из группы
        </button>
      )}
    </div>
  );
};

export default GroupDetail;
