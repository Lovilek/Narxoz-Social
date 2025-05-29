import React, { useEffect, useState } from "react";
import apiClient from "../utils/apiClient";
import {
  Container,
  TextField,
  Typography,
  Button,
  Avatar,
  CircularProgress,
  Box,
} from "@mui/material";
import { useNavigate } from "react-router-dom";

const EditProfile = () => {
  const [form, setForm] = useState({
    full_name: "",
    nickname: "",
    email: "",
  });
  const [avatarFile, setAvatarFile] = useState(null);
  const [preview, setPreview] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    apiClient
      .get("/users/profile/")
      .then((res) => {
        setForm({
          full_name: res.data.full_name || "",
          nickname: res.data.nickname || "",
          email: res.data.email || "",
        });
        if (res.data.avatar_path) {
          setPreview(`http://127.0.0.1:8000${res.data.avatar_path}`);
        }
      })
      .finally(() => setLoading(false));
  }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleAvatarChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setAvatarFile(file);
      setPreview(URL.createObjectURL(file));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const formData = new FormData();
      formData.append("nickname", form.nickname);
      if (avatarFile) {
        formData.append("avatar_path", avatarFile);
      }

      await apiClient.put("/users/update/", formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });

      alert("Профиль обновлён");
      window.location.reload(); // гарантированная перезагрузка для обновления аватара
    } catch (err) {
      alert("Ошибка при обновлении профиля");
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Container sx={{ textAlign: "center", mt: 8 }}>
        <CircularProgress />
      </Container>
    );
  }

  return (
    <Container maxWidth="sm" sx={{ mt: 4 }}>
      <Typography variant="h5" gutterBottom>
        Редактировать профиль
      </Typography>

      <form onSubmit={handleSubmit} encType="multipart/form-data">
        <Box display="flex" flexDirection="column" gap={2}>
          <TextField
            label="ФИО"
            name="full_name"
            value={form.full_name}
            InputProps={{ readOnly: true }}
            fullWidth
          />
          <TextField
            label="Email"
            name="email"
            value={form.email}
            InputProps={{ readOnly: true }}
            fullWidth
          />
          <TextField
            label="Никнейм"
            name="nickname"
            value={form.nickname}
            onChange={handleChange}
            fullWidth
          />

          <Box display="flex" alignItems="center" gap={2}>
            <Avatar src={preview} sx={{ width: 64, height: 64 }} />
            <Button variant="outlined" component="label">
              Загрузить аватар
              <input type="file" hidden accept="image/*" onChange={handleAvatarChange} />
            </Button>
          </Box>

          <Button
            type="submit"
            variant="contained"
            color="primary"
            disabled={loading}
          >
            Сохранить
          </Button>
        </Box>
      </form>
    </Container>
  );
};

export default EditProfile;
