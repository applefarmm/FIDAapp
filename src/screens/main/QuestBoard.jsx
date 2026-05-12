import React, { useState } from 'react';
import './QuestBoard.css';

const QUESTS = [
  { id: 1, icon: '⚔️',  title: "Warrior's Path",  desc: 'Complete 3 HIIT workouts this week',  xp: 500,  progress: 2,  total: 3,  category: 'fitness'  },
  { id: 2, icon: '🔥', title: 'Inferno Challenge', desc: 'Burn 500 calories in one session',     xp: 300,  progress: 0,  total: 1,  category: 'cardio'   },
  { id: 3, icon: '🎯', title: 'Daily Devotion',    desc: 'Log in 7 days in a row',              xp: 200,  progress: 5,  total: 7,  category: 'streak'   },
  { id: 4, icon: '💪', title: 'Iron Man',          desc: 'Complete a strength workout',          xp: 150,  progress: 1,  total: 1,  category: 'strength', done: true },
  { id: 5, icon: '🌟', title: 'Rising Star',       desc: 'Reach Level 15',                      xp: 1000, progress: 12, total: 15, category: 'level'    },
];

function QuestCard({ quest }) {
  const pct = Math.min((quest.progress / quest.total) * 100, 100);
  return (
    <div className={`qb-card${quest.done ? ' qb-card--done' : ''}`}>
      <div className="qb-card-left">
        <div className="qb-card-icon">{quest.icon}</div>
      </div>
      <div className="qb-card-body">
        <div className="qb-card-title">{quest.title}{quest.done ? ' ✓' : ''}</div>
        <div className="qb-card-desc">{quest.desc}</div>
        <div className="qb-card-bar">
          <div className="qb-card-fill" style={{ width: `${pct}%` }} />
        </div>
        <div className="qb-card-meta">{quest.progress}/{quest.total} · {quest.xp} XP</div>
      </div>
    </div>
  );
}

export default function QuestBoard() {
  const [filter, setFilter] = useState('all');
  const filtered =
    filter === 'all'  ? QUESTS :
    filter === 'done' ? QUESTS.filter(q => q.done) :
                        QUESTS.filter(q => !q.done);
  return (
    <div className="qb-screen">
      <div className="qb-header">
        <div className="qb-title">Quest Board ⚔️</div>
        <div className="qb-sub">Complete quests, earn XP</div>
      </div>
      <div className="qb-filters">
        {['all', 'active', 'done'].map(f => (
          <button
            key={f}
            className={`qb-filter${filter === f ? ' qb-filter--active' : ''}`}
            onClick={() => setFilter(f)}
          >
            {{ all: 'All', active: 'Active', done: 'Completed' }[f]}
          </button>
        ))}
      </div>
      <div className="qb-list">
        {filtered.map(q => <QuestCard key={q.id} quest={q} />)}
      </div>
    </div>
  );
}
