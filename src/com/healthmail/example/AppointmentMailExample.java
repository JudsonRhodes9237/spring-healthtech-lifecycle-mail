package com.healthmail.example;

import java.time.LocalDateTime;

public final class AppointmentMailExample {
    public static void main(String[] args) throws Exception {
        String recipient = System.getenv("DEMO_EMAIL_TO");
        if (recipient == null || recipient.isBlank()) throw new IllegalArgumentException("DEMO_EMAIL_TO is required");
        AppointmentMailService service = new AppointmentMailService(new InfraiEmailClient(new InfraiProperties(System.getenv("INFRAI_API_KEY"))));
        String messageId = service.sendLifecycleMail(new AppointmentMailService.Appointment(recipient, "Dr. Rivera", LocalDateTime.now().plusDays(1), false));
        System.out.println("sent appointment email, message_id=" + messageId);
    }
}
