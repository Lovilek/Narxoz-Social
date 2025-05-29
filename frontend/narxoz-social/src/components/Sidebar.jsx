import React, { useState } from "react";
import { NavLink } from "react-router-dom";
import { useSelector } from "react-redux";

import logo from "../assets/images/logo.png";

// Иконки: default и hover
import homeDefault from "../assets/icons/home.svg";
import homeHover from "../assets/icons/home-hover.svg";

import friendsDefault from "../assets/icons/friends.svg";
import friendsHover from "../assets/icons/friends-hover.svg";

import postsDefault from "../assets/icons/posts.svg";
import postsHover from "../assets/icons/posts-hover.svg";

import messagesDefault from "../assets/icons/messages.svg";
import messagesHover from "../assets/icons/messages-hover.svg";

import groupsDefault from "../assets/icons/groups.svg";
import groupsHover from "../assets/icons/groups-hover.svg";

import settingsDefault from "../assets/icons/settings.svg";
import settingsHover from "../assets/icons/settings-hover.svg";

import logoutIcon from "../assets/icons/logout.svg";

const navItems = [
  { to: "/home", default: homeDefault, hover: homeHover, alt: "Home" },
  { to: "/friends", default: friendsDefault, hover: friendsHover, alt: "Friends" },
  { to: "/posts/create", default: postsDefault, hover: postsHover, alt: "Add Post" },
  { to: "/messages", default: messagesDefault, hover: messagesHover, alt: "Messages" },
  { to: "/groups", default: groupsDefault, hover: groupsHover, alt: "Groups" },
  { to: "/settings", default: settingsDefault, hover: settingsHover, alt: "Settings" },
];

const Sidebar = () => {
  const currentUser = useSelector((state) => state.auth.user);
  const [hoveredIndex, setHoveredIndex] = useState(null);

  return (
    <div className="sidebar">
      <img src={logo} alt="Logo" className="sidebar-logo" />

      {navItems.map((item, index) => (
        <NavLink
          key={item.to}
          to={item.to}
          end
          className={({ isActive }) =>
            `icon-wrapper ${isActive ? "active" : ""}`
          }
          onMouseEnter={() => setHoveredIndex(index)}
          onMouseLeave={() => setHoveredIndex(null)}
        >
          <img
            src={hoveredIndex === index ? item.hover : item.default}
            alt={item.alt}
            className="icon-img"
          />
        </NavLink>
      ))}

      <NavLink to="/logout" className="icon-wrapper logout-btn">
        <img src={logoutIcon} alt="Logout" className="logout" />
      </NavLink>
    </div>
  );
};

export default Sidebar;
