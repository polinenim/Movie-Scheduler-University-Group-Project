import {useState} from "react"
import {useNavigate} from "react-router-dom"
import { fetchWithAuth } from '../utils/fetchWithAuth'
const API_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export default function Login() {
    const [email, setEmail] = useState("")
    const [password, setPassword] = useState("")

    const navigate = useNavigate()

    async function loginUser(e) {
        e.preventDefault()
        try {
            const request = {email: email, password: password}
            const response = await fetchWithAuth(`${API_URL}/api/auth/login`, {
                method: "POST",
                body: JSON.stringify(request),
                headers: {
                    "Content-Type": "application/json"
                },
                handleUnauthorized: false,
            })
            console.log(response)
            if (response.ok) navigate("/")
        }
        catch(error) {
            console.log(error)
        } 
    }

    return (
    <div className="min-h-screen w-full flex items-center justify-center bg-white px-4 py-12">
        <div className="mx-auto w-full max-w-sm sm:max-w-md p-8 bg-white rounded-lg shadow border border-gray-200">
        <form onSubmit={loginUser} className="w-full">
        <h1 className="text-2xl font-semibold mb-4 text-gray-900 text-center">Login</h1>
            <div className="emailContainer">
                    <label className="block text-sm font-medium text-gray-700">E-Mail:</label>
                    <input onChange={e => setEmail(e.target.value)} className="w-full px-4 py-2 border rounded-lg mt-1 focus:outline-none focus:ring-2 focus:ring-primary" type="email"/>
            </div>
            <div className="passwordContainer">
                <label className="block text-sm font-medium text-gray-700">Password:</label>
                    <input onChange={e => setPassword(e.target.value)} className="w-full px-4 py-2 border rounded-lg mt-1 focus:outline-none focus:ring-2 focus:ring-primary" type="password"/>
            </div>
            <button type="submit" className="w-full bg-primary hover:bg-indigo-600 text-white py-2 rounded-lg">Login</button>
        </form>
        </div>
    </div>
    )
}