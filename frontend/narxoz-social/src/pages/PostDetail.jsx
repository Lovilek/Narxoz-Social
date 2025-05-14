import React, { useEffect, useState, useCallback } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useDispatch, useSelector, shallowEqual } from "react-redux";
import {
  fetchComments,
  createComment,
  deleteComment,
  fetchLikes,
} from "../store/postSlice";
import apiClient from "../utils/apiClient";
import LikeButton from "../components/LikeButton";
import ImageLightbox from "../components/ImageLightbox";
import { parseISO, format, isToday, isYesterday } from "date-fns";
import { ru } from "date-fns/locale";

const smart = (iso) => {
  if (!iso) return "";
  const d = parseISO(iso);
  if (isToday(d)) return `Сегодня, ${format(d, "HH:mm", { locale: ru })}`;
  if (isYesterday(d)) return `Вчера, ${format(d, "HH:mm", { locale: ru })}`;
  return format(d, "d MMM yyyy, HH:mm", { locale: ru });
};

export default function PostDetail() {
  const { id } = useParams(); // postId из URL
  const nav = useNavigate();
  const dispatch = useDispatch();
  const nick = useSelector((s) => s.auth.user?.nickname);

  const [post, setPost] = useState(null);
  const [imgs, setImgs] = useState([]);

  const cmts = useSelector(
    (s) => s.posts.commentsByPost[id] || [],
    shallowEqual
  );

  const [draft, setDraft] = useState("");

  // Lightbox
  const [idx, setIdx] = useState(null);
  const close = () => setIdx(null);
  const prev = useCallback(
    () => setIdx((i) => (i === 0 ? imgs.length - 1 : i - 1)),
    [imgs]
  );
  const next = useCallback(
    () => setIdx((i) => (i === imgs.length - 1 ? 0 : i + 1)),
    [imgs]
  );

  // Загрузка данных
  useEffect(() => {
    apiClient
      .get(`/posts/${id}/`)
      .then(({ data }) => {
        setPost(data);
        if (Array.isArray(data.images) && data.images.length) {
          setImgs(data.images);
        } else {
          apiClient
            .get(`/posts/image-list/${id}/`)
            .then(({ data }) => setImgs(Array.isArray(data) ? data : []))
            .catch(() => setImgs([]));
        }
      })
      .catch(() => setPost(null));

    dispatch(fetchComments(id)); // ✅ загружаем комменты каждый раз
    if (nick) {
      dispatch(fetchLikes({ postId: id, currentNickname: nick }));
    }
  }, [id, dispatch, nick]);

  if (!post) return <p style={{ padding: 20 }}>Загрузка поста…</p>;

  return (
    <div style={{ maxWidth: 800, margin: "0 auto", padding: 24 }}>
      <button onClick={() => nav(-1)} style={{ marginBottom: 16 }}>
        ← Назад
      </button>

      {/* Заголовок и текст */}
      <h2 style={{ color: "#D50032" }}>{post.content}</h2>
      <p>
        <strong>{post.author}</strong> · {smart(post.created_at)}
      </p>

      {/* Картинки */}
      {imgs.length > 0 && (
        <div style={{ display: "flex", flexWrap: "wrap", gap: 12, marginTop: 12 }}>
          {imgs.map((im, i) => (
            <img
              key={im.id}
              src={im.image_path}
              alt=""
              style={{ width: 230, borderRadius: 8, cursor: "pointer" }}
              onClick={() => setIdx(i)}
            />
          ))}
        </div>
      )}

      {/* Лайки */}
      <div style={{ marginTop: 20, fontSize: 24 }}>
        <LikeButton postId={+id} />
      </div>

      {/* Комментарии */}
      <h3 style={{ marginTop: 32 }}>Комментарии ({cmts.length})</h3>
      {cmts.length > 0 ? (
        cmts.map((c) => (
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
        ))
      ) : (
        <p style={{ color: "#777" }}>Комментариев пока нет.</p>
      )}

      {/* Форма комментария */}
      {nick && (
        <form
          onSubmit={(e) => {
            e.preventDefault();
            if (!draft.trim()) return;
            dispatch(createComment({ postId: id, content: draft })).then(() =>
              setDraft("")
            );
          }}
          style={{ display: "flex", gap: 8, marginTop: 12 }}
        >
          <input
            value={draft}
            onChange={(e) => setDraft(e.target.value)}
            placeholder="Написать комментарий…"
            style={{
              flex: 1,
              padding: 8,
              borderRadius: 6,
              border: "1px solid #ccc",
            }}
          />
          <button
            type="submit"
            style={{
              padding: "0 16px",
              background: "#D50032",
              color: "white",
              border: "none",
              borderRadius: 6,
              fontWeight: "bold",
            }}
          >
            Отпр.
          </button>
        </form>
      )}

      <ImageLightbox
        sources={imgs.map((i) => i.image_path)}
        index={idx}
        onClose={close}
        onPrev={prev}
        onNext={next}
      />
    </div>
  );
}
