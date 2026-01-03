import { apiClient } from "./apiClient";

const BASE = apiClient.bases.patient + "/api";

export function fetchPatients() {
    return apiClient.get(`${BASE}/patients`);
}

export function createPatient(data) {
    return apiClient.post(`${BASE}/patients`, data);
}
