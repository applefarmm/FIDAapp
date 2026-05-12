import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AppProvider } from "./context/AppContext";

import Welcome from "./screens/Onboarding/Welcome";
import GoalSetter from "./screens/Onboarding/GoalSetter";
import AvatarCustomization from "./screens/Onboarding/AvatarCustomization";
import FitnessLevel from "./screens/profile/FitnessLevel";

import Dashboard from "./screens/main/Dashboard";
import Workouts from "./screens/main/Workouts";
import WorkoutPlayer from "./screens/main/WorkoutPlayer";
import QuestBoard from "./screens/main/QuestBoard";
import Leaderboard from "./screens/main/Leaderboard";
import Achievements from "./screens/main/Achievements";
import Profile from "./screens/main/Profile";

export default function App() {
  return (
    <AppProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/welcome" element={<Welcome />} />
          <Route path="/goal-setter" element={<GoalSetter />} />
          <Route path="/avatar" element={<AvatarCustomization />} />
          <Route path="/fitness-level" element={<FitnessLevel />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/workouts" element={<Workouts />} />
          <Route path="/workout-player" element={<WorkoutPlayer />} />
          <Route path="/quests" element={<QuestBoard />} />
          <Route path="/leaderboard" element={<Leaderboard />} />
          <Route path="/achievements" element={<Achievements />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="*" element={<Navigate to="/welcome" replace />} />
        </Routes>
      </BrowserRouter>
    </AppProvider>
  );
}
