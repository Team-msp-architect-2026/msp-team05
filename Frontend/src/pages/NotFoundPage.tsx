import { useNavigate } from 'react-router-dom';

export default function NotFoundPage() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center">
      <div className="text-center">
        <p className="text-8xl mb-4">⚾</p>
        <h1 className="text-6xl font-bold text-blue-900 mb-4">404</h1>
        <p className="text-xl text-gray-600 mb-2">페이지를 찾을 수 없습니다.</p>
        <p className="text-gray-400 mb-8">요청하신 페이지가 존재하지 않습니다.</p>
        <button
          onClick={() => navigate('/')}
          className="bg-blue-900 text-white px-8 py-3 rounded-lg font-bold hover:bg-blue-800 transition"
        >
          메인으로 돌아가기
        </button>
      </div>
    </div>
  );
}