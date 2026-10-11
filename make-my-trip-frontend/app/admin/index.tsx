"use client";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

import { Textarea } from "@/components/ui/textarea";
import FlightList from "@/components/Flights/Flightlist";
import {
  addflight,
  addhotel,
  editflight,
  edithotel,
  getuserbyemail,
} from "@/api";
import HotelList from "@/components/Hotel/Hotel";
import AdminInventory from "@/components/features/AdminInventory";
import { getPendingBookingPayments, reviewBookingPayment } from "@/api/features";
type AdminAuthState = { user: { user: { role?: string } | null } };

interface User {
  _id: string;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  phoneNumber: string;
}

function UserSearch() {
  const [email, setEmail] = useState("");
  const [user, setUser] = useState<User | null>(null);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    const data = await getuserbyemail(email);
    const mockUser: User = data;
    setUser(mockUser);
  };

  return (
    <div className="space-y-4">
      <form onSubmit={handleSearch} className="flex gap-2">
        <div className="flex-1">
          <Label htmlFor="email" className="sr-only">
            Email
          </Label>
          <Input
            id="email"
            type="email"
            placeholder="Search user by email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <Button type="submit">Search</Button>
      </form>
      {user && (
        <div className="border p-4 rounded-md">
          <h3 className="font-bold mb-2">User Details</h3>
          <p>
            <strong>Name:</strong> {user.firstName} {user.lastName}
          </p>
          <p>
            <strong>Email:</strong> {user.email}
          </p>
          <p>
            <strong>Role:</strong> {user.role}
          </p>
          <p>
            <strong>Phone:</strong> {user.phoneNumber}
          </p>
        </div>
      )}
    </div>
  );
}

interface Hotel {
  id?: string;
  _id?: string;
  hotelName: string;
  location: string;
  pricePerNight: number;
  availableRooms: number;
  amenities: string;
}

function AddEditHotel({ hotel, onSaved }: { hotel: Hotel | null; onSaved?: () => void }) {
  const [formData, setFormData] = useState<Hotel>(() => hotel ?? {
    hotelName: "", location: "", pricePerNight: 0, availableRooms: 0, amenities: "",
  });

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (hotel) {
      await edithotel(
        hotel.id ?? hotel._id,
        formData.hotelName,
        formData.location,
        formData.pricePerNight,
        formData.availableRooms,
        formData.amenities
      );
      onSaved?.();
      return;
    }
    await addhotel(
      formData.hotelName,
      formData.location,
      formData.pricePerNight,
      formData.availableRooms,
      formData.amenities
    );
    onSaved?.();
    if (!hotel) {
      setFormData({
        hotelName: "",
        location: "",
        pricePerNight: 0,
        availableRooms: 0,
        amenities: "",
      });
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <h3 className="text-lg font-semibold mb-2">
        {hotel ? "Edit Hotel" : "Add New Hotel"}
      </h3>
      <div>
        <Label htmlFor="hotelName">Hotel Name</Label>
        <Input
          id="hotelName"
          name="hotelName"
          value={formData.hotelName}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <Label htmlFor="location">Location</Label>
        <Input
          id="location"
          name="location"
          value={formData.location}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <Label htmlFor="pricePerNight">Price Per Night</Label>
        <Input
          id="pricePerNight"
          name="pricePerNight"
          type="number"
          value={formData.pricePerNight}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <Label htmlFor="availableRooms">Available Rooms</Label>
        <Input
          id="availableRooms"
          name="availableRooms"
          type="number"
          value={formData.availableRooms}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <Label htmlFor="amenities">Amenities</Label>
        <Textarea
          id="amenities"
          name="amenities"
          value={formData.amenities}
          onChange={handleChange}
          required
        />
      </div>
      <Button type="submit">{hotel ? "Update Hotel" : "Add Hotel"}</Button>
    </form>
  );
}

interface Flight {
  id?: string;
  _id?: string;
  flightName: string;
  from: string;
  to: string;
  departureTime: string;
  arrivalTime: string;
  price: number;
  availableSeats: number;
}

