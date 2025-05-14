import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import { Provider } from "react-redux";
import { store, persistor } from "./store/store";
import { PersistGate } from "redux-persist/integration/react";
import "./index.css";

const SafePersistGate = ({ children }) => {
  return (
    <PersistGate
      loading={<p style={{ padding: 20 }}>⏳ Загрузка состояния...</p>}
      persistor={persistor}
      onBeforeLift={() => {
        console.log("✅ Состояние восстановлено из localStorage");
      }}
    >
      {children}
    </PersistGate>
  );
};

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(
  <Provider store={store}>
    <SafePersistGate>
      <App />
    </SafePersistGate>
  </Provider>
);
