import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { useApp } from '../../context/AppContext';
import './GoalSetter.css';

const GOALS = [
  {
    id: 'fitness',
    emoji: '🏃',
    label: 'Fitness',
    description: 'Run more, get stronger',
  },
  {
    id: 'hydration',
    emoji: '💧',
    label: 'Hydration',
    description: 'Drink more water daily',
  },
  {
    id: 'sleep',
    emoji: '😴',
    label: 'Sleep',
    description: 'Improve sleep habits',
  },
  {
    id: 'all-around',
    emoji: '⭐',
    label: 'All-around',
    description: 'Balance everything',
  },
];

export default function GoalSetter() {
  const navigate = useNavigate();
  const { updateProfile } = useApp();
  const [selected, setSelected] = useState(null);

  const handleContinue = () => {
    if (!selected) return;
    updateProfile({ goal: selected });
    navigate('/setup/avatar');
  };

  return (
    <div className="gs-screen">
      <div className="gs-container">
        {/* Progress dots */}
        <div className="gs-progress" aria-label="Step 2 of 3">
          <span className="gs-dot gs-dot--done" />
          <span className="gs-dot gs-dot--active" />
          <span className="gs-dot" />
        </div>

        {/* Back */}
        <button className="gs-back" onClick={() => navigate(-1)} aria-label="Go back">
          <ArrowLeft size={20} />
          Back
        </button>

        <div className="gs-header">
          <h1 className="gs-title">What's Your Primary Goal?</h1>
          <p className="gs-subtitle">Focus your journey on what matters most</p>
        </div>

        <div className="gs-grid">
          {GOALS.map((goal) => {
            const isSelected = selected === goal.id;
            return (
              <button
                key={goal.id}
                className={`gs-card${isSelected ? ' gs-card--selected' : ''}`}
                onClick={() => setSelected(goal.id)}
                aria-pressed={isSelected}
              >
                <span className="gs-card-emoji">{goal.emoji}</span>
                <span className="gs-card-label">{goal.label}</span>
                <span className="gs-card-desc">{goal.description}</span>
                {isSelected && <span className="gs-card-badge">✓</span>}
              </button>
            );
          })}
        </div>

        <button
          className="gs-continue"
          disabled={!selected}
          onClick={handleContinue}
        >
          Continue
        </button>
      </div>
    </div>
  );
}
