package com.makemytrip.makemytrip;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.makemytrip.makemytrip.models.*;
import com.makemytrip.makemytrip.repositories.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import java.time.Instant;
import java.util.UUID;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired BookingRepository bookings;
    @Autowired RefundRepository refunds;
    @Autowired PriceFreezeRepository freezes;
    @Autowired ReviewRepository reviews;
    @Autowired ReviewReplyRepository replies;
    @Autowired RecommendationRepository recommendations;
    @Autowired FlightNotificationRepository notifications;
    @Autowired SeatPreferenceRepository seatPreferences;
    @Autowired RoomPreferenceRepository roomPreferences;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper json;
    private final String suffix=UUID.randomUUID().toString();
    private String emailA,emailB,password="TestPassword!123";
    private Users userA,userB;
    private Booking bookingB;
    private Refund refundB;
    private PriceFreeze freezeB;
    private Review reviewB;
    private ReviewReply replyB;
    private Recommendation recommendationB;
    private FlightNotification notificationB;

    @BeforeEach void seed(){
        emailA="a-"+suffix+"@example.test";emailB="b-"+suffix+"@example.test";
        userA=saveUser(emailA);userB=saveUser(emailB);
        bookingB=new Booking();bookingB.setUserId(userB.getId());bookingB.setBookingType(Booking.Type.FLIGHT);bookingB.setFlightId("flight-"+suffix);bookingB.setQuantity(1);bookingB.setTotalPrice(100);bookingB=bookings.save(bookingB);
        refundB=new Refund();refundB.setBookingId(bookingB.getId());refundB.setUserId(userB.getId());refundB.setOriginalAmount(100);refundB.setRefundAmount(50);refundB=refunds.save(refundB);
        freezeB=new PriceFreeze();freezeB.setBookingId(bookingB.getId());freezeB.setUserId(userB.getId());freezeB.setFrozenPrice(100);freezeB.setExpiresAt(Instant.now().plusSeconds(600));freezeB=freezes.save(freezeB);
        reviewB=new Review();reviewB.setUserId(userB.getId());reviewB.setBookingId(bookingB.getId());reviewB.setTargetType(Review.TargetType.FLIGHT);reviewB.setTargetId(bookingB.getFlightId());reviewB.setRating(5);reviewB.setText("Review by B");reviewB=reviews.save(reviewB);
        replyB=new ReviewReply();replyB.setReviewId(reviewB.getId());replyB.setUserId(userB.getId());replyB.setText("Reply by B");replyB=replies.save(replyB);
        recommendationB=new Recommendation();recommendationB.setUserId(userB.getId());recommendationB.setRecommendationType(Recommendation.Type.FLIGHT);recommendationB.setTargetId("target-"+suffix);recommendationB.setTitle("Flight");recommendationB.setReason("History match");recommendationB.setScore(2);recommendationB=recommendations.save(recommendationB);
        notificationB=new FlightNotification();notificationB.setUserId(userB.getId());notificationB.setFlightId(bookingB.getFlightId());notificationB.setType(FlightNotification.Type.DELAY);notificationB.setTitle("Delayed");notificationB.setMessage("Delayed");notificationB=notifications.save(notificationB);
    }
    private Users saveUser(String email){Users u=new Users();u.setEmail(email);u.setPassword(encoder.encode(password));u.setRole("USER");return users.save(u);}

    @Test void userCannotAccessAnotherUsersBookingRefundFreezeReviewReplyRecommendationOrNotifications() throws Exception {
        mvc.perform(get("/api/bookings/"+bookingB.getId()).with(httpBasic(emailA,password))).andExpect(status().isForbidden());
        mvc.perform(get("/api/bookings/"+bookingB.getId()+"/refund").with(httpBasic(emailA,password))).andExpect(status().isNotFound());
        mvc.perform(get("/api/price-freezes/"+freezeB.getId()).with(httpBasic(emailA,password))).andExpect(status().isForbidden());
        mvc.perform(put("/api/reviews/"+reviewB.getId()).with(httpBasic(emailA,password)).contentType(MediaType.APPLICATION_JSON).content("{\"rating\":4,\"text\":\"tamper\"}")).andExpect(status().isForbidden());
        mvc.perform(put("/api/reviews/"+reviewB.getId()+"/replies/"+replyB.getId()).with(httpBasic(emailA,password)).contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"tamper\"}")).andExpect(status().isForbidden());
        mvc.perform(post("/api/recommendations/"+recommendationB.getId()+"/feedback").with(httpBasic(emailA,password)).contentType(MediaType.APPLICATION_JSON).content("{\"feedback\":\"HELPFUL\"}")).andExpect(status().isNotFound());
        mvc.perform(get("/api/users/me/notifications").with(httpBasic(emailA,password))).andExpect(status().isOk()).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string("[]"));
        mvc.perform(put("/api/users/me/notifications/"+notificationB.getId()+"/read").with(httpBasic(emailA,password))).andExpect(status().isNotFound());
    }

    @Test void protectedRoutesRequireAuthenticationAndUsersCannotUseAdminRoutes() throws Exception {
        mvc.perform(get("/api/users/me/bookings")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/admin/refunds").with(httpBasic(emailA,password))).andExpect(status().isForbidden());
        mvc.perform(put("/api/admin/reviews/"+UUID.randomUUID()+"/moderate").with(httpBasic(emailA,password)).contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"APPROVED\"}")).andExpect(status().isForbidden());
    }

    @Test void seatAndRoomPreferencesAreOwnedByTheAuthenticatedUserAndCanBeUpdatedAndRemoved() throws Exception {
        mvc.perform(put("/api/users/me/preferences/seat").with(httpBasic(emailA,password)).contentType(MediaType.APPLICATION_JSON).content("{\"preferredClass\":\"ECONOMY\",\"preferredType\":\"WINDOW\",\"preferWindow\":true}")).andExpect(status().isOk());
        mvc.perform(get("/api/users/me/preferences/seat").with(httpBasic(emailA,password))).andExpect(status().isOk()).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.userId").value(userA.getId()));
        mvc.perform(get("/api/users/me/preferences/seat").with(httpBasic(emailB,password))).andExpect(status().isNotFound());
        mvc.perform(put("/api/users/me/preferences/room").with(httpBasic(emailA,password)).contentType(MediaType.APPLICATION_JSON).content("{\"preferredRoomType\":\"DELUXE\",\"minimumCapacity\":2,\"preferredAmenities\":[\"WIFI\"],\"preferPremium\":true}")).andExpect(status().isOk());
        mvc.perform(get("/api/users/me/preferences/room").with(httpBasic(emailA,password))).andExpect(status().isOk()).andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.preferredRoomType").value("DELUXE"));
        mvc.perform(delete("/api/users/me/preferences/seat").with(httpBasic(emailA,password))).andExpect(status().isNoContent());
        mvc.perform(get("/api/users/me/preferences/seat").with(httpBasic(emailA,password))).andExpect(status().isNotFound());
    }

    @AfterEach void cleanup(){if(bookingB!=null){bookings.deleteById(bookingB.getId());refunds.deleteById(refundB.getId());freezes.deleteById(freezeB.getId());reviews.deleteById(reviewB.getId());replies.deleteById(replyB.getId());recommendations.deleteById(recommendationB.getId());notifications.deleteById(notificationB.getId());}if(userA!=null)users.deleteById(userA.getId());if(userB!=null)users.deleteById(userB.getId());}
}
