import { useEffect, useState } from 'react';
import api from '../api';
import { Building, MapPin, IndianRupee, PlusCircle, Users, CheckCircle, Clock, XCircle, Award, Download, ArrowRight, X } from 'lucide-react';
import { Navigate } from 'react-router-dom';

const formatSalaryInr = (salary) => {
  if (!salary) return '';
  return salary;
};

export default function RecruiterDashboard() {
  const [jobs, setJobs] = useState([]);
  const [formData, setFormData] = useState({
    role: '',
    companyName: '',
    location: '',
    salaryRange: '',
    requiredSkills: '',
    minCgpa: '',
    graduationYear: '',
    description: ''
  });
  const [showForm, setShowForm] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  // Applicants Modal state
  const [selectedJob, setSelectedJob] = useState(null);
  const [applicants, setApplicants] = useState([]);
  const [loadingApplicants, setLoadingApplicants] = useState(false);
  const [statusMessage, setStatusMessage] = useState('');

  const role = localStorage.getItem('role');
  const isRecruiter = role === 'ROLE_RECRUITER' || role === 'RECRUITER' || role === 'ROLE_ADMIN' || role === 'ADMIN';

  useEffect(() => {
    if (isRecruiter) {
      fetchJobs();
    }
  }, [isRecruiter]);

  const fetchJobs = async () => {
    try {
      const res = await api.get('/jobs');
      setJobs(res.data);
    } catch (err) {
      console.error('Failed to load jobs', err);
    }
  };

  const handleChange = (e) => setFormData({...formData, [e.target.name]: e.target.value});

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await api.post('/jobs', formData);
      setFormData({
        role: '',
        companyName: '',
        location: '',
        salaryRange: '',
        requiredSkills: '',
        minCgpa: '',
        graduationYear: '',
        description: ''
      });
      setShowForm(false);
      setStatusMessage('Job listing published successfully!');
      setTimeout(() => setStatusMessage(''), 4000);
      fetchJobs();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to post job');
    } finally {
      setSubmitting(false);
    }
  };


  const openApplicantsModal = async (job) => {
    setSelectedJob(job);
    setLoadingApplicants(true);
    try {
      const res = await api.get(`/applications/job/${job.id}`);
      setApplicants(res.data);
    } catch (err) {
      console.error(err);
      setApplicants([]);
    } finally {
      setLoadingApplicants(false);
    }
  };

  const handleUpdateStatus = async (appId, newStatus) => {
    try {
      const res = await api.put(`/applications/${appId}/status?status=${newStatus}`);
      setApplicants(prev => prev.map(a => a.id === appId ? { ...a, status: res.data.status } : a));
      setStatusMessage(`Application #${appId} updated to ${newStatus}`);
      setTimeout(() => setStatusMessage(''), 3500);
    } catch (err) {
      alert('Failed to update candidate status');
    }
  };

  const handleDownloadResume = async (versionId, resumeName) => {
    if (!versionId) {
      alert('Resume snapshot not available for direct download.');
      return;
    }
    try {
      const response = await api.get(`/resumes/versions/${versionId}/download`, {
        responseType: 'blob'
      });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `${resumeName || 'candidate_resume'}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
    } catch (err) {
      alert('Could not download resume file.');
    }
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'OFFERED':
        return <span className="badge badge-success flex items-center gap-1"><Award size={14} /> OFFERED</span>;
      case 'INTERVIEW_SCHEDULED':
        return <span className="badge badge-warning flex items-center gap-1"><Clock size={14} /> INTERVIEW</span>;
      case 'UNDER_REVIEW':
        return <span className="badge badge-info flex items-center gap-1"><Clock size={14} /> UNDER REVIEW</span>;
      case 'REJECTED':
        return <span className="badge badge-danger flex items-center gap-1"><XCircle size={14} /> REJECTED</span>;
      default:
        return <span className="badge badge-info flex items-center gap-1"><CheckCircle size={14} /> {status || 'APPLIED'}</span>;
    }
  };

  if (!isRecruiter) {
    return <Navigate to="/dashboard" />;
  }

  return (
    <div>
      {/* Toast Notification */}
      {statusMessage && (
        <div style={{
          position: 'fixed',
          top: '24px',
          right: '24px',
          background: 'var(--ink-black)',
          color: 'var(--cream-bg)',
          padding: '12px 24px',
          borderRadius: '12px',
          boxShadow: '0 8px 24px rgba(0,0,0,0.2)',
          zIndex: 2000,
          fontWeight: 600,
          display: 'flex',
          alignItems: 'center',
          gap: '8px'
        }}>
          <CheckCircle size={18} color="#86efac" />
          {statusMessage}
        </div>
      )}

      {/* Header Banner */}
      <div className="flex justify-between items-center" style={{ marginBottom: '28px', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <h2 style={{ margin: 0 }}>Recruiter Talent Portal</h2>
          <p style={{ margin: '4px 0 0 0', color: 'var(--text-secondary)' }}>
            Manage campus job openings, evaluate student fit scores, and fast-track interviews
          </p>
        </div>
        <button 
          className="btn btn-primary" 
          onClick={() => setShowForm(!showForm)}
          style={{ display: 'flex', alignItems: 'center', gap: '8px' }}
        >
          <PlusCircle size={20} />
          {showForm ? 'Close Editor' : 'Post New Job Opening'}
        </button>
      </div>

      {/* Job Creation Form */}
      {showForm && (
        <div className="glass-panel" style={{ marginBottom: '32px', background: 'rgba(253, 224, 71, 0.15)', borderColor: '#eab308' }}>
          <div className="flex justify-between items-center" style={{ marginBottom: '16px' }}>
            <h3 style={{ margin: 0 }}>Create Campus Job Opening</h3>
            <button className="btn btn-outline" style={{ padding: '4px 12px' }} onClick={() => setShowForm(false)}>
              Cancel
            </button>
          </div>
          <form onSubmit={handleSubmit}>
            <div className="flex gap-4" style={{ flexWrap: 'wrap' }}>
              <div className="form-group" style={{ flex: 1, minWidth: '240px' }}>
                <label>Role / Position Title</label>
                <input 
                  type="text" 
                  name="role" 
                  value={formData.role} 
                  onChange={handleChange} 
                  className="form-input" 
                  placeholder="e.g. Senior Backend Engineer"
                  required 
                />
              </div>
              <div className="form-group" style={{ flex: 1, minWidth: '240px' }}>
                <label>Company / Organization Name</label>
                <input 
                  type="text" 
                  name="companyName" 
                  value={formData.companyName} 
                  onChange={handleChange} 
                  className="form-input" 
                  placeholder="e.g. Google, Stripe, Microsoft" 
                  required 
                />
              </div>
            </div>
            <div className="flex gap-4" style={{ flexWrap: 'wrap' }}>
              <div className="form-group" style={{ flex: 1, minWidth: '240px' }}>
                <label>Location / Work Mode</label>
                <input 
                  type="text" 
                  name="location" 
                  value={formData.location} 
                  onChange={handleChange} 
                  className="form-input" 
                  placeholder="e.g. Bangalore, India (Hybrid)"
                  required 
                />
              </div>
              <div className="form-group" style={{ flex: 1, minWidth: '240px' }}>
                <label>Annual Salary / CTC Range (INR)</label>
                <input 
                  type="text" 
                  name="salaryRange" 
                  value={formData.salaryRange} 
                  onChange={handleChange} 
                  className="form-input" 
                  placeholder="e.g. ₹18 LPA - ₹24 LPA" 
                  required 
                />
              </div>
            </div>
            <div className="flex gap-4" style={{ flexWrap: 'wrap' }}>
              <div className="form-group" style={{ flex: 2, minWidth: '240px' }}>
                <label>Required Skills (comma-separated)</label>
                <input 
                  type="text" 
                  name="requiredSkills" 
                  value={formData.requiredSkills} 
                  onChange={handleChange} 
                  className="form-input" 
                  placeholder="e.g. Java, Spring Boot, React, SQL, AWS" 
                />
              </div>
              <div className="form-group" style={{ flex: 1, minWidth: '120px' }}>
                <label>Min CGPA</label>
                <input 
                  type="number" 
                  step="0.1" 
                  name="minCgpa" 
                  value={formData.minCgpa} 
                  onChange={handleChange} 
                  className="form-input" 
                  placeholder="6.5" 
                />
              </div>
              <div className="form-group" style={{ flex: 1, minWidth: '120px' }}>
                <label>Graduation Year</label>
                <input 
                  type="number" 
                  name="graduationYear" 
                  value={formData.graduationYear} 
                  onChange={handleChange} 
                  className="form-input" 
                  placeholder="2025" 
                />
              </div>
            </div>
            <div className="form-group">
              <label>Role Overview & Key Requirements</label>
              <textarea 
                name="description" 
                value={formData.description} 
                onChange={handleChange} 
                className="form-input" 
                rows="4" 
                placeholder="Detail the technical responsibilities, stack requirements, and culture fit..."
                required
              ></textarea>
            </div>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Publishing...' : 'Publish Campus Listing'}
            </button>
          </form>
        </div>
      )}

      {/* Active Job Openings Grid */}
      <div className="flex justify-between items-center" style={{ marginBottom: '16px' }}>
        <h3 style={{ margin: 0 }}>Active Job Postings ({jobs.length})</h3>
        <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Click "View Applicants" on any role to manage candidates</span>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(360px, 1fr))', gap: '24px' }}>
        {jobs.map(job => (
          <div key={job.id} className="glass-panel flex flex-col justify-between" style={{ transition: 'transform 0.2s ease' }}>
            <div>
              <div className="flex justify-between items-start" style={{ marginBottom: '8px' }}>
                <h3 style={{ margin: 0, fontSize: '1.25rem' }}>{job.role}</h3>
                <span className="badge badge-info" style={{ fontSize: '0.75rem' }}>ACTIVE</span>
              </div>
              
              <div className="flex items-center gap-4" style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginBottom: '12px' }}>
                <span className="flex items-center gap-1"><Building size={16} /> {job.companyName}</span>
                <span className="flex items-center gap-1"><MapPin size={16} /> {job.location}</span>
              </div>

              {job.salaryRange && (
                <div className="flex items-center gap-1" style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--ink-black)', marginBottom: '12px' }}>
                  <IndianRupee size={15} color="#16a34a" /> {formatSalaryInr(job.salaryRange)}
                </div>
              )}

              {job.requiredSkills && (
                <div className="flex gap-2 items-center" style={{ flexWrap: 'wrap', marginBottom: '12px' }}>
                  {job.requiredSkills.split(',').map((skill, idx) => (
                    <span key={idx} className="badge" style={{ fontSize: '0.75rem', background: 'white', padding: '2px 8px' }}>
                      {skill.trim()}
                    </span>
                  ))}
                </div>
              )}

              <p style={{ 
                margin: '0 0 16px 0', 
                fontSize: '0.92rem', 
                color: 'var(--text-secondary)',
                lineHeight: 1.5,
                display: '-webkit-box',
                WebkitLineClamp: 3,
                WebkitBoxOrient: 'vertical',
                overflow: 'hidden'
              }}>
                {job.description || 'No job description provided.'}
              </p>
            </div>

            <div style={{ borderTop: '2px dashed var(--ink-black)', paddingTop: '16px' }}>
              <button 
                className="btn btn-outline w-full flex items-center justify-center gap-2" 
                onClick={() => openApplicantsModal(job)}
                style={{ padding: '10px' }}
              >
                <Users size={18} />
                View & Manage Applicants
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* APPLICANTS MANAGEMENT MODAL (TRUE CENTERED OVERLAY) */}
      {selectedJob && (
        <div className="modal-backdrop">
          <div className="modal-content glass-panel" style={{
            background: 'var(--cream-bg)',
            width: '92%',
            maxWidth: '960px',
            maxHeight: '88vh',
            display: 'flex',
            flexDirection: 'column',
            padding: '28px',
            position: 'relative'
          }}>
            {/* Modal Header */}
            <div className="flex justify-between items-start" style={{ borderBottom: '2px solid var(--ink-black)', paddingBottom: '16px', marginBottom: '20px' }}>
              <div>
                <h3 style={{ margin: 0, fontSize: '1.4rem' }}>Applicants for {selectedJob.role}</h3>
                <span style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
                  {selectedJob.companyName} • {selectedJob.location} • Total Candidates: {applicants.length}
                </span>
              </div>
              <button 
                onClick={() => setSelectedJob(null)}
                style={{ background: 'none', border: 'none', cursor: 'pointer', padding: '4px' }}
              >
                <X size={24} />
              </button>
            </div>


            {/* Modal Body */}
            <div style={{ overflowY: 'auto', flex: 1, paddingRight: '4px' }}>
              {loadingApplicants ? (
                <div style={{ textAlign: 'center', padding: '40px' }}>Loading applicant records...</div>
              ) : applicants.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '48px 24px', color: 'var(--text-secondary)' }}>
                  <Users size={48} style={{ margin: '0 auto 12px auto', opacity: 0.4 }} />
                  <h4>No Applications Yet</h4>
                  <p>Students who apply through the Job Feed or the 1-Click Auto-Apply engine will appear here with live fit calculations.</p>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                  {applicants.map((app) => (
                    <div 
                      key={app.id} 
                      className="glass-panel" 
                      style={{ 
                        padding: '16px 20px', 
                        background: '#ffffff',
                        display: 'flex',
                        justifyContent: 'space-between',
                        alignItems: 'center',
                        flexWrap: 'wrap',
                        gap: '16px'
                      }}
                    >
                      {/* Candidate Info */}
                      <div style={{ minWidth: '220px', flex: 1 }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                          <span style={{ fontWeight: 800, fontSize: '1.05rem' }}>{app.studentName || 'Student Candidate'}</span>
                          {getStatusBadge(app.status)}
                        </div>
                        <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                          Email: {app.studentEmail || 'Registered Student'}
                        </div>
                        <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '2px' }}>
                          Applied: {app.appliedDate ? new Date(app.appliedDate).toLocaleDateString() : 'Recent'}
                        </div>
                      </div>

                      {/* AI Fit & Resume Snapshot */}
                      <div style={{ minWidth: '180px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
                          <span style={{ fontSize: '0.85rem', fontWeight: 600 }}>Fit Score:</span>
                          <span className="badge badge-success" style={{ fontWeight: 800 }}>
                            {app.jobFitScore != null ? `${app.jobFitScore.toFixed(1)}%` : 'Evaluated'}
                          </span>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                          <button 
                            className="btn btn-outline" 
                            style={{ padding: '4px 10px', fontSize: '0.8rem', display: 'flex', alignItems: 'center', gap: '4px' }}
                            onClick={() => handleDownloadResume(app.resumeVersionId, app.resumeName)}
                          >
                            <Download size={13} />
                            {app.resumeName ? `${app.resumeName} v${app.resumeVersion || 1}` : 'Snapshot Resume'}
                          </button>
                        </div>
                      </div>

                      {/* Recruiter Action Buttons */}
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
                        <button
                          className="btn btn-outline"
                          style={{ padding: '6px 12px', fontSize: '0.8rem', borderColor: '#3b82f6', color: '#1d4ed8' }}
                          onClick={() => handleUpdateStatus(app.id, 'UNDER_REVIEW')}
                        >
                          Review
                        </button>
                        <button
                          className="btn btn-outline"
                          style={{ padding: '6px 12px', fontSize: '0.8rem', borderColor: '#eab308', color: '#a16207' }}
                          onClick={() => handleUpdateStatus(app.id, 'INTERVIEW_SCHEDULED')}
                        >
                          Interview
                        </button>
                        <button
                          className="btn btn-primary"
                          style={{ padding: '6px 14px', fontSize: '0.8rem', background: '#22c55e', color: '#ffffff', borderColor: '#16a34a' }}
                          onClick={() => handleUpdateStatus(app.id, 'OFFERED')}
                        >
                          Offer
                        </button>
                        <button
                          className="btn btn-outline"
                          style={{ padding: '6px 12px', fontSize: '0.8rem', borderColor: '#ef4444', color: '#b91c1c' }}
                          onClick={() => handleUpdateStatus(app.id, 'REJECTED')}
                        >
                          Reject
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Modal Footer */}
            <div style={{ marginTop: '20px', paddingTop: '16px', borderTop: '2px solid var(--ink-black)', textAlign: 'right' }}>
              <button className="btn btn-outline" onClick={() => setSelectedJob(null)}>
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
