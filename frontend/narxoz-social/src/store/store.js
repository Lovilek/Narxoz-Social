// store.js
import storage from "redux-persist/lib/storage";
import { persistStore, persistReducer, 
  FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER 
} from "redux-persist";
import { configureStore } from "@reduxjs/toolkit";
import rootReducer from "./reducers"; // комбинированный reducer, например combineReducers({ auth })

const persistConfig = {
  key: "root",
  storage,
  whitelist: ["auth"], // только auth сохраняем
  version: 1,
  migrate: (state) => {
    try {
      return Promise.resolve(state);
    } catch (e) {
      console.warn("🧨 Ошибка при восстановлении persist. Очистка...");
      storage.removeItem("persist:root");
      return Promise.resolve(undefined);
    }
  },
};

const persistedReducer = persistReducer(persistConfig, rootReducer);

export const store = configureStore({
  reducer: persistedReducer,
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: [
          FLUSH, REHYDRATE, PAUSE, PERSIST, PURGE, REGISTER,
        ],
      },
    }),
});

export const persistor = persistStore(store);
