package com.spoutouts.acqnet;

import com.jetdrone.vertx.yoke.middleware.YokeRequest;

public class Controller {
	public void index(YokeRequest req) {
		req.response().render("com.spoutouts.acqnet.index.soy");
	}
}
