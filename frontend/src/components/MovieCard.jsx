import { useMarkWatched } from "../api/list";

export default function MovieCard({
	movie,
	onAdd,
	onDelete,
	onMarkWatched,
	onSchedule,
	onViewSchedule,
}) {
	const markWatchedMutation = useMarkWatched();

	const poster = movie?.Poster || movie?.posterUrl || movie?.poster || "";
	const isMarking =
		markWatchedMutation.isPending ?? markWatchedMutation.isLoading;

	return (
		<div className="bg-white p-2 rounded-lg shadow-md w-[150px]">
			{/* Poster */}
			<div
				className="relative w-full h-[200px] rounded-md bg-cover bg-center border"
				style={{
					backgroundImage: poster ? `url(${poster})` : "none",
					backgroundColor: poster ? "transparent" : "#f3f4f6",
				}}
			>
				{/* Title overlay (FIXED properly) */}
				<div className="absolute top-1 left-1 bg-black/60 text-white p-1 rounded max-w-[120px]">
					<p className="text-xs font-semibold m-0 leading-tight break-words">
						{movie.Title}
					</p>
					<p className="text-[10px] m-0">{movie.Year}</p>
				</div>
			</div>

			{/* BUTTONS BELOW POSTER */}
			<div className="mt-3 flex flex-col items-center gap-2">
				{onAdd && (
					<button
						onClick={onAdd}
						className="bg-blue-500 hover:bg-blue-600 text-white text-xs px-3 py-1 rounded w-full"
					>
						Add
					</button>
				)}

				{/* Only show schedule/watched/delete for movies already in watchlist (no onAdd) */}
				{!onAdd && (
					<>
						{/* Schedule / Scheduled state */}
						{movie && movie.scheduled_at ? (
							<button
								onClick={() => onViewSchedule && onViewSchedule(movie)}
								className="w-full bg-indigo-600 text-white text-xs px-2 py-1 rounded"
								title="View in My Schedule"
							>
								Scheduled
							</button>
						) : (
							onSchedule && (
								<button
									onClick={onSchedule}
									className="bg-indigo-500 hover:bg-indigo-600 text-white text-xs px-3 py-1 rounded w-full"
								>
									Schedule
								</button>
							)
						)}

						{onDelete && (
							<button
								onClick={onDelete}
								className="bg-red-500 hover:bg-red-600 text-white text-xs px-3 py-1 rounded w-full"
							>
								Delete
							</button>
						)}

						{movie.watched ? (
							<div className="bg-green-500 text-white text-xs px-3 py-1 rounded w-full text-center">
								✓ Watched
							</div>
						) : (
							onMarkWatched && (
								<button
									onClick={onMarkWatched}
									disabled={isMarking}
									className="bg-green-500 hover:bg-green-600 text-white text-xs px-3 py-1 rounded w-full disabled:opacity-50"
								>
									{isMarking ? "Marking..." : "Watched"}
								</button>
							)
						)}
					</>
				)}
			</div>
		</div>
	);
}
