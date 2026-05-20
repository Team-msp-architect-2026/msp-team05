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
  ticketOpenTime: string;
  status: string;
}

export default function MainPage() {
  const navigate = useNavigate();
  const [games, setGames] = useState<Game[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const PAGE_SIZE = 6;

  useEffect(() => {
    setLoading(true);
    api.get(`/api/games?page=${page}&size=${PAGE_SIZE}`)
      .then(res => {
        setGames(res.data.data.content);
        setTotalPages(res.data.data.totalPages);
      })
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, [page]);

  const formatDate = (dateStr: string) => {
    const d = new Date(dateStr);
    return {
      month: d.getMonth() + 1,
      day: d.getDate(),
      weekday: ['일','월','화','수','목','금','토'][d.getDay()],
      time: d.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', hour12: false }),
    };
  };

  const formatOpenDate = (dateStr: string) => {
    const d = new Date(dateStr);
    return `${d.getMonth() + 1}.${String(d.getDate()).padStart(2, '0')} ${
      d.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit', hour12: false })
    } 오픈`;
  };

  const isExpired = (startTime: string) => new Date(startTime) < new Date();

  if (loading) return <Spinner />;

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <div className="bg-blue-900 text-white py-12 text-center">
        <h2 className="text-3xl font-bold mb-2">2026 KKY 리그</h2>
        <p className="text-blue-200">지금 바로 티켓을 예매하세요</p>
      </div>

      <div className="max-w-4xl mx-auto px-4 py-8">
        {/* 헤더 + 페이지네이션 */}
        <div className="flex items-center justify-between mb-4">
          <h2 className="text-xl font-bold text-gray-800">경기 일정</h2>
          <div className="flex items-center gap-2">
            <span className="text-sm font-bold text-gray-600">
              {page + 1} / {totalPages}
            </span>
            <button
              onClick={() => setPage(p => Math.max(0, p - 1))}
              disabled={page === 0}
              className="w-8 h-8 border border-gray-300 rounded flex items-center justify-center text-gray-600 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition"
            >
              {'<'}
            </button>
            <button
              onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
              className="w-8 h-8 border border-gray-300 rounded flex items-center justify-center text-gray-600 hover:bg-gray-100 disabled:opacity-30 disabled:cursor-not-allowed transition"
            >
              {'>'}
            </button>
          </div>
        </div>

        {games.length === 0 && (
          <div className="text-center py-12 text-gray-500">
            <p className="text-lg">등록된 경기가 없습니다.</p>
          </div>
        )}

        <div className="bg-white rounded-xl shadow overflow-hidden">
          {/* 테이블 헤더 */}
          <div className="grid grid-cols-12 bg-gray-100 text-sm text-gray-600 font-bold px-6 py-3 border-b">
            <div className="col-span-2">경기일시</div>
            <div className="col-span-6 text-center">경기명</div>
            <div className="col-span-2 text-center">경기장</div>
            <div className="col-span-2 text-center">티켓예매</div>
          </div>

          {games.map((game) => {
            const date = formatDate(game.startTime);
            const expired = isExpired(game.startTime);
            const isOnSale = game.status === 'ON_SALE';
            const isScheduled = game.status === 'SCHEDULED';

            return (
              <div
                key={game.gameId}
                className={`grid grid-cols-12 items-center px-6 py-4 border-b last:border-b-0 ${
                  expired ? 'bg-gray-50 opacity-60' : 'hover:bg-blue-50 transition'
                }`}
              >
                <div className="col-span-2">
                  <p className="text-lg font-bold text-gray-800">
                    {date.month}.{String(date.day).padStart(2, '0')}
                    <span className="text-sm text-gray-500 ml-1">({date.weekday})</span>
                  </p>
                  <p className="text-sm text-gray-500">{date.time}</p>
                </div>

                <div className="col-span-6 flex items-center justify-center gap-4">
                  <span className="font-bold text-gray-800">{game.homeTeam.name}</span>
                  <span className="text-gray-400 font-bold">VS</span>
                  <span className="font-bold text-gray-800">{game.awayTeam.name}</span>
                </div>

                <div className="col-span-2 text-center text-sm text-gray-600">
                  {game.stadium.name}
                </div>

                <div className="col-span-2 flex justify-center">
                  {expired ? (
                    <span className="text-sm text-gray-400">종료</span>
                  ) : isOnSale ? (
                    <button
                      onClick={() => navigate(`/games/${game.gameId}`)}
                      className="bg-blue-900 text-white px-4 py-2 rounded-lg text-sm font-bold hover:bg-blue-800 transition"
                    >
                      예매하기
                    </button>
                  ) : isScheduled ? (
                    <div className="text-center">
                      <p className="text-xs text-gray-500 font-bold">판매예정</p>
                      <p className="text-xs text-gray-400">
                        {game.ticketOpenTime
                          ? formatOpenDate(game.ticketOpenTime)
                          : '일정 미정'}
                      </p>
                    </div>
                  ) : (
                    <span className="text-sm text-red-500 font-bold">매진</span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}