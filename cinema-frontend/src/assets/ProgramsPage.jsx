import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { fetchPrograms, createProgram, changeProgramState } from "../api/programApi.js";

function getErrMsg(err, fallback="Error") {
  if (!err) return fallback;
  if (err.body && String(err.body).trim()) return String(err.body);
  return err.message || fallback;
}

const NEXT_PROGRAM_STATE = {
  CREATED: "SUBMISSION",
  SUBMISSION: "ASSIGNMENT",
  ASSIGNMENT: "REVIEW",
  REVIEW: "SCHEDULING",
  SCHEDULING: "FINAL_PUBLICATION",
  FINAL_PUBLICATION: "DECISION",
  DECISION: "ANNOUNCED",
};

export default function ProgramsPage({ auth }) {
  const [programs, setPrograms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [err, setErr] = useState(null);

  // create program
  const [name, setName] = useState("");
  const [desc, setDesc] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [creating, setCreating] = useState(false);
  const [createErr, setCreateErr] = useState(null);

  async function load() {
    setErr(null);
    setLoading(true);
    try {
      setPrograms(await fetchPrograms());
    } catch (e) {
      setErr(getErrMsg(e, "Failed to load programs"));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { load(); }, []);

  async function handleCreate(e) {
    e.preventDefault();
    setCreateErr(null);

    if (!auth?.username) {
      setCreateErr("Πρέπει να κάνεις login για create program.");
      return;
    }
    if (!name || !desc || !startDate || !endDate) {
      setCreateErr("Συμπλήρωσε όλα τα πεδία.");
      return;
    }

    setCreating(true);
    try {
      await createProgram({ name, description: desc, startDate, endDate });
      setName(""); setDesc(""); setStartDate(""); setEndDate("");
      await load();
    } catch (e2) {
      setCreateErr(getErrMsg(e2, "Create failed"));
    } finally {
      setCreating(false);
    }
  }

  async function handleAdvance(p) {
    const next = NEXT_PROGRAM_STATE[p.state];
    if (!next) return;

    try {
      await changeProgramState(p.id, next);
      await load();
    } catch (e) {
      alert(getErrMsg(e, "State change failed"));
    }
  }

  return (
    <div>
      <h2>Programs</h2>

      {loading && <div>Loading...</div>}
      {err && <div style={{ color: "crimson" }}>{err}</div>}

      <table border="1" cellPadding="6" style={{ borderCollapse: "collapse", width: "100%" }}>
        <thead>
          <tr>
            <th>ID</th><th>Name</th><th>Dates</th><th>State</th><th>Open</th><th>Advance</th>
          </tr>
        </thead>
        <tbody>
          {programs?.map((p) => (
            <tr key={p.id}>
              <td>{p.id}</td>
              <td>{p.name}</td>
              <td>{p.startDate} → {p.endDate}</td>
              <td>{p.state}</td>
              <td><Link to={`/programs/${p.id}`}>Open</Link></td>
              <td>
                {auth?.username && NEXT_PROGRAM_STATE[p.state] ? (
                  <button onClick={() => handleAdvance(p)}>
                    → {NEXT_PROGRAM_STATE[p.state]}
                  </button>
                ) : (
                  "-"
                )}
              </td>
            </tr>
          ))}
          {!programs?.length && (
            <tr><td colSpan={6}>No programs.</td></tr>
          )}
        </tbody>
      </table>

      <hr style={{ margin: "20px 0" }} />

      <h3>Create Program (SUBMITTER/PROGRAMMER)</h3>
      <form onSubmit={handleCreate} style={{ display: "grid", gap: 8, maxWidth: 520 }}>
        <input value={name} onChange={(e) => setName(e.target.value)} placeholder="name" />
        <textarea value={desc} onChange={(e) => setDesc(e.target.value)} placeholder="description" />
        <label>
          Start date:
          <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
        </label>
        <label>
          End date:
          <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
        </label>
        <button disabled={creating} type="submit">{creating ? "Creating..." : "Create"}</button>
        {createErr && <div style={{ color: "crimson" }}>{createErr}</div>}
      </form>
    </div>
  );
}
