# Gymboard App

The responsive web application for Gymboard. This is a Vue 3 + Quasar project that's written in Typescript.

## Developing

In order to develop this app, you'll most likely want to start a local instance of each Gymboard service that it relies on. Each service should expose a `./start-dev.sh` script that you can call to quickly boot it up, but refer to each service's README for more detailed information, like generating sample data.

### Install the dependencies

```bash
yarn
# or
npm install
```

### Start the app in development mode (hot-code reloading, error reporting, etc.)

```bash
quasar dev
```

### Lint the files

```bash
yarn lint
# or
npm run lint
```

### Format the files

```bash
yarn format
# or
npm run format
```

### Build the app for production

```bash
quasar build
```

### Customize the configuration

See [Configuring quasar.config.js](https://v2.quasar.dev/quasar-cli-vite/quasar-config-js).

## Structure

Within `src/`, there are a few key directories whose functions are listed below:

- `src/api/` contains a Typescript client implementation for consuming the various APIs from Gymboard services such as the CDN, Search, and main API. The main API (`src/api/main/index.ts`) can be imported with `import api from 'src/api/main';`. While the other APIs may differ based on their usage, generally the main API is structured as a hierarchy of classes with `readonly` properties to act as namespaces.
- `src/components/` contains auxiliary Vue components that help in building pages.
- `src/pages/` contains all the pages as Vue components.
- `src/i18n` contains translation files as JSON objects for all supported languages.
