// src/components/MyPostCard.jsx
import React, { useState } from "react";
import { parseISO, format } from "date-fns";
import { ru } from "date-fns/locale";
import { useSelector } from "react-redux";
import LikeButton from "./LikeButton";
import CommentSection from "./CommentSection";

import commentIcon from "../assets/icons/comments.svg";
import moreIcon from "../assets/icons/more.svg";

const MyPostCard = ({ post, images = [], likeCount = 0, onClick }) => {
  const [showComments, setShowComments] = useState(false);
  const comments = useSelector((s) => s.posts.commentsByPost[post.id] || []);
  const commentCount = comments.length;

  return (
    <div style={styles.wrapper}>
      {/* 🔹 Верхняя секция — аватар, имя, дата, и кнопка ⋮ */}
      <div style={styles.top}>
        <div style={styles.userBlock}>
          <img
            src={post.author_avatar_path || "/avatar.jpg"}
            alt="avatar"
            style={styles.avatar}
          />
          <div>
            <div style={styles.name}>{post.author}</div>
            <div style={styles.time}>
              {format(parseISO(post.created_at), "d MMM yyyy HH:mm", { locale: ru })}
            </div>
          </div>
        </div>
        <button onClick={onClick} style={styles.editBtn}>
          <img src={moreIcon} alt="edit" />
        </button>
      </div>

      {/* 🔻 Основной блок — контент, картинка, лайки и комментарии */}
      <div style={styles.card}>
        {/* Текст поста */}
        <div style={styles.content}>
          <strong style={styles.title}>{post.content}</strong>
        </div>

        {/* Первая картинка — 100% ширины */}
        {images.length > 0 && (
          <div style={styles.mainImage}>
            <img
              src={images[0].image_path}
              alt="main-img"
              style={styles.fullImage}
            />
          </div>
        )}

        {/* Лайк и комментарии */}
        <div style={styles.metaRow}>
          <LikeButton postId={post.id} />

          <div
            style={styles.metaIcon}
            onClick={() => setShowComments((prev) => !prev)}
          >
            <img src={commentIcon} alt="comments" style={{ width: 20 }} />
            <span style={styles.metaText}>{commentCount}</span>
          </div>
        </div>

        {/* Комментарии (раскрывающиеся) */}
        {showComments && (
          <div style={{ marginTop: 10 }}>
            <CommentSection postId={post.id} />
          </div>
        )}
      </div>
    </div>
  );
};

const styles = {
  wrapper: {
    marginBottom: 20,
  },
  top: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
    marginBottom: 6,
    padding: "0 4px",
  },
  userBlock: {
    display: "flex",
    gap: 12,
    alignItems: "center",
  },
  avatar: {
    width: 44,
    height: 44,
    borderRadius: "50%",
    objectFit: "cover",
  },
  name: {
    fontWeight: 600,
    fontSize: 16,
    display: "flex",
  },
  time: {
    fontSize: 13,
    color: "#B2B2B2",
    marginTop: 2,
  },
  editBtn: {
    background: "none",
    border: "none",
    cursor: "pointer",
    padding: 6,
  },
  card: {
    background: "white",
    padding: 16,
    borderRadius: 12,
    color: "black",
    boxShadow: "0 2px 8px rgba(0,0,0,0.05)",
  },
  content: {
    marginBottom: 12,
  },
  title: {
    fontSize: 15,
  },
  mainImage: {
    width: "100%",
    overflow: "hidden",
    borderRadius: 8,
  },
  fullImage: {
    width: "100%",
    height: "auto",
    objectFit: "cover",
    borderRadius: 8,
  },
  metaRow: {
    display: "flex",
    alignItems: "center",
    gap: 20,
    marginTop: 12,
  },
  metaIcon: {
    display: "flex",
    alignItems: "center",
    gap: 6,
    cursor: "pointer",
  },
  metaText: {
    fontSize: 14,
    color: "#333",
  },
};

export default MyPostCard;
