import "./App.css";
import { BrowserRouter, Routes, Route, Navigate, Link, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";

import LoginPage from "./LoginPage.jsx";
import ProgramsPage from "./ProgramsPage.jsx";
import ProgramPage from "./ProgramPage.jsx";
import ScreeningPage from "./ScreeningPage.jsx";

import { clearAuth } from "../api/http.js";

function Layout({ auth, onLogout, children }) {
  return (
    <div className="page">
      <header style={{ display: "flex", gap: 12, alignItems: "center", marginBottom: 16 }}>
        <Link to="/programs">Programs</Link>
        <div style={{ flex: 1 }} />
        {auth?.username ? (
          <>
            <span>
              Logged in as <b>{auth.username}</b>
            </span>
            <button onClick={onLogout}>Logout</button>
          </>
        ) : (
          <Link to="/login">Login</Link>
        )}
      </header>

      {children}
    </div>
  );
}

function AppRoutes({ auth, setAuth }) {
  const nav = useNavigate();

  const logout = () => {
    clearAuth();
    setAuth({ username: null });
    localStorage.removeItem("cinemaAuth");
    nav("/login");
  };

  return (
    <Layout auth={auth} onLogout={logout}>
      <Routes>
        <Route path="/" element={<Navigate to="/programs" replace />} />
        <Route
          path="/login"
          element={
            <LoginPage
              onLogin={(username) => {
                setAuth({ username });
                nav("/programs");
              }}
            />
          }
        />
        <Route path="/programs" element={<ProgramsPage auth={auth} />} />
        <Route path="/programs/:programId" element={<ProgramPage auth={auth} />} />
        <Route
          path="/programs/:programId/screenings/:screeningId"
          element={<ScreeningPage auth={auth} />}
        />
        <Route path="*" element={<Navigate to="/programs" replace />} />
      </Routes>
    </Layout>
  );
}

export default function App() {
  const [auth, setAuth] = useState(() => {
    const saved = localStorage.getItem("cinemaAuth");
    return saved ? JSON.parse(saved) : { username: null };
  });

  useEffect(() => {
    localStorage.setItem("cinemaAuth", JSON.stringify(auth));
  }, [auth]);

  return (
    <BrowserRouter>
      <AppRoutes auth={auth} setAuth={setAuth} />
    </BrowserRouter>
  );
}
