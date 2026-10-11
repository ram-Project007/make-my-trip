"use client";
import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { useDispatch, useSelector } from "react-redux";
import { handleTravelServiceBooking, getTravelOptions } from "@/api";
import { setUser } from "@/store";
import { Button } from "@/components/ui/button";
import SignupDialog from "@/components/SignupDialog";

type Listing = { id?: string; _id?: string; type: string; name: string; provider?: string; from?: string; to?: string; location?: string; departureTime?: string; arrivalTime?: string; price: number; availableUnits: number; description?: string; amenities?: string[]; images?: string[] };
type SessionUser = { id?: string; bookings?: unknown[]; role?: string };
type RootState = { user: { user: SessionUser | null } };

export default function TravelServiceBooking(){
  const params=useParams<{type:string;id:string}>();
  const router=useRouter(); const dispatch=useDispatch();
  const user=useSelector((state:RootState)=>state.user.user);
  const [listing,setListing]=useState<Listing|null>(null); const [loading,setLoading]=useState(true);
  const [quantity,setQuantity]=useState(1);const [paying,setPaying]=useState(false);const [error,setError]=useState("");const [bookingResult,setBookingResult]=useState<{id:string;totalPrice:number}|null>(null);
  const id=params?.id??""; const type=(params?.type??"").toUpperCase();
  useEffect(()=>{let active=true;getTravelOptions(type).then((data:Listing[])=>{const found=data.find(x=>(x.id??x._id)===id);if(active)setListing(found??null);}).catch(()=>{if(active)setError("Unable to load this option. Please return to search and try again.");}).finally(()=>{if(active)setLoading(false);});return()=>{active=false;};},[id,type]);
  if(loading)return <main className="mx-auto max-w-3xl p-8">Loading booking details…</main>;
  if(!listing)return <main className="mx-auto max-w-3xl p-8"><p role="alert">{error||"This travel option is no longer available."}</p><Button className="mt-4" onClick={()=>router.push("/")}>Back to search</Button></main>;
  const total=Number(listing.price)*quantity;
  const submit=async()=>{if(!user?.id){setError("Please sign in to book.");return;}setPaying(true);setError("");try{const booking=await handleTravelServiceBooking(user.id,id,quantity,total,"");dispatch(setUser({...user,bookings:[...(user.bookings||[]).filter((item: any)=>item?.id!==booking.id),booking]}));setBookingResult(booking);}catch(e){console.error(e);setError("Booking request failed. Check availability and retry.");}finally{setPaying(false);}};
  const locationBased=type==="HOMESTAY"||type==="HOLIDAY"||type==="INSURANCE"; const unit=type==="HOMESTAY"?"room/night":type==="CAB"?"vehicle":type==="HOLIDAY"?"traveler":type==="INSURANCE"?"policy":"ticket";
  return <main className="min-h-screen bg-slate-50 px-4 py-10"><div className="mx-auto max-w-3xl rounded-xl bg-white p-6 shadow">
    <Button variant="outline" onClick={()=>router.push("/")}>Back</Button>
    <h1 className="mt-5 text-2xl font-bold">Book {listing.name}</h1><p className="text-gray-600">{listing.provider}</p>
    {listing.images?.[0]&&<img src={listing.images[0]} alt={listing.name} className="mt-4 h-56 w-full rounded-lg object-cover" />}
    {(locationBased?listing.location:listing.from||listing.to)&&<p className="mt-3">{locationBased?listing.location:`${listing.from} → ${listing.to}`}</p>}
    {listing.departureTime&&<p className="mt-1 text-sm text-gray-600">Departure {new Date(listing.departureTime).toLocaleString()} {listing.arrivalTime?`· Arrival ${new Date(listing.arrivalTime).toLocaleString()}`:""}</p>}
    {listing.description&&<p className="mt-3 text-gray-700">{listing.description}</p>}
    {!!listing.amenities?.length&&<p className="mt-2 text-sm text-gray-600">{listing.amenities.join(" · ")}</p>}
    <label className="mt-5 block text-sm font-medium" htmlFor="quantity">Quantity ({unit})</label>
    <input id="quantity" type="number" min={1} max={listing.availableUnits} value={quantity} onChange={e=>setQuantity(Math.max(1,Math.min(listing.availableUnits,Number(e.target.value)||1)))} className="mt-1 w-full rounded-md border px-3 py-2" />
    <p className="mt-3 text-lg font-bold">₹{Number(listing.price).toLocaleString()} × {quantity} = ₹{total.toLocaleString()}</p>
    {bookingResult ? <div className="mt-5 rounded-lg border border-green-200 bg-green-50 p-4 text-sm text-center"><strong>Booking request submitted.</strong><p>This request is pending confirmation. No payment was taken.</p><p className="mt-1 text-xs">Booking ID: {bookingResult.id}</p></div> : <p className="mt-5 rounded-lg border p-4 text-sm text-gray-600">Submit a booking request for admin confirmation. No payment is collected here.</p>}
    {error&&<p role="alert" className="mt-3 text-sm text-red-600">{error}</p>}
    {!bookingResult&&(user?<Button className="mt-5 w-full" disabled={paying||listing.availableUnits<quantity} onClick={submit}>{paying?"Creating booking…":"Submit booking request"}</Button>:<SignupDialog trigger={<Button className="mt-5 w-full">Sign in to continue</Button>} />)}
  </div></main>;
}
