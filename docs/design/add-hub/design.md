Nice — below is a **practical, step-by-step checklist** for implementing Option A **with the hub being the registered device** (so only one `hubs` table). It covers DB schema, endpoints, Spring Boot implementation notes, CSR handling, sample curl/OpenSSL commands, security, revocation, and a testing checklist. Copy/paste ready where useful.

# 1 — High-level flow (reminder)

1. Hub (physical device) calls open endpoint to request registration → server returns `hubId` + `otp` (short expiry).
2. Hub displays OTP to user.
3. User confirms OTP in Next.js frontend (authenticated).
4. Hub generates keypair and CSR locally, then uploads CSR to server.
5. Server validates CSR, signs it with CA, stores cert & public key, sets hub `REGISTERED`.
6. Hub stores certificate locally and uses it for future mTLS or signed requests.

# 2 — Database (single hubs table)

Create / extend `hubs` table to include onboarding & cert fields.

Example SQL (Postgres):

```sql
CREATE TABLE hubs (
  hub_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  owner_user_id UUID,                        -- FK to users table (nullable until confirmed)
  name TEXT,
  status TEXT NOT NULL DEFAULT 'PENDING',    -- PENDING, CONFIRMED, REGISTERED, REVOKED
  otp TEXT,
  otp_hash TEXT,                             -- optional: store hashed OTP
  otp_expires_at TIMESTAMP WITH TIME ZONE,
  public_key_pem TEXT,
  csr_pem TEXT,                              -- optional: store CSR if you want audit/verification
  certificate_pem TEXT,
  cert_serial BIGINT,
  issued_at TIMESTAMP WITH TIME ZONE,
  expires_at TIMESTAMP WITH TIME ZONE,
  last_seen_at TIMESTAMP WITH TIME ZONE,
  metadata JSONB,
  revoked_at TIMESTAMP WITH TIME ZONE,
  revocation_reason TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);
CREATE INDEX idx_hubs_owner ON hubs(owner_user_id);
```

# 3 — Endpoints (suggested)

1. `POST /api/hubs/register` — open, unauthenticated

   * Request: optional `{ model, fwVersion, extra }`
   * Response: `{ hubId, otp, otpExpiresAt }`
   * Server: create hub row with `PENDING`, generate OTP (store hashed if desired), set expiry.

2. `POST /api/hubs/confirm` — authenticated (user)

   * Request: `{ hubId, otp }`
   * Response: `200 OK` / error
   * Server: verify user session, verify OTP & expiry, set `owner_user_id`, `status = CONFIRMED`.

3. `POST /api/hubs/{hubId}/csr` — unauthenticated but must include hubId in URL and CSR in body

   * Request: raw CSR PEM or `{ csrPem }`
   * Response: `{ certificatePem }` or `201 Created`
   * Server: require hub `status == CONFIRMED`; validate CSR; sign; store cert & public key; set `REGISTERED`.

4. `GET /api/hubs/{hubId}/ca` — optional: return CA cert PEM for hub to trust server.

5. `POST /api/hubs/{hubId}/revoke` — authenticated admin or owner

   * Request: `{ reason }`
   * Server: set `status=REVOKED`, add to CRL/OCSP data.

# 4 — OTP generation & security

* Use `SecureRandom` for OTP generation. 6–8 digits is common.
* OTP expiry short: 5–10 minutes.
* Consider storing **otp_hash = HMAC_SHA256(otp, server_secret)** instead of plaintext OTP.
* Rate limit `/register` and `/confirm` (per IP / per user).
* Lock hub registration attempts after N failed tries.

# 5 — CSR handling & certificate issuance (server)

* Require a **PKCS#10 CSR** from hub (not raw public key). CSR contains the public key + subject.
* Validate CSR:

  * Parse CSR (BouncyCastle or java.security).
  * Check key type & size: RSA >= 2048 or EC secp256r1/SECP384R1.
  * Ensure CSR subject or SAN includes `CN=hub-<hubId>` or some claim you can check; or compare CSR public key fingerprint with any earlier-reported fingerprint.
  * Reject CSR if hub status is not `CONFIRMED`.
* Sign CSR:

  * Use an offline/secure CA private key (prefer KMS or Vault) to sign.
  * Build X.509v3 cert with `KeyUsage` and `ExtendedKeyUsage=clientAuth`.
  * Set `NotBefore = now - 1m`, `NotAfter = now + N days` (e.g., 90 days).
  * Choose secure serial numbers (random).
* Return cert PEM to hub and store `certificate_pem`, `public_key_pem`, `issued_at`, `expires_at`, `cert_serial`.
* Consider storing the CSR (`csr_pem`) for audits.

# 6 — Spring Boot implementation outline

### Dependencies

* `spring-boot-starter-web`, `spring-boot-starter-security` (for protected endpoints)
* `spring-boot-starter-data-jpa` + driver
* BouncyCastle: `org.bouncycastle:bcprov-jdk15on` and `org.bouncycastle:bcpkix-jdk15on`

### Entities (JPA)

Provide `Hub` entity mapping to the `hubs` table (fields from SQL above).

### Controllers

* `HubController` with methods for `/register`, `/confirm`, `/{id}/csr`, `/ca`, `/revoke`.

