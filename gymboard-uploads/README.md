# Gymboard Uploads

A service for handling large file uploads and processing. This service manages user-submitted video files, processing of the video files, and rapidly serving the raw file content.

## Upload Logical Flow

When a user is creating a new Gymboard submission, they need to upload their video, which must be processed to compress and prepare it for web usage. The following series of steps describe how this is done.

1. User uploads raw video to **gymboard-uploads**, receives an `upload-id`.
2. User posts the submission, including the `upload-id`, to **gymboard-api**.
3. **gymboard-api** validates the submission, then sends a request to **gymboard-uploads** to start processing the video identified by `upload-id`, and marks the submission as _processing_.
4. **gymboard-uploads** processes the video, and upon completion, sends a request to **gymboard-api** indicating that the processed video is available at `video-url`.
5. **gymboard-api** updates the user's submission to include the `video-url`, and marks the submission as no longer _processing_.

Therefore, **gymboard-uploads** is responsible for the following actions:

- Receiving raw video uploads from users and storing them temporarily until **gymboard-api** requests that we start processing, or they'll be discarded.
- Processing of raw videos to reduce file size and optimize for web. This will most likely be done using FFMPEG in the backend.
- Reporting the status of video processing for recently uploaded videos.
- Serving processed video files.

**THIS PROJECT IS A WORK-IN-PROGRESS** Do not use it for production.
