import { http, setApiCredentials } from "./client";

const safeUser = (user) => {
  if (!user || typeof user !== "object") return user;
  const safe = { ...user };
  delete safe.password;
  return safe;
};

export const login = async (email, password) => {
  const response = await http.post("/user/login", { email, password });
  setApiCredentials(email, password);
  return safeUser(response.data);
};

export const signup = async (firstName, lastName, email, phoneNumber, password, adminSignupToken = "") => {
  const response = await http.post("/user/signup", {
    firstName,
    lastName,
    email,
    phoneNumber,
    password,
    adminSignupToken,
  });
  setApiCredentials(email, password);
  return safeUser(response.data);
};

export const getuserbyemail = async (email) => {
  const response = await http.get("/user/email", { params: { email } });
  return safeUser(response.data);
};

export const editprofile = async (id, firstName, lastName, email, phoneNumber) => {
  const response = await http.post(`/user/edit`, {
    firstName,
    lastName,
    email,
    phoneNumber,
  }, { params: { id } });
  return safeUser(response.data);
};

export const getflight = async () => (await http.get("/flight")).data;

export const addflight = async (
  flightName, from, to, departureTime, arrivalTime, price, availableSeats
) => (await http.post("/admin/flight", {
  flightName, from, to, departureTime, arrivalTime, price, availableSeats,
})).data;

export const editflight = async (
  id, flightName, from, to, departureTime, arrivalTime, price, availableSeats
) => (await http.put(`/admin/flight/${id}`, {
  flightName, from, to, departureTime, arrivalTime, price, availableSeats,
})).data;

export const deleteflight = async (id) => http.delete(`/admin/flight/${id}`);

export const gethotel = async () => (await http.get("/hotel")).data;

export const addhotel = async (hotelName, location, pricePerNight, availableRooms, amenities) =>
  (await http.post("/admin/hotel", {
    hotelName, location, pricePerNight, availableRooms, amenities,
  })).data;

export const edithotel = async (id, hotelName, location, pricePerNight, availableRooms, amenities) =>
  (await http.put(`/admin/hotel/${id}`, {
    hotelName, location, pricePerNight, availableRooms, amenities,
  })).data;

export const deletehotel = async (id) => http.delete(`/admin/hotel/${id}`);

export const createTravelOption = async (option) => (await http.post("/admin/travel/options", option)).data;
export const updateTravelOption = async (id, option) => (await http.put(`/admin/travel/options/${id}`, option)).data;
export const deleteTravelOption = async (id) => http.delete(`/admin/travel/options/${id}`);

export const handleflightbooking = async (userId, flightId, seats, price, paymentReference) =>
  (await http.post("/booking/flight", null, {
    params: { userId, flightId, seats, price, paymentReference },
  })).data;

export const handlehotelbooking = async (userId, hotelId, rooms, price, paymentReference) =>
  (await http.post("/booking/hotel", null, {
    params: { userId, hotelId, rooms, price, paymentReference },
  })).data;

export const getTravelOptions = async (type) =>
  (await http.get("/travel/options", { params: { type: String(type).toUpperCase() } })).data;

export const handleTravelServiceBooking = async (userId, serviceOptionId, quantity, price, paymentReference) =>
  (await http.post("/booking/service", null, {
    params: { userId, serviceOptionId, quantity, price, paymentReference },
  })).data;
