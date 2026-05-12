import React, { useState } from 'react';
import BottomNav from '../../components/BottomNav';
import './Home.css';

const DAILY_QUESTS = [
  { id: 1, icon: '🏃', title: 'Complete a Cardio Workout', xp: 200, done: false },
  { id: 2, icon: '💧', title: 'Drink 8 Glasses of Water',  xp: 50,  done: true  },
  { id: 3, icon: '🧘', title: 'Stretch for 10 Minutes',    xp: 80,  done: false },
];

const STATS = [
  { icon: '🔥', value: '7',   label: 'Day Streak' },
  { icon: '⚡', value: '4.2k', label: 'Weekly XP'  },
  { icon: '💪', value: '12',  label: 'Workouts'   },
];

export default function Home({ onNavigate }) {
  const [tab, setTab] = useState('home');
  const xpCurrent = 4200;
  const xpNext    = 5000;
  const pct       = Math.round((xpCurrent / xpNext) * 100);

  return (
    <div className="home-screen">
      <div className="home-scroll">

        {/* Header */}
        <div className="home-header">
          <div className="home-greeting">
            <span className="home-greeting-sub">Good morning 👋</span>
            <h1 className="home-greeting-name">Alex the Bold</h1>
          </div>
          <div className="home-avatar">🦁</div>
        </div>

        {/* XP Card */}
        <div className="home-xp-card">
          <div className="home-xp-top">
            <span className="home-level">⚔️ Level 14</span>
            <span className="home-xp-text">{xpCurrent.toLocaleString()} / {xpNext.toLocaleString()} XP</span>
          </div>
          <div className="home-xp-bar">
            <div className="home-xp-fill" style={{ width: `${pct}%` }} />
          </div>
          <p className="home-xp-hint">{xpNext - xpCurrent} XP until Level 15 🚀</p>
        </div>

        {/* Stats Row */}
        <div className="home-stats">
          {STATS.map(s => (
            <div key={s.label} className="home-stat">
              <span className="home-stat-icon">{s.icon}</span>
              <span className="home-stat-value">{s.value}</span>
              <span className="home-stat-label">{s.label}</span>
            </div>
          ))}
        </div>

        {/* Daily Quests */}
        <div className="home-section">
          <h2 className="home-section-title">Daily Quests</h2>
          {DAILY_QUESTS.map(q => (
            <div key={q.id} className={`home-quest${q.done ? ' home-quest--done' : ''}`}>
              <span className="home-quest-icon">{q.icon}</span>
              <div className="home-quest-info">
                <p className="home-quest-title">{q.title}</p>
                <p className="home-quest-xp">+{q.xp} XP</p>
              </div>
              <span className="home-quest-check">{q.done ? '✅' : '⭕'}</span>
            </div>
          ))}
        </div>

        {/* Start Workout CTA */}
        <button
          className="home-cta"
          onClick={() => { onNavigate?.('workouts'); }}
        >
          ⚡ Start Today's Workout
        </button>

        <div style={{ height: 88 }} />
      </div>

      <BottomNav active={tab} onChange={id => { setTab(id); onNavigate?.(id); }} />
    </div>
  );
}
