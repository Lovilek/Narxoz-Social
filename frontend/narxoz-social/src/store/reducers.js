// reducers.js или rootReducer.js
import { combineReducers } from "@reduxjs/toolkit";
import authReducer from "./authSlice";
import postsReducer from "./postSlice"; // название файла может быть postsSlice, но импорт корректный

const rootReducer = combineReducers({
  auth: authReducer,
  posts: postsReducer,
  // сюда можно добавить другие редьюсеры, например friends, messages и т.д.
});

export default rootReducer;
