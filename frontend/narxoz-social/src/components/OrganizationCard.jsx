// src/components/OrganizationCard.jsx
import React from "react";
import { useNavigate } from "react-router-dom";

const OrganizationCard = ({ org }) => {
  const navigate = useNavigate();

  return (
    <div
      className="organization-card"
      onClick={() => navigate(`/profile/${org.id}`)}
    >
      <img
        src={org.avatar_path || "/avatar.jpg"}
        alt={org.nickname}
        className="organization-avatar"
      />
      <div className="organization-info">
        <h4>{org.full_name}</h4>
        <p>@{org.nickname}</p>
        <span className="organization-meta">561K подписчиков</span>
      </div>
    </div>
  );
};

export default OrganizationCard;
