"use client";
import { useEffect, type ReactNode } from "react";
import { Provider } from "react-redux";
import store,{setUser} from "@/store";
import Navbar from "@/components/Navbar";
import Footer from "@/components/Fotter";
export default function AppProviders({children}:{children:ReactNode}){
 useEffect(()=>{const raw=localStorage.getItem("user");if(raw){try{store.dispatch(setUser(JSON.parse(raw)))}catch{localStorage.removeItem("user")}}},[]);
 return <Provider store={store}><Navbar/>{children}<Footer/></Provider>;
}
