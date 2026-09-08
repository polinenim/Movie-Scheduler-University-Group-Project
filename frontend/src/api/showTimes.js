import mockjson from "./showTimesMock.json";

const USE_MOCK = false; // switch to false to call real API

export async function fetchShowtimes() {
	const query = `
    query {
     allMovies {
				nodes {
					title
					imdbUrl
					showtimesByMovieId {
						edges {
							node {
								showDatetime
								theatreName
								buyUrl
							}
						}
					}
				}
			}
    }
  `;

	// --------------------------
	//   MOCK MODE
	// --------------------------
	if (USE_MOCK) {
		return mockjson.data.allMovies.nodes; // correct path for your mock
	}

	// --------------------------
	//   REAL API MODE
	// --------------------------
	const res = await fetch("https://api.jeanbaptistevanparys.be/graphql", {
		method: "POST",
		headers: { "Content-Type": "application/json" },
		body: JSON.stringify({ query }),
	});

	const json = await res.json();

	if (json.errors) {
		console.error(json.errors);
		throw new Error("GraphQL query failed");
	}

	return json.data.allMovies.nodes;
}

