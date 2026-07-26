# Stage 1: Build Angular application
FROM node:20-alpine AS build
WORKDIR /app

# Copy dependency files
COPY package*.json ./
RUN npm ci

# Copy project files and build
COPY . .
RUN npm run build -- --configuration=production

# Stage 2: Serve static content via Nginx
FROM nginx:alpine
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose default HTTP port
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
