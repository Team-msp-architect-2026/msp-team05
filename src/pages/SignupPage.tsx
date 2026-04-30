import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api/axios';

export default function SignupPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [form, setForm] = useState({
    email: '',
    username: '',
    passwd: '',
    phonenum: '',
    address: '',
  });
  const [smsCode, setSmsCode] = useState('');
  const [smsSent, setSmsSent] = useState(false);
  const [smsVerified, setSmsVerified] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSmsSend = async () => {
  if (!form.phonenum) {
    setError('휴대폰 번호를 입력해주세요.');
    return;
  }
  // SMS 실제 발송 없이 바로 인증 완료 처리
  setSmsSent(true);
  setSmsVerified(true);
  setError('');
  alert('인증이 완료되었습니다.');
};

  const handleSmsVerify = async () => {
    if (!smsCode) {
      setError('인증번호를 입력해주세요.');
      return;
    }
    setLoading(true);
    try {
      await api.post('/api/auth/sms/verify', {
        phone: form.phonenum,
        code: smsCode,
      });
      setSmsVerified(true);
      setError('');
      alert('인증이 완료되었습니다.');
    } catch {
      setError('인증번호가 올바르지 않습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!smsVerified) {
      setError('휴대폰 인증을 완료해주세요.');
      return;
    }
    setLoading(true);
    try {
      await api.post('/api/auth/signup', form);
      navigate('/login');
    } catch {
      setError('회원가입에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="bg-white p-8 rounded-xl shadow-md w-full max-w-md">

        {/* 로고 - 클릭하면 홈으로 */}
        <div
          className="text-center mb-8 cursor-pointer"
          onClick={() => navigate('/')}
        >
          <h1 className="text-3xl font-bold text-blue-900">⚾ KKY</h1>
          <p className="text-gray-500 mt-1">회원가입</p>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">이메일</label>
            <input
              name="email"
              type="email"
              placeholder="이메일을 입력하세요"
              value={form.email}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">이름</label>
            <input
              name="username"
              type="text"
              placeholder="이름을 입력하세요"
              value={form.username}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">비밀번호</label>
            <input
              name="passwd"
              type="password"
              placeholder="비밀번호를 입력하세요 (8자 이상)"
              value={form.passwd}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">휴대폰 번호</label>
            <div className="flex gap-2">
              <input
                name="phonenum"
                type="tel"
                placeholder="휴대폰 번호를 입력하세요"
                value={form.phonenum}
                onChange={handleChange}
                className="flex-1 border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                required
              />
              <button
                type="button"
                onClick={handleSmsSend}
                disabled={loading || smsVerified}
                className={`px-4 py-2 rounded-lg text-sm font-bold transition whitespace-nowrap ${
                  smsVerified
                    ? 'bg-green-500 text-white cursor-not-allowed'
                    : 'bg-blue-900 text-white hover:bg-blue-800'
                }`}
              >
                {smsVerified ? '인증완료' : smsSent ? '재발송' : '인증번호 발송'}
              </button>
            </div>
          </div>

          {smsSent && !smsVerified && (
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-1">인증번호</label>
              <div className="flex gap-2">
                <input
                  type="text"
                  placeholder="인증번호 6자리를 입력하세요"
                  value={smsCode}
                  onChange={(e) => setSmsCode(e.target.value)}
                  className="flex-1 border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  maxLength={6}
                />
                <button
                  type="button"
                  onClick={handleSmsVerify}
                  disabled={loading}
                  className="px-4 py-2 rounded-lg text-sm font-bold bg-blue-900 text-white hover:bg-blue-800 transition"
                >
                  확인
                </button>
              </div>
            </div>
          )}

          <div className="mb-6">
            <label className="block text-sm font-medium text-gray-700 mb-1">주소</label>
            <input
              name="address"
              type="text"
              placeholder="주소를 입력하세요"
              value={form.address}
              onChange={handleChange}
              className="w-full border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          {error && <p className="text-red-500 text-sm mb-4">{error}</p>}

          <button
            type="submit"
            disabled={loading}
            className={`w-full py-2 rounded-lg font-bold transition ${
              loading
                ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                : 'bg-blue-900 text-white hover:bg-blue-800'
            }`}
          >
            {loading ? '처리 중...' : '회원가입'}
          </button>
        </form>

        <p className="text-center text-sm text-gray-500 mt-4">
          이미 계정이 있으신가요?{' '}
          <Link to="/login" className="text-blue-600 font-medium hover:underline">
            로그인
          </Link>
        </p>
      </div>
    </div>
  );
}