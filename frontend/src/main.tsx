import React, { Suspense, lazy } from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './lib/AuthContext'
import { ProtectedRoute } from './components/ProtectedRoute'
import { AppLayout } from './components/AppLayout'
const AuthPage = lazy(() => import('./pages/AuthPage').then(m => ({ default: m.AuthPage })))
const DashboardPage = lazy(() => import('./pages/DashboardPage').then(m => ({ default: m.DashboardPage })))
const AnalyzePage = lazy(() => import('./pages/AnalyzePage').then(m => ({ default: m.AnalyzePage })))
const ProblemsPage = lazy(() => import('./pages/ProblemsPage').then(m => ({ default: m.ProblemsPage })))
const StudyPage = lazy(() => import('./pages/StudyPage').then(m => ({ default: m.StudyPage })))
const QuestionBankPage = lazy(() => import('./pages/QuestionBankPage').then(m => ({ default: m.QuestionBankPage })))
const QuestionDetailPage = lazy(() => import('./pages/QuestionDetailPage').then(m => ({ default: m.QuestionDetailPage })))
const SubmissionsPage = lazy(() => import('./pages/SubmissionsPage').then(m => ({ default: m.SubmissionsPage })))
const ProgressPage = lazy(() => import('./pages/ProgressPage').then(m => ({ default: m.ProgressPage })))
const InterviewPage = lazy(() => import('./pages/InterviewPage').then(m => ({ default: m.InterviewPage })))
const ProfilePage = lazy(() => import('./pages/AccountPages').then(m => ({ default: m.ProfilePage })))
const SettingsPage = lazy(() => import('./pages/AccountPages').then(m => ({ default: m.SettingsPage })))
const pageLoader = <div className="loading-screen"><div className="spinner"/><span>Loading your workspace…</span></div>
import './styles.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Suspense fallback={pageLoader}><AuthPage mode="login"/></Suspense>}/>
          <Route path="/register" element={<Suspense fallback={pageLoader}><AuthPage mode="register"/></Suspense>}/>
          <Route element={<ProtectedRoute/>}>
            <Route element={<AppLayout/>}>
              <Route path="/analyze" element={<Suspense fallback={<div className="loading-screen"><div className="spinner"/><span>Opening the code editor…</span></div>}><AnalyzePage/></Suspense>}/>
              <Route path="/" element={<Navigate to="/dashboard" replace/>}/>
              <Route path="/dashboard" element={<Suspense fallback={pageLoader}><DashboardPage/></Suspense>}/>
              <Route path="/problems" element={<Suspense fallback={pageLoader}><ProblemsPage/></Suspense>}/>
              <Route path="/questions" element={<Suspense fallback={pageLoader}><QuestionBankPage/></Suspense>}/>
              <Route path="/questions/:slug" element={<Suspense fallback={pageLoader}><QuestionDetailPage/></Suspense>}/>
              <Route path="/study" element={<Suspense fallback={pageLoader}><StudyPage/></Suspense>}/>
              <Route path="/submissions" element={<Suspense fallback={pageLoader}><SubmissionsPage/></Suspense>}/>
              <Route path="/progress" element={<Suspense fallback={pageLoader}><ProgressPage/></Suspense>}/>
              <Route path="/interview" element={<Suspense fallback={pageLoader}><InterviewPage/></Suspense>}/>
              <Route path="/profile" element={<Suspense fallback={pageLoader}><ProfilePage/></Suspense>}/>
              <Route path="/settings" element={<Suspense fallback={pageLoader}><SettingsPage/></Suspense>}/>
            </Route>
          </Route>
          <Route path="*" element={<Navigate to="/dashboard" replace/>}/>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  </React.StrictMode>,
)
