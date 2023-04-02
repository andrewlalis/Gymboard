import std.stdio;
import handy_httpd;
import handy_httpd.handlers.path_delegating_handler;
import slf4d;
import slf4d.default_provider;

import handlers;

void main() {
	// Configure logging
	auto provider = new shared DefaultProvider(true, Levels.TRACE);
	configureLoggingProvider(provider);

	PathDelegatingHandler pathHandler = new PathDelegatingHandler();
	pathHandler.addMapping("GET", "/status", (ref HttpRequestContext ctx) {
		ctx.response.writeBodyString("online");
	});
	pathHandler.addMapping("POST", "/uploads", new VideoUploadHandler());
	pathHandler.addMapping("POST", "/uploads/{uploadId}/process", new VideoProcessingHandler());

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
