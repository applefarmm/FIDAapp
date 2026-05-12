import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './FitnessLevel.css';

const LEVELS = [
  { id: 'beginner',     icon: '🌱', label: 'Beginner',     desc: 'New to working out'     },
  { id: 'intermediate', icon: '⚡', label: 'Intermediate', desc: '1–3 years of training'  },
  { id: 'advanced',     icon: '🔥', label: 'Advanced',     desc: 'Serious athlete'         },
  { id: 'elite',        icon: '👑', label: 'Elite',        desc: 'Professional level'      },
];

export default function FitnessLevel() {
  const navigate = useNavigate();
  const [selected, setSelected] = useState('intermediate');
  return (
    <div className="fl-screen">
      <div className="fl-header">
        <div className="fl-title">Your Fitness Level</div>
        <div className="fl-sub">We'll tailor your experience</div>
      </div>
      <div className="fl-list">
        {LEVELS.map(l => (
          <button
            key={l.id}
            className={`fl-item${selected === l.id ? ' fl-item--active' : ''}`}
            onClick={() => setSelected(l.id)}
          >
            <div className="fl-item-icon">{l.icon}</div>
            <div className="fl-item-body">
              <div className="fl-item-label">{l.label}</div>
              <div className="fl-item-desc">{l.desc}</div>
            </div>
            {selected === l.id && <div className="fl-item-check">✓</div>}
          </button>
        ))}
      </div>
      <button className="fl-next" onClick={() => navigate('/dashboard')}>Start Training 🚀</button>
    </div>
  );
}
