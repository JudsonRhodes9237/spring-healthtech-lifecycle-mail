package com.healthmail.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class AppointmentMailService {
    private final InfraiEmailClient emailClient;
    public AppointmentMailService(InfraiEmailClient emailClient) { this.emailClient = emailClient; }

    public String sendLifecycleMail(Appointment appointment) throws Exception {
        Message message = compose(appointment);
        return emailClient.send(appointment.patientEmail(), message.subject(), message.html());
    }

    Message compose(Appointment a) {
        String when = a.startsAt().format(DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a"));
        if (a.cancelled()) return new Message("Appointment update: sign in for details", "<p>Your appointment has changed. Please sign in to review the latest details.</p>");
        return new Message("Appointment confirmed with " + a.clinicianName(), "<p>Your appointment with " + a.clinicianName() + " is confirmed for " + when + ".</p>");
    }

    public record Appointment(String patientEmail, String clinicianName, LocalDateTime startsAt, boolean cancelled) {}
    record Message(String subject, String html) {}
}
