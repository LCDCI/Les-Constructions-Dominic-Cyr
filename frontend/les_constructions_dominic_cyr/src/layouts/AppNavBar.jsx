import React from 'react'
import { NavLink } from 'react-router-dom'
import './AppNavBar.css'

export default function AppNavBar() {
  return (
    <header className="site-nav">
      <div className="site-nav-inner">
        <div className="brand">Les Constructions Dominic Cyr</div>
        <nav>
          <NavLink to="/" end className={({ isActive }) => (isActive ? 'active' : '')}>
            Home
          </NavLink>
          <NavLink to="/lots" className={({ isActive }) => (isActive ? 'active' : '')}>
            Lots
          </NavLink>
          {/* add other links here */}
        </nav>
      </div>
    </header>
  )
}