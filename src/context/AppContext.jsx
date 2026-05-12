import React, { createContext, useContext, useReducer } from 'react';

const AppContext = createContext(null);

const INIT = {
  avatar:  { name: 'Warrior', icon: '🦊', color: '#FF6B35' },
  level:   12,
  xp:      7340,
  xpNext:  8000,
  streak:  7,
  goal:    'Build Muscle',
  fitness: 'Intermediate',
};

function reducer(state, action) {
  switch (action.type) {
    case 'SET_AVATAR':  return { ...state, avatar:  action.payload };
    case 'SET_GOAL':    return { ...state, goal:    action.payload };
    case 'SET_FITNESS': return { ...state, fitness: action.payload };
    case 'ADD_XP':      return { ...state, xp: state.xp + action.payload };
    default:            return state;
  }
}

export function AppProvider({ children }) {
  const [state, dispatch] = useReducer(reducer, INIT);
  return <AppContext.Provider value={{ state, dispatch }}>{children}</AppContext.Provider>;
}

export function useApp() {
  return useContext(AppContext);
}
