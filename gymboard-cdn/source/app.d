import handy_httpd;
import handy_httpd.handlers.path_delegating_handler;
import dpq2;
import std.stdio;

const DB_URL = "host=127.0.0.1 port=5433 dbname=gymboard-cdn-dev user=gymboard-cdn-dev password=testpass";

void main() {
	ServerConfig config = ServerConfig.defaultValues();
	config.port = 8082;
	config.reuseAddress = true;
	config.workerPoolSize = 10;
	PathDelegatingHandler handler = new PathDelegatingHandler();
	handler.addMapping("GET", "/uploads/{uploadId}", new VideoFetcher());
	handler.addMapping("POST", "/uploads", new VideoUploader());
	HttpServer server = new HttpServer(handler, config);
	server.start();
}

Connection connectToDb() {
	return new Connection(DB_URL);
}

/** 
 * Handler that fetches a video by its id (if it exists).
 */
class VideoFetcher : HttpRequestHandler {
	void handle(ref HttpRequestContext ctx) {
		long uploadId = ctx.request.getPathParamAs!ulong("uploadId", 0);
		writeln(uploadId);
		auto conn = connectToDb();
		QueryParams p;
		p.sqlCommand = "SELECT 'hello world';";
		auto answer = conn.execParams(p);
		scope(exit) destroy(answer);
		writeln(answer[0][0].as!PGtext);
	}
}

/** 
 * Handler that processes a video upload.
 */
class VideoUploader : HttpRequestHandler {
	void handle(ref HttpRequestContext ctx) {

	}
}
