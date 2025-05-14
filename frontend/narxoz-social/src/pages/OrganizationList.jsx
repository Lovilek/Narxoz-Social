import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { loadOrganizations } from "../store/organizationSlice";

const OrganizationList = () => {
  const dispatch = useDispatch();
  const { list, loading, error } = useSelector((state) => state.organizations);

  useEffect(() => {
    dispatch(loadOrganizations());
  }, [dispatch]);

  if (loading) return <p>Загрузка...</p>;
  if (error) return <p>{error}</p>;

  return (
    <div style={{ padding: "20px" }}>
      <h2>Организации</h2>
      <ul style={{ listStyle: "none", padding: 0 }}>
        {list.map((org) => (
          <li key={org.id} style={{ marginBottom: "10px", display: "flex", alignItems: "center" }}>
            <img
              src={`http://127.0.0.1:8000${org.avatar_path}`}
              alt={org.nickname}
              style={{ width: "50px", height: "50px", borderRadius: "50%", marginRight: "10px" }}
            />
            <div>
              <strong>{org.full_name}</strong>
              <div style={{ fontSize: "14px", color: "#555" }}>@{org.nickname}</div>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default OrganizationList;
