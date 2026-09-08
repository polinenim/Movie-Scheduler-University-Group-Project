
# Cineschedule Frontend

This is a frontend application built using  **React**  and  **Vite**. It has basic routes, a login page, weather details page, chart visualisations on watchlist data and pages to manage watchlists and schedules.

## Tools

-   **React**: A JavaScript library for building user interfaces.
-   **Vite**: A fast build tool that provides an optimized development and production workflow.
-   **Tailwind CSS**: A utility-first CSS framework for styling.
-   **React Query (TanStack Query)**: For managing server state and API calls.
-   **Environment Variables**: Used to configure API keys and base URLs.

## Prerequisites

Before building or running the application, ensure the following are installed:

-   **Node.js**  (v16 or higher)
-   **npm**  (Node Package Manager) or  **yarn**

## Environment Variables

This project uses environment variables to manage configuration settings. Create a  .env  file in the root directory based on the  `.env.example`  file and set the following variables:

-   `VITE_OMDB_API_KEY`: API key for accessing the OMDB API. You can obtain an API key by registering on the  OMDB website.
-   `VITE_API_BASE_URL`: The base URL for the backend API.

Example  .env  file:

## Install dependencies

1.  `npm install`
    
## Development

To start the development server with Hot Module Replacement (HMR):
    `npm run dev`

This will start the app on  `http://localhost:5173`  (or another port if  `5173`  is in use).

## Build for Production

To build the app for production: `npm run build`

This will generate the production-ready files in the  `dist`  folder.

## Preview Production Build

To preview the production build locally: `npm run preview`

This will serve the production build on a local server.

## Folder Structure

-   **`src/`**: Contains all the source code for the application.
    -   **`api/`**: API hooks and mock data.
    -   **`components/`**: Reusable React components.
    -   **`context/`**: Context providers for global state management.
    -   **`routes/`**: React components for different pages/routes.
-   **`public/`**: Static assets like images and icons.
-   **`index.html`**: The main HTML file for the app.

## Key Dependencies

-   **React**: For building the user interface.
-   **Vite**: For development and build tooling.
-   **Tailwind CSS**: For styling.
-   **React Query**: For API state management.
-   **Chart.js**: For data visualisations.