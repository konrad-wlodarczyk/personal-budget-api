# Personal Budget API

---

## About 

A REST API for managing personal budgets. The application allows users to track income and expense across
multiple accounts, with automatic balance updates for every transaction linked to specific account.

---
## Key Features

- **Account Management**: Create, list, delete accounts, as well as exporting transactions linked to account in CSV format (deletion is blocked if transactions are associated with the account for data integrity)
- **Transaction Handling**: Create, list, and delete transactions. Support for _INCOME_ and _EXPENSE_ types. Balances are recalculated automatically. 
     Includes filtering by date range ('from', 'to') and category.
- **Reporting**: Provides an endpoint for summary data (total income, total expenses, and spending grouped by category).

---

## Tech Stack

- **Java 21**, **Spring Boot 3.4.0**
- **PostgreSQL**
- **Spring Data JPA / Hibernate**
- **SpringDoc OpenAPI (Swagger)**
- **Docker & Docker Compose**
- **JaCoCo**
- **Postman**

---

## Cloning The Repository

```Bash
git clone https://github.com/konrad-wlodarczyk/personal-budget-api
cd personal-budget-api
```

---

## Getting Started

---

### Option 1: Docker (Recommended)

1. Copy the environment template:
    ```bash
    cp .env.example .env
    ```
   Or manually create file: _.env_ and copy contents of _.env.example_ into it.
2. Update the _.env_ file with your database credentials (username and password)
3. Start the infrastructure:
    ```bash
    docker-compose up --build
    ```
The API will be available at: [http://localhost:8080](http://localhost:8080) - as there is no UI, visiting root URL (/) will result in redirecting to the Swagger documentation page <br>
Swagger documentation: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) <br>

---

### Option 2: Local Setup

1. Ensure you have ***Java 21*** and a ***PostgreSQL*** instance running.
2. Configure your database connection in ***application.properties*** (or set environment variables ***DB_USERNAME*** and ***DB_PASSWORD*** in your system)
3. Run the application: 
    - Linux/macOs/Windows (terminal): 
      ```Bash
      ./mvnw spring-boot:run
      ```
    - Windows (PowerShell):
      ```Bash
      ./mvnw.cmd spring-boot:run
      ```
---

## Architecture & Design Choices

- **Consistency**: All operations are wrapped in ***@Transactional*** to ensure data integrity (one fail - all fails)
- **Error Handling**: Implementation of RFC 7807 (***ProblemDetail***) with standard HTTP error responses (400, 404, 409)
- **Validation**: Input data validation is handled via Spring Validation (***@Valid***) with messages
- **Testing Strategy**: ~95% code coverage with unit and integration tests (JaCoCo). Manual tests possible through Swagger

---

## Testing

To run the test suite and check code coverage (JaCoCo):
```Bash
./mvnw clean verify
```

---

## API Quick Start

Example JSON for creating a new account:

```Bash
POST /api/accounts
{
  "name": "Main Account"
}
```

---

## Future Improvements

- **Budget Limits**: Adding budget limits per category with alerts when the threshold is exceeded
- **Data Export**: Extending data export to more formats
- **Dashboard UI**: A frontend application for basic visualization
- **Authentication**: Implementing Spring Security to allow budget management for multiple users

---

## Contact

If you have any questions, feel free to reach out.
