import { useState, useEffect } from "react";
import SearchBar from "../components/SearchBar";
import MovieCard from "../components/MovieCard";
import useSchedule from "../hooks/useSchedule";
import {
	useSearch,
	useGetMyList,
	useAddToList,
	useDeleteItem,
	useGetMovies,
	useMarkWatched,
} from "../api/list";
import { Chart, registerables } from "chart.js";
import { useRef } from "react";
Chart.register(...registerables);

// import { addToList, getMyList } from '../dbMock'

function RatingsChart({ movies }) {
	const chartRef = useRef(null);
	const chartInstance = useRef(null);

	useEffect(() => {
		if (!chartRef.current || !movies || movies.length === 0) return;

		const ctx = chartRef.current.getContext("2d");

		// Destroy previous chart instance
		if (chartInstance.current) {
			chartInstance.current.destroy();
		}

		// Filter out null/undefined movies and movies without titles
		const validMovies = movies.filter((m) => m && m.Title);

		if (validMovies.length === 0) {
			return;
		}

		const labels = validMovies.map((m) => {
			const title = m.Title || "Unknown";
			return title.length > 20 ? title.substring(0, 20) + "..." : title;
		});

		const imdbData = validMovies.map((m) => {
			if (!m.imdbRating) return 0;
			const rating = parseFloat(m.imdbRating);
			return isNaN(rating) ? 0 : rating;
		});

		const rtData = validMovies.map((m) => {
			if (!m.Ratings || !Array.isArray(m.Ratings)) return null;
			const rtRating = m.Ratings.find(
				(r) => r && r.Source === "Rotten Tomatoes"
			);
			if (!rtRating || !rtRating.Value) return null;
			const value = parseInt(rtRating.Value);
			return isNaN(value) ? null : value / 10;
		});

		const metaData = validMovies.map((m) => {
			if (!m.Ratings || !Array.isArray(m.Ratings)) return null;
			const metaRating = m.Ratings.find((r) => r && r.Source === "Metacritic");
			if (!metaRating || !metaRating.Value) return null;
			const value = parseInt(metaRating.Value);
			return isNaN(value) ? null : value / 10;
		});

		chartInstance.current = new Chart(ctx, {
			type: "bar",
			data: {
				labels: labels,
				datasets: [
					{
						label: "IMDB (out of 10)",
						data: imdbData,
						backgroundColor: "rgba(245, 158, 11, 0.6)",
						borderColor: "rgba(245, 158, 11, 1)",
						borderWidth: 1,
					},
					{
						label: "Rotten Tomatoes (out of 10)",
						data: rtData,
						backgroundColor: "rgba(239, 68, 68, 0.6)",
						borderColor: "rgba(239, 68, 68, 1)",
						borderWidth: 1,
					},
					{
						label: "Metacritic (out of 10)",
						data: metaData,
						backgroundColor: "rgba(34, 197, 94, 0.6)",
						borderColor: "rgba(34, 197, 94, 1)",
						borderWidth: 1,
					},
				],
			},
			options: {
				responsive: true,
				maintainAspectRatio: false,
				scales: {
					y: {
						beginAtZero: true,
						max: 10,
						title: {
							display: true,
							text: "Rating",
						},
					},
					x: {
						title: {
							display: true,
							text: "Movies",
						},
					},
				},
				plugins: {
					legend: {
						display: true,
						position: "top",
					},
					title: {
						display: true,
						text: "Movie Ratings Comparison",
					},
				},
			},
		});

		return () => {
			if (chartInstance.current) {
				chartInstance.current.destroy();
			}
		};
	}, [movies]);

	if (!movies || movies.length === 0) {
		return (
			<p className="text-gray-500">
				No movies to display ratings. Add some to your watchlist!
			</p>
		);
	}
	return <canvas ref={chartRef} />;
}

