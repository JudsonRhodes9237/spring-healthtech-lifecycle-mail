package com.healthmail.example;

import java.time.LocalDateTime;

public final class AppointmentMailServiceTest {
    public static void main(String[] args) {
        AppointmentMailService service = new AppointmentMailService(null);
        var cancelled = new AppointmentMailService.Appointment("patient@example.com", "Dr. Rivera", LocalDateTime.of(2026, 9, 2, 10, 0), true);
        var message = service.compose(cancelled);
        if (!message.subject().contains("sign in") || message.subject().contains("Dr. Rivera")) throw new AssertionError(message.subject());
        System.out.println("patient-safe cancellation decision: PASS");
    }
}
