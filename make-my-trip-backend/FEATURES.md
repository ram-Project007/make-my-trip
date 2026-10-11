# MakeMyTour backend feature API

This document describes the existing Spring Boot backend. Frontend code is unchanged.

## Authentication

Signup and login remain at `POST /user/signup` and `POST /user/login`. The backend uses HTTP Basic for protected endpoints; send `Authorization: Basic <base64(email:password)>` on each request. Public signup always creates a `USER`. Admin endpoints require an existing database user with the `ADMIN` role. Booking, profile, refund, review, reply, tracked-flight, price-freeze, and recommendation operations derive user identity from the authenticated principal.

## Booking and refunds

Legacy booking routes remain available: `POST /booking/flight` and `POST /booking/hotel` with `userId`, resource ID, quantity, and optional `price` query parameters. The submitted price is ignored when calculating the stored booking total; server-side dynamic pricing determines the amount. Users can view their bookings through `GET /api/users/me/bookings` and `GET /api/bookings/{id}`.

* `POST /api/bookings/{id}/cancel` with `{"reason":"CHANGE_OF_PLANS"}`
* `GET /api/bookings/{id}/refund`
* `GET /api/users/me/refunds`
* Admin: `GET /api/admin/refunds`, `PUT /api/admin/refunds/{id}/status` with `{"status":"PROCESSING"}`

The default policy returns 50% when cancellation is within 24 hours of booking; configure with `REFUND_WITHIN_HOURS` and `REFUND_PERCENTAGE`. Cancellation restores the associated inventory and creates a server-calculated refund.

## Reviews

* `POST /api/reviews` with `bookingId`, `targetType` (`FLIGHT` or `HOTEL`), `targetId`, `rating` (1–5), `text`, and optional `photos`
* `GET /api/reviews?targetType=HOTEL&targetId=…&rating=5&photos=true&sort=newest`
* `GET /api/reviews/{id}`, `PUT /api/reviews/{id}`, `DELETE /api/reviews/{id}`
* Reply: `POST /api/reviews/{id}/reply`; list: `GET /api/reviews/{id}/replies`; owner edit/delete: `PUT/DELETE /api/reviews/{id}/replies/{replyId}`
* Flag: `POST /api/reviews/{id}/flag` with a `reason` enum
* Helpful vote: `POST /api/reviews/{id}/helpful` (one vote per authenticated user)
* Admin moderation: `GET /api/admin/reviews/flagged`, `PUT /api/admin/reviews/{id}/moderate` with `{"status":"APPROVED|HIDDEN|REMOVED|PENDING"}`

New reviews are pending until approved. Public review reads only return approved reviews. Reviews require an owned completed booking matching the target; one review per booking is enforced.

Sort values: `newest`, `highest-rated`, `lowest-rated`, `most-helpful`. Filters: target type/ID, rating, and presence of photos.

## Flight status and tracking

* `GET /api/flights/{id}/status`
* `GET /api/flights/status?flightNumber=…`
* Admin: `POST /api/admin/flights/{id}/status`
* Track/untrack: `POST/DELETE /api/flights/{id}/track`
* Current user's tracked flights: `GET /api/users/me/tracked-flights`
* Notifications: `GET /api/users/me/notifications`, `PUT /api/users/me/notifications/{notificationId}/read`, `PUT /api/users/me/notifications/read`

The mock service creates a status from existing flight data and advances simulated records deterministically from `ON_TIME` to `BOARDING`, `DEPARTED`, `IN_AIR`, and `LANDED`. An admin-supplied status is treated as a manual snapshot. Configure simulation with `FLIGHT_STATUS_SIMULATION_ENABLED` and `FLIGHT_STATUS_SIMULATION_INTERVAL_SECONDS`.

Flights deterministically selected for the mock delay path also enter `DELAYED`, update estimated departure/arrival, and later depart. Status changes create persisted in-app notifications for users tracking that flight. Notifications support departure-time changes, delays, boarding, estimated-arrival changes, and cancellation. This is a backend notification feed; no mobile push provider is configured.

