import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ArrowLeft, Rocket } from 'lucide-react';
import { useApp } from '../../context/AppContext';
import './AvatarCustomization.css';

const AVATARS = [
  { id: 'hero-m',   emoji: '🦸‍♂️' },
  { id: 'hero-f',   emoji: '🦸‍♀️' },
  { id: 'wizard-m', emoji: '🧙‍♂️' },
  { id: 'wizard-f', emoji: '🧙‍♀️' },
  { id: 'villain-m',emoji: '🦹‍♂️' },
  { id: 'villain-f',emoji: '🦹‍♀️' },
  { id: 'elf-m',    emoji: '🧝‍♂️' },
  { id: 'elf-f',    emoji: '🧝‍♀️' },
];

const COLORS = [
  '#6C63FF',
  '#FF6B6B',
  '#4ECDC4',
  '#FFD93D',
  '#6BCB77',
  '#FF9F43',
];

export default function AvatarCustomization() {
  const navigate = useNavigate();
  const { updateProfile } = useApp();

  const [selectedAvatar, setSelectedAvatar] = useState(AVATARS[0].id);
  const [selectedColor, setSelectedColor]   = useState(COLORS[0]);

  const currentEmoji = AVATARS.find((a) => a.id === selectedAvatar)?.emoji ?? '🦸‍♂️';

  const handleStart = () => {
    updateProfile({
      avatar: { id: selectedAvatar, color: selectedColor },
      isOnboarded: true,
    });
    navigate('/home');
  };

  return (
    <div className="av-screen">
      <div className="av-container">
        {/* Progress dots */}
        <div className="av-progress" aria-label="Step 3 of 3">
          <span className="av-dot av-dot--done" />
          <span className="av-dot av-dot--done" />
          <span className="av-dot av-dot--active" />
        </div>

        {/* Back */}
        <button className="av-back" onClick={() => navigate(-1)} aria-label="Go back">
          <ArrowLeft size={20} />
          Back
        </button>

        <div className="av-header">
          <h1 className="av-title">Choose Your Avatar</h1>
          <p className="av-subtitle">Pick your hero for the journey</p>
        </div>

        {/* Preview */}
        <div className="av-preview">
          <div
            className="av-preview-circle"
            style={{ background: selectedColor }}
          >
            <span className="av-preview-emoji">{currentEmoji}</span>
          </div>
          <p className="av-preview-label">Your hero</p>
        </div>

        {/* Avatar grid */}
        <div className="av-grid">
          {AVATARS.map((av) => {
            const isSel = selectedAvatar === av.id;
            return (
              <button
                key={av.id}
                className={`av-item${isSel ? ' av-item--selected' : ''}`}
                onClick={() => setSelectedAvatar(av.id)}
                aria-pressed={isSel}
                style={isSel ? { boxShadow: `0 0 0 3px ${selectedColor}, 0 0 18px ${selectedColor}66` } : {}}
              >
                <div
                  className="av-item-circle"
                  style={{ background: isSel ? selectedColor : '#EEEEFF' }}
                >
                  <span className="av-item-emoji">{av.emoji}</span>
                </div>
              </button>
            );
          })}
        </div>

        {/* Color picker */}
        <div className="av-colors">
          <p className="av-colors-label">Background color</p>
          <div className="av-colors-row">
            {COLORS.map((color) => (
              <button
                key={color}
                className={`av-color-swatch${selectedColor === color ? ' av-color-swatch--selected' : ''}`}
                style={{ background: color }}
                onClick={() => setSelectedColor(color)}
                aria-label={`Color ${color}`}
              />
            ))}
          </div>
        </div>

        {/* CTA */}
        <button className="av-start" onClick={handleStart}>
          <Rocket size={18} style={{ marginRight: 8 }} />
          Start Your Journey!
        </button>
      </div>
    </div>
  );
}
