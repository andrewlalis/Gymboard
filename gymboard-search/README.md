# Gymboard Search

A simple search API for Gymboard, backed by Apache Lucene. This application includes both indexing of Gyms and other searchable entities, and a public web interface for searching those indexes.

This application is configured with read-only access to the central Gymboard database, for its indexing operations.

## Developing

Currently, this application is designed to boot up and immediately read the latest data from the Gymboard API's database to rebuild its indexes.
