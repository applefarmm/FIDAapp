import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../../context/AppContext';
import BottomNav from '../../components/BottomNav';
import './Dashboard.css';

export default function Dashboard() {
  const { state } = useApp();
  const navigate = useNavigate();
  const avatar = state?.avatar || { name: 'Warrior', icon: '🦊', color: '#FF6B35' };
  const level  = state?.level  || 12;
  const xp     = state?.xp     || 7340;
  const xpNext = state?.xpNext || 8000;
  const streak = state?.streak || 7;

  return (
    <div className="ds-screen">
      <div className="ds-header">
        <div className="ds-avatar-wrap">
          <div className="ds-avatar-icon">{avatar.icon}</div>
          <div className="ds-avatar-info">
            <div className="ds-greeting">Hey, {avatar.name}! 👋</div>
            <div className="ds-level">Level {level}</div>
          </div>
        </div>
        <div className="ds-streak">🔥 {streak}-day streak</div>
      </div>
      <div className="ds-xp-row">
        <span className="ds-xp-label">XP: {xp.toLocaleString()} / {xpNext.toLocaleString()}</span>
        <div className="ds-xp-bar">
          <div className="ds-xp-fill" style={{ width: `${(xp / xpNext) * 100}%` }} />
        </div>
      </div>
      <div className="ds-quick-actions">
        {[
          { icon: '💪', label: 'Train',   path: '/workouts' },
          { icon: '⚔️',  label: 'Quests', path: '/quests' },
          { icon: '🏆', label: 'Ranks',   path: '/leaderboard' },
          { icon: '🎖️', label: 'Badges',  path: '/achievements' },
        ].map(a => (
          <button key={a.label} className="ds-action-btn" onClick={() => navigate(a.path)}>
            <span className="ds-action-icon">{a.icon}</span>
            <span className="ds-action-label">{a.label}</span>
          </button>
        ))}
      </div>
      <div className="ds-today-card">
        <div className="ds-today-title">Today's Quest 🎯</div>
        <div className="ds-today-desc">Complete Power HIIT – 350 XP awaits!</div>
        <button className="ds-today-btn" onClick={() => navigate('/workout-player')}>Let's Go ⚡</button>
      </div>
      <BottomNav active="home" onChange={path => navigate(`/${path}`)} />
    </div>
  );
}
