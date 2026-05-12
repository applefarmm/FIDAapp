import React, { useState } from 'react';
import BottomNav from '../../components/BottomNav';
import './Workouts.css';

const CATEGORIES = [
  { id: 'strength', icon: '🏋️', label: 'Strength',    color: '#e74c3c' },
  { id: 'cardio',   icon: '🏃', label: 'Cardio',      color: '#3498db' },
  { id: 'hiit',     icon: '⚡', label: 'HIIT',         color: '#f5a623' },
  { id: 'yoga',     icon: '🧘', label: 'Yoga',         color: '#9b59b6' },
  { id: 'cycling',  icon: '🚴', label: 'Cycling',     color: '#27ae60' },
  { id: 'boxing',   icon: '🥊', label: 'Boxing',      color: '#e67e22' },
];

const WORKOUTS = [
  { id: 1, name: 'Full Body Blast',    cat: 'hiit',     duration: '25 min', xp: 300, difficulty: 'Hard',   icon: '⚡' },
  { id: 2, name: 'Upper Body Power',   cat: 'strength', duration: '40 min', xp: 350, difficulty: 'Medium', icon: '💪' },
  { id: 3, name: 'Morning Run 5K',     cat: 'cardio',   duration: '30 min', xp: 250, difficulty: 'Easy',   icon: '🏃' },
  { id: 4, name: 'Core Crusher',       cat: 'strength', duration: '20 min', xp: 200, difficulty: 'Hard',   icon: '🔥' },
  { id: 5, name: 'Yoga Flow',          cat: 'yoga',     duration: '35 min', xp: 150, difficulty: 'Easy',   icon: '🧘' },
  { id: 6, name: 'Sprint Intervals',   cat: 'hiit',     duration: '15 min', xp: 280, difficulty: 'Hard',   icon: '⚡' },
];

const DIFF_COLOR = { Easy: '#27ae60', Medium: '#f5a623', Hard: '#e74c3c' };

export default function Workouts({ onNavigate }) {
  const [tab, setTab]         = useState('workouts');
  const [active, setActive]   = useState('all');

  const filtered = active === 'all'
    ? WORKOUTS
    : WORKOUTS.filter(w => w.cat === active);

  return (
    <div className="wk-screen">
      <div className="wk-scroll">

        {/* Header */}
        <div className="wk-header">
          <h1 className="wk-title">Workouts</h1>
          <p className="wk-subtitle">Choose your battle ⚔️</p>
        </div>

        {/* Featured */}
        <div className="wk-featured">
          <div className="wk-featured-badge">🔥 Featured</div>
          <h2 className="wk-featured-name">Full Body Blast</h2>
          <p className="wk-featured-meta">25 min  •  HIIT  •  +300 XP</p>
          <button className="wk-featured-btn">Start Now ⚡</button>
        </div>

        {/* Categories */}
        <div className="wk-cats">
          <button
            className={`wk-cat${active === 'all' ? ' wk-cat--active' : ''}`}
            onClick={() => setActive('all')}
          >
            All
          </button>
          {CATEGORIES.map(c => (
            <button
              key={c.id}
              className={`wk-cat${active === c.id ? ' wk-cat--active' : ''}`}
              style={active === c.id ? { background: c.color } : {}}
              onClick={() => setActive(c.id)}
            >
              {c.icon} {c.label}
            </button>
          ))}
        </div>

        {/* List */}
        <div className="wk-list">
          {filtered.map(w => (
            <div key={w.id} className="wk-card">
              <div className="wk-card-icon">{w.icon}</div>
              <div className="wk-card-info">
                <p className="wk-card-name">{w.name}</p>
                <p className="wk-card-meta">{w.duration}  •  +{w.xp} XP</p>
              </div>
              <span
                className="wk-card-diff"
                style={{ color: DIFF_COLOR[w.difficulty] }}
              >
                {w.difficulty}
              </span>
            </div>
          ))}
        </div>

        <div style={{ height: 88 }} />
      </div>

      <BottomNav active={tab} onChange={id => { setTab(id); onNavigate?.(id); }} />
    </div>
  );
}
