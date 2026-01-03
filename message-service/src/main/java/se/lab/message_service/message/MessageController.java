package se.lab.message_service.message;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService service;

    public MessageController(MessageService service) {
        this.service = service;
    }

    // patient/läkare/personal ska kunna se sina egna meddelanden
    @GetMapping("/my-inbox")
    @PreAuthorize("hasAnyRole('patient','doctor','staff')")
    public List<Message> myInbox(@AuthenticationPrincipal Jwt jwt) {
        String sub = jwt.getSubject();
        return service.inboxForUser(sub);
    }

    // läkare + övrig personal ska kunna se meddelanden per patient
    @GetMapping("/by-patient/{patientId}")
    @PreAuthorize("hasAnyRole('doctor','staff')")
    public List<Message> byPatient(@PathVariable Long patientId) {
        return service.byPatient(patientId);
    }

    // alla tre roller ska kunna skicka meddelanden
    @PostMapping
    @PreAuthorize("hasAnyRole('patient','doctor','staff')")
    public Message send(@AuthenticationPrincipal Jwt jwt,
                        @RequestBody CreateMessageRequest req) {

        String sub = jwt.getSubject();
        String role = extractMainRole(jwt); // patient/doctor/staff

        Message m = new Message();
        m.setPatientId(req.getPatientId());
        m.setReceiverKeycloakId(req.getReceiverKeycloakId());
        m.setSenderKeycloakId(sub);
        m.setSenderRole(role);
        m.setContent(req.getContent());

        return service.sendMessage(m);
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('patient','doctor','staff')")
    public Message markRead(@PathVariable Long id) {
        return service.markAsRead(id);
    }

    @SuppressWarnings("unchecked")
    private String extractMainRole(Jwt jwt) {
        var realmAccess = (java.util.Map<String, Object>) jwt.getClaim("realm_access");
        if (realmAccess == null) return "unknown";

        var roles = (java.util.List<String>) realmAccess.get("roles");
        if (roles == null || roles.isEmpty()) return "unknown";

        if (roles.contains("doctor")) return "doctor";
        if (roles.contains("staff")) return "staff";
        if (roles.contains("patient")) return "patient";

        return roles.get(0);
    }
}
