import React, { useState } from "react";
import { TextField, Button, Typography, Box, Alert, CircularProgress } from "@mui/material";
import { useDispatch, useSelector } from "react-redux";
import { requestPasswordReset, clearAuthMessages } from "../store/authSlice";
import { useNavigate } from "react-router-dom";

function ForgotPassword() {
  const [login, setLogin] = useState("");
  const [email, setEmail] = useState("");
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error, successMessage } = useSelector((s) => s.auth);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const res = await dispatch(requestPasswordReset({ login, email }));
    if (res.meta.requestStatus === "fulfilled") {
      setTimeout(() => {
        dispatch(clearAuthMessages());
        navigate("/login", { state: { resetRequested: true } });
      }, 1500);
    }
  };

  return (
    <Box className="login-box">
      <Typography variant="h5" sx={{ mb: 2 }}>Password reset</Typography>
      <form onSubmit={handleSubmit}>
        <TextField label="S/F/G - Login" fullWidth sx={{ mb: 2 }}
                   value={login} onChange={(e) => setLogin(e.target.value)} />
        <TextField label="Email" fullWidth sx={{ mb: 2 }}
                   value={email} onChange={(e) => setEmail(e.target.value)} />
        <Button variant="contained" type="submit" fullWidth disabled={loading}>
          {loading ? <CircularProgress size={24} /> : "Send reset link"}
        </Button>
      </form>
      {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
      {successMessage && <Alert severity="success" sx={{ mt: 2 }}>{successMessage}</Alert>}
    </Box>
  );
}

export default ForgotPassword;
