package se.lab.message_service.message;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Message {

    // ✅ REQUIRED by JPA
    protected Message() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // vilket patient-id meddelandet hör till
    private Long patientId;

    // keycloak-sub för avsändare/mottagare
    private String senderKeycloakId;
    private String receiverKeycloakId;

    // t.ex. "patient", "doctor", "staff"
    private String senderRole;

    @Column(nullable = false, length = 2000)
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    /* ========= GETTERS ========= */

    public Long getId() {
        return id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public String getSenderKeycloakId() {
        return senderKeycloakId;
    }

    public String getReceiverKeycloakId() {
        return receiverKeycloakId;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    /* ========= SETTERS ========= */

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public void setSenderKeycloakId(String senderKeycloakId) {
        this.senderKeycloakId = senderKeycloakId;
    }

    public void setReceiverKeycloakId(String receiverKeycloakId) {
        this.receiverKeycloakId = receiverKeycloakId;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }
}
