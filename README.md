# URL Shortener with Analytics and Scalability Design

Production-grade URL shortener built with Spring Boot 4.0.7, PostgreSQL, Redis, and React + TypeScript.

## Tech Stack
- Backend: Java 21, Spring Boot 4.0.7, Spring Data JPA, Spring Data Redis
- Database: PostgreSQL 16 (Flyway migrations)
- Cache: Redis 7
- Frontend: React + TypeScript (Vite)
- Build: Maven
- Deployment: Railway (backend, PostgreSQL, Redis), Vercel/Render (frontend)

## Architecture
- Deterministic short-code generation via Base62 encoding of the database-generated ID
- Indexed PostgreSQL lookups for redirection (`short_code` unique index)
- Redis cache-aside pattern with TTL-based expiry and explicit invalidation on update/delete
- Stateless REST APIs, horizontally scalable
- Async click analytics recording (non-blocking redirect path)
- GeoIP country resolution via MaxMind GeoLite2 (optional, degrades gracefully if database absent)

## Project Structure
src/main/java/com/urlshortener/
config/
controller/
dto/
entity/
exception/
repository/
service/
service/impl/
mapper/
util/
cache/
frontend/
src/
components/
pages/
hooks/
services/
utils/
types/

## Local Development

### Prerequisites
- Java 21
- Maven (via `./mvnw`)
- Docker & Docker Compose
- Node.js 18+

### Run full stack with Docker Compose
```bash
docker compose up --build
```
Backend available at `http://localhost:8080`.

### Run backend locally (without Docker)
```bash
./mvnw spring-boot:run
```

### Run frontend locally
```bash
cd frontend
npm install
npm run dev
```
Frontend available at `http://localhost:5173`.

---

## Deployment

### Backend + PostgreSQL + Redis on Railway

1. Push this repository to GitHub.
2. In Railway, create a new project and select **Deploy from GitHub repo**.
3. Add two Railway plugins to the project: **PostgreSQL** and **Redis**.
4. Add a service for this repo. Railway will detect `railway.json` and build using the root `Dockerfile`.
5. In the backend service's **Variables** tab, set:

   | Variable | Value |
   |---|---|
   | `SPRING_PROFILES_ACTIVE` | `production` |
   | `DATABASE_URL` | `jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}` |
   | `DATABASE_USERNAME` | `${{Postgres.PGUSER}}` |
   | `DATABASE_PASSWORD` | `${{Postgres.PGPASSWORD}}` |
   | `REDIS_HOST` | `${{Redis.REDISHOST}}` |
   | `REDIS_PORT` | `${{Redis.REDISPORT}}` |
   | `REDIS_PASSWORD` | `${{Redis.REDISPASSWORD}}` |
   | `APP_BASE_URL` | your Railway public domain, e.g. `https://url-shortener-backend-production.up.railway.app` |
   | `CORS_ALLOWED_ORIGINS` | your deployed frontend URL, e.g. `https://url-shortener.vercel.app` |

   The `${{ServiceName.VAR}}` syntax references variables from the linked Postgres/Redis plugins directly in Railway's dashboard.

6. Deploy. Railway will build the Docker image, run the container, and expose it on the port from the `PORT` env var (already wired via `server.port: ${PORT:8080}`).
7. Confirm health:
```bash
   curl https://<your-railway-domain>/actuator/health
```
   Expected: `{"status":"UP"}`

### Frontend on Vercel

1. Import the repository into Vercel.
2. Set the project's **Root Directory** to `frontend`.
3. Framework preset: **Vite**. Build command: `npm run build`. Output directory: `dist`.
4. Add environment variable:

   | Variable | Value |
   |---|---|
   | `VITE_API_BASE_URL` | your Railway backend URL, e.g. `https://url-shortener-backend-production.up.railway.app` |

5. Deploy. `vercel.json` handles SPA routing so client-side routes (`/result/:shortCode`, `/analytics/:shortCode`) don't 404 on refresh.

### Frontend on Render (alternative)

1. Create a new **Static Site** in Render, or use the included `frontend/render.yaml` as a Blueprint.
2. Root directory: `frontend`. Build command: `npm install && npm run build`. Publish directory: `dist`.
3. Set `VITE_API_BASE_URL` in the Render dashboard environment variables to your Railway backend URL.
4. Deploy. The rewrite rule in `render.yaml` handles SPA routing.

### Post-deployment checklist
- [ ] Backend health check returns `UP`
- [ ] `POST /api/urls` succeeds from the deployed frontend (no CORS errors)
- [ ] Redirect via `GET /<shortCode>` works and returns `302`
- [ ] `GET /api/analytics/<shortCode>` returns populated data after a redirect
- [ ] Flyway migration applied on the Railway Postgres instance (`\dt` shows all 4 tables)

---

## REST API Reference

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/urls` | Create a short URL |
| `GET` | `/{shortCode}` | Redirect to original URL |
| `GET` | `/api/urls/{id}` | Get URL details by ID |
| `GET` | `/api/urls?page=&size=` | List URLs (paginated) |
| `PUT` | `/api/urls/{id}` | Update a URL |
| `DELETE` | `/api/urls/{id}` | Delete a URL |
| `GET` | `/api/analytics/{shortCode}` | Get click analytics |

## Known Gaps / Not Yet Implemented
- **Rate limiting**: `bucket4j-core` is included as a dependency (see `pom.xml`) but no rate-limiting filter/interceptor has been wired into the request pipeline yet. This was listed as a scalability requirement but wasn't covered in a dedicated build phase — flagging it here so it isn't mistaken for a finished feature.
- **GeoIP**: functional but requires manually placing a `GeoLite2-Country.mmdb` file at `src/main/resources/geoip/`; without it, `country` resolves to `"Unknown"`.