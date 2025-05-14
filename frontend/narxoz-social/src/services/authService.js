import apiClient from "../utils/apiClient";

export const loginUser = async (login, password) => {
  const res = await apiClient.post("/users/login/", { login, password });
  return res.data; // { access, refresh, user }
};

export const logoutUser = async (refreshToken) => {
  const res = await apiClient.post("/users/logout/", {
    refresh: refreshToken,
  });
  return res.data;
};

export const getProfile = async () => {
  const res = await apiClient.get("/users/profile/");
  return res.data;
};

export const fetchOrganizations = async () => {
  const response = await apiClient.get("/users/organizations/");
  return response.data;
};
