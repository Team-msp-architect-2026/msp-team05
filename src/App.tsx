import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import MainPage from './pages/MainPage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import GameDetailPage from './pages/GameDetailPage';
import SeatSelectPage from './pages/SeatSelectPage';
import CheckoutPage from './pages/CheckoutPage';
import ReservationPage from './pages/ReservationPage';
import TicketPage from './pages/TicketPage';
import NotFoundPage from './pages/NotFoundPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route path="/games/:gameId" element={<GameDetailPage />} />
        <Route path="/games/:gameId/seats" element={<SeatSelectPage />} />
        <Route path="/checkout" element={<CheckoutPage />} />
        <Route path="/reservations" element={<ReservationPage />} />
        <Route path="/tickets/:ticketId" element={<TicketPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;