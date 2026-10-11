"use client";
const fmt=(value)=>new Date(value).toLocaleDateString(undefined,{month:"short",day:"numeric"});
export default function PriceHistoryChart({history=[]}){
 const price=(h)=>Number(h.currentPrice??h.calculatedPrice??h.price??0);
 const max=Math.max(1,...history.map(price));
 return <div role="img" aria-label="Backend price history chart" className="mt-4 space-y-3">{history.map((item,index)=><div key={item.id||item.timestamp||index} className="grid grid-cols-[5rem_1fr_7rem] items-center gap-3 text-sm"><span className="text-slate-500">{item.timestamp?fmt(item.timestamp):"—"}</span><div className="h-4 rounded bg-slate-100"><div className="h-4 rounded bg-blue-600" style={{width:`${Math.max(2,price(item)/max*100)}%`}}/></div><b className="text-right">{price(item).toLocaleString("en-IN",{style:"currency",currency:"INR",maximumFractionDigits:2})}</b></div>)}</div>;
}
