import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import Navbar from '../components/Navbar';
import Spinner from '../components/Spinner';

interface Reservation {
  reservationId: string;
  homeTeam: string;
  awayTeam: string;
  stadiumName: string;
  startTime: string;
  zoneName: string;
  rowNum: string;
  number: string;
  price: number;
  status: string;
  createdAt: string;
}

export default function ReservationPage() {
  const navigate = useNavigate();
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get('/api/reservations')
      .then(res => setReservations(res.data.data || []))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, []);

  const handleCancel = async (reservationId: string) => {
    if (!window.confirm('예매를 취소하시겠습니까?')) return;
    try {
      await api.delete(`/api/reservations/${reservationId}`);
      setReservations(reservations.filter(r => r.reservationId !== reservationId));
      alert('예매가 취소되었습니다.');
    } catch (err) {
      alert('예매 취소에 실패했습니다.');
    }
  };

  if (loading) return <Spinner />;

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <div className="max-w-6xl mx-auto px-4 py-8">
        <h1 className="text-2xl font-bold text-gray-800 mb-6">예매 내역</h1>
        {reservations.length === 0 && (
          <div className="text-center py-12">
            <p className="text-gray-500 text-lg">예매 내역이 없습니다.</p>
            <button
              onClick={() => navigate('/')}
              className="mt-4 bg-blue-900 text-white px-6 py-2 rounded-lg hover:bg-blue-800 transition"
            >
              경기 보러가기
            </button>
          </div>
        )}
        <div className="flex flex-col gap-4">
          {reservations.map(reservation => (
            <div key={reservation.reservationId} className="bg-white rounded-xl shadow p-6">
              <div className="flex justify-between items-start mb-3">
                <h3 className="font-bold text-gray-800 text-lg">
                  {reservation.homeTeam} vs {reservation.awayTeam}
                </h3>
                <span className="bg-green-100 text-green-700 text-xs px-2 py-1 rounded-full font-bold">
                  {reservation.status === 'CONFIRMED' ? '예매 완료' : reservation.status}
                </span>
              </div>
              <div className="text-sm text-gray-500 flex flex-col gap-1 mb-4">
                <p>예매 번호: {reservation.reservationId}</p>
                <p>경기장: {reservation.stadiumName}</p>
                <p>경기 시간: {new Date(reservation.startTime).toLocaleString('ko-KR')}</p>
                <p>좌석: {reservation.zoneName} {reservation.rowNum}열 {reservation.number}번</p>
                <p>결제 금액: {reservation.price?.toLocaleString()}원</p>
                <p>결제 시간: {new Date(reservation.createdAt).toLocaleString('ko-KR')}</p>
              </div>
              <div className="flex gap-2">
                <button
                  onClick={() => navigate(`/tickets/${reservation.reservationId}`)}
                  className="flex-1 bg-blue-900 text-white py-2 rounded-lg font-bold hover:bg-blue-800 transition"
                >
                  티켓 보기
                </button>
                <button
                  onClick={() => handleCancel(reservation.reservationId)}
                  className="flex-1 border border-red-300 text-red-500 py-2 rounded-lg font-bold hover:bg-red-50 transition"
                >
                  예매 취소
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}