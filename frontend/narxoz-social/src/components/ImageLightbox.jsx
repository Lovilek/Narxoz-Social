import React, { useEffect } from "react";

const overlay = {
  position: "fixed",
  inset: 0,
  background: "rgba(0,0,0,0.85)",
  display: "flex",
  alignItems: "center",
  justifyContent: "center",
  zIndex: 1000,
};

const imgStyle = {
  maxWidth: "90vw",
  maxHeight: "90vh",
  objectFit: "contain",
  borderRadius: 8,
  boxShadow: "0 8px 16px rgba(0,0,0,0.5)",
};

/**
 * Lightbox
 * @param {string[]} sources — массив URL-ов изображений
 * @param {number}   index   — текущее изображение
 * @param {function} onClose — закрыть
 * @param {function} onPrev  — предыдущее
 * @param {function} onNext  — следующее
 */
export default function ImageLightbox({ sources, index, onClose, onPrev, onNext }) {
  if (index === null) return null;

  /* Esc + стрелки */
  useEffect(() => {
    const keyHandler = (e) => {
      if (e.key === "Escape")    onClose();
      if (e.key === "ArrowLeft") onPrev();
      if (e.key === "ArrowRight")onNext();
    };
    window.addEventListener("keydown", keyHandler);
    return () => window.removeEventListener("keydown", keyHandler);
  }, [onClose, onPrev, onNext]);

  return (
    <div style={overlay} onClick={onClose}>
      {/* картинка: stopPropagation, чтобы не закрыть, если клик по самой картинке */}
      <img
        src={sources[index]}
        alt=""
        style={imgStyle}
        onClick={(e) => e.stopPropagation()}
      />

      {/* стрелки */}
      {sources.length > 1 && (
        <>
          <button onClick={(e)=>{e.stopPropagation();onPrev();}} style={navBtn("left")}>‹</button>
          <button onClick={(e)=>{e.stopPropagation();onNext();}} style={navBtn("right")}>›</button>
        </>
      )}
    </div>
  );
}

const navBtn = (side) => ({
  position: "fixed",
  top: "50%",
  [side]: 20,
  transform: "translateY(-50%)",
  background: "rgba(255,255,255,0.15)",
  border: "none",
  color: "#fff",
  fontSize: 48,
  lineHeight: 1,
  width: 60,
  height: 60,
  borderRadius: "50%",
  cursor: "pointer",
});
