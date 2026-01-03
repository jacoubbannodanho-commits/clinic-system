import { useEffect, useState } from "react";
import {
    uploadImage,
    fetchPatientImage,
    annotatePatientImage
} from "../api/imagesApi";

export default function ImagePage({ patientId, roles = [] }) {
    const [imageUrl, setImageUrl] = useState(null);
    const [file, setFile] = useState(null);

    // ⬅️ SKYDD: roles är alltid en array
    const isDoctor = Array.isArray(roles) && roles.includes("doctor");

    useEffect(() => {
        if (!patientId) return;

        fetchPatientImage(patientId)
            .then(setImageUrl)
            .catch(() => setImageUrl(null));
    }, [patientId]);

    async function handleUpload() {
        if (!file) return;

        await uploadImage(patientId, file);
        const img = await fetchPatientImage(patientId);
        setImageUrl(img);
    }

    async function handleAnnotate() {
        await annotatePatientImage(patientId, {
            text: "Doctor note",
            x: 40,
            y: 40,
            color: "red"
        });

        const img = await fetchPatientImage(patientId);
        setImageUrl(img);
    }

    return (
        <div>
            <h3>Patient image</h3>

            {imageUrl ? (
                <img
                    src={imageUrl}
                    alt="patient"
                    style={{ maxWidth: 400, border: "1px solid #ccc" }}
                />
            ) : (
                <p>No image uploaded</p>
            )}

            {isDoctor && (
                <div>
                    <input
                        type="file"
                        onChange={e => setFile(e.target.files[0])}
                    />
                    <button onClick={handleUpload}>Upload image</button>
                    <button onClick={handleAnnotate}>Annotate image</button>
                </div>
            )}
        </div>
    );
}
