import React, { useState } from 'react';
import Home        from './screens/Home/Home';
import Workouts    from './screens/Workouts/Workouts';
import Leaderboard from './screens/Leaderboard/Leaderboard';
import Profile     from './screens/Profile/Profile';

export default function App() {
  const [screen, setScreen] = useState('home');

  const handleNavigate = (id) => setScreen(id);

  return (
    <div>
      {screen === 'home'        && <Home        onNavigate={handleNavigate} />}
      {screen === 'workouts'    && <Workouts    onNavigate={handleNavigate} />}
      {screen === 'leaderboard' && <Leaderboard onNavigate={handleNavigate} />}
      {screen === 'profile'     && <Profile     onNavigate={handleNavigate} />}
    </div>
  );
}
