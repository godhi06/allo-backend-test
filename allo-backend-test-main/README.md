# IDR Rate Aggregator — Allo Bank Backend Developer Take-Home Test

A production-ready Spring Boot REST API that aggregates Indonesian Rupiah (IDR) exchange rate data from the [Frankfurter Exchange Rate API](https://api.frankfurter.app/), applying a personalized banking spread calculation.

---

## 📋 Table of Contents

- [Setup & Run Instructions](#-setup--run-instructions)
- [Endpoint Usage & cURL Examples](#-endpoint-usage--curl-examples)
- [Personalization Note (Spread Factor)](#-personalization-note)
- [Project Structure](#-project-structure)
- [Architectural Rationale](#️-architectural-rationale)
- [Testing](#-testing)

---

## 🚀 Setup & Run Instructions

### Prerequisites

- **Java 17** or later
- **Maven 3.8+** (or use the included Maven wrapper if available)
- Internet connection (to fetch data from Frankfurter API on startup)

### Clone & Build

```bash
git clone https://github.com/godhi06/allo-backend-test.git
cd allo-backend-test-main
```

### Run Tests

```bash
mvn clean test
```

### Build & Run the Application

```bash
mvn clean package -DskipTests
java -jar target/idr-rate-aggregator-0.0.1-SNAPSHOT.jar
```

Or run directly with Maven:

```bash
mvn spring-boot:run
```

The application will start on **http://localhost:8080** and automatically fetch all three data resources from the Frankfurter API during startup.

---

## 🌐 Endpoint Usage & cURL Examples

The application exposes a single polymorphic endpoint:

```
GET /api/finance/data/{resourceType}
```

Where `{resourceType}` is one of: `latest_idr_rates`, `historical_idr_usd`, or `supported_currencies`.

### 1. Latest IDR Rates (with USD_BuySpread_IDR)

```bash
curl -s http://localhost:8080/api/finance/data/latest_idr_rates | jq .
```

**Sample Response:**
```json
{
  "resourceType": "latest_idr_rates",
  "success": true,
  "data": {
    "base": "IDR",
    "date": "2024-12-20",
    "rates": {
      "USD": 0.0000625,
      "EUR": 0.0000590,
      "GBP": 0.0000498
    },
    "USD_BuySpread_IDR": 16100.00,
        "spread_factor": 0.00625,
        "github_username": "godhi06"
  }
}
```

### 2. Historical IDR/USD Data

```bash
curl -s http://localhost:8080/api/finance/data/historical_idr_usd | jq .
```

**Sample Response:**
```json
{
  "resourceType": "historical_idr_usd",
  "success": true,
  "data": {
    "base": "IDR",
    "target": "USD",
    "start_date": "2024-01-01",
    "end_date": "2024-01-05",
    "rates": {
      "2024-01-02": { "USD": 0.0000645 },
      "2024-01-03": { "USD": 0.0000643 },
      "2024-01-04": { "USD": 0.0000641 },
      "2024-01-05": { "USD": 0.0000640 }
    }
  }
}
```

### 3. Supported Currencies

```bash
curl -s http://localhost:8080/api/finance/data/supported_currencies | jq .
```

**Sample Response:**
```json
{
  "resourceType": "supported_currencies",
  "success": true,
  "data": {
    "AUD": "Australian Dollar",
    "IDR": "Indonesian Rupiah",
    "USD": "United States Dollar",
    "EUR": "Euro"
  }
}
```

### 4. Invalid Resource Type (Error Handling)

```bash
curl -s http://localhost:8080/api/finance/data/invalid_type | jq .
```

**Response (404):**
```json
{
  "resourceType": "unknown",
  "success": false,
  "error": "Unsupported resource type: 'invalid_type'. Valid types are: [latest_idr_rates, historical_idr_usd, supported_currencies]"
}
```

---

## 🔑 Personalization Note

| Item | Value |
|---|---|
| **GitHub Username** | `godhi06` |
| **Characters (lowercase)** | g, o, d, h, i, 0, 6 |
| **Unicode Values** | g=103, o=111, d=100, h=104, i=105, 0=48, 6=54 |
| **Sum of Unicode Values** | **625** |
| **Spread Factor** | `(625 % 1000) / 100000.0` = **0.00625** |

**Formula applied:**
```
USD_BuySpread_IDR = (1 / Rate_USD) * (1 + 0.00625)
```

For example, if `Rate_USD = 0.0000625`:
```
USD_BuySpread_IDR = (1 / 0.0000625) * (1 + 0.00625) = 16000 * 1.00625 = 16,100.00 IDR
```

---

## 📁 Project Structure

```
src/main/java/com/allobank/idrrate/
├── IdrRateAggregatorApplication.java     # Spring Boot entry point
├── config/
│   ├── SpreadConfig.java                 # Spread factor calculation from GitHub username
│   └── WebClientFactoryBean.java         # FactoryBean<WebClient> with externalized config
├── controller/
│   └── FinanceController.java            # Single REST endpoint (no if/else)
├── dto/
│   ├── ApiResponse.java                  # Unified API response wrapper
│   ├── FrankfurterLatestResponse.java    # External API DTO for /latest
│   ├── FrankfurterHistoricalResponse.java # External API DTO for time-series
│   ├── LatestIdrRatesData.java           # Transformed response with spread
│   └── HistoricalIdrUsdData.java         # Transformed historical data
├── exception/
│   ├── GlobalExceptionHandler.java       # @RestControllerAdvice for graceful errors
│   ├── DataNotLoadedException.java       # Store not sealed yet
│   ├── ExternalApiException.java         # Frankfurter API failures
│   └── ResourceNotFoundException.java    # Invalid resource type
├── runner/
│   └── DataLoaderRunner.java             # ApplicationRunner — fetches data on startup
├── service/
│   └── FinanceDataService.java           # Service layer serving from in-memory store
├── store/
│   └── InMemoryDataStore.java            # Thread-safe, immutable ConcurrentHashMap store
└── strategy/
    ├── IDRDataFetcher.java               # Strategy interface
    ├── LatestIdrRatesFetcher.java         # Strategy: /latest?base=IDR + spread
    ├── HistoricalIdrUsdFetcher.java       # Strategy: /2024-01-01..2024-01-05
    └── SupportedCurrenciesFetcher.java    # Strategy: /currencies
```

---

## 🛠️ Architectural Rationale

### 1. Polymorphism Justification — Why the Strategy Pattern?

The **Strategy Pattern** was chosen over a simpler `if/else` or `switch` block in the service layer for the following reasons:

- **Open/Closed Principle (OCP):** Adding a new resource type (e.g., `historical_eur_usd`) requires only creating a new class implementing `IDRDataFetcher` and annotating it with `@Component`. No existing code needs to be modified — the new strategy is automatically discovered by Spring's dependency injection and included in the `List<IDRDataFetcher>` injected into `DataLoaderRunner`.

- **Single Responsibility Principle (SRP):** Each strategy class encapsulates only the logic for its specific resource — API call, error handling, and data transformation. The `LatestIdrRatesFetcher` handles the spread calculation, while `SupportedCurrenciesFetcher` handles a simple map response. Each class has one reason to change.

- **Testability:** Each strategy can be unit tested in complete isolation using `MockWebServer`, without needing to mock the entire service layer. This results in focused, fast, and reliable tests.

- **Maintainability:** When the spread formula changes, only `LatestIdrRatesFetcher` is modified. When the historical date range changes, only `HistoricalIdrUsdFetcher` is touched. There is zero risk of accidentally breaking unrelated resource logic.

- **No Conditional Logic in Controller/Service:** The controller and service layer contain zero `if/else` or `switch` statements for resource type dispatch. Spring injects all strategies as a `List`, and the `DataLoaderRunner` iterates them. The service simply looks up pre-loaded data by key from the `InMemoryDataStore`.

### 2. Client Factory — Why `FactoryBean<WebClient>`?

The `WebClientFactoryBean` implements Spring's `FactoryBean<T>` interface to construct the `WebClient` instance:

- **Encapsulated Construction Logic:** The `FactoryBean` centralizes all WebClient configuration — base URL, connect timeout, read timeout, default headers — in a single, cohesive component. The `getObject()` method acts as a factory method with full control over the instantiation lifecycle.

- **Externalized Configuration:** All parameters (`frankfurter.api.base-url`, `connect-timeout-ms`, `read-timeout-ms`) are injected via `@Value` from `application.yml`, making the client fully configurable without code changes.

- **Lifecycle Control:** Unlike a simple `@Bean` method, `FactoryBean` provides the `isSingleton()` contract, giving explicit control over whether the bean is a singleton or prototype. The `getObjectType()` method aids Spring's type resolution for autowiring.

- **Separation of Concerns:** The WebClient construction is decoupled from any `@Configuration` class. The `FactoryBean` is a self-contained `@Component` that Spring discovers and uses to transparently inject `WebClient` wherever it is autowired — consumers are unaware they're using a factory-produced bean.

- **Production Readiness:** In real-world scenarios, the factory can be extended with retry policies, circuit breaker configuration, logging interceptors, or custom codecs without modifying consumers.

### 3. Startup Runner — Why `ApplicationRunner` over `@PostConstruct`?

`ApplicationRunner` was chosen over `@PostConstruct` for the initial data ingestion for these critical reasons:

- **Full Context Guarantee:** `ApplicationRunner.run()` executes **after** the entire Spring application context is fully initialized, including all `FactoryBean`-produced beans (like our `WebClient`), all `@Component` beans, and all post-processing. `@PostConstruct` runs during bean initialization, which means dependent beans (especially `FactoryBean`-created ones) may not yet be available, leading to potential `NullPointerException` or initialization ordering issues.

- **Explicit Lifecycle Phase:** `ApplicationRunner` represents a well-defined lifecycle phase: "the application is ready, now perform startup tasks." This is semantically clearer than `@PostConstruct`, which is a per-bean lifecycle callback not designed for application-level orchestration.

- **Access to `ApplicationArguments`:** `ApplicationRunner` provides access to command-line arguments, which could be useful for overriding behavior at runtime (e.g., `--skip-data-load` for local development).

- **Error Propagation:** If data loading fails in `ApplicationRunner`, it propagates to `SpringApplication.run()`, preventing the application from starting in a broken state. With `@PostConstruct`, failure handling is less predictable and may result in a partially initialized application that silently serves errors.

- **Ordering:** Multiple `ApplicationRunner` beans can be ordered with `@Order`, allowing fine-grained control over startup task sequencing if additional runners are added later.

---

## 🧪 Testing

### Test Coverage

| Test Class | Type | Description |
|---|---|---|
| `LatestIdrRatesFetcherTest` | Unit | Tests spread calculation, API response parsing, error handling |
| `HistoricalIdrUsdFetcherTest` | Unit | Tests historical data parsing, error handling |
| `SupportedCurrenciesFetcherTest` | Unit | Tests currencies map parsing, error handling |
| `SpreadConfigTest` | Unit | Tests spread factor calculation for username "ardhi" |
| `InMemoryDataStoreTest` | Unit | Tests thread-safety, immutability, seal behavior |
| `FinanceDataServiceTest` | Unit | Tests service layer with valid/invalid resource types |
| `FinanceControllerTest` | Unit | Tests REST endpoint with MockMvc, error responses |
| `DataLoaderRunnerTest` | Unit | Tests runner loads all fetchers and seals store |
| `IdrRateAggregatorIntegrationTest` | Integration | Tests full application startup, data loading, endpoint responses |

### Run All Tests

```bash
mvn clean test
```

### Run Only Unit Tests

```bash
mvn test -Dtest="!IdrRateAggregatorIntegrationTest"
```

### Run Only Integration Tests

```bash
mvn test -Dtest="IdrRateAggregatorIntegrationTest"
```

All unit tests use `MockWebServer` (OkHttp) to mock external API calls, ensuring fast and reliable execution without network dependency.
