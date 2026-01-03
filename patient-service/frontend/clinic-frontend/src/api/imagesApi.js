import keycloak from "./keycloak";
import { apiClient } from "./apiClient";

const BASE = apiClient.bases.image + "/api/images";

/* ===== Upload image (doctor only) ===== */
export async function uploadImage(patientId, file) {
    await keycloak.updateToken(5).catch(() => keycloak.login());

    const fd = new FormData();
    fd.append("file", file);

    const resp = await fetch(`${BASE}/upload/${patientId}`, {
        method: "POST",
        headers: {
            Authorization: `Bearer ${keycloak.token}`
        },
        body: fd
    });

    if (!resp.ok) {
        throw new Error(await resp.text());
    }

    return resp.json();
}

/* ===== Get latest image (doctor / staff / patient) ===== */
export async function fetchPatientImage(patientId) {
    await keycloak.updateToken(5).catch(() => keycloak.login());

    const resp = await fetch(`${BASE}/latest/${patientId}`, {
        headers: {
            Authorization: `Bearer ${keycloak.token}`
        }
    });

    if (!resp.ok) {
        throw new Error(await resp.text());
    }

    const blob = await resp.blob();
    return URL.createObjectURL(blob);
}

/* ===== Annotate image (doctor only) ===== */
export async function annotatePatientImage(patientId, payload) {
    await keycloak.updateToken(5).catch(() => keycloak.login());

    const resp = await fetch(`${BASE}/annotate/${patientId}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${keycloak.token}`
        },
        body: JSON.stringify(payload)
    });

    if (!resp.ok) {
        throw new Error(await resp.text());
    }

    return resp.json();
}
