import http, { httpGet, httpPost } from "./http";

// λίστα όλων των programs
export function fetchPrograms() {
  return httpGet("/programs");
}

// λεπτομέρειες ενός program
export function fetchProgramById(id) {
  return httpGet(`/programs/${id}`);
}

// Search programs
export function searchPrograms(criteria) {

  return httpPost("/programs/search", criteria ?? {});
}

// Δημιουργία program
export function createProgram(payload) {

  return httpPost("/programs", payload);
}

// Αλλαγή κατάστασης προγράμματος
export function changeProgramState(id, newState) {
  // Σωστό: query params μέσω helper, όχι string concat
  return http.post(`/programs/${id}/state`, null, { newState });
}

// Assign staff σε program
export function addStaffToProgram(programId, username) {

  return http.post(`/programs/${programId}/staff/${encodeURIComponent(username)}`, null);
}

// Assign programmer σε program
export function addProgrammerToProgram(programId, username) {
  return http.post(
    `/programs/${programId}/programmers/${encodeURIComponent(username)}`,
    null
  );
}
