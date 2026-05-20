import { useEffect, useState, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../api/axios';
import Navbar from '../components/Navbar';

interface QueueStatus {
  position: number;
  estimatedWaitSeconds: number;
  status: string;
  entryAllowed: boolean;
}

export default function QueuePage() {
  const { gameId } = useParams();
  const navigate = useNavigate();
  const [queueToken, setQueueToken] = useState<string | null>(null);
  const [queueStatus, setQueueStatus] = useState<QueueStatus | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const hasEntered = useRef(false);

  // 대기열 진입
  useEffect(() => {
    if (hasEntered.current) return;
    hasEntered.current = true;

    const token = localStorage.getItem('accessToken');
    if (!token) {
      alert('로그인 후 이용해주세요.');
      navigate('/login');
      return;
    }

    api.post('/api/queue/enter', { gameId })
      .then(res => {
        const qToken = res.data.data.queueToken;
        setQueueToken(qToken);
        localStorage.setItem('queueToken', qToken);
        setQueueStatus({
          position: res.data.data.position,
          estimatedWaitSeconds: res.data.data.estimatedWaitSeconds,
          status: 'WAITING',
          entryAllowed: false,
        });
      })
      .catch((err) => {
        if (err.response?.status === 409) {
          setError('이미 대기열에 진입한 경기입니다. 기존 창을 확인해주세요.');
        } else {
          setError('대기열 진입에 실패했습니다.');
        }
      })
      .finally(() => setLoading(false));
  }, [gameId]);

  // 순번 폴링 (3초마다)
  useEffect(() => {
    if (!queueToken) return;

    const interval = setInterval(() => {
      api.get(`/api/queue/status/${queueToken}`)
        .then(res => {
          const data = res.data.data;
          setQueueStatus(data);

          if (data.status === 'ALLOWED') {
            clearInterval(interval);
            navigate(`/games/${gameId}/seats`);
          }

          if (data.status === 'EXPIRED') {
            clearInterval(interval);
            setError('대기 시간이 초과되었습니다. 다시 시도해주세요.');
          }
        })
        .catch(() => clearInterval(interval));
    }, 3000);

    return () => clearInterval(interval);
  }, [queueToken, gameId, navigate]);

  // 대기열 이탈
  const handleExit = async () => {
    if (!queueToken) return;
    try {
      await api.delete(`/api/queue/exit/${queueToken}`);
      navigate(`/games/${gameId}`);
    } catch {
      navigate(`/games/${gameId}`);
    }
  };

  const formatTime = (seconds: number) => {
    const min = Math.floor(seconds / 60);
    const sec = seconds % 60;
    return min > 0 ? `${min}분 ${sec}초` : `${sec}초`;
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <div className="max-w-md mx-auto px-4 py-16 text-center">
        {loading && (
          <div>
            <p className="text-gray-500">대기열 진입 중...</p>
          </div>
        )}

        {error && (
          <div className="bg-red-50 border border-red-200 rounded-xl p-6">
            <p className="text-red-500 mb-4">{error}</p>
            <button
              onClick={() => navigate(`/games/${gameId}`)}
              className="bg-blue-900 text-white px-6 py-2 rounded-lg hover:bg-blue-800 transition"
            >
              돌아가기
            </button>
          </div>
        )}

        {!loading && !error && queueStatus && (
          <div className="bg-white rounded-xl shadow p-8">
            <div className="text-6xl mb-6">⏳</div>
            <h1 className="text-2xl font-bold text-blue-900 mb-2">대기 중</h1>
            <p className="text-gray-500 mb-8">잠시만 기다려주세요</p>

            <div className="bg-blue-50 rounded-xl p-6 mb-6">
              <div className="mb-4">
                <p className="text-sm text-gray-500 mb-1">현재 순번</p>
                <p className="text-4xl font-bold text-blue-900">
                  {queueStatus.position}번
                </p>
              </div>
              <div>
                <p className="text-sm text-gray-500 mb-1">예상 대기 시간</p>
                <p className="text-2xl font-bold text-blue-600">
                  {formatTime(queueStatus.estimatedWaitSeconds)}
                </p>
              </div>
            </div>

            <div className="flex items-center justify-center gap-2 mb-6">
              <div className="w-2 h-2 bg-blue-900 rounded-full animate-bounce"></div>
              <div className="w-2 h-2 bg-blue-900 rounded-full animate-bounce"
                style={{ animationDelay: '0.1s' }}></div>
              <div className="w-2 h-2 bg-blue-900 rounded-full animate-bounce"
                style={{ animationDelay: '0.2s' }}></div>
            </div>

            <button
              onClick={handleExit}
              className="w-full border border-red-300 text-red-500 py-2 rounded-lg hover:bg-red-50 transition"
            >
              대기열 이탈
            </button>
          </div>
        )}
      </div>
    </div>
  );
}