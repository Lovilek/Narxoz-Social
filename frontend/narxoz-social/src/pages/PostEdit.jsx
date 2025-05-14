import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useSelector, useDispatch } from "react-redux";
import {
  uploadImageToPost,
  fetchLikes,
  fetchComments,
  createComment,
  deleteComment,
} from "../store/postSlice";
import apiClient from "../utils/apiClient";
import LikeButton from "../components/LikeButton";

export default function PostEdit() {
  const { id } = useParams();
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const nick = useSelector((s) => s.auth.user?.nickname);
  const cmts = useSelector((s) => s.posts.commentsByPost[id] || []);

  const [content, setContent] = useState("");
  const [newImages, setNewImages] = useState([]);
  const [existingImages, setExistingImages] = useState([]);
  const [authorNick, setAuthorNick] = useState("");
  const [draft, setDraft] = useState("");

  useEffect(() => {
    (async () => {
      const { data } = await apiClient.get(`/posts/${id}/`);
      setContent(data.content);
      setAuthorNick(data.author);

      const { data: imgs } = await apiClient.get(`/posts/image-list/${id}/`);
      setExistingImages(imgs);
    })();

    dispatch(fetchLikes({ postId: id, currentNickname: nick }));
    dispatch(fetchComments(id));
  }, [id, dispatch, nick]);

  if (nick && nick !== authorNick) {
    return (
      <div style={{ padding: 20 }}>
        <h2>Нет доступа</h2>
        <button onClick={() => navigate(-1)}>← Назад</button>
      </div>
    );
  }

  const handleSave = async (e) => {
    e.preventDefault();
    await apiClient.put(`/posts/${id}/`, { content });

    for (const f of newImages) {
      await dispatch(uploadImageToPost({ postId: id, file: f }));
    }

    const { data: updatedImgs } = await apiClient.get(`/posts/image-list/${id}/`);
    setExistingImages(updatedImgs);
    setNewImages([]);
  };

  const deleteImage = async (imgId) => {
    if (!window.confirm("Удалить изображение?")) return;
    await apiClient.delete(`/posts/${id}/image/${imgId}/`);
    setExistingImages((imgs) => imgs.filter((img) => img.id !== imgId));
  };

  const handleComment = (e) => {
    e.preventDefault();
    if (!draft.trim()) return;
    dispatch(createComment({ postId: id, content: draft })).then(() => setDraft(""));
  };

  const handleDeletePost = async () => {
    if (!window.confirm("Вы точно хотите удалить этот пост?")) return;
    try {
      await apiClient.delete(`/posts/${id}/`);
      navigate("/home");
    } catch (err) {
      console.error("Ошибка при удалении поста:", err);
    }
  };

  return (
    <div style={{ padding: 20, maxWidth: 720, margin: "0 auto" }}>
      <h2 style={{ color: "#D50032" }}>Редактировать пост</h2>

      <form onSubmit={handleSave}>
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          rows={5}
          style={{ width: "100%", padding: 12, fontSize: 16, borderRadius: 6 }}
        />

        <input
          type="file"
          multiple
          accept="image/*"
          onChange={(e) => setNewImages(Array.from(e.target.files))}
          style={{ marginTop: 12 }}
        />

        {newImages.length > 0 && (
          <div style={{ display: "flex", gap: 8, marginTop: 10 }}>
            {newImages.map((f, i) => (
              <img
                key={i}
                src={URL.createObjectURL(f)}
                alt="preview"
                style={{
                  width: 80,
                  height: 80,
                  objectFit: "cover",
                  borderRadius: 8,
                  border: "1px solid #ccc",
                }}
              />
            ))}
          </div>
        )}

        <div style={{ display: "flex", gap: 10, marginTop: 20 }}>
          <button type="submit">💾 Сохранить</button>
          <button
            type="button"
            onClick={handleDeletePost}
            style={{ background: "#8B0000", color: "white", border: "none", padding: "8px 12px", borderRadius: 6 }}
          >
            🗑 Удалить пост
          </button>
        </div>
      </form>

      {existingImages.length > 0 && (
        <>
          <h4 style={{ marginTop: 30 }}>Старые изображения</h4>
          <div style={{ display: "flex", flexWrap: "wrap", gap: 12 }}>
            {existingImages.map((img) => (
              <div key={img.id} style={{ position: "relative" }}>
                <img
                  src={img.image_path}
                  alt=""
                  style={{
                    width: 80,
                    height: 80,
                    objectFit: "cover",
                    borderRadius: 8,
                    border: "1px solid #ccc",
                  }}
                />
                <button
                  onClick={() => deleteImage(img.id)}
                  style={{
                    position: "absolute",
                    top: -6,
                    right: -6,
                    width: 22,
                    height: 22,
                    borderRadius: "50%",
                    background: "#D50032",
                    color: "#fff",
                    border: "none",
                    fontWeight: "bold",
                    cursor: "pointer",
                  }}
                >
                  ×
                </button>
              </div>
            ))}
          </div>
        </>
      )}

      <div style={{ marginTop: 40 }}>
        <LikeButton postId={+id} />

        <h4 style={{ marginTop: 20 }}>Комментарии ({cmts.length})</h4>
        {cmts.map((c) => (
          <div
            key={c.id}
            style={{
              background: "#f6f6f6",
              padding: 10,
              borderRadius: 6,
              marginBottom: 6,
              display: "flex",
              justifyContent: "space-between",
            }}
          >
            <span>
              <strong>{c.author_nickname}</strong>: {c.content}
            </span>
            {c.author_nickname === nick && (
              <span
                style={{ cursor: "pointer" }}
                onClick={() =>
                  dispatch(deleteComment({ postId: id, commentId: c.id }))
                }
              >
                🗑
              </span>
            )}
          </div>
        ))}

        <form
          onSubmit={handleComment}
          style={{ display: "flex", gap: 8, marginTop: 12 }}
        >
          <input
            value={draft}
            onChange={(e) => setDraft(e.target.value)}
            placeholder="Комментарий…"
            style={{
              flex: 1,
              padding: 8,
              borderRadius: 6,
              border: "1px solid #ccc",
            }}
          />
          <button style={{ padding: "0 16px" }}>Отпр.</button>
        </form>
      </div>
    </div>
  );
}