package com.spoutouts.acqnet.controller;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.spoutouts.acqnet.WebRouter;
import com.spoutouts.acqnet.support.UserAuth;

public class ReceptionController {
	private final Vertx vertx;
	private final Container container;

	public ReceptionController(Vertx vertx, Container container) {
		this.vertx = vertx;
		this.container = container;
	}

	public void index(YokeRequest req) {
		req.put("pagename", "AcqNet");
		req.response().render("com.spoutouts.acqnet.index.soy");
	}

	public void logoff(YokeRequest req) {
		UserAuth.doLogoff(vertx, container, req, result -> {
			if ("".equals(result)) {
				//logoff succeeded
				req.response().redirect(WebRouter.Routes.ReceptionController$index.path());
			} else {
				//login failed, show the login form again, with an error
				req.put("error", result);
				index(req);
			}
		});
	}
}
