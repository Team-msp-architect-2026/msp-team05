import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import Navbar from '../components/Navbar';
import Spinner from '../components/Spinner';

interface Game {
  gameId: string;
  homeTeam: { name: string; logoUrl: string };
  awayTeam: { name: string; logoUrl: string };
  stadium: { name: string };
  startTime: string;
  status: string;
  remainingSeats: number;
}

export default function MainPage() {
  const navigate = useNavigate();
  const [games, setGames] = useState<Game[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/api/games')
      .then(res => setGames(res.data.data.content))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <Spinner />;

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <div className="bg-blue-800 text-white py-12 text-center">
        <h2 className="text-3xl font-bold mb-2">2025 KBO 리그</h2>
        <p className="text-blue-200">지금 바로 티켓을 예매하세요</p>
      </div>

      <div className="max-w-6xl mx-auto px-4 py-8">
        <h2 className="text-xl font-bold mb-4 text-gray-800">경기 일정</h2>
        {games.length === 0 && (
          <div className="text-center py-12 text-gray-500">
            <p className="text-lg">등록된 경기가 없습니다.</p>
          </div>
        )}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {games.map(game => (
            <div
              key={game.gameId}
              onClick={() => navigate(`/games/${game.gameId}`)}
              className="bg-white rounded-xl shadow hover:shadow-md cursor-pointer transition p-4 border border-gray-100"
            >
              <div className="flex justify-between items-center mb-3">
                <span className="text-sm text-gray-500">{game.stadium.name}</span>
                <span className={`text-xs px-2 py-1 rounded-full font-bold ${
                  game.status === 'ON_SALE' ? 'bg-green-100 text-green-700' :
                  game.status === 'SOLD_OUT' ? 'bg-red-100 text-red-700' :
                  'bg-gray-100 text-gray-500'
                }`}>
                  {game.status === 'ON_SALE' ? '예매 중' :
                   game.status === 'SOLD_OUT' ? '매진' :
                   game.status === 'SCHEDULED' ? '예매 예정' : '종료'}
                </span>
              </div>
              <div className="flex justify-between items-center text-lg font-bold text-gray-800 mb-2">
                <span>{game.homeTeam.name}</span>
                <span className="text-gray-400 text-sm">VS</span>
                <span>{game.awayTeam.name}</span>
              </div>
              <div className="text-sm text-gray-500">
                {new Date(game.startTime).toLocaleString('ko-KR')}
              </div>
              <div className="mt-2 text-sm text-blue-600 font-medium">
                잔여 좌석: {game.remainingSeats}석
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}