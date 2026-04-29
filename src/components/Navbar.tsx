import { useNavigate } from 'react-router-dom';

export default function Navbar() {
  const navigate = useNavigate();
  const isLoggedIn = !!localStorage.getItem('accessToken');

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    navigate('/login');
  };

  return (
    <header className="bg-blue-900 text-white shadow-lg">
      <div className="max-w-6xl mx-auto px-4 py-4 flex justify-between items-center">
        <div
          className="flex items-center gap-2 cursor-pointer"
          onClick={() => navigate('/')}
        >
          <span className="text-2xl">⚾</span>
          <span className="text-xl font-bold">KKY 티켓팅</span>
        </div>
        <div className="flex items-center gap-4">
          {isLoggedIn ? (
            <>
              <button
                onClick={() => navigate('/reservations')}
                className="text-sm hover:text-yellow-300 transition"
              >
                예매 내역
              </button>
              <button
                onClick={handleLogout}
                className="bg-yellow-400 text-blue-900 px-4 py-1 rounded font-bold text-sm hover:bg-yellow-300 transition"
              >
                로그아웃
              </button>
            </>
          ) : (
            <>
              <button
                onClick={() => navigate('/reservations')}
                className="text-sm hover:text-yellow-300 transition"
              >
                예매 내역
              </button>
              <button
                onClick={() => navigate('/login')}
                className="bg-yellow-400 text-blue-900 px-4 py-1 rounded font-bold text-sm hover:bg-yellow-300 transition"
              >
                로그인
              </button>
            </>
          )}
        </div>
      </div>
    </header>
  );
}