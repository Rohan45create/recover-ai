# RecoverAI

## 💡 Introduction

**Problem Statement**: AI Revenue Recovery (Razorpay Buildathon 2026, Track 03) — Merchants lose real revenue every day to payments that simply fail: a card declines, a network call times out, a UPI mandate expires. Most businesses respond one of two ways — do nothing, or blindly retry the same payment and hope. Neither approach diagnoses *why* the payment failed, neither is measured against any baseline, and neither can prove to a regulator or a business owner that it stayed within safe limits.

**Solution**: RecoverAI is an autonomous agent that detects a failed payment the moment it happens, diagnoses the reason using AI, decides the best way to recover it, and executes that recovery — a payment link, a reminder, a scheduled retry, or an escalation to a human — all while staying inside deterministic, configurable limits that the AI itself has no power to override. Every step is written to a permanent, tamper-evident audit trail, and the whole system's real-world effectiveness is measured with a dedicated evaluation tool that compares RecoverAI against doing nothing and against a simple rule-based retry script, on the same data.

The entire system is built around one sentence:

> **AI proposes. Policy decides. Tools execute. Audit proves.**

A **Razorpay Buildathon 2026** submission — Track 03: AI Revenue Recovery.

---

## 🧠 How It Works (in plain English)

Here is exactly what happens, in order, every time a payment fails:

1. **A payment fails on Razorpay.** Razorpay sends RecoverAI a webhook — a signed notification saying "this payment just failed, here's why."
2. **RecoverAI checks the signature.** If the webhook isn't genuinely from Razorpay, it's rejected immediately. Nothing happens until this check passes.
3. **A case is created.** RecoverAI stores this failed payment as a "recovery case" and marks it as ready to be worked on.
4. **A background worker picks it up automatically.** This worker runs continuously in the background, checking for new cases every few seconds — no person needs to click anything for this to start.
5. **The AI diagnoses the failure.** An AI model (see "AI Providers" below) looks at anonymized details of the failure — never raw card numbers or personal information — and answers two questions: *why did this fail?* and *what actions could fix it?* It might suggest sending a payment link, sending a reminder, retrying later, or escalating to a human.
6. **The policy engine checks every suggestion.** This is a separate, simple, deterministic piece of code — not AI — that checks each suggested action against hard rules: Is this amount within our automated recovery limit? Is it currently within do-not-disturb hours? Has this customer opted out of contact? Every suggestion is marked ALLOWED or BLOCKED, with a stated reason.
7. **The decision engine picks the best allowed action.** Among everything the policy engine allowed, RecoverAI calculates which action is worth the most — the actual formula is shown in the audit trail, not hidden.
8. **The chosen action actually runs.** If it's a payment link, a real Razorpay payment link is created and (if the customer's email/phone was captured) sent to them. If it's a retry, it's scheduled for later. If nothing was allowed, the case stops and is logged as such — never silently forgotten.
9. **The outcome is recorded.** If the customer completes the payment, Razorpay tells RecoverAI via a second webhook, and the case is marked Recovered — with the real, confirmed amount, never an estimate.
10. **Everything above is written to an audit trail that cannot be edited or deleted**, only appended to — so every decision can be reconstructed and checked later.

The AI never has the power to move money or contact a customer by itself. It only ever *proposes*. The policy engine is what *decides*.

---

## 🏗️ Architecture

<p align="center"> <img src="https://i.ibb.co/m5FV946k/Chat-GPT-Image-Sep-5-2026-09-45-38-PM.png" width="100%" style="border-radius: 8px;"> </p>

---

## ⚙️ Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 4.1.1, Spring Data JPA, Flyway |
| Database | PostgreSQL (Docker locally, Azure Database for PostgreSQL in the cloud) |
| Frontend | React + TypeScript, Vite, Tailwind CSS |
| Payments | Razorpay (Orders API, Payment Links API, Webhooks) |
| AI (primary) | NEAR AI Cloud — confidential inference in a Trusted Execution Environment |
| AI (fallback) | Groq — used automatically if NEAR AI Cloud is unreachable |
| Hosting | Azure App Service (backend), Azure Static Web Apps (frontend) |
| CI/CD | GitHub Actions |

