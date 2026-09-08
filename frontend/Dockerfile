FROM node:20-alpine
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

# Set build-time environment variables
# the defaults are set to localhost for local development
# when the env file is not present during build time the defaults will be used
ARG VITE_API_BASE_URL=http://localhost:8080
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

ARG VITE_OMDB_API_KEY=d2991ad0
ENV VITE_OMDB_API_KEY=${VITE_OMDB_API_KEY}

# Build the static site
RUN npm run build

EXPOSE 4173

# Serve the built assets with Vite preview
CMD ["npm", "run", "preview", "--", "--host", "0.0.0.0", "--port", "4173"]
