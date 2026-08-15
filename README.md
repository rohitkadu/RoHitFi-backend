# RoHitFi — Backend Setup Guide

A modular-monolith Spring Boot backend for a BFSI (digital banking) portfolio project.
One deployable app, two databases: **Neon PostgreSQL** for relational/financial data,
**MongoDB Atlas** for audit logs / notifications / flexible documents.

This skeleton is already built for you. Follow the steps below in order.

---

## 0. What's already in this folder

```
rohitfi-backend/
├── pom.xml
├── .gitignore
├── README.md
└── src/main/
    ├── java/com/rohitfi/
    │   ├── RohitFiApplication.java        ← main class
    │   ├── config/                        ← security, OpenAPI, health check
    │   ├── common/
    │   │   ├── entity/                    ← shared base entity (createdAt etc.)
    │   │   ├── exception/                 ← global exception handler goes here
    │   │   ├── dto/                       ← shared DTOs (ApiError, PageResponse)
    │   │   └── util/                      ← JWT helper, ref-no generator, etc.
    │   └── <module>/                      ← one folder per business module:
    │       ├── controller/                  auth, customer, kyc, account,
    │       ├── service/                     transaction, payment, upi, card,
    │       ├── repository/                  loan, investment, manager,
    │       ├── entity/                      notification, audit
    │       └── dto/
    └── resources/
        ├── application.yml                ← reads DB_URL / MONGO_URI / JWT_SECRET from env
        └── application-local.yml.example  ← copy → application-local.yml for local secrets
```

Every layer folder has a `package-info.java` so Git tracks the empty package —
that's normal, you'll fill these in module by module.

---

## 1. Create the GitHub repos

You'll want two repos: one for this backend, one for the React frontend later.

1. Go to https://github.com/new
2. Repo name: `rohitfi-backend` → Private (recommended while it's WIP) → **do not**
   initialize with a README/.gitignore (you already have both here) → Create.
3. Repeat with `rohitfi-frontend` for later — you can leave that one empty for now.
4. Back in this folder, push the skeleton:

```bash
cd rohitfi-backend
git init
git add .
git commit -m "chore: initial modular-monolith project skeleton"
git branch -M main
git remote add origin https://github.com/<your-username>/rohitfi-backend.git
git push -u origin main
```

---

## 2. Set up Neon (PostgreSQL)

1. Go to https://neon.tech and sign up / log in (GitHub login is fastest).
2. **Create a project** → name it `rohitfi` → pick a region close to you (e.g. Mumbai/Singapore) → PostgreSQL version 16 is fine.
3. Neon creates a default database (usually named `neondb`) and a default role automatically.
4. On the project dashboard, click **Connect** → copy the **connection string**. It looks like:
   ```
   postgresql://<user>:<password>@<host>/<dbname>?sslmode=require
   ```
5. You'll split this into three pieces for Spring:
   - `DB_URL` → `jdbc:postgresql://<host>/<dbname>?sslmode=require` (note the `jdbc:` prefix!)
   - `DB_USERNAME` → `<user>`
   - `DB_PASSWORD` → `<password>`
6. Neon free tier auto-suspends idle databases — the first request after idle takes a
   couple of extra seconds to wake up. Normal, not a bug.

---

## 3. Set up MongoDB Atlas

1. Go to https://www.mongodb.com/cloud/atlas/register and sign up / log in.
2. **Build a Database** → choose the **free M0 cluster** → pick a cloud provider/region
   close to you → name the cluster (e.g. `rohitfi-cluster`) → Create.
3. **Database Access** (left sidebar) → Add New Database User → username/password
   (autogenerate a strong password and save it) → built-in role `Read and write to any database`.
4. **Network Access** (left sidebar) → Add IP Address:
   - For local development, "Allow access from anywhere" (`0.0.0.0/0`) is the easiest
     option while you're learning — tighten this later.
5. Go back to the cluster → **Connect** → "Drivers" → copy the connection string:
   ```
   mongodb+srv://<user>:<password>@<cluster>.mongodb.net/?retryWrites=true&w=majority
   ```
6. Add a database name to it (Mongo creates it on first write): change it to
   ```
   mongodb+srv://<user>:<password>@<cluster>.mongodb.net/rohitfi?retryWrites=true&w=majority
   ```
   → this is your `MONGO_URI`.

---

## 4. Import into Spring Tool Suite

1. Open STS → **File → Import → Maven → Existing Maven Projects**.
2. Browse to the `rohitfi-backend` folder → Finish. STS will download all dependencies
   from `pom.xml` (first time takes a few minutes).
3. Once imported, right-click the project → **Maven → Update Project** if you see red
   error markers.
4. Confirm the JDK: right-click project → Properties → Java Build Path → Libraries →
   make sure it's using **JDK 21** (you already have this installed).

---

## 5. Configure your secrets

Pick **one** of these two options:

**Option A — application-local.yml (recommended, easiest in STS)**
```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```
Fill in the real Neon and Atlas values in that file (it's git-ignored, safe to edit).
Then, in STS: right-click `RohitFiApplication.java` → **Run As → Run Configurations** →
Arguments tab → VM arguments:
```
-Dspring.profiles.active=local
```

**Option B — environment variables**
Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `MONGO_URI`, `JWT_SECRET` as OS/IDE
environment variables and run with the default profile.

Either way, `JWT_SECRET` should be a random string of 32+ characters — you'll use it
once we build the JWT auth module. For now any placeholder string works.

---

## 6. Run it

Right-click `RohitFiApplication.java` → **Run As → Spring Boot App** (or plain Java
Application). Once it's up:

```
GET http://localhost:8080/api/health
```
should return:
```json
{"status": "UP", "service": "RoHitFi Backend"}
```

If you see connection errors, it's almost always one of: wrong Neon host string
missing `jdbc:` prefix, Atlas IP not whitelisted, or a typo in the password.

---

## 7. What's next

Once this boots cleanly against both databases, we build module by module in the order
from the blueprint:

`auth → customer → kyc → account → transaction → payment/upi/card → loan → manager → investment`

Each module gets: entity → repository → DTO → service → controller, tested with
Postman/Swagger (`/swagger-ui.html`) before moving to the next one.
