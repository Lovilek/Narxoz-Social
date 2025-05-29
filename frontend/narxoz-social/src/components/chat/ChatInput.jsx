import React, { useState } from "react";

const ChatInput = ({ onSend }) => {
  const [text, setText] = useState("");

  const handleSend = () => {
    if (text.trim()) {
      onSend(text);
      setText("");
    }
  };

  return (
    <div className="p-2 border-t flex gap-2">
      <input
        className="flex-1 border rounded px-2 py-1"
        value={text}
        onChange={(e) => setText(e.target.value)}
        placeholder="Введите сообщение..."
      />
      <button className="bg-blue-500 text-white px-4 py-1 rounded" onClick={handleSend}>
        Отправить
      </button>
    </div>
  );
};

export default ChatInput;
