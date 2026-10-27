import { useEffect, useState, useMemo } from 'react';
import { Link } from 'react-router-dom';
import api from '../api';
import { 
  Building, MapPin, IndianRupee, X, Zap, Sparkles, CheckCircle2, 
  AlertCircle, HelpCircle, Search, Filter, RotateCcw, SlidersHorizontal, 
  Check, Users, PlusCircle, Award, Clock, Download, CheckCircle, XCircle 
} from 'lucide-react';

const formatSalaryInr = (salary) => {
  if (!salary) return '';
  return salary;
};

export default function Jobs() {
  const [jobs, setJobs] = useState([]);
  const [resumes, setResumes] = useState([]);
  const [eligibility, setEligibility] = useState({});
  const [jobFit, setJobFit] = useState({});
  const [applyingTo, setApplyingTo] = useState(null); // stores job object for student application
  const [selectedResumeId, setSelectedResumeId] = useState('');
  
  // Role check
  const role = localStorage.getItem('role') || '';
  const isRecruiter = role === 'ROLE_RECRUITER' || role === 'RECRUITER' || role === 'ROLE_ADMIN' || role === 'ADMIN';
  const isStudent = !isRecruiter;

  // Recruiter: Applicants modal state
  const [selectedJobForApplicants, setSelectedJobForApplicants] = useState(null);
  const [applicants, setApplicants] = useState([]);
  const [loadingApplicants, setLoadingApplicants] = useState(false);
  const [toastMessage, setToastMessage] = useState('');

  // Auto-Apply modal / state (Student only)
  const [showAutoApplyModal, setShowAutoApplyModal] = useState(false);
  const [autoApplyResumeId, setAutoApplyResumeId] = useState('');
  const [autoApplyResults, setAutoApplyResults] = useState(null);
  const [autoApplyLoading, setAutoApplyLoading] = useState(false);

  // AI Interview Prep modal
  const [prepJob, setPrepJob] = useState(null);
  const [prepQuestions, setPrepQuestions] = useState(null);
  const [prepLoading, setPrepLoading] = useState(false);

  // Search & Filter State
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCompany, setSelectedCompany] = useState('ALL');
  const [selectedLocation, setSelectedLocation] = useState('ALL');
  const [eligibilityFilter, setEligibilityFilter] = useState('ALL'); // 'ALL' | 'ELIGIBLE_ONLY'
  const [sortBy, setSortBy] = useState('DEFAULT'); // 'DEFAULT' | 'ROLE_ASC' | 'NEWEST'
  const [showFilters, setShowFilters] = useState(true);

  useEffect(() => {
    fetchJobs();
    if (isStudent) {
      fetchResumes();
    }
  }, [isStudent]);

  const fetchJobs = () => {
    api.get('/jobs').then(res => setJobs(res.data)).catch(console.error);
  };

  const fetchResumes = () => {
    api.get('/resumes').then(res => {
      const allVersions = [];
      if (Array.isArray(res.data)) {
        res.data.forEach(vault => {
          if (Array.isArray(vault.versions)) {
            vault.versions.forEach(v => {
              allVersions.push({
                id: v.id,
                name: `${vault.name} (v${v.versionNumber} - ${v.targetRole || 'General'})`,
              });
            });
          }
        });
      }
      setResumes(allVersions);
      if (allVersions.length > 0) {
        setSelectedResumeId(allVersions[0].id);
        setAutoApplyResumeId(allVersions[0].id);
      }
    }).catch(console.error);
  };

  const checkEligibility = async (id) => {
    try {
      const res = await api.post(`/jobs/${id}/eligibility-check`);
      setEligibility(prev => ({ ...prev, [id]: res.data }));
    } catch (err) {
      alert('Eligibility check failed: ' + (err.response?.data?.message || err.message));
    }
  };

  const checkJobFit = async (id) => {
    try {
      const res = await api.post(`/jobs/${id}/jobfit`);
      setJobFit(prev => ({ ...prev, [id]: res.data }));
    } catch (err) {
      alert('Fit calculation failed: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleApply = async () => {
    if (!selectedResumeId) {
      alert("Please select a resume version from your vault.");
      return;
    }
    try {
      await api.post('/applications', {
        jobId: applyingTo.id,
        resumeVersionId: selectedResumeId
      });
      alert(`Successfully applied to ${applyingTo.role} at ${applyingTo.companyName}!`);
      setApplyingTo(null);
    } catch (err) {
      alert('Application error: ' + (err.response?.data?.message || err.message));
    }
  };

  const [useAsync, setUseAsync] = useState(true);

  const handleRunAutoApply = async () => {
    if (!autoApplyResumeId) {
      alert("Please select a default resume version.");
      return;
    }
    setAutoApplyLoading(true);
    setAutoApplyResults(null);
    try {
      const endpoint = useAsync ? '/auto-apply/run-async' : '/auto-apply/run';
      const res = await api.post(`${endpoint}?defaultResumeVersionId=${autoApplyResumeId}`);
      setAutoApplyResults(res.data);
    } catch (err) {
      alert('Auto-apply failed: ' + (err.response?.data?.message || err.message));
    } finally {
      setAutoApplyLoading(false);
    }
  };

  const handleOpenInterviewPrep = async (job) => {
    setPrepJob(job);
    setPrepQuestions(null);
    setPrepLoading(true);
    try {
      const res = await api.get(`/ai/job/${job.id}/interview-prep`);
      setPrepQuestions(res.data);
    } catch (err) {
      alert('Failed to load interview prep questions.');
    } finally {
      setPrepLoading(false);
    }
  };

  // Recruiter: Open Applicants modal
  const openApplicantsModal = async (job) => {
    setSelectedJobForApplicants(job);
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
      setToastMessage(`Application #${appId} updated to ${newStatus}`);
      setTimeout(() => setToastMessage(''), 3500);
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

  // Derive unique companies and locations dynamically
  const uniqueCompanies = useMemo(() => {
    const set = new Set(jobs.map(j => j.companyName).filter(Boolean));
    return Array.from(set).sort();
  }, [jobs]);

  const uniqueLocations = useMemo(() => {
    const set = new Set(jobs.map(j => j.location).filter(Boolean));
    return Array.from(set).sort();
  }, [jobs]);

  const hasActiveFilters = searchTerm !== '' || selectedCompany !== 'ALL' || selectedLocation !== 'ALL' || (isStudent && eligibilityFilter !== 'ALL') || sortBy !== 'DEFAULT';

  const resetFilters = () => {
    setSearchTerm('');
    setSelectedCompany('ALL');
    setSelectedLocation('ALL');
    setEligibilityFilter('ALL');
    setSortBy('DEFAULT');
  };

  // Filter and sort jobs
  const filteredJobs = useMemo(() => {
    let result = jobs.filter(j => {
      // 1. Search term (role, company, location, description)
      const term = searchTerm.toLowerCase().trim();
      const matchesSearch = !term ||
        (j.role && j.role.toLowerCase().includes(term)) ||
        (j.companyName && j.companyName.toLowerCase().includes(term)) ||
        (j.location && j.location.toLowerCase().includes(term)) ||
        (j.description && j.description.toLowerCase().includes(term));

      // 2. Company filter
      const matchesCompany = selectedCompany === 'ALL' || j.companyName === selectedCompany;

      // 3. Location filter
      const matchesLocation = selectedLocation === 'ALL' || j.location === selectedLocation;

      // 4. Eligibility filter (Student only)
      const matchesEligibility = !isStudent || eligibilityFilter === 'ALL' || (eligibility[j.id]?.status === 'ELIGIBLE');

      return matchesSearch && matchesCompany && matchesLocation && matchesEligibility;
    });

    // Sorting
    if (sortBy === 'ROLE_ASC') {
      result = [...result].sort((a, b) => (a.role || '').localeCompare(b.role || ''));
    } else if (sortBy === 'NEWEST') {
      result = [...result].sort((a, b) => (b.id || 0) - (a.id || 0));
    }

    return result;
  }, [jobs, searchTerm, selectedCompany, selectedLocation, eligibilityFilter, sortBy, eligibility, isStudent]);

  return (
    <div>
      {/* Toast Notification */}
      {toastMessage && (
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
          gap: '8px'
        }}>
          <CheckCircle size={18} color="#86efac" />
          {toastMessage}
        </div>
      )}

      {/* Header Banner */}
      <div className="flex justify-between items-center" style={{ marginBottom: '24px', flexWrap: 'wrap', gap: '16px' }}>
        <div>
          <div className="flex items-center gap-2">
            <h2 style={{ margin: 0 }}>
              {isRecruiter ? 'Campus Job Postings' : 'Job Opportunities Feed'}
            </h2>
            {isRecruiter && (
              <span className="badge badge-success" style={{ fontSize: '0.8rem' }}>
                RECRUITER PORTAL VIEW
              </span>
            )}
          </div>
          <p style={{ color: 'var(--text-secondary)', margin: '4px 0 0 0' }}>
            {isRecruiter 
              ? 'Browse campus job openings and evaluate applicant pools directly'
              : 'Verified campus placements, engineering roles, and smart fit calculations'}
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button 
            onClick={() => setShowFilters(!showFilters)}
            className="btn btn-outline flex items-center gap-2"
            style={{ padding: '10px 16px' }}
          >
            <SlidersHorizontal size={18} />
            {showFilters ? 'Hide Filters' : 'Filter Options'}
          </button>

          {/* Student: Auto Apply Button | Recruiter: Manage Portal Button */}
          {isStudent ? (
            <button 
              onClick={() => setShowAutoApplyModal(true)} 
              className="btn" 
              style={{ 
                background: 'var(--highlighter-yellow)', 
                border: '3px solid var(--ink-black)', 
                display: 'flex', 
                alignItems: 'center', 
                gap: '8px', 
                fontSize: '1rem',
                fontWeight: 700 
              }}>
              <Zap size={20} color="#d97706" /> ⚡ 1-Click Auto-Apply Engine
            </button>
          ) : (
            <Link 
              to="/recruiter" 
              className="btn btn-primary flex items-center gap-2" 
              style={{ fontSize: '1rem' }}
            >
              <PlusCircle size={18} /> Recruiter Portal & Post Job
            </Link>
          )}
        </div>
      </div>

      {/* FILTER CONTROLS PANEL */}
      {showFilters && (
        <div className="glass-panel" style={{ marginBottom: '28px', background: 'rgba(255, 255, 255, 0.85)', border: '2px solid var(--ink-black)' }}>
          {/* Main Search Row */}
          <div className="flex items-center gap-3" style={{ borderBottom: '1px solid #e2e8f0', paddingBottom: '16px', marginBottom: '16px' }}>
            <Search size={22} color="var(--text-secondary)" />
            <input 
              type="text" 
              className="form-input" 
              style={{ margin: 0, border: 'none', background: 'transparent', fontSize: '1.05rem', flex: 1 }} 
              placeholder="Search by role title, company name, tech skills, or location..."
              value={searchTerm}
              onChange={e => setSearchTerm(e.target.value)}
            />
            {searchTerm && (
              <button 
                onClick={() => setSearchTerm('')} 
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-secondary)' }}
              >
                <X size={18} />
              </button>
            )}
          </div>

          {/* Granular Filters Grid */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '16px', alignItems: 'flex-end' }}>
            {/* Company Dropdown */}
            <div>
              <label style={{ fontSize: '0.85rem', fontWeight: 700, color: '#475569', display: 'block', marginBottom: '6px' }}>
                <Building size={14} style={{ display: 'inline', marginRight: '4px' }} />
                Company
              </label>
              <select 
                className="form-input" 
                style={{ margin: 0, padding: '8px 12px', fontSize: '0.9rem' }}
                value={selectedCompany}
                onChange={e => setSelectedCompany(e.target.value)}
              >
                <option value="ALL">All Companies ({uniqueCompanies.length})</option>
                {uniqueCompanies.map(comp => (
                  <option key={comp} value={comp}>{comp}</option>
                ))}
              </select>
            </div>

            {/* Location Dropdown */}
            <div>
              <label style={{ fontSize: '0.85rem', fontWeight: 700, color: '#475569', display: 'block', marginBottom: '6px' }}>
                <MapPin size={14} style={{ display: 'inline', marginRight: '4px' }} />
                Location
              </label>
              <select 
                className="form-input" 
                style={{ margin: 0, padding: '8px 12px', fontSize: '0.9rem' }}
                value={selectedLocation}
                onChange={e => setSelectedLocation(e.target.value)}
              >
                <option value="ALL">All Locations ({uniqueLocations.length})</option>
                {uniqueLocations.map(loc => (
                  <option key={loc} value={loc}>{loc}</option>
                ))}
              </select>
            </div>

            {/* Eligibility Filter (Students only) */}
            {isStudent && (
              <div>
                <label style={{ fontSize: '0.85rem', fontWeight: 700, color: '#475569', display: 'block', marginBottom: '6px' }}>
                  <CheckCircle2 size={14} style={{ display: 'inline', marginRight: '4px' }} />
                  Eligibility
                </label>
                <select 
                  className="form-input" 
                  style={{ margin: 0, padding: '8px 12px', fontSize: '0.9rem' }}
                  value={eligibilityFilter}
                  onChange={e => setEligibilityFilter(e.target.value)}
                >
                  <option value="ALL">All Listings</option>
                  <option value="ELIGIBLE_ONLY">Only Verified Eligible (Checked)</option>
                </select>
              </div>
            )}

            {/* Sort Order */}
            <div>
              <label style={{ fontSize: '0.85rem', fontWeight: 700, color: '#475569', display: 'block', marginBottom: '6px' }}>
                <Filter size={14} style={{ display: 'inline', marginRight: '4px' }} />
                Sort By
              </label>
              <select 
                className="form-input" 
                style={{ margin: 0, padding: '8px 12px', fontSize: '0.9rem' }}
                value={sortBy}
                onChange={e => setSortBy(e.target.value)}
              >
                <option value="DEFAULT">Default / Recommended</option>
                <option value="NEWEST">Newest Job Openings</option>
                <option value="ROLE_ASC">Role Title (A to Z)</option>
              </select>
            </div>

            {/* Reset Button */}
            <div>
              <button 
                onClick={resetFilters}
                className="btn btn-outline flex items-center justify-center gap-1 w-full"
                disabled={!hasActiveFilters}
                style={{ 
                  padding: '9px 12px', 
                  fontSize: '0.85rem',
                  opacity: hasActiveFilters ? 1 : 0.5,
                  cursor: hasActiveFilters ? 'pointer' : 'not-allowed'
                }}
              >
                <RotateCcw size={15} />
                Reset Filters
              </button>
            </div>
          </div>

          {/* Filter Status Summary Bar */}
          <div className="flex justify-between items-center" style={{ marginTop: '16px', paddingTop: '12px', borderTop: '1px dashed #cbd5e1', fontSize: '0.85rem' }}>
            <span style={{ color: 'var(--text-secondary)' }}>
              Showing <strong style={{ color: 'var(--ink-black)' }}>{filteredJobs.length}</strong> of <strong style={{ color: 'var(--ink-black)' }}>{jobs.length}</strong> active opportunities
            </span>
            {hasActiveFilters && (
              <span className="badge badge-info" style={{ fontSize: '0.75rem' }}>
                Filters Applied
              </span>
            )}
          </div>
        </div>
      )}

      {/* JOBS LISTING */}
      {filteredJobs.length === 0 ? (
        <div className="glass-panel" style={{ textAlign: 'center', padding: '60px 20px', color: 'var(--text-secondary)' }}>
          <Search size={48} style={{ margin: '0 auto 16px auto', opacity: 0.3 }} />
          <h3>No Job Opportunities Match Your Filters</h3>
          <p style={{ maxWidth: '450px', margin: '0 auto 20px auto' }}>
            Try clearing or adjusting your search keywords, selected company, or location filters.
          </p>
          <button className="btn btn-primary" onClick={resetFilters}>
            Reset All Filters
          </button>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {filteredJobs.map(job => (
            <div key={job.id} className="glass-panel">
              <div className="flex justify-between items-center" style={{ marginBottom: '16px', flexWrap: 'wrap', gap: '12px' }}>
                <div>
                  <h3 style={{ margin: '0 0 8px 0', fontSize: '1.4rem' }}>{job.role}</h3>
                  <div className="flex items-center gap-4" style={{ color: 'var(--text-secondary)', fontSize: '0.95rem', flexWrap: 'wrap' }}>
                    <span className="flex items-center gap-2"><Building size={16} /> <strong>{job.companyName}</strong></span>
                    <span className="flex items-center gap-2"><MapPin size={16} /> {job.location}</span>
                    <span className="flex items-center gap-1"><IndianRupee size={15} color="#16a34a" /> <strong>{formatSalaryInr(job.salaryRange)}</strong></span>
                  </div>
                </div>

                {/* ROLE-AWARE ACTION BUTTONS */}
                <div className="flex gap-2">
                  <button 
                    className="btn btn-outline flex items-center gap-1" 
                    onClick={() => handleOpenInterviewPrep(job)} 
                    style={{ padding: '8px 14px', fontSize: '0.9rem' }}
                  >
                    <Sparkles size={16} color="#7c3aed" /> Prep AI
                  </button>

                  {/* Student sees "Apply Now" | Recruiter sees "View Applicants" */}
                  {isStudent ? (
                    <button 
                      className="btn btn-primary" 
                      onClick={() => setApplyingTo(job)} 
                      style={{ padding: '8px 18px', fontSize: '1rem' }}
                    >
                      Apply Now
                    </button>
                  ) : (
                    <button 
                      className="btn btn-primary flex items-center gap-2" 
                      onClick={() => openApplicantsModal(job)} 
                      style={{ padding: '8px 18px', fontSize: '1rem', background: '#3b82f6', borderColor: '#2563eb' }}
                    >
                      <Users size={18} /> View Applicants
                    </button>
                  )}
                </div>
              </div>

              {job.requiredSkills && (
                <div className="flex gap-2 items-center" style={{ flexWrap: 'wrap', marginBottom: '12px' }}>
                  <span style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)' }}>Required:</span>
                  {job.requiredSkills.split(',').map((skill, idx) => (
                    <span key={idx} className="badge" style={{ fontSize: '0.75rem', background: 'white', padding: '2px 8px' }}>
                      {skill.trim()}
                    </span>
                  ))}
                  {job.minCgpa && (
                    <span className="badge badge-warning" style={{ fontSize: '0.75rem', padding: '2px 8px' }}>
                      Min CGPA: {job.minCgpa}
                    </span>
                  )}
                  {job.graduationYear && (
                    <span className="badge badge-info" style={{ fontSize: '0.75rem', padding: '2px 8px' }}>
                      Batch: {job.graduationYear}
                    </span>
                  )}
                </div>
              )}

              <p style={{ margin: '0 0 16px 0', color: '#475569', fontSize: '1rem', lineHeight: 1.5 }}>
                {job.description || 'No job description provided.'}
              </p>
              
              {/* ACTION / TELEMETRY TOOLBAR */}
              <div className="flex justify-between items-center" style={{ borderTop: '1px solid var(--border-color)', paddingTop: '16px', flexWrap: 'wrap', gap: '12px' }}>
                {isStudent ? (
                  // Student: Eligibility and Job Fit checks
                  <div className="flex gap-4 items-center" style={{ flexWrap: 'wrap' }}>
                    <div>
                      <button onClick={() => checkEligibility(job.id)} className="btn btn-outline" style={{ padding: '6px 12px', fontSize: '0.85rem' }}>
                        Check Eligibility
                      </button>
                      {eligibility[job.id] && (
                        <span className={`badge ${eligibility[job.id].status === 'ELIGIBLE' ? 'badge-success' : 'badge-danger'}`} style={{ marginLeft: '10px' }}>
                          {eligibility[job.id].status === 'ELIGIBLE' ? '✓ Eligible' : '✗ Requirements Not Met'}
                        </span>
                      )}
                    </div>

                    <div>
                      <button onClick={() => checkJobFit(job.id)} className="btn btn-outline" style={{ padding: '6px 12px', fontSize: '0.85rem' }}>
                        Analyze Fit
                      </button>
                      {jobFit[job.id] && (
                        <span className="badge badge-info" style={{ marginLeft: '10px' }}>
                          ★ {jobFit[job.id].overallScore}% Match
                        </span>
                      )}
                    </div>
                  </div>
                ) : (
                  // Recruiter: Management Telemetry Info
                  <div className="flex items-center gap-3">
                    <span className="badge badge-success" style={{ fontSize: '0.75rem' }}>OPEN FOR APPLICANTS</span>
                    <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                      Posting #{job.id} • Click "View Applicants" to manage candidate pipeline
                    </span>
                  </div>
                )}

                {isStudent && eligibility[job.id]?.reasons?.length > 0 && (
                  <div style={{ fontSize: '0.85rem', color: '#475569', maxWidth: '350px', textAlign: 'right' }}>
                    {eligibility[job.id].reasons.join(', ')}
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* STUDENT ONLY: Apply to Job Modal */}
      {isStudent && applyingTo && (
        <div className="modal-backdrop">
          <div className="modal-content glass-panel" style={{ width: '90%', maxWidth: '460px' }}>
            <div className="flex justify-between items-center" style={{ marginBottom: '20px' }}>
              <div>
                <h2 style={{ margin: 0 }}>Apply to Job</h2>
                <span style={{ color: 'var(--text-secondary)' }}>{applyingTo.role} at {applyingTo.companyName}</span>
              </div>
              <button onClick={() => setApplyingTo(null)} className="btn btn-outline" style={{ padding: '4px' }}>
                <X size={20} />
              </button>
            </div>

            {resumes.length === 0 ? (
              <div style={{ padding: '16px', background: 'rgba(239, 68, 68, 0.1)', borderRadius: '8px', marginBottom: '16px' }}>
                <p style={{ margin: 0 }}>You don't have any resume versions in your vault yet.</p>
                <Link to="/resumes" className="btn btn-outline" style={{ marginTop: '10px', display: 'inline-block' }}>Go to Resume Vault</Link>
              </div>
            ) : (
              <div className="form-group">
                <label>Select Resume Version from Vault</label>
                <select 
                  className="form-input" 
                  value={selectedResumeId} 
                  onChange={(e) => setSelectedResumeId(e.target.value)}
                >
                  {resumes.map(r => (
                    <option key={r.id} value={r.id}>{r.name}</option>
                  ))}
                </select>
                <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                  A permanent snapshot of this resume will be archived with your application.
                </span>
              </div>
            )}

            <div className="flex justify-end gap-2 mt-4">
              <button className="btn btn-outline" onClick={() => setApplyingTo(null)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleApply} disabled={resumes.length === 0}>
                Confirm & Submit Application
              </button>
            </div>
          </div>
        </div>
      )}

      {/* RECRUITER ONLY: Applicants Management Modal */}
      {isRecruiter && selectedJobForApplicants && (
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
                <h3 style={{ margin: 0, fontSize: '1.4rem' }}>Applicants for {selectedJobForApplicants.role}</h3>
                <span style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
                  {selectedJobForApplicants.companyName} • {selectedJobForApplicants.location} • Total Candidates: {applicants.length}
                </span>
              </div>
              <button 
                onClick={() => setSelectedJobForApplicants(null)}
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
              <button className="btn btn-outline" onClick={() => setSelectedJobForApplicants(null)}>
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* STUDENT ONLY: Auto-Apply Modal */}
      {isStudent && showAutoApplyModal && (
        <div className="modal-backdrop">
          <div className="modal-content glass-panel" style={{ width: '90%', maxWidth: '560px' }}>
            <div className="flex justify-between items-center" style={{ marginBottom: '16px' }}>
              <div className="flex items-center gap-2">
                <Zap size={24} color="#d97706" />
                <h2 style={{ margin: 0 }}>1-Click Auto-Apply Engine</h2>
              </div>
              <button onClick={() => setShowAutoApplyModal(false)} className="btn btn-outline" style={{ padding: '4px' }}>
                <X size={20} />
              </button>
            </div>

            <p style={{ fontSize: '0.95rem', color: 'var(--text-secondary)', marginBottom: '16px' }}>
              CareerBridge will scan every active job listing, run real-time eligibility checks (CGPA, branch, backlogs), and automatically submit applications for all positions where you meet the criteria!
            </p>

            <div className="form-group">
              <label>Default Resume Version</label>
              <select 
                className="form-input" 
                value={autoApplyResumeId} 
                onChange={(e) => setAutoApplyResumeId(e.target.value)}
              >
                {resumes.map(r => (
                  <option key={r.id} value={r.id}>{r.name}</option>
                ))}
              </select>
            </div>

            <div className="form-group" style={{ marginBottom: '14px' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', fontSize: '0.85rem', color: '#1e293b' }}>
                <input 
                  type="checkbox" 
                  checked={useAsync} 
                  onChange={(e) => setUseAsync(e.target.checked)} 
                />
                <span><strong>Multithreaded Concurrent Execution (CompletableFuture)</strong></span>
              </label>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginLeft: '24px', display: 'block' }}>
                Executes eligibility verification on background worker threads managed by Spring's taskExecutor.
              </span>
            </div>

            <button 
              className="btn btn-primary w-full" 
              onClick={handleRunAutoApply} 
              disabled={autoApplyLoading || resumes.length === 0}
              style={{ fontSize: '1.1rem', padding: '12px' }}
            >
              {autoApplyLoading ? 'Running Eligibility & Applying...' : (useAsync ? '⚡ Run Multithreaded Auto-Apply' : '⚡ Run Auto-Apply Now')}
            </button>

            {autoApplyResults && (
              <div style={{ marginTop: '20px', maxHeight: '200px', overflowY: 'auto', border: '2px dashed var(--ink-black)', padding: '12px', borderRadius: '8px' }}>
                <h4 style={{ margin: '0 0 8px 0' }}>Results Summary:</h4>
                <ul style={{ paddingLeft: '18px', margin: 0, fontSize: '0.9rem' }}>
                  {autoApplyResults.map((msg, i) => (
                    <li key={i} style={{ marginBottom: '4px', color: msg.includes('Successfully') ? '#059669' : '#64748b' }}>
                      {msg}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        </div>
      )}

      {/* AI Interview Prep Modal */}
      {prepJob && (
        <div className="modal-backdrop">
          <div className="modal-content glass-panel" style={{ width: '90%', maxWidth: '640px' }}>
            <div className="flex justify-between items-center" style={{ marginBottom: '16px' }}>
              <div className="flex items-center gap-2">
                <Sparkles size={24} color="#7c3aed" />
                <h2 style={{ margin: 0 }}>AI Interview Prep: {prepJob.role}</h2>
              </div>
              <button onClick={() => setPrepJob(null)} className="btn btn-outline" style={{ padding: '4px' }}>
                <X size={20} />
              </button>
            </div>

            {prepLoading ? (
              <div style={{ textAlign: 'center', padding: '30px' }}>Generating tailored interview questions...</div>
            ) : prepQuestions ? (
              <div>
                <div style={{ marginBottom: '16px' }}>
                  <h4 style={{ color: '#2563eb', marginBottom: '8px' }}>🛠️ Technical Assessment Questions:</h4>
                  <ul style={{ paddingLeft: '20px', fontSize: '0.95rem' }}>
                    {prepQuestions.technical?.map((q, idx) => (
                      <li key={idx} style={{ marginBottom: '6px' }}>{q}</li>
                    ))}
                  </ul>
                </div>
                <div>
                  <h4 style={{ color: '#059669', marginBottom: '8px' }}>🎯 Behavioral & Culture Questions:</h4>
                  <ul style={{ paddingLeft: '20px', fontSize: '0.95rem' }}>
                    {prepQuestions.behavioral?.map((q, idx) => (
                      <li key={idx} style={{ marginBottom: '6px' }}>{q}</li>
                    ))}
                  </ul>
                </div>
              </div>
            ) : null}

            <div className="flex justify-end mt-4">
              <button className="btn btn-outline" onClick={() => setPrepJob(null)}>Close</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
