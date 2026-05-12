import React from 'react';
import { useNavigate } from 'react-router-dom';
import './Welcome.css';

export default function Welcome() {
  const navigate = useNavigate();
  return (
    <div className="wc-screen">
      <div className="wc-logo">⚡</div>
      <div className="wc-title">FitQuest</div>
      <div className="wc-tagline">Level up your fitness game</div>
      <div className="wc-features">
        <div className="wc-feature">🏆 Earn XP &amp; level up</div>
        <div className="wc-feature">⚔️ Complete epic quests</div>
        <div className="wc-feature">🏅 Unlock achievements</div>
        <div className="wc-feature">📊 Track progress</div>
      </div>
      <button className="wc-cta" onClick={() => navigate('/goal-setter')}>Begin Your Journey 🚀</button>
    </div>
  );
}
