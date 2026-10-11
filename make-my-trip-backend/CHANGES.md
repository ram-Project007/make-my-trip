# Backend changes

## Existing behavior preserved

- Retained Spring Boot, MongoDB, signup/login, flight and hotel booking routes, and admin routes.
- Retained legacy embedded booking summaries for compatibility while introducing standalone booking documents as the source of truth.
- Signup continues to hash passwords and now always assigns `USER`; password fields remain excluded from JSON output.

## Booking, security, and feature services

- Added the idempotent legacy-booking migration, standalone booking repository/service APIs, server-calculated booking amounts, atomic inventory decrement/restore, and ownership checks.
- Added database-backed HTTP Basic authentication and role checks for administrator routes. Profile and booking operations verify ownership against the authenticated principal.
- Added typed cancellation/refund, reviews/replies/flags/helpful votes, mock flight status/tracking, seat and room inventory, pricing/history/freezes, and explainable recommendations.
- Added unique indexes for standalone booking-related constraints such as one review and one helpful vote per user/resource.
- Added flight mock simulation configuration and sanitized generic server-error responses.
- Added authenticated user seat/room preference endpoints and linked legacy booking summaries to standalone IDs/status while retaining them for existing user responses.
- Added persisted notifications for tracked-flight schedule, delay, boarding, arrival, and cancellation events; this is an in-app API feed, not a mobile push integration.

## Build notes

The project uses Maven Wrapper. Configure MongoDB with `MONGODB_URI`; pricing, refund, freeze, and mock status settings are documented in `FEATURES.md` and `application.properties`.
