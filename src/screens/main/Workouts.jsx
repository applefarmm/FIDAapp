import React from 'react';
import { useNavigate } from 'react-router-dom';
import './Workouts.css';

const WORKOUTS = [
  { id: 1, icon: '⚡', title: 'Power HIIT',     duration: '22 min', xp: 350, difficulty: 'Hard',   tags: ['Cardio', 'HIIT']      },
  { id: 2, icon: '🏋️', title: 'Upper Strength', duration: '35 min', xp: 280, difficulty: 'Medium', tags: ['Strength', 'Upper']   },
  { id: 3, icon: '🧘', title: 'Yoga Flow',       duration: '40 min', xp: 150, difficulty: 'Easy',   tags: ['Flexibility', 'Calm'] },
  { id: 4, icon: '🏃', title: 'Sprint Circuit',  duration: '18 min', xp: 320, difficulty: 'Hard',   tags: ['Cardio', 'Speed']     },
  { id: 5, icon: '🔥', title: 'Core Crusher',    duration: '25 min', xp: 240, difficulty: 'Medium', tags: ['Core', 'Strength']    },
];

function WorkoutCard({ w, onStart }) {
  return (
    <div className="wk-card">
      <div className="wk-card-icon">{w.icon}</div>
      <div className="wk-card-info">
        <div className="wk-card-title">{w.title}</div>
        <div className="wk-card-meta">
          {w.duration} · {w.xp} XP ·{' '}
          <span className={`wk-diff wk-diff--${w.difficulty.toLowerCase()}`}>{w.difficulty}</span>
        </div>
        <div className="wk-card-tags">
          {w.tags.map(t => <span key={t} className="wk-tag">{t}</span>)}
        </div>
      </div>
      <button className="wk-card-btn" onClick={onStart}>▶</button>
    </div>
  );
}

export default function Workouts() {
  const navigate = useNavigate();
  return (
    <div className="wk-screen">
      <div className="wk-header">
        <button className="wk-back" onClick={() => navigate(-1)}>← Back</button>
        <div className="wk-title">Workouts 💪</div>
      </div>
      <div className="wk-list">
        {WORKOUTS.map(w => (
          <WorkoutCard key={w.id} w={w} onStart={() => navigate('/workout-player')} />
        ))}
      </div>
    </div>
  );
}
