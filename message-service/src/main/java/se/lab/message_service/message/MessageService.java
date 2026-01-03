package se.lab.message_service.message;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository repo;

    public MessageService(MessageRepository repo) {
        this.repo = repo;
    }

    public Message sendMessage(Message m) {
        m.setCreatedAt(LocalDateTime.now());
        return repo.save(m);
    }

    public List<Message> inboxForUser(String keycloakSub) {
        return repo.findBySenderKeycloakIdOrReceiverKeycloakId(keycloakSub, keycloakSub);
    }

    public List<Message> byPatient(Long patientId) {
        return repo.findByPatientId(patientId);
    }

    public Message markAsRead(Long id) {
        Message m = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Message not found: " + id));
        m.setReadAt(LocalDateTime.now());
        return repo.save(m);
    }
}
