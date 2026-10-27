import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../api';
import { Pencil } from 'lucide-react';

export default function Register() {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    role: 'STUDENT',
    college: '',
    branch: '',
    graduationYear: '',
    cgpa: ''
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    const payload = {
      name: formData.name,
      email: formData.email,
      password: formData.password,
      role: formData.role,
      college: formData.college || null,
      branch: formData.branch || null,
      graduationYear: formData.graduationYear ? parseInt(formData.graduationYear, 10) : null,
      cgpa: formData.cgpa ? parseFloat(formData.cgpa) : null,
    };

    try {
      await api.post('/auth/register', payload);
      setSuccess('Account created successfully! Redirecting to login...');
      setTimeout(() => navigate('/login'), 1500);
    } catch (err) {
      setError(typeof err.response?.data === 'string' ? err.response?.data : err.response?.data?.message || 'Failed to register account.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex justify-center items-center" style={{ minHeight: '100vh', padding: '40px 15px' }}>
      <div className="glass-panel" style={{ width: '100%', maxWidth: '520px' }}>
        <div className="flex flex-col items-center" style={{ marginBottom: '20px' }}>
          <Pencil size={56} color="#f9a8d4" className="floating-element" style={{ marginBottom: '12px' }}/>
          <h2>Join CareerBridge</h2>
          <p style={{ color: 'var(--text-secondary)', marginTop: '2px' }}>Start managing your applications & career portfolio</p>
        </div>
        
        {error && <div className="badge badge-danger" style={{ marginBottom: '16px', display: 'block', textAlign: 'center' }}>{error}</div>}
        {success && <div className="badge badge-success" style={{ marginBottom: '16px', display: 'block', textAlign: 'center' }}>{success}</div>}
        
        <form onSubmit={handleRegister}>
          <div className="form-group">
            <label>Full Name</label>
            <input type="text" name="name" value={formData.name} onChange={handleChange} className="form-input" required placeholder="e.g. Jane Doe" />
          </div>
          
          <div className="form-group">
            <label>Email Address</label>
            <input type="email" name="email" value={formData.email} onChange={handleChange} className="form-input" required placeholder="e.g. jane@university.edu" />
          </div>
          
          <div className="form-group">
            <label>Password</label>
            <input type="password" name="password" value={formData.password} onChange={handleChange} className="form-input" required placeholder="At least 6 characters" />
          </div>
          
          <div className="form-group">
            <label>I am registering as a...</label>
            <select name="role" value={formData.role} onChange={handleChange} className="form-input">
              <option value="STUDENT">Student Candidate</option>
              <option value="RECRUITER">Company Recruiter</option>
            </select>
          </div>

          {formData.role === 'STUDENT' && (
            <div style={{ padding: '16px', border: '2px dashed var(--ink-black)', borderRadius: '12px', marginBottom: '20px', background: 'rgba(253, 224, 71, 0.1)' }}>
              <h3 style={{ fontSize: '1.2rem', marginBottom: '12px' }}>Academic & Cut-off Profile</h3>
              <div className="form-group">
                <label>College / University</label>
                <input type="text" name="college" value={formData.college} onChange={handleChange} className="form-input" placeholder="e.g. Stanford University" />
              </div>
              <div className="form-group">
                <label>Branch / Major</label>
                <input type="text" name="branch" value={formData.branch} onChange={handleChange} className="form-input" placeholder="e.g. Computer Science" />
              </div>
              <div className="flex gap-4">
                <div className="form-group w-full">
                  <label>Graduation Year</label>
                  <input type="number" name="graduationYear" value={formData.graduationYear} onChange={handleChange} className="form-input" placeholder="e.g. 2026" />
                </div>
                <div className="form-group w-full">
                  <label>CGPA (Scale 10.0)</label>
                  <input type="number" step="0.1" name="cgpa" value={formData.cgpa} onChange={handleChange} className="form-input" placeholder="e.g. 8.5" />
                </div>
              </div>
            </div>
          )}

          <button type="submit" className="btn btn-primary w-full" style={{ fontSize: '1.2rem', padding: '12px' }} disabled={loading}>
            {loading ? 'Creating Account...' : 'Create Account'}
          </button>
        </form>
        
        <div style={{ textAlign: 'center', marginTop: '20px', fontSize: '1rem', fontWeight: 'bold' }}>
          Already have an account? <Link to="/login">Login here</Link>
        </div>
      </div>
    </div>
  );
}
