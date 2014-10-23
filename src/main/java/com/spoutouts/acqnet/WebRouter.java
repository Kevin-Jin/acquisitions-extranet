package com.spoutouts.acqnet;

import org.vertx.java.core.Handler;
import org.vertx.java.platform.Verticle;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.BodyParser;
import com.jetdrone.vertx.yoke.middleware.CookieParser;
import com.jetdrone.vertx.yoke.middleware.ErrorHandler;
import com.jetdrone.vertx.yoke.middleware.Logger;
import com.jetdrone.vertx.yoke.middleware.Router;
import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.spoutouts.acqnet.controller.GroupClientController;
import com.spoutouts.acqnet.controller.ReceptionController;
import com.spoutouts.acqnet.support.CookieUtil;
import com.spoutouts.acqnet.support.UserAuth;
import com.spoutouts.acqnet.templating.AssetHandler;
import com.spoutouts.acqnet.templating.ClosureTemplateEngine;
import com.spoutouts.acqnet.templating.YokeMarkupTemplateEngine;

public class WebRouter extends Verticle {
	public static final String
		HOME			= "/",
		BUYER_HOME		= "/buyer/listing",
		SELLER_HOME		= "/seller/listing",
		BANKS_HOME		= "/banks/listing",
		BANKS_LOGIN		= "/banks/user/bind"
	;

	private static final int PORT = 9000;

	@Override
	public void start() {
		String instanceId = container.config().getString("instanceId");

		ReceptionController c0 = new ReceptionController(vertx, container);
		GroupClientController c1 = new GroupClientController(vertx, container);
		//IndividualBuyerController c2 = new IndividualBuyerController(vertx, container);
		//IndividualSellerController c3 = new IndividualSellerController(vertx, container);
		Yoke yoke = new Yoke(this);
		yoke.engine(new ClosureTemplateEngine(container.logger(), "./www/views"));
		yoke.engine(new YokeMarkupTemplateEngine(container.logger(), "./www/views"));
		yoke.use(new ErrorHandler(true));
		yoke.use(new Logger());
		yoke.use(new CookieParser(CookieUtil.COOKIE_SIGNER));
		//TODO: error code 400 after CookieParser should delete all cookies for user
		yoke.use(new BodyParser());
		//yoke.use(new Csrf());
		yoke.use("/assets", new AssetHandler(vertx, container, instanceId, "./www/assets", "./compiled/assets"));
		yoke.use(new Middleware() {
			public void handle(final YokeRequest request, final Handler<Object> next) {
				UserAuth.validateSessionCookie(vertx, container, request, error1 -> {
					UserAuth.loadRememberedClient(vertx, container, request, error2 -> {
						request.put("user", UserAuth.getCurrentUserId(request));
						next.handle(null);
					});
				});
			}
		});
		yoke.use(new Router()
			.get(HOME, c0::index)
			.all("/user/unbind", c0::logoff)
			.get("/banks/?", c1::banks)
			.get(BANKS_LOGIN, c1::banksLogin)
			.post(BANKS_LOGIN, c1::doBanksLogin)
			.all(BANKS_HOME, c1::banksListing)
			.all("/banks/listing/new", c1::banksRegister)
			/*.get("/buyer/?", c2::buyer)
			.get("/buyer/user/bind", c2::buyerLogin)
			.post("/buyer/user/bind", c2::doBuyerLogin)
			.get("/buyer/user/new", c2::buyerRegister)
			.post("/buyer/user/new", c2::doBuyerRegister)
			.all(BUYER_HOME, c2::buyerListing)
			.get("/buyer/details", c2::buyerDetails)
			.get("/seller/?", c3::seller)
			.get("/seller/user/bind", c3::sellerLogin)
			.post("/seller/user/bind", c3::doSellerLogin)
			.get("/seller/user/new", c3::sellerRegister)
			.all(SELLER_HOME, c3::sellerListing)
			.get("/seller/details", c3::sellerDetails)*/
		);
		yoke.listen(PORT, "0.0.0.0");
	}
}
