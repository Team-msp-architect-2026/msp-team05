import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/axios';

export default function LoginPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', passwd: '' });
  const [error, setError] = useState('');

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await api.post('/api/auth/login', form);
      localStorage.setItem('accessToken', res.data.data.accessToken);
      localStorage.setItem('refreshToken', res.data.data.refreshToken);

      // JWT 디코딩으로 username 추출
      const token = res.data.data.accessToken;
      const payload = JSON.parse(atob(token.split('.')[1]));
      const email = payload.sub;
      localStorage.setItem('username', email.split('@')[0]);

      const role = res.data.data.role;
      if (role === 'ADMIN') {
        navigate('/admin');
      } else {
        navigate('/');
      }
    } catch (err) {
      setError('이메일 또는 비밀번호가 올바르지 않습니다.');
    }
  };

  return (
    <div className="min-h-screen bg-white flex flex-col items-center justify-center px-4">
      {/* 왼쪽 위 로고 */}
      <div className="py-4">
        <div
          className="flex items-center gap-2 cursor-pointer w-fit"
          onClick={() => navigate('/')}
        >
          <span className="text-2xl">⚾</span>
          <span className="text-xl font-bold text-blue-900">KKY 티켓팅</span>
        </div>
      </div>

      {/* 나머지 중앙 콘텐츠 */}
      <div className="flex flex-col items-center justify-center flex-1">

      {/* 로고 */}
      <div className="mb-8 text-center">
        <h1 className="text-4xl font-bold text-blue-900">⚾ KKY</h1>
        <p className="text-gray-500 mt-2">야구 티켓팅</p>
      </div>

      <div className="w-full max-w-md">

        {/* 이메일 로그인 폼 */}
        <form onSubmit={handleSubmit} className="mb-6">
          <div className="mb-4">
            <input
              name="email"
              type="email"
              placeholder="이메일"
              value={form.email}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded-lg px-4 py-3 focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>
          <div className="mb-4">
            <input
              name="passwd"
              type="password"
              placeholder="비밀번호"
              value={form.passwd}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded-lg px-4 py-3 focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>
          {error && <p className="text-red-500 text-sm mb-4">{error}</p>}
          <button
            type="submit"
            className="w-full bg-blue-900 text-white py-3 rounded-lg font-bold hover:bg-blue-800 transition"
          >
            로그인
          </button>
        </form>

        {/* 구분선 */}
        <div className="flex items-center gap-4 mb-6">
          <div className="flex-1 h-px bg-gray-200"></div>
          <span className="text-sm text-gray-400">또는</span>
          <div className="flex-1 h-px bg-gray-200"></div>
        </div>

        {/* 소셜 로그인 버튼들 */}
        <div className="flex flex-col gap-3">
          <button
            onClick={() => alert('카카오 로그인 준비 중입니다.')}
            className="w-full flex items-center gap-4 px-6 py-3 rounded-full border border-gray-200 hover:bg-gray-50 transition"
          >
            <span className="text-xl">💬</span>
            <span className="font-medium text-gray-700">카카오로 시작하기</span>
          </button>

          <button
            onClick={() => alert('네이버 로그인 준비 중입니다.')}
            className="w-full flex items-center gap-4 px-6 py-3 rounded-full border border-gray-200 hover:bg-gray-50 transition"
          >
            <span className="text-xl font-bold text-green-500">N</span>
            <span className="font-medium text-gray-700">네이버로 시작하기</span>
          </button>

          <button
            onClick={() => alert('구글 로그인 준비 중입니다.')}
            className="w-full flex items-center gap-4 px-6 py-3 rounded-full border border-gray-200 hover:bg-gray-50 transition"
          >
            <span className="text-xl font-bold text-red-500">G</span>
            <span className="font-medium text-gray-700">구글로 시작하기</span>
          </button>
        </div>

        {/* 회원가입 링크 */}
        <p className="text-center text-sm text-gray-500 mt-6">
          계정이 없으신가요?{' '}
          <Link to="/signup" className="text-blue-600 font-medium hover:underline">
            회원가입
          </Link>
        </p>
      </div>

      {/* 하단 안내 */}
      <p className="mt-8 text-xs text-gray-400 text-center">
        개인정보 처리 사항은 개인정보처리방침에서 확인하세요.
      </p>
      </div>
    </div>
  );
}