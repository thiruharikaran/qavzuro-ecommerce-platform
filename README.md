# Qavzuro

A full-stack e-commerce platform: Java 21 + Spring Boot + MongoDB backend, React + Vite frontend.

> **Status note (read this first):** This project was built in a sandboxed environment with
> **no network access**, so none of it has been compiled, installed, or run — not `mvn`, not
> `npm install`, not the app itself, not the test suites. Everything below has been checked
> as thoroughly as possible *statically* (every Java import resolves to a real class, every
> controller→service→repository method call matches a real method signature with the right
> argument count, every frontend import/export pair matches, every route's lazy-loaded file
> exists). But static checking is not the same as a successful build — please run the steps
> below yourself and treat step 1 (backend build) and step 4 (frontend build) as the real
> verification. If something doesn't compile, it's most likely a small issue (an unused
> import, a subtle type mismatch) rather than a structural one, given how much of the wiring
> has already been cross-checked.

---

## 1. Prerequisites

- **Java 21** (JDK)
- **Maven 3.9+**
- **Node.js 20+** and npm
- **MongoDB 6+** running locally (a plain standalone `mongod` is fine — the app does **not**
  require a replica set; it deliberately avoids multi-document transactions for that reason)
- (Optional, for E2E tests) **Microsoft Edge** installed, plus Playwright's browser binaries

## 2. Get MongoDB running

**Option A — Docker:**
```bash
docker run -d --name qavzuro-mongo -p 27017:27017 mongo:7
```

**Option B — local install:** install MongoDB Community Server for your OS and start the
`mongod` service normally (no special replica-set configuration needed).

## 3. Backend setup and run

```bash
cd backend
cp .env.example .env      # then edit .env if you want non-default values
```

Load the `.env` values into your shell (or use an IDE run-configuration / a tool like
`direnv`), then:

```bash
mvn clean install          # compiles and runs unit tests
mvn spring-boot:run        # starts the API on http://localhost:8080
```

On first startup with `SEED_ENABLED=true` (the default), the app seeds:
- All permissions and roles (`CUSTOMER`, `WORKER`, `CLEANER`, `SUPERVISOR`, `TEAM_LEAD`,
  `MANAGER`, `ADMIN`, `MASTER_ADMIN`)
- A Master Admin (`masteradmin@qavzuro.dev` / `ChangeMe123!` by default — override via env vars)
- An Admin (`admin@qavzuro.dev` / `ChangeMe123!`)
- Sample accounts: `customer@qavzuro.dev` / `Customer123!`, `worker@qavzuro.dev` /
  `Worker123!`, `manager@qavzuro.dev` / `Manager123!`
- Sample categories, products (including one with size/color variants), and a `WELCOME10`
  coupon

**Change or disable these credentials before any real deployment** — set `SEED_ENABLED=false`
in production, or override the seeded passwords via environment variables.

### Backend tests

```bash
cd backend
mvn test
```

Tests cover: registration and BCrypt password hashing, login lockout, refresh-token rotation
and reuse detection, Role→Permission resolution (including the Master Admin wildcard), the
order state machine's legal/illegal transitions, coupon validation edge cases, atomic
inventory reservation (including a simulated concurrent-conflict case), cart pricing being
server-authoritative, order ownership enforcement (IDOR protection), return eligibility and
refund processing, and verified-purchase review detection.

Note: `pom.xml` includes `de.flapdoodle.embed.mongo` for tests that need a real embedded
Mongo instance, but the tests actually written here are pure Mockito unit tests that don't
require it — they were written this way specifically so they don't depend on downloading an
embedded Mongo binary at test time.

## 4. Frontend setup and run

```bash
cd frontend
cp .env.example .env       # defaults to http://localhost:8080/api, matching the backend above
npm install
npm run dev                # starts on http://localhost:5173
```

### Frontend tests

```bash
cd frontend
npm test                   # Vitest unit/component tests
```

### Production build

```bash
cd frontend
npm run build               # outputs to frontend/dist
npm run preview             # serve the production build locally to sanity-check it
```

For the backend:
```bash
cd backend
mvn clean package -DskipTests   # produces backend/target/qavzuro-backend.jar
java -jar target/qavzuro-backend.jar
```