We chose Spring Boot for the backend because it gives a mature, well-tested way to build a REST API, a background scheduled job, and database access all in one place, without needing several different frameworks glued together. PostgreSQL was chosen because the whole system relies on features a simple database needs to get right — row locking, so two background workers never grab the same case twice, and strict data integrity, so money-related numbers are never silently wrong.

---

## ✨ Key Features

- **Autonomous failure detection** — a real Razorpay webhook creates a case automatically, with no manual step.
- **AI diagnosis with a real fallback** — if the primary AI provider is down, the system automatically tries a second provider rather than getting stuck, and the audit trail always shows honestly which provider actually answered.
- **A deterministic policy engine** — hard limits (maximum automated recovery amount, do-not-disturb hours, mandate expiry windows) that the AI cannot override, fully visible and editable from the dashboard.
- **A transparent decision engine** — every chosen action shows its expected-value calculation in full, not as a hidden number.
- **Real tool execution** — a chosen action really creates a Razorpay payment link, really schedules a retry, or really escalates — nothing is a stub.
- **Closing the loop** — a second, independent webhook confirms whether the customer actually paid, so "Recovered" always means a real, confirmed settlement.
- **An append-only audit trail** — every step (webhook received, AI diagnosis, policy check, decision, execution, outcome) is stored permanently and cannot be edited, only added to.
- **A live dashboard** — Overview, Analytics, Recovery Log, and Policies pages that read real data from the database and update automatically.
- **An evaluation harness** — a synthetic batch of failed payments is run through RecoverAI, a simple rule-based script, and a "do nothing" baseline, so RecoverAI's benefit is measured, not just claimed.

---

## 🔐 The Policy Engine — What's Configured and Why

| Policy | Example limit | Why it exists |
|---|---|---|
| Max Recovery Cost | ₹25,000 | The hard ceiling on how much money the system can move without a human approving it — this is what "policy decides" actually means in practice. |
| DND Hours Exclusion | 21:00–08:00 | Matches Indian telecom rules on when unsolicited commercial messages are allowed to be sent — the system will not contact a customer at 2 AM. |
| Mandate Window Expiry | 24 hours | Matches RBI rules on retrying a UPI/Autopay mandate — retrying too close to a mandate's expiry isn't just pointless, it can break the mandate's terms. |
| Escalation Threshold | ₹50,000 | Above this amount, instead of the case simply stopping, it's routed to a human review queue — so the system hands off instead of giving up. |

All four of these are stored in the database, not hardcoded, and can be changed from the Policies page in the dashboard — every change is itself recorded in the audit trail.

---

## 🚀 Local Setup

### 1. Prerequisites
- Java 21 (JDK)
- Node.js 20+
- Docker Desktop
- A Razorpay account (Test Mode) — free to sign up
- A NEAR AI Cloud account and a Groq account (both have free tiers usable for this project)

### 2. Clone the Repository
```bash
git clone https://github.com/YOUR-USERNAME/recover-ai.git
cd recover-ai
```

### 3. Start the Local Database
```bash
cd backend
docker compose up -d
```
This starts a local PostgreSQL container matching what the backend expects in development.

### 4. Backend Environment Variables
Create `backend/.env`:
```env
NEAR_AI_URL=https://cloud.near.ai/v1/chat/completions
NEAR_AI_KEY=your_near_ai_api_key
NEAR_AI_MODEL=gemma-4-31b-it

GROQ_API_KEY=your_groq_api_key
GROQ_MODEL=openai/gpt-oss-120b

RAZORPAY_KEY_ID=rzp_test_your_key_id
RAZORPAY_KEY_SECRET=your_razorpay_key_secret
RAZORPAY_WEBHOOK_SECRET=your_razorpay_webhook_secret

API_DEMO_KEY=generate_a_random_string_here
```
A `backend/.env.example` file is included with the same keys and placeholder values — copy it and fill in your own real values, and never commit the real `.env` file.

