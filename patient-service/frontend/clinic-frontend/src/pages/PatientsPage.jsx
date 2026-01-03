import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { fetchPatients, createPatient } from "../api/patientsApi";

export default function PatientsPage() {
    const [patients, setPatients] = useState([]);
    const [error, setError] = useState("");

    const [newPatient, setNewPatient] = useState({
        firstName: "",
        lastName: "",
        ssn: ""
    });

    useEffect(() => {
        fetchPatients()
            .then(setPatients)
            .catch(e => setError(e.message));
    }, []);

    function handleCreatePatient(e) {
        e.preventDefault();

        createPatient(newPatient)
            .then(() => fetchPatients().then(setPatients))
            .then(() =>
                setNewPatient({ firstName: "", lastName: "", ssn: "" })
            )
            .catch(e => setError(e.message));
    }

    return (
        <div>
            <h2>Patients</h2>

            {error && <p style={{ color: "red" }}>{error}</p>}

            <h3>Create new patient</h3>
            <form onSubmit={handleCreatePatient}>
                <input
                    placeholder="First name"
                    value={newPatient.firstName}
                    onChange={e =>
                        setNewPatient({ ...newPatient, firstName: e.target.value })
                    }
                    required
                />
                <input
                    placeholder="Last name"
                    value={newPatient.lastName}
                    onChange={e =>
                        setNewPatient({ ...newPatient, lastName: e.target.value })
                    }
                    required
                />
                <input
                    placeholder="SSN"
                    value={newPatient.ssn}
                    onChange={e =>
                        setNewPatient({ ...newPatient, ssn: e.target.value })
                    }
                    required
                />
                <button>Create patient</button>
            </form>

            <ul>
                {patients.map(p => (
                    <li key={p.id}>
                        <Link to={`/patients/${p.id}`}>
                            {p.firstName} {p.lastName}
                        </Link>
                    </li>
                ))}
            </ul>
        </div>
    );
}
