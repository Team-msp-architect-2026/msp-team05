import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import api from '../api/axios';
import Navbar from '../components/Navbar';
import Spinner from '../components/Spinner';

interface Seat {
  seatId: string;
  rowNum: string;
  number: string;
  status: string;
  zone: string;
}

const ZONES = [
  { id: 'zone-001', name: '1루 지정석', color: '#3B82F6', price: 15000 },
  { id: 'zone-002', name: '3루 지정석', color: '#8B5CF6', price: 15000 },
  { id: 'zone-003', name: '외야 응원석', color: '#EF4444', price: 10000 },
  { id: 'zone-004', name: '내야 지정석', color: '#10B981', price: 20000 },
];

export default function SeatSelectPage() {
  const { gameId } = useParams();
  const navigate = useNavigate();
  const [seats, setSeats] = useState<Seat[]>([]);
  const [selectedSeats, setSelectedSeats] = useState<string[]>([]);
  const [selectedZone, setSelectedZone] = useState<string>('zone-001');
  const [loading, setLoading] = useState(true);
  const stompClient = useRef<Client | null>(null);

  // API 호출용 useEffect
  useEffect(() => {
    const fetchSeats = async () => {
      try {
        const res = await api.get(`/api/games/${gameId}/seats`);
        console.log('시트응답', res.data);
        const zones = res.data.data.zones;
        const seatList: Seat[] = [];
        zones.forEach((zone: any) => {
          zone.seats.forEach((seat: any) => {
            seatList.push({
              seatId: seat.seatId,
              rowNum: seat.rowNum,
              number: seat.number,
              status: seat.status,
              zone: zone.zoneId,
            });
          });
        });
        setSeats(seatList);
      } catch (e) {
        console.error('좌석 조회 실패', e);
      } finally {
        setLoading(false);
      }
    };
    fetchSeats();
  }, [gameId]);

  // WebSocket 연결용 useEffect 분리
  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS(
        `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/ws/seats`
      ),
      onConnect: () => {
        client.subscribe(`/topic/seats/${gameId}`, (message) => {
          const data = JSON.parse(message.body);
          setSeats(prev => prev.map(seat =>
            seat.seatId === data.seatId
              ? { ...seat, status: data.status }
              : seat
          ));
        });
      },
    });
    client.activate();
    stompClient.current = client;
    return () => { client.deactivate(); };
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
      localStorage.setItem('gameId', gameId!);
      navigate('/checkout');
    } catch {
      alert('좌석 선점에 실패했습니다. 다시 시도해주세요.');
    }
  };

  if (loading) return <Spinner />;

  const currentZoneSeats = seats.filter(s => s.zone === selectedZone);
  const currentZone = ZONES.find(z => z.id === selectedZone);
  const rows = [...new Set(currentZoneSeats.map(s => s.rowNum))];

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <div className="max-w-6xl mx-auto px-4 py-8">

        {/* 야구장 시각화 */}
        <div className="bg-white rounded-xl shadow p-6 mb-6">
          <h2 className="text-xl font-bold text-gray-800 mb-4 text-center">구역 선택</h2>
          <div className="relative w-full max-w-lg mx-auto" style={{ height: '300px' }}>
            <div className="absolute inset-0 flex items-center justify-center">
              <div className="w-40 h-40 bg-green-600 rounded-full flex items-center justify-center">
                <div className="w-24 h-24 bg-yellow-600 rounded-full flex items-center justify-center">
                  <span className="text-white font-bold text-xs text-center">홈플레이트</span>
                </div>
              </div>
            </div>

            <button
              onClick={() => setSelectedZone('zone-001')}
              className={`absolute right-4 top-1/2 -translate-y-1/2 w-24 h-20 rounded-lg font-bold text-white text-xs transition ${
                selectedZone === 'zone-001' ? 'ring-4 ring-yellow-400 scale-110' : 'opacity-80 hover:opacity-100'
              }`}
              style={{ backgroundColor: '#3B82F6' }}
            >
              1루<br/>지정석<br/>15,000원
            </button>

            <button
              onClick={() => setSelectedZone('zone-002')}
              className={`absolute left-4 top-1/2 -translate-y-1/2 w-24 h-20 rounded-lg font-bold text-white text-xs transition ${
                selectedZone === 'zone-002' ? 'ring-4 ring-yellow-400 scale-110' : 'opacity-80 hover:opacity-100'
              }`}
              style={{ backgroundColor: '#8B5CF6' }}
            >
              3루<br/>지정석<br/>15,000원
            </button>

            <button
              onClick={() => setSelectedZone('zone-003')}
              className={`absolute top-2 left-1/2 -translate-x-1/2 w-32 h-16 rounded-lg font-bold text-white text-xs transition ${
                selectedZone === 'zone3' ? 'ring-4 ring-yellow-400 scale-110' : 'opacity-80 hover:opacity-100'
              }`}
              style={{ backgroundColor: '#EF4444' }}
            >
              외야 응원석<br/>10,000원
            </button>

            <button
              onClick={() => setSelectedZone('zone-004')}
              className={`absolute bottom-2 left-1/2 -translate-x-1/2 w-32 h-16 rounded-lg font-bold text-white text-xs transition ${
                selectedZone === 'zone4' ? 'ring-4 ring-yellow-400 scale-110' : 'opacity-80 hover:opacity-100'
              }`}
              style={{ backgroundColor: '#10B981' }}
            >
              내야 지정석<br/>20,000원
            </button>
          </div>
        </div>

        {/* 선택된 구역 좌석 */}
        <div className="bg-white rounded-xl shadow p-6 mb-24">
          <div className="flex justify-between items-center mb-4">
            <h2 className="font-bold text-gray-800 text-lg">
              {currentZone?.name}
              <span className="ml-2 text-blue-600">{currentZone?.price.toLocaleString()}원</span>
            </h2>
            <span className="bg-blue-100 text-blue-800 font-bold px-3 py-1 rounded-full text-sm">
              {selectedSeats.length}석 선택
            </span>
          </div>

          <div className="flex gap-4 text-xs mb-4">
            <div className="flex items-center gap-1"><div className="w-3 h-3 bg-white border border-gray-300 rounded"></div> 선택 가능</div>
            <div className="flex items-center gap-1"><div className="w-3 h-3 bg-green-500 rounded"></div> 선택됨</div>
            <div className="flex items-center gap-1"><div className="w-3 h-3 bg-orange-400 rounded"></div> 선점 중</div>
            <div className="flex items-center gap-1"><div className="w-3 h-3 bg-gray-400 rounded"></div> 매진</div>
          </div>

          <div className="text-center text-xs text-gray-400 mb-4 bg-gray-100 py-1 rounded">
            ▲ 필드 방향
          </div>

          <div className="overflow-x-auto">
            {rows.map(row => (
              <div key={row} className="flex items-center gap-1 mb-1">
                <span className="text-xs text-gray-400 w-4 font-bold">{row}</span>
                <div className="flex gap-1 flex-wrap">
                  {currentZoneSeats
                    .filter(s => s.rowNum === row)
                    .sort((a, b) => parseInt(a.number) - parseInt(b.number))
                    .map(seat => (
                      <button
                        key={seat.seatId}
                        onClick={() => seat.status === 'AVAILABLE' && toggleSeat(seat.seatId)}
                        className={`w-8 h-8 rounded text-xs font-medium transition ${
                          seat.status === 'RESERVED' ? 'bg-gray-400 text-white cursor-not-allowed' :
                          seat.status === 'LOCKED' ? 'bg-orange-400 text-white cursor-not-allowed' :
                          selectedSeats.includes(seat.seatId) ? 'bg-green-500 text-white' :
                          'bg-white border border-gray-300 hover:border-blue-500'
                        }`}
                        title={`${seat.rowNum}열 ${seat.number}번`}
                      >
                        {seat.number}
                      </button>
                    ))}
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="fixed bottom-0 left-0 right-0 bg-white border-t shadow-lg p-4">
          <div className="max-w-6xl mx-auto flex justify-between items-center">
            <div>
              <p className="text-sm text-gray-500">선택한 좌석</p>
              <p className="font-bold text-blue-900">{selectedSeats.length}석 / 최대 4석</p>
            </div>
            <button
              onClick={handleLock}
              className="bg-blue-900 text-white px-8 py-3 rounded-lg font-bold text-lg hover:bg-blue-800 transition"
            >
              선택 완료
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
