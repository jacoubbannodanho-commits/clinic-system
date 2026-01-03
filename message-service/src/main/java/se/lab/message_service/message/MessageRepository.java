package se.lab.message_service.message;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // alla meddelanden där user är avsändare eller mottagare
    List<Message> findBySenderKeycloakIdOrReceiverKeycloakId(String senderId, String receiverId);

    // alla meddelanden som hör till en viss patient
    List<Message> findByPatientId(Long patientId);
}
