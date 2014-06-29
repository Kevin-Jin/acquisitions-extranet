package com.spoutouts.acqnet;

import java.util.UUID;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class Boot extends Verticle {
	@Override
	public void start() {
		final String instanceId = UUID.randomUUID().toString();

		JsonObject config = new JsonObject();
		config.putString("instanceId", instanceId);
		container.deployWorkerVerticle(CompilerVerticle.class.getCanonicalName(), config, 2);
		container.deployVerticle(WebRouter.class.getCanonicalName(), config, Runtime.getRuntime().availableProcessors());
	}
}
