import React, { useState } from "react";
import { TextField, Button, Typography, Box, Alert, CircularProgress } from "@mui/material";
import { useParams, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { resetPasswordConfirm, clearAuthMessages } from "../store/authSlice";

function ResetPasswordConfirm() {
  const { uid, token } = useParams();
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { loading, error, successMessage } = useSelector((s) => s.auth);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const res = await dispatch(
      resetPasswordConfirm({ uid, token, new_password: newPassword, confirm_new_password: confirmPassword })
    );
    if (res.meta.requestStatus === "fulfilled") {
      setTimeout(() => {
        dispatch(clearAuthMessages());
        navigate("/login", { state: { passwordReset: true } });
      }, 1500);
    }
  };

  return (
    <Box className="login-box">
      <Typography variant="h5" sx={{ mb: 2 }}>Set new password</Typography>
      <form onSubmit={handleSubmit}>
        <TextField label="New password" type="password" fullWidth sx={{ mb: 2 }}
                   value={newPassword} onChange={(e) => setNewPassword(e.target.value)} />
        <TextField label="Repeat password" type="password" fullWidth sx={{ mb: 2 }}
                   value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} />
        <Button variant="contained" type="submit" fullWidth disabled={loading}>
          {loading ? <CircularProgress size={24} /> : "Save"}
        </Button>
      </form>
      {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
      {successMessage && <Alert severity="success" sx={{ mt: 2 }}>{successMessage}</Alert>}
    </Box>
  );
}

export default ResetPasswordConfirm;
