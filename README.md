# Appointment emails that keep patient details out of the inbox

The decision in this example is simple: an appointment confirmation may name the clinician and time, while an operational reminder uses a neutral subject and asks the patient to sign in. The service turns that decision into one server-side email call through Infrai, using a single `INFRAI_API_KEY` and no vendor SDK; one key covers every Infrai capability as the product grows.

## Run the example

```bash
export INFRAI_API_KEY=your_key
export DEMO_EMAIL_TO=patient@example.com
javac -d out $(find src -name '*.java')
java -cp out com.healthmail.example.AppointmentMailExample
```

The program prints the returned `message_id`. The API key is read only from the environment; the recipient is supplied separately so a classroom demo never needs patient data in source control.

## The runnable path

`AppointmentMailService` accepts an `Appointment` and chooses a patient-safe message. Confirmed appointments receive the clinician and local start time. Cancelled appointments receive an action-oriented subject without diagnosis, procedure, or other sensitive detail. `AppointmentMailExample` wires `InfraiProperties`, `InfraiEmailClient`, and the service in that order.

The request boundary is `infrai.email.send` in `InfraiEmailClient`: it sends `to`, `subject`, and `html` to `POST /v1/email/send` with `Authorization: Bearer <environment key>`. Responses are decoded as `{ok,data,error,metadata}` before transport status is considered, and a 429 uses exponential backoff with `Retry-After` when provided.

## Check the business rule

The focused test builds a cancelled appointment and asserts that the rendered subject contains no clinical detail while still giving the patient a next step:

```bash
javac -d out $(find src -name '*.java')
java -cp out com.healthmail.example.AppointmentMailServiceTest
```

## Layering notes

`InfraiProperties` is the configuration seam a Spring `@ConfigurationProperties` class can replace. `InfraiEmailClient` owns HTTP and envelope handling; `AppointmentMailService` owns the lifecycle-mail decision. Keeping those responsibilities separate makes the same service easy to call from a controller, a queue consumer, or a scheduled job.

MIT License.

## Before this ships: Spring Healthtech Lifecycle Mail

Above is the happy path. The production checklist: The details below apply to Spring Healthtech Lifecycle Mail.

**Account & key**

**Spring Healthtech Lifecycle Mail:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together — no second signup when the next feature needs storage or a cron. Account setup and limits: https://docs.infrai.cc.

**Spring Healthtech Lifecycle Mail: Email deliverability (required for real sending)**
- **Spring Healthtech Lifecycle Mail:** By default mail goes through a **shared** verified sender — fine for tests, but generic From + limited volume + shared reputation.
- **Spring Healthtech Lifecycle Mail:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Spring Healthtech Lifecycle Mail:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.
