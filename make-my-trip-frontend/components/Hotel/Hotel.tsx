import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "../ui/button";
import { useEffect, useState } from "react";
import { deletehotel, gethotel } from "@/api";
import Loader from "../Loader";

type Hotel = {
  id?: string;
  _id?: string;
  hotelName: string;
  location: string;
  pricePerNight: number;
  availableRooms: number;
  amenities: string;
};

const HotelList = ({ onSelect, onDelete }: { onSelect: (hotel: Hotel) => void; onDelete?: (id: string) => void }) => {
  const [hotel, sethotel] = useState<Hotel[]>([]);
  const [loading, setloading] = useState(true);
  const [error, setError] = useState("");
  useEffect(() => {
    const fetchhotel = async () => {
      try {
        const data = await gethotel();
        sethotel(data);
      } catch (error) {
        console.error(error);
      } finally {
        setloading(false);
      }
    };
    fetchhotel();
  }, []);

  if (loading) {
    return <Loader />;
  }
  return (
    <div>
      <h3 className="text-lg font-semibold mb-2">Hotel List</h3>
      {error && <p role="alert" className="mb-2 text-sm text-red-600">{error}</p>}
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Hotel Name</TableHead>
            <TableHead>Location</TableHead>
            <TableHead>Price/Night</TableHead>
            <TableHead>Action</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {hotel.length > 0 ? (
            hotel.map((hotel) => (
              <TableRow key={hotel.id ?? hotel._id}>
                <TableCell>{hotel.hotelName}</TableCell>
                <TableCell>{hotel.location}</TableCell>
                <TableCell>${hotel.pricePerNight}</TableCell>
                <TableCell>
                  <div className="flex gap-2"><Button onClick={() => onSelect(hotel)}>Edit</Button><Button variant="outline" onClick={async () => {
                    const id = hotel.id ?? hotel._id;
                    if (!id || !window.confirm(`Remove hotel “${hotel.hotelName}”?`)) return;
                    try { await deletehotel(id); sethotel(current => current.filter(item => (item.id ?? item._id) !== id)); onDelete?.(id); }
                    catch { setError("Could not remove this hotel. Refresh and try again."); }
                  }}>Remove</Button></div>
                </TableCell>
              </TableRow>
            ))
          ) : (
            <TableRow>
              <TableCell>No data</TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </div>
  );
};
export default HotelList;
