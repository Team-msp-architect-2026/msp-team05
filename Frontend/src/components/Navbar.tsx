import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function Navbar() {
  const navigate = useNavigate();
  const isLoggedIn = !!localStorage.getItem('accessToken');
  const username = localStorage.getItem('username');
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('username');
    navigate('/login');
  };

  return (
    <header className="bg-blue-900 text-white shadow-lg">
      <div className="max-w-6xl mx-auto px-4 py-4 flex justify-between items-center">

        {/* 로고 */}
        <div
          className="flex items-center gap-2 cursor-pointer"
          onClick={() => navigate('/')}
        >
          <span className="text-2xl">⚾</span>
          <span className="text-xl font-bold">KKY 티켓팅</span>
        </div>

        {/* 데스크탑 메뉴 */}
        <div className="hidden md:flex items-center gap-4">
          {isLoggedIn ? (
            <>
              {username && (
                <span className="text-sm text-yellow-300 font-semibold">
                  {username}님
                </span>
              )}
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

        {/* 모바일 햄버거 메뉴 */}
        <button
          className="md:hidden text-white text-2xl"
          onClick={() => setMenuOpen(!menuOpen)}
        >
          {menuOpen ? '✕' : '☰'}
        </button>
      </div>

      {/* 모바일 드롭다운 메뉴 */}
      {menuOpen && (
        <div className="md:hidden bg-blue-800 px-4 py-3 flex flex-col gap-3">
          {isLoggedIn && username && (
            <span className="text-sm text-yellow-300 font-semibold py-2 border-b border-blue-700">
              {username}님
            </span>
          )}
          <button
            onClick={() => { navigate('/reservations'); setMenuOpen(false); }}
            className="text-left text-sm hover:text-yellow-300 transition py-2 border-b border-blue-700"
          >
            예매 내역
          </button>
          {isLoggedIn ? (
            <button
              onClick={() => { handleLogout(); setMenuOpen(false); }}
              className="text-left text-sm hover:text-yellow-300 transition py-2"
            >
              로그아웃
            </button>
          ) : (
            <button
              onClick={() => { navigate('/login'); setMenuOpen(false); }}
              className="text-left text-sm hover:text-yellow-300 transition py-2"
            >
              로그인
            </button>
          )}
        </div>
      )}
    </header>
  );
}