import { http } from "./client";

const root = "/api";
export const getMyBookings = async () => (await http.get(`${root}/users/me/bookings`)).data;
export const cancelBooking = async (id, reason) =>
  (await http.post(`${root}/bookings/${id}/cancel`, { reason })).data;
export const getRefund = async (id) => (await http.get(`${root}/bookings/${id}/refund`)).data;
export const getMyRefunds = async () => (await http.get(`${root}/users/me/refunds`)).data;

export const listReviews = async (filters = {}) =>
  (await http.get(`${root}/reviews`, { params: filters })).data;
export const createReview = async (review) =>
  (await http.post(`${root}/reviews`, review)).data;
export const updateReview = async (id, review) =>
  (await http.put(`${root}/reviews/${id}`, review)).data;
export const deleteReview = async (id) => http.delete(`${root}/reviews/${id}`);
export const replyToReview = async (id, text) =>
  (await http.post(`${root}/reviews/${id}/reply`, { text })).data;
export const getReviewReplies = async (id) =>
  (await http.get(`${root}/reviews/${id}/replies`)).data;
export const helpfulReview = async (id) =>
  (await http.post(`${root}/reviews/${id}/helpful`)).data;
export const flagReview = async (id, reason) =>
  (await http.post(`${root}/reviews/${id}/flag`, { reason })).data;
export const getFlaggedReviews = async () => (await http.get(`${root}/admin/reviews/flagged`)).data;
export const moderateReview = async (id, status) =>
  (await http.put(`${root}/admin/reviews/${id}/moderate`, { status })).data;
export const getAdminRefunds = async () => (await http.get(`${root}/admin/refunds`)).data;
export const getPendingBookingPayments = async () => (await http.get("/admin/payments/pending")).data;
export const reviewBookingPayment = async (bookingId, paymentStatus) =>
  (await http.put(`/admin/payments/${bookingId}/review`, { paymentStatus })).data;
export const updateRefundStatus = async (id, status) =>
  (await http.put(`${root}/admin/refunds/${id}/status`, { status })).data;

export const getFlightStatus = async (id) => (await http.get(`${root}/flights/${id}/status`)).data;
export const getFlightStatusByNumber = async (flightNumber) =>
  (await http.get(`${root}/flights/status`, { params: { flightNumber } })).data;
export const trackFlight = async (id) => (await http.post(`${root}/flights/${id}/track`)).data;
export const untrackFlight = async (id) => http.delete(`${root}/flights/${id}/track`);
export const getTrackedFlights = async () => (await http.get(`${root}/users/me/tracked-flights`)).data;
export const getNotifications = async () => (await http.get(`${root}/users/me/notifications`)).data;
export const markNotificationRead = async (id) => (await http.put(`${root}/users/me/notifications/${id}/read`)).data;
export const markAllNotificationsRead = async () => http.put(`${root}/users/me/notifications/read`);

export const getSeats = async (flightId) => (await http.get(`${root}/flights/${flightId}/seats`)).data;
export const selectSeat = async (bookingId, seatNumber) =>
  (await http.post(`${root}/bookings/${bookingId}/seat`, { seatNumber })).data;
export const clearSeat = async (bookingId) => (await http.delete(`${root}/bookings/${bookingId}/seat`)).data;
export const getRooms = async (hotelId) => (await http.get(`${root}/hotels/${hotelId}/rooms`)).data;
export const selectRoom = async (bookingId, roomType) =>
  (await http.post(`${root}/bookings/${bookingId}/room`, { roomType })).data;

export const getCurrentPrice = async (type, id) =>
  (await http.get(`${root}/${type.toLowerCase()}s/${id}/current-price`)).data;
export const getPriceHistory = async (type, id) =>
  (await http.get(`${root}/${type.toLowerCase()}s/${id}/price-history`)).data;
export const createPriceFreeze = async (bookingId) =>
  (await http.post(`${root}/bookings/${bookingId}/price-freeze`)).data;
export const getPriceFreeze = async (id) => (await http.get(`${root}/price-freezes/${id}`)).data;
export const usePriceFreeze = async (id) => (await http.post(`${root}/price-freezes/${id}/use`)).data;

export const getRecommendations = async () => (await http.get(`${root}/recommendations`)).data;
export const sendRecommendationFeedback = async (id, feedback) =>
  (await http.post(`${root}/recommendations/${id}/feedback`, { feedback })).data;

export const getSeatPreference = async () => (await http.get(`${root}/users/me/preferences/seat`)).data;
export const saveSeatPreference = async (data) =>
  (await http.put(`${root}/users/me/preferences/seat`, data)).data;
export const deleteSeatPreference = async () => http.delete(`${root}/users/me/preferences/seat`);
export const getRoomPreference = async () => (await http.get(`${root}/users/me/preferences/room`)).data;
export const saveRoomPreference = async (data) =>
  (await http.put(`${root}/users/me/preferences/room`, data)).data;
export const deleteRoomPreference = async () => http.delete(`${root}/users/me/preferences/room`);

export function apiErrorMessage(error) {
  const status = error?.response?.status;
  if (status === 401) return "Your session is not authenticated. Please sign in again.";
  if (status === 403) return "You do not have permission to perform this action.";
  if (status === 404) return "The requested travel record could not be found.";
  if (status === 409) return error?.response?.data?.message || "This item is no longer available. Refresh and try again.";
  return error?.response?.data?.message || "We could not complete your request. Please try again.";
}
