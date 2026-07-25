# Crypto Wallet system
A Spring Boot backend service that allows a user to fetch the value and performance of cryptocurrency wallet. 
Uses a scheduler job that fetch market prices from an external API, the CoinCap API, and stores price data history, and have REST APIs to query wallet value and asset performance.

## Tech Stack
- Java 17
- Spring Boot 4.1.0
- Spring Data JPA / Hibernate
- PostgreSQL
- springdoc-openapi (Swagger UI)
- Lombok

## Prerequisites
- Windows/macOS: install [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- A CoinCap API key (https://coincap.io)

It's not necessary to install JAVA, IDEA, maven because will run inside a docker environment.

## Setup
1. Clone the repository:
```bash
git clone https://github.com/carolinaGoncalves/crypto-wallet.git
```
go to the root:
```bash
cd crypto-wallet
```

3. Set the required environment variables:

macOS / Linux:
```bash
export COINCAP_API_KEY=your_coincap_api_key
```
Windows (cmd):
```bash
set COINCAP_API_KEY=your_coincap_api_key
```
## How to Run

From the project root, on any OS:

```bash
docker compose up --build
```

(If you're on an older Docker Compose version, use `docker-compose up --build` with a hyphen instead.)
This builds the app image and starts two containers: `cryptowallet-db` (Postgres) and `cryptowallet-app` (the Spring Boot app). 
The app will wait for the database to be ready before starting and will be available at `http://localhost:8080`.

The database schema is created/updated automatically on startup (because of `spring.jpa.hibernate.ddl-auto=update`).

Swagger is available at:
```
http://localhost:8080/swagger-ui.html
```
### Running tests
Tests run inside the Maven build stage of the Docker image

## API Endpoints

| Method | Endpoint | Description                                                                                       |
|---|---|---------------------------------------------------------------------------------------------------|
| POST | `/api/users` | Create a user                                                                                     |
| GET | `/api/users/username/{username}` | Get a user by username                                                                            |
| POST | `/wallets` | Create a wallet for a user given a username                                                       |
| POST | `/wallets/{username}/assets` | Add an asset to a user's wallet                                                                   |
| GET | `/wallets/{username}` | Get a user's wallet and its assets                                                                |
| GET | `/wallets/{username}/value?filterDate=YYYY-MM-DD` | Get wallet value; `filterDate` is optional and defaults to today, returns historical wallet value |
| GET | `/wallets/{username}/performances` | Get wallet performance in % and returns the best/worst performing asset                           |

## Design Decisions
(TBD)

## Assumptions
(TBD) 

## Testing

Unit tests cover the service layer: price retrieval/persistence, wallet valuation, wallet performance, and the scheduling logic (including failure handling and concurrency behavior).

## Possible Improvements

- Rate limiting / retry logic for CoinCap API calls
- Caching of latest prices to decrease database reads
- Pagination for price history queries

