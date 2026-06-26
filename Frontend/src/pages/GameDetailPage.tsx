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
  const [activeTab, setActiveTab] = useState<'booking' | 'seating'>('booking');

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

  const isExpired = new Date(game.startTime) < new Date();

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <div className="max-w-6xl mx-auto px-4 py-8">
        {/* 경기 정보 카드 */}
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

        {/* 탭 버튼 */}
        <div className="flex border-b border-gray-200 mb-6">
          <button
            onClick={() => setActiveTab('booking')}
            className={`px-6 py-3 font-bold text-sm transition border-b-2 ${
              activeTab === 'booking'
                ? 'border-blue-900 text-blue-900'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            예매/관람안내
          </button>
          <button
            onClick={() => setActiveTab('seating')}
            className={`px-6 py-3 font-bold text-sm transition border-b-2 ${
              activeTab === 'seating'
                ? 'border-blue-900 text-blue-900'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            좌석도/가격
          </button>
        </div>

        {/* 탭 1: 예매/관람안내 */}
        {activeTab === 'booking' && (
          <div>
            {/* 예매 안내 */}
            <div className="bg-white rounded-xl shadow p-6 mb-6">
              <h3 className="font-bold text-gray-800 mb-4">예매안내</h3>
              <ul className="text-sm text-gray-600 space-y-2">
                <li>• 예매 가능시간: 경기당일 경기시작 1시간 전까지</li>
                <li>• 예매티켓 취소 가능시간: 경기당일 경기시작 4시간 전까지</li>
                <li>• 1회 최대 예매수량: 4석</li>
                <li>• 예매수수료: 장당 1,000원</li>
                <li className="text-red-500 font-medium">• 예매하신 티켓의 전매, 위조 등의 위법행위를 엄격히 금지합니다.</li>
              </ul>
            </div>

            {/* 구역별 좌석 선택 */}
            <h2 className="text-xl font-bold mb-4 text-gray-800">구역 선택</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {game.seatZones.map(zone => (
                <div key={zone.zoneId} className="bg-white rounded-xl shadow p-4">
                  <div className="flex justify-between items-center mb-2">
                    <h3 className="font-bold text-gray-800">{zone.zoneName}</h3>
                    <span className="text-blue-600 font-bold">{zone.price.toLocaleString()}원</span>
                  </div>

                  {game.status === 'SCHEDULED' ? (
                    <div className="mb-3">
                      <p className="text-sm text-gray-500">예매 예정일</p>
                      <p className="font-bold text-gray-700">
                        {new Date(game.ticketOpenTime).toLocaleString('ko-KR')}
                      </p>
                    </div>
                  ) : (
                    <p className="text-gray-500 text-sm mb-3">잔여 좌석: {zone.remainingSeats}석</p>
                  )}

                  <button
                    onClick={() => navigate(`/games/${gameId}/queue`)}
                    disabled={
                      game.status !== 'ON_SALE' ||
                      zone.remainingSeats === 0 ||
                      isExpired
                    }
                    className={`w-full py-2 rounded-lg font-bold transition ${
                      isExpired
                        ? 'bg-gray-200 text-gray-400 cursor-not-allowed' :
                      game.status === 'SCHEDULED'
                        ? 'bg-yellow-100 text-yellow-700 cursor-not-allowed' :
                      zone.remainingSeats === 0
                        ? 'bg-gray-100 text-gray-400 cursor-not-allowed' :
                      game.status !== 'ON_SALE'
                        ? 'bg-gray-100 text-gray-400 cursor-not-allowed' :
                      'bg-blue-900 text-white hover:bg-blue-800'
                    }`}
                  >
                    {isExpired ? '종료된 경기' :
                     game.status === 'SCHEDULED' ? '예매 예정' :
                     zone.remainingSeats === 0 ? '매진' :
                     game.status !== 'ON_SALE' ? '예매 불가' : '좌석 선택'}
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* 탭 2: 좌석도/가격 */}
        {activeTab === 'seating' && (
          <div>
            <div className="bg-white rounded-xl shadow p-6 mb-6">
              <h3 className="font-bold text-gray-800 mb-4">좌석도</h3>
              <div className="flex justify-center">
                <svg viewBox="0 0 420 400" className="w-full max-w-md" xmlns="http://www.w3.org/2000/svg">
                  <ellipse cx="210" cy="200" rx="185" ry="175" fill="#1b5e35" />
                  <path d="M210,40 A185,175,0,0,1,395,200 L330,200 A120,113,0,0,0,210,95 Z" fill="#16a34a" stroke="white" strokeWidth="1" />
                  <path d="M395,200 A185,175,0,0,1,210,375 L210,310 A120,113,0,0,0,330,200 Z" fill="#1565c0" stroke="white" strokeWidth="1" />
                  <path d="M210,375 A185,175,0,0,1,25,200 L90,200 A120,113,0,0,0,210,310 Z" fill="#ca8a04" stroke="white" strokeWidth="1" />
                  <path d="M25,200 A185,175,0,0,1,210,40 L210,95 A120,113,0,0,0,90,200 Z" fill="#c1121f" stroke="white" strokeWidth="1" />
                  <ellipse cx="210" cy="200" rx="70" ry="65" fill="#1e7a40" />
                  <path d="M210,162 L248,200 L210,238 L172,200 Z" fill="#c8a063" stroke="#b8905a" strokeWidth="0.5" />
                  <ellipse cx="210" cy="191" rx="7" ry="5" fill="#b8905a" />
                  <text x="290" y="130" textAnchor="middle" fill="white" fontSize="12" fontWeight="bold">1루 지정석</text>
                  <text x="290" y="280" textAnchor="middle" fill="white" fontSize="12" fontWeight="bold">내야 지정석</text>
                  <text x="130" y="280" textAnchor="middle" fill="white" fontSize="12" fontWeight="bold">3루 지정석</text>
                  <text x="210" y="60" textAnchor="middle" fill="white" fontSize="12" fontWeight="bold">2루 지정석</text>
                </svg>
              </div>
            </div>

            {/* 가격표 */}
            <div className="bg-white rounded-xl shadow p-6">
              <h3 className="font-bold text-gray-800 mb-4">가격표</h3>
              <table className="w-full text-sm">
                <thead>
                  <tr className="bg-gray-50">
                    <th className="text-left py-3 px-4 font-bold text-gray-700">구역</th>
                    <th className="text-right py-3 px-4 font-bold text-gray-700">가격</th>
                    <th className="text-right py-3 px-4 font-bold text-gray-700">잔여석</th>
                  </tr>
                </thead>
                <tbody>
                  {game.seatZones.map((zone, index) => (
                    <tr key={zone.zoneId} className={index % 2 === 0 ? 'bg-white' : 'bg-gray-50'}>
                      <td className="py-3 px-4 text-gray-800 font-medium">{zone.zoneName}</td>
                      <td className="py-3 px-4 text-right text-blue-600 font-bold">{zone.price.toLocaleString()}원</td>
                      <td className="py-3 px-4 text-right">
                        {game.status === 'SCHEDULED' ? (
                          <span className="text-yellow-600 font-medium">판매예정</span>
                        ) : zone.remainingSeats === 0 ? (
                          <span className="text-red-500 font-medium">매진</span>
                        ) : (
                          <span className="text-green-600 font-medium">{zone.remainingSeats}석</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}