"use client";
import { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { getAdminRefunds, getFlaggedReviews, moderateReview, updateRefundStatus, apiErrorMessage } from "@/api/features";

const box = "rounded-2xl border bg-white p-5 shadow-sm";
type AdminState = { user: { user: { role?: string } | null } };
type RefundRow = { id: string; refundStatus: string; bookingId: string; userId: string; refundAmount: number; originalAmount: number; cancellationReason: string };
type FlagRow = { id: string; reviewId: string; userId: string; reason: string };
export default function AdminTravelTools(){
 const user=useSelector((s:AdminState)=>s.user.user),[refunds,setRefunds]=useState<RefundRow[]>([]),[flags,setFlags]=useState<FlagRow[]>([]),[error,setError]=useState(""),[notice,setNotice]=useState(""),[loading,setLoading]=useState(true);
 const load=async()=>{setLoading(true);setError("");try{const [r,f]=await Promise.all([getAdminRefunds(),getFlaggedReviews()]);setRefunds(r||[]);setFlags(f||[])}catch(e){setError(apiErrorMessage(e))}finally{setLoading(false)}};
 useEffect(()=>{if(user?.role!=="ADMIN")return;const timer=setTimeout(()=>{void load()},0);return()=>clearTimeout(timer)},[user]);
 if(!user)return <main className="mx-auto max-w-5xl p-8"><section className={box}><h1 className="text-2xl font-bold">Administrator sign in required</h1></section></main>;
 if(user.role!=="ADMIN")return <main className="mx-auto max-w-5xl p-8"><section className={box}><h1 className="text-2xl font-bold">Administrator access only</h1><p className="mt-2">Your account does not have permission to manage refunds or reviews.</p></section></main>;
 if(loading)return <main className="p-8">Loading admin tools…</main>;
 return <main className="mx-auto min-h-screen max-w-7xl space-y-5 bg-slate-50 p-5 sm:p-8"><h1 className="text-3xl font-bold">Travel moderation</h1>{error&&<p role="alert" className="rounded bg-red-50 p-3 text-red-700">{error}</p>}{notice&&<p role="status" className="rounded bg-emerald-50 p-3 text-emerald-700">{notice}</p>}
 <section className={box}><h2 className="mb-3 text-xl font-bold">Refunds</h2>{!refunds.length?<p>No refunds found.</p>:refunds.map(r=><article key={r.id} className="flex flex-wrap items-center justify-between gap-3 border-t py-3"><div><b>{r.refundStatus} · {r.bookingId}</b><p>{r.userId} · {r.refundAmount} / {r.originalAmount} · {r.cancellationReason}</p></div><select aria-label={"Refund status "+r.id} defaultValue={r.refundStatus} className="rounded border p-2" onChange={async e=>{try{await updateRefundStatus(r.id,e.target.value);setNotice("Refund status updated.");await load()}catch(err){setError(apiErrorMessage(err))}}}>{["PENDING","PROCESSING","PROCESSED","COMPLETED","REJECTED"].map(x=><option key={x}>{x}</option>)}</select></article>)}</section>
 <section className={box}><h2 className="mb-3 text-xl font-bold">Flagged reviews</h2>{!flags.length?<p>No flagged reviews.</p>:flags.map(f=><article key={f.id} className="flex flex-wrap items-center justify-between gap-3 border-t py-3"><div><b>Review {f.reviewId}</b><p>{f.reason} · reported by {f.userId}</p></div><div className="flex flex-wrap gap-2">{["APPROVED","HIDDEN","REMOVED","PENDING"].map(status=><button key={status} className="rounded border px-3 py-2 text-sm" onClick={async()=>{try{await moderateReview(f.reviewId,status);setNotice("Review moderation updated.");await load()}catch(e){setError(apiErrorMessage(e))}}}>{status==="PENDING"?"RESTORE":status}</button>)}</div></article>)}</section></main>
}
