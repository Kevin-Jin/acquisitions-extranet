package com.spoutouts.acqnet;

import org.vertx.java.platform.Verticle;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.ErrorHandler;
import com.jetdrone.vertx.yoke.middleware.Logger;
import com.jetdrone.vertx.yoke.middleware.Router;

public class WebRouter extends Verticle {
	private static final int PORT = 8080;

	@Override
	public void start() {
		String instanceId = container.config().getString("instanceId");

		Controller c = new Controller();
		Yoke yoke = new Yoke(this);
		yoke.engine(new ClosureTemplateEngine(container.logger(), "./www/views"));
		yoke.use(new Logger());
		yoke.use(new ErrorHandler(true));
		yoke.use("/assets", new AssetHandler(vertx, container, instanceId, "./www/assets", "./compiled/assets"));
		yoke.use(new Router().get("/", c::index));
		yoke.listen(PORT, "0.0.0.0");
	}
}
