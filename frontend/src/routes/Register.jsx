import { useContext, useState } from "react";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";
import { fetchWithAuth } from "../utils/fetchWithAuth";

export default function Register() {
	const [name, setName] = useState("");
	const [email, setEmail] = useState("");
	const [password, setPassword] = useState("");
	const { setToken, setUser } = useContext(AuthContext);

	const navigate = useNavigate();

async function registerUser(e) {
	e.preventDefault();
	try {
		const request = { name: name, email: email, password: password };
		const response = await fetchWithAuth(`${import.meta.env.VITE_API_BASE_URL}/api/auth/register`, {
			method: "POST",
			body: JSON.stringify(request),
			headers: {
				"Content-Type": "application/json",
			},
			handleUnauthorized: false,
		});
		if (!response.ok) throw new Error("Registration failed");
		const body = await response.json();
		if (body?.token) {
			setToken(body.token);
		}
		if (body?.user) {
			setUser(body.user);
		}
		navigate("/");
	} catch (error) {
		console.log(error);
	}
}

	return (
		<div className="regContainer">
			<form onSubmit={registerUser}>
				<h1>Register now</h1>
				<div className="nameContainer">
					<label>Username:</label>
					<input
						onChange={(e) => setName(e.target.value)}
						className="inputField"
					/>
				</div>
				<div className="emailContainer">
					<label>E-Mail:</label>
					<input
						onChange={(e) => setEmail(e.target.value)}
						className="inputField"
						type="email"
					/>
				</div>
				<div className="passwordContainer">
					<label>Password:</label>
					<input
						onChange={(e) => setPassword(e.target.value)}
						className="inputField"
						type="password"
					/>
				</div>
				<button type="submit">Register now</button>
			</form>
		</div>
	);
}
