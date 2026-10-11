"use client";

import { useCallback, useEffect, useState } from "react";
import { createTravelOption, deleteTravelOption, getTravelOptions, updateTravelOption } from "@/api";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";

type ListingType = "BUS" | "CAB" | "TRAIN" | "HOMESTAY" | "INSURANCE" | "HOLIDAY";
type Listing = {
  id?: string; _id?: string; type: ListingType; name: string; provider: string; from: string; to: string;
  location: string; departureTime: string; arrivalTime: string; price: number; availableUnits: number;
  description: string; amenities: string[] | string; images: string[] | string;
};
const types: { value: ListingType; label: string }[] = [
  { value: "BUS", label: "Buses" }, { value: "CAB", label: "Cabs" }, { value: "TRAIN", label: "Trains" },
  { value: "HOMESTAY", label: "Homestays" }, { value: "INSURANCE", label: "Insurance" }, { value: "HOLIDAY", label: "Holiday packages" },
];
const empty = (type: ListingType): Listing => ({ type, name: "", provider: "", from: "", to: "", location: "", departureTime: "", arrivalTime: "", price: 0, availableUnits: 0, description: "", amenities: [], images: [] });

export default function AdminInventory() {
  const [type, setType] = useState<ListingType>("BUS");
  const [rows, setRows] = useState<Listing[]>([]);
  const [form, setForm] = useState<Listing>(() => empty("BUS"));
  const [editing, setEditing] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  const load = useCallback(async () => {
    setLoading(true); setError("");
    try { setRows(await getTravelOptions(type)); }
    catch { setError(`Could not load ${type.toLowerCase()} listings.`); }
    finally { setLoading(false); }
  }, [type]);
  useEffect(() => {
    let active = true;
    getTravelOptions(type)
      .then((data) => { if (active) { setRows(data); setError(""); } })
      .catch(() => { if (active) setError(`Could not load ${type.toLowerCase()} listings.`); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [type]);

  const changeType = (next: ListingType) => { setLoading(true); setType(next); setEditing(null); setForm(empty(next)); setNotice(""); };
  const set = (key: keyof Listing, value: string | number) => setForm(current => ({ ...current, [key]: value }));
  const edit = (row: Listing) => { setEditing(row.id ?? row._id ?? null); setForm({ ...empty(type), ...row, amenities: row.amenities ?? [], images: row.images ?? [] }); setNotice(""); };
  const save = async (event: React.FormEvent) => {
    event.preventDefault(); setSaving(true); setError(""); setNotice("");
    const payload = { ...form, type, amenities: typeof form.amenities === "string" ? form.amenities.split(",").map(x => x.trim()).filter(Boolean) : form.amenities, images: typeof form.images === "string" ? form.images.split(",").map(x => x.trim()).filter(Boolean) : form.images };
    try {
      if (editing) await updateTravelOption(editing, payload); else await createTravelOption(payload);
      setForm(empty(type)); setEditing(null); setNotice(`${({ BUS: "Bus", CAB: "Cab", TRAIN: "Train", HOMESTAY: "Homestay", INSURANCE: "Insurance", HOLIDAY: "Holiday package" } as const)[type]} ${editing ? "updated" : "added"}.`); await load();
    } catch { setError("Could not save this listing."); }
    finally { setSaving(false); }
  };
  const remove = async (row: Listing) => {
    const id = row.id ?? row._id; if (!id || !window.confirm(`Remove “${row.name}” from ${type.toLowerCase()} listings?`)) return;
    setError(""); setNotice("");
    try { await deleteTravelOption(id); if (editing === id) { setEditing(null); setForm(empty(type)); } setNotice("Listing removed."); await load(); }
    catch { setError("Could not remove this listing."); }
  };

  const isRoute = type === "BUS" || type === "CAB" || type === "TRAIN";
  return <section className="space-y-5">
    <div><h2 className="text-xl font-semibold">Travel inventory</h2><p className="text-sm text-slate-600">Add, update, and remove transport, lodging, insurance, and holiday listings.</p></div>
    <div className="flex flex-wrap gap-2" role="tablist" aria-label="Inventory type">
      {types.map(item => <Button key={item.value} type="button" variant={type === item.value ? "default" : "outline"} onClick={() => changeType(item.value)}>{item.label}</Button>)}
    </div>
    {error && <p role="alert" className="rounded bg-red-50 p-3 text-red-700">{error}</p>}{notice && <p role="status" className="rounded bg-emerald-50 p-3 text-emerald-700">{notice}</p>}
    <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_minmax(320px,0.8fr)]">
      <div className="space-y-3">
        <div className="flex items-center justify-between"><h3 className="font-semibold">{types.find(x => x.value === type)?.label} ({rows.length})</h3><Button type="button" variant="outline" onClick={() => void load()} disabled={loading}>Refresh</Button></div>
        {loading ? <p>Loading listings…</p> : rows.length === 0 ? <p className="rounded border border-dashed p-5 text-sm text-slate-600">No listings yet.</p> : <div className="space-y-3">{rows.map(row => <article key={row.id ?? row._id} className="rounded-xl border p-4">
          <div className="flex flex-wrap items-start justify-between gap-3"><div><h4 className="font-semibold">{row.name}</h4><p className="text-sm text-slate-600">{row.provider} {isRoute ? `· ${row.from} → ${row.to}` : row.location ? `· ${row.location}` : ""}</p><p className="text-sm">₹{Number(row.price).toLocaleString()} · {row.availableUnits} available</p></div><div className="flex gap-2"><Button type="button" variant="outline" onClick={() => edit(row)}>Edit</Button><Button type="button" variant="outline" onClick={() => void remove(row)}>Remove</Button></div></div>
        </article>)}</div>}
      </div>
      <form onSubmit={save} className="space-y-3 rounded-xl border p-4">
        <h3 className="font-semibold">{editing ? "Update listing" : "Add listing"}</h3>
        <div><Label htmlFor="listing-name">Name</Label><Input id="listing-name" value={form.name} onChange={e => set("name", e.target.value)} required /></div>
        <div><Label htmlFor="listing-provider">Provider</Label><Input id="listing-provider" value={form.provider} onChange={e => set("provider", e.target.value)} /></div>
        {isRoute && <div className="grid grid-cols-2 gap-3"><div><Label htmlFor="listing-from">From</Label><Input id="listing-from" value={form.from} onChange={e => set("from", e.target.value)} /></div><div><Label htmlFor="listing-to">To</Label><Input id="listing-to" value={form.to} onChange={e => set("to", e.target.value)} /></div></div>}
        {!isRoute && <div><Label htmlFor="listing-location">Location / destination</Label><Input id="listing-location" value={form.location} onChange={e => set("location", e.target.value)} /></div>}
        {(type === "BUS" || type === "TRAIN") && <div className="grid grid-cols-2 gap-3"><div><Label htmlFor="listing-departure">Departure</Label><Input id="listing-departure" type="datetime-local" value={form.departureTime} onChange={e => set("departureTime", e.target.value)} /></div><div><Label htmlFor="listing-arrival">Arrival</Label><Input id="listing-arrival" type="datetime-local" value={form.arrivalTime} onChange={e => set("arrivalTime", e.target.value)} /></div></div>}
        <div className="grid grid-cols-2 gap-3"><div><Label htmlFor="listing-price">Price (₹)</Label><Input id="listing-price" type="number" min="0" step="0.01" value={form.price} onChange={e => set("price", Number(e.target.value))} required /></div><div><Label htmlFor="listing-units">Available units</Label><Input id="listing-units" type="number" min="0" value={form.availableUnits} onChange={e => set("availableUnits", Number(e.target.value))} required /></div></div>
        <div><Label htmlFor="listing-description">Description / policy details</Label><Textarea id="listing-description" value={form.description} onChange={e => set("description", e.target.value)} rows={3} /></div>
        <div><Label htmlFor="listing-amenities">Features / inclusions (comma separated)</Label><Input id="listing-amenities" value={Array.isArray(form.amenities) ? form.amenities.join(", ") : form.amenities} onChange={e => set("amenities", e.target.value)} /></div>
        <div><Label htmlFor="listing-images">Image URLs (comma separated)</Label><Input id="listing-images" value={Array.isArray(form.images) ? form.images.join(", ") : form.images} onChange={e => set("images", e.target.value)} /></div>
        <div className="flex gap-2"><Button type="submit" disabled={saving}>{saving ? "Saving…" : editing ? "Save changes" : "Add listing"}</Button>{editing && <Button type="button" variant="outline" onClick={() => { setEditing(null); setForm(empty(type)); }}>Cancel</Button>}</div>
      </form>
    </div>
  </section>;
}