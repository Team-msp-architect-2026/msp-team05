import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import Navbar from '../components/Navbar';

export default function AdminPage() {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<'team' | 'stadium' | 'game' | 'zone' | 'seat'>('team');
  const [teams, setTeams] = useState<any[]>([]);
  const [stadiums, setStadiums] = useState<any[]>([]);
  const [zones, setZones] = useState<any[]>([]);
  const [message, setMessage] = useState('');

  // 팀 목록
  const [teamForm, setTeamForm] = useState({ name: '', logoUrl: '' });
  // 구장 목록
  const [stadiumForm, setStadiumForm] = useState({ name: '', address: '' });
  // 경기 등록
  const [gameForm, setGameForm] = useState({
    homeTeamId: '', awayTeamId: '', stadiumId: '',
    startTime: '', ticketOpenTime: ''
  });
  // 구역 등록
  const [zoneForm, setZoneForm] = useState({
    stadiumId: '', zoneName: '', price: '', totalSeats: ''
  });
  // 좌석 등록
  const [seatForm, setSeatForm] = useState({
    zoneId: '', row: '', number: ''
  });

  useEffect(() => {
    api.get('/api/admin/teams').then(res => setTeams(res.data.data || [])).catch(() => {});
    api.get('/api/admin/stadiums').then(res => setStadiums(res.data.data || [])).catch(() => {});
  }, []);

  useEffect(() => {
    if (zoneForm.stadiumId) {
      api.get(`/api/admin/zones?stadiumId=${zoneForm.stadiumId}`)
        .then(res => setZones(res.data.data || [])).catch(() => {});
    }
  }, [zoneForm.stadiumId]);

  const showMessage = (msg: string) => {
    setMessage(msg);
    setTimeout(() => setMessage(''), 3000);
  };

  const handleTeam = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/api/admin/teams', teamForm);
      showMessage('✅ 팀 등록 완료');
      setTeamForm({ name: '', logoUrl: '' });
      const res = await api.get('/api/admin/teams');
      setTeams(res.data.data || []);
    } catch { showMessage('❌ 팀 등록 실패'); }
  };

  const handleStadium = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/api/admin/stadiums', stadiumForm);
      showMessage('✅ 구장 등록 완료');
      setStadiumForm({ name: '', address: '' });
      const res = await api.get('/api/admin/stadiums');
      setStadiums(res.data.data || []);
    } catch { showMessage('❌ 구장 등록 실패'); }
  };

  const handleGame = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/api/admin/games', {
        ...gameForm,
        startTime: new Date(gameForm.startTime).toISOString(),
        ticketOpenTime: new Date(gameForm.ticketOpenTime).toISOString(),
      });
      showMessage('✅ 경기 등록 완료');
      setGameForm({ homeTeamId: '', awayTeamId: '', stadiumId: '', startTime: '', ticketOpenTime: '' });
    } catch { showMessage('❌ 경기 등록 실패'); }
  };

  const handleZone = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/api/admin/zones', {
        ...zoneForm,
        price: Number(zoneForm.price),
        totalSeats: Number(zoneForm.totalSeats),
      });
      showMessage('✅ 구역 등록 완료');
      setZoneForm({ stadiumId: '', zoneName: '', price: '', totalSeats: '' });
    } catch { showMessage('❌ 구역 등록 실패'); }
  };

  const handleSeat = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.post('/api/admin/seats', seatForm);
      showMessage('✅ 좌석 등록 완료');
      setSeatForm({ zoneId: '', row: '', number: '' });
    } catch { showMessage('❌ 좌석 등록 실패'); }
  };

  const inputClass = "w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500";
  const selectClass = "w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white";

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-3xl mx-auto px-4 py-8">
        <h1 className="text-2xl font-bold text-blue-900 mb-6">⚙️ 관리자 페이지</h1>

        {message && (
          <div className="mb-4 p-3 bg-blue-50 border border-blue-200 rounded-lg text-center font-medium">
            {message}
          </div>
        )}

        {/* 탭 */}
        <div className="flex gap-2 mb-6 flex-wrap">
          {(['team', 'stadium', 'game', 'zone', 'seat'] as const).map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`px-4 py-2 rounded-lg font-bold transition ${
                activeTab === tab ? 'bg-blue-900 text-white' : 'bg-white text-gray-600 border'
              }`}
            >
              {tab === 'team' ? '팀 등록' :
               tab === 'stadium' ? '구장 등록' :
               tab === 'game' ? '경기 등록' :
               tab === 'zone' ? '구역 등록' : '좌석 등록'}
            </button>
          ))}
        </div>

        <div className="bg-white rounded-xl shadow p-6">

          {/* 팀 등록 */}
          {activeTab === 'team' && (
            <form onSubmit={handleTeam}>
              <h2 className="text-lg font-bold mb-4">팀 등록</h2>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">팀 이름 *</label>
                <input className={inputClass} value={teamForm.name}
                  onChange={e => setTeamForm({...teamForm, name: e.target.value})} required />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">로고 URL</label>
                <input className={inputClass} value={teamForm.logoUrl}
                  onChange={e => setTeamForm({...teamForm, logoUrl: e.target.value})} />
              </div>
              <button type="submit" className="w-full bg-blue-900 text-white py-2 rounded-lg font-bold hover:bg-blue-800">
                팀 등록
              </button>
            </form>
          )}

          {/* 구장 등록 */}
          {activeTab === 'stadium' && (
            <form onSubmit={handleStadium}>
              <h2 className="text-lg font-bold mb-4">구장 등록</h2>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">구장 이름 *</label>
                <input className={inputClass} value={stadiumForm.name}
                  onChange={e => setStadiumForm({...stadiumForm, name: e.target.value})} required />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">주소</label>
                <input className={inputClass} value={stadiumForm.address}
                  onChange={e => setStadiumForm({...stadiumForm, address: e.target.value})} />
              </div>
              <button type="submit" className="w-full bg-blue-900 text-white py-2 rounded-lg font-bold hover:bg-blue-800">
                구장 등록
              </button>
            </form>
          )}

          {/* 경기 등록 */}
          {activeTab === 'game' && (
            <form onSubmit={handleGame}>
              <h2 className="text-lg font-bold mb-4">경기 등록</h2>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">홈팀 *</label>
                <select className={selectClass} value={gameForm.homeTeamId}
                  onChange={e => setGameForm({...gameForm, homeTeamId: e.target.value})} required>
                  <option value="">홈팀 선택</option>
                  {teams.map((t: any) => <option key={t.teamId} value={t.teamId}>{t.name}</option>)}
                </select>
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">원정팀 *</label>
                <select className={selectClass} value={gameForm.awayTeamId}
                  onChange={e => setGameForm({...gameForm, awayTeamId: e.target.value})} required>
                  <option value="">원정팀 선택</option>
                  {teams.map((t: any) => <option key={t.teamId} value={t.teamId}>{t.name}</option>)}
                </select>
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">구장 *</label>
                <select className={selectClass} value={gameForm.stadiumId}
                  onChange={e => setGameForm({...gameForm, stadiumId: e.target.value})} required>
                  <option value="">구장 선택</option>
                  {stadiums.map((s: any) => <option key={s.stadiumId} value={s.stadiumId}>{s.name}</option>)}
                </select>
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">경기 시간 *</label>
                <input type="datetime-local" className={inputClass} value={gameForm.startTime}
                  onChange={e => setGameForm({...gameForm, startTime: e.target.value})} required />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">예매 오픈 시간</label>
                <input type="datetime-local" className={inputClass} value={gameForm.ticketOpenTime}
                  onChange={e => setGameForm({...gameForm, ticketOpenTime: e.target.value})} />
              </div>
              <button type="submit" className="w-full bg-blue-900 text-white py-2 rounded-lg font-bold hover:bg-blue-800">
                경기 등록
              </button>
            </form>
          )}

          {/* 구역 등록 */}
          {activeTab === 'zone' && (
            <form onSubmit={handleZone}>
              <h2 className="text-lg font-bold mb-4">구역 등록</h2>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">구장 *</label>
                <select className={selectClass} value={zoneForm.stadiumId}
                  onChange={e => setZoneForm({...zoneForm, stadiumId: e.target.value})} required>
                  <option value="">구장 선택</option>
                  {stadiums.map((s: any) => <option key={s.stadiumId} value={s.stadiumId}>{s.name}</option>)}
                </select>
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">구역 이름 *</label>
                <input className={inputClass} value={zoneForm.zoneName}
                  onChange={e => setZoneForm({...zoneForm, zoneName: e.target.value})} required />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">가격 *</label>
                <input type="number" className={inputClass} value={zoneForm.price}
                  onChange={e => setZoneForm({...zoneForm, price: e.target.value})} required />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">총 좌석 수 *</label>
                <input type="number" className={inputClass} value={zoneForm.totalSeats}
                  onChange={e => setZoneForm({...zoneForm, totalSeats: e.target.value})} required />
              </div>
              <button type="submit" className="w-full bg-blue-900 text-white py-2 rounded-lg font-bold hover:bg-blue-800">
                구역 등록
              </button>
            </form>
          )}

          {/* 좌석 등록 */}
          {activeTab === 'seat' && (
            <form onSubmit={handleSeat}>
              <h2 className="text-lg font-bold mb-4">좌석 등록</h2>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">구역 ID *</label>
                <input className={inputClass} value={seatForm.zoneId} placeholder="구역 ID 입력"
                  onChange={e => setSeatForm({...seatForm, zoneId: e.target.value})} required />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">열 (예: A) *</label>
                <input className={inputClass} value={seatForm.row}
                  onChange={e => setSeatForm({...seatForm, row: e.target.value})} required />
              </div>
              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">번호 (예: 1) *</label>
                <input className={inputClass} value={seatForm.number}
                  onChange={e => setSeatForm({...seatForm, number: e.target.value})} required />
              </div>
              <button type="submit" className="w-full bg-blue-900 text-white py-2 rounded-lg font-bold hover:bg-blue-800">
                좌석 등록
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}