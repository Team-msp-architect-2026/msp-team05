//import Navbar from '../components/Navbar';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';

export default function CheckoutPage() {
  const navigate = useNavigate();
  const [paymentMethod, setPaymentMethod] = useState('CARD');
  const [loading, setLoading] = useState(false);

  const handlePayment = async () => {
    const lockToken = localStorage.getItem('lockToken');
    const gameId = localStorage.getItem('gameId');
    if (!lockToken || !gameId) {
      alert('좌석 선점 정보가 없습니다. 다시 시도해주세요.');
      navigate('/');
      return;
    }

    setLoading(true);
    try {
    // 1. 결제 준비
      const prepareRes = await api.post('/api/payments/prepare', {
        lockToken,
        paymentMethod,
      });

      const { orderId } = prepareRes.data.data;

    // 2. 결제 승인 (Mock PG)
      //const confirmRes = await api.post('/api/payments/confirm', {
      await api.post('/api/payments/confirm', {
        orderId,
        pgPaymentId: orderId,
      });

      //const paymentId = confirmRes.data.data.paymentId;

    // 3. 예매 확정
      await api.post('/api/reservations', {
        lockToken,
        gameId,
      });

      localStorage.removeItem('lockToken');
      localStorage.removeItem('gameId');
      alert('예매가 완료되었습니다!');
      navigate('/reservations');
    } catch (err) {
      alert('결제에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setLoading(false);
    }
  };

  const methods = [
    { value: 'CARD', label: '💳 신용카드' },
    { value: 'KAKAO_PAY', label: '💛 카카오페이' },
    { value: 'NAVER_PAY', label: '💚 네이버페이' },
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-blue-900 text-white shadow-lg">
        <div className="max-w-6xl mx-auto px-4 py-4 flex items-center gap-4">
          <button onClick={() => navigate(-1)} className="text-white hover:text-yellow-300">← 뒤로</button>
          <h1 className="text-xl font-bold">결제</h1>
        </div>
      </header>

      <div className="max-w-md mx-auto px-4 py-8">
        <div className="bg-white rounded-xl shadow p-6 mb-6">
          <h2 className="font-bold text-gray-800 text-lg mb-4">결제 수단 선택</h2>
          <div className="flex flex-col gap-3">
            {methods.map(method => (
              <label
                key={method.value}
                className={`flex items-center gap-3 p-4 rounded-lg border-2 cursor-pointer transition ${
                  paymentMethod === method.value
                    ? 'border-blue-500 bg-blue-50'
                    : 'border-gray-200 hover:border-gray-300'
                }`}
              >
                <input
                  type="radio"
                  name="paymentMethod"
                  value={method.value}
                  checked={paymentMethod === method.value}
                  onChange={() => setPaymentMethod(method.value)}
                  className="accent-blue-600"
                />
                <span className="font-medium text-gray-700">{method.label}</span>
              </label>
            ))}
          </div>
        </div>

        <button
          onClick={handlePayment}
          disabled={loading}
          className={`w-full py-4 rounded-xl font-bold text-lg transition ${
            loading
              ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
              : 'bg-blue-900 text-white hover:bg-blue-800'
          }`}
        >
          {loading ? '결제 처리 중...' : '결제하기'}
        </button>

        <button
          onClick={() => navigate(-1)}
          className="w-full py-3 mt-3 rounded-xl font-medium text-gray-600 border border-gray-300 hover:bg-gray-50 transition"
        >
          취소
        </button>
      </div>
    </div>
  );
}