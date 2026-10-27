import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api';
import { Briefcase, UserCheck, Shield, Award } from 'lucide-react';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const performLogin = async (loginEmail, loginPassword) => {
    setError('');
    setLoading(true);
    try {
      const res = await api.post('/auth/login', {
        email: loginEmail,
        password: loginPassword,
      });
      const token = res.data.accessToken;
      localStorage.setItem('token', token);
      
      let userRole = 'STUDENT';
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        userRole = payload.role || 'STUDENT';
        localStorage.setItem('role', userRole);
        localStorage.setItem('email', payload.sub || loginEmail);
      } catch (err) {
        localStorage.setItem('role', 'STUDENT');
      }

      if (userRole === 'ROLE_ADMIN' || userRole === 'ADMIN') {
        navigate('/admin');
      } else if (userRole === 'ROLE_RECRUITER' || userRole === 'RECRUITER') {
        navigate('/recruiter');
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid credentials or server unavailable.');
    } finally {
      setLoading(false);
    }
  };

  const handleLogin = (e) => {
    e.preventDefault();
    performLogin(email, password);
  };

  const handleQuickLogin = (quickEmail, quickPassword) => {
    setEmail(quickEmail);
    setPassword(quickPassword);
    performLogin(quickEmail, quickPassword);
  };

  return (
    <div className="flex justify-center items-center" style={{ minHeight: '100vh', padding: '30px 15px' }}>
      <div className="glass-panel" style={{ width: '100%', maxWidth: '440px' }}>
        <div className="flex flex-col items-center" style={{ marginBottom: '20px' }}>
          <Briefcase size={54} color="#ff6b9e" className="floating-element" style={{ marginBottom: '12px' }}/>
          <h2>Welcome Back</h2>
          <p style={{ color: 'var(--text-secondary)', marginTop: '2px', fontSize: '1rem' }}>Sign in to CareerBridge</p>
        </div>
        
        {error && <div className="badge badge-danger" style={{ marginBottom: '16px', display: 'block', textAlign: 'center' }}>{error}</div>}
        
        <form onSubmit={handleLogin}>
          <div className="form-group">
            <label>Email Address</label>
            <input type="email" value={email} onChange={e => setEmail(e.target.value)} className="form-input" required placeholder="you@example.com" />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input type="password" value={password} onChange={e => setPassword(e.target.value)} className="form-input" required placeholder="••••••••" />
          </div>
          <button type="submit" className="btn btn-primary w-full" style={{ marginTop: '12px', fontSize: '1.2rem' }} disabled={loading}>
            {loading ? 'Authenticating...' : 'Sign In'}
          </button>
        </form>

        <div style={{ margin: '24px 0 16px 0', borderTop: '2px dashed var(--ink-black)', paddingTop: '16px' }}>
          <div style={{ textAlign: 'center', fontSize: '0.9rem', fontWeight: 'bold', marginBottom: '12px', color: 'var(--text-secondary)' }}>
            ⚡ 1-Click Demo Accounts:
          </div>
          <div className="flex flex-col gap-2">
            <button 
              type="button" 
              onClick={() => handleQuickLogin('student@careerbridge.com', 'password123')}
              className="btn btn-outline flex items-center justify-between"
              style={{ padding: '8px 12px', fontSize: '0.9rem', width: '100%' }}>
              <span className="flex items-center gap-2"><Award size={18} color="#2563eb" /> Student Persona</span>
              <span className="badge badge-info" style={{ fontSize: '0.75rem' }}>Demo</span>
            </button>
            <button 
              type="button" 
              onClick={() => handleQuickLogin('recruiter@careerbridge.com', 'password123')}
              className="btn btn-outline flex items-center justify-between"
              style={{ padding: '8px 12px', fontSize: '0.9rem', width: '100%' }}>
              <span className="flex items-center gap-2"><UserCheck size={18} color="#059669" /> Recruiter Portal</span>
              <span className="badge badge-success" style={{ fontSize: '0.75rem' }}>Demo</span>
            </button>
            <button 
              type="button" 
              onClick={() => handleQuickLogin('admin@careerbridge.com', 'password123')}
              className="btn btn-outline flex items-center justify-between"
              style={{ padding: '8px 12px', fontSize: '0.9rem', width: '100%' }}>
              <span className="flex items-center gap-2"><Shield size={18} color="#dc2626" /> Admin Panel</span>
              <span className="badge badge-danger" style={{ fontSize: '0.75rem' }}>Demo</span>
            </button>
          </div>
        </div>

        <div style={{ textAlign: 'center', marginTop: '16px', fontSize: '1rem', fontWeight: 'bold' }}>
          Don't have an account? <Link to="/register">Register here</Link>
        </div>
      </div>
    </div>
  );
}
