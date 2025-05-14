import React, { useState } from "react";
import apiClient from "../../utils/apiClient";

const AddUserModal = ({ chatId, currentMembers, onClose }) => {
  const [userId, setUserId] = useState("");
  const [error, setError] = useState("");

  const handleAdd = async () => {
    const numericId = parseInt(userId);

    if (!userId || isNaN(numericId)) {
      setError("Введите корректный ID пользователя");
      return;
    }

    if (currentMembers.includes(numericId)) {
      setError("Этот пользователь уже в группе");
      return;
    }

    try {
      await apiClient.post(`/chats/${chatId}/add/`, { user_id: numericId });
      onClose();
    } catch (err) {
      setError("Ошибка при добавлении пользователя");
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-lg p-6 w-full max-w-sm">
        <h2 className="text-xl font-semibold mb-4">Добавить участника</h2>
        <input
          type="number"
          value={userId}
          onChange={(e) => {
            setUserId(e.target.value);
            setError("");
          }}
          placeholder="Введите ID пользователя"
          className="w-full border px-3 py-2 rounded mb-3"
        />
        {error && <div className="text-red-500 text-sm mb-3">{error}</div>}
        <div className="flex justify-end gap-2">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
          >
            Отмена
          </button>
          <button
            onClick={handleAdd}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            Добавить
          </button>
        </div>
      </div>
    </div>
  );
};

export default AddUserModal;
