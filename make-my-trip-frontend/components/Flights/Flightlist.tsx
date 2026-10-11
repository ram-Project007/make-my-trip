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
import { deleteflight, getflight } from "@/api";
import Loader from "../Loader";
type Flight = {
  id?: string;
  _id?: string;
  flightName: string;
  from: string;
  to: string;
  departureTime: string;
  arrivalTime: string;
  price: number;
  availableSeats: number;
};

const FlightList = ({ onSelect, onDelete }: { onSelect: (flight: Flight) => void; onDelete?: (id: string) => void }) => {
  const [flight, setflight] = useState<Flight[]>([]);
  const [loading, setloading] = useState(true);
  const [error, setError] = useState("");
  useEffect(() => {
    const fetchflight = async () => {
      try {
        const data = await getflight();
        setflight(data);
      } catch (error) {
        console.error(error);
      } finally {
        setloading(false);
      }
    };
    fetchflight();
  }, []);

  if (loading) {
    return <Loader />;
  }
  return (
    <div>
      <h3 className="text-lg font-semibold mb-2">Flight List</h3>
      {error && <p role="alert" className="mb-2 text-sm text-red-600">{error}</p>}
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Flight Name</TableHead>
            <TableHead>From</TableHead>
            <TableHead>To</TableHead>
            <TableHead>Action</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {flight.length > 0 ? (
            flight.map((flight) => (
              <TableRow key={flight.id ?? flight._id}>
                <TableCell>{flight.flightName}</TableCell>
                <TableCell>{flight.from}</TableCell>
                <TableCell>{flight.to}</TableCell>
                <TableCell>
                  <div className="flex gap-2"><Button onClick={() => onSelect(flight)}>Edit</Button><Button variant="outline" onClick={async () => {
                    const id = flight.id ?? flight._id;
                    if (!id || !window.confirm(`Remove flight “${flight.flightName}”?`)) return;
                    try { await deleteflight(id); setflight(current => current.filter(item => (item.id ?? item._id) !== id)); onDelete?.(id); }
                    catch { setError("Could not remove this flight. Refresh and try again."); }
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
export default FlightList;
