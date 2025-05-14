import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import apiClient from "../utils/apiClient";

/*──────────────────────── THUNKS ────────────────────────*/

/* 1. Получить все посты */
export const fetchPosts = createAsyncThunk("posts/fetchAll", async () => {
  const { data } = await apiClient.get("/posts/");
  return Array.isArray(data) ? { results: data, next: null } : data;
});

/* 2. Создание нового поста */
export const createPost = createAsyncThunk(
  "posts/create",
  async ({ content }) => {
    const { data } = await apiClient.post("/posts/create/", { content });
    return data;
  }
);

/* 3. Комментарии */
export const fetchComments = createAsyncThunk(
  "posts/fetchComments",
  async (postId) => {
    const { data } = await apiClient.get(`/posts/${postId}/comments/`);
    const comments = Array.isArray(data) ? data : data.results || [];
    return { postId, comments };
  }
);

export const createComment = createAsyncThunk(
  "posts/createComment",
  async ({ postId, content }) => {
    const { data } = await apiClient.post(`/posts/${postId}/comments/`, { content });
    return { postId, comment: data };
  }
);

export const deleteComment = createAsyncThunk(
  "posts/deleteComment",
  async ({ postId, commentId }, thunkAPI) => {
    try {
      await apiClient.delete(`/posts/${postId}/comments/${commentId}/`);
      return { postId, commentId };
    } catch (err) {
      console.error("Ошибка при удалении комментария:", err.response?.data || err.message);
      return thunkAPI.rejectWithValue({ postId, commentId, error: err.response?.data });
    }
  }
);

/* 4. Лайки */
export const fetchLikes = createAsyncThunk(
  "posts/fetchLikes",
  async ({ postId, currentNickname }) => {
    const { data } = await apiClient.get(`/posts/${postId}/likes/`);
    const likes = Array.isArray(data) ? data : data.results || [];
    return {
      postId,
      count: likes.length,
      liked: likes.some((l) => l.author_nickname === currentNickname),
    };
  }
);

export const toggleLike = createAsyncThunk(
  "posts/toggleLike",
  async (postId, { getState }) => {
    await apiClient.post(`/posts/${postId}/like/`);
    const nickname = getState().auth.user?.nickname;
    const { data } = await apiClient.get(`/posts/${postId}/likes/`);
    const likes = Array.isArray(data) ? data : data.results || [];
    return {
      postId,
      count: likes.length,
      liked: likes.some((l) => l.author_nickname === nickname),
    };
  }
);

/* 5. Загрузка изображений */
export const uploadImageToPost = createAsyncThunk(
  "posts/uploadImage",
  async ({ postId, file }) => {
    const form = new FormData();
    form.append("image_path", file);
    const { data } = await apiClient.post(
      `/posts/image-upload/${postId}/`,
      form,
      { headers: { "Content-Type": "multipart/form-data" } }
    );
    return { postId, image: data };
  }
);

/*──────────────────────── SLICE ─────────────────────────*/

const slice = createSlice({
  name: "posts",
  initialState: {
    items: [],
    nextUrl: null,
    loading: false,
    error: null,
    commentsByPost: {},     // { [postId]: Comment[] }
    likesByPost: {},        // { [postId]: {count, liked} }
    imagesByPost: {},       // { [postId]: PostImage[] }
  },
  reducers: {
    append: (state, action) => {
      state.items = [...state.items, ...action.payload];
    },
  },
  extraReducers: (builder) => {
    builder
      /* --- ЛЕНТА --- */
      .addCase(fetchPosts.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchPosts.fulfilled, (state, action) => {
        state.loading = false;
        state.items = action.payload.results;
        state.nextUrl = action.payload.next;
      })
      .addCase(fetchPosts.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message;
      })

      /* --- СОЗДАНИЕ ПОСТА --- */
      .addCase(createPost.fulfilled, (state, action) => {
        state.items.unshift(action.payload);
      })

      /* --- КОММЕНТАРИИ --- */
      .addCase(fetchComments.fulfilled, (state, action) => {
        state.commentsByPost[action.payload.postId] = action.payload.comments;
      })
      .addCase(createComment.fulfilled, (state, action) => {
        const { postId, comment } = action.payload;
        const list = Array.isArray(state.commentsByPost[postId])
          ? state.commentsByPost[postId]
          : [];
        state.commentsByPost[postId] = [...list, comment];
      })
      .addCase(deleteComment.fulfilled, (state, action) => {
        const { postId, commentId } = action.payload;
        const list = Array.isArray(state.commentsByPost[postId])
          ? state.commentsByPost[postId]
          : [];
        state.commentsByPost[postId] = list.filter((c) => c.id !== commentId);
      })
      .addCase(deleteComment.rejected, (state, action) => {
        console.warn("Комментарий не был удалён на сервере:", action.payload);
      })

      /* --- ЛАЙКИ --- */
      .addCase(fetchLikes.fulfilled, (state, action) => {
        state.likesByPost[action.payload.postId] = {
          count: action.payload.count,
          liked: action.payload.liked,
        };
      })
      .addCase(toggleLike.fulfilled, (state, action) => {
        state.likesByPost[action.payload.postId] = {
          count: action.payload.count,
          liked: action.payload.liked,
        };
      })

      /* --- ИЗОБРАЖЕНИЯ --- */
      .addCase(uploadImageToPost.fulfilled, (state, action) => {
        const { postId, image } = action.payload;
        const list = state.imagesByPost[postId] || [];
        state.imagesByPost[postId] = [...list, image];
      });
  },
});

export const { append } = slice.actions;
export default slice.reducer;
