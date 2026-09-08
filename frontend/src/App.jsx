import { Routes, Route, NavLink, Navigate } from 'react-router-dom'
import { useContext } from 'react'
import { AuthContext } from './context/AuthContext'
import Auth from './routes/AuthPage'
import MyList from './routes/MyList'
import MySchedule from './routes/MySchedule'
import Temperature from './routes/Temperature'
import NowShowing from './routes/NowShowing'

function ProtectedRoute({ children }) {
  const { token } = useContext(AuthContext)
  return token ? children : <Navigate to="/auth" replace />
}

export default function App() {
  const { token, logout, user } = useContext(AuthContext)

  if (!token) {
    return <Auth />
  }

  return (
    <div className="min-h-screen bg-gray-50 text-gray-900">
      <nav className="flex gap-4 p-4 border-b bg-white shadow-sm justify-between items-center">
        <div className="flex gap-4">
          <NavLink to="/nowshowing" className={({ isActive }) => (isActive ? 'font-bold' : '')}>
            Now Showing
          </NavLink>
          <NavLink to="/mylist" className={({ isActive }) => (isActive ? 'font-bold' : '')}>
            My List
          </NavLink>
          <NavLink to="/myschedule" className={({ isActive }) => (isActive ? 'font-bold' : '')}>
            My Schedule
          </NavLink>
          <NavLink to="/temperature" className={({ isActive }) => (isActive ? 'font-bold' : '')}>
            Temperature
          </NavLink>
          
        </div>
        <div className="flex items-center gap-4">
          <span className="text-sm text-gray-600 max-w-xs truncate">{user?.name || user?.email}</span>
          <button
            onClick={logout}
            className="px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600 text-sm"
          >
            Logout
          </button>
        </div>
      </nav>

      <main className="p-4">
        <Routes>
          <Route path="/" element={<Navigate to="/mylist" />} />
          <Route path="/mylist" element={<ProtectedRoute><MyList /></ProtectedRoute>} />
          <Route path="/myschedule" element={<ProtectedRoute><MySchedule /></ProtectedRoute>} />
          <Route path="/temperature" element={<ProtectedRoute><Temperature /></ProtectedRoute>} />
          <Route path="/nowshowing" element={<ProtectedRoute><NowShowing /></ProtectedRoute>} />

        </Routes>
      </main>
    </div>
  )
}