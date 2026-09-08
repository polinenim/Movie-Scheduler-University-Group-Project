import { useQuery } from "@tanstack/react-query";
import { useState, useEffect } from "react";
import { fetchShowtimes } from "../api/showTimes";
import { fetchPoster } from "../api/list";

export default function NowShowing() {
	const [moviesWithPosters, setMoviesWithPosters] = useState([]);
	const [loadingPosters, setLoadingPosters] = useState(false);

	const { data, isLoading, error } = useQuery({
		queryKey: ["showtimes"],
		queryFn: fetchShowtimes,
	});

	// Extract IMDb ID from URL
	const extractImdbId = (imdbUrl) => {
		if (!imdbUrl) return null;
		const match = imdbUrl.match(/tt\d+/);
		return match ? match[0] : null;
	};

	useEffect(() => {
		if (!data || data.length === 0) return;

		async function loadPosters() {
			setLoadingPosters(true);

			// Transform movies and fetch posters
			const moviesPromises = data.map(async (movie) => {
				const edges = movie.showtimesByMovieId?.edges ?? [];

				// Get today's date at midnight for comparison
				const today = new Date();
				today.setHours(0, 0, 0, 0);

				// Group showtimes by date and theatre
				const showtimesByDateAndTheatre = edges.reduce((acc, edge) => {
					const datetime = new Date(edge.node.showDatetime);
					const dateOnly = new Date(datetime);
					dateOnly.setHours(0, 0, 0, 0);

					// Skip dates before today
					if (dateOnly < today) return acc;

					const dateKey = datetime.toLocaleDateString("fi-FI");
					const theatreName = edge.node.theatreName;

					if (!acc[dateKey]) {
						acc[dateKey] = {};
					}

					if (!acc[dateKey][theatreName]) {
						acc[dateKey][theatreName] = [];
					}

					acc[dateKey][theatreName].push({
						time: datetime.toLocaleTimeString("fi-FI", {
							hour: "2-digit",
							minute: "2-digit",
						}),
						datetime: datetime,
						buyUrl: edge.node.buyUrl,
					});

					return acc;
				}, {});

				// Sort times within each theatre and date
				Object.keys(showtimesByDateAndTheatre).forEach((date) => {
					Object.keys(showtimesByDateAndTheatre[date]).forEach((theatre) => {
						showtimesByDateAndTheatre[date][theatre].sort(
							(a, b) => a.datetime - b.datetime
						);
					});
				});

				// Sort dates
				const sortedDates = Object.keys(showtimesByDateAndTheatre).sort(
					(a, b) => {
						const dateA = new Date(a.split(".").reverse().join("-"));
						const dateB = new Date(b.split(".").reverse().join("-"));
						return dateA - dateB;
					}
				);

				// Fetch poster from OMDB
				const imdbId = extractImdbId(movie.imdbUrl);
				const omdbPoster = await fetchPoster(imdbId);

				return {
					title: movie.title,
					poster: omdbPoster || movie.posterUrl || null,
					imdbUrl: movie.imdbUrl,
					showtimesByDateAndTheatre,
					sortedDates,
				};
			});

			const allMovies = await Promise.all(moviesPromises);

			// Remove movies that have zero showtimes
			const moviesWithShows = allMovies.filter(
				(m) => Object.keys(m.showtimesByDateAndTheatre).length > 0
			);

			setMoviesWithPosters(moviesWithShows);
			setLoadingPosters(false);
		}

		loadPosters();
	}, [data]);

	if (isLoading || loadingPosters)
		return <div className="p-6">Loading movies…</div>;
	if (error) return <div className="p-6">Error loading data</div>;
	if (moviesWithPosters.length === 0)
		return <div className="p-6">No movies available</div>;

	return (
		<div className="p-6">
			<h1 className="text-3xl font-bold mb-6">Now Showing</h1>
			<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
				{moviesWithPosters.map((movie, idx) => (
					<div
						key={idx}
						className="bg-white rounded-xl shadow-lg overflow-hidden border"
					>
						{movie.poster ? (
							<img
								src={movie.poster}
								alt={movie.title}
								className="w-full h-64 object-cover"
							/>
						) : (
							<div className="h-64 bg-linear-to-br from-gray-100 to-gray-200 flex items-center justify-center">
								<span className="text-gray-400 text-lg">
									No poster available
								</span>
							</div>
						)}

						<div className="p-4">
							<h2 className="font-bold text-xl mb-3 line-clamp-2">
								{movie.title}
							</h2>

							{movie.imdbUrl && (
								<a
									href={movie.imdbUrl}
									target="_blank"
									rel="noopener noreferrer"
									className="text-blue-600 hover:text-blue-800 text-sm mb-3 inline-block"
								>
									View on IMDb →
								</a>
							)}

							<div className="space-y-3 mt-3">
								{movie.sortedDates.map((date) => (
									<div key={date} className="border-t pt-2">
										<div className="font-semibold text-sm text-gray-700 mb-2">
											{date}
										</div>
										<div className="space-y-2">
											{Object.keys(movie.showtimesByDateAndTheatre[date]).map(
												(theatre) => (
													<div key={theatre} className="ml-2">
														<div className="text-xs text-gray-600 mb-1">
															{theatre}
														</div>
														<div className="flex flex-wrap gap-2">
															{movie.showtimesByDateAndTheatre[date][
																theatre
															].map((showtime, stIdx) =>
																showtime.buyUrl ? (
																	<a
																		key={stIdx}
																		href={showtime.buyUrl}
																		target="_blank"
																		rel="noopener noreferrer"
																		className="inline-block bg-blue-100 text-blue-800 px-2 py-1 rounded text-xs font-medium hover:bg-blue-200 cursor-pointer transition-colors"
																	>
																		{showtime.time}
																	</a>
																) : (
																	<span
																		key={stIdx}
																		className="inline-block bg-gray-100 text-gray-800 px-2 py-1 rounded text-xs font-medium"
																	>
																		{showtime.time}
																	</span>
																)
															)}
														</div>
													</div>
												)
											)}
										</div>
									</div>
								))}
							</div>
						</div>
					</div>
				))}
			</div>
		</div>
	);
}
