import { useEffect, useState } from 'react';
import api from '../api';
import { CheckSquare, Building, History, X, FileText, Calendar, CheckCircle, Clock } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function Applications() {
  const [applications, setApplications] = useState([]);
  const [selectedApp, setSelectedApp] = useState(null);
  const [timeline, setTimeline] = useState([]);
  const [timelineLoading, setTimelineLoading] = useState(false);

  useEffect(() => {
    fetchApplications();
  }, []);

  const fetchApplications = async () => {
    try {
      const res = await api.get('/applications');
      setApplications(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const handleOpenTimeline = async (app) => {
    setSelectedApp(app);
    setTimelineLoading(true);
    try {
      const res = await api.get(`/applications/${app.id}/timeline`);
      setTimeline(res.data);
    } catch (err) {
      setTimeline([]);
    } finally {
      setTimelineLoading(false);
    }
  };

  const getStatusBadge = (status) => {
    const s = (status || '').toUpperCase();
    if (s.contains ? s.contains('OFFER') : s.includes('OFFER')) return 'badge-success';
    if (s.contains ? s.contains('INTERVIEW') : s.includes('INTERVIEW')) return 'badge-warning';
    if (s.contains ? s.contains('REJECT') : s.includes('REJECT')) return 'badge-danger';
    return 'badge-info';
  };

  return (
    <div>
      <div className="flex justify-between items-center" style={{ marginBottom: '24px' }}>
        <div>
          <h2>My Applications</h2>
          <p style={{ color: 'var(--text-secondary)', marginTop: '4px' }}>
            Track submissions, immutable resume snapshots, and recruiting stages
          </p>
        </div>
        <Link to="/jobs" className="btn btn-primary">Find More Jobs</Link>
      </div>
      
      {applications.length === 0 ? (
        <div className="glass-panel" style={{ textAlign: 'center', padding: '60px' }}>
          <CheckSquare size={48} color="#a78bfa" className="floating-element" style={{ marginBottom: '16px' }} />
          <h3 style={{ margin: '0 0 8px 0' }}>No active applications</h3>
          <p style={{ fontSize: '1.1rem', color: 'var(--text-secondary)', marginBottom: '20px' }}>
            You haven't submitted any applications yet. Explore openings or use the Auto-Apply engine!
          </p>
          <Link to="/jobs" className="btn btn-primary">Browse Jobs Feed</Link>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr', gap: '20px' }}>
          {applications.map(app => (
            <div key={app.id} className="glass-panel">
              <div className="flex justify-between items-center" style={{ marginBottom: '12px' }}>
                <div>
                  <h3 style={{ margin: '0 0 6px 0', fontSize: '1.3rem' }}>{app.jobRole}</h3>
                  <div className="flex items-center gap-3" style={{ color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
                    <span className="flex items-center gap-1"><Building size={16} /> <strong>{app.companyName}</strong></span>
                    <span>•</span>
                    <span className="flex items-center gap-1"><Calendar size={16} /> Applied on {new Date(app.appliedDate).toLocaleDateString()}</span>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <span className={`badge ${getStatusBadge(app.status)}`} style={{ fontSize: '0.9rem', padding: '4px 12px' }}>
                    {app.status}
                  </span>
                  <div style={{ background: 'var(--bg-paper)', padding: '6px 12px', borderRadius: '8px', border: '2px solid var(--ink-black)', fontWeight: 'bold' }}>
                    Fit: {app.jobFitScore || 0}%
                  </div>
                </div>
              </div>

              {/* Snapshot and timeline footer */}
              <div className="flex justify-between items-center" style={{ borderTop: '1px solid var(--border-color)', paddingTop: '12px', marginTop: '8px', fontSize: '0.9rem' }}>
                <div className="flex items-center gap-2" style={{ color: '#475569' }}>
                  <FileText size={16} color="#7c3aed" />
                  <span>Snapshot: <strong>{app.resumeName || 'General Resume'}</strong> (v{app.resumeVersion || 1})</span>
                </div>
                <button 
                  onClick={() => handleOpenTimeline(app)}
                  className="btn btn-outline flex items-center gap-1"
                  style={{ padding: '4px 10px', fontSize: '0.85rem' }}>
                  <History size={14} /> View Audit Timeline
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Timeline Modal */}
      {selectedApp && (
        <div className="modal-backdrop">
          <div className="modal-content glass-panel" style={{ width: '90%', maxWidth: '520px' }}>
            <div className="flex justify-between items-center" style={{ marginBottom: '16px' }}>
              <div>
                <h3 style={{ margin: 0 }}>Application Timeline</h3>
                <span style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
                  {selectedApp.jobRole} at {selectedApp.companyName}
                </span>
              </div>
              <button onClick={() => setSelectedApp(null)} className="btn btn-outline" style={{ padding: '4px' }}>
                <X size={20} />
              </button>
            </div>

            {timelineLoading ? (
              <div style={{ padding: '30px', textAlign: 'center' }}>Loading timeline...</div>
            ) : timeline.length === 0 ? (
              <div style={{ padding: '20px', textAlign: 'center', color: 'var(--text-secondary)' }}>
                Application registered. No further status changes yet.
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '14px', margin: '16px 0' }}>
                {timeline.map((event, idx) => (
                  <div key={idx} style={{ display: 'flex', gap: '12px', alignItems: 'flex-start' }}>
                    <div style={{ background: '#3b82f6', color: '#fff', borderRadius: '50%', padding: '6px', marginTop: '2px' }}>
                      <Clock size={16} />
                    </div>
                    <div style={{ flex: 1 }}>
                      <div style={{ fontWeight: 'bold', fontSize: '1rem' }}>{event.eventType}</div>
                      <div style={{ color: '#475569', fontSize: '0.9rem' }}>{event.description}</div>
                      <div style={{ color: '#94a3b8', fontSize: '0.8rem', marginTop: '2px' }}>
                        {new Date(event.timestamp).toLocaleString()}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}

            <div className="flex justify-end mt-4">
              <button className="btn btn-outline" onClick={() => setSelectedApp(null)}>Close</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
