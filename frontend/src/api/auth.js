import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useContext } from "react";
import { AuthContext } from "../context/AuthContext";
import { fetchWithAuth } from "../utils/fetchWithAuth";

const API_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export function useRegister(setIsLogin) {
	const { setToken, setUser } = useContext(AuthContext);
  const queryClient = useQueryClient();

	return useMutation({
		mutationFn: async (data) => {
			const response = await fetch(`${API_URL}/api/auth/register`, {
				method: "POST",
				headers: { "Content-Type": "application/json" },
				body: JSON.stringify(data),
			});
			if (!response.ok) throw new Error("Registration failed");
			return response.json();
		},
		onSuccess: (data) => {
			setToken(data.token);
			setUser(data.user);
      queryClient.invalidateQueries({ queryKey: ["doList"] });
			setIsLogin(true)
		},
	});
}

export function useLogin() {
	const { setToken, setUser } = useContext(AuthContext);
  const queryClient = useQueryClient();

return useMutation({
	mutationFn: async (data) => {
		const response = await fetchWithAuth(`${API_URL}/api/auth/login`, {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify(data),
			// don't auto-redirect or clear tokens on auth endpoints
			handleUnauthorized: false,
		});
		if (!response.ok) throw new Error("Login failed");
		return response.json();
	},
	onSuccess: (data) => {
		setToken(data.token);
		setUser(data.user);
    queryClient.invalidateQueries({ queryKey: ["doList"] });
	},
});
}
