import type { Metadata } from "next";
import "./globals.css";
import AppProviders from "@/components/AppProviders";
export const metadata:Metadata={title:"MakeMyTour",description:"Plan flights and stays with MakeMyTour."};
export default function RootLayout({children}:{children:React.ReactNode}){
 return <html lang="en"><body className="antialiased"><AppProviders>{children}</AppProviders></body></html>;
}
