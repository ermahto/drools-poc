# Spring Drools Kafka Validation POC

Kotlin / Spring Boot service that consumes an order message from Kafka, validates it with Drools, and publishes the result to a validated or rejected topic.

## Prerequisites

- JDK 8
- Maven 3.6+
- Docker Desktop (for the local Kafka POC)
- Optionally, an internal Kafka cluster instead of Docker

## How to run Kafka

### Local Kafka (Docker Compose)

From the project root:

```bash
docker compose up -d
```

This starts a single-node Kafka broker on `localhost:9092`. Topics are auto-created on first use:

- `order-inbound` — input
- `order-validated` — passed Drools validation
- `order-rejected` — failed Drools validation

Stop Kafka with:

```bash
docker compose down
```

### Internal Kafka

Skip Docker and point the app at the internal bootstrap servers:

```bash
set KAFKA_BOOTSTRAP_SERVERS=your-kafka-host:9092
```

On Linux/macOS:

```bash
export KAFKA_BOOTSTRAP_SERVERS=your-kafka-host:9092
```

Create the three topics above if auto-create is disabled.

## How to start the application

With local Kafka already running:

```bash
mvn spring-boot:run
```

The app listens for JSON messages on `order-inbound` (group `order-validation-service`).

## Publish with Postman and inspect Kafka output

1. Start Kafka: `docker compose up -d`
2. Wait about 15 seconds, then start the app: `mvn spring-boot:run`
3. Open **two extra terminals** and watch the result topics:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic order-validated --from-beginning
```

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic order-rejected --from-beginning
```

4. In Postman: **POST** `http://localhost:8080/orders` with header `Content-Type: application/json`.
5. Send a valid body to see output in the `order-validated` terminal, or an invalid body to see it in `order-rejected`.

Postman only publishes to `order-inbound`. The Drools listener writes the validated/rejected message to Kafka, which is what the Docker consumers show.

## Sample messages

### Valid order → `order-validated`

```json
{
  "orderId": "ORD-1001",
  "customerName": "Alex Kumar",
  "country": "US",
  "orderType": "STANDARD",
  "amount": 5000
}
```

Produce it:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic order-inbound
```

Paste the JSON, then press Enter. Watch the validated topic:

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic order-validated --from-beginning
```

Expected status:

```json
{
  "orderId": "ORD-1001",
  "customerName": "Alex Kumar",
  "country": "US",
  "orderType": "STANDARD",
  "amount": 5000.0,
  "valid": true,
  "validationStatus": "VALIDATED",
  "rejectionReasons": []
}
```

### Invalid order → `order-rejected`

```json
{
  "orderId": "ORD-BAD",
  "customerName": "Alex Kumar",
  "country": "FR",
  "orderType": "STANDARD",
  "amount": 15000
}
```

```bash
docker compose exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic order-rejected --from-beginning
```

This payload fails two rules: `FR` is not an allowed country, and STANDARD orders cannot exceed `10000`.

Ready-made files also live in `src/main/resources/samples/`.

### Current validation rules

| Rule | Requirement |
| --- | --- |
| Mandatory fields | `orderId`, `customerName`, `country`, `orderType`, and `amount` must be present |
| Country | Allowed: `US`, `IN`, `GB`, `DE`, `SG` |
| Amount / type | Types: `STANDARD`, `EXPRESS`, `BULK`. Amount must be `> 0`. Limits: STANDARD ≤ 10000, EXPRESS ≤ 50000, BULK ≤ 100000 |

## How to add or modify DRL rules

1. Edit `src/main/resources/rules/order-validation.drl`.
2. Keep the import pointing at `com.javatechie.spring.drools.api.model.OrderMessage`.
3. Call `$order.reject("reason")` to fail a message. All matching rules run, so multiple reasons can be collected.
4. Restart the application. Drools compiles the DRL at startup.

Example extra rule:

```
rule "SG orders must be EXPRESS or BULK"
dialect "java"
when
    $order : OrderMessage(country == "SG", orderType == "STANDARD")
then
    $order.reject("STANDARD orders are not allowed for country SG");
end
```

The DRL path can be changed with `drools.rule-file` in `src/main/resources/application.yml`.

## How to execute tests

```bash
mvn test
```

The suite covers:

- Valid STANDARD / EXPRESS / BULK payloads
- Missing mandatory fields
- Unsupported country
- Amount `<= 0` and type-specific amount limits
- Unknown order type
- Multiple violations on one message
- Kafka routing to `order-validated` vs `order-rejected`
- Malformed JSON on the inbound topic

Context-load tests use the `test` profile so the Kafka listener does not start.
