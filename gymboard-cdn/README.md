# Gymboard-CDN

To avoid having to do expensive and complex computations in the main Gymboard API to handle user video uploads, that functionality is provided by this _Gymboard-CDN_ project.

This CDN offers basic, unauthenticated upload and download capabilities, as well as asynchronous processing of video files to reduce disk usage.

It offers the following endpoints:
- `POST /uploads` - Upload files here.
- `GET /uploads/{id}` - Fetch uploaded files.
