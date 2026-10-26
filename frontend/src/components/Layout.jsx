import { Outlet, Navigate } from 'react-router-dom';
import Navbar from './Navbar';

export default function Layout() {
  const token = localStorage.getItem('token');
  
  if (!token) {
    return <Navigate to="/login" />;
  }

  return (
    <div>
      <Navbar />
      <main className="container" style={{ marginTop: '32px' }}>
        <Outlet />
      </main>
    </div>
  );
}
