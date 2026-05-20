import { useState, useEffect } from 'react';
import api from '../api/axios';
import Navbar from '../components/Navbar';

export default function AdminPage() {
  const [teams, setTeams] = useState<any[]>([]);
  const [stadiums, setStadiums] = useState<any[]>([]);
  const [message, setMessage] = useState('');
  const [gameForm, setGameForm] = useState({
    homeTeamId: '', awayTeamId: '', stadiumId: '',
    startTime: '', ticketOpenTime: ''
  });

  useEffect(() => {
    api.get('/api/admin/teams').then(res => setTeams(res.data.data || [])).catch(() => {});
    api.get('/api/admin/stadiums').then(res => setStadiums(res.data.data || [])).catch(() => {});
  }, []);

  const showMessage = (msg: string) => {
    setMessage(msg);
    setTimeout(() => setMessage(''), 3000);
  };

  const handleGame = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/api/admin/games', {
        ...gameForm,
        startTime: gameForm.startTime + ':00',
        ticketOpenTime: gameForm.ticketOpenTime
          ? gameForm.ticketOpenTime + ':00' : null,
      });
      showMessage('✅ 경기 등록 완료');
      setGameForm({
        homeTeamId: '', awayTeamId: '', stadiumId: '',
        startTime: '', ticketOpenTime: ''
      });
    } catch { showMessage('❌ 경기 등록 실패'); }
  };

  const inputClass = "w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500";
  const selectClass = "w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white";

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-3xl mx-auto px-4 py-8">
        <h1 className="text-2xl font-bold text-blue-900 mb-6">⚙️ 관리자 페이지</h1>

        {message && (
          <div className="mb-4 p-3 bg-blue-50 border border-blue-200 rounded-lg
            text-center font-medium">
            {message}
          </div>
        )}

        <div className="bg-white rounded-xl shadow p-6">
          <form onSubmit={handleGame}>
            <h2 className="text-lg font-bold mb-6">경기 등록</h2>

            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                홈팀 *
              </label>
              <select className={selectClass} value={gameForm.homeTeamId}
                onChange={e => setGameForm({...gameForm, homeTeamId: e.target.value})}
                required>
                <option value="">홈팀 선택</option>
                {teams.map((t: any) => (
                  <option key={t.teamId} value={t.teamId}>{t.name}</option>
                ))}
              </select>
            </div>

            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                원정팀 *
              </label>
              <select className={selectClass} value={gameForm.awayTeamId}
                onChange={e => setGameForm({...gameForm, awayTeamId: e.target.value})}
                required>
                <option value="">원정팀 선택</option>
                {teams.map((t: any) => (
                  <option key={t.teamId} value={t.teamId}>{t.name}</option>
                ))}
              </select>
            </div>

            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                구장 *
              </label>
              <select className={selectClass} value={gameForm.stadiumId}
                onChange={e => setGameForm({...gameForm, stadiumId: e.target.value})}
                required>
                <option value="">구장 선택</option>
                {stadiums.map((s: any) => (
                  <option key={s.stadiumId} value={s.stadiumId}>{s.name}</option>
                ))}
              </select>
            </div>

            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                경기 시간 *
              </label>
              <input type="datetime-local" className={inputClass}
                value={gameForm.startTime}
                onChange={e => setGameForm({...gameForm, startTime: e.target.value})}
                required />
            </div>

            <div className="mb-6">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                예매 오픈 시간
              </label>
              <input type="datetime-local" className={inputClass}
                value={gameForm.ticketOpenTime}
                onChange={e => setGameForm({...gameForm, ticketOpenTime: e.target.value})} />
            </div>

            <button type="submit"
              className="w-full bg-blue-900 text-white py-3 rounded-lg font-bold
                hover:bg-blue-800 transition">
              경기 등록
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}