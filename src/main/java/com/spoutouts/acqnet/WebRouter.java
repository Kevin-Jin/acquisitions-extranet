package com.spoutouts.acqnet;

import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.platform.Verticle;

public class WebRouter extends Verticle {
	private static final int PORT = 8080;

	@Override
	public void start() {
		final String instanceId = container.config().getString("instanceId");

		HttpServer server = vertx.createHttpServer();
		RouteMatcher routeMatcher = new RouteMatcher();

		final Controller controller = new Controller(container, vertx);
		routeMatcher.get("/", controller::index);
		routeMatcher.get("/assets/.*", new AssetHandler(container, vertx, instanceId));
		routeMatcher.noMatch(req -> req.response().setStatusCode(404).end("No endpoint."));

		server.requestHandler(routeMatcher).listen(PORT, "0.0.0.0");
	}
}
