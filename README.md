# Gymboard
Leaderboards and social lifting for your local community gym.

Gymboard is a platform for sharing videos of your gym lifts with the world,
from your local gym to the world's stage.

## Architecture Overview

Gymboard is designed as a sort of hybrid architecture combining the best of
microservices and monoliths. Here's a short list of each project that makes
up the Gymboard ecosystem, and what they do:

| Project | Type | Description |
| ---     | ---  | ---         |
| 💻 **gymboard-app** | TS, VueJS, Quasar | A front-end web application for users accessing Gymboard. |
| 🧬 **gymboard-api** | Java, Spring | The *main* backend service that the app talks to. Includes auth and most business logic. |
| 🔍 **gymboard-search** | Java, Lucene | An indexing and searching service that scrapes data from *gymboard-api* to build Lucene search indexes. |
| 🗂 **gymboard-cdn** | Java, Spring | A minimal content-delivery service that manages general file storage, including video processing. |
| 🛠 **gymboard-cli** | D | **WIP** command-line-interface for managing all services for development and deployment. |
| 📸 **gymboard-uploads** | D, Handy-Httpd | **WIP** dedicated service for video upload processing, to extract functionality from *gymboard-cdn*. |

## Development

Gymboard is comprised of a variety of components, each in its own directory,
and with its own project format. Follow the instructions in the README of the
respective project to set that one up.

A `docker-compose.yml` file is defined in this directory, and it defines a set
of services that may be used by one or more services. Install docker on your
system if you haven't already, and run `docker-compose up -d` to start the
services.

**WIP:**
A `build_apps.d` script is available to try and build all projects and collect
their artifacts in a `build/` directory for deployment.
> Eventually, this functionality will be merged into *gymboard-cli*.

### Local Environment

The requirements to develop each project depend of course on the type of
project. But in general, the following software recommendations should hold:

| Type | Requirements |
| ---  | ---          |
| **Java** | [Latest LTS version](https://adoptium.net/en-GB/temurin/releases/), latest Maven version |
| **VueJS** | Vue 3, with a recent version of NodeJS and NPM or similar. |
| **D** | [D toolchain](https://dlang.org/download.html) (compiler + dub) for D version >= 2.103, DMD recommended compiler |

[Docker](https://www.docker.com/) is recommended for running local dependencies
like DB, mail server, message queues, etc.
