import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import { fetchOrganizations } from "../services/userService";

export const loadOrganizations = createAsyncThunk(
  "organizations/load",
  async (_, thunkAPI) => {
    try {
      const data = await fetchOrganizations();
      return data;
    } catch (err) {
      return thunkAPI.rejectWithValue("Ошибка загрузки организаций");
    }
  }
);

const organizationSlice = createSlice({
  name: "organizations",
  initialState: {
    list: [],
    loading: false,
    error: null,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(loadOrganizations.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(loadOrganizations.fulfilled, (state, action) => {
        state.loading = false;
        state.list = action.payload;
      })
      .addCase(loadOrganizations.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  },
});

export default organizationSlice.reducer;
