import { useEffect, useState } from "react";
import {
    fetchEncounters,
    createEncounter,
    createCondition
} from "../api/encountersApi";

export default function EncountersPage({ token }) {
    const [encounters, setEncounters] = useState([]);
    const [error, setError] = useState(null);

    const [newEncounter, setNewEncounter] = useState({
        patientId: "",
        practitionerId: "",
        notes: ""
    });

    const [newCondition, setNewCondition] = useState({
        patientId: "",
        encounterId: "",
        code: "",
        description: ""
    });

    useEffect(() => {
        fetchEncounters(token)
            .then(setEncounters)
            .catch(e => setError(e.message));
    }, [token]);

    return (
        <div>
            <h2>Encounters</h2>
            {error && <p style={{ color: "red" }}>{error}</p>}

            <h3>Create Encounter</h3>
            <form onSubmit={e => {
                e.preventDefault();
                createEncounter(token, {
                    patientId: Number(newEncounter.patientId),
                    practitionerId: Number(newEncounter.practitionerId),
                    notes: newEncounter.notes
                })
                    .then(() => fetchEncounters(token).then(setEncounters))
                    .catch(err => setError(err.message));
            }}>
                <input
                    placeholder="Patient ID"
                    value={newEncounter.patientId}
                    onChange={e => setNewEncounter({ ...newEncounter, patientId: e.target.value })}
                />
                <input
                    placeholder="Practitioner ID"
                    value={newEncounter.practitionerId}
                    onChange={e =>
                        setNewEncounter({ ...newEncounter, practitionerId: e.target.value })
                    }
                />
                <input
                    placeholder="Notes"
                    value={newEncounter.notes}
                    onChange={e =>
                        setNewEncounter({ ...newEncounter, notes: e.target.value })
                    }
                />
                <button>Create</button>
            </form>

            <h3>Create Condition</h3>
            <form onSubmit={e => {
                e.preventDefault();
                createCondition(token, {
                    patientId: Number(newCondition.patientId),
                    encounterId: Number(newCondition.encounterId),
                    code: newCondition.code,
                    description: newCondition.description
                }).catch(err => setError(err.message));
            }}>
                <input
                    placeholder="Patient ID"
                    value={newCondition.patientId}
                    onChange={e => setNewCondition({ ...newCondition, patientId: e.target.value })}
                />
                <input
                    placeholder="Encounter ID"
                    value={newCondition.encounterId}
                    onChange={e => setNewCondition({ ...newCondition, encounterId: e.target.value })}
                />
                <input
                    placeholder="Code"
                    value={newCondition.code}
                    onChange={e => setNewCondition({ ...newCondition, code: e.target.value })}
                />
                <input
                    placeholder="Description"
                    value={newCondition.description}
                    onChange={e => setNewCondition({ ...newCondition, description: e.target.value })}
                />
                <button>Create</button>
            </form>

            <h3>All Encounters</h3>
            <ul>
                {encounters.map(e => (
                    <li key={e.id}>
                        #{e.id} — P:{e.patientId} — Dr:{e.practitionerId} — {e.notes}
                    </li>
                ))}
            </ul>
        </div>
    );
}
