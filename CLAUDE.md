# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a fullstack e-commerce application with:
- **Backend**: Spring Boot 4.0.6 API (`onlineshopapi/`)
- **Frontend**: Angular 21 SPA (`onlineshopui/`)
- **Database**: PostgreSQL 18

The application supports two user roles (Customer and Administrator) with features including product catalog, shopping cart, order management, and admin product CRUD operations.

## Development Setup

### Prerequisites
- Java 21 (backend)
- Node.js 24+ (frontend)
- Maven (backend build)
- Docker & Docker Compose (database)

### Database Setup

Start PostgreSQL database:
```bash
cd docker/development
docker-compose up -d
```

Database credentials (local):
- Host: localhost:5433  ← Docker maps 5433 on the host to 5432 inside the container
- Database: shopdb
- User: shopuser
- Password: shoppassword
- Schema: onlineshop

Flyway migrations run automatically on startup and create the schema and seed mock data.

### Backend (onlineshopapi)

**Run locally:**
```bash
cd onlineshopapi
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

The API runs on `http://localhost:3000/api` (note the `/api` context path).

**Build:**
```bash
mvn clean install
```

**Run tests:**
```bash
mvn test
```

**Environment variables required:**
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` - Database connection
- `CORS_ALLOWED_ORIGINS` - Allowed CORS origins (comma-separated)
- `JWT_SECRET` - Secret key for JWT token generation

For local development, use the `local` profile which has these pre-configured in `application-local.yml`.

### Frontend (onlineshopui)

**Install dependencies:**
```bash
cd onlineshopui
npm install
```

**Run development server (with real API):**
```bash
npm start
# or
ng serve
```

Runs on `http://localhost:4200/`

**Run with mock backend:**
```bash
npm run start:mock
# or
ng serve --configuration mock
```

**Build:**
```bash
npm run build
```

**Run tests:**
```bash
npm test
# or
ng test
```

**Lint:**
```bash
npm run lint
```

**Format code:**
```bash
npm run format
```

## Architecture

### Backend Structure

```
onlineshopapi/src/main/java/msg/onlineshopapi/
├── config/          - Application configuration (OpenAPI, CORS)
├── controller/      - REST API endpoints
├── dto/             - Data Transfer Objects
│   └── mapper/      - Entity-DTO mappers
├── exception/       - Custom exceptions and handlers
├── model/           - JPA entities
├── repository/      - Spring Data JPA repositories
├── security/        - Security config, JWT filter, user details
└── service/         - Business logic
    └── strategy/    - Order processing strategies
```

**Key patterns:**
- REST controllers use DTOs for request/response
- Service layer contains business logic
- JWT-based authentication with Spring Security
- Strategy pattern for order processing (`SINGLE_LOCATION` vs `MOST_ABUNDANT`)
- Flyway for database migrations in `src/main/resources/db/migration/`

**API Documentation:**
Swagger UI available at `http://localhost:3000/api/swagger-ui.html` when running.

### Frontend Structure

```
onlineshopui/src/app/
├── clib/            - Shared component library (navbar, modals, cards, etc.)
│   ├── components/  - Reusable UI components
│   ├── layouts/     - Layout components (root-layout)
│   └── services/    - Shared services
├── core/            - Core app functionality
│   ├── config/      - Constants and navigation routes
│   ├── mocks/       - Mock data and MSW handlers for development
│   ├── providers/   - Dependency injection providers
│   ├── services/    - Core services (notifications, environment)
│   └── types/       - Shared types, DTOs, enums
└── features/        - Feature modules (lazy-loaded)
    ├── auth/        - Authentication (login, register, guards, interceptors)
    ├── cart/        - Shopping cart
    ├── orders/      - Order management
    └── products/    - Product catalog and management
```

**Key patterns:**
- Feature-based architecture with lazy-loaded routes
- Standalone components (no NgModules)
- Route guards: `authGuard` for authentication, `rolesGuard` for authorization, `guestGuard` for unauthenticated routes
- `AuthTokenInterceptor` adds JWT to requests
- Mock Service Worker (MSW) for API mocking in mock mode
- Signals for reactive state management
- Tailwind CSS 4 for styling
- Environment-based configuration with file replacements

**Environments:**
- `development` - Real backend at `${API_URL}` (set via environment variable)
- `mock` - Uses MSW to mock API responses
- `production` - Production build with real backend

### Authentication Flow

1. User logs in via `/api/auth/login` with username/password
2. Backend validates credentials and returns JWT token
3. Frontend stores token in localStorage
4. `AuthTokenInterceptor` adds `Authorization: Bearer {token}` header to all requests
5. Backend `JwtAuthFilter` validates token on protected endpoints

User roles (`ADMIN`, `CUSTOMER`) control access via `@PreAuthorize` annotations on backend and `hasRole` directive on frontend.

## Key Files

- `onlineshopapi/src/main/resources/application.yml` - Main backend configuration
- `onlineshopapi/src/main/resources/application-local.yml` - Local development overrides
- `onlineshopui/angular.json` - Angular build configurations
- `onlineshopui/src/environments/` - Environment-specific settings
- `onlineshopui/src/app/app.routes.ts` - Main routing configuration
- `onlineshopui/src/app/core/config/constants/navigation.constants.ts` - Route constants
- `docker/development/docker-compose.yml` - Local database setup

## Gotchas

- **DB host port is 5433, not 5432.** Docker Compose maps `5433→5432`. The `local` Spring profile already accounts for this; watch out when connecting with external clients (e.g. TablePlus, psql).

- **Local seed data comes from a separate Flyway path.** The `local` Spring profile adds `classpath:db/migration/local` so `V1.1__populate_mock_data.sql` runs only in local dev. Mock data won't appear in other profiles.

- **`npm start` hardcodes the backend URL.** It uses `environment.dev.ts` (`http://localhost:3000/api`), not the base `environment.ts`. Setting `API_URL` in your shell has no effect on local dev — edit `environment.dev.ts` if you need a different URL.

## Contributing

- All changes must go through a PR to the `main` branch
- Branch naming: `feat/<task_id>-<short-desc>`
- Backend uses Lombok - ensure annotation processing is enabled in your IDE
- Frontend uses Prettier - run `npm run format` before committing
