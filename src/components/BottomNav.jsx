import React from 'react';
import './BottomNav.css';

const TABS = [
  { id: 'home', icon: '🏠', label: 'Home' },
  { id: 'workouts', icon: '💪', label: 'Workouts' },
  { id: 'quests', icon: '📋', label: 'Quests' },
  { id: 'leaderboard', icon: '🏆', label: 'Ranks' },
  { id: 'profile', icon: '👤', label: 'Profile' },
];

export default function BottomNav({ active, onChange }) {
  return (
    <nav className="bn-nav" aria-label="Main navigation">
      {TABS.map(tab => (
        <button
          key={tab.id}
          className={`bn-tab${active === tab.id ? ' bn-tab--active' : ''}`}
          onClick={() => onChange(tab.id)}
          aria-current={active === tab.id ? 'page' : undefined}
        >
          <span className="bn-icon">{tab.icon}</span>
          <span className="bn-label">{tab.label}</span>
        </button>
      ))}
    </nav>
  );
}
