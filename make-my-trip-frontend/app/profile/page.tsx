"use client";
import { useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { editprofile } from "@/api";
import { setUser } from "@/store";
import TravelFeatureDashboard from "@/components/features/TravelFeatureDashboard";

type ProfileState = { user: { user: { id?: string; firstName?: string; lastName?: string; email?: string; phoneNumber?: string } | null } };
function ProfileCard(){
 const user=useSelector((s:ProfileState)=>s.user.user),dispatch=useDispatch(),[editing,setEditing]=useState(false),[form,setForm]=useState({firstName:user?.firstName||"",lastName:user?.lastName||"",email:user?.email||"",phoneNumber:user?.phoneNumber||""}),[error,setError]=useState("");
 if(!user)return null;
 return <section className="rounded-2xl border bg-white p-5 shadow-sm"><div className="flex items-center justify-between"><div><p className="text-sm text-slate-500">Signed in as</p><h2 className="text-xl font-bold">{user.firstName} {user.lastName}</h2><p>{user.email}</p><p>{user.phoneNumber}</p></div><button className="rounded-lg border px-3 py-2" onClick={()=>setEditing(!editing)}>{editing?"Close":"Edit profile"}</button></div>{editing&&<form className="mt-4 grid gap-3 sm:grid-cols-2" onSubmit={async e=>{e.preventDefault();setError("");try{const next=await editprofile(user.id,form.firstName,form.lastName,form.email,form.phoneNumber);dispatch(setUser({...user,...next}));setEditing(false)}catch{setError("Could not save profile. Verify your access and retry.")}}}>{(["firstName","lastName","email","phoneNumber"] as const).map(k=><label key={k} className="text-sm capitalize">{k}<input required className="mt-1 w-full rounded-lg border p-2" value={form[k]} onChange={e=>setForm({...form,[k]:e.target.value})}/></label>)}{error&&<p className="text-red-700 sm:col-span-2">{error}</p>}<button className="rounded-lg bg-blue-700 p-2 text-white sm:col-span-2">Save profile</button></form>}</section>
}
export default function ProfilePage(){const user=useSelector((s:ProfileState)=>s.user.user);return <main className="mx-auto max-w-7xl space-y-5 bg-slate-50 px-4 py-6"><ProfileCard key={user?.id||"anonymous"}/><TravelFeatureDashboard/></main>}
