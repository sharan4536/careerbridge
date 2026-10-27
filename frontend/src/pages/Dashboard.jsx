import { useEffect, useState } from 'react';
import api from '../api';
import { Briefcase, CheckCircle, XCircle, Calendar, Star, User, Edit3, X, School, BookOpen, Award } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function Dashboard() {
  const [stats, setStats] = useState(null);
  const [profile, setProfile] = useState(null);
  const [editingProfile, setEditingProfile] = useState(false);
  const [profileForm, setProfileForm] = useState({
    name: '',
    phone: '',
    degree: '',
    branch: '',
    college: '',
    graduationYear: '',
    cgpa: '',
    backlogs: 0,
    preferredRoles: '',
    preferredLocations: '',
    salaryExpectations: ''
  });
  const [updateMsg, setUpdateMsg] = useState('');

  useEffect(() => {
    fetchDashboard();
    fetchProfile();
  }, []);

  const fetchDashboard = () => {
    api.get('/dashboard')
      .then(res => setStats(res.data))
      .catch(console.error);
  };

  const fetchProfile = () => {
    api.get('/students/profile')
      .then(res => {
        setProfile(res.data);
        setProfileForm({
          name: res.data.name || '',
          phone: res.data.phone || '',
          degree: res.data.degree || '',
          branch: res.data.branch || '',
          college: res.data.college || '',
          graduationYear: res.data.graduationYear || '',
          cgpa: res.data.cgpa || '',
          backlogs: res.data.backlogs || 0,
          preferredRoles: res.data.preferredRoles || '',
          preferredLocations: res.data.preferredLocations || '',
          salaryExpectations: res.data.salaryExpectations || ''
        });
      })
      .catch(console.error);
  };

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await api.put('/students/profile', profileForm);
      setProfile(res.data);
      setUpdateMsg('Profile updated successfully!');
      setTimeout(() => {
        setUpdateMsg('');
        setEditingProfile(false);
      }, 1500);
    } catch (err) {
      alert('Failed to update profile: ' + (err.response?.data?.message || err.message));
    }
  };

  if (!stats) return <div style={{ textAlign: 'center', padding: '60px' }}>Loading dashboard...</div>;

  return (
    <div>
      <div className="flex justify-between items-center" style={{ marginBottom: '24px' }}>
        <div>
          <h2>Student Dashboard</h2>
          <p style={{ color: 'var(--text-secondary)', marginTop: '4px' }}>
            Welcome back, <strong>{profile?.name || (profile?.email ? profile.email.split('@')[0] : 'Student')}</strong>! Here is your placement summary.
          </p>
        </div>
        <div className="flex gap-2">
          <button onClick={() => setEditingProfile(true)} className="btn btn-outline flex items-center gap-2">
            <Edit3 size={18} /> Edit Academic Profile
          </button>
          <Link to="/jobs" className="btn btn-primary">Browse Jobs</Link>
        </div>
      </div>
      
      {/* 4 Stats Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px', marginBottom: '32px' }}>
        <div className="glass-panel flex flex-col items-center">
          <Briefcase size={44} color="#60a5fa" className="floating-element" style={{ marginBottom: '8px' }}/>
          <h3 style={{ fontSize: '2rem', margin: '4px 0' }}>{stats.totalApplications}</h3>
          <span style={{ color: 'var(--text-secondary)', fontWeight: 'bold' }}>Total Applications</span>
        </div>
        <div className="glass-panel flex flex-col items-center">
          <Calendar size={44} color="#fcd34d" className="floating-element" style={{ marginBottom: '8px' }}/>
          <h3 style={{ fontSize: '2rem', margin: '4px 0' }}>{stats.interviewsScheduled}</h3>
          <span style={{ color: 'var(--text-secondary)', fontWeight: 'bold' }}>Interviews</span>
        </div>
        <div className="glass-panel flex flex-col items-center">
          <CheckCircle size={44} color="#34d399" className="floating-element" style={{ marginBottom: '8px' }}/>
          <h3 style={{ fontSize: '2rem', margin: '4px 0' }}>{stats.offers}</h3>
          <span style={{ color: 'var(--text-secondary)', fontWeight: 'bold' }}>Offers</span>
        </div>
        <div className="glass-panel flex flex-col items-center">
          <XCircle size={44} color="#ff6b9e" className="floating-element" style={{ marginBottom: '8px' }}/>
          <h3 style={{ fontSize: '2rem', margin: '4px 0' }}>{stats.rejections}</h3>
          <span style={{ color: 'var(--text-secondary)', fontWeight: 'bold' }}>Rejections</span>
        </div>
      </div>

      {/* Profile & Analytics Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '24px', marginBottom: '32px' }}>
        {/* Academic Profile Snapshot */}
        <div className="glass-panel" style={{ background: 'rgba(253, 224, 71, 0.08)' }}>
          <div className="flex justify-between items-center" style={{ marginBottom: '16px' }}>
            <h3 style={{ margin: 0 }} className="flex items-center gap-2">
              <School size={22} color="#f59e0b" /> Academic & Eligibility Profile
            </h3>
            <span className="badge badge-success">Active</span>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', fontSize: '0.95rem' }}>
            <div>
              <span style={{ color: 'var(--text-secondary)', display: 'block', fontSize: '0.85rem' }}>College / University</span>
              <strong>{profile?.college || 'Not set'}</strong>
            </div>
            <div>
              <span style={{ color: 'var(--text-secondary)', display: 'block', fontSize: '0.85rem' }}>Major / Branch</span>
              <strong>{profile?.branch || 'Not set'}</strong>
            </div>
            <div>
              <span style={{ color: 'var(--text-secondary)', display: 'block', fontSize: '0.85rem' }}>CGPA (Scale 10.0)</span>
              <strong style={{ fontSize: '1.2rem', color: 'var(--ink-blue)' }}>{profile?.cgpa || '0.0'}</strong>
            </div>
            <div>
              <span style={{ color: 'var(--text-secondary)', display: 'block', fontSize: '0.85rem' }}>Graduation Year</span>
              <strong>{profile?.graduationYear || 'Not set'}</strong>
            </div>
            <div>
              <span style={{ color: 'var(--text-secondary)', display: 'block', fontSize: '0.85rem' }}>Active Backlogs</span>
              <strong>{profile?.backlogs ?? 0}</strong>
            </div>
            <div>
              <span style={{ color: 'var(--text-secondary)', display: 'block', fontSize: '0.85rem' }}>Target Roles</span>
              <strong>{profile?.preferredRoles || 'Not set'}</strong>
            </div>
            <div>
              <span style={{ color: 'var(--text-secondary)', display: 'block', fontSize: '0.85rem' }}>Expected CTC (INR)</span>
              <strong style={{ color: '#16a34a' }}>{profile?.salaryExpectations || 'Not set'}</strong>
            </div>
          </div>
        </div>

        {/* Fit Analytics */}
        <div className="glass-panel">
          <h3 className="flex items-center gap-2" style={{ margin: '0 0 16px 0' }}>
            <Star size={22} color="#fcd34d" /> Average Eligibility & Job Fit
          </h3>
          <div className="flex items-center gap-4 mt-2">
            <Star size={56} color="#fcd34d" className="floating-element" />
            <div>
              <h2 style={{ margin: 0, fontSize: '2.5rem', color: 'var(--ink-black)' }}>
                {stats.averageJobFit?.Overall ? `${stats.averageJobFit.Overall.toFixed(1)}%` : (stats.totalApplications > 0 ? '0%' : 'N/A')}
              </h2>
              <span style={{ color: 'var(--text-secondary)', fontWeight: 'bold' }}>Overall match across applications</span>
            </div>
          </div>
          <div style={{ marginTop: '16px', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
            ⚡ Calculated from branch eligibility, GPA thresholds, and resume skill keywords.
          </div>
        </div>
      </div>

      {/* Resume Performance Breakdown */}
      <div className="glass-panel">
        <h3 className="flex items-center gap-2" style={{ marginBottom: '16px' }}>
          <BookOpen size={22} color="#a78bfa" /> Resume Vault Utilization
        </h3>
        {stats.resumePerformance && Object.keys(stats.resumePerformance).length > 0 ? (
          <ul style={{ paddingLeft: '20px', listStyleType: 'disc' }}>
            {Object.entries(stats.resumePerformance).map(([resume, count]) => (
              <li key={resume} style={{ marginBottom: '10px', fontSize: '1.05rem' }}>
                <strong>{resume}</strong>: <span className="badge badge-info" style={{ marginLeft: '8px' }}>{count} submitted applications</span>
              </li>
            ))}
          </ul>
        ) : (
          <p style={{ color: 'var(--text-secondary)' }}>
            No resumes submitted yet. <Link to="/resumes">Upload your resume in the vault</Link> to start applying!
          </p>
        )}
      </div>

      {/* Edit Profile Modal */}
      {editingProfile && (
        <div className="modal-backdrop">
          <div className="modal-content glass-panel" style={{ width: '90%', maxWidth: '540px' }}>
            <div className="flex justify-between items-center" style={{ marginBottom: '20px' }}>
              <h2 style={{ margin: 0 }}>Update Academic Profile</h2>
              <button onClick={() => setEditingProfile(false)} className="btn btn-outline" style={{ padding: '4px' }}>
                <X size={20} />
              </button>
            </div>

            {updateMsg && <div className="badge badge-success" style={{ marginBottom: '16px', display: 'block', textAlign: 'center' }}>{updateMsg}</div>}

            <form onSubmit={handleProfileSubmit}>
              <div className="form-group">
                <label>Full Name</label>
                <input type="text" className="form-input" value={profileForm.name} onChange={e => setProfileForm({ ...profileForm, name: e.target.value })} required />
              </div>

              <div className="flex gap-4">
                <div className="form-group w-full">
                  <label>College</label>
                  <input type="text" className="form-input" value={profileForm.college} onChange={e => setProfileForm({ ...profileForm, college: e.target.value })} />
                </div>
                <div className="form-group w-full">
                  <label>Branch / Major</label>
                  <input type="text" className="form-input" value={profileForm.branch} onChange={e => setProfileForm({ ...profileForm, branch: e.target.value })} />
                </div>
              </div>

              <div className="flex gap-4">
                <div className="form-group w-full">
                  <label>CGPA</label>
                  <input type="number" step="0.1" className="form-input" value={profileForm.cgpa} onChange={e => setProfileForm({ ...profileForm, cgpa: parseFloat(e.target.value) })} />
                </div>
                <div className="form-group w-full">
                  <label>Graduation Year</label>
                  <input type="number" className="form-input" value={profileForm.graduationYear} onChange={e => setProfileForm({ ...profileForm, graduationYear: parseInt(e.target.value, 10) })} />
                </div>
                <div className="form-group w-full">
                  <label>Backlogs</label>
                  <input type="number" className="form-input" value={profileForm.backlogs} onChange={e => setProfileForm({ ...profileForm, backlogs: parseInt(e.target.value, 10) })} />
                </div>
              </div>

              <div className="form-group">
                <label>Preferred Roles</label>
                <input type="text" className="form-input" value={profileForm.preferredRoles} onChange={e => setProfileForm({ ...profileForm, preferredRoles: e.target.value })} placeholder="e.g. Software Engineer, Frontend Developer" />
              </div>

              <div className="form-group">
                <label>Expected CTC / Salary Range (INR)</label>
                <input type="text" className="form-input" value={profileForm.salaryExpectations} onChange={e => setProfileForm({ ...profileForm, salaryExpectations: e.target.value })} placeholder="e.g. ₹18 LPA - ₹24 LPA" />
              </div>

              <div className="flex justify-end gap-2 mt-4">
                <button type="button" className="btn btn-outline" onClick={() => setEditingProfile(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary">Save Changes</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
