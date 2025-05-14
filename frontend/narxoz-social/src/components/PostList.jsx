// src/pages/PostList.jsx
import React, { useEffect, useState, useCallback } from "react";
import { useDispatch, useSelector, shallowEqual } from "react-redux";
import {
  fetchPosts, append,
  fetchComments, createComment, deleteComment,
  fetchLikes,
} from "../store/postSlice";
import { parseISO, format, isToday, isYesterday } from "date-fns";
import { ru } from "date-fns/locale";
import { Link, useNavigate } from "react-router-dom";
import LikeButton from "../components/LikeButton";
import ImageLightbox from "../components/ImageLightbox";

import commentIcon from "../assets/icons/comments.svg";
import moreIcon from "../assets/icons/more.svg";
import "../assets/css/PostList.css";

const dateFmt = (iso) => {
  if (!iso) return "";
  const d = parseISO(iso);
  if (isToday(d)) return `Сегодня, ${format(d, "HH:mm", { locale: ru })}`;
  if (isYesterday(d)) return `Вчера, ${format(d, "HH:mm", { locale: ru })}`;
  return format(d, "d MMM yyyy, HH:mm", { locale: ru });
};

export default function PostList() {
  const dispatch = useDispatch();
  const nav = useNavigate();
  const nick = useSelector((s) => s.auth.user?.nickname);

  const raw = useSelector((s) => s.posts.items, shallowEqual);
  const commentsByPost = useSelector((s) => s.posts.commentsByPost, shallowEqual);
  const loading = useSelector((s) => s.posts.loading);
  const error = useSelector((s) => s.posts.error);
  const nextUrl = useSelector((s) => s.posts.nextUrl);

  const posts = Array.isArray(raw) ? raw : raw?.results || [];

  const [open, setOpen] = useState({});
  const [draft, setDraft] = useState({});
  const [lbSrc, setLbSrc] = useState([]);
  const [lbIdx, setLbIdx] = useState(null);

  useEffect(() => {
    dispatch(fetchPosts());
  }, [dispatch]);

  useEffect(() => {
    posts.forEach((p) => {
      if (nick) dispatch(fetchLikes({ postId: p.id, currentNickname: nick }));
      dispatch(fetchComments(p.id));
    });
  }, [posts, nick, dispatch]);

  useEffect(() => {
    if (!nextUrl) return;
    const onScroll = () => {
      if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 200) {
        window.removeEventListener("scroll", onScroll);
        fetch(nextUrl.replace("/api", ""))
          .then((res) => res.json())
          .then((data) => dispatch(append(data.results)));
      }
    };
    window.addEventListener("scroll", onScroll);
    return () => window.removeEventListener("scroll", onScroll);
  }, [nextUrl, dispatch]);

  const closeLb = () => setLbIdx(null);
  const prevLb = useCallback(() => setLbIdx(i => i === 0 ? lbSrc.length - 1 : i - 1), [lbSrc]);
  const nextLb = useCallback(() => setLbIdx(i => i === lbSrc.length - 1 ? 0 : i + 1), [lbSrc]);

  if (loading && !posts.length) return <p>Загрузка…</p>;
  if (error) return <p style={{ color: "red" }}>{error}</p>;

  return (
    <>
      <div className="post-list">
        {posts.map((p) => {
          const cmts = commentsByPost[p.id] || [];
          const show = open[p.id];

          return (
            <div key={p.id}>
              {/* 🔷 Верхний блок вне карточки */}
              <div className="post-top">
                <div className="post-header">
                  <img
                    src={p.author_avatar_path || "/avatar.jpg"}
                    alt="avatar"
                    className="post-avatar"
                  />
                  <div className="post-user-info">
                    <Link
                      to={`/profile/${p.author_id}`}
                      className="post-author"
                      onClick={(e) => e.stopPropagation()}
                    >
                      {p.author}
                    </Link>
                    <div className="post-time">{dateFmt(p.created_at)}</div>
                  </div>
                </div>
                <img
                  src={moreIcon}
                  className="post-more-btn"
                  alt="more"
                  onClick={() => nav(`/posts/${p.id}`)}
                />
              </div>

              {/* 🔻 Контент карточки */}
              <div className="post-card">
                <div className="post-content">
                  <h4>{p.content}</h4>
                </div>

                {p.images?.length > 0 && (
                  <div
                    className={`post-images ${p.images.length === 1 ? "single" : "multi"}`}
                  >
                    {p.images.map((im, i) => (
                      <img
                        key={im.id}
                        src={im.image_path}
                        alt="post-img"
                        className="post-img"
                        onClick={(e) => {
                          e.stopPropagation();
                          setLbSrc(p.images.map((x) => x.image_path));
                          setLbIdx(i);
                        }}
                      />
                    ))}
                  </div>
                )}

                <div className="like-comment-row">
                  <LikeButton postId={p.id} />
                  <div
                    className="comment-icon-block"
                    onClick={(e) => {
                      e.stopPropagation();
                      setOpen((o) => ({ ...o, [p.id]: !o[p.id] }));
                    }}
                  >
                    <img src={commentIcon} alt="comments" className="comment-icon" />
                    <span className="comment-count">{cmts.length}</span>
                  </div>
                </div>

                {show && (
                  <div className="post-comments">
                    {cmts.length > 0 ? (
                      cmts.map((c) => (
                        <div key={c.id} className="comment-line">
                          <span>
                            <strong>{c.author_nickname}</strong>: {c.content}
                          </span>
                          {c.author_nickname === nick && (
                            <span
                              className="comment-delete"
                              onClick={(e) => {
                                e.stopPropagation();
                                dispatch(deleteComment({ postId: p.id, commentId: c.id }));
                              }}
                            >
                              🗑
                            </span>
                          )}
                        </div>
                      ))
                    ) : (
                      <p className="no-comments">Комментариев нет</p>
                    )}

                    {nick && (
                      <form
                        onSubmit={(e) => {
                          e.preventDefault();
                          e.stopPropagation();
                          if (!draft[p.id]?.trim()) return;
                          dispatch(createComment({ postId: p.id, content: draft[p.id] }))
                            .then(() => setDraft((d) => ({ ...d, [p.id]: "" })));
                        }}
                        className="comment-form"
                      >
                        <input
                          value={draft[p.id] || ""}
                          onClick={(e) => e.stopPropagation()}
                          onChange={(e) => setDraft((d) => ({ ...d, [p.id]: e.target.value }))}
                          placeholder="Комментарий…"
                        />
                        <button type="submit" onClick={(e) => e.stopPropagation()}>
                          Отпр.
                        </button>
                      </form>
                    )}
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      <ImageLightbox
        sources={lbSrc}
        index={lbIdx}
        onClose={closeLb}
        onPrev={prevLb}
        onNext={nextLb}
      />
    </>
  );
}
