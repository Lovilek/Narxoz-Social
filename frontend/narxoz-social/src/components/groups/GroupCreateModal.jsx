import React, { useEffect, useState } from "react";
import apiClient from "../../utils/apiClient";
import "../../assets/css/GroupCreateModal.css";

const GroupCreateModal = ({ onClose }) => {
  const [groupName, setGroupName] = useState("");
  const [avatarFile, setAvatarFile] = useState(null);
  const [friends, setFriends] = useState([]);
  const [selectedIds, setSelectedIds] = useState([]);

  useEffect(() => {
    const fetchFriends = async () => {
      try {
        const res = await apiClient.get("/friends/list/");
        setFriends(res.data);
      } catch (err) {
        console.error("Ошибка загрузки друзей:", err);
      }
    };
    fetchFriends();
  }, []);

  const toggleCheckbox = (id) => {
    setSelectedIds((prev) =>
      prev.includes(id) ? prev.filter((i) => i !== id) : [...prev, id]
    );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!groupName || selectedIds.length === 0) {
      alert("Укажите название и выберите хотя бы одного участника.");
      return;
    }

    const formData = new FormData();
    formData.append("name", groupName);
    if (avatarFile) {
      formData.append("avatar", avatarFile);
    }
    selectedIds.forEach((id) => formData.append("members", id));

    try {
      const res = await apiClient.post("/chats/group/create/", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });
      console.log("Группа создана:", res.data);
      onClose(); // закрыть модалку после создания
    } catch (err) {
      console.error("Ошибка создания группы:", err);
      alert("Ошибка при создании группы. Проверьте данные.");
    }
  };

  return (
    <div className="modal-overlay">
      <div className="modal-box">
        <h2 className="modal-title">Создать новую группу</h2>
        <form onSubmit={handleSubmit}>
          <input
            type="text"
            placeholder="Название группы"
            value={groupName}
            onChange={(e) => setGroupName(e.target.value)}
            required
          />

          <input
            type="file"
            accept="image/*"
            onChange={(e) => setAvatarFile(e.target.files[0])}
          />

          <div className="friends-list">
            {friends.map((f) => (
              <label key={f.id} className="friend-item">
                <input
                  type="checkbox"
                  checked={selectedIds.includes(f.id)}
                  onChange={() => toggleCheckbox(f.id)}
                />
                <img
                  src={f.avatar_path || "/avatar.jpg"}
                  alt="avatar"
                  className="friend-avatar"
                />
                <span>{f.full_name} (@{f.nickname})</span>
              </label>
            ))}
          </div>

          <button type="submit" className="submit-btn">Создать</button>
          <button type="button" className="cancel-btn" onClick={onClose}>
            Отмена
          </button>
        </form>
      </div>
    </div>
  );
};

export default GroupCreateModal;
