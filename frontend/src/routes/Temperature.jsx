import { useQuery } from "@tanstack/react-query";

// Tampere coordinates
const TAMPERE_LAT = 61.4978;
const TAMPERE_LON = 23.761;

// Weather code to emoji and description mapping
const getWeatherInfo = (code) => {
	const weatherCodes = {
		0: { icon: "☀️", desc: "Clear Skies" },
		1: { icon: "🌤️", desc: "Mainly Clear" },
		2: { icon: "⛅", desc: "Partly Cloudy" },
		3: { icon: "☁️", desc: "Overcast" },
		45: { icon: "🌫️", desc: "Foggy" },
		48: { icon: "🌫️", desc: "Foggy" },
		51: { icon: "🌦️", desc: "Light Drizzle" },
		53: { icon: "🌦️", desc: "Drizzle" },
		55: { icon: "🌧️", desc: "Heavy Drizzle" },
		61: { icon: "🌧️", desc: "Light Rain" },
		63: { icon: "🌧️", desc: "Rain" },
		65: { icon: "⛈️", desc: "Heavy Rain" },
		71: { icon: "🌨️", desc: "Light Snow" },
		73: { icon: "❄️", desc: "Snow" },
		75: { icon: "❄️", desc: "Heavy Snow" },
		80: { icon: "🌦️", desc: "Light Showers" },
		81: { icon: "🌧️", desc: "Showers" },
		82: { icon: "⛈️", desc: "Heavy Showers" },
		95: { icon: "⛈️", desc: "Thunderstorm" },
	};
	return weatherCodes[code] || weatherCodes[0];
};

// Calculate weather score (higher = worse weather = better for movies)
const calculateWeatherScore = (temp, precipitation, weatherCode) => {
	let score = 0;

	// Temperature score (further from 20°C = worse)
	const tempDiff = Math.abs(temp - 20);
	score += tempDiff * 2;

	// Precipitation score
	score += precipitation;

	// Weather code score (higher codes = worse weather)
	if (weatherCode >= 80) score += 40; // Heavy rain/thunderstorm
	else if (weatherCode >= 61) score += 30; // Rain
	else if (weatherCode >= 51) score += 20; // Drizzle
	else if (weatherCode >= 3) score += 10; // Cloudy

	return score;
};

// Determine if weather is good for movies based on ranking
const getMovieRecommendation = (score, isWorstDay) => {
	if (isWorstDay) {
		return { label: "Best for movies!", color: "bg-red-500" };
	}
	if (score >= 40) {
		return { label: "Great for movies!", color: "bg-green-500" };
	}
	return { label: "Good day", color: "bg-yellow-500" };
};

export default function Temperature() {
	const { data, isLoading, error } = useQuery({
		queryKey: ["weather-tampere"],
		queryFn: async () => {
			const response = await fetch(
				`https://api.open-meteo.com/v1/forecast?latitude=${TAMPERE_LAT}&longitude=${TAMPERE_LON}&daily=weathercode,temperature_2m_max,temperature_2m_min,precipitation_probability_max&timezone=Europe/Helsinki&forecast_days=7`
			);
			return await response.json();
		},
		staleTime: 1000 * 60 * 30, // Cache for 30 minutes
	});

	if (isLoading)
		return <div className="p-6 text-center">Loading weather...</div>;
	if (error)
		return (
			<div className="p-6 text-center text-red-600">Error loading weather</div>
		);

	const days = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

	// Calculate weather scores and find worst day
	const weatherScores = data.daily.time.map((date, index) => ({
		index,
		score: calculateWeatherScore(
			Math.round(data.daily.temperature_2m_max[index]),
			data.daily.precipitation_probability_max[index],
			data.daily.weathercode[index]
		),
	}));

	const worstDayIndex = weatherScores.reduce(
		(worst, current) => (current.score > worst.score ? current : worst),
		weatherScores[0]
	).index;

	return (
		<div className="p-6 max-w-4xl mx-auto">
			<div className="mb-6">
				<h1 className="text-3xl font-bold mb-2">7-Day Weather Forecast</h1>
				<p className="text-gray-600">
					Plan your perfect movie day based on the weather
				</p>
			</div>

			<div className="space-y-3">
				{data.daily.time.map((date, index) => {
					const weatherCode = data.daily.weathercode[index];
					const maxTemp = Math.round(data.daily.temperature_2m_max[index]);
					const precipProb = data.daily.precipitation_probability_max[index];
					const weatherInfo = getWeatherInfo(weatherCode);
					const weatherScore = weatherScores[index].score;
					const isWorstDay = index === worstDayIndex;
					const recommendation = getMovieRecommendation(
						weatherScore,
						isWorstDay
					);
					const dateObj = new Date(date);
					const dayName = days[dateObj.getDay()];
					const month = dateObj.toLocaleDateString("en-US", { month: "short" });
					const dayNum = dateObj.getDate();
					const isToday = index === 0;

					return (
						<div
							key={date}
							className={`bg-white rounded-xl shadow border p-4 transition-all hover:shadow-lg ${
								isWorstDay ? "ring-2 ring-red-400" : ""
							}`}
						>
							<div className="flex items-center justify-between">
								<div className="flex items-center gap-4 flex-1">
									<div className="text-4xl">{weatherInfo.icon}</div>
									<div>
										<div className="flex items-center gap-2">
											<span className="font-semibold text-lg">{dayName}</span>
											{isToday && (
												<span className="bg-black text-white text-xs px-2 py-0.5 rounded">
													Today
												</span>
											)}
										</div>
										<div className="text-sm text-gray-600">
											{month} {dayNum}
										</div>
									</div>
								</div>

								<div className="flex items-center gap-8">
									<div className="text-right">
										<div className="text-xs text-gray-600 mb-1">Condition</div>
										<div className="font-medium">{weatherInfo.desc}</div>
									</div>

									<div className="text-right min-w-[100px]">
										<div className="text-3xl font-bold">{maxTemp}°C</div>
										<div className="text-sm text-gray-600">
											{precipProb}% rain
										</div>
									</div>

									<div>
										<span
											className={`${recommendation.color} text-white text-xs font-medium px-3 py-1.5 rounded-full inline-block min-w-[140px] text-center`}
										>
											{recommendation.label}
										</span>
									</div>
								</div>
							</div>
						</div>
					);
				})}
			</div>
		</div>
	);
}
