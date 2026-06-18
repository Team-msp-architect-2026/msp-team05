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
  zoneId: string;
}

interface ZoneInfo {
  zoneId: string;
  zoneName: string;
  price: number;
  availableCount: number;
  totalCount: number;
}

const ZONE_CONFIG: Record<string, { a1: number; a2: number; color: string }> = {
  '1루 지정석': { a1: 60,  a2: 140, color: '#16a34a' },
  '3루 지정석': { a1: 220, a2: 300, color: '#ca8a04' },
  '중앙석':     { a1: 140, a2: 220, color: '#1565c0' },
  '외야 응원석':{ a1: 300, a2: 60,  color: '#c1121f' },
};

const FALLBACK_COLORS = ['#7c3aed', '#0891b2', '#be185d', '#b45309'];

const CX = 210, CY = 210;

const WAF_CAPTCHA_API_KEY = "EmiTvaZ8WwCGxbGLHpwxfsYqBtONuYY4w/tBpnC14Ow2Ub8qcMI4KMoHz7zrD6imuLRruR2BgBzLRIJk0CVq746eUTVYYTdvzG/i2S4N6IQlmKg3ujhX+sub6DOOhp7w7iEcNvEOdlcyD1G3XmrJ0QFTqykMfwH/jeNpFog6hrW241HfC/vrWG2vu3qxUcv7FsbxdVFD7RCehif5He+IN5rx2MHuOsc0TFj4C/WNkwbxH9vcbTUfPW8MnlKRvbbo7b5tSwUOFxv3JVihlXQjecZOkq04mNUeR5+Vu46bgB6azS4TqZYQ0587zGHCHMXk/1d1B70ReuHM+w2F58MHsvJ5+7+pBVHPSP1oxFHpkRJ8C9S4bYwuGhwjsm/x9BCIqBz7PcbaT3qNizs9sQ/t+czf4u08nhbNc+0OqzFVxA3zeeTvgoUPGX6IEfECTIzzUYjLWv/OsLZ7wtNlVVZXZJ/8AEqeokXWgfxL/8Ts+0bN9VmQKfyBvZrCMV1E+pz3+nZeYbkTbjPuB5e0UlPuVL0O7o7Yx0G0t7XqFygKlAjHMuRu+J7xRKo/fL6UYnDPJoKtj5Tceq2ggV73SvJXj0Z5fiVXwmnlCwSrH2v3aWi3Ba4xyiGcR1CuhssjwjP+wCrARJFdACBj2uLKiL5esn541FeDwmYyHHjrKwTNe7k=_1_1";

function toXY(deg: number, rx: number, ry: number): [number, number] {
  const r = (deg - 90) * Math.PI / 180;
  return [
    parseFloat((CX + rx * Math.cos(r)).toFixed(1)),
    parseFloat((CY + ry * Math.sin(r)).toFixed(1))
  ];
}

function donut(r1: number, ry1: number, r2: number, ry2: number, a1: number, a2: number): string {
  const norm = (a2 - a1 + 360) % 360;
  const la = norm > 180 ? 1 : 0;
  const [ax, ay] = toXY(a1, r1, ry1), [bx, by] = toXY(a2, r1, ry1);
  const [cx, cy] = toXY(a2, r2, ry2), [dx, dy] = toXY(a1, r2, ry2);
  return `M${ax},${ay} A${r1},${ry1},0,${la},1,${bx},${by} L${cx},${cy} A${r2},${ry2},0,${la},0,${dx},${dy} Z`;
}

function getTextPos(a1: number, a2: number): [number, number] {
  const norm = (a2 - a1 + 360) % 360;
  const mid = (a1 + norm / 2 + 360) % 360;
  return toXY(mid, 160, 155);
}

declare global {
  interface Window {
    AwsWafCaptcha?: {
      renderCaptcha: (container: HTMLElement, options: {
        apiKey: string;
        onSuccess?: (wafToken: string) => void;
        onError?: (error: unknown) => void;
      }) => void;
    };
  }
}

