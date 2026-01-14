import { useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import {
  fetchScreeningById,
  updateScreening,
  submitScreening,
  withdrawScreening,
  assignHandler,
  reviewScreening,
  approveScreening,
  rejectScreening,
  finalSubmitScreening,
  acceptScreening,
} from "../api/screeningApi.js";
import { fetchProgramById } from "../api/programApi.js";

function getErrMsg(err, fallback = "Error") {
  if (!err) return fallback;
  if (err.body && String(err.body).trim()) return String(err.body);
  return err.message || fallback;
}

export default function ScreeningPage({ auth }) {
  const { programId, screeningId } = useParams();
  const [program, setProgram] = useState(null);
  const [screening, setScreening] = useState(null);
  const [err, setErr] = useState(null);


  const [title, setTitle] = useState("");
  const [aud, setAud] = useState("");
  const [start, setStart] = useState("");
  const [duration, setDuration] = useState("");
  const [end, setEnd] = useState("");


  const [handlerUsername, setHandlerUsername] = useState("");
  const [score, setScore] = useState("");
  const [comments, setComments] = useState("");
  const [approveNotes, setApproveNotes] = useState("");
  const [rejectReason, setRejectReason] = useState("");

  async function load() {
    setErr(null);
    try {
      const p = await fetchProgramById(programId);
      setProgram(p);

      const s = await fetchScreeningById(programId, screeningId);
      setScreening(s);


      setTitle(s.filmTitle || "");
      setAud(s.auditoriumName || "");
      setStart(s.startTime ? s.startTime.substring(0, 16) : "");
      setEnd(s.endTime ? s.endTime.substring(0, 16) : "");
      setDuration(
        s.filmDurationMinutes !== null && s.filmDurationMinutes !== undefined
          ? String(s.filmDurationMinutes)
          : ""
      );


      setScore(
        s.reviewScore !== null && s.reviewScore !== undefined
          ? String(s.reviewScore)
          : ""
      );
      setComments(s.reviewComments || "");
    } catch (e) {
      setErr(getErrMsg(e, "Failed to load"));
    }
  }

  useEffect(() => {
    load();

  }, [programId, screeningId]);

  const isSubmitter = useMemo(() => {
    return (
      !!auth?.username &&
      screening?.submitterUsername &&
      screening.submitterUsername === auth.username
    );
  }, [auth?.username, screening?.submitterUsername]);

  const isHandler = useMemo(() => {
    return (
      !!auth?.username &&
      screening?.handlerUsername &&
      screening.handlerUsername === auth.username
    );
  }, [auth?.username, screening?.handlerUsername]);


  const canUpdate =
    auth?.username &&
    program?.state === "SUBMISSION" &&
    isSubmitter &&
    screening?.state === "CREATED";
  const canSubmit = canUpdate;
  const canWithdraw = canUpdate;

  const canAssignHandler =
    auth?.username &&
    program?.state === "ASSIGNMENT" &&
    screening?.state === "SUBMITTED";
  const canReview =
    auth?.username &&
    program?.state === "REVIEW" &&
    isHandler &&
    (screening?.state === "SUBMITTED" || screening?.state === "REVIEWED");
  const canApprove =
    auth?.username &&
    program?.state === "SCHEDULING" &&
    isSubmitter &&
    screening?.state === "REVIEWED";
  const canFinalSubmit =
    auth?.username &&
    program?.state === "FINAL_PUBLICATION" &&
    isSubmitter &&
    screening?.state === "APPROVED";
  const canReject =
    auth?.username &&
    (program?.state === "SCHEDULING" || program?.state === "DECISION");
  const canAccept =
    auth?.username &&
    program?.state === "DECISION" &&
    screening?.state === "APPROVED";

  async function doUpdate() {
    try {
      const payload = {
        filmTitle: title,
        auditoriumName: aud,
        filmDurationMinutes: duration ? Number(duration) : 0,
        startTime: start ? `${start}:00` : null,
        endTime: end ? `${end}:00` : null,
      };
      await updateScreening(programId, screeningId, payload);
      await load();
    } catch (e) {
      alert(getErrMsg(e, "Update failed"));
    }
  }

  async function doSubmit() {
    try {
      await submitScreening(programId, screeningId);
      await load();
    } catch (e) {
      alert(getErrMsg(e, "Submit failed"));
    }
  }

  async function doWithdraw() {
    try {
      await withdrawScreening(programId, screeningId);
      await load();
    } catch (e) {
      alert(getErrMsg(e, "Withdraw failed"));
    }
  }

  async function doAssignHandler() {
    try {
      if (!handlerUsername || !handlerUsername.trim()) {
        alert("Δώσε username staff (π.χ. staff1)");
        return;
      }
      await assignHandler(programId, screeningId, handlerUsername.trim());
      await load();
    } catch (e) {
      alert(getErrMsg(e, "Assign handler failed"));
    }
  }

  async function doReview() {
    try {
      const scoreNum = Number(score);

      if (Number.isNaN(scoreNum) || scoreNum < 0 || scoreNum > 10) {
        alert("Score πρέπει να είναι αριθμός 0-10");
        return;
      }
      if (!comments || !comments.trim()) {
        alert("Γράψε comments για το review");
        return;
      }


      await reviewScreening(programId, screeningId, scoreNum, comments.trim());
      await load();
    } catch (e) {
      alert(getErrMsg(e, "Review failed"));
    }
  }

  async function doApprove() {
    try {
      await approveScreening(programId, screeningId, approveNotes);
      await load();
    } catch (e) {
      alert(getErrMsg(e, "Approve failed"));
    }
  }

  async function doReject() {
    try {
      if (!rejectReason || !rejectReason.trim()) {
        alert("Δώσε rejection reason");
        return;
      }
      await rejectScreening(programId, screeningId, rejectReason.trim());
      await load();
    } catch (e) {
      alert(getErrMsg(e, "Reject failed"));
    }
  }

  async function doFinalSubmit() {
    try {
      await finalSubmitScreening(programId, screeningId);
      await load();
    } catch (e) {
      alert(getErrMsg(e, "Final submit failed"));
    }
  }

  async function doAccept() {
    try {
      await acceptScreening(programId, screeningId);
      await load();
    } catch (e) {
      alert(getErrMsg(e, "Accept failed"));
    }
  }

  const hasReview =
    screening &&
    (screening.reviewScore !== null &&
      screening.reviewScore !== undefined);

  return (
    <div>
      <h2>
        Screening #{screeningId} (Program #{programId})
      </h2>

      {err && <div style={{ color: "crimson" }}>{err}</div>}

      {!screening ? (
        <div>Loading...</div>
      ) : (
        <>
          <div style={{ display: "grid", gap: 6 }}>
            <div>
              <b>Film:</b> {screening.filmTitle}
            </div>
            <div>
              <b>State:</b> {screening.state}
            </div>
            <div>
              <b>Submitter:</b> {screening.submitterUsername || "-"}
            </div>
            <div>
              <b>Handler:</b> {screening.handlerUsername || "-"}
            </div>
            <div>
              <b>Start:</b> {screening.startTime || "-"}
            </div>
            <div>
              <b>End:</b> {screening.endTime || "-"}
            </div>

            {/* ΕΜΦΑΝΙΣΗ REVIEW */}
            {hasReview && (
              <div style={{ marginTop: 10 }}>
                <h3 style={{ margin: "10px 0 6px" }}>Review</h3>
                <div>
                  <b>Score:</b> {screening.reviewScore}
                </div>
                <div>
                  <b>Comments:</b> {screening.reviewComments || "-"}
                </div>
              </div>
            )}
          </div>

          <hr />

          <h3>Submitter actions</h3>
          <div style={{ display: "grid", gap: 8, maxWidth: 520 }}>
            <button disabled={!canSubmit} onClick={doSubmit}>
              Submit
            </button>
            <button disabled={!canWithdraw} onClick={doWithdraw}>
              Withdraw
            </button>

            <label>
              Approve notes (SCHEDULING + REVIEWED):
              <input
                value={approveNotes}
                onChange={(e) => setApproveNotes(e.target.value)}
              />
            </label>
            <button disabled={!canApprove} onClick={doApprove}>
              Approve
            </button>

            <button disabled={!canFinalSubmit} onClick={doFinalSubmit}>
              Final Submit
            </button>
          </div>

          <hr />

          <h3>Update (μόνο SUBMISSION + CREATED + owner)</h3>
          <div style={{ display: "grid", gap: 8, maxWidth: 520 }}>
            <input
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="filmTitle"
            />
            <input
              value={aud}
              onChange={(e) => setAud(e.target.value)}
              placeholder="auditoriumName"
            />
            <label>
              start:
              <input
                type="datetime-local"
                value={start}
                onChange={(e) => setStart(e.target.value)}
              />
            </label>
            <input
              value={duration}
              onChange={(e) => setDuration(e.target.value)}
              placeholder="duration minutes"
            />
            <label>
              end:
              <input
                type="datetime-local"
                value={end}
                onChange={(e) => setEnd(e.target.value)}
              />
            </label>
            <button disabled={!canUpdate} onClick={doUpdate}>
              Update
            </button>
          </div>

          <hr />

          <h3>Programmer actions</h3>
          <div style={{ display: "grid", gap: 8, maxWidth: 520 }}>
            <label>
              Assign handler username:
              <input
                value={handlerUsername}
                onChange={(e) => setHandlerUsername(e.target.value)}
                placeholder="staff1"
              />
            </label>
            <button disabled={!canAssignHandler} onClick={doAssignHandler}>
              Assign Handler
            </button>

            <label>
              Reject reason:
              <input
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
              />
            </label>
            <button disabled={!canReject} onClick={doReject}>
              Reject
            </button>

            <button disabled={!canAccept} onClick={doAccept}>
              Accept (Schedule)
            </button>
          </div>

          <hr />

          <h3>Staff actions</h3>
          <div style={{ display: "grid", gap: 8, maxWidth: 520 }}>
            <input
              value={score}
              onChange={(e) => setScore(e.target.value)}
              placeholder="score (0-10)"
            />
            <textarea
              value={comments}
              onChange={(e) => setComments(e.target.value)}
              placeholder="comments"
            />
            <button disabled={!canReview} onClick={doReview}>
              Review
            </button>
          </div>
        </>
      )}
    </div>
  );
}
