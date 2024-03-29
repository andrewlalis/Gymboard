# Gymboard API

An HTTP/REST API powered by Java and Spring Boot. This API serves as the main entrypoint for all data processing, and is generally the first point-of-contact for the web app or other services that consume Gymboard data.

## Development

To get started, follow these steps:
1. Clone the repository, and open this project it in your editor. (Open the pom.xml file if you're using a maven-aware editor).
2. Run `./gen_keys.d` to generate the keys that will be used by this application for signing JWT tokens. (Requires a D lang installation).
3. Make sure the *gymboard-cdn* service is running locally.
4. Boot up the project.

### Sample Data

To ease development, `nl.andrewlalis.gymboard_api.util.SampleDataLoader` will run on startup and populate the database with some sample entities. You can regenerate this data by manually deleting the database, and deleting the `.sample_data` marker file that's generated in the project directory.

You should have the *gymboard-cdn* project running when starting up this API, since the sample data includes videos that will be uploaded as part of some sample submissions.

## ULIDs

For entities that don't need a human-readable primary key (or keys), we choose to use [ULID](https://github.com/ulid/spec) strings, which are like UUIDs, but use a timestamp based preamble such that their values are monotonically increasing, and lexicographically ordered by creation time. The result is a pseudorandom string of 26 characters which appears random to a human, yet is efficient as a primary key. It's also near-impossible for automated systems to guess previous/next ids.
