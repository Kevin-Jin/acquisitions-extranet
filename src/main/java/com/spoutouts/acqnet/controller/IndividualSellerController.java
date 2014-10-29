package com.spoutouts.acqnet.controller;

import org.vertx.java.core.Vertx;
import org.vertx.java.platform.Container;

import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.spoutouts.acqnet.WebRouter;
import com.spoutouts.acqnet.support.UserAuth;

public class IndividualSellerController {
	private final Vertx vertx;
	private final Container container;

	public IndividualSellerController(Vertx vertx, Container container) {
		this.vertx = vertx;
		this.container = container;
	}


	public void seller(YokeRequest req) {
		req.put("pagename", "Seller");
		req.response().render("com.spoutouts.acqnet.seller.index.soy");
	}

	public void sellerLogin(YokeRequest req) {
		req.put("pagename", "Seller Login");
		req.response().render("com.spoutouts.acqnet.seller.login.soy");
	}

	public void doSellerLogin(YokeRequest req) {
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
							req.response().redirect(WebRouter.Routes.IndividualSellerController$sellerListing.path());
						} else {
							//persistent login failed, show the login form again, with an error
							req.put("error", result);
							sellerLogin(req);
						}
					});
				} else {
					req.response().redirect(WebRouter.Routes.IndividualSellerController$sellerListing.path());
				}
			} else {
				//login failed, show the login form again, with an error
				req.put("error", result);
				sellerLogin(req);
			}
		});
	}

	public void sellerRegister(YokeRequest req) {
		req.put("pagename", "Seller Register");
		req.response().render("com.spoutouts.acqnet.seller.register.soy");
	}

	public void sellerListing(YokeRequest req) {
		req.put("pagename", "Seller Listing");
		req.response().render("com.spoutouts.acqnet.seller.listing.soy");
	}

	public void sellerDetails(YokeRequest req) {
		req.put("pagename", "Seller Details");
		req.response().render("com.spoutouts.acqnet.seller.details.soy");
	}
}
