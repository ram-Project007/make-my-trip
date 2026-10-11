"use client";
import type { ChangeEvent, ReactNode } from "react";


import { getflight, gethotel, getTravelOptions } from "@/api";
import { getCurrentPrice } from "@/api/features";
import Loader from "@/components/Loader";
import { SearchSelect } from "@/components/SearchSelect";
import TravelServiceSearch from "@/components/TravelServiceSearch";
import { Button } from "@/components/ui/button";
import {
  Bus,
  Calendar,
  Car,
  CreditCard,
  HomeIcon,
  Hotel,
  MapPin,
  Plane,
  QrCode,
  Shield,
  Train,
  Umbrella,
  Users,
} from "lucide-react";
import { useRouter } from "next/navigation";
import { useEffect, useMemo, useState } from "react";

type TravelListing = {
  id?: string | number;
  _id?: string;
  flightName?: string;
  from?: string;
  to?: string;
  departureTime?: string;
  arrivalTime?: string;
  price?: number;
  hotelName?: string;
  location?: string;
  pricePerNight?: number;
  name?: string;
  provider?: string;
  description?: string;
  availableUnits?: number;
  images?: string[];
};
type CardImageProps = { title: string; imageUrl: string };
type OfferCardProps = CardImageProps & { description: string };
type CollectionCardProps = CardImageProps & { tag: string };
type NavItemProps = { icon?: ReactNode; text: string; active?: boolean; onClick?: () => void };
type SearchInputProps = {
  icon: ReactNode;
  placeholder: string;
  value: string;
  onChange: (event: ChangeEvent<HTMLInputElement>) => void;
  subtitle?: string;
  type?: string;
};
type CurrentPrice = {
  currentPrice?: number;
  calculatedPrice?: number;
  basePrice?: number;
  reason?: string;
};

