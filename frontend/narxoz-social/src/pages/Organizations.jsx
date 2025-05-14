import React, { useEffect, useState } from "react";
import { fetchOrganizations } from "../services/userService";
import "../assets/css/Organizations.css"; // подключим стили

const Organizations = () => {
  const [organizations, setOrganizations] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadOrgs = async () => {
      try {
        const data = await fetchOrganizations();
        setOrganizations(data);
      } catch (error) {
        console.error("Ошибка при загрузке организаций:", error);
      } finally {
        setLoading(false);
      }
    };

    loadOrgs();
  }, []);

  if (loading) return <p>Загрузка организаций...</p>;

  return (
    <div className="organizations-container">
      <h2 className="organizations-title">Организации</h2>
      <div className="organizations-list">
        {organizations.map((org) => (
          <div key={org.id} className="organization-card">
            <img
              src={
                org.avatar_path
                  ? `http://127.0.0.1:8000${org.avatar_path}`
                  : "/default-avatar.png"
              }
              alt={org.nickname}
              className="organization-avatar"
            />
            <div className="organization-info">
              <h3>{org.full_name}</h3>
              <p>@{org.nickname}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default Organizations;
