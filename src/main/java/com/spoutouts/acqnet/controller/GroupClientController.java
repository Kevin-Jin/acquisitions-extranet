package com.spoutouts.acqnet.controller;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.spoutouts.acqnet.WebRouter;
import com.spoutouts.acqnet.support.UserAuth;

public class GroupClientController {
	private final Vertx vertx;
	private final Container container;

	public GroupClientController(Vertx vertx, Container container) {
		this.vertx = vertx;
		this.container = container;
	}

	public void banks(YokeRequest req) {
		req.response().redirect(WebRouter.BANKS_LOGIN);
	}

	public void banksLogin(YokeRequest req) {
		req.put("pagename", "Client Area");
		req.response().render("com.spoutouts.acqnet.banks.index.soy");
	}

	public void doBanksLogin(YokeRequest req) {
		String email = req.getFormParameter("email");
		String password = req.getFormParameter("password");
		String rememberMe = req.getFormParameter("persistent");
		UserAuth.doLogin(vertx, container, req, email, password, result -> {
			if ("".equals(result)) {
				//login succeeded, show the home page
				if ("1".equals(rememberMe)) {
					UserAuth.createPersistentLoginCookie(vertx, container, req, v -> {
						if ("".equals(v)) {
							//persistent login succeeded, show the home page
							req.response().redirect(WebRouter.BANKS_HOME);
						} else {
							//persistent login failed, show the login form again, with an error
							req.put("error", result);
							banksLogin(req);
						}
					});
				} else {
					req.response().redirect(WebRouter.BANKS_HOME);
				}
			} else {
				//login failed, show the login form again, with an error
				req.put("error", result);
				banksLogin(req);
			}
		});
	}

	public void banksListing(YokeRequest req) {
		req.put("pagename", "Client Area");
		req.response().render("com.spoutouts.acqnet.banks.listing.soy");
	}

	public void banksRegister(YokeRequest req) {
		req.put("pagename", "Client Register");
		req.response().render("com.spoutouts.acqnet.banks.register.soy");
	}
}
