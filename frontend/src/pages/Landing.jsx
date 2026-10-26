import { Link } from 'react-router-dom';
import { Briefcase, FileText, CheckSquare, Sparkles, TrendingUp, Target } from 'lucide-react';

export default function Landing() {
  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '40px 20px' }}>
      {/* Hero Section */}
      <div className="flex flex-col items-center text-center" style={{ marginBottom: '64px', marginTop: '40px' }}>
        <div style={{ position: 'relative', display: 'inline-block' }}>
          <h1 style={{ fontSize: '4.5rem', marginBottom: '16px', zIndex: 2, position: 'relative' }}>
            Your <span style={{ color: 'var(--ink-blue)' }}>Career</span> Notebook
          </h1>
          <div style={{ 
            position: 'absolute', bottom: '25px', left: '-10px', right: '-10px', 
            height: '25px', background: 'var(--highlighter-yellow)', zIndex: 1, 
            transform: 'rotate(-2deg)' 
          }}></div>
        </div>
        
        <p style={{ fontSize: '1.5rem', maxWidth: '700px', margin: '0 auto 32px', color: 'var(--ink-black)', fontWeight: '600' }}>
          Stop losing track of which resume went where. CareerBridge is the ultimate operating system for your job hunt.
        </p>
        
        <div className="flex gap-4">
          <Link to="/register" className="btn btn-primary" style={{ fontSize: '1.8rem', padding: '16px 32px' }}>
            Start Building!
          </Link>
          <Link to="/login" className="btn btn-outline" style={{ fontSize: '1.8rem', padding: '16px 32px' }}>
            Login
          </Link>
        </div>
      </div>

      {/* Features Grid */}
      <h2 style={{ textAlign: 'center', fontSize: '3rem', marginBottom: '40px', width: '100%' }}>
        Everything You Need
      </h2>
      
      <div style={{ 
        display: 'grid', 
        gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', 
        gap: '32px' 
      }}>
        
        {/* Feature 1 */}
        <div className="glass-panel flex flex-col items-center text-center" style={{ transform: 'rotate(1deg)' }}>
          <FileText size={56} color="#a78bfa" className="floating-element" style={{ marginBottom: '16px' }} />
          <h3>Resume Vault</h3>
          <p style={{ fontSize: '1.1rem', marginTop: '8px' }}>
            Store endless versions of your resume tailored for different roles. Never send the wrong PDF again.
          </p>
        </div>

        {/* Feature 2 */}
        <div className="glass-panel flex flex-col items-center text-center" style={{ transform: 'rotate(-1deg)' }}>
          <Target size={56} color="#f9a8d4" className="floating-element" style={{ marginBottom: '16px' }} />
          <h3>Job Eligibility Engine</h3>
          <p style={{ fontSize: '1.1rem', marginTop: '8px' }}>
            Instantly check if you meet the cut-off for campus placements and open roles based on your CGPA and branch.
          </p>
        </div>

        {/* Feature 3 */}
        <div className="glass-panel flex flex-col items-center text-center" style={{ transform: 'rotate(2deg)' }}>
          <CheckSquare size={56} color="#86efac" className="floating-element" style={{ marginBottom: '16px' }} />
          <h3>Application Tracking</h3>
          <p style={{ fontSize: '1.1rem', marginTop: '8px' }}>
            Track every application from "Applied" to "Offer". See exactly which resume version you used for which company.
          </p>
        </div>

        {/* Feature 4 */}
        <div className="glass-panel flex flex-col items-center text-center" style={{ transform: 'rotate(-2deg)' }}>
          <Sparkles size={56} color="#fde047" className="floating-element" style={{ marginBottom: '16px' }} />
          <h3>AI Studio Hub</h3>
          <p style={{ fontSize: '1.1rem', marginTop: '8px' }}>
            A built-in sandbox designed for you to plug in your custom AI agents to generate tailored cover letters on the fly.
          </p>
        </div>

        {/* Feature 5 */}
        <div className="glass-panel flex flex-col items-center text-center" style={{ transform: 'rotate(1deg)' }}>
          <TrendingUp size={56} color="#2563eb" className="floating-element" style={{ marginBottom: '16px' }} />
          <h3>Fit Analytics</h3>
          <p style={{ fontSize: '1.1rem', marginTop: '8px' }}>
            Analyze your resume against job descriptions to get a compatibility score before you hit submit.
          </p>
        </div>

        {/* Feature 6 */}
        <div className="glass-panel flex flex-col items-center text-center" style={{ transform: 'rotate(-1deg)' }}>
          <Briefcase size={56} color="#fb923c" className="floating-element" style={{ marginBottom: '16px' }} />
          <h3>Recruiter Portal</h3>
          <p style={{ fontSize: '1.1rem', marginTop: '8px' }}>
            Dedicated workflows for recruiters to post jobs, filter candidates, and manage the campus hiring pipeline.
          </p>
        </div>

      </div>
    </div>
  );
}
