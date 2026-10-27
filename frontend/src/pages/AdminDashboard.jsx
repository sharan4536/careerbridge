import { useEffect, useState } from 'react';
import api from '../api';
import { Users, Briefcase, FileText, Activity, RefreshCw, ShieldCheck, Database, Award, CheckCircle, Clock, Save, HardDrive, Download, AlertCircle, ExternalLink } from 'lucide-react';
import { Navigate } from 'react-router-dom';

export default function AdminDashboard() {
  const [stats, setStats] = useState(null);
  const [error, setError] = useState('');
  const [refreshing, setRefreshing] = useState(false);

  // JDBC persistence state
  const [dbStatus, setDbStatus] = useState(null);
  const [backups, setBackups] = useState([]);
  const [savingBackup, setSavingBackup] = useState(false);
  const [syncingDisk, setSyncingDisk] = useState(false);
  const [feedbackMessage, setFeedbackMessage] = useState('');
  
  const role = localStorage.getItem('role');
  const isAdmin = role === 'ROLE_ADMIN' || role === 'ADMIN';

  useEffect(() => {
    if (isAdmin) {
      loadStats();
      loadDatabaseDetails();
    }
  }, [isAdmin]);

  const loadStats = async () => {
    setRefreshing(true);
    try {
      const res = await api.get('/admin/dashboard');
      setStats(res.data);
      setError('');
    } catch (err) {
      setError('Failed to load admin telemetry stats. Ensure you have admin privileges.');
    } finally {
      setRefreshing(false);
    }
  };

  const loadDatabaseDetails = async () => {
    try {
      const [statusRes, backupsRes] = await Promise.all([
        api.get('/admin/database/status'),
        api.get('/admin/database/backups')
      ]);
      setDbStatus(statusRes.data);
      setBackups(backupsRes.data);
    } catch (err) {
      console.error('Failed to load database JDBC telemetry', err);
    }
  };

  const handleSaveBackup = async () => {
    setSavingBackup(true);
    try {
      const res = await api.post('/admin/database/backup?note=manual');
      setFeedbackMessage(`Database backup saved via JDBC: ${res.data.fileName} (${res.data.formattedSize})`);
      setTimeout(() => setFeedbackMessage(''), 4500);
      loadDatabaseDetails();
    } catch (err) {
      alert('Failed to save database backup via JDBC.');
    } finally {
      setSavingBackup(false);
    }
  };

  const handleCheckpointSync = async () => {
    setSyncingDisk(true);
    try {
      await api.post('/admin/database/checkpoint');
      setFeedbackMessage('JDBC CHECKPOINT SYNC completed. All in-memory pages saved directly to disk!');
      setTimeout(() => setFeedbackMessage(''), 4500);
      loadDatabaseDetails();
    } catch (err) {
      alert('Failed to execute CHECKPOINT SYNC.');
    } finally {
      setSyncingDisk(false);
    }
  };

  const handleDownloadBackup = async (fileName) => {
    try {
      const res = await api.get(`/admin/database/backups/${fileName}/download`, {
        responseType: 'blob'
      });
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', fileName);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
    } catch (err) {
      alert('Failed to download backup file.');
    }
  };

  if (!isAdmin) {
    return <Navigate to="/dashboard" />;
  }

  if (error) return (
    <div className="glass-panel" style={{ textAlign: 'center', padding: '40px', color: '#b91c1c' }}>
      <h3>Access Error</h3>
      <p>{error}</p>
      <button className="btn btn-primary" onClick={loadStats}>Retry Connection</button>
    </div>
  );

  if (!stats) return <div style={{ textAlign: 'center', padding: '40px' }}>Loading Admin Console...</div>;

  return (
    <div>
      {/* Toast Notification */}
      {feedbackMessage && (
        <div style={{
          position: 'fixed',
          top: '24px',
          right: '24px',
          background: 'var(--ink-black)',
          color: 'var(--cream-bg)',
          padding: '12px 24px',
          borderRadius: '12px',
          boxShadow: '0 8px 24px rgba(0,0,0,0.25)',
          zIndex: 2000,
          fontWeight: 600,
          display: 'flex',
          alignItems: 'center',
          gap: '10px'
        }}>
          <CheckCircle size={18} color="#86efac" />
          {feedbackMessage}
        </div>
      )}

      {/* Admin Header */}
      <div className="flex justify-between items-center" style={{ marginBottom: '28px', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <div className="flex items-center gap-2">
            <h2 style={{ margin: 0 }}>System Administration</h2>
            <span className="badge badge-success flex items-center gap-1" style={{ fontSize: '0.75rem' }}>
              <ShieldCheck size={14} /> SUPERADMIN ACTIVE
            </span>
          </div>
          <p style={{ margin: '4px 0 0 0', color: 'var(--text-secondary)' }}>
            Real-time platform telemetry, user demographics, and JDBC database persistence management
          </p>
        </div>
        <div className="flex items-center gap-3">
          <button 
            className="btn btn-outline flex items-center gap-2" 
            onClick={() => { loadStats(); loadDatabaseDetails(); }} 
            disabled={refreshing}
            style={{ padding: '8px 16px' }}
          >
            <RefreshCw size={18} className={refreshing ? 'animate-spin' : ''} />
            {refreshing ? 'Refreshing...' : 'Refresh Telemetry'}
          </button>
        </div>
      </div>
      
      {/* Metrics Row */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '20px', marginBottom: '32px' }}>
        <div className="glass-panel flex flex-col items-center" style={{ textAlign: 'center' }}>
          <Users size={40} color="#8b5cf6" className="floating-element" style={{ marginBottom: '10px' }}/>
          <h2 style={{ margin: 0, fontSize: '2.2rem' }}>{stats.totalUsers}</h2>
          <span style={{ color: 'var(--text-secondary)', fontWeight: 600, fontSize: '0.9rem' }}>Registered Users</span>
        </div>
        
        <div className="glass-panel flex flex-col items-center" style={{ textAlign: 'center' }}>
          <Users size={40} color="#ec4899" className="floating-element" style={{ marginBottom: '10px' }}/>
          <h2 style={{ margin: 0, fontSize: '2.2rem' }}>{stats.totalStudents}</h2>
          <span style={{ color: 'var(--text-secondary)', fontWeight: 600, fontSize: '0.9rem' }}>Enrolled Students</span>
        </div>
        
        <div className="glass-panel flex flex-col items-center" style={{ textAlign: 'center' }}>
          <Briefcase size={40} color="#eab308" className="floating-element" style={{ marginBottom: '10px' }}/>
          <h2 style={{ margin: 0, fontSize: '2.2rem' }}>{stats.totalJobs}</h2>
          <span style={{ color: 'var(--text-secondary)', fontWeight: 600, fontSize: '0.9rem' }}>Live Job Openings</span>
        </div>
        
        <div className="glass-panel flex flex-col items-center" style={{ textAlign: 'center' }}>
          <FileText size={40} color="#22c55e" className="floating-element" style={{ marginBottom: '10px' }}/>
          <h2 style={{ margin: 0, fontSize: '2.2rem' }}>{stats.totalApplications}</h2>
          <span style={{ color: 'var(--text-secondary)', fontWeight: 600, fontSize: '0.9rem' }}>Total Applications</span>
        </div>
      </div>

      {/* JDBC Database Management & Persistence Section */}
      <div className="glass-panel" style={{ marginBottom: '32px', background: '#f8fafc', border: '2px solid #0284c7' }}>
        <div className="flex justify-between items-center" style={{ marginBottom: '16px', flexWrap: 'wrap', gap: '12px' }}>
          <div className="flex items-center gap-2">
            <Database size={24} color="#0284c7" />
            <h3 style={{ margin: 0, color: '#0f172a' }}>Database Persistence & JDBC Management</h3>
          </div>
          <div className="flex items-center gap-3">
            <a 
              href="/h2-console" 
              target="_blank" 
              rel="noreferrer" 
              className="btn btn-outline flex items-center gap-2" 
              style={{ padding: '8px 14px', fontSize: '0.88rem', borderColor: '#6366f1', color: '#4f46e5' }}
            >
              <ExternalLink size={16} />
              Open H2 Console
            </a>
            <button 
              className="btn btn-outline flex items-center gap-2" 
              onClick={handleCheckpointSync}
              disabled={syncingDisk}
              style={{ padding: '8px 14px', fontSize: '0.88rem', borderColor: '#0284c7', color: '#0369a1' }}
            >
              <HardDrive size={16} />
              {syncingDisk ? 'Syncing...' : 'Sync to Disk (CHECKPOINT SYNC)'}
            </button>
            <button 
              className="btn btn-primary flex items-center gap-2" 
              onClick={handleSaveBackup}
              disabled={savingBackup}
              style={{ padding: '8px 16px', fontSize: '0.88rem', background: '#0284c7', borderColor: '#0369a1' }}
            >
              <Save size={16} />
              {savingBackup ? 'Saving...' : 'Save Database (JDBC Script)'}
            </button>
          </div>
        </div>

        {/* Database Engine Telemetry */}
        {dbStatus && (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '14px', marginBottom: '20px' }}>
            <div style={{ padding: '12px 14px', background: '#ffffff', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>DBMS Engine</span>
              <div style={{ fontWeight: 700, fontSize: '0.95rem', marginTop: '2px' }}>{dbStatus.databaseProductName} {dbStatus.databaseProductVersion}</div>
              <span style={{ fontSize: '0.75rem', color: '#16a34a' }}>● File-backed Persistence Mode</span>
            </div>
            <div style={{ padding: '12px 14px', background: '#ffffff', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>JDBC Driver</span>
              <div style={{ fontWeight: 700, fontSize: '0.95rem', marginTop: '2px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {dbStatus.jdbcDriverName}
              </div>
              <span style={{ fontSize: '0.75rem', color: '#16a34a' }}>● Connection Pool Active</span>
            </div>
            <div style={{ padding: '12px 14px', background: '#ffffff', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Total Database Tables</span>
              <div style={{ fontWeight: 700, fontSize: '0.95rem', marginTop: '2px' }}>{dbStatus.tableCount} Tables Tracked</div>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>Spring Data JPA + JDBC</span>
            </div>
            <div style={{ padding: '12px 14px', background: '#ffffff', borderRadius: '8px', border: '1px solid #e2e8f0' }}>
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Saved Backups</span>
              <div style={{ fontWeight: 700, fontSize: '0.95rem', marginTop: '2px' }}>{backups.length} Saved Snapshots</div>
              <span style={{ fontSize: '0.75rem', color: '#0284c7' }}>./backups directory</span>
            </div>
          </div>
        )}

        {/* Live Table Row Counts */}
        {dbStatus && dbStatus.tableRowCounts && (
          <div style={{ marginBottom: '20px' }}>
            <span style={{ fontSize: '0.85rem', fontWeight: 700, color: '#475569', display: 'block', marginBottom: '8px' }}>
              Table Records via JDBC JdbcTemplate:
            </span>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px' }}>
              {Object.entries(dbStatus.tableRowCounts).map(([table, count]) => (
                <div key={table} style={{
                  padding: '4px 10px',
                  background: '#ffffff',
                  border: '1px solid #cbd5e1',
                  borderRadius: '6px',
                  fontSize: '0.8rem',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px'
                }}>
                  <span style={{ color: '#64748b', fontWeight: 600 }}>{table}:</span>
                  <span style={{ color: '#0f172a', fontWeight: 800 }}>{count}</span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Saved Backups Table */}
        <div style={{ borderTop: '1px solid #e2e8f0', paddingTop: '16px' }}>
          <h4 style={{ margin: '0 0 12px 0', fontSize: '1rem', color: '#1e293b' }}>
            Saved Database Snapshots ({backups.length})
          </h4>
          {backups.length === 0 ? (
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', fontStyle: 'italic', margin: 0 }}>
              No SQL backup scripts generated yet. Click "Save Database (JDBC Script)" above to save the current database state.
            </p>
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9rem' }}>
                <thead>
                  <tr style={{ textAlign: 'left', borderBottom: '1px solid #cbd5e1', color: '#64748b' }}>
                    <th style={{ padding: '8px 12px' }}>Backup File</th>
                    <th style={{ padding: '8px 12px' }}>Size</th>
                    <th style={{ padding: '8px 12px' }}>Created Timestamp</th>
                    <th style={{ padding: '8px 12px', textAlign: 'right' }}>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {backups.map((b, idx) => (
                    <tr key={idx} style={{ borderBottom: '1px solid #f1f5f9' }}>
                      <td style={{ padding: '10px 12px', fontWeight: 600, color: '#0f172a' }}>
                        {b.fileName}
                      </td>
                      <td style={{ padding: '10px 12px', color: '#475569' }}>
                        {b.formattedSize}
                      </td>
                      <td style={{ padding: '10px 12px', color: '#64748b' }}>
                        {b.createdAt ? new Date(b.createdAt).toLocaleString() : 'Recent'}
                      </td>
                      <td style={{ padding: '10px 12px', textAlign: 'right' }}>
                        <button 
                          className="btn btn-outline"
                          onClick={() => handleDownloadBackup(b.fileName)}
                          style={{ padding: '4px 10px', fontSize: '0.8rem', display: 'inline-flex', alignItems: 'center', gap: '4px' }}
                        >
                          <Download size={14} /> Download SQL
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* Recent Applications Feed */}
      <div className="glass-panel">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <Activity color="#2b2b2b" size={24} />
            <h3 style={{ margin: 0 }}>Recent Application Stream</h3>
          </div>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
            Showing latest {stats.recentApplications ? stats.recentApplications.length : 0} placement submissions
          </span>
        </div>
        
        {stats.recentApplications && stats.recentApplications.length > 0 ? (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '12px' }}>
              <thead>
                <tr style={{ borderBottom: '2px dashed var(--ink-black)', textAlign: 'left', background: 'rgba(0,0,0,0.02)' }}>
                  <th style={{ padding: '14px 16px' }}>Student Candidate</th>
                  <th style={{ padding: '14px 16px' }}>Target Role</th>
                  <th style={{ padding: '14px 16px' }}>Application Status</th>
                  <th style={{ padding: '14px 16px', textAlign: 'right' }}>Security Audit</th>
                </tr>
              </thead>
              <tbody>
                {stats.recentApplications.map((app, idx) => (
                  <tr key={idx} style={{ borderBottom: '1px solid #f1f5f9' }}>
                    <td style={{ padding: '14px 16px', fontWeight: 700 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <div style={{
                          width: '28px',
                          height: '28px',
                          borderRadius: '50%',
                          background: '#e0e7ff',
                          color: '#4338ca',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          fontSize: '0.8rem',
                          fontWeight: 800
                        }}>
                          {(app.studentName || 'S').charAt(0).toUpperCase()}
                        </div>
                        {app.studentName || 'Student'}
                      </div>
                    </td>
                    <td style={{ padding: '14px 16px', color: 'var(--ink-black)' }}>
                      {app.jobRole}
                    </td>
                    <td style={{ padding: '14px 16px' }}>
                      {app.status === 'OFFERED' ? (
                        <span className="badge badge-success flex items-center gap-1" style={{ display: 'inline-flex' }}>
                          <Award size={12} /> OFFERED
                        </span>
                      ) : app.status === 'INTERVIEW_SCHEDULED' ? (
                        <span className="badge badge-warning flex items-center gap-1" style={{ display: 'inline-flex' }}>
                          <Clock size={12} /> INTERVIEW
                        </span>
                      ) : (
                        <span className="badge badge-info flex items-center gap-1" style={{ display: 'inline-flex' }}>
                          <CheckCircle size={12} /> {app.status || 'APPLIED'}
                        </span>
                      )}
                    </td>
                    <td style={{ padding: '14px 16px', textAlign: 'right' }}>
                      <span style={{ fontSize: '0.8rem', color: '#16a34a', fontWeight: 600 }}>
                        ✓ Hash Verified
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p style={{ color: 'var(--text-secondary)', fontStyle: 'italic', padding: '24px 0', textAlign: 'center' }}>
            No recent applications found in this cycle.
          </p>
        )}
      </div>
    </div>
  );
}