### Services

* `HubService` for DB operations & OTP handling.
* `CertificateService` for CSR parsing & signing:

  * `X509Certificate signCsr(String csrPem, UUID hubId)`

### Example pseudo-code for CSR signing (conceptual)

```java
PKCS10CertificationRequest csr = parseCsr(csrPem);
PublicKey pk = csr.getPublicKey();
validateKey(pk);
X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(
    caSubject, serial, notBefore, notAfter, csr.getSubject(), SubjectPublicKeyInfo.getInstance(pk.getEncoded()));
// add extensions, EKU clientAuth
X509CertificateHolder signedHolder = signWithCa(certBuilder, caPrivateKey);
PEMWriter pem = ...
String certPem = pem.write(signedHolder);
```

(If you want, I can provide a full copy-pasteable Java class that signs a CSR using BouncyCastle.)

# 7 — Node (hub) instructions (what firmware/software should do)

1. Call `POST /api/hubs/register` → store `hubId` and show OTP to user.
2. Wait for user to confirm via frontend.
3. Generate keypair locally:

   * OpenSSL example:

     ```bash
     openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out device.key.pem
     openssl req -new -key device.key.pem -subj "/CN=hub-<hubId>" -out device.csr.pem
     ```

   * Prefer OS/hardware secure keystore (TPM, secure element) on devices that support it.
4. POST CSR to `/api/hubs/{hubId}/csr`:

   * `Content-Type: application/pem-csr` or JSON `{ csrPem }`.
5. Store returned `certificatePem` securely beside the private key.
6. Use TLS client cert (mTLS) or sign requests with private key; server verifies using stored cert/public key.

# 8 — Example curl commands (for testing)

Register:

```bash
curl -X POST https://api.example.com/api/hubs/register \
  -H "Content-Type: application/json" \
  -d '{"model":"hub-v1","fw":"1.2.3"}'
# => { "hubId":"...", "otp":"123456", "otpExpiresAt":"..."}
```

Confirm (from frontend, authenticated):

```bash
curl -X POST https://api.example.com/api/hubs/confirm \
  -H "Authorization: Bearer <user-jwt>" \
  -H "Content-Type: application/json" \
  -d '{"hubId":"<uuid>","otp":"123456"}'
```

Upload CSR:

```bash
curl -X POST https://api.example.com/api/hubs/<hubId>/csr \
  -H "Content-Type: application/pem-csr" \
  --data-binary @device.csr.pem
# Response: certificate PEM
```

Test mTLS (if your server accepts client certs):

```bash
curl --cert device.crt.pem --key device.key.pem https://hub.example.com/protected
```

# 9 — Revocation & lifecycle

* Implement revocation endpoint to set `status = REVOKED`, store `revoked_at`, `revocation_reason`.
* Maintain a CRL table or OCSP responder data: store revoked `cert_serial` & `revoked_at`.
* On server-side auth checks, reject certs that appear in revocation list.
* Plan certificate rotation (short TTL, auto-renew flow).

# 10 — Security & operational hardening

* Always enforce HTTPS for all endpoints.
* Protect confirm/revoke endpoints with Spring Security (JWT or session).
* Store CA private key in secure KMS / HSM / Vault. Do not keep in source control.
* Log audit events (register, confirm, csr upload, issue, revoke) with hubId and userId.
* Use short certificate TTL (90 days or less) and auto-rotation.
* Rate-limit registration and confirmation.
* Validate CSR public key parameters and reject weak keys.
* Consider adding challenge that ties CSR to earlier registration (e.g., CSR CN includes hubId).

# 11 — Testing checklist

* Unit tests for OTP generation and expiry.
* Integration test: full happy path (register → confirm → csr → issue cert → mTLS).
* Test invalid OTPs, expired OTPs, replay attempts.
* Test CSR rejection for:

  * Hub not confirmed
  * Weak key sizes
  * CSR subject mismatch
* Test revocation: revoke cert and ensure auth fails.
* Test CA key rotation: issue new CA and ensure old certs invalidated or reissued as per policy.
* Penetration checks: replay attacks, MITM attempts (ensure TLS), DB leak simulation (otp hashing).

# 12 — UX & edge-cases

* If user doesn’t confirm before expiry: allow re-register/new OTP flow.
* Allow owner to view pending hubs and confirm or cancel.
* Provide clear error messages for CSR rejects (e.g., “CSR public key too small”).
* Offer a way to re-enroll lost hubs (revoke old, create new hub entry).

# 13 — Optional improvements (future)

* Have hub submit CSR immediately at register time and hold CSR pending user confirmation (you can sign after confirm).
* Use short-lived bootstrap token (instead of OTP) that the user activates in the UI.
* Support hardware-backed key storage (TPM/secure element).
* Show certificate fingerprint in UI for manual verification if needed.

---

If you want now I can:

* generate the **exact SQL schema** file for Postgres with constraints and indexes,
* or produce a **copy-pasteable Spring Boot controller + CertificateService** that signs CSRs with BouncyCastle,
* or produce a **Next.js snippet** that calls the confirm endpoint and displays pending hubs.

Which one should I produce next?
