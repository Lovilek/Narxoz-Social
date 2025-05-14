import React, { useEffect, useState } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";

/* --- layout & общие страницы --- */
import Layout from "./components/Layout";
import Home from "./pages/Home";

/* --- post --- */
import PostEdit from "./pages/PostEdit";
import CreatePost from "./pages/CreatePost";
import PostDetail from "./pages/PostDetail";

/* --- auth --- */
import Login from "./pages/Login";
import Logout from "./pages/Logout";
import RegisterUser from "./pages/RegisterUser";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPasswordConfirm from "./pages/ResetPasswordConfirm";

/* --- profile / social --- */
import EditProfile from "./pages/EditProfile";
import AnotherUserProfile from "./pages/AnotherUserProfile";
import Friends from "./pages/Friends";
import Organizations from "./pages/Organizations";

/* --- chats --- */
import Messages from "./pages/Messages";
import DirectChat from "./pages/DirectChat";
import Groups from "./pages/Groups";
import GroupDetail from "./pages/GroupDetail";

/* --- redux --- */
import { fetchProfile } from "./store/authSlice";

function App() {
  const dispatch = useDispatch();
  const isAuthenticated = useSelector((state) => state.auth.isAuthenticated);

  const [ready, setReady] = useState(false);

  /* проверяем токен + защищаем от F5-сброса */
  useEffect(() => {
    const token = localStorage.getItem("token");

    if (token) {
      dispatch(fetchProfile()).finally(() => setReady(true));
    } else {
      setReady(true);
    }

    /* запрет контекст-меню на сообщениях */
    const preventDefaultContextMenu = (e) => {
      const isMessage = e.target.closest(".message-bubble");
      if (isMessage) e.preventDefault();
    };
    document.addEventListener("contextmenu", preventDefaultContextMenu);
    return () =>
      document.removeEventListener("contextmenu", preventDefaultContextMenu);
  }, [dispatch]);

  if (!ready) return null; // можно заменить на спиннер

  return (
    <BrowserRouter>
      <Routes>
        {/* ---------- Публичные маршруты ---------- */}
        <Route path="/login" element={<Login />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route
          path="/reset-password/:uid/:token"
          element={<ResetPasswordConfirm />}
        />
        <Route path="admin/register" element={<RegisterUser />} />
        <Route path="/logout" element={<Logout />} />

        {/* ---------- Защищённая зона ---------- */}
        <Route
          path="/"
          element={
            isAuthenticated ? <Layout /> : <Navigate to="/login" replace />
          }
        >
          {/* главная + посты */}
          <Route path="home" element={<Home />} />
          <Route path="posts/create" element={<CreatePost />} />
          <Route path="posts/:id" element={<PostDetail />} />
          <Route path="posts/:id/edit" element={<PostEdit />} />

          {/* профиль */}
          <Route path="profile/edit" element={<EditProfile />} />
          <Route path="profile/:id" element={<AnotherUserProfile />} />

          {/* друзья / организации */}
          <Route path="friends" element={<Friends />} />
          <Route path="/organizations" element={<Organizations />} />

          {/* группы */}
          <Route path="groups" element={<Groups />} />
          <Route path="groups/:chatId" element={<GroupDetail />} />

          {/* чаты */}
          <Route path="messages" element={<Messages />} />
          <Route path="messages/:chatId" element={<DirectChat />} />
        </Route>

        {/* ---------- Fallback ---------- */}
        <Route path="*" element={<Navigate to="/home" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
