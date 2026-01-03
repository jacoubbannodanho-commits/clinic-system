import { apiClient } from "./apiClient";

// går via Vite proxy
const BASE = "/api/search";

export function searchPatients(params = {}) {
    const { name = "", ssn = "", conditionCode = "" } = params;

    const q = new URLSearchParams();
    if (name) q.set("name", name);
    if (ssn) q.set("ssn", ssn);
    if (conditionCode) q.set("conditionCode", conditionCode);

    const url = q.toString() ? `${BASE}/patients?${q}` : `${BASE}/patients`;
    return apiClient.get(url);
}

// === Doctor/day via ID ===
export function searchDoctorDayById({ practitionerId, date }) {
    const pid = String(practitionerId ?? "").trim();
    const d = String(date ?? "").trim();
    if (!pid) return Promise.reject(new Error("Missing practitionerId"));
    if (!d) return Promise.reject(new Error("Missing date (yyyy-MM-dd)"));

    const q = new URLSearchParams({ date: d });
    return apiClient.get(`${BASE}/practitioners/${pid}/day?${q}`);
}

// ✅ Backwards compatible export for current SearchPage.jsx
export function searchDoctorDay({ practitionerId, date } = {}) {
    // Om UI skickar tomt: defaulta till fungerande testvärden
    const pid = String(practitionerId ?? "1").trim() || "1";
    const d = String(date ?? "2025-12-29").trim() || "2025-12-29";
    return searchDoctorDayById({ practitionerId: pid, date: d });
}

// === Doctor/day via NAME ===
export function searchDoctorDayByName({ name, date }) {
    const n = String(name ?? "").trim();
    const d = String(date ?? "").trim();
    if (!n) return Promise.reject(new Error("Missing doctor name"));
    if (!d) return Promise.reject(new Error("Missing date (yyyy-MM-dd)"));

    const q = new URLSearchParams({ name: n, date: d });
    return apiClient.get(`${BASE}/practitioners/day?${q}`);
}
