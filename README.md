# GrillGauge

Measure, monitor, and manage cooks with a connected grilling hub, temperature probes, and a web dashboard.

## Tech Stack

- API: Java 21, Spring Boot 3, Spring Security OAuth2, JPA (Hibernate), H2 (dev), Lombok.
- UI: Next.js 15, React 19, Tailwind CSS 4, NextAuth, Chart.js, Recharts, Framer Motion.
- Tooling: Maven, Spotless, Checkstyle, TypeScript, ESLint.

## API

- Spring Boot 3 REST API securing hub/probe/user endpoints with Spring Security and OAuth2 resource server.
- Persists device and cook data via Spring Data JPA; ships with H2 for local dev and Hibernate types for JSON fields.
- Includes tooling for code quality (Checkstyle, Spotless) and dev ergonomics (DevTools, Lombok).

## UI

- Next.js 15 app with React 19 and Turbopack for a fast landing page, auth, and dashboard.
- NextAuth handles sessions; charts (Chart.js/Recharts) visualise probe temps and history; Tailwind CSS v4 styles the experience.
- Animations via Framer Motion and iconography via Lucide/React Icons.