export default function Home() {
  const [bookingtype, setbookingtype] = useState("flights");
  const [from, setfrom] = useState("");
  const [to, setto] = useState("");
  const [date, setdate] = useState("");
  const [travelers, settravelers] = useState(1);
  const [searchresults, setsearchresult] = useState<TravelListing[]>([]);
  const [hotel, sethotel] = useState<TravelListing[]>([]);
  const [loading, setloading] = useState(true);
  const [currentPrices, setCurrentPrices] = useState<Record<string, CurrentPrice>>({});
  const [priceLoading, setPriceLoading] = useState(false);
  const [priceError, setPriceError] = useState(false);
  const [flight, setflight] = useState<TravelListing[]>([]);
  const [holidayPackages, setHolidayPackages] = useState<TravelListing[]>([]);
  const router = useRouter();
  const offers = [
    {
      title: "Domestic Flights",
      description: "Get up to 20% off on domestic flights",
      imageUrl:
        "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?auto=format&fit=crop&w=800",
    },
    {
      title: "International Hotels",
      description: "Book luxury hotels worldwide",
      imageUrl:
        "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800",
    },
    {
      title: "Holiday Packages",
      description: "Exclusive deals on holiday packages",
      imageUrl:
        "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800",
    },
  ];

  const collections = [
    {
      title: "Stays in & Around Delhi",
      imageUrl:
        "https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800",
      tag: "TOP 8",
    },
    {
      title: "Stays in & Around Mumbai",
      imageUrl:
        "https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=800",
      tag: "TOP 8",
    },
    {
      title: "Stays in & Around Bangalore",
      imageUrl:
        "https://images.unsplash.com/photo-1587474260584-136574528ed5?auto=format&fit=crop&w=800",
      tag: "TOP 9",
    },
    {
      title: "Beach Destinations",
      imageUrl:
        "https://images.unsplash.com/photo-1520454974749-611b7248ffdb?auto=format&fit=crop&w=800",
      tag: "TOP 11",
    },
  ];

  const wonders = [
    {
      title: "Shimla's Best Kept Secret",
      imageUrl:
        "https://images.unsplash.com/photo-1626621341517-bbf3d9990a23?auto=format&fit=crop&w=800",
    },
    {
      title: "Tamil Nadu's Charming Hill Town",
      imageUrl:
        "https://images.unsplash.com/photo-1544735716-392fe2489ffa?auto=format&fit=crop&w=800",
    },
    {
      title: "Quaint Little Hill Station in Gujarat",
      imageUrl:
        "https://images.unsplash.com/photo-1506905925346-21bda4d32df4?auto=format&fit=crop&w=800",
    },
    {
      title: "A pleasant summer retreat",
      imageUrl:
        "https://images.unsplash.com/photo-1593181629936-11c609b8db9b?auto=format&fit=crop&w=800",
    },
  ];

  useEffect(() => {
    const fetchdata = async () => {
      try {
        const data = await gethotel();
        sethotel(data as TravelListing[]);
        const flightdata = await getflight();
        setflight(flightdata as TravelListing[]);
      } catch (error) {
        console.error(error);
      } finally {
        setloading(false);
      }
    };

    fetchdata();
  }, []);

  useEffect(() => {
    let active = true;
    getTravelOptions("HOLIDAY")
      .then((data) => { if (active) setHolidayPackages(data as TravelListing[]); })
      .catch((error) => console.error("Could not load holiday packages", error));
    return () => { active = false; };
  }, []);
  useEffect(() => {
    if (!searchresults.length) return;
    let active = true;
    const timer = setTimeout(() => {
      setPriceLoading(true);
      setPriceError(false);
      const resourceType = bookingtype === "flights" ? "FLIGHT" : "HOTEL";
      Promise.allSettled(searchresults.map(async (item) => {
        const id = item.id ?? item._id;
        if (id == null) throw new Error("Listing has no id");
        return [String(id), await getCurrentPrice(resourceType, String(id))] as const;
      })).then((results) => {
        if (!active) return;
        const next: Record<string, CurrentPrice> = {};
        let failed = false;
        results.forEach((result) => {
          if (result.status === "fulfilled") next[result.value[0]] = result.value[1] as CurrentPrice;
          else failed = true;
        });
        setCurrentPrices(next);
        setPriceError(failed);
        setPriceLoading(false);
      });
    }, 0);
    return () => { active = false; clearTimeout(timer); };
  }, [searchresults, bookingtype]);

  const cityOptions = useMemo(() => {
    const cities = new Set<string>();
      flight.forEach((item) => {
      if (item.from) cities.add(item.from);
      if (item.to) cities.add(item.to);
    });
    hotel.forEach((item) => {
      if (item.location) cities.add(item.location);
    });
    return Array.from(cities).map((city) => ({ value: city, label: city }));
  }, [flight, hotel]);

  if (loading) {
    return <Loader />;
  }
  const handlesearch = () => {
    if (bookingtype === "flights") {
      const results = flight.filter(
        (item) =>
          item.from?.toLowerCase() === from.toLowerCase() &&
          item.to?.toLowerCase() === to.toLowerCase()
      );
      setsearchresult(results);
    } else if (bookingtype === "hotels") {
      const results = hotel.filter(
        (item) => item.location?.toLowerCase() === to.toLowerCase()
      );
      setsearchresult(results);
    }
  };
  const formatDate = (dateString?: string): string => {
    if (!dateString) return "Not provided";
    const options: Intl.DateTimeFormatOptions = {
      year: "numeric",
      month: "long",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    };
    const date = new Date(dateString);
    return date.toLocaleString("en-US", options);
  };
  const handlebooknow = (id: string | number) => {
    if (bookingtype === "flights") {
      router.push(`/book-flight/${id}`);
    } else {
      router.push(`/book-hotel/${id}`);
    }
  };
  return (
    <div
      className="min-h-screen bg-cover bg-center bg-no-repeat"
      style={{
        backgroundImage:
          'url("https://images.unsplash.com/photo-1464037866556-6812c9d1c72e?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=2940&q=80")',
      }}
    >
      <main className="container mx-auto px-4 py-6">
        <nav className="bg-white rounded-xl shadow-lg mx-auto max-w-5xl mb-6 p-4 overflow-x-auto">
          <div className="flex justify-between items-center min-w-max space-x-8">
            <NavItem
              icon={<Plane />}
              text="Flights"
              active={bookingtype === "flights"}
              onClick={() => setbookingtype("flights")}
            />
            <NavItem
              icon={<Hotel />}
              text="Hotels"
              active={bookingtype === "hotels"}
              onClick={() => setbookingtype("hotels")}
            />
            <NavItem icon={<HomeIcon />} text="Homestays" active={bookingtype === "homestays"} onClick={() => setbookingtype("homestays")} />
            <NavItem icon={<Umbrella />} text="Holiday" active={bookingtype === "holiday"} onClick={() => setbookingtype("holiday")} />
            <NavItem icon={<Train />} text="Trains" active={bookingtype === "trains"} onClick={() => setbookingtype("trains")} />
            <NavItem icon={<Bus />} text="Buses" active={bookingtype === "buses"} onClick={() => setbookingtype("buses")} />
            <NavItem icon={<Car />} text="Cabs" active={bookingtype === "cabs"} onClick={() => setbookingtype("cabs")} />
            <NavItem icon={<CreditCard />} text="Forex" />
            <NavItem icon={<Shield />} text="Insurance" active={bookingtype === "insurance"} onClick={() => setbookingtype("insurance")} />
          </div>
        </nav>

        {(["buses", "cabs", "trains", "homestays", "holiday", "insurance"].includes(bookingtype)) ? (
          <TravelServiceSearch type={bookingtype === "buses" ? "BUS" : bookingtype === "cabs" ? "CAB" : bookingtype === "trains" ? "TRAIN" : bookingtype === "homestays" ? "HOMESTAY" : bookingtype === "insurance" ? "INSURANCE" : "HOLIDAY"} />
        ) : <div className="bg-white rounded-xl shadow-lg mx-auto max-w-5xl p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-4">
            {bookingtype === "flights" && (
              <div className="col-span-1">
                <SearchSelect
                  options={cityOptions}
                  placeholder="From"
                  value={from}
                  onChange={setfrom}
                  icon={<MapPin className="text-gray-400" />}
                  subtitle="Enter city or airport"
                />
              </div>
            )}

            <div className="col-span-1">
              <SearchSelect
                options={cityOptions}
                placeholder={bookingtype === "flights" ? "To" : "City"}
                value={to}
                onChange={setto}
                icon={<MapPin className="text-gray-400" />}
                subtitle={
                  bookingtype === "flights"
                    ? "Enter city or airport"
                    : "Enter city"
                }
              />
            </div>

            <div className="col-span-1">
              <SearchInput
                icon={<Calendar className="text-gray-400" />}
                placeholder="Date"
                value={date}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                  setdate(e.target.value)
                }
                subtitle="Select a date"
                type="date"
              />
            </div>

            <div className="col-span-1">
              <SearchInput
                icon={<Users className="text-gray-400" />}
                placeholder="Travelers"
                value={travelers.toString()}
                onChange={(e: React.ChangeEvent<HTMLInputElement>) =>
                  settravelers(parseInt(e.target.value) || 1)
                }
                subtitle="Number of travelers"
                type="number"
              />
            </div>

            <Button className="col-span-1 h-full" onClick={handlesearch}>
              SEARCH
            </Button>
          </div>
          <div className="mt-6">
            <h2 className="text-xl font-semibold mb-4 text-white">
              Search Results
            </h2>
            {searchresults.length > 0 ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {searchresults.map((result) => (
                  <div
                    key={String(result.id ?? result._id)}
                    className="bg-white rounded-lg shadow p-4 border border-gray-200"
                  >
                    {bookingtype === "flights" ? (
                      <>
                        <p className="font-semibold text-lg">
                          Flight Name: {result.flightName}
                        </p>
                        <h3 className="font-semibold text-lg">
                          {result.from} to {result.to}
                        </h3>
                        <p className="text-gray-600">
                          Departure Time: {formatDate(result.departureTime)}
                        </p>
                        <p className="text-gray-600">
                          Arrival Time: {formatDate(result.arrivalTime)}
                        </p>
                        <p className="text-lg font-bold mt-2">
                          {priceLoading ? "Loading current price…" : currentPrices[String(result.id ?? result._id)] ? `₹${currentPrices[String(result.id ?? result._id)].currentPrice ?? currentPrices[String(result.id ?? result._id)].calculatedPrice}` : priceError ? "Current price unavailable" : `Listed price ₹${result.price}`}
                        </p>
                        {currentPrices[String(result.id ?? result._id)]?.basePrice != null && <p className="text-xs text-gray-500">Base ₹{currentPrices[String(result.id ?? result._id)].basePrice} · {currentPrices[String(result.id ?? result._id)].reason || "Current price from backend"}</p>}
                        <Button
                          className="w-full mt-4"
                          onClick={() => result.id != null || result._id ? handlebooknow(result.id ?? result._id!) : undefined}
                        >
                          Book Now
                        </Button>
                      </>
                    ) : (
                      <>
                        <h3 className="font-semibold text-lg">
                          {result.hotelName}
                        </h3>
                        <p className="text-gray-600">City: {result.location}</p>
                        <p className="text-lg font-bold mt-2">
                          {priceLoading ? "Loading current price…" : currentPrices[String(result.id ?? result._id)] ? `₹${currentPrices[String(result.id ?? result._id)].currentPrice ?? currentPrices[String(result.id ?? result._id)].calculatedPrice} per night` : priceError ? "Current price unavailable" : `Listed price ₹${result.pricePerNight} per night`}
                        </p>
                        {currentPrices[String(result.id ?? result._id)]?.basePrice != null && <p className="text-xs text-gray-500">Base ₹{currentPrices[String(result.id ?? result._id)].basePrice} · {currentPrices[String(result.id ?? result._id)].reason || "Current price from backend"}</p>}
                        <Button
                          className="w-full mt-4"
                          onClick={() => result.id != null || result._id ? handlebooknow(result.id ?? result._id!) : undefined}
                        >
                          Book Now
                        </Button>
                      </>
                    )}
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-gray-600">
                No {bookingtype} available for the selected criteria.
              </p>
            )}
          </div>
        </div>}
        <div className="max-w-7xl mx-auto px-4">
          {/* Offers Section */}
          <section className="my-16">
            <h2 className="text-2xl font-bold mb-8 text-white">Best Offers</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
              {offers.map((offer, index) => (
                <OfferCard key={index} {...offer} />
              ))}
            </div>
          </section>

          {holidayPackages.length > 0 && <section className="my-16">
            <div className="mb-8 flex items-end justify-between gap-4"><div><h2 className="text-2xl font-bold text-white">Holiday packages</h2><p className="mt-1 text-sm text-white/80">Packages curated by our travel team</p></div><button className="text-sm font-semibold text-white underline" onClick={() => setbookingtype("holiday")}>View all packages</button></div>
            <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
              {holidayPackages.slice(0, 6).map((pkg) => {
                const id = pkg.id ?? pkg._id;
                return <article key={id} className="overflow-hidden rounded-xl bg-white shadow-lg">
                  {pkg.images?.[0] && <img src={pkg.images[0]} alt={pkg.name ?? "Holiday package"} className="h-48 w-full object-cover" />}
                  <div className="space-y-2 p-4"><h3 className="text-lg font-semibold">{pkg.name}</h3><p className="text-sm text-slate-600">{pkg.location}{pkg.provider ? ` · ${pkg.provider}` : ""}</p>{pkg.description && <p className="text-sm text-slate-600">{pkg.description}</p>}<p className="font-bold">₹{Number(pkg.price).toLocaleString()} per traveler</p><Button className="w-full" disabled={!id || (pkg.availableUnits ?? 0) < 1} onClick={() => id && router.push(`/book-service/holiday/${id}`)}>Book package</Button></div>
                </article>;
              })}
            </div>
          </section>}
          {/* Collections Section */}
          <section className="my-16">
            <div className="flex justify-between items-center mb-8">
              <h2 className="text-2xl font-bold text-white">
                Handpicked Collections for You
              </h2>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {collections.map((collection, index) => (
                <CollectionCard key={index} {...collection} />
              ))}
            </div>
          </section>

          {/* Wonders Section */}
          <section className="my-16">
            <div className="flex justify-between items-center mb-8">
              <h2 className="text-2xl font-bold text-white">
                Unlock Lesser-Known <span></span> Wonders of India
              </h2>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {wonders.map((wonder, index) => (
                <WonderCard key={index} {...wonder} />
              ))}
            </div>
          </section>

          {/* Download App Section */}
          <DownloadApp />
        </div>
      </main>
    </div>
  );
}
const OfferCard = ({ title, description, imageUrl }: OfferCardProps) => {
  return (
    <div className="bg-white rounded-lg shadow-md overflow-hidden">
      <img src={imageUrl} alt={title} className="w-full h-48 object-cover" />
      <div className="p-4">
        <h3 className="font-semibold text-lg mb-2">{title}</h3>
        <p className="text-gray-600 text-sm">{description}</p>
        <button className="mt-4 px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors">
          Book Now
        </button>
      </div>
    </div>
  );
};

const CollectionCard = ({ title, imageUrl, tag }: CollectionCardProps) => {
  return (
    <div className="relative group cursor-pointer overflow-hidden rounded-lg">
      <img
        src={imageUrl}
        alt={title}
        className="w-full h-64 object-cover transition-transform duration-300 group-hover:scale-110"
      />
      <div className="absolute inset-0 bg-gradient-to-b from-transparent to-black/70">
        <div className="absolute top-4 left-4">
          <span className="bg-white text-black text-sm font-semibold px-2 py-1 rounded">
            {tag}
          </span>
        </div>
        <div className="absolute bottom-4 left-4 right-4">
          <h3 className="text-white text-lg font-semibold">{title}</h3>
        </div>
      </div>
    </div>
  );
};
const DownloadApp = () => {
  return (
    <div className="bg-white p-6 rounded-lg shadow-md max-w-7xl mx-auto my-12">
      <div className="flex flex-col md:flex-row items-center justify-between">
        <div className="mb-6 md:mb-0">
          <h3 className="text-xl font-bold mb-2">Download App Now!</h3>
          <p className="text-gray-600 mb-4">
            Get India&apos;s #1 travel super app with best deals on flights
          </p>
          <div className="flex space-x-4">
            <img
              src="https://upload.wikimedia.org/wikipedia/commons/3/3c/Download_on_the_App_Store_Badge.svg"
              alt="App Store"
              className="h-10"
            />
            <img
              src="https://upload.wikimedia.org/wikipedia/commons/7/78/Google_Play_Store_badge_EN.svg"
              alt="Play Store"
              className="h-10"
            />
          </div>
        </div>
        <div className="flex items-center space-x-4">
          <QrCode className="w-24 h-24" />
          <p className="text-sm text-gray-600">
            Scan QR code to download the app
          </p>
        </div>
      </div>
    </div>
  );
};

const WonderCard = ({ title, imageUrl }: CardImageProps) => {
  return (
    <div className="relative group cursor-pointer overflow-hidden rounded-lg">
      <img
        src={imageUrl}
        alt={title}
        className="w-full h-64 object-cover transition-transform duration-300 group-hover:scale-110"
      />
      <div className="absolute inset-0 bg-gradient-to-b from-transparent to-black/70">
        <div className="absolute bottom-4 left-4 right-4">
          <h3 className="text-white text-lg font-semibold">{title}</h3>
        </div>
      </div>
    </div>
  );
};
function NavItem({ icon, text, active = false, onClick }: NavItemProps) {
  return (
    <button
      className={`flex flex-col items-center p-2 rounded-lg transition-colors ${active ? "text-blue-500" : "text-gray-600 hover:text-blue-500"
        }`}
      onClick={onClick}
    >
      {icon}
      <span className="text-sm mt-1 whitespace-nowrap">{text}</span>
    </button>
  );
}

function SearchInput({
  icon,
  placeholder,
  value,
  onChange,
  subtitle,
  type = "text",
}: SearchInputProps) {
  return (
    <div className="border rounded-lg p-3 hover:border-blue-500 cursor-pointer h-full">
      <div className="flex items-center space-x-2">
        {icon}
        <div className="flex-1 min-w-0">
          <div className="text-sm text-gray-500 truncate">{placeholder}</div>
          <input
            type={type}
            value={value}
            onChange={onChange}
            className="font-semibold w-full bg-transparent outline-none"
            placeholder={placeholder}
          />
          <div className="text-xs text-gray-400 truncate">{subtitle}</div>
        </div>
      </div>
    </div>
  );
}
