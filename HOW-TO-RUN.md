# How to run and test (local Kafka, no Docker)

Use this when Kafka is already installed on the machine.

```
Postman POST /orders
        → order-inbound
        → Drools rules
        → order-validated   (pass)
        → order-rejected    (deviation)
```

## Prerequisites

- JDK 21 (not JDK 8)
- Maven 3.6.3+
- Kafka running (default `localhost:9092`)
- Postman (optional)

If Kafka is on another host:

```powershell
set KAFKA_BOOTSTRAP_SERVERS=your-kafka-host:9092
```

Linux/macOS:

```bash
export KAFKA_BOOTSTRAP_SERVERS=your-kafka-host:9092
```

Kafka CLI scripts:

- Windows: `KAFKA_HOME\bin\windows\` (`kafka-topics.bat`, `kafka-console-consumer.bat`, ...)
- Linux/macOS: `KAFKA_HOME\bin\` (`kafka-topics.sh`, ...)

If those commands are on `PATH`, run them from any folder.

## 1. Confirm Kafka is up

Windows:

```powershell
kafka-topics.bat --bootstrap-server localhost:9092 --list
```

Linux/macOS:

```bash
kafka-topics.sh --bootstrap-server localhost:9092 --list
```

You should get a topic list (it can be empty). If this fails, start Kafka the way you normally do on that machine, then retry.

## 2. Create topics

```powershell
kafka-topics.bat --bootstrap-server localhost:9092 --create --if-not-exists --topic order-inbound --partitions 1 --replication-factor 1
kafka-topics.bat --bootstrap-server localhost:9092 --create --if-not-exists --topic order-validated --partitions 1 --replication-factor 1
kafka-topics.bat --bootstrap-server localhost:9092 --create --if-not-exists --topic order-rejected --partitions 1 --replication-factor 1
```

Linux/macOS: use `kafka-topics.sh` with the same arguments.

Topics:

- `order-inbound` — input
- `order-validated` — passed Drools validation
- `order-rejected` — failed Drools validation (deviation flow)

## 3. Watch result topics

Keep these two terminals open.

**Validated:**

```powershell
kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic order-validated --from-beginning
```

**Rejected:**

```powershell
kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic order-rejected --from-beginning
```

Linux/macOS: use `kafka-console-consumer.sh`.

## 4. Start the application

From the project root:

```powershell
mvn spring-boot:run
```

Wait for `Started SpringDroolsApplication`.

The app listens on `order-inbound` (group `order-validation-service`) and exposes **POST** `http://localhost:8080/orders`.

## 5. Test with Postman (valid)

- Method: **POST**
- URL: `http://localhost:8080/orders`
- Header: `Content-Type` = `application/json`
- Body → raw → JSON:

```json
{
  "orderId": "ORD-1001",
  "customerName": "Alex Kumar",
  "country": "US",
  "orderType": "STANDARD",
  "amount": 5000
}
```

Postman should return **202**. The `order-validated` consumer should print:

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

## 6. Test with Postman (rejected / deviation)

Same URL, body:

```json
{
  "orderId": "ORD-BAD",
  "customerName": "Alex Kumar",
  "country": "FR",
  "orderType": "STANDARD",
  "amount": 15000
}
```

The `order-rejected` consumer should print `"validationStatus":"REJECTED"` with reasons (`FR` is not allowed, STANDARD cannot exceed 10000).

Sample files also live in `src/main/resources/samples/`.

## 7. Optional: produce without Postman

```powershell
kafka-console-producer.bat --bootstrap-server localhost:9092 --topic order-inbound
```

Paste one JSON line and press Enter. Linux/macOS: use `kafka-console-producer.sh`.

## 8. Unit tests (no Kafka needed)

```powershell
mvn test
```

If the app fails with “Broker may not be available”, Kafka is not listening on `9092`. Start the broker or set `KAFKA_BOOTSTRAP_SERVERS`, then restart the app.
