package com.spoutouts.acqnet;

import java.util.UUID;

import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import com.spoutouts.acqnet.support.BCryptWorker;
import com.spoutouts.acqnet.templating.CompilerVerticle;

public class Boot extends Verticle {
	public static final String DB_HANDLE = "io.vertx~mod-mysql-postgresql";
	public static final String BC_HANDLE = "bcrypt";

	@Override
	public void start() {
		final String instanceId = UUID.randomUUID().toString();

		JsonObject config = new JsonObject();
		config.putString("address", DB_HANDLE);
		config.putString("connection", "MySQL");
		config.putString("host", "localhost");
		config.putNumber("port", Integer.valueOf(3306));
		config.putString("username", "root");
		config.putString("password", null);
		config.putString("database", "acqnet");
		container.deployModule("io.vertx~mod-mysql-postgresql_2.10~0.4.0-SNAPSHOT", config);

		config = new JsonObject();
		config.putString("address", BC_HANDLE);
		config.putNumber("log_rounds", Integer.valueOf(10));
		container.deployWorkerVerticle(BCryptWorker.class.getCanonicalName(), config, 2);

		config = new JsonObject();
		config.putString("instanceId", instanceId);
		container.deployWorkerVerticle(CompilerVerticle.class.getCanonicalName(), config, 2);
		container.deployVerticle(WebRouter.class.getCanonicalName(), config, Runtime.getRuntime().availableProcessors());
	}
}
