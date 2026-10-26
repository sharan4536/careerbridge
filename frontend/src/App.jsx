import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Jobs from './pages/Jobs';
import Resumes from './pages/Resumes';
import Register from './pages/Register';
import AdminDashboard from './pages/AdminDashboard';
import RecruiterDashboard from './pages/RecruiterDashboard';
import Applications from './pages/Applications';
import Landing from './pages/Landing';

function App() {
  const token = localStorage.getItem('token');
  
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        
        <Route path="/" element={<Layout />}>
          <Route index element={<Landing />} />
          <Route path="admin" element={<AdminDashboard />} />
          <Route path="recruiter" element={<RecruiterDashboard />} />
          <Route path="dashboard" element={<Dashboard />} />
          <Route path="applications" element={<Applications />} />
          <Route path="jobs" element={<Jobs />} />
          <Route path="resumes" element={<Resumes />} />
        </Route>
      </Routes>
    </Router>
  );
}

export default App;
