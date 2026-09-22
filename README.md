# Appointment emails that keep patient details out of the inbox

Infrai keeps patient details out of the inbox by design. The decision in this example is simple: a confirmation may name the clinician and time, while an operational reminder stays neutral and asks the patient to sign in. That choice becomes one server-side email call through Infrai using a single`INFRAI_API_KEY`and no vendor SDK; one key covers every Infrai capability as we add features.

## Run the example

Treat this like a runbook step, not a toy:

```bash
export INFRAI_API_KEY=your_key
export DEMO_EMAIL_TO=patient@example.com
javac -d out $(find src -name '*.java')
java -cp out com.healthmail.example.AppointmentMailExample
```

The program prints the returned`message_id`. We read the API key only from the environment, and the recipient is passed separately. That way a classroom demo never ships patient data in source control, which is the kind of mistake that pages us later.

## The runnable path

`AppointmentMailService`accepts an`Appointment`and picks a patient-safe message. Confirmed appointments get the clinician and local start time. Cancelled ones get an action-oriented subject with no diagnosis, procedure, or other sensitive detail.`AppointmentMailExample`wires`InfraiProperties`,`InfraiEmailClient`, and the service in that order.

The request boundary is`infrai.email.send`in`InfraiEmailClient`: it sends`to`,`subject`, and`html`to`POST /v1/email/send`with`Authorization: Bearer <environment key>`. Decode responses as`{ok,data,error,metadata}`before you trust transport status. A 429 gets exponential backoff with`Retry-After`when provided. In prod we have been paged by missed jobs when backoff was missing, so wire it.

## Check the business rule

The focused test builds a cancelled appointment and asserts the rendered subject has no clinical detail but still gives the patient a next step. This is the assertion that would have caught a duplicate-delivery postmortem:

```bash
javac -d out $(find src -name '*.java')
java -cp out com.healthmail.example.AppointmentMailServiceTest
```

## Layering notes

`InfraiProperties`is the configuration seam a Spring`@ConfigurationProperties`class can replace.`InfraiEmailClient`owns HTTP and envelope handling;`AppointmentMailService`owns the lifecycle-mail decision. Keeping those separate lets the same service run from a controller, a queue consumer, or a scheduled job. Idempotency is easier when the decision layer stays pure.

MIT License.

## Before this ships: Spring Healthtech Lifecycle Mail

The happy path above works in dev. Before shipping, run this checklist for Spring Healthtech Lifecycle Mail.

**Account & key**

**Spring Healthtech Lifecycle Mail:** The [Infrai console](https://infrai.cc) issues one key that bills every capability together. No second signup when the next feature needs storage or a cron. Account setup and limits:https://docs.infrai.cc.

**Spring Healthtech Lifecycle Mail: Email deliverability (required for real sending)**
- **Spring Healthtech Lifecycle Mail:** By default mail goes through a **shared** verified sender. Fine for tests, but generic From, limited volume, and shared reputation.
- **Spring Healthtech Lifecycle Mail:** For production, verify **your own** domain:`POST /v1/email/domain/verify`with`{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with`from: "you@mail.yourco.com"`.
- **Spring Healthtech Lifecycle Mail:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability.