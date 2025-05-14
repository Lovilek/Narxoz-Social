import React, { useState } from "react";
import {
  Button,
  TextField,
  Typography,
  Container,
  Box,
  CircularProgress,
} from "@mui/material";
import { useDispatch } from "react-redux";
import { createPost } from "../store/postSlice";
import { useNavigate } from "react-router-dom";
import apiClient from "../utils/apiClient";

export default function CreatePost() {
  const [content, setContent] = useState("");
  const [images, setImages] = useState([]);
  const [busy, setBusy] = useState(false);

  const dispatch = useDispatch();
  const nav = useNavigate();

  // выбор файлов
  const onFile = (e) => {
    const files = Array.from(e.target.files);
    setImages((prev) => [...prev, ...files]);
  };

  // удалить отдельный файл
  const removeImage = (index) => {
    setImages((prev) => prev.filter((_, i) => i !== index));
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    if (!content.trim() && images.length === 0) return;
    setBusy(true);

    const res = await dispatch(createPost({ content }));
    if (res.meta.requestStatus !== "fulfilled") {
      setBusy(false);
      return;
    }

    const postId = res.payload.id;

    for (const file of images) {
      const form = new FormData();
      form.append("image_path", file);
      await apiClient.post(`/posts/image-upload/${postId}/`, form, {
        headers: { "Content-Type": "multipart/form-data" },
      });
    }

    nav("/home");
    setBusy(false);
  };

  return (
    <Container maxWidth="sm" sx={{ mt: 5 }}>
      <Typography variant="h5" gutterBottom>
        Создать пост
      </Typography>

      <Box component="form" onSubmit={onSubmit}>
        <TextField
          fullWidth
          multiline
          rows={4}
          label="Содержимое поста"
          value={content}
          onChange={(e) => setContent(e.target.value)}
          margin="normal"
        />

        <input
          type="file"
          multiple
          accept="image/*"
          onChange={onFile}
          style={{ margin: "16px 0" }}
        />

        {/* превью + кнопка удаления */}
        <Box display="flex" flexWrap="wrap" gap={1} mb={2}>
          {images.map((file, index) => (
            <Box key={index} position="relative">
              <img
                src={URL.createObjectURL(file)}
                alt={`preview-${index}`}
                style={{
                  width: 100,
                  height: 100,
                  objectFit: "cover",
                  borderRadius: 8,
                }}
              />
              <Button
                size="small"
                onClick={() => removeImage(index)}
                style={{
                  position: "absolute",
                  top: -8,
                  right: -8,
                  background: "#fff",
                  minWidth: "24px",
                  padding: "0 4px",
                  borderRadius: "50%",
                  color: "#D50032",
                  fontWeight: "bold",
                  cursor: "pointer",
                }}
              >
                ×
              </Button>
            </Box>
          ))}
        </Box>

        <Button
          type="submit"
          variant="contained"
          disabled={busy}
          fullWidth
        >
          {busy ? <CircularProgress size={24} /> : "Опубликовать"}
        </Button>
      </Box>
    </Container>
  );
}
