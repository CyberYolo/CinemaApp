const API_BASE = "/api";

// Basic Auth header
let authHeader = null;

export function setBasicAuth(username, password) {
  if (username && password) {
    const token = btoa(`${username}:${password}`);
    authHeader = `Basic ${token}`;
  } else {
    authHeader = null;
  }
}

export function clearAuth() {
  authHeader = null;
}

// Helper
function toQuery(params) {
  if (!params) return "";
  const usp = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v === undefined || v === null || v === "") return;
    usp.append(k, String(v));
  });
  const s = usp.toString();
  return s ? `?${s}` : "";
}

async function readErrorBody(response) {
  const contentType = response.headers.get("content-type") || "";
  try {
    if (contentType.includes("application/json")) {
      const data = await response.json();
      return typeof data === "string" ? data : JSON.stringify(data);
    }
  } catch {

  }
  try {
    return await response.text();
  } catch {
    return "";
  }
}

async function request(path, options = {}) {
  const headers = {
    Accept: "application/json",
    ...(options.headers || {}),
  };

  if (authHeader) headers["Authorization"] = authHeader;

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers,
    credentials: "include",
  });


  if (response.status === 204) return null;


  if (!response.ok) {
    const errBody = await readErrorBody(response);
    const msg = errBody?.trim() ? errBody : (response.statusText || "Request failed");
    const error = new Error(`HTTP ${response.status}: ${msg}`);
    error.status = response.status;
    error.body = errBody;
    throw error;
  }


  const contentType = response.headers.get("content-type") || "";
  if (!contentType.includes("application/json")) {
    return response.text();
  }

  return response.json();
}

export function httpGet(path, params) {
  return request(`${path}${toQuery(params)}`, { method: "GET" });
}

export function httpPost(path, body, params) {
  return request(`${path}${toQuery(params)}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: body === undefined || body === null ? undefined : JSON.stringify(body),
  });
}

export function httpPut(path, body, params) {
  return request(`${path}${toQuery(params)}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: body === undefined || body === null ? undefined : JSON.stringify(body),
  });
}

export function httpDelete(path, params) {
  return request(`${path}${toQuery(params)}`, { method: "DELETE" });
}

const http = {
  request,
  get: httpGet,
  post: httpPost,
  put: httpPut,
  delete: httpDelete,
  setBasicAuth,
  clearAuth,
};

export default http;
