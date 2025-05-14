import React, { useEffect, useState } from "react";
import { useDispatch, useSelector, shallowEqual } from "react-redux";
import {
  fetchComments,
  createComment,
  deleteComment,
} from "../store/postSlice";

const CommentSection = ({ postId }) => {
  const dispatch = useDispatch();
  const currentNick = useSelector((s) => s.auth.user?.nickname);
  const comments = useSelector(
    (s) => s.posts.commentsByPost[postId] || [],
    shallowEqual
  );

  const [draft, setDraft] = useState("");

  useEffect(() => {
    dispatch(fetchComments(postId));
  }, [dispatch, postId]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!draft.trim()) return;
    dispatch(createComment({ postId, content: draft })).then(() =>
      setDraft("")
    );
  };

  return (
    <div style={{ marginTop: 10 }}>
      <h4>Комментарии ({comments.length})</h4>

      {comments.length > 0 ? (
        comments.map((c) => (
          <div
            key={c.id}
            style={{
              background: "#ffffff22",
              padding: 8,
              borderRadius: 8,
              marginBottom: 6,
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
            }}
          >
            <span>
              <strong>{c.author_nickname}</strong>: {c.content}
            </span>

            {c.author_nickname === currentNick && (
              <span
                style={{
                  cursor: "pointer",
                  marginLeft: 8,
                  fontWeight: "bold",
                }}
                title="Удалить"
                onClick={() =>
                  dispatch(deleteComment({ postId, commentId: c.id }))
                }
              >
                🗑
              </span>
            )}
          </div>
        ))
      ) : (
        <p style={{ fontSize: 14, color: "#eee" }}>Комментариев пока нет</p>
      )}

      {currentNick && (
        <form
          onSubmit={handleSubmit}
          style={{ display: "flex", gap: 8, marginTop: 8 }}
        >
          <input
            style={{
              flex: 1,
              padding: 8,
              borderRadius: 6,
              border: "1px solid #ccc",
            }}
            placeholder="Написать комментарий…"
            value={draft}
            onChange={(e) => setDraft(e.target.value)}
          />
          <button
            type="submit"
            style={{
              padding: "8px 16px",
              background: "#fff",
              color: "#D50032",
              border: "none",
              borderRadius: 6,
              fontWeight: 600,
              cursor: "pointer",
            }}
          >
            Отпр.
          </button>
        </form>
      )}
    </div>
  );
};

export default CommentSection;
