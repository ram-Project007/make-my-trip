package com.makemytrip.makemytrip.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.makemytrip.makemytrip.models.Booking;
import com.makemytrip.makemytrip.repositories.BookingRepository;
import com.paytm.pg.merchant.PaytmChecksum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class PaytmDynamicQrService {
    private static final Logger log = LoggerFactory.getLogger(PaytmDynamicQrService.class);
    public record QrResult(String orderId, String image, Instant expiresAt) {}
    public record Verification(String status, String transactionId, Booking booking) {}

    private final BookingRepository bookings;
    private final BookingPaymentService paymentService;
    private final ObjectMapper mapper;
    private final RestTemplate http = new RestTemplate();

    @Value("${paytm.mid:}") private String mid;
    @Value("${paytm.merchant-key:}") private String merchantKey;
    @Value("${paytm.pos-id:}") private String posId;
    @Value("${paytm.qr-url:https://securegw.paytm.in/paymentservices/qr/create}") private String qrUrl;
    @Value("${paytm.status-url:https://securegw.paytm.in/v3/order/status}") private String statusUrl;

    public PaytmDynamicQrService(BookingRepository bookings, BookingPaymentService paymentService, ObjectMapper mapper) {
        this.bookings = bookings;
        this.paymentService = paymentService;
        this.mapper = mapper;
    }

    public QrResult createQr(String bookingId, String userId) {
        Booking booking = ownedPendingBooking(bookingId, userId);
        if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This booking has already been paid");
        }
        requireConfiguration();
        if (booking.getPaytmOrderId() != null && booking.getPaytmQrImage() != null
                && booking.getPaytmQrExpiresAt() != null && booking.getPaytmQrExpiresAt().isAfter(Instant.now())) {
            return new QrResult(booking.getPaytmOrderId(), booking.getPaytmQrImage(), booking.getPaytmQrExpiresAt());
        }

        // One stable order ID per booking makes QR retries idempotent and prevents duplicate orders.
        String orderId = booking.getPaytmOrderId();
        if (orderId == null || orderId.isBlank()) {
            orderId = "MMT-" + UUID.nameUUIDFromBytes((mid + ":" + bookingId).getBytes(java.nio.charset.StandardCharsets.UTF_8))
                    .toString().replace("-", "");
            booking.setPaytmOrderId(orderId);
            bookings.save(booking);
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mid", mid);
        body.put("orderId", orderId);
        body.put("amount", BigDecimal.valueOf(booking.getTotalPrice()).setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
        body.put("businessType", "UPI_QR_CODE");
        body.put("posId", posId);
        body.put("orderDetails", "MakeMyTrip booking " + bookingId);
        body.put("imageRequired", true);
        Map<String, Object> head = new LinkedHashMap<>();
        head.put("version", "v1");
        head.put("signature", signature(body));
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("body", body);
        request.put("head", head);

        JsonNode response = post(qrUrl, request);
        JsonNode responseBody = response.path("body");
        verifyResponseSignature(responseBody, response.path("head").path("signature").asText());
        if (!"SUCCESS".equals(responseBody.path("resultInfo").path("resultStatus").asText())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Paytm could not create the payment QR");
        }
        String image = responseBody.path("image").asText("");
        if (image.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Paytm returned no QR image");

        Instant expiresAt = Instant.now().plusSeconds(600);
        booking.setPaytmQrImage(image);
        booking.setPaytmQrExpiresAt(expiresAt);
        bookings.save(booking);
        return new QrResult(orderId, image, expiresAt);
    }

    public Verification verify(String bookingId, String userId) {
        Booking booking = ownedPendingBooking(bookingId, userId);
        if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID) {
            return new Verification("PAID", booking.getPaymentReference(), booking);
        }
        if (booking.getPaytmOrderId() == null || booking.getPaytmOrderId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Create the payment QR before checking payment status");
        }
        requireConfiguration();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("mid", mid);
        body.put("orderId", booking.getPaytmOrderId());
        Map<String, Object> head = new LinkedHashMap<>();
        head.put("signature", signature(body));
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("body", body);
        request.put("head", head);
        JsonNode response = post(statusUrl, request);
        JsonNode responseBody = response.path("body");
        verifyResponseSignature(responseBody, response.path("head").path("signature").asText());

        String status = responseBody.path("resultInfo").path("resultStatus").asText("PENDING");
        if (!mid.equals(responseBody.path("mid").asText())
                || !booking.getPaytmOrderId().equals(responseBody.path("orderId").asText())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Paytm status did not match this booking");
        }
        String transactionId = responseBody.path("txnId").asText("");
        if ("TXN_SUCCESS".equals(status)) {
            BigDecimal paid = new BigDecimal(responseBody.path("txnAmount").asText("0.00"));
            BigDecimal expected = BigDecimal.valueOf(booking.getTotalPrice()).setScale(2, java.math.RoundingMode.HALF_UP);
            if (paid.compareTo(expected) != 0 || transactionId.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Paytm payment amount did not match this booking");
            }
            booking = paymentService.confirmProviderPayment(bookingId, transactionId);
            status = "PAID";
        } else if ("TXN_FAILURE".equals(status) || "NO_RECORD_FOUND".equals(status)) {
            status = "PENDING"; // Keep inventory reserved; Paytm may report delayed bank settlement.
        } else {
            status = "PENDING";
        }
        return new Verification(status, transactionId.isBlank() ? null : transactionId, booking);
    }

    /** Reconciles Paytm orders even when a customer closes the checkout page. */
    @Scheduled(fixedDelayString = "${paytm.status-poll-interval-ms:30000}")
    public void reconcilePendingOrders() {
        if (mid.isBlank() || merchantKey.isBlank()) return;
        for (Booking booking : bookings.findByPaymentStatus(Booking.PaymentStatus.PENDING_VERIFICATION)) {
            if (booking.getPaytmOrderId() == null || booking.getPaytmOrderId().isBlank()) continue;
            try {
                verify(booking.getId(), booking.getUserId());
            } catch (RuntimeException ex) {
                log.warn("Paytm status reconciliation failed for booking {}", booking.getId());
            }
        }
    }

    private Booking ownedPendingBooking(String bookingId, String userId) {
        Booking booking = bookings.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!userId.equals(booking.getUserId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID) return booking;
        if (booking.getPaymentStatus() != Booking.PaymentStatus.PENDING_VERIFICATION) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking is not awaiting payment");
        }
        return booking;
    }

    private JsonNode post(String url, Map<String, Object> request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String json = mapper.writeValueAsString(request);
            String response = http.postForObject(url,
                    new HttpEntity<>(json, headers), String.class);
            return mapper.readTree(response);
        } catch (RestClientException | java.io.IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not contact Paytm", ex);
        }
    }

    private String signature(Object body) {
        try {
            return PaytmChecksum.generateSignature(mapper.writeValueAsString(body), merchantKey);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not sign Paytm request", ex);
        }
    }

    private void verifyResponseSignature(JsonNode body, String checksum) {
        try {
            if (checksum.isBlank() || !PaytmChecksum.verifySignature(mapper.writeValueAsString(body), merchantKey, checksum)) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Paytm response signature was invalid");
            }
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not verify Paytm response", ex);
        }
    }

    private void requireConfiguration() {
        if (mid.isBlank() || merchantKey.isBlank() || posId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Paytm Dynamic QR is not configured on the server");
        }
    }
}
