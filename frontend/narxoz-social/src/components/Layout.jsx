import React from "react";
import Sidebar from "./Sidebar";
import Topbar from "./Topbar";
import UserPanel from "./UserPanel";
import { Outlet } from "react-router-dom";
import "../assets/css/Layout.css";

const Layout = () => {
  return (
    <div className="layout">
      <Sidebar />
      <div className="main-content">
        <Topbar />
        <div className="page-content">
          <Outlet />
        </div>
      </div>
      <UserPanel />
    </div>
  );
};

export default Layout;
