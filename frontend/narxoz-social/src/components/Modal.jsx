import React from "react";

const Modal = ({ children, onClose }) => (
  <div style={styles.overlay} onClick={onClose}>
    <div style={styles.modal} onClick={(e) => e.stopPropagation()}>
      <button style={styles.close} onClick={onClose}>×</button>
      {children}
    </div>
  </div>
);

const styles = {
  overlay: {
    position: "fixed",
    top: 0, left: 0, right: 0, bottom: 0,
    backgroundColor: "rgba(0, 0, 0, 0.6)",
    display: "flex",
    alignItems: "center",
    justifyContent: "center",
    zIndex: 1000,
  },
  modal: {
    background: "#fff",
    color: "#000",
    padding: 20,
    borderRadius: 8,
    maxWidth: 400,
    width: "100%",
  },
  close: {
    position: "absolute",
    top: 12,
    right: 16,
    background: "none",
    border: "none",
    fontSize: 20,
    cursor: "pointer",
  },
};

export default Modal;
