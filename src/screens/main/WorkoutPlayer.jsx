import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import './WorkoutPlayer.css';

const WORKOUT = {
  title: 'Power HIIT',
  icon: '⚡',
  totalXP: 350,
  exercises: [
    { name: 'Warm-Up Jog',      duration: 60, icon: '🏃' },
    { name: 'Burpees',          duration: 45, icon: '💥' },
    { name: 'Jump Squats',      duration: 45, icon: '🦵' },
    { name: 'Push-Ups',         duration: 45, icon: '💪' },
    { name: 'Mountain Climbers', duration: 30, icon: '🧗' },
    { name: 'Plank',            duration: 60, icon: '🧘' },
    { name: 'Cool-Down Stretch', duration: 90, icon: '🌿' },
  ],
};

export default function WorkoutPlayer() {
  const [phase, setPhase]       = useState('intro'); // intro | active | done
  const [exIdx, setExIdx]       = useState(0);
  const [timeLeft, setTimeLeft] = useState(WORKOUT.exercises[0].duration);
  const [running, setRunning]   = useState(false);
  const [xpEarned, setXpEarned] = useState(0);
  const navigate                = useNavigate();
  const timerRef                = useRef(null);

  // timer tick
  useEffect(() => {
    if (!running) return;
    timerRef.current = setInterval(() => {
      setTimeLeft(t => {
        if (t <= 1) {
          clearInterval(timerRef.current);
          handleNext();
          return 0;
        }
        return t - 1;
      });
    }, 1000);
    return () => clearInterval(timerRef.current);
  }, [running, exIdx]);

  function handleNext() {
    const next = exIdx + 1;
    if (next >= WORKOUT.exercises.length) {
      setRunning(false);
      setPhase('done');
      setXpEarned(WORKOUT.totalXP);
    } else {
      setExIdx(next);
      setTimeLeft(WORKOUT.exercises[next].duration);
    }
  }

  const ex = WORKOUT.exercises[exIdx];
  const progress = ((WORKOUT.exercises[exIdx].duration - timeLeft) / WORKOUT.exercises[exIdx].duration) * 100;

  if (phase === 'intro') {
    return (
      <div className="wp-screen">
        <button className="wp-back" onClick={() => navigate(-1)}>← Back</button>
        <div className="wp-intro-icon">{WORKOUT.icon}</div>
        <div className="wp-intro-title">{WORKOUT.title}</div>
        <div className="wp-intro-meta">{WORKOUT.exercises.length} exercises · {WORKOUT.totalXP} XP</div>
        <button className="wp-start-btn" onClick={() => { setPhase('active'); setRunning(true); }}>
          Start Workout ⚡
        </button>
      </div>
    );
  }

  if (phase === 'done') {
    return (
      <div className="wp-screen">
        <div className="wp-done-icon">🏆</div>
        <div className="wp-done-title">Workout Complete!</div>
        <div className="wp-done-xp">+{xpEarned} XP</div>
        <button className="wp-done-btn" onClick={() => navigate('/dashboard')}>Back to Dashboard</button>
      </div>
    );
  }

  return (
    <div className="wp-screen">
      <button className="wp-back" onClick={() => navigate(-1)}>← Back</button>
      <div className="wp-exercise-icon">{ex.icon}</div>
      <div className="wp-exercise-name">{ex.name}</div>
      <div className="wp-timer">{timeLeft}s</div>
      <div className="wp-progress-bar">
        <div className="wp-progress-fill" style={{ width: `${progress}%` }} />
      </div>
      <div className="wp-controls">
        <button className="wp-btn" onClick={() => setRunning(r => !r)}>
          {running ? '⏸ Pause' : '▶ Resume'}
        </button>
        <button className="wp-btn" onClick={handleNext}>⏭ Skip</button>
      </div>
      <div className="wp-step-counter">{exIdx + 1} / {WORKOUT.exercises.length}</div>
    </div>
  );
}
