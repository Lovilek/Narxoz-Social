import React, { useState } from "react";
import {
  TextField,
  Button,
  Checkbox,
  FormControlLabel,
  Typography,
  Box,
  Alert,
  CircularProgress,
} from "@mui/material";
import { useDispatch, useSelector } from "react-redux";
import { login, logout } from "../store/authSlice";
import { useNavigate, Link } from "react-router-dom";   // ← добавили Link
import "../assets/css/Login.css";
import logo from "../assets/images/logo.png";

function Login() {
  const [loginValue, setLoginValue] = useState("");
  const [password, setPassword] = useState("");
  const [staySignedIn, setStaySignedIn] = useState(false);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error } = useSelector((state) => state.auth);

  const handleSubmit = async (e) => {
    e.preventDefault();
    await dispatch(logout());                             // очищаем предыдущую сессию
    const res = await dispatch(login({ login: loginValue, password }));
    if (res.meta.requestStatus === "fulfilled") navigate("/home");
  };

  return (
    <div className="login-fullscreen">
      <Box className="login-box">
        <img src={logo} alt="Narxoz Social" className="login-logo" />

        <Typography variant="h5" className="login-title">
          SIGN IN
        </Typography>

        <form onSubmit={handleSubmit}>
          <TextField
            label="S/F/G - Login"
            variant="outlined"
            fullWidth
            value={loginValue}
            onChange={(e) => setLoginValue(e.target.value)}
            className="login-input"
          />
          <TextField
            label="Password"
            type="password"
            variant="outlined"
            fullWidth
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            className="login-input"
          />

          <FormControlLabel
            control={
              <Checkbox
                checked={staySignedIn}
                onChange={() => setStaySignedIn(!staySignedIn)}
              />
            }
            label="stay signed in"
            className="login-checkbox"
          />

          <Button
            type="submit"
            variant="contained"
            fullWidth
            className="login-button"
            disabled={loading}
          >
            {loading ? <CircularProgress size={24} /> : "SIGN IN"}
          </Button>
        </form>

        {error && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {error}
          </Alert>
        )}

        {/* ссылка на форму сброса пароля */}
        <Typography variant="body2" className="forgot-password" sx={{ mt: 2 }}>
          <Link to="/forgot-password">Forgot password?</Link>
        </Typography>
      </Box>
    </div>
  );
}

export default Login;
