import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { setBasicAuth } from "../api/http.js";
import { searchPrograms } from "../api/programApi.js";

function getErrMsg(err) {
  if (!err) return "Login failed";
  if (err.body && String(err.body).trim()) return String(err.body);
  return err.message || "Login failed";
}

export default function LoginPage({ onLogin }) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState(null);
  const [loading, setLoading] = useState(false);
  const nav = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();
    setErr(null);

    if (!username || !password) {
      setErr("Βάλε username/password");
      return;
    }

    setBasicAuth(username, password);

    setLoading(true);
    try {

      await searchPrograms({});
      onLogin(username);
      nav("/programs");
    } catch (e2) {
      setBasicAuth(null, null);
      setErr(getErrMsg(e2));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ maxWidth: 520 }}>
      <h2>Login</h2>
      <form onSubmit={handleSubmit} style={{ display: "grid", gap: 8 }}>
        <input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="username" />
        <input value={password} onChange={(e) => setPassword(e.target.value)} placeholder="password" type="password" />
        <button disabled={loading} type="submit">{loading ? "Logging in..." : "Login"}</button>
      </form>
      {err && <div style={{ marginTop: 12, color: "crimson" }}>{err}</div>}
      <p style={{ opacity: 0.8 }}>
        Demo users: visitor/user1/prog1/staff1/submitter (password = ίδιο)
      </p>
    </div>
  );
}
