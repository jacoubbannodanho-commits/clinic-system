import keycloak from "./keycloak";
import { apiClient } from "./apiClient";

const BASE = apiClient.bases.message + "/api/messages";

/* ===== Patient inbox ===== */
export async function fetchInbox() {
    await keycloak.updateToken(5).catch(() => keycloak.login());

    const resp = await fetch(`${BASE}/my-inbox`, {
        headers: {
            Authorization: `Bearer ${keycloak.token}`
        }
    });

    if (!resp.ok) {
        throw new Error(await resp.text());
    }

    return resp.json();
}

/* ===== Doctor / staff: messages for patient ===== */
export async function fetchMessagesByPatient(patientId) {
    await keycloak.updateToken(5).catch(() => keycloak.login());

    const resp = await fetch(`${BASE}/by-patient/${patientId}`, {
        headers: {
            Authorization: `Bearer ${keycloak.token}`
        }
    });

    if (!resp.ok) {
        throw new Error(await resp.text());
    }

    return resp.json();
}

/* ===== Send message ===== */
export async function sendMessage({ patientId, receiverKeycloakId, content }) {
    await keycloak.updateToken(5).catch(() => keycloak.login());

    const resp = await fetch(BASE, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${keycloak.token}`
        },
        body: JSON.stringify({
            patientId,
            receiverKeycloakId,
            content
        })
    });

    if (!resp.ok) {
        throw new Error(await resp.text());
    }

    return resp.json();
}

/* ===== Mark read ===== */
export async function markMessageRead(id) {
    await keycloak.updateToken(5).catch(() => keycloak.login());

    const resp = await fetch(`${BASE}/${id}/read`, {
        method: "POST",
        headers: {
            Authorization: `Bearer ${keycloak.token}`
        }
    });

    if (!resp.ok) {
        throw new Error(await resp.text());
    }
}
