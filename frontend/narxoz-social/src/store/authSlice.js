import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import { loginUser, logoutUser, getProfile } from "../services/authService";
import apiClient from "../utils/apiClient";

/* ---------- THUNK'И ---------- */

// --- LOGIN ---
export const login = createAsyncThunk(
  "auth/login",
  async ({ login, password }, thunkAPI) => {
    try {
      const data = await loginUser(login, password);          // { access, refresh, user }
      return data;
    } catch (err) {
      return thunkAPI.rejectWithValue(err.message);
    }
  }
);

// --- LOGOUT ---
export const logout = createAsyncThunk("auth/logout", async (_, thunkAPI) => {
  const state = thunkAPI.getState();
  const { refresh, token: access } = state.auth;

  try {
    await apiClient.post(
      "/users/logout/",
      { refresh },
      { headers: { Authorization: `Bearer ${access}` } }
    );
  } catch {/* ошибку игнорируем */ }

  localStorage.removeItem("token");
  localStorage.removeItem("refresh");
  return true;
});

// --- FETCH PROFILE ---
export const fetchProfile = createAsyncThunk(
  "auth/fetchProfile",
  async (_, thunkAPI) => {
    try {
      const data = await getProfile();
      return data;
    } catch (err) {
      return thunkAPI.rejectWithValue(err.message);
    }
  }
);

// --- REGISTER USER ---
export const registerUser = createAsyncThunk(
  "auth/registerUser",
  async (formData, thunkAPI) => {
    try {
      const { data } = await apiClient.post("/users/register/", formData, {
        headers: { "Content-Type": "multipart/form-data" },
      });
      return data;
    } catch (err) {
      return thunkAPI.rejectWithValue(
        err.response?.data?.message || "Ошибка регистрации"
      );
    }
  }
);

// --- REQUEST PASSWORD RESET ---
export const requestPasswordReset = createAsyncThunk(
  "auth/requestPasswordReset",
  async ({ login, email }, thunkAPI) => {
    try {
      const { data } = await apiClient.post("/users/password-reset/", {
        login,
        email,
      });
      return data.message;  // «Ссылка отправлена…»
    } catch (err) {
      return thunkAPI.rejectWithValue(
        err.response?.data?.error || "Ошибка сброса пароля"
      );
    }
  }
);

// --- CONFIRM PASSWORD RESET ---
export const resetPasswordConfirm = createAsyncThunk(
  "auth/resetPasswordConfirm",
  async ({ uid, token, new_password, confirm_new_password }, thunkAPI) => {
    try {
      const { data } = await apiClient.post("/users/password-reset/confirm/", {
        uid,
        token,
        new_password,
        confirm_new_password,
      });
      return data.message;  // «Пароль успешно сброшен»
    } catch (err) {
      return thunkAPI.rejectWithValue(
        err.response?.data?.error || "Ошибка подтверждения пароля"
      );
    }
  }
);

/* ---------- SLICE ---------- */

const authSlice = createSlice({
  name: "auth",
  initialState: {
    isAuthenticated: false,
    token: localStorage.getItem("token") || null,
    refresh: localStorage.getItem("refresh") || null,
    user: null,
    loading: false,
    error: null,
    successMessage: null,
  },
  reducers: {
    clearAuthMessages(state) {
      state.error = null;
      state.successMessage = null;
    },
  },
  extraReducers: (builder) => {
    builder
      /* LOGIN */
      .addCase(login.pending, (s) => { s.loading = true; s.error = null; })
      .addCase(login.fulfilled, (s, a) => {
        const { access, refresh } = a.payload;
        s.loading = false;
        s.isAuthenticated = true;
        s.token = access;
        s.refresh = refresh;
        localStorage.setItem("token", access);
        localStorage.setItem("refresh", refresh);
      })
      .addCase(login.rejected, (s, a) => { s.loading = false; s.error = a.payload; })

      /* FETCH PROFILE */
      .addCase(fetchProfile.fulfilled, (s, a) => {
        s.user = a.payload;
        s.isAuthenticated = true;
      })

      /* LOGOUT */
      .addCase(logout.fulfilled, (s) => {
        s.isAuthenticated = false;
        s.token = null;
        s.refresh = null;
        s.user = null;
      })

      /* REGISTER */
      .addCase(registerUser.pending, (s) => { s.loading = true; s.error = null; })
      .addCase(registerUser.fulfilled, (s) => { s.loading = false; })
      .addCase(registerUser.rejected, (s, a) => { s.loading = false; s.error = a.payload; })

      /* PASSWORD RESET REQUEST */
      .addCase(requestPasswordReset.pending, (s) => { s.loading = true; s.error = null; })
      .addCase(requestPasswordReset.fulfilled, (s, a) => {
        s.loading = false;
        s.successMessage = a.payload;
      })
      .addCase(requestPasswordReset.rejected, (s, a) => {
        s.loading = false;
        s.error = a.payload;
      })

      /* PASSWORD RESET CONFIRM */
      .addCase(resetPasswordConfirm.pending, (s) => { s.loading = true; s.error = null; })
      .addCase(resetPasswordConfirm.fulfilled, (s, a) => {
        s.loading = false;
        s.successMessage = a.payload;
      })
      .addCase(resetPasswordConfirm.rejected, (s, a) => {
        s.loading = false;
        s.error = a.payload;
      });
  },
});

export const { clearAuthMessages } = authSlice.actions;
export default authSlice.reducer;
