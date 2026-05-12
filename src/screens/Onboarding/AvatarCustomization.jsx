import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './AvatarCustomization.css';

const AVATARS = [
  { id: 'fox',   icon: '🦊', name: 'The Fox',   color: '#FF6B35' },
  { id: 'lion',  icon: '🦁', name: 'The Lion',  color: '#FFD700' },
  { id: 'tiger', icon: '🐯', name: 'The Tiger', color: '#FF4500' },
  { id: 'wolf',  icon: '🐺', name: 'The Wolf',  color: '#6C63FF' },
  { id: 'eagle', icon: '🦅', name: 'The Eagle', color: '#00B4D8' },
  { id: 'bear',  icon: '🐻', name: 'The Bear',  color: '#8B4513' },
];

export default function AvatarCustomization() {
  const navigate = useNavigate();
  const [selected, setSelected] = useState('fox');
  const av = AVATARS.find(a => a.id === selected);

  return (
    <div className="av-screen">
      <div className="av-header">
        <div className="av-title">Choose Your Avatar</div>
        <div className="av-sub">Pick your spirit animal</div>
      </div>
      <div className="av-preview" style={{ background: av.color }}>
        <div className="av-preview-icon">{av.icon}</div>
        <div className="av-preview-name">{av.name}</div>
      </div>
      <div className="av-grid">
        {AVATARS.map(a => (
          <button
            key={a.id}
            className={`av-option${selected === a.id ? ' av-option--active' : ''}`}
            style={{ '--av-color': a.color }}
            onClick={() => setSelected(a.id)}
          >
            {a.icon}
          </button>
        ))}
      </div>
      <button className="av-next" onClick={() => navigate('/fitness-level')}>Next →</button>
    </div>
  );
}
