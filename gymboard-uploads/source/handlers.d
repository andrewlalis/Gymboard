module handlers;

import handy_httpd;
import slf4d;
import std.conv : to;
import std.path;
import std.file;
import std.uuid;
import std.json;
import std.stdio;

static immutable MAX_UPLOAD_SIZE = 1024 * 1024 * 1024;
static immutable ALLOWED_MEDIA_TYPES = ["video/mp4"];
static immutable TEMP_UPLOADS_DIR = "temp-uploads";

class VideoUploadHandler : HttpRequestHandler {
    public void handle(ref HttpRequestContext ctx) {
        if (!validateHeaders(ctx)) return;

        if (!exists(TEMP_UPLOADS_DIR)) mkdir(TEMP_UPLOADS_DIR);

        UUID uploadId = sha1UUID("gymboard-uploads");

        ctx.request.readBodyToFile(getTempFilePath(uploadId));
        JSONValue metadataObj = JSONValue(string[string].init); // Empty object.
        string originalFilename = ctx.request.getHeader("X-GYMBOARD-FILENAME");
        if (originalFilename is null) {
            originalFilename = "unnamed.mp4";
        }
        metadataObj.object["filename"] = originalFilename;
        File f = File(getTempFileMetadataPath(uploadId), "w");
        f.write(metadataObj.toPrettyString());
        f.close();

        infoF!"Saved uploaded video file with id %s."(uploadId.toString);

        ctx.response.setStatus(HttpStatus.CREATED);
        ctx.response.writeBodyString(uploadId.toString());
    }

    private bool validateHeaders(ref HttpRequestContext ctx) {
        ulong contentLength = ctx.request.getHeaderAs!ulong("Content-Length");
        if (contentLength == 0) {
            ctx.response.status = HttpStatus.LENGTH_REQUIRED;
            return false;
        } else if (contentLength > MAX_UPLOAD_SIZE) {
            ctx.response.status = HttpStatus.PAYLOAD_TOO_LARGE;
            return false;
        }

        import std.algorithm : canFind;
        string contentType = ctx.request.getHeader("Content-Type");
        if (contentType is null || !canFind(ALLOWED_MEDIA_TYPES, contentType)) {
            ctx.response.status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
            return false;
        }

        return true;
    }

    private string getTempFilePath(const ref UUID uploadId) {
        return buildPath(TEMP_UPLOADS_DIR, uploadId.toString());
    }

    private string getTempFileMetadataPath(const ref UUID uploadId) {
        return buildPath(TEMP_UPLOADS_DIR, uploadId.toString() ~ "_meta.json");
    }
}

class VideoProcessingHandler : HttpRequestHandler {
    public void handle(ref HttpRequestContext ctx) {
        string uploadIdStr = ctx.request.getPathParamAs!string("uploadId");
        infoF!"Processing upload %s"(uploadIdStr);
    }
}
