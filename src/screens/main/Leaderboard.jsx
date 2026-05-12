import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../../context/AppContext';
import BottomNav from '../../components/BottomNav';
import './Leaderboard.css';

const PLAYERS = [
  { rank: 1, name: 'XFit_King', xp: 9800, avatar: '👑', isMe: false },
  { rank: 2, name: 'IronMike', xp: 8750, avatar: '🦾', isMe: false },
  { rank: 3, name: 'SprintQueen', xp: 7620, avatar: '👸', isMe: false },
  { rank: 4, name: 'Athlete', xp: 1240, avatar: '🧑', isMe: true },
  { rank: 5, name: 'BeastMode', xp: 980, avatar: '🐉', isMe: false },
];

export default function Leaderboard() {
  const navigate = useNavigate();
  const { profile } = useApp();

  const handleNav = (tab) => {
    const routes = { home: '/dashboard', quests: '/quests', workouts: '/workouts', leaderboard: '/leaderboard', profile: '/profile' };
    navigate(routes[tab] || '/dashboard');
  };

  return (
    <div className="lb-screen">
      <div className="lb-container">
        <div className="lb-header">
          <h1 className="lb-title">Leaderboard</h1>
          <span className="lb-period">This Week</span>
        </div>

        {/* Top 3 Podium */}
        <div className="lb-podium">
          {PLAYERS.slice(0, 3).map((p, i) => (
            <div key={p.rank} className={`lb-podium-item lb-podium-item--${i + 1}`}>
              <span className="lb-podium-avatar">{p.avatar}</span>
              <span className="lb-podium-name">{p.name}</span>
              <span className="lb-podium-xp">{p.xp} XP</span>
              <div className="lb-podium-rank">{p.rank}</div>
            </div>
          ))}
        </div>

        {/* Full List */}
        <div className="lb-list">
          {PLAYERS.map(p => (
            <div key={p.rank} className={`lb-row${p.isMe ? ' lb-row--me' : ''}`}>
              <span className="lb-row-rank">#{p.rank}</span>
              <span className="lb-row-avatar">{p.avatar}</span>
              <span className="lb-row-name">{p.name}</span>
              <span className="lb-row-xp">{p.xp} XP</span>
            </div>
          ))}
        </div>

        <div style={{ height: 88 }} />
      </div>
      <BottomNav active="leaderboard" onChange={handleNav} />
    </div>
  );
}
