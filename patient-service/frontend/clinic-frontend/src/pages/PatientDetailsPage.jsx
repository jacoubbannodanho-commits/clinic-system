import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { fetchPatients } from "../api/patientsApi";
import {
    fetchEncounters,
    fetchEncountersByPatient,
    createEncounter
} from "../api/encountersApi";
import {
    fetchInbox,
    fetchMessagesByPatient,
    sendMessage,
    markMessageRead
} from "../api/messagesApi";
import {
    fetchPatientImage,
    uploadImage,
    annotatePatientImage
} from "../api/imagesApi";

export default function PatientDetailsPage({ keycloak }) {
    const { id } = useParams();

    const roles = keycloak.tokenParsed?.realm_access?.roles || [];
    const isPatient = roles.includes("patient");
    const isCareStaff = roles.includes("doctor") || roles.includes("staff");
    const isDoctor = roles.includes("doctor");

    const loggedInUsername = keycloak.tokenParsed?.preferred_username;

    const [patient, setPatient] = useState(null);
    const [encounters, setEncounters] = useState([]);
    const [messages, setMessages] = useState([]);
    const [error, setError] = useState("");

    /* ===== IMAGE STATE ===== */
    const [imageUrl, setImageUrl] = useState(null);
    const [imageFile, setImageFile] = useState(null);

    const [newEncounter, setNewEncounter] = useState({
        diagnosis: "",
        notes: ""
    });

    const [newMessage, setNewMessage] = useState({
        content: ""
    });

    /* ===============================
       Load correct patient
    =============================== */
    useEffect(() => {
        async function loadPatient() {
            try {
                const list = await fetchPatients();

                if (isPatient) {
                    const p = list.find(x => x.ssn === loggedInUsername);
                    if (!p) throw new Error("No patient for logged in user");
                    setPatient(p);
                } else {
                    const p = list.find(x => x.id === Number(id));
                    setPatient(p);
                }
            } catch (e) {
                setError(e.message);
            }
        }
        loadPatient();
    }, [isPatient, id, loggedInUsername]);

    /* ===============================
       Load encounters
    =============================== */
    useEffect(() => {
        if (!patient?.id) return;

        if (isPatient) {
            fetchEncountersByPatient(patient.id)
                .then(setEncounters)
                .catch(e => setError(e.message));
        } else {
            fetchEncounters()
                .then(all =>
                    setEncounters(all.filter(e => e.patientId === patient.id))
                )
                .catch(e => setError(e.message));
        }
    }, [patient, isPatient]);

    /* ===============================
       Load messages
    =============================== */
    useEffect(() => {
        if (!patient?.id) return;

        if (isCareStaff) {
            fetchMessagesByPatient(patient.id)
                .then(setMessages)
                .catch(e => setError(e.message));
        }

        if (isPatient) {
            fetchInbox()
                .then(setMessages)
                .catch(e => setError(e.message));
        }
    }, [patient, isCareStaff, isPatient]);

    /* ===============================
       Load patient image
    =============================== */
    useEffect(() => {
        if (!patient?.id) return;

        fetchPatientImage(patient.id)
            .then(setImageUrl)
            .catch(() => setImageUrl(null));
    }, [patient]);

    if (!patient) return <div>Loading patient...</div>;

    return (
        <div>
            <h2>Patient Information</h2>
            <p>
                {patient.firstName} {patient.lastName}
            </p>
            <p>SSN: {patient.ssn}</p>

            {error && <p style={{ color: "red" }}>{error}</p>}

            <hr />

            {/* ================= IMAGE ================= */}
            <h2>Patient Image</h2>

            {imageUrl ? (
                <img
                    src={imageUrl}
                    alt="Patient"
                    style={{ maxWidth: 400, border: "1px solid #ccc" }}
                />
            ) : (
                <p>No image uploaded</p>
            )}

            {isDoctor && (
                <div style={{ marginTop: 10 }}>
                    <input
                        type="file"
                        onChange={e => setImageFile(e.target.files[0])}
                    />
                    <button
                        onClick={async () => {
                            if (!imageFile) return;
                            await uploadImage(patient.id, imageFile);
                            const img = await fetchPatientImage(patient.id);
                            setImageUrl(img);
                        }}
                    >
                        Upload image
                    </button>

                    <button
                        onClick={async () => {
                            await annotatePatientImage(patient.id, {
                                text: "Doctor note",
                                x: 40,
                                y: 40,
                                color: "red"
                            });
                            const img = await fetchPatientImage(patient.id);
                            setImageUrl(img);
                        }}
                    >
                        Annotate image
                    </button>
                </div>
            )}

            <hr />

            {/* ================= ENCOUNTERS ================= */}
            <h2>Encounters</h2>

            {isCareStaff && (
                <form
                    onSubmit={e => {
                        e.preventDefault();

                        createEncounter({
                            patientId: patient.id,
                            practitionerId: 1,
                            notes: `Diagnosis: ${newEncounter.diagnosis}\n\n${newEncounter.notes}`
                        })
                            .then(() => {
                                setNewEncounter({ diagnosis: "", notes: "" });
                                return fetchEncounters();
                            })
                            .then(all =>
                                setEncounters(
                                    all.filter(e => e.patientId === patient.id)
                                )
                            )
                            .catch(err => setError(err.message));
                    }}
                >
                    <input
                        placeholder="Diagnosis"
                        value={newEncounter.diagnosis}
                        onChange={e =>
                            setNewEncounter({
                                ...newEncounter,
                                diagnosis: e.target.value
                            })
                        }
                        required
                    />
                    <textarea
                        placeholder="Notes"
                        value={newEncounter.notes}
                        onChange={e =>
                            setNewEncounter({
                                ...newEncounter,
                                notes: e.target.value
                            })
                        }
                        rows={4}
                    />
                    <button>Create encounter</button>
                </form>
            )}

            <ul>
                {encounters.map(e => (
                    <li key={e.id}>{e.notes}</li>
                ))}
            </ul>

            <hr />

            {/* ================= MESSAGES ================= */}
            <h2>Messages</h2>

            <ul>
                {messages.map(m => (
                    <li key={m.id} style={{ marginBottom: 10 }}>
                        <b>{m.senderRole}</b>: {m.content}
                        {!m.read && (
                            <button
                                style={{ marginLeft: 10 }}
                                onClick={() =>
                                    markMessageRead(m.id)
                                        .then(() =>
                                            isCareStaff
                                                ? fetchMessagesByPatient(patient.id)
                                                : fetchInbox()
                                        )
                                        .then(setMessages)
                                }
                            >
                                Mark as read
                            </button>
                        )}
                    </li>
                ))}
            </ul>

            <form
                onSubmit={e => {
                    e.preventDefault();

                    sendMessage({
                        patientId: patient.id,
                        receiverKeycloakId: isPatient
                            ? "doctor"
                            : messages[0]?.senderKeycloakId,
                        content: newMessage.content
                    })
                        .then(() => {
                            setNewMessage({ content: "" });
                            return isCareStaff
                                ? fetchMessagesByPatient(patient.id)
                                : fetchInbox();
                        })
                        .then(setMessages)
                        .catch(e => setError(e.message));
                }}
            >
                <input
                    placeholder="Message"
                    value={newMessage.content}
                    onChange={e =>
                        setNewMessage({ content: e.target.value })
                    }
                    required
                />
                <button>Send</button>
            </form>
        </div>
    );
}
