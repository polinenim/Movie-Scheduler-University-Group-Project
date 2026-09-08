# 🎬 Movie Scheduler

**Prototype Version:** 0.1  
**Last Updated:** November 2025  
**Authors:** Java null pointer exception

---

## 📖 Overview

**Movie Scheduler** helps users decide **what to watch** and **when/where to watch it** — combining:

- Movie metadata (OMDb)
- Local cinema showtimes (Scraped Data on public api: https://api.jeanbaptistevanparys.be/graphiql)
- 7-day weather forecasts (Open-Meteo)

Users can plan a **home movie night** or a **cinema visit** based on convenient showtimes and weather conditions.

📋 **[View Full Design Document](design/DesignDocument.md)** - Complete technical architecture, design decisions, and system specifications.

---

## 🚀 Features (Prototype Scope)

- 🔍 **Search movies** via OMDb API (title, plot, cast, ratings)
- 🧾 **Manage a personal Watchlist** (quick add/remove)
- 🎬 **View current cinema showtimes** grouped by date and theatre with movie posters
- 🗓️ **Schedule movies** for home or cinema viewing
- ⭐ **Mark movies as seen** with notes and personal ratings
- 🌦️ **7-day weather forecast** with smart movie recommendations
  - Shows daily weather conditions, temperature, and precipitation probability for Tampere
  - Automatically identifies the worst weather day as **"Best for movies!"**
  - Perfect for planning indoor movie nights on cold/rainy days
- 📊 **Watchlist Ratings Comparison Chart**
  - Visualize IMDB/Rotten Tomatoes/Metacritic ratings side-by-side
  - Quickly compare movies you plan to watch
  - Helpful for choosing what to watch next
- ⏱️ **Watched Movies Timeline Chart**
  - Displays all movies you've watched in chronological order
  - Highlights streaks, monthly activity, and viewing patterns
  - Great for tracking your movie-watching habits over time
- 🔐 **User authentication** (JWT-based)

---

## 🧩 Tech Stack

| Layer            | Technology                                                                |
| ---------------- | ------------------------------------------------------------------------- |
| **Frontend**     | React 18 + Vite, Tailwind CSS, TanStack Query                             |
| **Backend**      | Java 17 + Vert.x, MySQL, Flyway, JWT Auth                                 |
| **Build & Test** | Maven, JUnit 5, Testcontainers                                            |
| **APIs**         | OMDb (movie metadata), Scrapper GraphQL (showtimes), Open-Meteo (weather) |

---

## 🐳 Docker (frontend + backend + MySQL)

1. Make sure Docker is running before starting, ensure **Docker Desktop** (or Docker Engine) is running.
2. Copy the sample env to create an env file in the root: `cp .env.example .env` and adjust the secrets (e.g., `JWT_SECRET`, `DB_PASS`, `MYSQL_ROOT_PASSWORD`).
3. Build and run: `docker compose up --build`.
4. The following services should have started:
   - Backend: http://localhost:${BACKEND_PORT:-8080}
   - Frontend: http://localhost:${FRONTEND_PORT:-4173} (served via `vite preview`)
   - MySQL: localhost:${MYSQL_PORT:-3306} (db/user/pass from `.env`)
5. Data:
   - MySQL persists in the `mysql-data` volume.
   - Backend uses MySQL via `JDBC_URL`; defaults point to the compose `mysql` service.

## 🖥️ Run Without Docker (frontend + backend)

1. Ensure you have the required tools installed:
   - **Java 17+**
   - **Maven**
   - **Node.js 18+** and **npm**
   - **vite**
2. To start the **backend**:
   1. `cd backend`
   2. Build the project:  
      `mvn clean package`
   3. Run the backend JAR:  
      `java -jar target/CineSchedule-1.0-SNAPSHOT.jar`
3. To start the **frontend** (in a second terminal window):
   1. `cd frontend`
   2. Install dependencies:  
      `npm install`
   3. Run the dev server:  
      `npm vite dev`
4. Services will now be available at:
   - Backend: http://localhost:${BACKEND_PORT:-8080}
   - Frontend Dev Server: http://localhost:5173 (default Vite dev port)

Environment variables (override in `.env`):

- `BACKEND_PORT`, `SERVER_PORT`
- `FRONTEND_PORT`
- `JDBC_URL`, `DB_USER`, `DB_PASS`
- `JWT_SECRET`
- `MYSQL_DATABASE`, `MYSQL_ROOT_PASSWORD`, `MYSQL_PORT`
- `VITE_API_BASE_URL` (frontend build-time API base — point it at the backend service/host)
- `VITE_OMDB_API_KEY` (OMDb API key for fetching movie posters)

---

## 👥 Test Users

For testing purposes, you can use these pre-configured accounts:

| Name    | Email                      | Password     | Description                          |
| ------- | -------------------------- | ------------ | ------------------------------------ |
| Alice   | `alice@cineschedule.com`   | `alice123`   | Test user with sample watchlist data |
| Bob     | `bob@cineschedule.com`     | `bob123`     | Test user with watched movies        |
| Charlie | `charlie@cineschedule.com` | `charlie123` | Test user with timeline data         |

**Note:** These are test credentials only. In production, always use strong, unique passwords and never commit credentials to version control.

---

## 🎭 Key Pages

### Now Showing

- Browse current and upcoming movie showtimes from Finnkino cinemas
- Showtimes grouped by date and theatre for easy navigation
- Movie posters fetched automatically from OMDb using IMDb IDs
- Click on showtimes with available booking links to purchase tickets
- Only shows dates from today onwards

### Temperature / Weather Forecast

- 7-day weather forecast for Tampere using Open-Meteo API
- Daily weather conditions with emoji icons, temperature, and precipitation probability
- Smart recommendation algorithm that scores weather based on:
  - Temperature deviation from 20°C (comfortable room temperature)
  - Precipitation probability
  - Weather severity (rain, snow, storms)
- The **worst weather day** is automatically highlighted as "Best for movies!"
- Perfect for planning cozy indoor movie nights during cold or rainy weather

### My List

- Personal watchlist for tracking movies you want to see
- Search and add movies from OMDb database
- Mark movies as watched with personal ratings
- Remove movies from your list

---

## 🗂️ Repository Structure

```plaintext
java.lang.nullpointerexception/
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── ...
├── frontend/
│   ├── src/
│   ├── vite.config.js
│   └── ...
├── design/
│   └── DesignDocument.md
└── README.md
```
