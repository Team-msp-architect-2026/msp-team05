import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Navbar from './components/Navbar';
import MainPage from './pages/MainPage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import GameDetailPage from './pages/GameDetailPage';
import QueuePage from './pages/QueuePage';
import SeatSelectPage from './pages/SeatSelectPage';
import CheckoutPage from './pages/CheckoutPage';
import ReservationPage from './pages/ReservationPage';
import TicketPage from './pages/TicketPage';
import NotFoundPage from './pages/NotFoundPage';
import AdminPage from './pages/AdminPage';

function AdminRoute({ children }: { children: React.ReactNode }) {
  const token = localStorage.getItem('accessToken');
  if (!token) return <Navigate to="/login" />;
  try {
    const payload = JSON.parse(atob(token.split('.')[1]));
    if (payload.role !== 'ADMIN') return <Navigate to="/" />;
  } catch {
    return <Navigate to="/login" />;
  }
  return <>{children}</>;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/games/:gameId" element={<GameDetailPage />} />
        <Route path="/games/:gameId/queue" element={<QueuePage />} />
        <Route path="/games/:gameId/seats" element={<SeatSelectPage />} />
        <Route path="/checkout" element={<CheckoutPage />} />
        <Route path="/reservations" element={<ReservationPage />} />
        <Route path="/tickets/:ticketId" element={<TicketPage />} />
        <Route path="/admin" element={
          <AdminRoute>
            <AdminPage />
          </AdminRoute>
        } />
      </Routes>
    </BrowserRouter>
  );
}

export default App;