import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axios';
import Navbar from '../components/Navbar';
import Spinner from '../components/Spinner';

interface Ticket {
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

export default function TicketPage() {
  const { ticketId } = useParams();
  const navigate = useNavigate();
  const [ticket, setTicket] = useState<Ticket | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.get(`/api/reservations/${ticketId}`)
      .then(res => setTicket(res.data.data))
      .catch(err => console.error(err))
      .finally(() => setLoading(false));
  }, [ticketId]);

  if (loading) return <Spinner />;
  if (!ticket) return (
    <div className="min-h-screen flex items-center justify-center">
      <p className="text-gray-500">티켓 정보를 찾을 수 없습니다.</p>
    </div>
  );

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-md mx-auto px-4 py-8">
        <div className="bg-white rounded-xl shadow overflow-hidden">
          <div className="bg-blue-900 text-white p-6 text-center">
            <h2 className="text-2xl font-bold mb-1">
              {ticket.homeTeam} vs {ticket.awayTeam}
            </h2>
            <p className="text-blue-200 text-sm">
              {new Date(ticket.startTime).toLocaleString('ko-KR')}
            </p>
          </div>

          <div className="p-6">
            <p className="text-center text-gray-500 text-sm mb-6">
              예매 번호: {ticket.reservationId}
            </p>

            <div className="border border-gray-200 rounded-xl p-4 mb-4 text-center">
              <p className="font-bold text-gray-800 mb-1">경기장: {ticket.stadiumName}</p>
              <p className="text-gray-600">
                좌석: {ticket.zoneName} {ticket.rowNum}열 {ticket.number}번
              </p>
            </div>

            <div className="border-t border-dashed border-gray-200 pt-4 mt-4">
              <div className="flex justify-between text-sm text-gray-600 mb-2">
                <span>결제 금액</span>
                <span className="font-bold text-blue-900">
                  {ticket.price?.toLocaleString()}원
                </span>
              </div>
              <div className="flex justify-between text-sm text-gray-600 mb-2">
                <span>상태</span>
                <span className="text-green-600 font-bold">
                  {ticket.status === 'CONFIRMED' ? '예매 완료' : ticket.status}
                </span>
              </div>
              <div className="flex justify-between text-sm text-gray-600">
                <span>예매 시간</span>
                <span>{new Date(ticket.createdAt).toLocaleString('ko-KR')}</span>
              </div>
            </div>
          </div>
        </div>

        <button
          onClick={() => navigate('/')}
          className="w-full mt-4 bg-blue-900 text-white py-3 rounded-xl font-bold hover:bg-blue-800 transition"
        >
          메인으로
        </button>
      </div>
    </div>
  );
}