## Flight seats and hotel rooms

* `GET /api/flights/{id}/seats`; admin inventory provisioning: `POST /api/admin/flights/{id}/seats`
* Select/release a seat: `POST/DELETE /api/bookings/{id}/seat`; selection body is `{"seatNumber":"12A"}`
* Read hotel room inventory: `GET /api/hotels/{id}/rooms`; admin provisioning: `POST /api/hotels/{id}/rooms`
* Select a room type: `POST /api/bookings/{id}/room` with `{"roomType":"DELUXE"}`

Saved preferences are scoped to the authenticated user: `GET/PUT/DELETE /api/users/me/preferences/seat` and `GET/PUT/DELETE /api/users/me/preferences/room`. Seat preference fields include class, type, window/aisle preference, and row. Room preference fields include room type, minimum capacity, amenities, and premium preference.

Seat claims use atomic conditional updates and a unique `(flightId, seatNumber)` index. Room claims atomically decrement available quantity for the booking quantity. Booking owners only may select or release inventory.

## Pricing and freezes

* `GET /api/flights/{id}/current-price`, `GET /api/hotels/{id}/current-price`
* `GET /api/flights/{id}/price-history`, `GET /api/hotels/{id}/price-history`
* Admin: `GET/PUT /api/admin/pricing/rules`
* `POST /api/bookings/{id}/price-freeze`, `GET /api/price-freezes/{id}`, `POST /api/price-freezes/{id}/use`

The pricing engine applies configurable holiday, demand, low-inventory, and close-to-travel multipliers and stores price snapshots. Defaults: holiday 1.20, demand 1.10, low inventory 1.15, lead time 1.10. Environment overrides are `PRICING_HOLIDAY_MULTIPLIER`, `PRICING_DEMAND_MULTIPLIER`, `PRICING_AVAILABILITY_MULTIPLIER`, `PRICING_LEAD_TIME_MULTIPLIER`, `PRICING_DEMAND_BOOKINGS_THRESHOLD`, `PRICING_LOW_INVENTORY_THRESHOLD`, and `PRICING_FREEZE_MINUTES`.

The existing booking flow has no separate checkout/payment stage: a freeze can be created for an owned confirmed booking and marked used once, but there is no checkout operation that applies it to a later purchase.

## Recommendations

* `GET /api/recommendations`
* `POST /api/recommendations/{id}/feedback` with `{"feedback":"HELPFUL|NOT_HELPFUL|NOT_INTERESTED"}`

Recommendations use the authenticated user's flight destinations and booking history, score repeated destinations more highly, and include an explanation. Feedback adjusts persisted ranking or suppresses a recommendation.

## Database and migration

Bookings are now stored in the standalone `bookings` collection. Startup migration copies legacy embedded `users.bookings` into standalone documents using deterministic IDs, avoids matching duplicates, and links the embedded summary to the standalone ID and status. Legacy embedded data is retained because it is included in existing user responses. New bookings and booking status/total changes update the compatibility summary; the standalone collection remains authoritative. Other typed collections include refunds, reviews/replies/flags/votes, flight statuses/tracking/notifications, seats, rooms, seat/room preferences, pricing rules/history/freezes, and recommendations. Relevant user, target, status, resource, and uniqueness indexes are declared on the models.

Set `MONGODB_URI` to connect to MongoDB; default is `mongodb://localhost:27017/makemytrip`.

## Known limitations

The booking contract has no travel/check-in dates, so legacy booking creation cannot populate them or calculate a multi-night hotel total. Reviews are returned as model objects rather than dedicated DTOs. Automated tests exercise the core services and selected edge cases; they do not provide full HTTP/security integration coverage for every endpoint. MongoDB multi-document transactions are not enabled; atomic conditional updates protect individual inventory claims and cancellation state, with limited compensation for booking persistence failures.
