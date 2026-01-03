import { apiClient } from "./apiClient";

const BASE = apiClient.bases.encounter + "/api";

// Doctor / Staff (alla encounters)
export async function fetchEncounters() {
    return apiClient.get(`${BASE}/encounters`);
}

// Patient (endast egna encounters)
export async function fetchEncountersByPatient(patientId) {
    return apiClient.get(`${BASE}/encounters?patientId=${patientId}`);
}

export async function createEncounter(data) {
    return apiClient.post(`${BASE}/encounters`, data);
}
