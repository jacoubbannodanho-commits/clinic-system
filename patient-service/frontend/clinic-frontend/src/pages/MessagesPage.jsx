import { useEffect, useState } from "react";
import { fetchInbox, sendMessage, markMessageRead } from "../api/messagesApi";

export default function MessagesPage({ keycloak }) {
    const roles = keycloak.tokenParsed?.realm_access?.roles || [];
    const isPatient = roles.includes("patient");

    const [messages, setMessages] = useState([]);
    const [error, setError] = useState("");
    const [replyContent, setReplyContent] = useState("");
    const [replyTo, setReplyTo] = useState(null);

    useEffect(() => {
        loadInbox();
    }, []);

    function loadInbox() {
        fetchInbox()
            .then(setMessages)
            .catch(e => setError(e.message));
    }

    function handleReply(e) {
        e.preventDefault();
        if (!replyTo) return;

        sendMessage({
            patientId: replyTo.patientId,
            receiverKeycloakId: replyTo.senderKeycloakId,
            content: replyContent
        })
            .then(() => {
                setReplyContent("");
                setReplyTo(null);
                loadInbox();
            })
            .catch(e => setError(e.message));
    }

    function handleMarkRead(id) {
        markMessageRead(id)
            .then(loadInbox)
            .catch(e => setError(e.message));
    }

    return (
        <div>
            <h2>Messages</h2>

            {error && <p style={{ color: "red" }}>{error}</p>}

            <ul>
                {messages.map(m => (
                    <li key={m.id} style={{ marginBottom: 12 }}>
                        <div>
                            <b>{m.senderRole}</b>: {m.content}
                        </div>

                        <div style={{ marginTop: 5 }}>
                            {!m.read && (
                                <button onClick={() => handleMarkRead(m.id)}>
                                    Mark as read
                                </button>
                            )}

                            <button
                                style={{ marginLeft: 10 }}
                                onClick={() => setReplyTo(m)}
                            >
                                Reply
                            </button>
                        </div>
                    </li>
                ))}
            </ul>

            {replyTo && (
                <>
                    <hr />
                    <h3>
                        Reply to {replyTo.senderRole}
                    </h3>

                    <form onSubmit={handleReply}>
                        <input
                            placeholder="Message"
                            value={replyContent}
                            onChange={e => setReplyContent(e.target.value)}
                            required
                        />
                        <button>Send reply</button>
                    </form>
                </>
            )}
        </div>
    );
}