export default function SeatSelectPage() {
  const { gameId } = useParams();
  const navigate = useNavigate();
  const [seats, setSeats] = useState<Seat[]>([]);
  const [zones, setZones] = useState<ZoneInfo[]>([]);
  const [selectedSeats, setSelectedSeats] = useState<string[]>([]);
  const [selectedZoneId, setSelectedZoneId] = useState<string | null>(null);
  const [hoveredZoneId, setHoveredZoneId] = useState<string | null>(null);
  const [loadingZones, setLoadingZones] = useState(true);
  const [loadingSeats, setLoadingSeats] = useState(false);
  const stompClient = useRef<Client | null>(null);

  const [captchaPassed, setCaptchaPassed] = useState(false);
  const [captchaError, setCaptchaError] = useState('');
  const [sdkReady, setSdkReady] = useState(false);
  const [captchaContainer, setCaptchaContainer] = useState<HTMLDivElement | null>(null);

  useEffect(() => {
    if (window.AwsWafCaptcha) {
      setSdkReady(true);
      return;
    }
    const interval = setInterval(() => {
      if (window.AwsWafCaptcha) {
        setSdkReady(true);
        clearInterval(interval);
      }
    }, 100);
    const timeout = setTimeout(() => clearInterval(interval), 10000);
    return () => {
      clearInterval(interval);
      clearTimeout(timeout);
    };
  }, []);

  useEffect(() => {
    if (sdkReady && !captchaPassed && captchaContainer && window.AwsWafCaptcha) {
      window.AwsWafCaptcha.renderCaptcha(captchaContainer, {
        apiKey: WAF_CAPTCHA_API_KEY,
        onSuccess: () => {
          setCaptchaPassed(true);
          setCaptchaError('');
        },
        onError: (error) => {
          console.error('WAF CAPTCHA 에러', error);
          setCaptchaError('보안 인증 중 오류가 발생했습니다. 새로고침 해주세요.');
        },
      });
    }
  }, [sdkReady, captchaPassed, captchaContainer]);

  // 좌석 선택 페이지 진입 시 대기열 토큰 정리
  useEffect(() => {
    const queueToken = localStorage.getItem('queueToken');
    if (queueToken) {
      api.delete(`/api/queue/exit/${queueToken}`)
        .catch(() => {})
        .finally(() => localStorage.removeItem('queueToken'));
    }
  }, []);

  // 1단계: 구역 목록만 조회 (페이지 진입 시)
  useEffect(() => {
    api.get(`/api/games/${gameId}/seats`)
      .then(res => {
        const zoneList = res.data.data.zones;
        const zoneInfoList: ZoneInfo[] = zoneList.map((zone: any) => ({
          zoneId: zone.zoneId,
          zoneName: zone.zoneName,
          price: zone.price,
          availableCount: zone.seats
            ? zone.seats.filter((s: any) => s.status === 'AVAILABLE').length
            : 0,
          totalCount: zone.seats ? zone.seats.length : 0,
        }));
        setZones(zoneInfoList);
      })
      .catch(e => console.error('구역 조회 실패', e))
      .finally(() => setLoadingZones(false));
  }, [gameId]);

  // 2단계: 구역 선택 시 해당 구역 좌석만 조회
  useEffect(() => {
    if (!selectedZoneId) {
      setSeats([]);
      return;
    }

    setLoadingSeats(true);
    api.get(`/api/games/${gameId}/seats?zoneId=${selectedZoneId}`)
      .then(res => {
        const zoneList = res.data.data.zones;
        const seatList: Seat[] = [];

        zoneList.forEach((zone: any) => {
          zone.seats.forEach((seat: any) => {
            seatList.push({
              seatId: seat.seatId,
              rowNum: seat.rowNum,
              number: seat.number,
              status: seat.status,
              zoneId: zone.zoneId,
            });
          });
        });

        setSeats(seatList);

        setZones(prev => prev.map(z => {
          const found = zoneList.find((zone: any) => zone.zoneId === z.zoneId);
          if (!found) return z;
          const available = found.seats.filter((s: any) => s.status === 'AVAILABLE').length;
          return { ...z, availableCount: available, totalCount: found.seats.length };
        }));
      })
      .catch(e => console.error('좌석 조회 실패', e))
      .finally(() => setLoadingSeats(false));
  }, [selectedZoneId, gameId]);

  // WebSocket 연결
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
          setZones(prev => prev.map(zone => {
            const seatInZone = zone.zoneId === data.zoneId;
            if (!seatInZone) return zone;
            if (data.status === 'AVAILABLE') {
              return { ...zone, availableCount: zone.availableCount + 1 };
            } else {
              return { ...zone, availableCount: Math.max(0, zone.availableCount - 1) };
            }
          }));
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

  if (loadingZones) return <Spinner />;

  const activeZoneId = hoveredZoneId || selectedZoneId;

  const getZoneColor = (zoneName: string, fallbackIndex: number) =>
    ZONE_CONFIG[zoneName]?.color ?? FALLBACK_COLORS[fallbackIndex % FALLBACK_COLORS.length];

  const getZoneAngles = (zoneName: string, fallbackIndex: number) =>
    ZONE_CONFIG[zoneName] ?? { a1: fallbackIndex * 90, a2: (fallbackIndex + 1) * 90 };

  const currentZoneSeats = selectedZoneId
    ? seats.filter(s => s.zoneId === selectedZoneId)
    : [];
  const rows = [...new Set(currentZoneSeats.map(s => s.rowNum))].sort();

  const [frx, fry] = toXY(60, 195, 185);
  const [flx, fly] = toXY(300, 195, 185);

  return (
    <div className="min-h-screen bg-gray-100">
      <Navbar />

      {!captchaPassed && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-lg p-6 w-96">
            <h2 className="text-center font-bold text-lg mb-1">클린예매 서비스</h2>
            <p className="text-center text-sm text-gray-500 mb-4">
              부정예매를 방지하기 위해 <span className="text-blue-600 font-bold">보안 인증</span> 후 예매가 가능합니다.
            </p>

            {!sdkReady && (
              <p className="text-center text-gray-400 text-sm py-8">보안 모듈을 불러오는 중...</p>
            )}

            <div ref={setCaptchaContainer} />

            {captchaError && (
              <p className="text-red-500 text-xs mt-2 text-center">{captchaError}</p>
            )}

            <button
              onClick={() => navigate(-1)}
              className="w-full mt-4 py-2 border border-gray-300 rounded text-sm font-bold text-gray-600 hover:bg-gray-50"
            >
              날짜 다시 선택
            </button>
          </div>
        </div>
      )}

      <div className="max-w-6xl mx-auto px-4 py-6">
        <div className="flex gap-4">

          {/* 좌측: 야구장 SVG */}
          <div className="bg-white rounded-xl shadow flex-1 p-4">
            <h2 className="text-base font-bold text-gray-800 mb-1 text-center">구역 선택</h2>
            <p className="text-xs text-gray-400 text-center mb-2">
              오른쪽 목록에서 구역을 선택하세요
            </p>

            <svg viewBox="0 0 420 430" className="w-full" xmlns="http://www.w3.org/2000/svg">
              <ellipse cx={CX} cy={CY} rx="185" ry="175" fill="#1b5e35" />

              {zones.map((zone, i) => {
                const cfg = getZoneAngles(zone.zoneName, i);
                const color = getZoneColor(zone.zoneName, i);
                const isActive = !activeZoneId || activeZoneId === zone.zoneId;
                return (
                  <path
                    key={`outer-${zone.zoneId}`}
                    d={donut(148, 140, 185, 175, cfg.a1, cfg.a2)}
                    fill={isActive ? color : '#374151'}
                    opacity={isActive ? 1 : 0.5}
                    stroke="white"
                    strokeWidth="0.8"
                    style={{ cursor: zone.availableCount > 0 ? 'pointer' : 'default' }}
                    onClick={() => zone.availableCount > 0 &&
                      setSelectedZoneId(p => p === zone.zoneId ? null : zone.zoneId)}
                    onMouseEnter={() => setHoveredZoneId(zone.zoneId)}
                    onMouseLeave={() => setHoveredZoneId(null)}
                  />
                );
              })}

              {zones.map((zone, i) => {
                const cfg = getZoneAngles(zone.zoneName, i);
                const color = getZoneColor(zone.zoneName, i);
                const isActive = !activeZoneId || activeZoneId === zone.zoneId;
                return (
                  <path
                    key={`inner-${zone.zoneId}`}
                    d={donut(110, 104, 148, 140, cfg.a1, cfg.a2)}
                    fill={isActive ? color : '#374151'}
                    opacity={isActive ? 0.8 : 0.4}
                    stroke="white"
                    strokeWidth="0.5"
                    style={{ cursor: zone.availableCount > 0 ? 'pointer' : 'default' }}
                    onClick={() => zone.availableCount > 0 &&
                      setSelectedZoneId(p => p === zone.zoneId ? null : zone.zoneId)}
                    onMouseEnter={() => setHoveredZoneId(zone.zoneId)}
                    onMouseLeave={() => setHoveredZoneId(null)}
                  />
                );
              })}

              <line x1={CX} y1={CY + 40} x2={frx} y2={fry}
                stroke="rgba(255,255,255,0.2)" strokeWidth="1" strokeDasharray="5,4" />
              <line x1={CX} y1={CY + 40} x2={flx} y2={fly}
                stroke="rgba(255,255,255,0.2)" strokeWidth="1" strokeDasharray="5,4" />

              <ellipse cx={CX} cy={CY} rx="72" ry="68" fill="#1e7a40" />
              <path d={`M${CX},${CY-38} L${CX+37},${CY} L${CX},${CY+38} L${CX-37},${CY} Z`}
                fill="#c8a063" stroke="#b8905a" strokeWidth="0.5" />
              <ellipse cx={CX} cy={CY - 9} rx="7" ry="5.5" fill="#b8905a" />
              <rect x={CX-4} y={CY-42} width="8" height="8" fill="white" rx="1"
                transform={`rotate(45,${CX},${CY-38})`} />
              <rect x={CX+33} y={CY-4} width="7" height="7" fill="white" rx="1"
                transform={`rotate(45,${CX+36},${CY})`} />
              <rect x={CX-40} y={CY-4} width="7" height="7" fill="white" rx="1"
                transform={`rotate(45,${CX-36},${CY})`} />
              <path d={`M${CX},${CY+43} L${CX+5},${CY+39} L${CX+5},${CY+34}
                L${CX-5},${CY+34} L${CX-5},${CY+39} Z`} fill="white" />

              {zones.map((zone, i) => {
                const cfg = getZoneAngles(zone.zoneName, i);
                const [tx, ty] = getTextPos(cfg.a1, cfg.a2);
                return (
                  <g key={`text-${zone.zoneId}`} style={{ pointerEvents: 'none' }}>
                    <text x={tx} y={ty - 6} textAnchor="middle" dominantBaseline="middle"
                      fill="white" fontSize="12" fontWeight="bold">
                      {zone.zoneName}
                    </text>
                    <text x={tx} y={ty + 9} textAnchor="middle" dominantBaseline="middle"
                      fill="rgba(255,255,255,0.85)" fontSize="10">
                      {zone.price.toLocaleString()}원
                    </text>
                  </g>
                );
              })}
            </svg>
          </div>

          {/* 우측 사이드바 */}
          <div className="bg-white rounded-xl shadow flex flex-col" style={{ width: '240px' }}>
            <div className="flex-1 overflow-y-auto">
              {zones.length === 0 ? (
                <p className="text-center text-gray-400 text-xs py-8">구역 정보 없음</p>
              ) : (
                zones.map((zone, i) => {
                  const color = getZoneColor(zone.zoneName, i);
                  const isSelected = selectedZoneId === zone.zoneId;
                  const isHovered = hoveredZoneId === zone.zoneId;
                  const isAvailable = zone.availableCount > 0;
                  const filledCount = zone.totalCount - zone.availableCount;

                  return (
                    <div
                      key={zone.zoneId}
                      onClick={() => isAvailable &&
                        setSelectedZoneId(p => p === zone.zoneId ? null : zone.zoneId)}
                      onMouseEnter={() => setHoveredZoneId(zone.zoneId)}
                      onMouseLeave={() => setHoveredZoneId(null)}
                      className={`flex items-center gap-2 px-3 py-3 border-b border-gray-100
                        transition-all ${isAvailable ? 'cursor-pointer' : 'cursor-default'}
                        ${isSelected ? 'bg-green-50' : isHovered ? 'bg-gray-50' : ''}`}
                    >
                      <div className="w-3 h-3 rounded-full flex-shrink-0"
                        style={{ backgroundColor: color, opacity: isAvailable ? 1 : 0.3 }} />
                      <div className="flex-1 min-w-0">
                        <p className={`text-xs font-medium leading-tight ${
                          isAvailable ? 'text-gray-800' : 'text-gray-400'
                        }`}>{zone.zoneName}</p>
                        <p className="text-xs text-gray-400">{zone.price.toLocaleString()}원</p>
                      </div>
                      <div className="text-right flex-shrink-0">
                        <p className={`text-xs font-bold ${
                          isAvailable ? 'text-green-600' : 'text-gray-300'
                        }`}>{zone.availableCount}석</p>
                        <p className="text-xs text-gray-400">{filledCount}/{zone.totalCount}</p>
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
                  if (first) setSelectedZoneId(first.zoneId);
                  else alert('예매 가능한 구역이 없습니다.');
                }}
                className="flex-1 py-2.5 rounded text-white text-xs font-bold
                  bg-gray-500 hover:bg-gray-600 transition"
              >
                ✓ 자동배정
              </button>
              <button
                onClick={handleLock}
                className="flex-1 py-2.5 rounded text-white text-xs font-bold
                  bg-red-600 hover:bg-red-700 transition"
              >
                선택완료
              </button>
            </div>
          </div>
        </div>

        {/* 하단 좌석 선택 */}
        {selectedZoneId && (
          <div className="bg-white rounded-xl shadow p-6 mt-4 mb-24">
            <div className="flex justify-between items-center mb-4">
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 rounded-full" style={{
                  backgroundColor: getZoneColor(
                    zones.find(z => z.zoneId === selectedZoneId)?.zoneName ?? '', 0
                  )
                }} />
                <h2 className="font-bold text-gray-800 text-lg">
                  {zones.find(z => z.zoneId === selectedZoneId)?.zoneName}
                </h2>
                <span className="font-bold text-sm" style={{
                  color: getZoneColor(
                    zones.find(z => z.zoneId === selectedZoneId)?.zoneName ?? '', 0
                  )
                }}>
                  {zones.find(z => z.zoneId === selectedZoneId)?.price.toLocaleString()}원
                </span>
              </div>
              <span className="bg-blue-100 text-blue-800 font-bold px-3 py-1 rounded-full text-sm">
                {selectedSeats.length}석 선택
              </span>
            </div>

            <div className="flex gap-4 text-xs mb-4">
              <div className="flex items-center gap-1">
                <div className="w-3 h-3 bg-white border border-gray-300 rounded" /> 선택 가능
              </div>
              <div className="flex items-center gap-1">
                <div className="w-3 h-3 bg-green-500 rounded" /> 선택됨
              </div>
              <div className="flex items-center gap-1">
                <div className="w-3 h-3 bg-orange-400 rounded" /> 선점 중
              </div>
              <div className="flex items-center gap-1">
                <div className="w-3 h-3 bg-gray-400 rounded" /> 매진
              </div>
            </div>

            <div className="text-center text-xs text-gray-400 mb-4 bg-gray-100 py-1 rounded">
              ▲ 필드 방향
            </div>

            {loadingSeats ? (
              <div className="text-center py-8 text-gray-400 text-sm">
                좌석 정보를 불러오는 중...
              </div>
            ) : (
              <div className="overflow-x-auto">
                {rows.length === 0 ? (
                  <p className="text-center text-gray-400 py-8">
                    해당 구역의 좌석 정보가 없습니다.
                  </p>
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
                              onClick={() =>
                                seat.status === 'AVAILABLE' && toggleSeat(seat.seatId)}
                              className={`w-8 h-8 rounded text-xs font-medium transition ${
                                seat.status === 'RESERVED'
                                  ? 'bg-gray-400 text-white cursor-not-allowed' :
                                seat.status === 'LOCKED'
                                  ? 'bg-orange-400 text-white cursor-not-allowed' :
                                selectedSeats.includes(seat.seatId)
                                  ? 'bg-green-500 text-white' :
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
            )}
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
            className="bg-blue-900 text-white px-8 py-3 rounded-lg font-bold text-lg
              hover:bg-blue-800 transition"
          >
            선택 완료
          </button>
        </div>
      </div>
    </div>
  );
}