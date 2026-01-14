import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { fetchProgramById, changeProgramState } from "../api/programApi.js";
import { fetchScreenings, createScreening, submitScreening, withdrawScreening } from "../api/screeningApi.js";

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

export default function ProgramPage({ auth }) {
  const { programId } = useParams();
  const [program, setProgram] = useState(null);
  const [screenings, setScreenings] = useState([]);
  const [err, setErr] = useState(null);

  // create screening
  const [filmTitle, setFilmTitle] = useState("");
  const [aud, setAud] = useState("");
  const [start, setStart] = useState("");
  const [duration, setDuration] = useState("");
  const [end, setEnd] = useState("");
  const [createErr, setCreateErr] = useState(null);

  const canCreate = useMemo(() => {
    return !!auth?.username && program?.state === "SUBMISSION";
  }, [auth?.username, program?.state]);

  async function load() {
    setErr(null);
    try {
      setProgram(await fetchProgramById(programId));
      setScreenings(await fetchScreenings(programId));
    } catch (e) {
      setErr(getErrMsg(e, "Failed to load"));
    }
  }

  useEffect(() => { load(); }, [programId]);

  async function advance() {
    const next = NEXT_PROGRAM_STATE[program?.state];
    if (!next) return;
    try {
      const updated = await changeProgramState(programId, next);
      setProgram(updated);
    } catch (e) {
      alert(getErrMsg(e, "State change failed"));
    }
  }

  async function handleCreate(e) {
    e.preventDefault();
    setCreateErr(null);

    if (!canCreate) {
      setCreateErr("Create screening επιτρέπεται μόνο στο SUBMISSION.");
      return;
    }
    if (!filmTitle || !aud || !start) {
      setCreateErr("Βάλε Title, Auditorium, Start.");
      return;
    }
    const durationNum = duration ? Number(duration) : 0;
    if (!end && (!durationNum || durationNum <= 0)) {
      setCreateErr("Βάλε Duration ή End time.");
      return;
    }

    try {
      await createScreening(programId, {
        filmTitle,
        auditoriumName: aud,
        filmDurationMinutes: durationNum,
        startTime: `${start}:00`,
        endTime: end ? `${end}:00` : null,
      });
      setFilmTitle(""); setAud(""); setStart(""); setDuration(""); setEnd("");
      setScreenings(await fetchScreenings(programId));
    } catch (e2) {
      setCreateErr(getErrMsg(e2, "Create screening failed"));
    }
  }

  async function doSubmit(sid) {
    try {
      await submitScreening(programId, sid);
      setScreenings(await fetchScreenings(programId));
    } catch (e) {
      alert(getErrMsg(e, "Submit failed"));
    }
  }

  async function doWithdraw(sid) {
    try {
      await withdrawScreening(programId, sid);
      setScreenings(await fetchScreenings(programId));
    } catch (e) {
      alert(getErrMsg(e, "Withdraw failed"));
    }
  }


  const myAssigned = useMemo(() => {
    if (!auth?.username) return [];
    return (screenings || []).filter(s => s.handlerUsername && s.handlerUsername === auth.username);
  }, [screenings, auth?.username]);

  return (
    <div>
      <h2>Program #{programId}</h2>
      {err && <div style={{ color: "crimson" }}>{err}</div>}
      {!program ? <div>Loading...</div> : (
        <>
          <div>
            <div><b>{program.name}</b></div>
            <div>{program.startDate} → {program.endDate}</div>
            <div><b>State:</b> {program.state}</div>
            <div style={{ marginTop: 8 }}>
              {auth?.username && NEXT_PROGRAM_STATE[program.state] ? (
                <button onClick={advance}>Advance → {NEXT_PROGRAM_STATE[program.state]}</button>
              ) : null}
            </div>
          </div>

          <hr />

          <h3>Screenings</h3>

          {auth?.username && myAssigned.length > 0 && (
            <>
              <h4>My assigned (STAFF)</h4>
              <ul>
                {myAssigned.map(s => (
                  <li key={s.id}>
                    <Link to={`/programs/${programId}/screenings/${s.id}`}>
                      #{s.id} {s.filmTitle} ({s.state})
                    </Link>
                  </li>
                ))}
              </ul>
            </>
          )}

          <table border="1" cellPadding="6" style={{ borderCollapse: "collapse", width: "100%" }}>
            <thead>
              <tr>
                <th>ID</th><th>Title</th><th>Aud</th><th>Start</th><th>End</th><th>State</th><th>Open</th><th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {(screenings || []).map((s) => (
                <tr key={s.id}>
                  <td>{s.id}</td>
                  <td>{s.filmTitle}</td>
                  <td>{s.auditoriumName}</td>
                  <td>{s.startTime}</td>
                  <td>{s.endTime}</td>
                  <td>{s.state}</td>
                  <td><Link to={`/programs/${programId}/screenings/${s.id}`}>Open</Link></td>
                  <td>
                    {}
                    {auth?.username &&
                      program?.state === "SUBMISSION" &&
                      s.submitterUsername &&
                      s.submitterUsername === auth.username &&
                      s.state === "CREATED" && (
                        <>
                          <button onClick={() => doSubmit(s.id)}>Submit</button>{" "}
                          <button onClick={() => doWithdraw(s.id)}>Withdraw</button>
                        </>
                      )
                    }
                  </td>
                </tr>
              ))}
              {!screenings?.length && <tr><td colSpan={8}>No screenings</td></tr>}
            </tbody>
          </table>

          <hr />

          <h3>Create Screening (SUBMITTER) — μόνο στο SUBMISSION</h3>
          {!canCreate && <div style={{ opacity: 0.8 }}>Δεν επιτρέπεται τώρα (state: {program?.state}).</div>}
          {canCreate && (
            <form onSubmit={handleCreate} style={{ display: "grid", gap: 8, maxWidth: 520 }}>
              <input value={filmTitle} onChange={(e) => setFilmTitle(e.target.value)} placeholder="filmTitle" />
              <input value={aud} onChange={(e) => setAud(e.target.value)} placeholder="auditoriumName" />
              <label>
                start:
                <input type="datetime-local" value={start} onChange={(e) => setStart(e.target.value)} />
              </label>
              <input value={duration} onChange={(e) => setDuration(e.target.value)} placeholder="duration minutes (optional if end given)" />
              <label>
                end (optional):
                <input type="datetime-local" value={end} onChange={(e) => setEnd(e.target.value)} />
              </label>
              <button type="submit">Create</button>
              {createErr && <div style={{ color: "crimson" }}>{createErr}</div>}
            </form>
          )}
        </>
      )}
    </div>
  );
}
