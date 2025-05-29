import React, { useState } from "react";
import {
  TextField,
  Button,
  MenuItem,
  Typography,
  Alert,
  Box,
  CircularProgress,
} from "@mui/material";
import apiClient from "../utils/apiClient";
import "../assets/css/Login.css";
import logo from "../assets/images/logo.png";

const RegisterUser = () => {
  const [login, setLogin] = useState("");
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [nickname, setNickname] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState("");
  const [avatar, setAvatar] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);

    if (!login.match(/^[SFG]\d{6,}$/)) {
      setError("Логин должен начинаться с S, F или G и содержать минимум 6 цифр");
      setLoading(false);
      return;
    }

    const formData = new FormData();
    formData.append("login", login.toUpperCase());
    formData.append("full_name", fullName);
    formData.append("email", email);
    formData.append("nickname", nickname);
    formData.append("password", password);
    formData.append("role", role);

    if (avatar) {
      formData.append("avatar_path", avatar);
    }

    try {
      await apiClient.post("/users/register/", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });
      setSuccess("Пользователь успешно зарегистрирован!");
      setLogin("");
      setFullName("");
      setEmail("");
      setNickname("");
      setPassword("");
      setRole("");
      setAvatar(null);
    } catch (err) {
      console.error("Ошибка:", err);
      if (err.response?.data) {
        const errorData = err.response.data;
        const firstError = Object.values(errorData)[0];
        setError(Array.isArray(firstError) ? firstError[0] : firstError);
      } else {
        setError("Ошибка регистрации. Проверьте данные.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-fullscreen">
      <Box className="login-box">
        <img src={logo} alt="Narxoz Social" className="login-logo" />
        <Typography variant="h5" className="login-title">
          Добавить пользователя
        </Typography>

        <form onSubmit={handleSubmit} encType="multipart/form-data">
          <TextField
            label="Логин (S/F/G######)"
            variant="outlined"
            fullWidth
            autoFocus
            value={login}
            onChange={(e) => setLogin(e.target.value)}
            className="login-input"
            inputProps={{ style: { textTransform: "uppercase" } }}
          />
          <TextField
            label="ФИО"
            variant="outlined"
            fullWidth
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            className="login-input"
          />
          <TextField
            label="Email"
            variant="outlined"
            fullWidth
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="login-input"
          />
          <TextField
            label="Nickname"
            variant="outlined"
            fullWidth
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            className="login-input"
          />
          <TextField
            label="Пароль"
            type="password"
            variant="outlined"
            fullWidth
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="login-input"
          />
          <TextField
            label="Роль"
            select
            fullWidth
            value={role}
            onChange={(e) => setRole(e.target.value)}
            className="login-input"
          >
            <MenuItem value="student">Студент (S######)</MenuItem>
            <MenuItem value="teacher">Преподаватель (F######)</MenuItem>
            <MenuItem value="moderator">Модератор (F######)</MenuItem>
            <MenuItem value="admin">Администратор (F######)</MenuItem>
            <MenuItem value="organization">Организация (G######)</MenuItem>
          </TextField>

          <Button
            variant="contained"
            component="label"
            className="login-button"
          >
            Загрузить аватар
            <input
              type="file"
              accept="image/*"
              hidden
              onChange={(e) => setAvatar(e.target.files[0])}
            />
          </Button>

          <Button
            type="submit"
            fullWidth
            variant="contained"
            className="login-button"
            disabled={loading}
            style={{ marginTop: "15px" }}
          >
            {loading ? <CircularProgress size={24} /> : "Зарегистрировать"}
          </Button>
        </form>

        {error && (
          <Alert severity="error" style={{ marginTop: "10px" }}>
            {error}
          </Alert>
        )}

        {success && (
          <Alert severity="success" style={{ marginTop: "10px" }}>
            {success}
          </Alert>
        )}
      </Box>
    </div>
  );
};

export default RegisterUser;