function AddEditFlight({ flight, onSaved }: { flight: Flight | null; onSaved?: () => void }) {
  const [formData, setFormData] = useState<Flight>(() => flight ?? {
    flightName: "", from: "", to: "", departureTime: "", arrivalTime: "", price: 0, availableSeats: 0,
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    // Here you would typically send this data to your backend
    console.log("Submitting flight data:", formData);
    if (flight) {
      await editflight(
        flight.id ?? flight._id,
        formData.flightName,
        formData.from,
        formData.to,
        formData.departureTime,
        formData.arrivalTime,
        formData.price,
        formData.availableSeats
      );
      onSaved?.();
      return;
    }
    await addflight(
      formData.flightName,
      formData.from,
      formData.to,
      formData.departureTime,
      formData.arrivalTime,
      formData.price,
      formData.availableSeats
    );
    onSaved?.();
    if (!flight) {
      setFormData({
        flightName: "",
        from: "",
        to: "",
        departureTime: "",
        arrivalTime: "",
        price: 0,
        availableSeats: 0,
      });
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <h3 className="text-lg font-semibold mb-2">
        {flight ? "Edit Flight" : "Add New Flight"}
      </h3>
      <div>
        <Label htmlFor="flightName">Flight Name</Label>
        <Input
          id="flightName"
          name="flightName"
          value={formData.flightName}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <Label htmlFor="from">From</Label>
        <Input
          id="from"
          name="from"
          value={formData.from}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <Label htmlFor="to">To</Label>
        <Input
          id="to"
          name="to"
          value={formData.to}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <Label htmlFor="departureTime">Departure Time</Label>
        <Input
          id="departureTime"
          name="departureTime"
          type="datetime-local"
          value={formData.departureTime}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <Label htmlFor="arrivalTime">Arrival Time</Label>
        <Input
          id="arrivalTime"
          name="arrivalTime"
          type="datetime-local"
          value={formData.arrivalTime}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <Label htmlFor="price">Price</Label>
        <Input
          id="price"
          name="price"
          type="number"
          value={formData.price}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <Label htmlFor="availableSeats">Available Seats</Label>
        <Input
          id="availableSeats"
          name="availableSeats"
          type="number"
          value={formData.availableSeats}
          onChange={handleChange}
          required
        />
      </div>
      <Button type="submit">{flight ? "Update Flight" : "Add Flight"}</Button>
    </form>
  );
}

interface PendingBookingPayment {
  id: string;
  bookingType: "FLIGHT" | "HOTEL" | "BUS" | "CAB" | "TRAIN" | "HOMESTAY" | "INSURANCE" | "HOLIDAY";
  flightId?: string;
  hotelId?: string;
  serviceOptionId?: string;
  quantity: number;
  totalPrice: number;
  paymentReference: string;
}

function BookingPaymentReview() {
  const [payments, setPayments] = useState<PendingBookingPayment[]>([]);
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState("");
  const [error, setError] = useState("");

  const refresh = async () => {
    try {
      const pendingPayments = await getPendingBookingPayments();
      setPayments(pendingPayments);
      setError("");
    } catch {
      setError("Could not load pending booking payments. Check your admin access and try again.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let active = true;
    getPendingBookingPayments()
      .then((pendingPayments) => {
        if (active) {
          setPayments(pendingPayments);
          setError("");
        }
      })
      .catch(() => {
        if (active) setError("Could not load pending booking payments. Check your admin access and try again.");
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => { active = false; };
  }, []);

  const review = async (bookingId: string, paymentStatus: "PAID" | "REJECTED") => {
    setBusyId(bookingId);
    setError("");
    try {
      await reviewBookingPayment(bookingId, paymentStatus);
      await refresh();
    } catch {
      setError("Payment review failed. Refresh the queue and try again.");
    } finally {
      setBusyId("");
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold">Pending booking payments</h2>
          <p className="text-sm text-gray-600">Check each payment reference against the payment account before approving.</p>
        </div>
        <Button variant="outline" onClick={() => void refresh()} disabled={loading}>Refresh</Button>
      </div>
      {error && <p role="alert" className="text-sm text-red-600">{error}</p>}
      {loading ? <p>Loading pending payments...</p> : payments.length === 0 ? <p className="text-sm text-gray-600">No pending payments.</p> : (
        <div className="space-y-3">
          {payments.map((payment) => (
            <Card key={payment.id}>
              <CardContent className="flex flex-col gap-3 p-4 md:flex-row md:items-center md:justify-between">
                <div className="space-y-1 text-sm">
                  <p className="font-semibold">{payment.bookingType} booking · {payment.quantity} {payment.bookingType === "FLIGHT" ? "seat(s)" : payment.bookingType === "HOTEL" || payment.bookingType === "HOMESTAY" ? "room(s)" : payment.bookingType === "CAB" ? "vehicle(s)" : payment.bookingType === "HOLIDAY" ? "traveler(s)" : payment.bookingType === "INSURANCE" ? "policy(ies)" : "ticket(s)"}</p>
                  <p>Product ID: {payment.flightId || payment.hotelId || payment.serviceOptionId}</p>
                  <p>Amount: ₹ {Number(payment.totalPrice).toLocaleString()}</p>
                  <p>Payment reference: <span className="font-mono">{payment.paymentReference}</span></p>
                  <p className="text-xs text-gray-500">Booking ID: {payment.id}</p>
                </div>
                <div className="flex gap-2">
                  <Button onClick={() => void review(payment.id, "PAID")} disabled={busyId === payment.id}>
                    Approve payment
                  </Button>
                  <Button variant="outline" onClick={() => void review(payment.id, "REJECTED")} disabled={busyId === payment.id}>
                    Reject & release inventory
                  </Button>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
export default function AdminDashboard() {
  const role = useSelector((state: AdminAuthState) => state.user.user?.role);
  const [activeTab, setActiveTab] = useState("flights");
  const [selectedFlight, setSelectedFlight] = useState<Flight | null>(null);
  const [selectedHotel, setSelectedHotel] = useState<Hotel | null>(null);
  const [listRevision, setListRevision] = useState(0);

  if (role !== "ADMIN") return <main className="mx-auto max-w-5xl p-8"><section className="rounded-xl border bg-white p-6"><h1 className="text-2xl font-bold">Administrator access required</h1><p className="mt-2">Sign in with an administrator account to manage travel inventory.</p></section></main>;

  return (
    <div className="container mx-auto p-4 bg-white max-w-full">
      <h1 className="text-3xl font-bold mb-6 ">Admin Dashboard</h1>
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-5 text-black">
          <TabsTrigger value="flights">Flights</TabsTrigger>
          <TabsTrigger value="inventory">Inventory</TabsTrigger>
          <TabsTrigger value="hotels">Hotels</TabsTrigger>
          <TabsTrigger value="users">Users</TabsTrigger>
          <TabsTrigger value="payments">Payments</TabsTrigger>
        </TabsList>
        <TabsContent value="inventory"><Card><CardContent className="pt-6"><AdminInventory /></CardContent></Card></TabsContent>
        <TabsContent value="flights">
          <Card>
            <CardHeader>
              <CardTitle>Manage Flights</CardTitle>
              <CardDescription>
                Add, edit, or remove flights from the system.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 gap-4">
                <FlightList key={listRevision} onSelect={setSelectedFlight} onDelete={(id) => { if ((selectedFlight?.id ?? selectedFlight?._id) === id) setSelectedFlight(null); setListRevision(value => value + 1); }} />
                <AddEditFlight key={selectedFlight?.id ?? selectedFlight?._id ?? "new-flight"} flight={selectedFlight} onSaved={() => setListRevision(value => value + 1)} />
              </div>
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value="hotels">
          <Card>
            <CardHeader>
              <CardTitle>Manage Hotels</CardTitle>
              <CardDescription>
                Add, edit, or remove hotels from the system.
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-2 gap-4">
                <HotelList key={listRevision} onSelect={setSelectedHotel} onDelete={(id) => { if ((selectedHotel?.id ?? selectedHotel?._id) === id) setSelectedHotel(null); setListRevision(value => value + 1); }} />
                <AddEditHotel key={selectedHotel?.id ?? selectedHotel?._id ?? "new-hotel"} hotel={selectedHotel} onSaved={() => setListRevision(value => value + 1)} />
              </div>
            </CardContent>
          </Card>
        </TabsContent>
        <TabsContent value="payments">
          <Card><CardHeader><CardTitle>Booking payment review</CardTitle><CardDescription>Confirm or reject payment submissions for flights and hotels.</CardDescription></CardHeader><CardContent><BookingPaymentReview /></CardContent></Card>
        </TabsContent>
        <TabsContent value="users">
          <Card>
            <CardHeader>
              <CardTitle>User Management</CardTitle>
              <CardDescription>Search for users by email.</CardDescription>
            </CardHeader>
            <CardContent>
              <UserSearch />
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}
