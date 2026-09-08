import { useState } from 'react'
import { useLogin, useRegister } from '../api/auth'

export default function Auth() {
  const [isLogin, setIsLogin] = useState(true)
  const [formData, setFormData] = useState({ email: '', password: '', name: '' })
  
  const loginMutation = useLogin()
  const registerMutation = useRegister(setIsLogin)

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    if (isLogin) {
      loginMutation.mutate({ email: formData.email, password: formData.password })
    } else {
      registerMutation.mutate(formData)
    }
  }

  const mutation = isLogin ? loginMutation : registerMutation
  const error = mutation.error?.message

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-white px-4 py-12">
      <div className="mx-auto w-full max-w-sm sm:max-w-md p-8 bg-white rounded-lg shadow border border-gray-200">
        <h2 className="text-2xl font-bold mb-6 text-center text-gray-900">{isLogin ? 'Login' : 'Register'}</h2>
        
        <form onSubmit={handleSubmit} className="space-y-4 w-full">
          {!isLogin && (
            <input
              type="text"
              name="name"
              placeholder="Full Name"
              value={formData.name}
              onChange={handleChange}
              required
              className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          )}
          
          <input
            type="email"
            name="email"
            placeholder="Email"
            value={formData.email}
            onChange={handleChange}
            required
            className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          
          <input
            type="password"
            name="password"
            placeholder="Password"
            value={formData.password}
            onChange={handleChange}
            required
            className="w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />

          {error && <p className="text-red-500 text-sm">{error}</p>}

          <button
            type="submit"
            disabled={mutation.isPending}
            className="w-full bg-blue-500 text-white py-2 rounded-lg hover:bg-blue-600 disabled:opacity-50"
          >
            {mutation.isPending ? 'Loading...' : isLogin ? 'Login' : 'Register'}
          </button>
        </form>

        <p className="mt-4 text-center text-sm text-gray-700">
          {isLogin ? "Don't have an account?" : 'Already have an account?'}{' '}
          <button
            onClick={() => setIsLogin(!isLogin)}
            className="text-blue-500 hover:underline"
          >
            {isLogin ? 'Register' : 'Login'}
          </button>
        </p>
      </div>
    </div>
  )
}