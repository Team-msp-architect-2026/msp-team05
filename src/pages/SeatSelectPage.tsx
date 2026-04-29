import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import Navbar from '../components/Navbar';
import Spinner from '../components/Spinner';

interface Seat {
  seatId: string;
  row: string;
  number: string;
  status: string;
}

interface Zone {
  zoneId: string;
  zoneName: string;
  price: number;
  seats: Seat[];
}

export default function SeatSelectPage() {
  const { gameId } = useParams();
  const navigate = useNavigate();
  const [zones, setZones] = useState<Zone[]>([]);
  const [selectedSeats, setSelectedSeats] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get(`/api/games/${gameId}/seats`)
      .then(res => setZones(res.data.data.zones))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, [gameId]);

  const toggleSeat = (seatId: string) => {
    if (selectedSeats.includes(seatId)) {
      setSelectedSeats(selectedSeats.filter(id => id !== seatId));
    } else {
      if (selectedSeats.length >= 4) {
        alert('최대 4석까지 선택 가능합니다.');
        return;
      }
      setSelectedSeats([...selectedSeats, seatId]);
    }
  };

  const handleLock = async () => {
    if (selectedSeats.length === 0) {
      alert('좌석을 선택해주세요.');
      return;
    }
    try {
      const res = await api.post(`/api/games/${gameId}/seats/lock`, {
        seatIds: selectedSeats,
      });
      localStorage.setItem('lockToken', res.data.data.lockToken);
      navigate('/checkout');
    } catch (err) {
      alert('좌석 선점에 실패했습니다. 다시 시도해주세요.');
    }
  };

  if (loading) return <Spinner />;

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <div className="max-w-6xl mx-auto px-4 py-8">
        <div className="bg-white rounded-xl shadow p-4 mb-6 flex justify-between items-center">
          <p className="text-gray-600">최대 4석까지 선택 가능합니다.</p>
          <span className="bg-blue-100 text-blue-800 font-bold px-3 py-1 rounded-full">
            {selectedSeats.length}석 선택
          </span>
        </div>

        <div className="bg-white rounded-xl shadow p-4 mb-4 flex gap-4 text-sm">
          <div className="flex items-center gap-2"><div className="w-4 h-4 bg-white border border-gray-300 rounded"></div> 선택 가능</div>
          <div className="flex items-center gap-2"><div className="w-4 h-4 bg-green-500 rounded"></div> 선택됨</div>
          <div className="flex items-center gap-2"><div className="w-4 h-4 bg-orange-400 rounded"></div> 선점 중</div>
          <div className="flex items-center gap-2"><div className="w-4 h-4 bg-gray-400 rounded"></div> 예매 완료</div>
        </div>

        {zones.map(zone => (
          <div key={zone.zoneId} className="bg-white rounded-xl shadow p-6 mb-4">
            <div className="flex justify-between items-center mb-4">
              <h2 className="font-bold text-gray-800 text-lg">{zone.zoneName}</h2>
              <span className="text-blue-600 font-bold">{zone.price.toLocaleString()}원</span>
            </div>
            <div className="flex flex-wrap gap-2">
              {zone.seats.map(seat => (
                <button
                  key={seat.seatId}
                  onClick={() => seat.status === 'AVAILABLE' && toggleSeat(seat.seatId)}
                  className={`w-12 h-12 rounded text-xs font-medium transition ${
                    seat.status === 'RESERVED' ? 'bg-gray-400 text-white cursor-not-allowed' :
                    seat.status === 'LOCKED' ? 'bg-orange-400 text-white cursor-not-allowed' :
                    selectedSeats.includes(seat.seatId) ? 'bg-green-500 text-white' :
                    'bg-white border border-gray-300 hover:border-blue-500'
                  }`}
                >
                  {seat.row}{seat.number}
                </button>
              ))}
            </div>
          </div>
        ))}

        <div className="fixed bottom-0 left-0 right-0 bg-white border-t shadow-lg p-4">
          <div className="max-w-6xl mx-auto">
            <button
              onClick={handleLock}
              className="w-full bg-blue-900 text-white py-3 rounded-lg font-bold text-lg hover:bg-blue-800 transition"
            >
              선택 완료 ({selectedSeats.length}석)
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}