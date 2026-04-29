import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import Navbar from '../components/Navbar';
import Spinner from '../components/Spinner';

interface GameDetail {
  gameId: string;
  homeTeam: { name: string; logoUrl: string };
  awayTeam: { name: string; logoUrl: string };
  stadium: { name: string };
  startTime: string;
  ticketOpenTime: string;
  status: string;
  seatZones: {
    zoneId: string;
    zoneName: string;
    price: number;
    remainingSeats: number;
  }[];
}

export default function GameDetailPage() {
  const { gameId } = useParams();
  const navigate = useNavigate();
  const [game, setGame] = useState<GameDetail | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get(`/api/games/${gameId}`)
      .then(res => setGame(res.data.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, [gameId]);

  if (loading) return <Spinner />;
  if (!game) return (
    <div className="min-h-screen flex items-center justify-center">
      <p className="text-gray-500">경기 정보를 찾을 수 없습니다.</p>
    </div>
  );

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <div className="max-w-6xl mx-auto px-4 py-8">
        <div className="bg-white rounded-xl shadow p-6 mb-6">
          <div className="flex justify-between items-center mb-4">
            <span className="text-gray-500">{game.stadium.name}</span>
            <span className={`text-xs px-3 py-1 rounded-full font-bold ${
              game.status === 'ON_SALE' ? 'bg-green-100 text-green-700' :
              game.status === 'SOLD_OUT' ? 'bg-red-100 text-red-700' :
              'bg-gray-100 text-gray-500'
            }`}>
              {game.status === 'ON_SALE' ? '예매 중' :
               game.status === 'SOLD_OUT' ? '매진' :
               game.status === 'SCHEDULED' ? '예매 예정' : '종료'}
            </span>
          </div>
          <div className="flex justify-center items-center gap-8 my-6">
            <div className="text-center">
              <p className="text-2xl font-bold text-blue-900">{game.homeTeam.name}</p>
              <p className="text-sm text-gray-500">홈</p>
            </div>
            <p className="text-3xl font-bold text-gray-300">VS</p>
            <div className="text-center">
              <p className="text-2xl font-bold text-blue-900">{game.awayTeam.name}</p>
              <p className="text-sm text-gray-500">원정</p>
            </div>
          </div>
          <div className="text-center text-gray-600">
            <p>경기 시간: {new Date(game.startTime).toLocaleString('ko-KR')}</p>
            <p>예매 오픈: {new Date(game.ticketOpenTime).toLocaleString('ko-KR')}</p>
          </div>
        </div>

        <h2 className="text-xl font-bold mb-4 text-gray-800">구역별 잔여 좌석</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {game.seatZones.map(zone => (
            <div key={zone.zoneId} className="bg-white rounded-xl shadow p-4">
              <div className="flex justify-between items-center mb-2">
                <h3 className="font-bold text-gray-800">{zone.zoneName}</h3>
                <span className="text-blue-600 font-bold">{zone.price.toLocaleString()}원</span>
              </div>
              <p className="text-gray-500 text-sm mb-3">잔여 좌석: {zone.remainingSeats}석</p>
              <button
                onClick={() => navigate(`/games/${gameId}/seats`)}
                disabled={zone.remainingSeats === 0}
                className={`w-full py-2 rounded-lg font-bold transition ${
                  zone.remainingSeats === 0
                    ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                    : 'bg-blue-900 text-white hover:bg-blue-800'
                }`}
              >
                {zone.remainingSeats === 0 ? '매진' : '좌석 선택'}
              </button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}