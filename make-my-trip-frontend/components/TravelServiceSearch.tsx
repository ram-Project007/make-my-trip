"use client";
import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { getTravelOptions } from "@/api";
import { Button } from "@/components/ui/button";

type Kind = "BUS" | "CAB" | "TRAIN" | "HOMESTAY" | "INSURANCE" | "HOLIDAY";
type Option = { id?: string; _id?: string; type: Kind; name: string; provider?: string; from?: string; to?: string; location?: string; departureTime?: string; arrivalTime?: string; price: number; availableUnits: number; description?: string; amenities?: string[]; images?: string[] };

export default function TravelServiceSearch({ type }: { type: Kind }) {
  const router = useRouter();
  const [items, setItems] = useState<Option[]>([]);
  const [from, setFrom] = useState("");
  const [to, setTo] = useState("");
  const [loadedFor, setLoadedFor] = useState<Kind | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    getTravelOptions(type).then(data => { if (active) { setItems(data as Option[]); setError(""); } })
      .catch(() => { if (active) setError("Could not load these options. Check that the backend is running."); })
      .finally(() => { if (active) setLoadedFor(type); });
    return () => { active = false; };
  }, [type]);

  const results = useMemo(() => items.filter(item => {
    if (type === "HOMESTAY" || type === "HOLIDAY") return !to || item.location?.toLowerCase().includes(to.toLowerCase());
    if (type === "INSURANCE") return true;
    return (!from || item.from?.toLowerCase().includes(from.toLowerCase())) && (!to || item.to?.toLowerCase().includes(to.toLowerCase()));
  }), [items, from, to, type]);
  const title = ({ BUS: "Buses", CAB: "Cabs", TRAIN: "Trains", HOMESTAY: "Homestays", INSURANCE: "Insurance", HOLIDAY: "Holiday packages" } as const)[type];

  return <section className="bg-white rounded-xl shadow-lg mx-auto max-w-5xl p-6">
    <h2 className="text-2xl font-bold mb-1">Find {title}</h2>
    <p className="text-sm text-gray-500 mb-5">Browse availability and submit payment for admin confirmation.</p>
    <div className="grid grid-cols-1 md:grid-cols-3 gap-3 mb-6">
      {["BUS", "CAB", "TRAIN"].includes(type) && <input className="rounded-lg border p-3" placeholder="From city" value={from} onChange={e => setFrom(e.target.value)} />}
      {type !== "INSURANCE" && <input className="rounded-lg border p-3" placeholder={type === "HOMESTAY" || type === "HOLIDAY" ? "Destination city" : "To city"} value={to} onChange={e => setTo(e.target.value)} />}
      <Button variant="outline" onClick={() => { setFrom(""); setTo(""); }}>Clear filters</Button>
    </div>
    {loadedFor !== type ? <p>Loading {title.toLowerCase()}…</p> : error ? <p role="alert" className="text-red-600">{error}</p> : results.length ? <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      {results.map(item => { const id = item.id ?? item._id; return <article key={id} className="overflow-hidden rounded-lg border shadow-sm">
        {item.images?.[0] && <img src={item.images[0]} alt={item.name} className="h-44 w-full object-cover" />}
        <div className="p-4">
          <h3 className="text-lg font-semibold">{item.name}</h3>
          <p className="text-sm text-gray-600">{item.provider}</p>
          <p className="mt-2 text-gray-700">{type === "HOMESTAY" || type === "HOLIDAY" || type === "INSURANCE" ? item.location : `${item.from} → ${item.to}`}</p>
          {item.departureTime && <p className="text-sm text-gray-600">Depart {new Date(item.departureTime).toLocaleString()}{item.arrivalTime ? ` · Arrive ${new Date(item.arrivalTime).toLocaleString()}` : ""}</p>}
          {item.description && <p className="mt-2 text-sm text-gray-600">{item.description}</p>}
          {!!item.amenities?.length && <p className="mt-2 text-xs text-gray-500">{item.amenities.join(" · ")}</p>}
          <p className="mt-3 font-bold">₹{Number(item.price).toLocaleString()} {type === "HOMESTAY" ? "per night" : type === "CAB" ? "per ride" : type === "INSURANCE" ? "per policy" : type === "HOLIDAY" ? "per traveler" : "per ticket"}</p>
          <p className="text-xs text-gray-500">{item.availableUnits} available</p>
          <Button className="mt-3 w-full" disabled={!id || item.availableUnits < 1} onClick={() => router.push(`/book-service/${type.toLowerCase()}/${id}`)}>Book and pay</Button>
        </div>
      </article>; })}
    </div> : <p className="text-gray-600">No {title.toLowerCase()} match those cities. Try clearing the filters.</p>}
  </section>;
}
