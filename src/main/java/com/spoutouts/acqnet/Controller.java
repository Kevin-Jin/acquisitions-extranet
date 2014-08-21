package com.spoutouts.acqnet;

import com.jetdrone.vertx.yoke.middleware.YokeRequest;

public class Controller {
	public void index(YokeRequest req) {
		req.put("pagename", "AcqNet");
		req.response().render("com.spoutouts.acqnet.index.soy");
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

	public void buyerListing(YokeRequest req) {
		req.put("pagename", "Buyer Listing");
		req.response().render("com.spoutouts.acqnet.buyer.listing.soy");
	}

	public void buyerDetails(YokeRequest req) {
		req.put("pagename", "Buyer Details");
		req.response().render("com.spoutouts.acqnet.buyer.details.soy");
	}

	public void seller(YokeRequest req) {
		req.put("pagename", "Seller");
		req.response().render("com.spoutouts.acqnet.seller.index.soy");
	}

	public void sellerLogin(YokeRequest req) {
		req.put("pagename", "Seller Login");
		req.response().render("com.spoutouts.acqnet.seller.login.soy");
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

	public void banks(YokeRequest req) {
		req.put("pagename", "Client Area");
		req.response().render("com.spoutouts.acqnet.banks.index.soy");
	}

	public void banksListing(YokeRequest req) {
		req.put("pagename", "Client Area");
		req.response().render("com.spoutouts.acqnet.banks.listing.soy");
	}
}
