import { useEffect, useMemo, useState } from "react";
import {
  fetchPrograms,
  fetchProgramById,
  changeProgramState,
  searchPrograms,
} from "../api/programApi";
import {
  fetchScreenings,
  createScreening,
  submitScreening,
  withdrawScreening,
} from "../api/screeningApi";
import { setBasicAuth } from "../api/http";
import "./App.css";

const NEXT_PROGRAM_STATE = {
  CREATED: "SUBMISSION",
  SUBMISSION: "ASSIGNMENT",
  ASSIGNMENT: "REVIEW",
  REVIEW: "SCHEDULING",
  SCHEDULING: "FINAL_PUBLICATION",
  FINAL_PUBLICATION: "DECISION",
  DECISION: "ANNOUNCED",
};


function getErrMsg(err, fallback) {
  if (!err) return fallback;
  if (typeof err.body === "string" && err.body.trim()) return err.body;
  if (typeof err.message === "string" && err.message.trim()) return err.message;
  return fallback;
}

function ProgramListPage() {
  const [programs, setPrograms] = useState([]);
  const [programsError, setProgramsError] = useState(null);
  const [loadingPrograms, setLoadingPrograms] = useState(false);

  const [selectedProgramId, setSelectedProgramId] = useState(null);
  const [selectedProgram, setSelectedProgram] = useState(null);
  const [detailsError, setDetailsError] = useState(null);
  const [loadingDetails, setLoadingDetails] = useState(false);

  // Program state change
  const [programStateError, setProgramStateError] = useState(null);
  const [programStateLoading, setProgramStateLoading] = useState(false);

  // Screenings
  const [allScreenings, setAllScreenings] = useState([]);
  const [screenings, setScreenings] = useState([]);
  const [screeningsError, setScreeningsError] = useState(null);
  const [loadingScreenings, setLoadingScreenings] = useState(false);

  // Error για submit/withdraw
  const [screeningActionError, setScreeningActionError] = useState(null);

  // Create screening form
  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newTitle, setNewTitle] = useState("");
  const [newCast, setNewCast] = useState("");
  const [newGenre, setNewGenre] = useState("");
  const [newDuration, setNewDuration] = useState("");
  const [newAuditorium, setNewAuditorium] = useState("");
  const [newStart, setNewStart] = useState("");
  const [newEnd, setNewEnd] = useState("");
  const [creatingScreening, setCreatingScreening] = useState(false);
  const [createScreeningError, setCreateScreeningError] = useState(null);

  // LOGIN STATE
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [loggedInUser, setLoggedInUser] = useState(null);
  const [loginError, setLoginError] = useState(null);
  const [authVersion, setAuthVersion] = useState(0);

  // SEARCH STATE
  const [searchTitle, setSearchTitle] = useState("");
  const [searchCast, setSearchCast] = useState("");
  const [searchGenre, setSearchGenre] = useState("");
  const [searchFrom, setSearchFrom] = useState("");
  const [searchTo, setSearchTo] = useState("");

  const canCreateScreening = useMemo(() => {

    return !!loggedInUser && selectedProgram?.state === "SUBMISSION";
  }, [loggedInUser, selectedProgram?.state]);

  const containsIgnoreCase = (text, needle) => {
    if (!needle) return true;
    if (!text) return false;
    return text.toLowerCase().includes(needle.toLowerCase());
  };

  const dateInRange = (isoDateString, fromStr, toStr) => {
    if (!fromStr && !toStr) return true;
    if (!isoDateString) return false;

    const d = new Date(isoDateString.substring(0, 10));
    if (fromStr) {
      const from = new Date(fromStr);
      if (d < from) return false;
    }
    if (toStr) {
      const to = new Date(toStr);
      if (d > to) return false;
    }
    return true;
  };

  const applySearchFilters = (baseList) => {
    return baseList.filter((s) => {
      const okTitle = containsIgnoreCase(s.filmTitle, searchTitle);
      const okCast = containsIgnoreCase(s.filmCast, searchCast);
      const genresText = s.filmGenres || "";
      const okGenre = containsIgnoreCase(genresText, searchGenre);
      const okDate = dateInRange(s.startTime, searchFrom, searchTo);
      return okTitle && okCast && okGenre && okDate;
    });
  };

  const handleSearchClick = () => {
    setScreenings(applySearchFilters(allScreenings));
  };

  const handleClearClick = () => {
    setSearchTitle("");
    setSearchCast("");
    setSearchGenre("");
    setSearchFrom("");
    setSearchTo("");
    setScreenings(allScreenings);
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoginError(null);

    if (!username || !password) {
      setLoginError("Δώσε username και password");
      return;
    }

    setBasicAuth(username, password);

    try {

      await searchPrograms({});

      setLoggedInUser(username);
      setAuthVersion((v) => v + 1);
    } catch (err) {
      console.error("Login failed", err);
      setBasicAuth(null, null);
      setLoggedInUser(null);
      setLoginError(getErrMsg(err, "Login failed (λάθος στοιχεία ή 401/403)"));
    }
  };

  const handleLogout = () => {
    setBasicAuth(null, null);
    setLoggedInUser(null);
    setUsername("");
    setPassword("");
    setAuthVersion((v) => v + 1);
  };

  // ΛΙΣΤΑ ΠΡΟΓΡΑΜΜΑΤΩΝ
  useEffect(() => {
    setLoadingPrograms(true);
    setProgramsError(null);

    fetchPrograms()
      .then((data) => {
        const list = data || [];
        setPrograms(list);

        if (list.length > 0) {
          setSelectedProgramId((prev) => prev ?? list[0].id);
        } else {
          setSelectedProgramId(null);
        }
      })
      .catch((err) => {
        console.error("Error loading programs", err);
        setProgramsError(getErrMsg(err, "Error loading programs"));
      })
      .finally(() => setLoadingPrograms(false));
  }, [authVersion]);

  // ΛΕΠΤΟΜΕΡΕΙΕΣ ΠΡΟΓΡΑΜΜΑΤΟΣ
  useEffect(() => {
    if (!selectedProgramId) {
      setSelectedProgram(null);
      return;
    }

    setLoadingDetails(true);
    setDetailsError(null);

    fetchProgramById(selectedProgramId)
      .then((data) => {
        setSelectedProgram(data);

        if (data?.state !== "SUBMISSION") setShowCreateForm(false);
      })
      .catch((err) => {
        console.error("Error loading program details", err);
        setDetailsError(getErrMsg(err, "Error loading program details"));
      })
      .finally(() => setLoadingDetails(false));
  }, [selectedProgramId, authVersion]);

  const refreshScreenings = async () => {
    if (!selectedProgramId) {
      setAllScreenings([]);
      setScreenings([]);
      return;
    }

    try {
      const data = await fetchScreenings(selectedProgramId);
      const list = data || [];
      setAllScreenings(list);
      setScreenings(applySearchFilters(list));
      setScreeningsError(null);
    } catch (err) {
      console.error("Error loading screenings", err);
      setScreeningsError(getErrMsg(err, "Error loading screenings"));
      setAllScreenings([]);
      setScreenings([]);
    }
  };

  // SCREENINGS
  useEffect(() => {
    if (!selectedProgramId) {
      setAllScreenings([]);
      setScreenings([]);
      return;
    }

    setLoadingScreenings(true);
    setScreeningsError(null);

    refreshScreenings().finally(() => setLoadingScreenings(false));
  }, [selectedProgramId, authVersion]);

  const handleProgramClick = (program) => {
    setSelectedProgramId(program.id);
    setProgramStateError(null);
    setScreeningActionError(null);
    setCreateScreeningError(null);
  };

  const formatDate = (dateString) => {
    if (!dateString) return "-";
    return dateString.replace("T", " ").substring(0, 16);
  };

  const formatRange = (start, end) => {
    if (!start || !end) return "-";
    return `${start} → ${end}`;
  };

  const handleAdvanceProgramState = async () => {
    if (!selectedProgram) return;

    const current = selectedProgram.state;
    const next = NEXT_PROGRAM_STATE[current];

    if (!next) {
      setProgramStateError("Program is already in final ANNOUNCED state.");
      return;
    }

    setProgramStateError(null);
    setProgramStateLoading(true);

    try {
      const updated = await changeProgramState(selectedProgram.id, next);
      setSelectedProgram(updated);

      const list = await fetchPrograms();
      setPrograms(list);
    } catch (err) {
      console.error("Error changing program state", err);
      setProgramStateError(getErrMsg(err, "Error changing program state"));
    } finally {
      setProgramStateLoading(false);
    }
  };

  const handleCreateScreening = async (e) => {
    e.preventDefault();
    if (!selectedProgramId) return;

    setCreateScreeningError(null);

    const durationNum = newDuration ? Number(newDuration) : 0;

    const payload = {
      filmTitle: newTitle?.trim(),
      filmCast: newCast?.trim(),
      filmGenres: newGenre?.trim(),
      filmDurationMinutes: durationNum,
      auditoriumName: newAuditorium?.trim(),

      startTime: newStart ? `${newStart}:00` : null,

      endTime: newEnd ? `${newEnd}:00` : null,
    };


    if (!payload.filmTitle || !payload.auditoriumName || !payload.startTime) {
      setCreateScreeningError("Συμπλήρωσε τουλάχιστον Title, Auditorium, Start.");
      return;
    }


    if (!payload.endTime && (!durationNum || durationNum <= 0)) {
      setCreateScreeningError("Βάλε Duration (min) ή δώσε End time.");
      return;
    }

    setCreatingScreening(true);
    try {
      await createScreening(selectedProgramId, payload);
      await refreshScreenings();


      setShowCreateForm(false);
      setNewTitle("");
      setNewCast("");
      setNewGenre("");
      setNewDuration("");
      setNewAuditorium("");
      setNewStart("");
      setNewEnd("");
    } catch (err) {
      console.error("Error creating screening", err);
      setCreateScreeningError(getErrMsg(err, "Error creating screening"));
    } finally {
      setCreatingScreening(false);
    }
  };


  const handleSubmitScreening = async (screeningId) => {
    if (!selectedProgramId) return;
    setScreeningActionError(null);

    try {
      await submitScreening(selectedProgramId, screeningId);
      await refreshScreenings();
    } catch (err) {
      console.error("Error submitting screening", err);
      setScreeningActionError(getErrMsg(err, "Error submitting screening"));
    }
  };

  const handleWithdrawScreening = async (screeningId) => {
    if (!selectedProgramId) return;
    setScreeningActionError(null);

    try {
      await withdrawScreening(selectedProgramId, screeningId);
      await refreshScreenings();
    } catch (err) {
      console.error("Error withdrawing screening", err);
      setScreeningActionError(getErrMsg(err, "Error withdrawing screening"));
    }
  };

  return (
    <div className="page">
      {/* LOGIN BAR */}
      <div className="login-bar">
        <form className="login-form" onSubmit={handleLogin}>
          <input
            type="text"
            placeholder="username (visitor, user1, prog1, staff1, submitter)"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
          />
          <input
            type="password"
            placeholder="password (ίδιο με το username)"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
          <button type="submit">Login</button>
          {loggedInUser && (
            <button type="button" className="logout-btn" onClick={handleLogout}>
              Logout
            </button>
          )}
        </form>

        <div className="login-info">
          {loggedInUser ? (
            <span>
              Logged in as <strong>{loggedInUser}</strong>
            </span>
          ) : (
            <span>Currently browsing as VISITOR</span>
          )}
          {loginError && <span className="error-text"> {loginError}</span>}
        </div>
      </div>

      <h1 className="page-title">Cinema Programs</h1>

      <div className="layout">
        {/* ΛΙΣΤΑ ΠΡΟΓΡΑΜΜΑΤΩΝ */}
        <div className="panel panel-left">
          <h2 className="panel-title">Programs</h2>

          {loadingPrograms && <div className="info-text">Loading programs...</div>}
          {programsError && <div className="error-text">{programsError}</div>}

          {!loadingPrograms && !programsError && (
            <table className="table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Dates</th>
                  <th>State</th>
                </tr>
              </thead>
              <tbody>
                {programs.map((p) => (
                  <tr
                    key={p.id}
                    className={p.id === selectedProgramId ? "table-row selected" : "table-row"}
                    onClick={() => handleProgramClick(p)}
                  >
                    <td>{p.id}</td>
                    <td>{p.name}</td>
                    <td>{formatRange(p.startDate, p.endDate)}</td>
                    <td>{p.state}</td>
                  </tr>
                ))}
                {programs.length === 0 && (
                  <tr>
                    <td colSpan={4} className="empty-text">
                      No programs found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          )}
        </div>

        {/* ΛΕΠΤΟΜΕΡΕΙΕΣ + SCREENINGS */}
        <div className="panel panel-right">
          <h2 className="panel-title">Details</h2>

          {loadingDetails && <div className="info-text">Loading details...</div>}
          {detailsError && <div className="error-text">{detailsError}</div>}

          {!loadingDetails && !detailsError && !selectedProgram && (
            <div className="info-text">Select a program from the list to view details.</div>
          )}

          {!loadingDetails && !detailsError && selectedProgram && (
            <>
              <div className="program-details">
                <h3 className="program-title">{selectedProgram.name}</h3>

                <p>
                  <strong>Dates:</strong>{" "}
                  {formatRange(selectedProgram.startDate, selectedProgram.endDate)}
                </p>

                <p>{selectedProgram.description}</p>

                <p>
                  <strong>State:</strong> {selectedProgram.state}
                </p>

                <div className="program-meta">
                  <div>
                    <strong>Creator:</strong> {selectedProgram.creatorUsername || "-"}
                  </div>
                  <div>
                    <strong>Created:</strong> {formatDate(selectedProgram.creationDate)}
                  </div>
                  <div>
                    <strong>Programmers:</strong> {selectedProgram.programmersCount ?? 0}
                  </div>
                  <div>
                    <strong>Staff:</strong> {selectedProgram.staffCount ?? 0}
                  </div>
                  <div>
                    <strong>Screenings:</strong> {selectedProgram.screeningsCount ?? 0}
                  </div>
                </div>

                {/* Advance state  */}
                {loggedInUser && (
                  <div className="program-actions">
                    <button
                      type="button"
                      className="btn-small"
                      onClick={handleAdvanceProgramState}
                      disabled={programStateLoading}
                    >
                      {programStateLoading
                        ? "Changing state..."
                        : (() => {
                            const next = NEXT_PROGRAM_STATE[selectedProgram.state];
                            return next ? `Advance state to ${next}` : "State is final (ANNOUNCED)";
                          })()}
                    </button>
                    {programStateError && <div className="error-text">{programStateError}</div>}
                  </div>
                )}
              </div>

              {/* SCREENINGS SECTION */}
              <div className="screenings-section">
                <div className="screenings-header">
                  <h2 className="panel-title">Screenings</h2>

                  {/* Add screening μόνο όταν SUBMISSION */}
                  {canCreateScreening && (
                    <button
                      type="button"
                      className="btn-small"
                      onClick={() => setShowCreateForm((prev) => !prev)}
                    >
                      {showCreateForm ? "Cancel" : "+ Add screening"}
                    </button>
                  )}
                </div>

                {showCreateForm && canCreateScreening && (
                  <form className="screening-form" onSubmit={handleCreateScreening}>
                    <input
                      className="search-input"
                      placeholder="Title"
                      value={newTitle}
                      onChange={(e) => setNewTitle(e.target.value)}
                      required
                    />
                    <input
                      className="search-input"
                      placeholder="Cast"
                      value={newCast}
                      onChange={(e) => setNewCast(e.target.value)}
                    />
                    <input
                      className="search-input"
                      placeholder="Genre (e.g. Thriller)"
                      value={newGenre}
                      onChange={(e) => setNewGenre(e.target.value)}
                    />
                    <input
                      className="search-input"
                      type="number"
                      placeholder="Duration (min)"
                      value={newDuration}
                      onChange={(e) => setNewDuration(e.target.value)}
                    />
                    <input
                      className="search-input"
                      placeholder="Auditorium (Hall 1)"
                      value={newAuditorium}
                      onChange={(e) => setNewAuditorium(e.target.value)}
                      required
                    />
                    <label>
                      Start:
                      <input
                        type="datetime-local"
                        value={newStart}
                        onChange={(e) => setNewStart(e.target.value)}
                        required
                      />
                    </label>
                    <label>
                      End (optional):
                      <input
                        type="datetime-local"
                        value={newEnd}
                        onChange={(e) => setNewEnd(e.target.value)}
                      />
                    </label>
                    <button type="submit" disabled={creatingScreening}>
                      {creatingScreening ? "Saving..." : "Save"}
                    </button>
                  </form>
                )}

                {createScreeningError && <div className="error-text">{createScreeningError}</div>}

                {/* SEARCH BAR ΓΙΑ SCREENINGS */}
                <div className="search-row">
                  <input
                    className="search-input"
                    placeholder="Title"
                    value={searchTitle}
                    onChange={(e) => setSearchTitle(e.target.value)}
                  />
                  <input
                    className="search-input"
                    placeholder="Cast"
                    value={searchCast}
                    onChange={(e) => setSearchCast(e.target.value)}
                  />
                  <input
                    className="search-input"
                    placeholder="Genre"
                    value={searchGenre}
                    onChange={(e) => setSearchGenre(e.target.value)}
                  />
                </div>
                <div className="search-row">
                  <label>
                    From:
                    <input
                      type="date"
                      value={searchFrom}
                      onChange={(e) => setSearchFrom(e.target.value)}
                    />
                  </label>
                  <label>
                    To:
                    <input
                      type="date"
                      value={searchTo}
                      onChange={(e) => setSearchTo(e.target.value)}
                    />
                  </label>
                  <button onClick={handleSearchClick}>Search</button>
                  <button onClick={handleClearClick}>Clear</button>
                </div>

                {loadingScreenings && <div className="info-text">Loading screenings...</div>}
                {screeningsError && <div className="error-text">{screeningsError}</div>}
                {screeningActionError && <div className="error-text">{screeningActionError}</div>}

                {!loadingScreenings && !screeningsError && screenings.length === 0 && (
                  <div className="empty-text">No screenings for this program.</div>
                )}

                {!loadingScreenings && !screeningsError && screenings.length > 0 && (
                  <table className="table">
                    <thead>
                      <tr>
                        <th>Title</th>
                        <th>Auditorium</th>
                        <th>Start</th>
                        <th>End</th>
                        <th>State</th>
                        <th>Actions</th>
                      </tr>
                    </thead>
                    <tbody>
                      {screenings.map((s) => (
                        <tr key={s.id}>
                          <td>{s.filmTitle}</td>
                          <td>{s.auditoriumName}</td>
                          <td>{formatDate(s.startTime)}</td>
                          <td>{formatDate(s.endTime)}</td>
                          <td>{s.state}</td>
                          <td>
                            {}
                            {loggedInUser &&
                              selectedProgram?.state === "SUBMISSION" &&
                              s.submitterUsername &&
                              loggedInUser === s.submitterUsername &&
                              s.state === "CREATED" && (
                                <>
                                  <button
                                    className="btn-small"
                                    onClick={() => handleSubmitScreening(s.id)}
                                  >
                                    Submit
                                  </button>
                                  <button
                                    className="btn-small"
                                    onClick={() => handleWithdrawScreening(s.id)}
                                  >
                                    Withdraw
                                  </button>
                                </>
                              )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
}

export default ProgramListPage;
