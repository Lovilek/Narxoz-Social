import React from "react";
import { useNavigate } from "react-router-dom";
import "../assets/css/SearchDropdown.css";   /* или свой путь к css */

const Section = ({ title, items, renderItem }) =>
  items && items.length ? (
    <>
      <div className="sd-title">{title}</div>
      {items.slice(0, 5).map(renderItem)}
    </>
  ) : null;

function SearchDropdown({ query, loading, data, onClose }) {
  const nav = useNavigate();
  const go  = (path) => { onClose(); nav(path); };

  return (
    <div className="search-dd">
      {loading && <div className="sd-loading">Поиск…</div>}

      {!loading && data && (
        <>
          <Section
            title="Посты"
            items={data.posts}
            renderItem={(p) => (
              <div key={p.id} className="sd-item" onClick={() => go(`/posts/${p.id}`)}>
                {p.content.slice(0, 60)}…
              </div>
            )}
          />

          <Section
            title="Друзья"
            items={data.friends}
            renderItem={(u) => (
              <div key={u.id} className="sd-item" onClick={() => go(`/profile/${u.id}`)}>
                👤 {u.full_name}
              </div>
            )}
          />

          <Section
            title="Чаты"
            items={data.chats}
            renderItem={(c) => (
              <div
                key={c.id}
                className="sd-item"
                onClick={() => go(c.type === "group" ? `/groups/${c.id}` : `/messages/${c.id}`)}
              >
                💬 {c.type === "group" ? c.name : c.title}
              </div>
            )}
          />

          <Section
            title="Организации"
            items={data.organizations}
            renderItem={(o) => (
              <div key={o.id} className="sd-item" onClick={() => go(`/profile/${o.id}`)}>
                🏢 {o.full_name}
              </div>
            )}
          />

          <Section
            title="Пользователи"
            items={data.users}
            renderItem={(u) => (
              <div key={u.id} className="sd-item" onClick={() => go(`/profile/${u.id}`)}>
                🙍 {u.full_name}
              </div>
            )}
          />

          {!(
            data.posts?.length ||
            data.friends?.length ||
            data.chats?.length ||
            data.organizations?.length ||
            data.users?.length
          ) && <div className="sd-empty">Ничего не найдено</div>}
        </>
      )}
    </div>
  );
}

export default SearchDropdown;
