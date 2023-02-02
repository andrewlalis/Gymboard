# Gymboard CDN
A content delivery and management system for Gymboard, which exposes endpoints for uploading and fetching content.

This service stores file content in a directory defined by the `app.files.storage-dir` configuration property. Within the storage directory, files are stored like so: `/<year>/<month>/<day>/<file>`.
