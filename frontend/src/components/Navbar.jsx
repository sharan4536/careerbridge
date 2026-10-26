import { Link, useNavigate, useLocation } from 'react-router-dom';
import { Briefcase, LogOut } from 'lucide-react';

export default function Navbar() {
  const navigate = useNavigate();
  const location = useLocation();
  
  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('email');
    navigate('/login');
  };

  const role = localStorage.getItem('role') || '';
  const token = localStorage.getItem('token');
  const isAdmin = role === 'ROLE_ADMIN' || role === 'ADMIN';
  const isRecruiter = role === 'ROLE_RECRUITER' || role === 'RECRUITER';
  const isStudent = !isAdmin && !isRecruiter;

  const isActive = (path) => location.pathname === path ? 'active' : '';

  const getRoleLabel = () => {
    if (isAdmin) return { text: 'Admin', badgeClass: 'badge-danger' };
    if (isRecruiter) return { text: 'Recruiter', badgeClass: 'badge-success' };
    return { text: 'Student', badgeClass: 'badge-info' };
  };

  const roleInfo = getRoleLabel();

  return (
    <nav className="navbar">
      <div className="flex items-center gap-3">
        <Briefcase color="#ff6b9e" size={32} className="floating-element" />
        <Link to="/" className="navbar-brand">CareerBridge</Link>
        {token && (
          <span className={`badge ${roleInfo.badgeClass}`} style={{ fontSize: '0.8rem', padding: '2px 8px' }}>
            {roleInfo.text}
          </span>
        )}
      </div>

      <div className="nav-links flex items-center gap-2">
        {token ? (
          <>
            {isStudent && (
              <>
                <Link to="/dashboard" className={`nav-link ${isActive('/dashboard')}`}>Dashboard</Link>
                <Link to="/jobs" className={`nav-link ${isActive('/jobs')}`}>Jobs Feed</Link>
                <Link to="/applications" className={`nav-link ${isActive('/applications')}`}>Applications</Link>
                <Link to="/resumes" className={`nav-link ${isActive('/resumes')}`}>Resume Vault</Link>
              </>
            )}

            {isRecruiter && (
              <>
                <Link to="/recruiter" className={`nav-link ${isActive('/recruiter')}`}>Recruiter Portal</Link>
                <Link to="/jobs" className={`nav-link ${isActive('/jobs')}`}>Job Listings</Link>
              </>
            )}

            {isAdmin && (
              <>
                <Link to="/admin" className={`nav-link ${isActive('/admin')}`}>Admin Panel</Link>
                <Link to="/recruiter" className={`nav-link ${isActive('/recruiter')}`}>Recruiter Portal</Link>
                <Link to="/jobs" className={`nav-link ${isActive('/jobs')}`}>Jobs Feed</Link>
              </>
            )}

            <button onClick={handleLogout} className="btn btn-outline flex items-center gap-1" style={{ padding: '6px 12px', fontSize: '0.9rem', marginLeft: '8px' }}>
              <LogOut size={16} /> Logout
            </button>
          </>
        ) : (
          <>
            <Link to="/login" className="btn btn-outline" style={{ padding: '8px 16px', fontSize: '1.1rem' }}>Login</Link>
            <Link to="/register" className="btn btn-primary" style={{ padding: '8px 16px', fontSize: '1.1rem' }}>Sign Up</Link>
          </>
        )}
      </div>
    </nav>
  );
}
