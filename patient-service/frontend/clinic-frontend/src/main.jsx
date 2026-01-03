import React from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App.jsx";
import keycloak from "./api/keycloak.js";

const root = document.getElementById("root");

keycloak
    .init({
        onLoad: "login-required",
        checkLoginIframe: false
    })
    .then((authenticated) => {
        if (!authenticated) {
            keycloak.login();
            return;
        }


        window.keycloak = keycloak;

        createRoot(root).render(
            <BrowserRouter>
                <App keycloak={keycloak} />
            </BrowserRouter>
        );
    })
    .catch(err => {
        console.error("Keycloak init failed", err);
    });
