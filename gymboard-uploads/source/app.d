import std.stdio;
import handy_httpd;
import handy_httpd.handlers.path_delegating_handler;

import handlers;

void main() {
	PathDelegatingHandler pathHandler = new PathDelegatingHandler();
	pathHandler.addMapping("GET", "/status", (ref HttpRequestContext ctx) {
		ctx.response.writeBodyString("online");
	});
	pathHandler.addMapping("POST", "/uploads", new VideoUploadHandler());

	HttpServer server = new HttpServer(pathHandler, getServerConfig());
	server.start();
}

private ServerConfig getServerConfig() {
	ServerConfig serverConfig = ServerConfig.defaultValues();
	serverConfig.port = 8085;
	serverConfig.workerPoolSize = 10;
	serverConfig.reuseAddress = true;
	return serverConfig;
}