## 5. End-to-end tests (Playwright, Microsoft Edge)

The E2E suite is configured to run against the **Microsoft Edge** browser channel
(`playwright.config.js` → `channel: 'msedge'`), per the project requirements — not the
default bundled Chromium.

```bash
cd frontend
npm install
npx playwright install msedge     # one-time: downloads/registers the Edge channel for Playwright
```

With the backend running (seeded) and the frontend dev server running:

```bash
npm run e2e            # headless run
npm run e2e:ui         # interactive UI mode, useful for debugging
```

Specs (`frontend/e2e/`):
- `customer-flow.spec.js` — register → browse → search → sort → product detail → variant
  select → add to cart → update quantity → checkout → sandbox payment (success and decline
  paths) → order confirmation → order history
- `admin-authorization.spec.js` — confirms a plain customer cannot see or reach `/admin`,
  a manager can reach `/admin` but not the Roles & Permissions section (no `ROLE_MANAGE`),
  and a worker can reach `/workforce` but not `/admin`

**These have not been executed** — there is no network access in the environment this project
was assembled in, so `npm install` cannot fetch the Playwright package or browser binaries.
Run them yourself once dependencies are installed.

## 6. Environment variables reference

See `backend/.env.example` and `frontend/.env.example` for the full list with comments. In
short:

| Variable | Where | Purpose |
|---|---|---|
| `MONGODB_URI` | backend | MongoDB connection string |
| `JWT_SECRET` | backend | HMAC signing key for access tokens — **change in production** |
| `JWT_ACCESS_EXPIRATION_MS` / `JWT_REFRESH_EXPIRATION_MS` | backend | Token lifetimes |
| `CORS_ALLOWED_ORIGINS` | backend | Comma-separated frontend origins allowed to call the API |
| `RATE_LIMIT_*` | backend | Per-endpoint-class rate limit capacity/refill |
| `SEED_ENABLED`, `MASTER_ADMIN_*`, `ADMIN_*` | backend | Dev seed data toggle and credentials |
| `VITE_API_BASE_URL` | frontend | Base URL the SPA calls for the API |

No real secrets are committed anywhere in this repository — only `.env.example` files with
placeholder/dev-only values. Copy them to `.env` and change the values for anything beyond
local development.

## 7. Architecture at a glance

```
backend/   Spring Boot (Java 21), layered: controller → service → repository → MongoDB
           JWT auth (access token + rotated/revocable opaque refresh tokens)
           Data-driven Role → Permission → User authorization (no hardcoded role checks)
           Server-authoritative cart/checkout/inventory/coupons; sandbox payment provider
           behind an interface; order state machine; returns/refunds; audit logging;
           Bucket4j rate limiting on sensitive endpoints

frontend/  React + Vite + Tailwind, React Router, Axios (with automatic refresh-token
           rotation on 401), lazy-loaded routes, permission-aware navigation for
           customer / workforce / admin / master admin areas
```

Full feature list, permission model, and known limitations are documented inline in the code
(see `com.qavzuro.service.PermissionCodes` for the full permission catalog, and
`com.qavzuro.config.DataSeeder` for exactly what roles/permissions each seeded role gets).

## 8. Known limitations (stated plainly, not glossed over)

- **Nothing in this project has been executed** in the environment it was built in (no
  network access — `mvn`, `npm install`, and `npx playwright install` were all unavailable).
  Static cross-checks were run instead (import resolution, method-signature/argument-count
  matching across the whole codebase) but that is not a substitute for an actual build.
- MongoDB transactions are intentionally not used (see above) — checkout instead uses atomic
  single-document conditional updates for stock reservation plus explicit compensating
  rollback logic in `CheckoutService` if a later step fails. This is correct for a
  single-instance MongoDB but means checkout isn't wrapped in one cross-collection ACID
  transaction the way a replica-set deployment could offer.
- The sandbox payment provider is intentionally fake (see `SandboxPaymentProvider`) — no real
  payment gateway is integrated. Swapping in a real provider means implementing the
  `PaymentProvider` interface.
- Playwright/E2E and full `mvn`/`npm` builds have not been run — see the note at the top.
