import { useState } from "react";
import {
    searchPatients,
    searchDoctorDay,       // via ID
    searchDoctorDayByName, // via namn
} from "../api/searchApi";

export default function SearchPage() {
    // =========================
    // 1) Patient-sök
    // =========================
    const [patientForm, setPatientForm] = useState({
        name: "",
        ssn: "",
        conditionCode: "",
    });

    const [patientResult, setPatientResult] = useState([]);
    const [patientError, setPatientError] = useState("");
    const [patientLoading, setPatientLoading] = useState(false);

    async function onSearchPatients(e) {
        e.preventDefault();
        setPatientError("");
        setPatientResult([]); // ✅ ersätt (inte klistra)
        setPatientLoading(true);

        try {
            const data = await searchPatients({
                name: patientForm.name.trim(),
                ssn: patientForm.ssn.trim(),
                conditionCode: patientForm.conditionCode.trim(),
            });
            setPatientResult(Array.isArray(data) ? data : []);
        } catch (err) {
            setPatientError(err?.message || "Kunde inte söka patienter");
        } finally {
            setPatientLoading(false);
        }
    }

    // =========================
    // 2) Läkare + dag (2 lägen)
    // =========================
    const [doctorMode, setDoctorMode] = useState("id"); // "id" eller "name"
    const [doctorId, setDoctorId] = useState("1");
    const [doctorName, setDoctorName] = useState("");
    const [doctorDate, setDoctorDate] = useState("2025-12-29");

    const [doctorResult, setDoctorResult] = useState(null); // visar EN sammanfattning
    const [doctorError, setDoctorError] = useState("");
    const [doctorLoading, setDoctorLoading] = useState(false);

    async function onSearchDoctorDay(e) {
        e.preventDefault();
        setDoctorError("");
        setDoctorResult(null); // ✅ ersätt (inte klistra)
        setDoctorLoading(true);

        try {
            if (doctorMode === "id") {
                const data = await searchDoctorDay({
                    practitionerId: doctorId.trim(),
                    date: doctorDate.trim(),
                });
                setDoctorResult(data || null);
            } else {
                const list = await searchDoctorDayByName({
                    name: doctorName.trim(),
                    date: doctorDate.trim(),
                });
                // backend returnerar lista (kan vara flera matchar namnet)
                setDoctorResult((Array.isArray(list) && list[0]) ? list[0] : null);
            }
        } catch (err) {
            setDoctorError(err?.message || "Kunde inte söka läkare/dag");
        } finally {
            setDoctorLoading(false);
        }
    }

    // =========================
    // UI
    // =========================
    return (
        <div style={{ maxWidth: 900 }}>
            <h2>Search</h2>

            {/* ==================== ROUTE 1: Patient-sök ==================== */}
            <div style={{ border: "1px solid #ddd", padding: 14, marginBottom: 18 }}>
                <h3>1) Sök patienter</h3>

                <form onSubmit={onSearchPatients}>
                    <div style={{ display: "grid", gap: 8 }}>
                        <label>
                            Name:
                            <input
                                style={{ width: "100%" }}
                                placeholder="t.ex. Jacoub"
                                value={patientForm.name}
                                onChange={(e) => setPatientForm({ ...patientForm, name: e.target.value })}
                            />
                        </label>

                        <label>
                            SSN:
                            <input
                                style={{ width: "100%" }}
                                placeholder="t.ex. 9701100308"
                                value={patientForm.ssn}
                                onChange={(e) => setPatientForm({ ...patientForm, ssn: e.target.value })}
                            />
                        </label>

                        <label>
                            Condition code:
                            <input
                                style={{ width: "100%" }}
                                placeholder="t.ex. 1111 (era diagnosis-koder)"
                                value={patientForm.conditionCode}
                                onChange={(e) => setPatientForm({ ...patientForm, conditionCode: e.target.value })}
                            />
                        </label>

                        <button type="submit" disabled={patientLoading}>
                            {patientLoading ? "Söker..." : "Sök patienter"}
                        </button>
                    </div>
                </form>

                {patientError && <p style={{ color: "red" }}>{patientError}</p>}

                {patientResult.length > 0 && (
                    <div style={{ marginTop: 12 }}>
                        <h4>Resultat</h4>
                        <ul>
                            {patientResult.map((r) => (
                                <li key={r.patient?.id}>
                                    <div>
                                        <b>
                                            {r.patient?.firstName} {r.patient?.lastName}
                                        </b>{" "}
                                        (id: {r.patient?.id}, ssn: {r.patient?.ssn})
                                    </div>

                                    <div style={{ marginTop: 4 }}>
                                        <b>Conditions:</b>{" "}
                                        {r.conditions?.length ? (
                                            <ul>
                                                {r.conditions.map((c) => (
                                                    <li key={c.id ?? c.code}>
                                                        {c.code}{c.description ? ` – ${c.description}` : ""}
                                                    </li>
                                                ))}
                                            </ul>
                                        ) : (
                                            <span>inga diagnoser</span>
                                        )}
                                    </div>
                                </li>
                            ))}
                        </ul>
                    </div>
                )}
            </div>

            {/* ==================== ROUTE 2: Läkare + dag ==================== */}
            <div style={{ border: "1px solid #ddd", padding: 14 }}>
                <h3>2) Sök via läkare + dag</h3>

                {/* Mode switch */}
                <div style={{ display: "flex", gap: 12, marginBottom: 10 }}>
                    <label>
                        <input
                            type="radio"
                            checked={doctorMode === "id"}
                            onChange={() => setDoctorMode("id")}
                        />
                        Sök med ID
                    </label>

                    <label>
                        <input
                            type="radio"
                            checked={doctorMode === "name"}
                            onChange={() => setDoctorMode("name")}
                        />
                        Sök med namn
                    </label>
                </div>

                <form onSubmit={onSearchDoctorDay}>
                    <div style={{ display: "grid", gap: 8 }}>
                        {doctorMode === "id" ? (
                            <label>
                                Practitioner id:
                                <input
                                    style={{ width: "100%" }}
                                    placeholder="t.ex. 1"
                                    value={doctorId}
                                    onChange={(e) => setDoctorId(e.target.value)}
                                />
                            </label>
                        ) : (
                            <label>
                                Läkare namn:
                                <input
                                    style={{ width: "100%" }}
                                    placeholder="t.ex. Jacoub"
                                    value={doctorName}
                                    onChange={(e) => setDoctorName(e.target.value)}
                                />
                            </label>
                        )}

                        <label>
                            Datum:
                            <input
                                style={{ width: "100%" }}
                                placeholder="åååå-mm-dd"
                                value={doctorDate}
                                onChange={(e) => setDoctorDate(e.target.value)}
                            />
                        </label>

                        <button type="submit" disabled={doctorLoading}>
                            {doctorLoading ? "Söker..." : "Sök dag"}
                        </button>
                    </div>
                </form>

                {doctorError && <p style={{ color: "red" }}>{doctorError}</p>}

                {doctorResult && (
                    <div style={{ marginTop: 12 }}>
                        <p>
                            <b>Läkare:</b>{" "}
                            {doctorResult.practitioner
                                ? `${doctorResult.practitioner.firstName ?? ""} ${doctorResult.practitioner.lastName ?? ""}`.trim()
                                : "(saknas i practitioner-service)"}
                        </p>
                        <p>
                            <b>Datum:</b> {doctorResult.date}
                        </p>

                        <p><b>Encounters den dagen:</b></p>
                        <ul>
                            {(doctorResult.encounters ?? []).map((x) => (
                                <li key={x.encounter?.id}>
                                    Patient: {x.patient?.firstName} {x.patient?.lastName} (id: {x.patient?.id})
                                    {" "}– encounter id: {x.encounter?.id}
                                </li>
                            ))}
                        </ul>
                    </div>
                )}

                {!doctorResult && !doctorLoading && !doctorError && (
                    <p style={{ marginTop: 10, opacity: 0.7 }}>
                        Tips: välj “Sök med ID” och testa id=1, datum=2025-12-29.
                    </p>
                )}
            </div>
        </div>
    );
}
