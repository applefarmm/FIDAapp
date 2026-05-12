import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../../context/AppContext';
import BottomNav from '../../components/BottomNav';
import './Achievements.css';

const ACHIEVEMENTS = [
  { id: 1, icon: '🏆', title: 'First Quest', desc: 'Complete your first quest', earned: true },
  { id: 2, icon: '🔥', title: '5 Day Streak', desc: '5 consecutive workout days', earned: true },
  { id: 3, icon: '💪', title: 'Level 5', desc: 'Reach Level 5', earned: true },
  { id: 4, icon: '⚡', title: 'HIIT Hero', desc: 'Complete 10 HIIT sessions', earned: false },
  { id: 5, icon: '🧘', title: 'Zen Warrior', desc: '30 days of stretching', earned: false },
  { id: 6, icon: '🏅', title: 'Top 10', desc: 'Reach top 10 on leaderboard', earned: false },
];

export default function Achievements() {
  const navigate = useNavigate();
  const { profile } = useApp();

  const handleNav = (tab) => {
    const routes = { home: '/dashboard', quests: '/quests', workouts: '/workouts', leaderboard: '/leaderboard', profile: '/profile' };
    navigate(routes[tab] || '/dashboard');
  };

  return (
    <div className="ach-screen">
      <div className="ach-container">
        <div className="ach-header">
          <h1 className="ach-title">Achievements</h1>
          <span className="ach-count">{ACHIEVEMENTS.filter(a => a.earned).length}/{ACHIEVEMENTS.length}</span>
        </div>

        <div className="ach-grid">
          {ACHIEVEMENTS.map(a => (
            <div key={a.id} className={`ach-card${a.earned ? ' ach-card--earned' : ''}`}>
              <span className="ach-icon">{a.icon}</span>
              <span className="ach-card-title">{a.title}</span>
              <span className="ach-desc">{a.desc}</span>
              {a.earned && <span className="ach-badge">✓</span>}
            </div>
          ))}
        </div>

        <div style={{ height: 88 }} />
      </div>
      <BottomNav active="achievements" onChange={handleNav} />
    </div>
  );
}
