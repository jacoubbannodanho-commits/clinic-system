import keycloak from "./keycloak";

async function authUpload(url, file) {
    await keycloak.updateToken(5).catch(() => keycloak.login());

    const form = new FormData();
    form.append("file", file);

    const resp = await fetch(url, {
        method: "POST",
        headers: {
            Authorization: `Bearer ${keycloak.token}` // لا تضع Content-Type هنا
        },
        body: form
    });

    if (!resp.ok) {
        const t = await resp.text();
        throw new Error(`API error ${resp.status}: ${t}`);
    }

    return resp.json();
}

async function authFetch(method, url, body) {
    await keycloak.updateToken(5).catch(() => keycloak.login());

    const headers = {
        "Content-Type": "application/json",
        Authorization: `Bearer ${keycloak.token}`
    };

    const options = { method, headers };

    if (body !== undefined && body !== null) {
        options.body = JSON.stringify(body);
    }

    const resp = await fetch(url, options);

    if (!resp.ok) {
        const t = await resp.text();
        throw new Error(`API error ${resp.status}: ${t}`);
    }

    // بعض الـ endpoints ممكن ترجع نص بدل JSON
    const ct = resp.headers.get("content-type") || "";
    if (ct.includes("application/json")) return resp.json();
    return resp.text();
}

const BASE_PATIENT   = "http://localhost:30082";
const BASE_ENCOUNTER = "http://localhost:30083";
const BASE_MESSAGE   = "http://localhost:30084";
const BASE_SEARCH    = "http://localhost:30085";
const BASE_IMAGE     = "http://localhost:30086";

export const apiClient = {
    get: (url) => authFetch("GET", url),
    post: (url, body) => authFetch("POST", url, body),

    // ✅ ny: multipart upload (image-service)
    uploadFile: (url, file) => authUpload(url, file),

    bases: {
        patient: BASE_PATIENT,
        encounter: BASE_ENCOUNTER,
        message: BASE_MESSAGE,
        search: BASE_SEARCH,
        image: BASE_IMAGE
    }
};
