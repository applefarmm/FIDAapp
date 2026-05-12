import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../../context/AppContext';
import './Profile.css';

const STATS = [
  { label: 'Workouts', value: '47'   },
  { label: 'Streak',   value: '7d'   },
  { label: 'Level',    value: '12'   },
  { label: 'XP',       value: '7,340' },
];

export default function Profile() {
  const { state } = useApp();
  const navigate  = useNavigate();
  const avatar    = state?.avatar || { name: 'Warrior', icon: '🦊', color: '#FF6B35' };

  return (
    <div className="pf-screen">
      <div className="pf-header" style={{ background: avatar.color }}>
        <div className="pf-avatar-icon">{avatar.icon}</div>
        <div className="pf-name">{avatar.name}</div>
        <div className="pf-title">Fitness Warrior ⚔️</div>
      </div>
      <div className="pf-stats-row">
        {STATS.map(s => (
          <div key={s.label} className="pf-stat">
            <div className="pf-stat-val">{s.value}</div>
            <div className="pf-stat-label">{s.label}</div>
          </div>
        ))}
      </div>
      <div className="pf-menu">
        {[
          { icon: '🎖️', label: 'Achievements', path: '/achievements' },
          { icon: '⚙️', label: 'Settings',     path: '/settings'     },
          { icon: '🚪', label: 'Log Out',       path: '/welcome'      },
        ].map(item => (
          <button key={item.label} className="pf-menu-item" onClick={() => navigate(item.path)}>
            <span className="pf-menu-icon">{item.icon}</span>
            <span className="pf-menu-label">{item.label}</span>
            <span className="pf-menu-arrow">›</span>
          </button>
        ))}
      </div>
    </div>
  );
}
