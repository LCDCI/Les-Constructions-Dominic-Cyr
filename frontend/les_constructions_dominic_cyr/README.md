# Les Constructions Dominic Cyr — Frontend

This project uses Vite as the development build tool and React for the UI.

## Available Scripts

In the project directory you can run the following scripts (defined in `package.json`):

### `npm run dev`

Starts the Vite development server (hot module replacement). Open `http://localhost:3000` (or the port shown) to view the app.

### `npm run build`

Builds the app for production using Vite. Output is written to the `dist/` directory.

### `npm run preview`

Locally preview the production build. This runs a static server that serves the `dist/` output.

### `npm run lint`

Runs ESLint across the `src/` directory. Use `npm run lint:fix` to apply auto-fixable changes.

### `npm run format`

Runs Prettier to format source files. Use `npm run format:check` to only check formatting.

### `npm test`

Runs the test runner (Vitest) configured for the project.

## Environment variables

Vite exposes environment variables under `import.meta.env`. Project-specific variables should be prefixed with `VITE_`.

- `VITE_API_BASE` — base path used by the frontend API clients (example: `/api/v1/lots` or an absolute URL). You can set this in your environment or when building the app.

Example (PowerShell) to run dev with an override:

```powershell
$env:VITE_API_BASE = '/api/v1/lots'; npm run dev
```

## Images and public assets

Put static assets (images, icons) in the `public/` folder. Files in `public/` are served at the app root, for example `public/images/lots/default.jpg` is available at `/images/lots/default.jpg`.

## Docker / Production notes

The repository includes a `Dockerfile` that builds the Vite app and serves it with nginx. If your backend runs behind a gateway and you rely on path rewrites (e.g. mapping `/lots` → `/api/v1/lots`), make sure the server or reverse-proxy is configured accordingly.

## Troubleshooting

- If you get CORS or proxy errors during development, confirm `VITE_API_BASE` points to the correct backend host/port and use Vite's dev server proxy if needed.
- If an API call fails, check the browser DevTools Network tab to inspect the actual request URL and response.

## Learn more

- Vite: https://vitejs.dev/
- React: https://reactjs.org/

---

If you'd like, I can further tailor this README (add Docker build/run examples, CI steps, or local environment tips).
