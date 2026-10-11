"use client";
import { useParams, useRouter } from "next/navigation";
import Link from "next/link";
import {
  Star,
  MapPin,
  School as Pool,
  UtensilsCrossed,
  Wine,
  Power,
  ChevronRight,
  Camera,
  Image,
  CreditCard,
  Ticket,
  Plane,
  Home,
} from "lucide-react";
import { useEffect, useState } from "react";
import { gethotel, handlehotelbooking } from "@/api";
import { getCurrentPrice } from "@/api/features";
interface Hotel {
  id: string; // Unique identifier for the hotel
  _id?: string;
  hotelName: string; // Name of the hotel
  location: string; // Location of the hotel
  pricePerNight: number; // Price per night
  availableRooms: number; // Number of available rooms
  amenities: string; // Amenities provided (comma-separated string or change to string[])
  images?: string[];
  imageUrl?: string;
  averageRating?: number;
  rating?: number;
  reviewCount?: number;
  guestPhotoCount?: number;
  description?: string;
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
import { useDispatch, useSelector } from "react-redux";
import SignupDialog from "@/components/SignupDialog";
import Loader from "@/components/Loader";
import { setUser } from "@/store";
const BookHotelPage = () => {
  const [quantity, setQuantity] = useState(1);
  const router = useRouter();
  const params = useParams<{ id: string }>();
  const id = params?.id ?? "";
  const [hotels, sethotels] = useState<Hotel[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentPrice, setCurrentPrice] = useState<number | null>(null);
  const [priceError, setPriceError] = useState(false);
  const user = useSelector((state: RootState) => state.user.user);
  const [open, setopem] = useState(false);
  const [paymentBooking, setPaymentBooking] = useState<{id:string;totalPrice:number}|null>(null);
  const [bookingError, setBookingError] = useState("");
  const [submittingBooking, setSubmittingBooking] = useState(false);
  const dispatch = useDispatch();
  useEffect(() => {
    const fetchhotels = async () => {
      try {
        const data = await gethotel();
        const filteredData = (data as Hotel[]).filter((hotel) => hotel.id === id || hotel._id === id);
        sethotels(filteredData);
        if (filteredData[0]) {
          try {
            const quote = await getCurrentPrice("HOTEL", id);
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
    fetchhotels();
  }, []);

  if (loading) {
    return <Loader />;
  }
  const hotel = hotels[0];
  const amenityNames = Array.isArray(hotel?.amenities)
    ? hotel.amenities
    : typeof hotel?.amenities === "string"
      ? hotel.amenities.split(",").map((item: string) => item.trim()).filter(Boolean)
      : [];
  const hotelImages = Array.isArray(hotel?.images) ? hotel.images : hotel?.imageUrl ? [hotel.imageUrl] : [];
  const rating = Math.max(0, Math.min(5, Number(hotel?.averageRating ?? hotel?.rating ?? 0)));
  const reviewCount = Number(hotel?.reviewCount ?? 0);
  const hotelData = {
    rating,
    maxRating: 5,
    propertyPhotos: hotelImages.length,
    guestPhotos: Number(hotel?.guestPhotoCount ?? 0),
    description: hotel?.description || "Property details have not been provided.",
    amenities: amenityNames.map((name: string) => ({ icon: <Home className="w-5 h-5" />, name })),
    room: {
      type: "Room selection",
      capacity: `${hotel?.availableRooms ?? 0} rooms currently available`,
      features: ["Room options are available after booking confirmation."],
      originalPrice: hotel?.pricePerNight ?? 0,
      discountedPrice: 0,
      taxes: 0,
    },
    reviews: {
      rating,
      count: reviewCount,
      text: reviewCount ? "Guest rating" : "No ratings yet",
    },
  };
  const handleQuantityChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    e.preventDefault();
    const value = Number.parseInt(e.target.value);
    setQuantity(
      isNaN(value) ? 1 : Math.max(1, Math.min(value, hotel.availableRooms))
    );
  };

  const unitPrice = currentPrice ?? Number(hotel?.pricePerNight || 0);
  const totalPrice = unitPrice * quantity;
  const grandTotal = totalPrice;
  const handlebooking = async () => {
    setSubmittingBooking(true);
    setBookingError("");
    try {
      const data = await handlehotelbooking(user?.id, hotel?.id, quantity, grandTotal, "");
      dispatch(setUser({ ...user, bookings: [...(user?.bookings || []).filter((item: any) => item?.id !== data.id), data] }));
      setPaymentBooking(data);
    } catch (error) {
      console.error("Error creating hotel booking:", error);
      setBookingError("We couldn't submit the booking request. Please try again.");
    } finally {
      setSubmittingBooking(false);
    }
  };
  const hotelContent = () => (
    <DialogContent className="sm:max-w-[600px] bg-white">
      <DialogHeader>
        <DialogTitle className="text-2xl font-bold flex items-center">
          <Home className="w-6 h-6 mr-2" />
          Hotel Booking Details
        </DialogTitle>
      </DialogHeader>
      <div className="grid gap-6 mt-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Hotel Name */}
          <div className="space-y-2">
            <Label htmlFor="hotelName" className="flex items-center">
              <MapPin className="w-4 h-4 mr-2" />
              Hotel Name
            </Label>
            <Input id="hotelName" value={hotel.hotelName} readOnly />
          </div>

          {/* Location */}
          <div className="space-y-2">
            <Label htmlFor="location" className="flex items-center">
              <MapPin className="w-4 h-4 mr-2" />
              Location
            </Label>
            <Input id="location" value={hotel.location} readOnly />
          </div>
          {/* Price Per Night */}
          <div className="space-y-2">
            <Label htmlFor="pricePerNight" className="flex items-center">
              <Ticket className="w-4 h-4 mr-2" />
              Price Per Night
            </Label>
            <Input
              id="pricePerNight"
              value={`₹ ${hotel.pricePerNight}`}
              readOnly
            />
          </div>

          {/* Available Rooms */}
          <div className="space-y-2">
            <Label htmlFor="availableRooms" className="flex items-center">
              <Ticket className="w-4 h-4 mr-2" />
              Available Rooms
            </Label>
            <Input id="availableRooms" value={hotel.availableRooms} readOnly />
          </div>

          {/* Number of Rooms to Book */}
          <div className="space-y-2">
            <Label htmlFor="quantity" className="flex items-center">
              <Ticket className="w-4 h-4 mr-2" />
              Number of Rooms to Book
            </Label>
            <Input
              id="quantity"
              type="number"
              min="1"
              max={hotel.availableRooms}
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
              <span className="text-gray-600">{currentPrice == null ? "Listed price" : "Current backend price"}</span>
              <span className="font-medium">
                ₹ {totalPrice.toLocaleString()}
              </span>
            </div>
            <p className="text-xs text-gray-500">Final booking amount is validated by the backend.</p>
            {priceError&&<p role="alert" className="text-sm text-amber-800">Live pricing is unavailable. The displayed catalog price is not a confirmed quote.</p>}
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
    <div className="min-h-screen bg-gray-50">
      {/* Breadcrumb */}
      <div className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 py-3">
          <div className="flex items-center space-x-2 text-sm">
            <Link href="/" className="text-blue-500">
              Home
            </Link>
            <ChevronRight className="w-4 h-4 text-gray-400" />
            <Link href="/" className="text-blue-500">
              {hotel?.location}
            </Link>
            <ChevronRight className="w-4 h-4 text-gray-400" />
            <span className="text-gray-600">{hotel?.hotelName}</span>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-6">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2">
            {/* Hotel Title & Rating */}
            <div className="mb-6">
              <h1 className="text-2xl font-bold mb-2">{hotel.hotelName}</h1>
              <div className="flex items-center space-x-1">
                {[...Array(hotelData.rating)].map((_, i) => (
                  <Star
                    key={i}
                    className="w-5 h-5 text-yellow-400 fill-current"
                  />
                ))}
                {[...Array(hotelData.maxRating - hotelData.rating)].map(
                  (_, i) => (
                    <Star key={i} className="w-5 h-5 text-gray-300" />
                  )
                )}
              </div>
            </div>

            {/* Image Gallery */}
            <div className="grid grid-cols-3 gap-4 mb-8">
              <div className="col-span-2 relative group cursor-pointer">
                <img
                  src={hotelImages[0] || "/file.svg"}
                  alt={hotel?.hotelName || "Hotel"}
                  className="w-full h-80 object-cover rounded-lg"
                />
                <div className="absolute bottom-4 left-4 bg-white/90 px-3 py-1 rounded-full flex items-center space-x-1">
                  <Camera className="w-4 h-4" />
                  <span className="text-sm">
                    +{hotelData.propertyPhotos} Property Photos
                  </span>
                </div>
              </div>
              <div className="space-y-4">
                <div className="relative group cursor-pointer">
                  <img
                    src={hotelImages[1] || "/file.svg"}
                    alt={hotel?.hotelName ? `${hotel.hotelName} room` : "Hotel room"}
                    className="w-full h-[152px] object-cover rounded-lg"
                  />
                </div>
                <div className="relative group cursor-pointer">
                  <img
                    src={hotelImages[2] || "/file.svg"}
                    alt={hotel?.hotelName ? `${hotel.hotelName} amenities` : "Hotel amenities"}
                    className="w-full h-[152px] object-cover rounded-lg"
                  />
                  <div className="absolute bottom-4 left-4 bg-white/90 px-3 py-1 rounded-full flex items-center space-x-1">
                    <Image className="w-4 h-4" />
                    <span className="text-sm">
                      +{hotelData.guestPhotos} Guest Photos
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Description */}
            <p className="text-gray-600 mb-6">
              {hotelData.description}
              <button className="text-blue-500 ml-2">Read more</button>
            </p>

            {/* Amenities */}
            <div className="mb-8">
              <h2 className="text-xl font-semibold mb-4">Amenities</h2>
              <div className="flex flex-wrap gap-6">
                {hotelData.amenities.map((amenity, index) => (
                  <div
                    key={index}
                    className="flex items-center space-x-2 text-gray-600"
                  >
                    {amenity.icon}
                    <span>{amenity.name}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Booking Card */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-xl shadow-lg p-6">
              <h3 className="text-xl font-semibold mb-4">
                {hotelData.room.type}
              </h3>
              <p className="text-gray-600 mb-4">{hotelData.room.capacity}</p>

              <ul className="space-y-3 mb-6">
                {hotelData.room.features.map((feature, index) => (
                  <li key={index} className="flex items-start space-x-2">
                    <span className="text-gray-400">•</span>
                    <span className="text-gray-600">{feature}</span>
                  </li>
                ))}
              </ul>
              <div className="mb-6">
                {/* Price Per Night */}
                <div className="flex items-center justify-between mb-2">
                  <span className="text-gray-800 font-semibold">
                    Price Per Night:
                  </span>
                  <span className="text-lg font-medium text-gray-800">
                    ₹ {unitPrice}
                  </span>
                </div>

                {/* Available Rooms */}
                <div className="flex items-center justify-between mb-4">
                  <span className="text-gray-800 font-semibold">
                    Available Rooms:
                  </span>
                  <span className="text-lg font-medium text-gray-800">
                    {hotel.availableRooms}
                  </span>
                </div>

                {/* Amenities */}
                <div>
                  <h4 className="text-gray-800 font-semibold mb-2">
                    Amenities:
                  </h4>
                  <p className="text-gray-600">{hotel.amenities}</p>
                </div>
              </div>
              <div className="space-y-2 mb-6">
                <div className="flex items-center justify-between">
                  <span className="text-gray-500">Per Night:</span>
                </div>
                <div className="flex items-center justify-between text-2xl font-bold">
                  <span>₹ {grandTotal}</span>
                  <span className="text-sm text-gray-500 font-normal">
                    Final amount confirmed by backend
                  </span>
                </div>
              </div>
              <Dialog open={open} onOpenChange={setopem}>
                <DialogTrigger asChild>
                  <button className="w-full bg-blue-500 text-white py-3 rounded-lg hover:bg-blue-600 transition-colors mb-3">
                    BOOK THIS NOW
                  </button>
                </DialogTrigger>
                {user ? (
                  hotelContent()
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
            </div>

            {/* Rating Card */}
            <div className="bg-white rounded-xl shadow-lg p-6 mt-6">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center space-x-4">
                  <div className="bg-blue-500 text-white text-2xl font-bold w-16 h-16 rounded-lg flex items-center justify-center">
                    {hotelData.reviews.rating}
                  </div>
                  <div>
                    <div className="font-semibold text-lg">
                      {hotelData.reviews.text}
                    </div>
                    <div className="text-gray-500">
                      ({hotelData.reviews.count} ratings)
                    </div>
                  </div>
                </div>
                <a href="#" className="text-blue-500">
                  All Reviews
                </a>
              </div>
            </div>

            {/* Location Card */}
            <div className="bg-white rounded-xl shadow-lg p-6 mt-6">
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="font-semibold text-lg mb-1">
                    {hotel.location}
                  </h3>
                </div>
                <button className="text-blue-500">See on Map</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BookHotelPage;
