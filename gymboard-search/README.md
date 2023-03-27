# Gymboard Search

A simple search API for Gymboard, backed by Apache Lucene. This application includes both indexing of Gyms and other searchable entities, and a public web interface for searching those indexes.

This application is configured with read-only access to the central Gymboard database for its indexing operations.

## Developing

Currently, this application is designed to boot up and immediately read the latest data from the Gymboard API's database to rebuild its indexes, then continue to do so at scheduled intervals.

Indexes are stored in the `indexes/` directory.

**Note:** At some point, it would be a lot more efficient to have this application listen for entity update events published by other services, instead of fully scanning the entire database for each index operation. But for now, that's not needed.
