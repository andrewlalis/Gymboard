module handlers;

import handy_httpd;
import std.conv : to;

const ulong MAX_UPLOAD_SIZE = 1024 * 1024 * 1024;

class VideoUploadHandler : HttpRequestHandler {
    public void handle(ref HttpRequestContext ctx) {
        if ("Content-Length" !in ctx.request.headers) {
            ctx.response.status = 411;
            ctx.response.statusText = "Length Required";
            return;
        }

        ulong contentLength = ctx.request.headers["Content-Length"].to!ulong;
        if (contentLength == 0 || contentLength > MAX_UPLOAD_SIZE) {
            ctx.response.status = 413;
            ctx.response.statusText = "Payload Too Large";
            return;
        }

        // TODO: Implement this!
    }
}