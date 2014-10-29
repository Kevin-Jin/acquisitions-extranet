package com.spoutouts.acqnet.controller;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.spoutouts.acqnet.WebRouter;
import com.spoutouts.acqnet.support.UserAuth;

public class IndividualBuyerController {
	private final Vertx vertx;
	private final Container container;

	public IndividualBuyerController(Vertx vertx, Container container) {
		this.vertx = vertx;
		this.container = container;
	}

	public void buyer(YokeRequest req) {
		req.put("pagename", "Buyer");
		req.response().render("com.spoutouts.acqnet.buyer.index.soy");
	}

	public void buyerLogin(YokeRequest req) {
		req.put("pagename", "Buyer Login");
		req.response().render("com.spoutouts.acqnet.buyer.login.soy");
	}

	public void buyerRegister(YokeRequest req) {
		req.put("pagename", "Buyer Register");
		req.response().render("com.spoutouts.acqnet.buyer.register.soy");
	}

	public void doBuyerLogin(YokeRequest req) {
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
							req.response().redirect(WebRouter.Routes.IndividualBuyerController$buyerListing.path());
						} else {
							//persistent login failed, show the login form again, with an error
							req.put("error", result);
							buyerLogin(req);
						}
					});
				} else {
					req.response().redirect(WebRouter.Routes.IndividualBuyerController$buyerListing.path());
				}
			} else {
				//login failed, show the login form again, with an error
				req.put("error", result);
				buyerLogin(req);
			}
		});
	}

	public void doBuyerRegister(YokeRequest req) {
		req.put("pagename", "Buyer Register");
		req.response().render("com.spoutouts.acqnet.buyer.register.soy");
	}

	public void buyerListing(YokeRequest req) {
		req.put("pagename", "Buyer Listing");
		req.response().render("com.spoutouts.acqnet.buyer.listing.soy");
	}

	public void buyerDetails(YokeRequest req) {
		req.put("pagename", "Buyer Details");
		req.response().render("com.spoutouts.acqnet.buyer.details.soy");
	}
}
