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

2. go to the root:
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
Or for older docker versions

```bash
docker-compose up --build
```

This builds the app image and starts two containers: `cryptowallet-db` (database Postgres) and `cryptowallet-app` (the Spring Boot app). 
The app will wait for the database to be ready before starting and will be available at `http://localhost:8080`.

The database schema is created/updated automatically on startup (because of `spring.jpa.hibernate.ddl-auto=update`).
During the first run, the database is empty, so resources must be created in order: a user first, then a wallet for that user, and finally the wallet's assets.

Swagger is available at:
```
http://localhost:8080/swagger-ui.html
```

To stop the app: press Ctrl+C, or run 
```bash
docker compose down 
```
from another terminal in the project root.

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

## Design Decisions and Assumptions
### Authentication and Authorization
I left this topic out of scope because it was not mentioned directly, and the challenge instructions said to keep a simple approach. Including this topic would increase the complexity of the solution.
I would do this using the spring-security dependency based on JWT and login. The endpoints would need to be adapted to not use the username as a path parameter, and the username would be extracted from the JWT inside the controllers.

### Entities
#### User model
The user entity will only have a username field, which is unique, and a fullname field, since authentication is out of scope.
The username was chosen as the external identifier across the service instead of the id, as it is already unique and more meaningful. 
The id remains the foreign key used in table relationships.

#### Wallet model
I’m assuming a user can only have one wallet with a list of assets. So, the wallet entity will have a OneToOne relationship with the user table and OneToMany with walletAsset table.
When adding a WalletAsset, the purchaseDate needs to be filled instead of using the server date and time at the moment. I decided to follow this approach because a purchase can happen long before it is recorded in the system.

### API design
Wallet asset endpoints are inside WalletController rather than a separate AssetController, since an asset has no meaning without the context of a wallet.
I didn't implement full CRUD for every entity, only the endpoints required by the functional specification.

### Price history 
This entity will store the price values coming from the external API by symbol and will not have a relationship with the user entity, because the prices stored here will be used by any user.
I didn't create the endpoint to get this table directly as it's used internally to feed the valuation and performance calculations but it's possible to check the logs and if the prices are fetch and save correctly:
```
cryptowallet-app  | 2026-07-25T21:32:01.123Z  INFO 1 --- [cryptowallet] [pool-2-thread-2] c.s.cryptowallet.service.PriceService    : Inserted KEK with value 6.0000000E-11
```

### Numeric precision and scale
All monetary values like price, purchasePrice, and quantity are stored as BigDecimal with precision = 20 and scale = 12 to avoid floating-points.
The scale was increased from the initial value after a test using lower assets prices, like 0.000000000139 
with a smaller scale this would be rounded to 0, returning not expecting performance calculations.

### Price retrieval and concurrency
I decided to use a cron scheduled job that runs every 30 seconds.
The first step is to fetch the distinct symbols that currently exist across all wallets. Then, an ExecutorService is used to create and run reusable threads in a thread pool with a size of 3 (configurable through application.properties).
These threads fetch the price for each specific symbol using the endpoint https://rest.coincap.io/v3/price/bysymbol/SYMBOL and then save the result in the price_history table.
Each thread handles its own exceptions, so if something fails while fetching one symbol, it will not crash the scheduler or affect the processing of the remaining symbols.

### Wallet valuation
I used the same endpoint to cover both point a) and point b) from the challenge instructions, with a query parameter to filter by date. 
This parameter is optional and if omitted will use the current system date.
Wallet value on a given date is calculated by summing only the WalletAsset rows that have the purchase date before or on that date, using the most recent PriceHistory entry available using the same filter date.
I validate if the date is in the future and if so the endpoint returns an invalidDateException.
I chose to compare dates using the end of day time value (23:59:59.999) to avoid unexpected results caused by time zone differences as Swagger doesn't convert input to UTC automatically,
Comparing against the exact current instant exclude assets purchased earlier.

I added transactional annotation because I have a more than one read operation and this will ensure that I will use the same connection database for all and to avoid LazyInitializationException
because session will be opened in all method.

### Performance calculation
It's calculated per symbol using the follow formula returning a % value:
```
(current value − total invested) / total invested × 100
```
where total invested summed per symbol is
```
quantity × purchase_price
```
The best and worst performing assets are the highest and lowest values in that wallet asset list.
If an asset has no investment, its performance is returned as 0% rather than throwing.

### Time zone handling
The backend will treat every date and comparison using UTC, never the server local time zone.
This keeps the behavior the same no matter where the app is deployed.
For that i needed to adapt the code for the swagger issue describe in Wallet valuation section: as purchaseDate is a plain date/time with no time zone attached, so the server can't tell if a submitted value was meant as UTC or as the client's local time. This caused a real bug during manual testing filtering a wallet asset that should not be filtered.
The fix: compare by calendar date, not exact time, so any time on today counts as today, no matter which hour it was entered at.
In a real app, the frontend would convert the user's local time to UTC automatically before sending it.

### Error handling and response
To normalize the error responses and avoid getting the stacktrace that spring does by default, I created
a class to centralize the exception using the annotation @ControllerAdvice that will map the following exceptions:
- UserNotFoundException - 404; 
- WalletNotFoundException - 404;
- WalletAlreadyExistsException - 409;
- InvalidDateException - 400;
- handle parameters validation - 400;

to the appropriate HTTP status and I added a method to catch all the other exception not mapped returning a 500. 

## Testing
Unit tests cover the service layer: price retrieval/persistence, wallet valuation, wallet performance, and the scheduling logic (including failure handling and concurrency behavior).

## Possible Improvements
- Authentication and authorization
- Rate limiting / retry logic for CoinCap API calls
- Caching of latest prices to decrease database reads
- Create an endpoint to return price history and it would need pagination