package se.lab.message_service.message;

public class CreateMessageRequest {

    private Long patientId;
    private String receiverKeycloakId;
    private String content;

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getReceiverKeycloakId() { return receiverKeycloakId; }
    public void setReceiverKeycloakId(String receiverKeycloakId) { this.receiverKeycloakId = receiverKeycloakId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
