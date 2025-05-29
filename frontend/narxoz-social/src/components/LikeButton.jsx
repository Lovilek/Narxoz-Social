import React, { useEffect, useState } from "react";
import { useDispatch, useSelector, shallowEqual } from "react-redux";
import { fetchLikes, toggleLike } from "../store/postSlice";

import activeLike from "../assets/icons/active-like.svg";
import unactiveLike from "../assets/icons/unactive_like.svg";

const EMPTY = { liked: false, count: 0 };

export default function LikeButton({ postId }) {
  const dispatch = useDispatch();
  const [initialFetched, setInitialFetched] = useState(false);
  const nickname = useSelector((s) => s.auth.user?.nickname);
  const like = useSelector(
    (s) => s.posts.likesByPost[postId] || EMPTY,
    shallowEqual
  );
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (!nickname || initialFetched) return;
    dispatch(fetchLikes({ postId, currentNickname: nickname }));
    setInitialFetched(true);
  }, [nickname, postId, dispatch, initialFetched]);

  const handleClick = async (e) => {
    e.stopPropagation();
    if (!nickname || busy) return;
    setBusy(true);
    await dispatch(toggleLike(postId));
    setBusy(false);
  };

  return (
    <div
      onClick={handleClick}
      style={{
        display: "flex",
        alignItems: "center",
        gap: 6,
        cursor: nickname && !busy ? "pointer" : "not-allowed",
        opacity: busy ? 0.5 : 1,
        userSelect: "none",
      }}
    >
      <img
        src={like.liked ? activeLike : unactiveLike}
        alt="like"
        style={{ width: 20, height: 20 }}
      />
      <span style={{ color: "black", fontSize: 14 }}>{like.count}</span>
    </div>
  );
}
