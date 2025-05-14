import React from "react";
import { useSelector, useDispatch } from "react-redux";
import { logout } from "../store/authSlice";
import { Button, Container, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";
import PostList from "../components/PostList";

function Home() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const user = useSelector((state) => state.auth.user);

  const handleLogout = () => {
    dispatch(logout());
    navigate("/login");
  };

  return (
    <PostList />
  );
}

export default Home;
