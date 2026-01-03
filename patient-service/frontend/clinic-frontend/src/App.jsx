import { Link, Routes, Route, Navigate } from "react-router-dom";
import PatientsPage from "./pages/PatientsPage.jsx";
import PatientDetailsPage from "./pages/PatientDetailsPage.jsx";
import MessagesPage from "./pages/MessagesPage.jsx";
import SearchPage from "./pages/SearchPage.jsx";
import ImagePage from "./pages/ImagePage";

import { useEffect } from "react";

export default function App({ keycloak }) {
    const roles = keycloak.tokenParsed?.realm_access?.roles || [];
    console.log(keycloak.tokenParsed);

    const isCareStaff = roles.includes("doctor") || roles.includes("staff");
    const isPatient = roles.includes("patient");

    useEffect(() => {
        const i = setInterval(() => {
            keycloak.updateToken(60).catch(() => keycloak.login());
        }, 20000);

        return () => clearInterval(i);
    }, [keycloak]);

    return (
        <div style={{ padding: 20 }}>
            <h1>Clinic</h1>

            <div style={{ marginBottom: 20 }}>
                Logged in as: {keycloak.tokenParsed?.preferred_username}
                <button
                    onClick={() => keycloak.logout()}
                    style={{ marginLeft: 10 }}
                >
                    Logout
                </button>
            </div>

            {/* 🔹 ROLE-BASERAD MENY */}
            <nav style={{ marginBottom: 20 }}>
                {isCareStaff && (
                    <>
                        <Link to="/patients" style={{ marginRight: 10 }}>
                            Patients
                        </Link>

                        <Link to="/search" style={{ marginRight: 10 }}>
                            Search
                        </Link>

                        {/* ✅ NY: Images */}
                        <Link to="/images" style={{ marginRight: 10 }}>
                            Images
                        </Link>
                    </>
                )}

                <Link to="/messages" style={{ marginRight: 10 }}>
                    Messages
                </Link>
            </nav>

            {/* 🔹 ROUTES */}
            <Routes>
                {/* Doctor / Staff */}
                {isCareStaff && (
                    <>
                        <Route path="/patients" element={<PatientsPage />} />
                        <Route
                            path="/patients/:id"
                            element={<PatientDetailsPage keycloak={keycloak} />}
                        />
                        <Route path="/search" element={<SearchPage />} />

                        {/* ✅ NY: Images */}
                        <Route path="/images" element={<ImagePage keycloak={keycloak} />} />
                    </>
                )}

                {/* Patient */}
                {isPatient && (
                    <Route
                        path="/"
                        element={<PatientDetailsPage keycloak={keycloak} />}
                    />
                )}

                {/* Messages (alla roller) */}
                <Route
                    path="/messages"
                    element={<MessagesPage keycloak={keycloak} />}
                />

                {/* Fallback */}
                <Route
                    path="*"
                    element={<Navigate to={isPatient ? "/" : "/patients"} />}
                />
            </Routes>
        </div>
    );
}
