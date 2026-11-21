import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../lib/AuthContext'
export function ProtectedRoute() { const { user, loading } = useAuth(); if (loading) return <div className="loading-screen"><div className="spinner"/><span>Loading your workspace…</span></div>; return user ? <Outlet/> : <Navigate to="/login" replace/> }
