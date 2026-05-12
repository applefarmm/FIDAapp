import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './GoalSetter.css';

const GOALS = [
  { id: 'lose',   icon: '🔥', label: 'Lose Weight',  desc: 'Burn fat, get lean'         },
  { id: 'muscle', icon: '💪', label: 'Build Muscle', desc: 'Gain strength & size'        },
  { id: 'endure', icon: '🏃', label: 'Endurance',    desc: 'Run farther, last longer'    },
  { id: 'flex',   icon: '🧘', label: 'Flexibility',  desc: 'Move with ease & grace'      },
  { id: 'health', icon: '❤️', label: 'Stay Healthy', desc: 'Balance body & mind'         },
];

export default function GoalSetter() {
  const navigate = useNavigate();
  const [selected, setSelected] = useState(null);
  return (
    <div className="gs-screen">
      <div className="gs-header">
        <div className="gs-title">What's Your Goal?</div>
        <div className="gs-sub">Pick one to begin your journey</div>
      </div>
      <div className="gs-grid">
        {GOALS.map(g => (
          <button
            key={g.id}
            className={`gs-card${selected === g.id ? ' gs-card--active' : ''}`}
            onClick={() => setSelected(g.id)}
          >
            <div className="gs-card-icon">{g.icon}</div>
            <div className="gs-card-label">{g.label}</div>
            <div className="gs-card-desc">{g.desc}</div>
          </button>
        ))}
      </div>
      <button
        className={`gs-next${selected ? ' gs-next--active' : ''}`}
        disabled={!selected}
        onClick={() => navigate('/avatar')}
      >
        Next →
      </button>
    </div>
  );
}
