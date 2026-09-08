import { useCallback } from "react";
import { fetchWithAuth } from "../utils/fetchWithAuth";

export default function useSchedule() {
	const scheduleMovie = useCallback(async (omdbId, isoDatetime) => {
		const url = `${import.meta.env.VITE_API_BASE_URL}/api/watchlist/schedule`;
		const payload = { omdb_id: omdbId, scheduled_at: isoDatetime };
		try {
			console.debug("[scheduleMovie] POST", url, payload);
			const res = await fetchWithAuth(url, {
				method: "POST",
				headers: {
					"Content-Type": "application/json",
				},
				body: JSON.stringify(payload),
			});

			// Log response status for debugging
			if (!res.ok) {
				const txt = await res.text().catch(() => "");
				console.error("[scheduleMovie] response not ok", res.status, txt);
				throw new Error("Failed to schedule: " + res.status + " " + txt);
			}

			const json = await res.json().catch(() => null);
			console.debug("[scheduleMovie] ok", json);
			return json;
		} catch (e) {
			console.error("[scheduleMovie] error", e);
			// rethrow so caller shows alert
			throw e;
		}
	}, []);

	const unscheduleMovie = useCallback(
		async (omdbId) => {
			// backend supports scheduling with null to remove schedule; reuse same endpoint
			return scheduleMovie(omdbId, null);
		},
		[scheduleMovie]
	);

	return { scheduleMovie, unscheduleMovie };
}
