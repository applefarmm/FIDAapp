import React, { useState } from 'react';
import BottomNav from '../../components/BottomNav';
import './Profile.css';

const ACHIEVEMENTS = [
  { icon: '🏆', label: 'Top 3',         unlocked: true  },
  { icon: '🔥', label: '7-Day Streak',  unlocked: true  },
  { icon: '💪', label: '50 Workouts',   unlocked: true  },
  { icon: '⚡', label: '10k XP',        unlocked: false },
  { icon: '🥇', label: 'First Place',   unlocked: false },
  { icon: '🚀', label: 'Level 20',      unlocked: false },
];

const STATS = [
  { label: 'Total Workouts', value: '47' },
  { label: 'Total XP',       value: '4,200' },
  { label: 'Best Streak',    value: '12 days' },
  { label: 'Global Rank',    value: '#3' },
];

const SETTINGS = [
  { icon: '🔔', label: 'Notifications' },
  { icon: '🎯', label: 'Goals'         },
  { icon: '🌙', label: 'Dark Mode'     },
  { icon: '❓', label: 'Help & FAQ'    },
  { icon: '🔒', label: 'Privacy'       },
];

export default function Profile({ onNavigate }) {
  const [tab, setTab] = useState('profile');

  return (
    <div className="pf-screen">
      <div className="pf-scroll">

        {/* Hero */}
        <div className="pf-hero">
          <div className="pf-avatar">🦁</div>
          <h1 className="pf-name">Alex the Bold</h1>
          <p className="pf-level">⚔️ Level 14  •  Rank #3</p>
          <button className="pf-edit-btn">Edit Profile</button>
        </div>

        {/* Stats Grid */}
        <div className="pf-stats">
          {STATS.map(s => (
            <div key={s.label} className="pf-stat">
              <span className="pf-stat-value">{s.value}</span>
              <span className="pf-stat-label">{s.label}</span>
            </div>
          ))}
        </div>

        {/* Achievements */}
        <div className="pf-section">
          <h2 className="pf-section-title">Achievements</h2>
          <div className="pf-badges">
            {ACHIEVEMENTS.map(a => (
              <div key={a.label} className={`pf-badge${a.unlocked ? '' : ' pf-badge--locked'}`}>
                <span className="pf-badge-icon">{a.icon}</span>
                <span className="pf-badge-label">{a.label}</span>
              </div>
            ))}
          </div>
        </div>

        {/* Settings */}
        <div className="pf-section">
          <h2 className="pf-section-title">Settings</h2>
          <div className="pf-settings">
            {SETTINGS.map(s => (
              <button key={s.label} className="pf-setting-row">
                <span className="pf-setting-icon">{s.icon}</span>
                <span className="pf-setting-label">{s.label}</span>
                <span className="pf-setting-arrow">›</span>
              </button>
            ))}
          </div>
        </div>

        {/* Logout */}
        <button className="pf-logout">Sign Out</button>

        <div style={{ height: 88 }} />
      </div>

      <BottomNav active={tab} onChange={id => { setTab(id); onNavigate?.(id); }} />
    </div>
  );
}
