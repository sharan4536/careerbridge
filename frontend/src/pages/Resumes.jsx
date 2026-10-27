import { useEffect, useState } from 'react';
import api from '../api';
import { FileText, Upload, Sparkles, Download, CheckCircle, AlertTriangle, Lightbulb, Plus, X, Copy } from 'lucide-react';

export default function Resumes() {
  const [resumes, setResumes] = useState([]);
  const [newResumeName, setNewResumeName] = useState('');
  
  // AI Cover Letter Studio Modal
  const [showAIModal, setShowAIModal] = useState(false);
  const [jobDescription, setJobDescription] = useState('');
  const [targetRole, setTargetRole] = useState('');
  const [coverLetterOutput, setCoverLetterOutput] = useState('');
  const [aiGenerating, setAiGenerating] = useState(false);
  const [copied, setCopied] = useState(false);

  // AI Resume Audit Modal
  const [auditResult, setAuditResult] = useState(null);
  const [auditLoading, setAuditLoading] = useState(false);
  const [auditVersion, setAuditVersion] = useState(null);

  // Version Upload State
  const [activeResumeId, setActiveResumeId] = useState(null);
  const [versionData, setVersionData] = useState({
    targetRole: '',
    skillsRepresented: '',
    description: '',
    file: null
  });
  const [uploadLoading, setUploadLoading] = useState(false);

  useEffect(() => {
    fetchResumes();
  }, []);

  const fetchResumes = async () => {
    try {
      const res = await api.get('/resumes');
      setResumes(res.data);
    } catch (err) {
      console.error(err);
    }
  };

  const handleCreateContainer = async (e) => {
    e.preventDefault();
    if (!newResumeName.trim()) return;
    try {
      await api.post(`/resumes?name=${encodeURIComponent(newResumeName)}`);
      setNewResumeName('');
      fetchResumes();
    } catch (err) {
      alert('Failed to create container: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleVersionUpload = async (e, resumeId) => {
    e.preventDefault();
    if (!versionData.file) {
      alert('Please choose a PDF resume file to upload.');
      return;
    }
    setUploadLoading(true);
    const formData = new FormData();
    formData.append('targetRole', versionData.targetRole);
    formData.append('skillsRepresented', versionData.skillsRepresented);
    formData.append('description', versionData.description);
    formData.append('file', versionData.file);

    try {
      await api.post(`/resumes/${resumeId}/versions`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      });
      alert('Version uploaded and archived successfully!');
      setActiveResumeId(null);
      setVersionData({ targetRole: '', skillsRepresented: '', description: '', file: null });
      fetchResumes();
    } catch (err) {
      alert('Upload failed: ' + (err.response?.data || err.message));
    } finally {
      setUploadLoading(false);
    }
  };

  const handleDownload = async (versionId) => {
    try {
      const res = await api.get(`/resumes/versions/${versionId}/download`, { responseType: 'blob' });
      const url = window.URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `resume_v${versionId}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      alert('Failed to download resume file.');
    }
  };

  const handleAuditResume = async (version) => {
    setAuditVersion(version);
    setAuditResult(null);
    setAuditLoading(true);
    try {
      const res = await api.get(`/ai/resume/${version.id}/analyze`);
      setAuditResult(res.data);
    } catch (err) {
      alert('AI audit request failed.');
    } finally {
      setAuditLoading(false);
    }
  };

  const handleGenerateCoverLetter = async () => {
    if (!jobDescription.trim()) {
      alert('Please enter or paste the target job description.');
      return;
    }
    setAiGenerating(true);
    setCoverLetterOutput('');
    try {
      const res = await api.post('/ai/cover-letter', {
        jobDescription,
        targetRole
      });
      setCoverLetterOutput(res.data.coverLetter);
    } catch (err) {
      alert('Failed to generate cover letter: ' + (err.response?.data?.message || err.message));
    } finally {
      setAiGenerating(false);
    }
  };

  const copyToClipboard = () => {
    navigator.clipboard.writeText(coverLetterOutput);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div>
      <div className="flex justify-between items-center" style={{ marginBottom: '24px' }}>
        <div>
          <h2>Resume Vault</h2>
          <p style={{ color: 'var(--text-secondary)', marginTop: '4px' }}>
            Version-controlled resume archives with immutable submission snapshots
          </p>
        </div>
        <button 
          onClick={() => setShowAIModal(true)} 
          className="btn" 
          style={{ background: 'var(--highlighter-yellow)', border: '3px solid var(--ink-black)', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '1.1rem' }}>
          <Sparkles size={22} className="floating-element" />
          ✨ AI Studio & Cover Letters
        </button>
      </div>

      {/* Create New Container Form */}
      <div className="glass-panel" style={{ marginBottom: '32px', background: 'rgba(167, 139, 250, 0.08)' }}>
        <h3 style={{ margin: '0 0 12px 0' }}>Create New Resume Container</h3>
        <form onSubmit={handleCreateContainer} className="flex gap-4 items-center">
          <input 
            type="text" 
            placeholder="e.g. Distributed Systems & Cloud Resume" 
            className="form-input" 
            value={newResumeName}
            onChange={e => setNewResumeName(e.target.value)}
            required
            style={{ maxWidth: '450px', marginBottom: 0 }}
          />
          <button type="submit" className="btn btn-primary flex items-center gap-1">
            <Plus size={18} /> Create Vault
          </button>
        </form>
      </div>

      {/* Resume Containers List */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(380px, 1fr))', gap: '24px' }}>
        {resumes.map(resume => (
          <div key={resume.id} className="glass-panel flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-2">
                  <FileText color="#a78bfa" size={26} />
                  <h3 style={{ margin: 0, fontSize: '1.3rem' }}>{resume.name}</h3>
                </div>
                <span className="badge badge-info" style={{ fontSize: '0.8rem' }}>
                  {resume.versions?.length || 0} versions
                </span>
              </div>

              {/* Version History */}
              <div style={{ margin: '14px 0', borderTop: '1px solid var(--border-color)', paddingTop: '12px' }}>
                <h4 style={{ fontSize: '1rem', margin: '0 0 10px 0', color: 'var(--text-secondary)' }}>Archived Versions:</h4>
                {resume.versions && resume.versions.length > 0 ? (
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                    {resume.versions.map(v => (
                      <div key={v.id} style={{ background: 'var(--bg-paper)', border: '2px solid var(--ink-black)', borderRadius: '8px', padding: '10px 14px' }}>
                        <div className="flex justify-between items-center">
                          <strong>v{v.versionNumber} - {v.targetRole || 'General'}</strong>
                          <div className="flex gap-2">
                            <button 
                              onClick={() => handleAuditResume(v)} 
                              className="btn btn-outline" 
                              style={{ padding: '2px 8px', fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '4px' }}>
                              <Sparkles size={12} color="#7c3aed" /> AI Audit
                            </button>
                            <button 
                              onClick={() => handleDownload(v.id)} 
                              className="btn btn-outline" 
                              style={{ padding: '2px 8px', fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '4px' }}>
                              <Download size={12} /> PDF
                            </button>
                          </div>
                        </div>
                        <div style={{ fontSize: '0.85rem', color: '#64748b', marginTop: '4px' }}>
                          Skills: {v.skillsRepresented || 'None listed'}
                        </div>
                        <div style={{ fontSize: '0.8rem', color: '#94a3b8', marginTop: '2px' }}>
                          {v.description}
                        </div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p style={{ color: 'var(--text-secondary)', fontStyle: 'italic', fontSize: '0.9rem' }}>
                    No versions uploaded yet. Add details and upload a PDF below.
                  </p>
                )}
              </div>
            </div>

            {/* Upload New Version Form */}
            {activeResumeId === resume.id ? (
              <form onSubmit={(e) => handleVersionUpload(e, resume.id)} style={{ marginTop: '16px', borderTop: '2px dashed var(--ink-black)', paddingTop: '16px' }}>
                <h4 style={{ margin: '0 0 12px 0' }}>Upload New Version:</h4>
                <div className="form-group">
                  <label>Target Role / Focus</label>
                  <input type="text" className="form-input" required placeholder="e.g. Frontend Engineer"
                    value={versionData.targetRole} onChange={e => setVersionData({ ...versionData, targetRole: e.target.value })} />
                </div>
                <div className="form-group">
                  <label>Key Skills (Comma separated)</label>
                  <input type="text" className="form-input" required placeholder="e.g. React, Tailwind, TypeScript"
                    value={versionData.skillsRepresented} onChange={e => setVersionData({ ...versionData, skillsRepresented: e.target.value })} />
                </div>
                <div className="form-group">
                  <label>Version Description / Changes</label>
                  <input type="text" className="form-input" required placeholder="e.g. Updated with Q4 capstone project"
                    value={versionData.description} onChange={e => setVersionData({ ...versionData, description: e.target.value })} />
                </div>
                <div className="form-group">
                  <label>Upload PDF Document</label>
                  <input type="file" accept="application/pdf" required
                    onChange={e => setVersionData({ ...versionData, file: e.target.files[0] })} />
                </div>
                <div className="flex gap-2">
                  <button type="submit" className="btn btn-primary w-full" disabled={uploadLoading}>
                    {uploadLoading ? 'Uploading...' : 'Confirm Upload'}
                  </button>
                  <button type="button" onClick={() => setActiveResumeId(null)} className="btn btn-outline">
                    Cancel
                  </button>
                </div>
              </form>
            ) : (
              <button onClick={() => setActiveResumeId(resume.id)} className="btn btn-outline w-full flex items-center justify-center gap-2" style={{ marginTop: '16px' }}>
                <Upload size={18} /> Upload New Version (PDF)
              </button>
            )}
          </div>
        ))}
      </div>

      {/* AI Resume Audit Modal */}
      {auditVersion && (
        <div className="modal-backdrop">
          <div className="modal-content glass-panel" style={{ width: '90%', maxWidth: '600px' }}>
            <div className="flex justify-between items-center" style={{ marginBottom: '16px' }}>
              <div className="flex items-center gap-2">
                <Sparkles size={24} color="#7c3aed" />
                <h3 style={{ margin: 0 }}>AI Resume Audit: v{auditVersion.versionNumber} ({auditVersion.targetRole})</h3>
              </div>
              <button onClick={() => setAuditVersion(null)} className="btn btn-outline" style={{ padding: '4px' }}>
                <X size={20} />
              </button>
            </div>

            {auditLoading ? (
              <div style={{ textAlign: 'center', padding: '30px' }}>Analyzing resume structure and metrics...</div>
            ) : auditResult ? (
              <div>
                <div className="flex items-center gap-3" style={{ marginBottom: '16px', background: 'rgba(99, 102, 241, 0.1)', padding: '12px 16px', borderRadius: '8px' }}>
                  <div style={{ fontSize: '2rem', fontWeight: 'bold', color: 'var(--ink-blue)' }}>{auditResult.score}/100</div>
                  <div>
                    <strong>Overall ATS Score</strong>
                    <div style={{ fontSize: '0.85rem', color: '#64748b' }}>Benchmark against top engineering rubrics</div>
                  </div>
                </div>

                <div style={{ marginBottom: '12px' }}>
                  <h4 style={{ color: '#059669', marginBottom: '6px' }} className="flex items-center gap-1">
                    <CheckCircle size={16} /> Key Strengths:
                  </h4>
                  <ul style={{ paddingLeft: '20px', margin: 0, fontSize: '0.9rem' }}>
                    {auditResult.strengths?.map((s, i) => <li key={i}>{s}</li>)}
                  </ul>
                </div>

                <div style={{ marginBottom: '12px' }}>
                  <h4 style={{ color: '#d97706', marginBottom: '6px' }} className="flex items-center gap-1">
                    <AlertTriangle size={16} /> Areas for Improvement:
                  </h4>
                  <ul style={{ paddingLeft: '20px', margin: 0, fontSize: '0.9rem' }}>
                    {auditResult.weaknesses?.map((w, i) => <li key={i}>{w}</li>)}
                  </ul>
                </div>

                <div>
                  <h4 style={{ color: '#2563eb', marginBottom: '6px' }} className="flex items-center gap-1">
                    <Lightbulb size={16} /> Actionable Recommendations:
                  </h4>
                  <ul style={{ paddingLeft: '20px', margin: 0, fontSize: '0.9rem' }}>
                    {auditResult.suggestions?.map((rec, i) => <li key={i}>{rec}</li>)}
                  </ul>
                </div>
              </div>
            ) : null}

            <div className="flex justify-end mt-4">
              <button className="btn btn-outline" onClick={() => setAuditVersion(null)}>Close</button>
            </div>
          </div>
        </div>
      )}

      {/* AI Cover Letter Generator Studio Modal */}
      {showAIModal && (
        <div className="modal-backdrop">
          <div className="modal-content glass-panel" style={{ width: '90%', maxWidth: '780px', maxHeight: '90vh', overflowY: 'auto' }}>
            <div className="flex justify-between items-center mb-4">
              <div className="flex items-center gap-2">
                <Sparkles size={26} color="#d97706" />
                <h2 style={{ margin: 0 }}>✨ AI Cover Letter Generator Studio</h2>
              </div>
              <button onClick={() => setShowAIModal(false)} className="btn btn-outline" style={{ padding: '4px' }}>
                <X size={20} />
              </button>
            </div>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div className="form-group">
                <label>Target Job Role / Title</label>
                <input 
                  type="text" 
                  className="form-input" 
                  value={targetRole} 
                  onChange={e => setTargetRole(e.target.value)} 
                  placeholder="e.g. Full-Stack Engineer" 
                />
              </div>

              <div className="form-group">
                <label>Paste Job Description / Requirements</label>
                <textarea 
                  className="form-input" 
                  rows="4" 
                  placeholder="Paste the company's job description here..."
                  value={jobDescription}
                  onChange={e => setJobDescription(e.target.value)}
                ></textarea>
              </div>

              <button 
                className="btn btn-primary" 
                onClick={handleGenerateCoverLetter}
                disabled={aiGenerating}
                style={{ fontSize: '1.1rem', padding: '12px' }}
              >
                {aiGenerating ? 'Synthesizing Tailored Cover Letter...' : 'Generate Tailored Cover Letter'}
              </button>
              
              {coverLetterOutput && (
                <div className="form-group">
                  <div className="flex justify-between items-center mb-2">
                    <label style={{ margin: 0 }}>Generated Output:</label>
                    <button onClick={copyToClipboard} className="btn btn-outline flex items-center gap-1" style={{ padding: '4px 10px', fontSize: '0.85rem' }}>
                      <Copy size={14} /> {copied ? 'Copied!' : 'Copy to Clipboard'}
                    </button>
                  </div>
                  <textarea 
                    className="form-input" 
                    style={{ minHeight: '220px', fontFamily: 'monospace', fontSize: '0.9rem', lineHeight: '1.5' }}
                    value={coverLetterOutput}
                    readOnly
                  ></textarea>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
