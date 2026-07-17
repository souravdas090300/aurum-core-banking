# Aurum Core Banking Frontend

React frontend for the Aurum Core Banking prototype.

## Run locally

1. Install dependencies:
   ```bash
   cd frontend
   npm install
   ```

2. Start the app:
   ```bash
   npm start
   ```

3. Run the frontend test suite:
   ```bash
   npm test
   ```

## Environment

The frontend expects the backend API at `http://localhost:8080` by default.
You can override the API base URL with `REACT_APP_API_BASE_URL`.

The default Keycloak configuration targets `http://localhost:8180/auth`, realm `banking`, client `banking-api`.
