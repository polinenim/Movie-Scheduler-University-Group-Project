import { useQuery, useMutation, useQueries } from "@tanstack/react-query";
import { useContext } from "react";
import { AuthContext } from "../context/AuthContext";
import { fetchWithAuth } from "../utils/fetchWithAuth";

export function useSearch(query) {
	return useQuery({
		queryKey: ["doSearch"],
		refetchOnMount: false,
		queryFn: async () => {
			const url =
				"https://www.omdbapi.com/?s=" +
				query +
				"&apikey=" +
				import.meta.env.VITE_OMDB_API_KEY;
			// console.log(url);
			const response = await fetch(url);
			const result = await response.json();
			const searches = result["Search"];
			console.log("inside api",searches);
			if (!searches) return [];
			return searches;
		},
	});
}

export function useAddToList() {
	return useMutation({
		mutationFn: async (data) => {
			const response = await fetch(
				`${import.meta.env.VITE_API_BASE_URL}/api/watchlist`,
				{
					method: "POST",
					body: JSON.stringify({ omdb_id: data.imdbID }),
					headers: {
						"Content-Type": "application/json",
						Authorization: "Bearer " + sessionStorage.getItem("authToken"),
					},
				}
			);
		},
	});
}

export function useGetMyList() {
	const { token } = useContext(AuthContext);
	return useQuery({
		queryKey: ["doList", token],
		enabled: !!token,
		queryFn: async () => {
			const url = `${import.meta.env.VITE_API_BASE_URL}/api/watchlist`;
			const response = await fetchWithAuth(url, { handleUnauthorized: false });
			if (!response.ok) throw new Error("Failed to load watchlist");
			const result = await response.json();
			return result;
		},
	});
}

export function useDeleteItem() {
	return useMutation({
		mutationFn: async (data) => {
			const url = `${
				import.meta.env.VITE_API_BASE_URL
			}/api/watchlist?omdb_id=${data}`;
			console.log(url);
			const response = await fetch(url, {
				method: "DELETE",
				headers: {
					"Content-Type": "application/json",
					Authorization: "Bearer " + sessionStorage.getItem("authToken"),
				},
			});
		},
	});
}

export function useGetMovies(movies) {
	return useQueries({
		queries: movies.map((m) => {
			return {
				queryKey: ["movie", m.omdb_id],
				queryFn: async () => {
					const res = await fetch(
						"https://www.omdbapi.com/?i=" +
							m.omdb_id +
							"&apikey=" +
							import.meta.env.VITE_OMDB_API_KEY
					);
					return await res.json();
				},
			};
		}),
		enabled: false,
		combine: (results) => {
			const combined = results.map((result, idx) => {
				const apiData = result.data;
				const original = movies[idx];

				if (!apiData) return null;

				return {
					...original, // Keep all properties from the movies[] param
					...apiData, // Override or add OMDB API fields
				};
			});

			return { data: combined };
		},
	});
}

export function useMarkWatched() {
	return useMutation({
		mutationFn: async (omdbId) => {
			const response = await fetchWithAuth(`${import.meta.env.VITE_API_BASE_URL}/api/watchlist/watch`, {
				method: "POST",
				body: JSON.stringify({ omdb_id: omdbId }),
				headers: {
					"Content-Type": "application/json",
				},
			});
			if (!response.ok) throw new Error("Failed to mark as watched");
			return response.json();
		},
	});
}
export async function fetchPoster(imdbId) {
	if (!imdbId) return null;
	try {
		const response = await fetch(
			`https://www.omdbapi.com/?i=${imdbId}&apikey=${
				import.meta.env.VITE_OMDB_API_KEY
			}`
		);
		const data = await response.json();
		return data.Poster && data.Poster !== "N/A" ? data.Poster : null;
	} catch (error) {
		console.error(`Error fetching poster for ${imdbId}:`, error);
		return null;
	}
}
