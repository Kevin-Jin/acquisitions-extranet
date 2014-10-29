package com.spoutouts.acqnet;

import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.CookieDecoder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.vertx.java.core.Handler;
import org.vertx.java.platform.Verticle;

import com.jetdrone.vertx.yoke.Middleware;
import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.core.YokeCookie;
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

import dk.brics.automaton.BasicOperations;
import dk.brics.automaton.RegExp;

public class WebRouter extends Verticle {
	public enum Routes {
		ReceptionController$index					("GET",		"/"),
		ReceptionController$logoff					("GET",		"/user/unbind"),
		GroupClientController$banks					("GET",		"/banks/?"),
		GroupClientController$banksListing			("GET",		"/banks/listing"),
		GroupClientController$banksLogin			("GET",		"/banks/user/bind"),
		GroupClientController$doBanksLogin			("POST",	"/banks/user/bind"),
		GroupClientController$banksRegister			("GET",		"/banks/listing/new"),
		GroupClientController$doBanksRegister		("POST",	"/banks/listing/new"),
		IndividualBuyerController$buyer				("GET",		"/buyer/?"),
		IndividualBuyerController$buyerLogin		("GET",		"/buyer/user/bind"),
		IndividualBuyerController$doBuyerLogin		("POST",	"/buyer/user/bind"),
		IndividualBuyerController$buyerRegister		("GET",		"/buyer/user/new"),
		IndividualBuyerController$doBuyerRegister	("POST",	"/buyer/user/new"),
		IndividualBuyerController$buyerListing		("GET",		"/buyer/listing"),
		IndividualBuyerController$buyerDetails		("GET",		"/buyer/details"),
		IndividualSellerController$seller			("GET",		"/seller/?"),
		IndividualSellerController$sellerLogin		("GET",		"/seller/user/bind"),
		IndividualSellerController$doSellerLogin	("GET",		"/seller/user/bind"),
		IndividualSellerController$sellerRegister	("GET",		"/seller/user/new"),
		IndividualSellerController$sellerListing	("GET",		"/seller/listing"),
		IndividualSellerController$sellerDetails	("GET",		"/seller/details")
		;

		public static final String WEB_ROOT = "";
		public static final Map<String, List<String>> forSoy;

		private static final String VALID_CAPTURE_GROUP = "[A-Za-z][A-Za-z0-9_]*";
		private static final Pattern VALID_CAPTURE_GROUP_PATTERN = Pattern.compile(VALID_CAPTURE_GROUP);

		static {
			Map<String, List<String>> collection = new HashMap<>();
			for (Routes page : Routes.values())
				collection.put(page.name(), Arrays.asList(page.verb.toLowerCase(), WEB_ROOT + page.path()));
			forSoy = Collections.unmodifiableMap(collection);
		}

		public final String verb;
		private final String pathPattern;

		private Routes(String verb, String path) {
			this.verb = verb;
			//turn any standard regex named capture groups into Yoke's :<token name> form
			//this is to simplify path(), so that we make only a single call to
			//replaceAll and so that Automaton doesn't trip (the library doesn't
			//support regex capture groups).
			this.pathPattern = path.replaceAll(Pattern.quote("(?<") + "(" + VALID_CAPTURE_GROUP + ")" + Pattern.quote(">[^/]+)"), ":$1");
		}

		/**
		 * Find shortest match to pathPattern in which certain values must be in the capturing groups.
		 * @param pathParams the values for the capturing groups
		 * @return a URL path
		 */
		public String path(String... pathParams) {
			String transformed = pathPattern;

			//replace the named capture groups with the passed values
			for (int i = 0; i + 1 < pathParams.length; i += 2) {
				String captureGroup = pathParams[i];
				String replacement = pathParams[i + 1];
				if (!VALID_CAPTURE_GROUP_PATTERN.matcher(captureGroup).matches())
					continue;

				transformed = transformed.replaceAll(":" + captureGroup + "(?![A-Za-z0-9_])", replacement);
			}

			//find the shortest match to the regex
			transformed = BasicOperations.getShortestExample(new RegExp(transformed).toAutomaton(), true);

			return transformed;
		}
	}

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
		yoke.use(new CookieParser(CookieUtil.COOKIE_SIGNER) {
			@Override
			public void handle(final YokeRequest request, final Handler<Object> next) {
				boolean invalid = false;
				String cookieHeader = request.getHeader("cookie");
				if (cookieHeader != null) {
					for (Cookie cookie : CookieDecoder.decode(cookieHeader)) {
						if (new YokeCookie(cookie, CookieUtil.COOKIE_SIGNER).getUnsignedValue() == null) {
							CookieUtil.discardCookie(request, cookie.getName());
							invalid = true;
						}
					}
				}
				if (invalid)
					next.handle(400);
				else
					super.handle(request, next);
			}
		});
		yoke.use(new BodyParser());
		yoke.use("/assets", new AssetHandler(vertx, container, instanceId, "./www/assets", "./compiled/assets"));
		yoke.use(new Middleware() {
			@Override
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
			.get(Routes.ReceptionController$index.pathPattern, c0::index)
			.all(Routes.ReceptionController$logoff.pathPattern, c0::logoff)
			.get(Routes.GroupClientController$banks.pathPattern, c1::banks)
			.get(Routes.GroupClientController$banksLogin.pathPattern, c1::banksLogin)
			.post(Routes.GroupClientController$doBanksLogin.pathPattern, c1::doBanksLogin)
			.all(Routes.GroupClientController$banksListing.pathPattern, c1::banksListing)
			.get(Routes.GroupClientController$banksRegister.pathPattern, c1::banksRegister)
			.post(Routes.GroupClientController$doBanksRegister.pathPattern, c1::doBanksRegister)
			/*.get(Routes.IndividualBuyerController$buyer.pathPattern, c2::buyer)
			.get(Routes.IndividualBuyerController$buyerLogin.pathPattern, c2::buyerLogin)
			.post(Routes.IndividualBuyerController$doBuyerLogin.pathPattern, c2::doBuyerLogin)
			.get(Routes.IndividualBuyerController$buyerRegister.pathPattern, c2::buyerRegister)
			.post(Routes.IndividualBuyerController$doBuyerRegister.pathPattern, c2::doBuyerRegister)
			.all(Routes.IndividualBuyerController$buyerListing.pathPattern, c2::buyerListing)
			.get(Routes.IndividualBuyerController$buyerDetails.pathPattern, c2::buyerDetails)
			.get(Routes.IndividualSellerController$seller.pathPattern, c3::seller)
			.get(Routes.IndividualSellerController$sellerLogin.pathPattern, c3::sellerLogin)
			.post(Routes.IndividualSellerController$doSellerLogin.pathPattern, c3::doSellerLogin)
			.get(Routes.IndividualSellerController$sellerRegister.pathPattern, c3::sellerRegister)
			.all(Routes.IndividualSellerController$sellerListing.pathPattern, c3::sellerListing)
			.get(Routes.IndividualSellerController$sellerDetails.pathPattern, c3::sellerDetails)*/
		);
		yoke.listen(PORT, "0.0.0.0");
	}
}
