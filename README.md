## Token Exchange

## Setup

```1 ./gradlew bootBuildImage```

```2. Go to folder /infra/docker```

```3. Run docker-compose up -d ```

```4. Import to Postman extracted API from json file (in the root folder)  ```

```5. POST http://localhost:8080/api/v1/publish-event to publish event with market data on Kafka```

```6. POST http://localhost:8080/api/v1/token-exchange to exchange tokens```

```7. GET http://localhost:8080/api/v1/token-exchange to fetch available tokens```
