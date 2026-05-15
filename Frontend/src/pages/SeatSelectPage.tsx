import { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import api from '../api/axios';
import Navbar from '../components/Navbar';
import Spinner from '../components/Spinner';

interface Seat {
  seatId: string;
  rowNum: string;
  number: string;
  status: string;
  zone: string;
}

interface ZoneInfo {
  zoneId: string;
  zoneName: string;
  price: number;
  availableCount: number;
  totalCount: number;
}

const ZONE_COLORS: Record<string, string> = {
  '1': '#16a34a',
  '2': '#ca8a04',
  '3': '#c1121f',
  '4': '#1565c0',
};

const ZONE_DISPLAY_NAMES: Record<string, string> = {
  '1': '1루 지정석',
  '2': '3루 지정석',
  '3': '2루 지정석',
  '4': '내야 지정석',
};

const ZONE_ORDER = ['4', '1', '3', '2'];

const CX = 210, CY = 210;

function toXY(deg: number, rx: number, ry: number): [number, number] {
  const r = (deg - 90) * Math.PI / 180;
  return [
    parseFloat((CX + rx * Math.cos(r)).toFixed(1)),
    parseFloat((CY + ry * Math.sin(r)).toFixed(1))
  ];
}

function donut(r1: number, ry1: number, r2: number, ry2: number, a1: number, a2: number): string {
  const la = (a2 - a1) > 180 ? 1 : 0;
  const [ax, ay] = toXY(a1, r1, ry1), [bx, by] = toXY(a2, r1, ry1);
  const [cx, cy] = toXY(a2, r2, ry2), [dx, dy] = toXY(a1, r2, ry2);
  return `M${ax},${ay} A${r1},${ry1},0,${la},1,${bx},${by} L${cx},${cy} A${r2},${ry2},0,${la},0,${dx},${dy} Z`;
}

export default function SeatSelectPage() {
  const { gameId } = useParams();
  const navigate = useNavigate();
  const [seats, setSeats] = useState<Seat[]>([]);
  const [zones, setZones] = useState<ZoneInfo[]>([]);
  const [selectedSeats, setSelectedSeats] = useState<string[]>([]);
  const [selectedZone, setSelectedZone] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const stompClient = useRef<Client | null>(null);

  useEffect(() => {
    const fetchSeats = async () => {
      try {
        const res = await api.get(`/api/games/${gameId}/seats`);
        const zoneList = res.data.data.zones;
        const seatList: Seat[] = [];
        const zoneInfoList: ZoneInfo[] = [];

        zoneList.forEach((zone: any) => {
          const available = zone.seats.filter((s: any) => s.status === 'AVAILABLE').length;
          const total = zone.seats.length;
          zoneInfoList.push({
            zoneId: zone.zoneId,
            zoneName: ZONE_DISPLAY_NAMES[zone.zoneId] || zone.zoneName,
            price: zone.price,
            availableCount: available,
            totalCount: total,
          });
          zone.seats.forEach((seat: any) => {
            seatList.push({
              seatId: seat.seatId,
              rowNum: seat.rowNum,
              number: seat.number,
              status: seat.status,
              zone: zone.zoneId,
            });
          });
        });

        setSeats(seatList);
        setZones(zoneInfoList);
      } catch (e) {
        console.error('좌석 조회 실패', e);
      } finally {
        setLoading(false);
      }
    };
    fetchSeats();
  }, [gameId]);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS(
        `${import.meta.env.VITE_API_URL || 'http://localhost:8080'}/ws/seats`
      ),
      onConnect: () => {
        client.subscribe(`/topic/seats/${gameId}`, (message) => {
          const data = JSON.parse(message.body);
          setSeats(prev => prev.map(seat =>
            seat.seatId === data.seatId ? { ...seat, status: data.status } : seat
          ));
        });
      },
    });
    client.activate();
    stompClient.current = client;
    return () => { client.deactivate(); };
  }, [gameId]);

  const toggleSeat = (seatId: string) => {
    if (selectedSeats.includes(seatId)) {
      setSelectedSeats(selectedSeats.filter(id => id !== seatId));
    } else {
      if (selectedSeats.length >= 4) {
        alert('최대 4석까지 선택 가능합니다.');
        return;
      }
      setSelectedSeats([...selectedSeats, seatId]);
    }
  };

  const handleLock = async () => {
    if (selectedSeats.length === 0) {
      alert('좌석을 선택해주세요.');
      return;
    }
    try {
      const res = await api.post(`/api/games/${gameId}/seats/lock`, {
        seatIds: selectedSeats,
      });
      localStorage.setItem('lockToken', res.data.data.lockToken);
      localStorage.setItem('gameId', gameId!);
      navigate('/checkout');
    } catch {
      alert('좌석 선점에 실패했습니다. 다시 시도해주세요.');
    }
  };

  if (loading) return <Spinner />;

  const currentZoneSeats = selectedZone ? seats.filter(s => s.zone === selectedZone) : [];
  const rows = [...new Set(currentZoneSeats.map(s => s.rowNum))];
  const sortedZones = [...zones].sort((a, b) =>
    ZONE_ORDER.indexOf(a.zoneId) - ZONE_ORDER.indexOf(b.zoneId)
  );

  // 경기장 색 - selectedZone만 반영, hover 영향 없음
  const getFill = (id: string) => {
    if (!selectedZone) return ZONE_COLORS[id];
    if (selectedZone === id) return ZONE_COLORS[id];
    return '#1f2937';
  };

  const getOpacity = (id: string) => {
    if (!selectedZone) return 1;
    if (selectedZone === id) return 1;
    return 0.6;
  };

  const [frx, fry] = toXY(60, 195, 185);
  const [flx, fly] = toXY(300, 195, 185);

  const textPos = (a1: number, a2: number, r: number): [number, number] => {
    const mid = (a1 + a2) / 2;
    return toXY(mid, r, r * 0.94);
  };

  const [t1x, t1y] = textPos(60, 140, 155);
  const [t4x, t4y] = textPos(140, 220, 155);
  const [t2x, t2y] = textPos(220, 300, 155);

  return (
    <div className="min-h-screen bg-gray-100">
      <Navbar />

      <div className="max-w-6xl mx-auto px-4 py-6">
        <div className="flex gap-4">

          {/* ── 좌측: 야구장 지도 (보기 전용) ── */}
          <div className="bg-white rounded-xl shadow flex-1 p-4">
            <h2 className="text-base font-bold text-gray-800 mb-1 text-center">구역 선택</h2>
            <p className="text-xs text-gray-400 text-center mb-2">오른쪽 목록에서 구역을 선택하세요</p>

            <svg viewBox="0 0 420 430" className="w-full" xmlns="http://www.w3.org/2000/svg">

              {/* 배경 잔디 */}
              <ellipse cx={CX} cy={CY} rx="185" ry="175" fill="#1b5e35" />

              {/* 외곽 링 */}
              <path d={donut(148, 140, 185, 175, 300, 360)} fill={getFill('3')} opacity={getOpacity('3')} stroke="white" strokeWidth="0.8" />
              <path d={donut(148, 140, 185, 175, 0, 60)}   fill={getFill('3')} opacity={getOpacity('3')} stroke="white" strokeWidth="0.8" />
              <path d={donut(148, 140, 185, 175, 60, 140)}  fill={getFill('1')} opacity={getOpacity('1')} stroke="white" strokeWidth="0.8" />
              <path d={donut(148, 140, 185, 175, 140, 220)} fill={getFill('4')} opacity={getOpacity('4')} stroke="white" strokeWidth="0.8" />
              <path d={donut(148, 140, 185, 175, 220, 300)} fill={getFill('2')} opacity={getOpacity('2')} stroke="white" strokeWidth="0.8" />

              {/* 내곽 링 */}
              <path d={donut(110, 104, 148, 140, 300, 360)} fill={getFill('3')} opacity={getOpacity('3') * 0.8} stroke="white" strokeWidth="0.5" />
              <path d={donut(110, 104, 148, 140, 0, 60)}   fill={getFill('3')} opacity={getOpacity('3') * 0.8} stroke="white" strokeWidth="0.5" />
              <path d={donut(110, 104, 148, 140, 60, 140)}  fill={getFill('1')} opacity={getOpacity('1') * 0.8} stroke="white" strokeWidth="0.5" />
              <path d={donut(110, 104, 148, 140, 140, 220)} fill={getFill('4')} opacity={getOpacity('4') * 0.8} stroke="white" strokeWidth="0.5" />
              <path d={donut(110, 104, 148, 140, 220, 300)} fill={getFill('2')} opacity={getOpacity('2') * 0.8} stroke="white" strokeWidth="0.5" />

              {/* 파울라인 */}
              <line x1={CX} y1={CY + 40} x2={frx} y2={fry} stroke="rgba(255,255,255,0.2)" strokeWidth="1" strokeDasharray="5,4" />
              <line x1={CX} y1={CY + 40} x2={flx} y2={fly} stroke="rgba(255,255,255,0.2)" strokeWidth="1" strokeDasharray="5,4" />

              {/* 내야 잔디 */}
              <ellipse cx={CX} cy={CY} rx="72" ry="68" fill="#1e7a40" />

              {/* 다이아몬드 */}
              <path d={`M${CX},${CY-38} L${CX+37},${CY} L${CX},${CY+38} L${CX-37},${CY} Z`} fill="#c8a063" stroke="#b8905a" strokeWidth="0.5" />

              {/* 마운드 */}
              <ellipse cx={CX} cy={CY - 9} rx="7" ry="5.5" fill="#b8905a" />

              {/* 베이스 */}
              <rect x={CX-4} y={CY-42} width="8" height="8" fill="white" rx="1" transform={`rotate(45,${CX},${CY-38})`} />
              <rect x={CX+33} y={CY-4} width="7" height="7" fill="white" rx="1" transform={`rotate(45,${CX+36},${CY})`} />
              <rect x={CX-40} y={CY-4} width="7" height="7" fill="white" rx="1" transform={`rotate(45,${CX-36},${CY})`} />
              <path d={`M${CX},${CY+43} L${CX+5},${CY+39} L${CX+5},${CY+34} L${CX-5},${CY+34} L${CX-5},${CY+39} Z`} fill="white" />

              {/* 텍스트 */}
              <text x={CX} y={CY - 162} textAnchor="middle" dominantBaseline="middle" fill="white" fontSize="13" fontWeight="bold">2루 지정석</text>
              <text x={CX} y={CY - 147} textAnchor="middle" dominantBaseline="middle" fill="rgba(255,255,255,0.85)" fontSize="10">10,000원</text>

              <text x={t1x} y={t1y - 6} textAnchor="middle" dominantBaseline="middle" fill="white" fontSize="12" fontWeight="bold">1루 지정석</text>
              <text x={t1x} y={t1y + 9} textAnchor="middle" dominantBaseline="middle" fill="rgba(255,255,255,0.85)" fontSize="10">15,000원</text>

              <text x={t4x} y={t4y - 6} textAnchor="middle" dominantBaseline="middle" fill="white" fontSize="12" fontWeight="bold">내야 지정석</text>
              <text x={t4x} y={t4y + 9} textAnchor="middle" dominantBaseline="middle" fill="rgba(255,255,255,0.85)" fontSize="10">20,000원</text>

              <text x={t2x} y={t2y - 6} textAnchor="middle" dominantBaseline="middle" fill="white" fontSize="12" fontWeight="bold">3루 지정석</text>
              <text x={t2x} y={t2y + 9} textAnchor="middle" dominantBaseline="middle" fill="rgba(255,255,255,0.85)" fontSize="10">15,000원</text>

            </svg>
          </div>

          {/* ── 우측: 사이드바 ── */}
          <div className="bg-white rounded-xl shadow flex flex-col" style={{ width: '240px' }}>
            <div className="flex-1 overflow-y-auto">
              {sortedZones.length === 0 ? (
                <p className="text-center text-gray-400 text-xs py-8">구역 정보 없음</p>
              ) : (
                sortedZones.map(zone => {
                  const color = ZONE_COLORS[zone.zoneId] || '#888';
                  const isSelected = selectedZone === zone.zoneId;
                  const isAvailable = zone.availableCount > 0;
                  const filledCount = zone.totalCount - zone.availableCount;
                  return (
                    <div
                      key={zone.zoneId}
                      onClick={() => isAvailable && setSelectedZone(p => p === zone.zoneId ? null : zone.zoneId)}
                      className={`flex items-center gap-2 px-3 py-3 border-b border-gray-100 transition-all ${
                        isAvailable ? 'cursor-pointer hover:bg-gray-50' : 'cursor-default'
                      } ${isSelected ? 'bg-green-50' : ''}`}
                    >
                      <div className="w-3 h-3 rounded-full flex-shrink-0" style={{ backgroundColor: color, opacity: isAvailable ? 1 : 0.3 }}></div>
                      <div className="flex-1 min-w-0">
                        <p className={`text-xs font-medium leading-tight ${isAvailable ? 'text-gray-800' : 'text-gray-400'}`}>{zone.zoneName}</p>
                        <p className="text-xs text-gray-400">{zone.price.toLocaleString()}원</p>
                      </div>
                      <div className="text-right flex-shrink-0">
                        <p className={`text-xs font-bold ${isAvailable ? 'text-green-600' : 'text-gray-300'}`}>
                          {zone.availableCount}석
                        </p>
                        <p className="text-xs text-gray-400">
                          {filledCount}/{zone.totalCount}
                        </p>
                      </div>
                    </div>
                  );
                })
              )}
            </div>

            <div className="p-3 border-t border-gray-100 flex gap-2">
              <button
                onClick={() => {
                  const first = zones.find(z => z.availableCount > 0);
                  if (first) setSelectedZone(first.zoneId);
                  else alert('예매 가능한 구역이 없습니다.');
                }}
                className="flex-1 py-2.5 rounded text-white text-xs font-bold bg-gray-500 hover:bg-gray-600 transition"
              >
                ✓ 자동배정
              </button>
              <button
                onClick={handleLock}
                className="flex-1 py-2.5 rounded text-white text-xs font-bold bg-red-600 hover:bg-red-700 transition"
              >
                선택완료
              </button>
            </div>
          </div>
        </div>

        {/* ── 하단: 좌석 선택 ── */}
        {selectedZone && (
          <div className="bg-white rounded-xl shadow p-6 mt-4 mb-24">
            <div className="flex justify-between items-center mb-4">
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 rounded-full" style={{ backgroundColor: ZONE_COLORS[selectedZone] }}></div>
                <h2 className="font-bold text-gray-800 text-lg">
                  {zones.find(z => z.zoneId === selectedZone)?.zoneName}
                </h2>
                <span className="font-bold text-sm" style={{ color: ZONE_COLORS[selectedZone] }}>
                  {zones.find(z => z.zoneId === selectedZone)?.price.toLocaleString()}원
                </span>
              </div>
              <span className="bg-blue-100 text-blue-800 font-bold px-3 py-1 rounded-full text-sm">
                {selectedSeats.length}석 선택
              </span>
            </div>

            <div className="flex gap-4 text-xs mb-4">
              <div className="flex items-center gap-1"><div className="w-3 h-3 bg-white border border-gray-300 rounded"></div> 선택 가능</div>
              <div className="flex items-center gap-1"><div className="w-3 h-3 bg-green-500 rounded"></div> 선택됨</div>
              <div className="flex items-center gap-1"><div className="w-3 h-3 bg-orange-400 rounded"></div> 선점 중</div>
              <div className="flex items-center gap-1"><div className="w-3 h-3 bg-gray-400 rounded"></div> 매진</div>
            </div>

            <div className="text-center text-xs text-gray-400 mb-4 bg-gray-100 py-1 rounded">
              ▲ 필드 방향
            </div>

            <div className="overflow-x-auto">
              {rows.length === 0 ? (
                <p className="text-center text-gray-400 py-8">해당 구역의 좌석 정보가 없습니다.</p>
              ) : (
                rows.map(row => (
                  <div key={row} className="flex items-center gap-1 mb-1">
                    <span className="text-xs text-gray-400 w-4 font-bold">{row}</span>
                    <div className="flex gap-1 flex-wrap">
                      {currentZoneSeats
                        .filter(s => s.rowNum === row)
                        .sort((a, b) => parseInt(a.number) - parseInt(b.number))
                        .map(seat => (
                          <button
                            key={seat.seatId}
                            onClick={() => seat.status === 'AVAILABLE' && toggleSeat(seat.seatId)}
                            className={`w-8 h-8 rounded text-xs font-medium transition ${
                              seat.status === 'RESERVED' ? 'bg-gray-400 text-white cursor-not-allowed' :
                              seat.status === 'LOCKED' ? 'bg-orange-400 text-white cursor-not-allowed' :
                              selectedSeats.includes(seat.seatId) ? 'bg-green-500 text-white' :
                              'bg-white border border-gray-300 hover:border-blue-500'
                            }`}
                            title={`${seat.rowNum}열 ${seat.number}번`}
                          >
                            {seat.number}
                          </button>
                        ))}
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        )}
      </div>

      <div className="fixed bottom-0 left-0 right-0 bg-white border-t shadow-lg p-4">
        <div className="max-w-6xl mx-auto flex justify-between items-center">
          <div>
            <p className="text-sm text-gray-500">선택한 좌석</p>
            <p className="font-bold text-blue-900">{selectedSeats.length}석 / 최대 4석</p>
          </div>
          <button
            onClick={handleLock}
            className="bg-blue-900 text-white px-8 py-3 rounded-lg font-bold text-lg hover:bg-blue-800 transition"
          >
            선택 완료
          </button>
        </div>
      </div>
    </div>
  );
}