function WatchTimelineChart({ movies, timeFilter, groupBy }) {
	const chartRef = useRef(null);
	const chartInstance = useRef(null);

	useEffect(() => {
		if (!chartRef.current || !movies || movies.length === 0) return;

		const ctx = chartRef.current.getContext("2d");

		if (chartInstance.current) {
			chartInstance.current.destroy();
		}

		// Filter watched movies - handle null values
		const watchedMovies = movies.filter(
			(m) =>
				m &&
				m.watched === true &&
				m.watched_at &&
				typeof m.watched_at === "string"
		);

		if (watchedMovies.length === 0) {
			chartInstance.current = new Chart(ctx, {
				type: "bar",
				data: {
					labels: [],
					datasets: [
						{
							label: "Movies Watched",
							data: [],
							backgroundColor: "rgba(59, 130, 246, 0.6)",
							borderColor: "rgba(59, 130, 246, 1)",
							borderWidth: 1,
						},
					],
				},
				options: {
					responsive: true,
					maintainAspectRatio: false,
					plugins: {
						title: {
							display: true,
							text: "No watched movies yet",
						},
					},
				},
			});
			return;
		}

		// Apply time filter
		const now = new Date();
		const filteredMovies = watchedMovies.filter((m) => {
			try {
				const watchDate = new Date(m.watched_at);
				if (isNaN(watchDate.getTime())) return false;

				const diffDays = (now - watchDate) / (1000 * 60 * 60 * 24);

				switch (timeFilter) {
					case "week":
						return diffDays <= 7;
					case "month":
						return diffDays <= 30;
					case "year":
						return diffDays <= 365;
					default:
						return true;
				}
			} catch (e) {
				console.error("Error parsing watched_at date:", e);
				return false;
			}
		});

		if (filteredMovies.length === 0) {
			chartInstance.current = new Chart(ctx, {
				type: "bar",
				data: {
					labels: [],
					datasets: [
						{
							label: "Movies Watched",
							data: [],
							backgroundColor: "rgba(59, 130, 246, 0.6)",
							borderColor: "rgba(59, 130, 246, 1)",
							borderWidth: 1,
						},
					],
				},
				options: {
					responsive: true,
					maintainAspectRatio: false,
					plugins: {
						title: {
							display: true,
							text: "No movies watched in this period",
						},
					},
				},
			});
			return;
		}

		// Group movies by day/week/month
		const grouped = {};
		filteredMovies.forEach((m) => {
			try {
				const date = new Date(m.watched_at);
				let key;

				if (groupBy === "day") {
					// Group by day
					key = date.toLocaleDateString("en-US", {
						month: "short",
						day: "numeric",
						year: "numeric",
					});
				} else if (groupBy === "week") {
					// Group by week (ISO week)
					const weekStart = new Date(date);
					weekStart.setDate(date.getDate() - date.getDay());
					key = `Week of ${weekStart.toLocaleDateString("en-US", {
						month: "short",
						day: "numeric",
					})}`;
				} else if (groupBy === "month") {
					// Group by month
					key = date.toLocaleDateString("en-US", {
						month: "short",
						year: "numeric",
					});
				}

				if (key) {
					if (!grouped[key]) {
						grouped[key] = { count: 0, date: date, movies: [] };
					}
					grouped[key].count++;
					grouped[key].movies.push(m.Title || "Unknown Movie");
				}
			} catch (e) {
				console.error("Error grouping movie:", e);
			}
		});

		// Sort by date
		const sortedGroups = Object.entries(grouped).sort(
			([, a], [, b]) => a.date - b.date
		);

		const labels = sortedGroups.map(([key]) => key);
		const data = sortedGroups.map(([, value]) => value.count);

		chartInstance.current = new Chart(ctx, {
			type: "bar",
			data: {
				labels: labels,
				datasets: [
					{
						label: "Movies Watched",
						data: data,
						backgroundColor: "rgba(59, 130, 246, 0.6)",
						borderColor: "rgba(59, 130, 246, 1)",
						borderWidth: 1,
					},
				],
			},
			options: {
				responsive: true,
				maintainAspectRatio: false,
				scales: {
					y: {
						beginAtZero: true,
						title: {
							display: true,
							text: "Number of Movies",
						},
						ticks: {
							stepSize: 1,
						},
					},
					x: {
						title: {
							display: true,
							text:
								groupBy === "day"
									? "Day"
									: groupBy === "week"
									? "Week"
									: "Month",
						},
					},
				},
				plugins: {
					legend: {
						display: false,
					},
					title: {
						display: true,
						text: `Movies Watched by ${
							groupBy.charAt(0).toUpperCase() + groupBy.slice(1)
						}`,
					},
					tooltip: {
						callbacks: {
							label: function (context) {
								return `Movies: ${context.parsed.y}`;
							},
							afterLabel: function (context) {
								const dataIndex = context.dataIndex;
								const group = sortedGroups[dataIndex];
								if (group && group[1].movies) {
									return group[1].movies;
								}
								return [];
							},
						},
					},
				},
			},
		});

		return () => {
			if (chartInstance.current) {
				chartInstance.current.destroy();
			}
		};
	}, [movies, timeFilter, groupBy]);

	if (!movies || movies.length === 0) {
		return (
			<p className="text-gray-500">
				No movies to display. Make sure you have watched some!
			</p>
		);
	}
	return <canvas ref={chartRef} />;
}

