import http, { httpGet } from "./http";

export function fetchScreenings(programId) {
  if (!programId) return Promise.resolve([]);
  return httpGet(`/programs/${programId}/screenings`);
}

// Φέρνει μία προβολή
export function fetchScreeningById(programId, screeningId) {
  return httpGet(`/programs/${programId}/screenings/${screeningId}`);
}

export function searchScreenings(programId, criteria) {

  return http.post(`/programs/${programId}/screenings/search`, criteria ?? {});
}

export function createScreening(programId, payload) {
  return http.post(`/programs/${programId}/screenings`, payload);
}

export function updateScreening(programId, screeningId, payload) {
  return http.put(`/programs/${programId}/screenings/${screeningId}`, payload);
}

export function withdrawScreening(programId, screeningId) {
  return http.delete(`/programs/${programId}/screenings/${screeningId}`);
}

export function submitScreening(programId, screeningId) {

  return http.post(`/programs/${programId}/screenings/${screeningId}/submit`, null);
}

export function assignHandler(programId, screeningId, staffUsername) {

  return http.post(
    `/programs/${programId}/screenings/${screeningId}/assign-handler`,
    null,
    { username: staffUsername }
  );
}

export function reviewScreening(programId, screeningId, score, comments) {
  return http.post(`/programs/${programId}/screenings/${screeningId}/review`, {
    score,
    comments,
  });
}

export function approveScreening(programId, screeningId, notes) {

  const params = notes ? { notes } : undefined;
  return http.post(
    `/programs/${programId}/screenings/${screeningId}/approve`,
    null,
    params
  );
}

export function rejectScreening(programId, screeningId, reason) {

  return http.post(
    `/programs/${programId}/screenings/${screeningId}/reject`,
    null,
    { reason }
  );
}

export function finalSubmitScreening(programId, screeningId) {
  return http.post(
    `/programs/${programId}/screenings/${screeningId}/final-submit`,
    null
  );
}

export function acceptScreening(programId, screeningId) {
  return http.post(`/programs/${programId}/screenings/${screeningId}/accept`, null);
}
