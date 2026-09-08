import { createContext, useState, useEffect } from 'react'
import { useQueryClient } from '@tanstack/react-query'

export const AuthContext = createContext()

export function AuthProvider({ children }) {
  const queryClient = useQueryClient()
  const [token, setToken] = useState(() => sessionStorage.getItem('authToken'))
  const [user, setUser] = useState(null)

  useEffect(() => {
    if (token) {
      sessionStorage.setItem('authToken', token)
    } else {
      sessionStorage.removeItem('authToken')
    }
  }, [token])

  const logout = () => {
    setToken(null)
    setUser(null)
    queryClient.clear()
  }

  return (
    <AuthContext.Provider value={{ token, setToken, user, setUser, logout }}>
      {children}
    </AuthContext.Provider>
  )
}
