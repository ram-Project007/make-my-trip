"use client";
import { useParams, useRouter } from "next/navigation";

import {
  Plane,
  Luggage,
  Clock,
  Calendar,
  MapPin,
  Gift,
  CreditCard,
  AlertCircle,
  ChevronRight,
  Star,
  Info,
  ArrowRight,
} from "lucide-react";
import { useEffect, useState } from "react";
import { getflight, handleflightbooking } from "@/api";
import { getCurrentPrice } from "@/api/features";
import { useDispatch, useSelector } from "react-redux";
interface Flight {
  id: string; // Unique identifier for the flight
  _id?: string;
  flightName: string; // Name of the flight
  from: string; // Departure location
  to: string; // Arrival location
  departureTime: string; // Departure time (ISO 8601 string recommended)
  arrivalTime: string; // Arrival time (ISO 8601 string recommended)
  price: number; // Price of the flight
  availableSeats: number; // Number of available seats
  flightNumber?: string;
  aircraft?: string;
  departureTerminal?: string;
  arrivalTerminal?: string;
  seatClass?: string;
  cabinBaggage?: string;
  checkInBaggage?: string;
}
type SessionUser = { id?: string; bookings?: unknown[]; role?: string };
type RootState = { user: { user: SessionUser | null } };
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Users, Ticket } from "lucide-react";
import SignupDialog from "@/components/SignupDialog";
import Loader from "@/components/Loader";
import { setUser } from "@/store";
const BookFlightPage = () => {
  const router = useRouter();
  const params = useParams<{ id: string }>();
  const id = params?.id ?? "";
  const [flights, setFlights] = useState<Flight[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentPrice, setCurrentPrice] = useState<number | null>(null);
  const [priceError, setPriceError] = useState(false);
  const [quantity, setQuantity] = useState(1);
  const [open, setopem] = useState(false);
  const [paymentBooking, setPaymentBooking] = useState<{id:string;totalPrice:number}|null>(null);
  const [bookingError, setBookingError] = useState("");
  const [submittingBooking, setSubmittingBooking] = useState(false);
  const user = useSelector((state: RootState) => state.user.user);
  const dispatch = useDispatch();
  useEffect(() => {
    const fetchFlights = async () => {
      try {
        const data = await getflight();
        const filteredData = (data as Flight[]).filter((flight) => flight.id === id || flight._id === id);
        setFlights(filteredData);
        if (filteredData[0]) {
          try {
            const quote = await getCurrentPrice("FLIGHT", id);
            const value = quote.currentPrice ?? quote.calculatedPrice;
            setCurrentPrice(value == null ? null : Number(value));
          } catch {
            setPriceError(true);
          }
        }
      } catch (error) {
        console.error("Error fetching flights:", error);
      } finally {
        setLoading(false);
      }
    };
    fetchFlights();
  }, [id, user]);

  if (loading) {
    return <Loader />;
  }
  if (flights.length === 0) {
    return <div>No flight data available for this ID.</div>;
  }
  const flight = flights[0];
  const durationMs = Date.parse(flight?.arrivalTime) - Date.parse(flight?.departureTime);
  const duration = Number.isFinite(durationMs) && durationMs >= 0
    ? `${Math.floor(durationMs / 3600000)}h ${Math.floor((durationMs % 3600000) / 60000)}m`
    : "Not provided";
  const flightDetails = {
    flightNo: flight?.flightNumber || flight?.flightName || "Flight number unavailable",
    aircraft: flight?.aircraft || "Aircraft information unavailable",
    departureTerminal: flight?.departureTerminal || "Terminal information unavailable",
    arrivalTerminal: flight?.arrivalTerminal || "Terminal information unavailable",
    seatClass: flight?.seatClass || "Cabin information unavailable",
    duration,
    cabinBaggage: flight?.cabinBaggage || "Not provided by the airline",
    checkInBaggage: flight?.checkInBaggage || "Not provided by the airline",
  };

  const formatDate = (dateString: string): string => {
    const options: Intl.DateTimeFormatOptions = {
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    };
    const date = new Date(dateString);
    return date.toLocaleString("en-US", options);
  };

  const handleQuantityChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    e.preventDefault();
    const value = Number.parseInt(e.target.value);
    setQuantity(
      isNaN(value) ? 1 : Math.max(1, Math.min(value, flight.availableSeats))
    );
  };

  const unitPrice = currentPrice ?? Number(flight?.price || 0);
  const totalPrice = unitPrice * quantity;
  const grandTotal = totalPrice;
  const promoOffers: { code: string; description: string }[] = [];

  const handlebooking = async (e?: React.FormEvent) => {
    e?.preventDefault();
    setSubmittingBooking(true);
    setBookingError("");
    try {
      const data = await handleflightbooking(user?.id, flight?.id, quantity, grandTotal, "");
      dispatch(setUser({ ...user, bookings: [...(user?.bookings || []).filter((item: any) => item?.id !== data.id), data] }));
      setPaymentBooking(data);
    } catch (error) {
      console.error("Error creating flight booking:", error);
      setBookingError("We couldn't create the booking. Please try again.");
    } finally {
      setSubmittingBooking(false);
    }
  };
  const bookingContent = () => (
    <DialogContent className="sm:max-w-[600px] bg-white">
      <DialogHeader>
        <DialogTitle className="text-2xl font-bold flex items-center">
          <Plane className="w-6 h-6 mr-2" />
          Flight Booking Details
        </DialogTitle>
      </DialogHeader>
      <div className="grid gap-6 mt-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="flightName" className="flex items-center">
              <Plane className="w-4 h-4 mr-2" />
              Flight Name
            </Label>
            <Input id="flightName" value={flight?.flightName} readOnly />
          </div>
          <div className="space-y-2">
            <Label htmlFor="from" className="flex items-center">
              <MapPin className="w-4 h-4 mr-2" />
              From
            </Label>
            <Input id="from" value={flight?.from} readOnly />
          </div>
          <div className="space-y-2">
            <Label htmlFor="to" className="flex items-center">
              <MapPin className="w-4 h-4 mr-2" />
              To
            </Label>
            <Input id="to" value={flight?.to} readOnly />
          </div>
          <div className="space-y-2">
            <Label htmlFor="departureTime" className="flex items-center">
              <Calendar className="w-4 h-4 mr-2" />
              Departure Time
            </Label>
            <Input
              id="departureTime"
              value={new Date(flight.departureTime).toLocaleString()}
              readOnly
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="arrivalTime" className="flex items-center">
              <Clock className="w-4 h-4 mr-2" />
              Arrival Time
            </Label>
            <Input
              id="arrivalTime"
              value={new Date(flight.arrivalTime).toLocaleString()}
              readOnly
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="quantity" className="flex items-center">
              <Ticket className="w-4 h-4 mr-2" />
              Number of Tickets
            </Label>
            <Input
              id="quantity"
              type="number"
              min="1"
              max={flight.availableSeats}
              value={quantity}
              onChange={handleQuantityChange}
            />
          </div>
        </div>
        <div className="bg-gray-100 rounded-lg p-4">
          <h3 className="text-lg font-bold mb-4 flex items-center">
            <CreditCard className="w-5 h-5 mr-2" />
            Fare Summary
          </h3>
          <div className="space-y-2">
            <div className="flex justify-between items-center">
              <span className="text-gray-600">{currentPrice == null ? "Listed fare" : "Current backend fare"}</span>
              <span className="font-medium">
                ₹ {totalPrice.toLocaleString()}
              </span>
            </div>
            <p className="text-xs text-gray-500">Final booking amount is validated by the backend.</p>
            {priceError&&<p role="alert" className="text-sm text-amber-800">Live pricing is unavailable. The displayed catalog fare is not a confirmed quote.</p>}
            <div className="border-t pt-2 mt-2">
              <div className="flex justify-between items-center">
                <span className="font-bold text-lg">Total Amount</span>
                <span className="font-bold text-lg">
                  ₹ {grandTotal.toLocaleString()}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
      {bookingError && <p role="alert" className="mt-3 text-sm text-red-600">{bookingError}</p>}
      {paymentBooking ? <div className="mt-4 rounded-xl border border-green-200 bg-green-50 p-5 text-center space-y-2"><h3 className="text-lg font-semibold">Booking request submitted</h3><p>This request is pending confirmation. No payment was taken.</p><p className="text-xs text-gray-600">Booking ID: {paymentBooking.id}</p></div> : <Button className="w-full mt-4" onClick={handlebooking} disabled={submittingBooking}>{submittingBooking ? "Submitting request…" : "Submit booking request"}</Button>}
    </DialogContent>
  );
  return (
    <div className="min-h-screen bg-[#f4f7fa]">
      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-6">
            {/* Flight Details */}
            <div className="bg-white rounded-xl shadow-sm p-6">
              <div className="flex flex-wrap justify-between items-start gap-4 mb-6">
                <div>
                  <div className="flex items-center flex-wrap gap-4 mb-2">
                    <h2 className="text-lg font-bold flex items-center">
                      <span>{flight?.from}</span>
                      <ArrowRight className="w-5 h-5 mx-2" />
                      <span>{flight?.to}</span>
                    </h2>
                    <span className="bg-green-100 text-green-600 text-xs px-3 py-1 rounded-full font-medium">
                      Cancellation details in My Bookings
                    </span>
                  </div>
                  <div className="flex items-center text-sm text-gray-600">
                    <Calendar className="w-4 h-4 mr-2" />
                    <span>{formatDate(flight.departureTime)}</span>
                    <span className="mx-2">•</span>
                    <Clock className="w-4 h-4 mr-2" />
                    <span>{flightDetails.duration}</span>
                  </div>
                </div>
                <button className="text-blue-600 text-sm font-medium hover:text-blue-700 flex items-center">
                  <Info className="w-4 h-4 mr-1" />
                  View Fare Rules
                </button>
              </div>

              <div className="flex items-center space-x-4 mb-6">
                <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                  <Plane className="w-6 h-6 text-blue-600" />
                </div>
                <div>
                  <div className="font-semibold">{flight.flightName}</div>
                  <div className="text-sm text-gray-600">
                    {flightDetails.flightNo} • {flightDetails.aircraft}
                  </div>
                </div>
                <div className="ml-auto text-sm">
                  <span className="px-3 py-1 bg-blue-50 text-blue-600 rounded-full">
                    {flightDetails.seatClass}
                  </span>
                </div>
              </div>

              <div className="flex flex-wrap md:flex-nowrap justify-between items-start gap-6 border-t pt-6">
                <div>
                  <div className="text-2xl font-bold">
                    {formatDate(flight.departureTime)}
                  </div>
                  <div className="text-sm text-gray-600 mt-1 flex items-start">
                    <MapPin className="w-4 h-4 mr-1 flex-shrink-0 mt-0.5" />
                    {flight.from} Airport, {flightDetails.departureTerminal}
                  </div>
                </div>
                <div className="text-center flex-shrink-0">
                  <div className="text-sm text-gray-600 mb-1">
                    {flightDetails.duration}
                  </div>
                  <div className="w-32 h-0.5 bg-gray-300 relative my-2">
                    <div className="absolute -top-2 right-0 w-4 h-4 rounded-full bg-gray-300 flex items-center justify-center">
                      <Plane className="w-3 h-3 text-gray-600" />
                    </div>
                  </div>
                  <div className="text-xs text-gray-500">Flight route</div>
                </div>
                <div className="text-right">
                  <div className="text-2xl font-bold">
                    {formatDate(flight.arrivalTime)}
                  </div>
                  <div className="text-sm text-gray-600 mt-1 flex items-start justify-end">
                    <MapPin className="w-4 h-4 mr-1 flex-shrink-0 mt-0.5" />
                    {flight.to} Airport, {flightDetails.arrivalTerminal}
                  </div>
                </div>
              </div>

              <div className="flex flex-wrap gap-6 mt-6 text-sm text-gray-600">
                <div className="flex items-center">
                  <Luggage className="w-5 h-5 mr-2 text-gray-500" />
                  <span>Cabin Baggage: {flightDetails.cabinBaggage}</span>
                </div>
                <div className="flex items-center">
                  <Luggage className="w-5 h-5 mr-2 text-gray-500" />
                  <span>Check-in Baggage: {flightDetails.checkInBaggage}</span>
                </div>
              </div>
            </div>

            {/* Cancellation Policy */}
            <div className="bg-white rounded-xl shadow-sm p-6">
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-lg font-bold flex items-center">
                  <AlertCircle className="w-5 h-5 mr-2 text-orange-500" />
                  Manage Cancellation & Refund
                </h2>
                <button className="text-blue-600 text-sm font-medium hover:text-blue-700" onClick={() => router.push("/profile")}>
                  My bookings
                </button>
              </div>
              <div className="bg-gray-50 p-6 rounded-xl">
                <p className="text-gray-700">Manage cancellation from My Bookings. The backend determines cancellation eligibility and calculates the refund.</p>
                <p className="mt-2 text-sm text-gray-500">Refund amount and status appear after the backend processes your cancellation.</p>
              </div>
            </div>

            <div className="bg-white rounded-xl shadow-sm p-6">
              <h2 className="text-lg font-bold">Personalized travel recommendations</h2>
              <p className="mt-2 text-sm text-gray-600">View recommendations generated from your account history and preferences.</p>
              <Button className="mt-4" variant="outline" onClick={() => router.push("/profile")}>View recommendations</Button>
            </div>
          </div>

          {/* Fare Summary */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-xl shadow-sm p-6 sticky top-24">
              <h2 className="text-lg font-bold mb-6 flex items-center">
                <CreditCard className="w-5 h-5 mr-2 text-gray-600" />
                Fare Summary
              </h2>
              <div className="space-y-2">
                <div className="flex justify-between items-center">
                  <span className="text-gray-600">{currentPrice == null ? "Listed fare" : "Current backend fare"}</span>
                  <span className="font-medium">
                    ₹ {totalPrice.toLocaleString()}
                  </span>
                </div>
                <p className="text-xs text-gray-500">Final booking amount is validated by the backend.</p>
                {priceError&&<p role="alert" className="text-sm text-amber-800">Live pricing is unavailable. The displayed catalog fare is not a confirmed quote.</p>}
                <div className="border-t pt-2 mt-2">
                  <div className="flex justify-between items-center">
                    <span className="font-bold text-lg">Total Amount</span>
                    <span className="font-bold text-lg">
                      ₹ {grandTotal.toLocaleString()}
                    </span>
                  </div>
                </div>
              </div>
              <Dialog open={open} onOpenChange={setopem}>
                <DialogTrigger asChild>
                  <Button className="w-full bg-red-600 text-white">
                    Book Now
                  </Button>
                </DialogTrigger>
                {user ? (
                  bookingContent()
                ) : (
                  <DialogContent className="bg-white">
                    <DialogHeader>
                      <DialogTitle>Login Required</DialogTitle>
                    </DialogHeader>
                    <p>Please log in to continue with your booking.</p>
                    <SignupDialog
                      trigger={
                        <Button className="w-full">Log In / Sign Up</Button>
                      }
                    />
                  </DialogContent>
                )}
              </Dialog>
              {/* Promo Codes */}
              <div className="mt-8">
                <div className="bg-[#FFF8E7] p-6 rounded-xl">
                  <h3 className="font-bold mb-4 flex items-center">
                    <Gift className="w-5 h-5 mr-2 text-yellow-600" />
                    PROMO CODES
                  </h3>
                  <div className="relative mb-4">
                    <input
                      type="text"
                      placeholder="No backend promotional offers available"
                      disabled
                      className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-colors"
                    />
                  </div>
                  {promoOffers.length === 0 && <p className="text-sm text-gray-600">No promotional offers available.</p>}
                  {promoOffers.map((offer, index) => (
                    <div
                      key={index}
                      className="bg-white p-4 rounded-lg mb-3 shadow-sm"
                    >
                      <div className="flex items-start gap-3">
                        <input
                          type="radio"
                          name="promo"
                          className="mt-1.5 h-4 w-4 text-red-600 focus:ring-red-500"
                        />
                        <div>
                          <div className="font-semibold text-red-600">
                            {offer.code}
                          </div>
                          <p className="text-sm text-gray-600 mt-1">
                            {offer.description}
                          </p>
                          <button className="text-blue-600 text-sm font-medium mt-2 hover:text-blue-700">
                            Terms & Conditions
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BookFlightPage;