### 5. Run the Backend
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
On first run, Flyway will set up the database tables automatically. The API will be available at `http://localhost:8080`.

### 6. Frontend Environment Variables
Create `frontend/.env`:
```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_API_KEY=same_value_as_backend_API_DEMO_KEY
VITE_RAZORPAY_KEY_ID=same_value_as_backend_RAZORPAY_KEY_ID
```

### 7. Run the Frontend
```bash
cd frontend
npm install
npm run dev
```
The dashboard will be available at `http://localhost:5173`.

### 8. Connecting Razorpay Webhooks Locally
Razorpay needs a public URL to send webhooks to, so for local testing you'll need a tunnel (e.g. `ngrok http 8080`), and you'll set that tunnel's URL as your webhook endpoint in the Razorpay Dashboard (Settings → Webhooks), pointing at `/api/webhooks/razorpay`.

---

## 🧪 Testing the Full Pipeline

1. Open the demo checkout page (`/demo-checkout` on the running frontend).
2. Enter an amount (keep it under your configured Max Recovery Cost to see the full pipeline run, or above it to see the policy engine correctly block it).
3. Choose Cards as the payment method and use a Razorpay domestic test card:
   - `4111 1111 1111 1111`, any future expiry date, any CVV.
4. On the mock bank page that appears, click **Failure**.
5. Open the Recovery Log page in the dashboard and watch the case appear and process automatically — no manual step needed from here.

---

## 📁 Project Structure

```
recover-ai/
├── backend/
│   ├── src/main/java/com/recoverai/
│   │   ├── controller/       # REST endpoints (webhooks, dashboard, demo checkout)
│   │   ├── service/          # Orchestrator, diagnosis, decision, audit logic
│   │   ├── ai/                # NEAR AI Cloud + Groq clients
│   │   ├── policy/            # The deterministic policy engine
│   │   ├── integration/       # Razorpay API clients
│   │   ├── evaluation/        # The offline evaluation harness
│   │   └── domain/            # Database entities
│   ├── src/main/resources/
│   │   ├── db/migration/      # Flyway SQL migrations
│   │   └── application*.yml   # Configuration (base + per-environment)
│   └── scripts/
│       └── generate_dataset.py  # Generates the synthetic evaluation dataset
├── frontend/
│   └── src/
│       ├── pages/              # Overview, Analytics, Recovery Log, Policies, Settings
│       ├── components/         # Shared shell, cards, charts
│       └── api/                # Backend API client
└── docs/
    ├── prd.md
    ├── architecture.md
    ├── rules.md
    ├── phases.md
    └── memory.md
```

---

## 📖 Resources & References

- [Razorpay Webhooks Documentation](https://razorpay.com/docs/webhooks/) — how signed payment events are received
- [Razorpay Payment Links API](https://razorpay.com/docs/api/payment-links/) — used to generate real recovery links
- [Razorpay Test Card Details](https://razorpay.com/docs/payments/payments/test-card-details/) — domestic test cards used for local/demo testing
- [NEAR AI Cloud](https://cloud.near.ai/) — confidential AI inference running inside a Trusted Execution Environment
- [Groq API Documentation](https://console.groq.com/docs) — the fallback AI provider used if NEAR AI Cloud is unavailable
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/index.html)
- [Flyway Documentation](https://documentation.red-gate.com/fd) — database migration tool used for schema management
- [Azure Database for PostgreSQL Documentation](https://learn.microsoft.com/en-us/azure/postgresql/)
- [Azure App Service Documentation](https://learn.microsoft.com/en-us/azure/app-service/)

---

Made for **Razorpay Buildathon 2026** — Track 03: AI Revenue Recovery.