export default function MyList() {
	const [query, setQuery] = useState("");
	const [timeFilter, setTimeFilter] = useState("all");
	const [groupBy, setGroupBy] = useState("day");
	const { data: searchresults, refetch: refetchSearches,isLoading: isSearching } = useSearch(query);
	const [filteredSearchResults, setFilteredSearchResults] = useState([]);
	const { data: watchlist = [], refetch: refetchWatchlist } = useGetMyList();
	const movies = useGetMovies(watchlist);
	const watchlistMutation = useAddToList();
	const deleteMutation = useDeleteItem();
	const markWatchedMutation = useMarkWatched();
	const { scheduleMovie } = useSchedule();
	const [scheduleModal, setScheduleModal] = useState(null);
	const [scheduleDate, setScheduleDate] = useState("");
	const [scheduleTime, setScheduleTime] = useState("");
	const [scheduleAmpm, setScheduleAmpm] = useState("AM");
	const [scheduleError, setScheduleError] = useState(null);
	// Minimum selectable date for schedule (today)
	const minDate = (() => {
		const d = new Date();
		const pad = (n) => String(n).padStart(2, "0");
		return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
	})();

	// Sync search results with filtered state
	useEffect(() => {
		if (searchresults) {
			setFilteredSearchResults(searchresults);
		}
	}, [searchresults]);

	async function handleSearch(query) {
		setQuery(query);
		setTimeout(function () {
			refetchSearches();
		}, 100);
	}

	function openSchedule(m) {
		setScheduleModal(m);
		if (m?.scheduled_at) {
			const parts = toLocalParts(m.scheduled_at);
			setScheduleDate(parts.date);
			setScheduleTime(parts.time);
			setScheduleAmpm(parts.ampm);
		} else {
			// default to today when opening scheduler for new items
			setScheduleDate(minDate);
			setScheduleTime("");
			setScheduleAmpm("AM");
		}
	}

	function closeSchedule() {
		setScheduleModal(null);
		setScheduleDate(minDate);
		setScheduleTime("");
		setScheduleAmpm("AM");
	}

	async function onApplySchedule(e) {
		e.preventDefault();
		if (!scheduleModal) return;
		try {
			let iso = null;
			if (scheduleDate && scheduleTime) {
				// scheduleTime is like HH:MM (24h) from input; but we accept user-entered 12h via AM/PM select
				let [hh, mm] = scheduleTime.split(":").map((s) => parseInt(s, 10));
				if (scheduleAmpm === "PM" && hh < 12) hh += 12;
				if (scheduleAmpm === "AM" && hh === 12) hh = 0;
				const [year, month, day] = scheduleDate
					.split("-")
					.map((s) => parseInt(s, 10));
				const dt = new Date(year, month - 1, day, hh, mm || 0);
				iso = dt.toISOString();
			}
			await scheduleMovie(
				scheduleModal.omdb_id || scheduleModal.imdbID || scheduleModal.id,
				iso
			);
			// close modal immediately after successful schedule so UI is responsive
			closeSchedule();
			// refresh watchlist, but don't treat refresh failures as schedule failures
			try {
				await refetchWatchlist();
			} catch (refreshErr) {
				console.warn(
					"Failed to refresh watchlist after scheduling",
					refreshErr
				);
				setScheduleError(
					"Scheduled but failed to refresh list: " +
						(refreshErr.message || refreshErr)
				);
				setTimeout(() => setScheduleError(null), 5000);
			}
		} catch (err) {
			console.error("Schedule failed", err);
			// show more informative message to the user when backend returns details
			const msg = err && err.message ? err.message : "Failed to schedule";
			setScheduleError(msg);
			// auto-clear after 5s
			setTimeout(() => setScheduleError(null), 5000);
		}
	}

	function handleAdd(movie) {
		watchlistMutation.mutate(movie);
		// Remove movie from search results immediately
		setFilteredSearchResults((prev) =>
			prev.filter(
				(m) =>
					(m.imdbID || m.imdbId || m.id) !==
					(movie.imdbID || movie.imdbId || movie.id)
			)
		);
		setTimeout(function () {
			refetchWatchlist();
		}, 100);
	}

	function handleDelete(movie) {
		deleteMutation.mutate(movie.imdbID);
		setTimeout(function () {
			refetchWatchlist();
		}, 100);
	}
	function handleClear() {
        setQuery("");
		setTimeout(function () {
			refetchSearches();
		}, 100);
        setFilteredSearchResults([]);
    }
	function handleMarkWatched(movie) {
		markWatchedMutation.mutate(movie.imdbID);
		setTimeout(function () {
			refetchWatchlist();
		}, 100);
	}

	// Navigate to My Schedule view for a movie with a schedule
	function handleViewSchedule(movie) {
		// prefer React Router navigation by setting window.location.hash or using history
		// `NavLink`/Routes configured at `/myschedule` — do a push to that route
		// Pass selected movie id via query param for convenience
		const id = movie.omdb_id || movie.imdbID || movie.id;
		window.location.href = `/myschedule?highlight=${encodeURIComponent(id)}`;
	}
	// debug logs removed
	return (
		<div className="p-6 max-w-7xl mx-auto space-y-8">
			<h1 className="text-3xl font-bold">My Movie Dashboard</h1>

			<div title="Watchlist" className="space-y-6">
				<h1 className="text-2xl font-semibold">My Watchlist</h1>
				<SearchBar onSearch={handleSearch} onClear={handleClear}/>


				{filteredSearchResults && filteredSearchResults.length > 0 && (
					<div>
						<h2 className="text-lg font-semibold mt-4 mb-2">Search Results</h2>
						<div
							className="grid gap-4"
							style={{ display: "flex", flexWrap: "wrap", width: "100%" }}
						>
							{filteredSearchResults.map((m) => {
								// Check if movie is already in watchlist
								const isInWatchlist = watchlist.some(
									(wm) =>
										wm.omdb_id === m.imdbID ||
										wm.omdb_id === m.imdbId ||
										wm.omdb_id === m.id
								);

								return (
									<MovieCard
										key={m.imdbID || m.imdbId || m.id}
										movie={m}
										onAdd={!isInWatchlist ? () => handleAdd(m) : undefined}
									/>
								);
							})}
						</div>
					</div>
				)}

				<div>
					<h2 className="text-lg font-semibold mt-4 mb-2">Your List</h2>
					{!movies.data || movies.data.length < 1 || !movies.data[0] ? (
						<p className="text-gray-500">
							Your list is empty. Search for a movie to add it.
						</p>
					) : (
						<div
							className="grid gap-4"
							style={{ display: "flex", flexWrap: "wrap", width: "100%" }}
						>
							{movies.data.map((m) => {
								if (m) {
									return (
										<MovieCard
											key={m.imdbID}
											movie={m}
											onDelete={() => handleDelete(m)}
											onMarkWatched={() => handleMarkWatched(m)}
											onSchedule={() => openSchedule(m)}
											onViewSchedule={(mv) => handleViewSchedule(mv)}
										/>
									);
								}
							})}
						</div>
					)}
				</div>
			</div>

			<div title="ChartSection" className="space-y-6">
				<h2 className="text-2xl font-semibold">📊 Analytics & Statistics</h2>

				{/* Main Charts Row */}
				<div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
					{/* Ratings Chart */}
					<div className="bg-white p-6 rounded-lg shadow-md">
						<div className="h-80">
							<RatingsChart movies={movies.data} />
						</div>
					</div>

					{/* Timeline Chart with grouping */}
					<div className="bg-white p-6 rounded-lg shadow-md">
						<div className="mb-4 flex items-center justify-between flex-wrap gap-2">
							<h3 className="text-lg font-semibold">Watch Timeline</h3>
							<div className="flex gap-2">
								<select
									value={groupBy}
									onChange={(e) => setGroupBy(e.target.value)}
									className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
								>
									<option value="day">By Day</option>
									<option value="week">By Week</option>
									<option value="month">By Month</option>
								</select>
								<select
									value={timeFilter}
									onChange={(e) => setTimeFilter(e.target.value)}
									className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
								>
									<option value="all">All Time</option>
									<option value="week">Last Week</option>
									<option value="month">Last Month</option>
									<option value="year">Last Year</option>
								</select>
							</div>
						</div>
						<div className="h-72">
							<WatchTimelineChart
								movies={movies.data}
								timeFilter={timeFilter}
								groupBy={groupBy}
							/>
						</div>
					</div>
				</div>
			</div>

			{scheduleModal && (
				<div
					className="fixed inset-0 bg-black/40 flex items-center justify-center z-50"
					onClick={closeSchedule}
				>
					<form
						onSubmit={onApplySchedule}
						onClick={(e) => e.stopPropagation()}
						className="bg-white p-6 rounded shadow w-96"
					>
						<h2 className="font-bold mb-2">
							Schedule:{" "}
							{scheduleModal.Title ||
								scheduleModal.title ||
								scheduleModal.omdb_id}
						</h2>
						{scheduleError && (
							<div className="mb-3 p-2 bg-red-100 border border-red-300 text-red-800 rounded">
								{scheduleError}
							</div>
						)}
						<div className="mb-4 grid grid-cols-3 gap-2 items-end">
							<div className="col-span-2">
								<label className="block text-sm mb-1">Date</label>
								<input
									type="date"
									min={minDate}
									value={scheduleDate}
									onChange={(e) => setScheduleDate(e.target.value)}
									className="w-full border px-2 py-1"
								/>
							</div>
							<div>
								<label className="block text-sm mb-1">Time</label>
								<input
									type="time"
									value={scheduleTime}
									onChange={(e) => setScheduleTime(e.target.value)}
									className="w-full border px-2 py-1"
								/>
							</div>
							<div className="col-span-3 mt-1">
								<label className="block text-sm mb-1">AM/PM</label>
								<select
									value={scheduleAmpm}
									onChange={(e) => setScheduleAmpm(e.target.value)}
									className="border px-2 py-1"
								>
									<option>AM</option>
									<option>PM</option>
								</select>
							</div>
						</div>
						<div className="flex justify-end gap-2 mt-4">
							<button
								type="submit"
								className="px-3 py-1 bg-green-600 text-white rounded"
							>
								Schedule
							</button>
						</div>
					</form>
				</div>
			)}
		</div>
	);
}

function toLocalParts(iso) {
	if (!iso) return { date: "", time: "", ampm: "AM" };
	const d = new Date(iso);
	const pad = (n) => String(n).padStart(2, "0");
	const date = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(
		d.getDate()
	)}`;
	let hours = d.getHours();
	const ampm = hours >= 12 ? "PM" : "AM";
	if (hours === 0) hours = 12;
	else if (hours > 12) hours = hours - 12;
	const time = `${pad(hours)}:${pad(d.getMinutes())}`;
	return { date, time, ampm };
}
