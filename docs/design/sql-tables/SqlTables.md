# SQL Tables

Finalized Table Design with Relationships

## 1. users

id (PK)

email (unique, optional if needed for login)

password_hash (if needed)

## 2. user_details (1:1 with users)

user_id (PK, FK → users.id)

first_name

last_name

## 3. hubs (1:N from users → hubs)

id (PK)

user_id (FK → users.id)

api_key (unique, hashed)

name

## 4. probes (1:N from hubs → probes)

id (PK)

hub_id (FK → hubs.id)

target_temp (nullable)

name (optional, e.g. “Grill Probe 1”)

## 5. readings (1:N from probes → readings)

id (PK)

probe_id (FK → probes.id)

timestamp (NOT NULL, defaults to current timestamp)

current_temp (NOT NULL)
