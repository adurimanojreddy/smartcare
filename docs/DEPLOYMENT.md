# Deploying SmartCare

SmartCare is one Spring Boot jar that serves both the API and the frontend,
plus a MySQL database. Anything that can run a Docker container and reach a
MySQL instance can host it.

## 0. Pre-flight (do this first)

**Run a real Maven build on your machine:**

```bash
mvn clean package
```

Eclipse can run code that contains compile errors (it swaps the broken
method for a runtime `Unresolved compilation problem` error), so a project
that "runs in Eclipse" can still fail `mvn package`. The Docker build uses
Maven, so find those problems now. If the build fails, send me the output.

Then confirm the packaged jar boots: `java -jar target/smartcare.jar`

## 1. Secrets you must set

| Variable | Notes |
|---|---|
| `DB_URL` | JDBC URL of your MySQL, e.g. `jdbc:mysql://host:3306/smartcare_db?useSSL=true&serverTimezone=UTC` |
| `DB_USERNAME` / `DB_PASSWORD` | Database credentials |
| `JWT_SECRET` | **Base64** string, 32+ bytes. Generate: `openssl rand -base64 48`. Never reuse the demo default. |
| `CORS_ALLOWED_ORIGINS` | Your public site URL(s) |
| `SPRING_PROFILES_ACTIVE` | `prod` (the Dockerfile already sets this) |
| `PORT` | Set automatically by most platforms |

## 2. Run it with Docker Compose (any server or VPS)

```bash
cp .env.example .env      # edit the values
docker compose up -d --build
docker compose logs -f app
```

Open `http://<server-ip>:8080`.

## 3. Platform options

- **Railway**: add a MySQL service and a GitHub-deployed service; reference the
  MySQL variables in `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`. Simplest path.
- **Render**: deploy as a Docker web service. Render has no managed MySQL, so
  point `DB_URL` at an external MySQL (Aiven, TiDB Cloud, or your own).
- **A VPS** (Oracle Cloud always-free, DigitalOcean, EC2): install Docker, use
  the compose file above, and put Caddy or Nginx in front for HTTPS.
- **AWS**: Elastic Beanstalk or App Runner for the app, RDS MySQL for the DB.

Free tiers change often; check current limits before choosing.

## 4. Change the seeded admin password (important)

`data.sql` creates `admin@smartcare.local` with the password published in the
README. The app has no change-password screen yet, so rotate it directly:

```bash
# 1. make a bcrypt hash (Spring accepts the $2a$ prefix)
htpasswd -bnBC 10 "" 'YourNewStrongPassword' | tr -d ':\n' | sed 's/$2y/$2a/'

# 2. apply it
mysql> UPDATE users SET password = '<paste hash>' WHERE email = 'admin@smartcare.local';
```

Do this before sharing the URL with anyone.

## 5. Production notes

- **HTTPS is required** for camera QR scanning on any non-localhost host, and
  for protecting JWTs in transit. Use your platform's TLS or a reverse proxy.
- `ddl-auto: update` is fine for a demo; move to Flyway/Liquibase before
  holding real data.
- Back up the `mysql_data` volume (or use your provider's automated backups).
- This is a portfolio project with a mock payment gateway. Do not put real
  patient data in it.

---

## Recommended path: VPS + Docker Compose + HTTPS

1. **Create a small Linux VPS** (Ubuntu 22.04/24.04, 1 GB RAM minimum; 2 GB is
   more comfortable for the JVM plus MySQL). Any provider works.
2. **Get a domain.** A free subdomain from DuckDNS works. Point its A record
   at the VPS's public IP. HTTPS needs a domain, and camera QR scanning needs HTTPS.
3. **Open ports 80 and 443** in the provider firewall. Do not expose 8080 or 3306.
4. **Install Docker** on the VPS: `curl -fsSL https://get.docker.com | sh`
5. **Copy the project** to the server (`git clone` your repo, or `scp` the zip).
6. **Configure:** `cp .env.example .env`, then fill in `DB_PASSWORD`,
   `JWT_SECRET` (`openssl rand -base64 48`), `DOMAIN`, and set
   `CORS_ALLOWED_ORIGINS=https://<your domain>`.
7. **Start:**
   ```bash
   docker compose -f docker-compose.yml -f docker-compose.https.yml up -d --build
   docker compose logs -f app caddy
   ```
8. Visit `https://<your domain>`, log in as admin, and **rotate the admin
   password** (section 4 above).

Updating later: `git pull && docker compose -f docker-compose.yml -f docker-compose.https.yml up -d --build`

---

## Free-tier path: Vercel + Render + Aiven

Vercel serves the static frontend; Render runs the Docker backend; Aiven hosts
MySQL. Free-tier terms change, so re-check each provider's pricing page.

**1. Database (Aiven for MySQL, free plan)**
- Create a free MySQL service. Copy host, port, user, password, and the
  database name (Aiven's default is `defaultdb`).
- Aiven requires TLS, so your URL looks like:
  `jdbc:mysql://<host>:<port>/defaultdb?sslMode=REQUIRED&serverTimezone=UTC`
  (Do not use `createDatabaseIfNotExist` there; use the existing database.)

**2. Backend (Render, free web service)**
- New > Web Service > connect your GitHub repo > Runtime: Docker.
- Environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`
  (`openssl rand -base64 48`), `CORS_ALLOWED_ORIGINS` (your Vercel URL).
- The free instance has about 512 MB RAM and a tiny CPU share, so add
  `JAVA_TOOL_OPTIONS=-Xmx256m` to keep the JVM inside the memory limit.
- Free services sleep after ~15 minutes idle and take about a minute to wake.
  Open the site once before any live demo.

**3. Frontend (Vercel)**
- Edit `vercel.json`: replace `YOUR-BACKEND.onrender.com` with your Render URL.
- Import the repo in Vercel, Framework Preset "Other", leave the build command
  empty. `vercel.json` points Vercel at the static folder and proxies `/api/*`
  to Render, so the frontend code needs no changes.
- Then set Render's `CORS_ALLOWED_ORIGINS` to the Vercel URL.

**4.** Rotate the admin password (section 4 above) before sharing the link.

Note: Render's own URL already serves the full app (the jar includes the
frontend). Vercel is optional and mainly adds a CDN and a second URL.
