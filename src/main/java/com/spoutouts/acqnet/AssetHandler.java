package com.spoutouts.acqnet;

import java.util.HashMap;
import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.file.FileSystem;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.platform.Container;

import com.jetdrone.vertx.mods.bson.BSON;

//TODO: implement caching. see https://gist.github.com/Ryan-ZA/8375100
public class AssetHandler implements Handler<HttpServerRequest> {
	private static final int COMPILE_TIMEOUT = 3000;

	private final Container container;
	private final Vertx vertx;
	private final String instanceId;

	public AssetHandler(Container container, Vertx vertx, String instanceId) {
		this.container = container;
		this.vertx = vertx;
		this.instanceId = instanceId;
	}

	private void compile(HttpServerRequest req, String source, String oldExtension, String newExtension, Handler<String> success, Handler<String> fail, boolean[] isRespClosed) {
		FileSystem fileSystem = vertx.fileSystem();
		String dest = source.substring(0, source.length() - oldExtension.length()) + newExtension;
		String address = CompilerVerticle.class.getCanonicalName() + oldExtension;
		if (fileSystem.existsSync(dest) && (!fileSystem.existsSync(source) || fileSystem.propsSync(dest).lastModifiedTime().getTime() >= fileSystem.propsSync(source).lastModifiedTime().getTime())) {
			//already compiled at its latest version
			if (isRespClosed[0])
				return;

			success.handle(dest);
		} else if (vertx.sharedData().getMap(address + "[" + instanceId + "]").putIfAbsent(source, Boolean.TRUE) != null) {
			//compile in progress
			long[] timer = new long[1];
			Handler<Message<Boolean>> finishedHandler = result -> {
				vertx.cancelTimer(timer[0]);
				if (isRespClosed[0])
					return;

				if (result.body().booleanValue())
					success.handle(dest);
				else
					fail.handle("Compile failed");
			};
			timer[0] = vertx.setTimer(COMPILE_TIMEOUT, id -> {
				vertx.eventBus().unregisterHandler(address + ".done[" + dest + "]", finishedHandler);
				if (isRespClosed[0])
					return;

				if (!vertx.sharedData().getMap(address + "[" + instanceId + "]").containsKey(source)) {
					//most likely a race condition where compiler wasn't done yet
					//when we retrieved alreadyLoading, but finished before we
					//registered finishedHandler
					success.handle(dest);
				} else {
					container.logger().warn("Did not receive a compiled copy of \"" + source + "\" in a timely manner");
					fail.handle("Compile timed out");
				}
			});
			vertx.eventBus().registerLocalHandler(address + ".done[" + dest + "]", finishedHandler);
		} else if (!fileSystem.existsSync(dest) || fileSystem.propsSync(dest).lastModifiedTime().getTime() < fileSystem.propsSync(source).lastModifiedTime().getTime()) {
			//compile not initiated
			Map<String, Object> bson = new HashMap<String, Object>();
			bson.put("source", source);
			bson.put("dest", dest);
			vertx.eventBus().sendWithTimeout(address, BSON.encode(bson), COMPILE_TIMEOUT, (AsyncResult<Message<Boolean>> event) -> {
				if (isRespClosed[0])
					return;

				if (event.succeeded()) {
					if (event.result().body().booleanValue())
						success.handle(dest);
					else
						fail.handle("Compile failed");
				} else {
					container.logger().warn("Did not receive a compiled copy of \"" + source + "\" in a timely manner");
					fail.handle("Compile timed out");
				}
			});
		}
	}

	private void baseFileReady(HttpServerRequest req, String basePath, boolean[] isRespClosed) {
		boolean js = basePath.endsWith(".js");
		boolean css = basePath.endsWith(".css");
		boolean minifiedPreferable = req.query() != null && req.query().contains("min");
		//String altPath;
		if (js && basePath.endsWith(".min.js") || css && basePath.endsWith(".min.css"))
			//client wants the raw minified file without further processing
			/*if (!vertx.fileSystem().existsSync(basePath))
				//.min file doesn't exist but source file does, so minify it
				compile(req, altPath = basePath.substring(0, basePath.lastIndexOf(".min")) + basePath.substring(basePath.lastIndexOf(".")), altPath.substring(altPath.lastIndexOf(".")), altPath.substring(altPath.lastIndexOf(".min")), dest -> req.response().sendFile(dest), reason -> req.response().setStatusCode(500).end("Failed to compile minified file"), isRespClosed);
			else
				//bypass modification time checks on source file if client explicitly requested min file*/
				req.response().sendFile(basePath);
		else if (js && minifiedPreferable)
			//client wants the source file in minified form. make sure minified file is up to date
			compile(req, basePath, ".js", ".min.js", dest -> req.response().sendFile(dest), reason -> req.response().sendFile(basePath), isRespClosed);
		else if (css && minifiedPreferable)
			//client wants the source file in minified form. make sure minified file is up to date
			compile(req, basePath, ".css", ".min.css", dest -> req.response().sendFile(dest), reason -> req.response().sendFile(basePath), isRespClosed);
		else
			//no minified files requested or none available
			req.response().sendFile(basePath);
	}

	@Override
	public void handle(HttpServerRequest req) {
		boolean[] isRespClosed = { false };
		req.response().closeHandler(v -> isRespClosed[0] = true);
		String basePath = "./www" + req.path();
		String altPath;
		if (basePath.endsWith(".css") && vertx.fileSystem().existsSync(altPath = basePath.substring(0, basePath.length() - (/*basePath.endsWith(".min.css") ? ".min.css".length() : */".css".length())) + ".less"))
			//even if the .css file exists, call compile to ensure it's modified after .less file was 
			compile(req, altPath, ".less", ".css", dest -> baseFileReady(req, basePath, isRespClosed), reason -> req.response().setStatusCode(500).end("Failed to compile LESS to CSS"), isRespClosed);
		else if (vertx.fileSystem().existsSync(basePath)/* || (basePath.endsWith(".min.css") || basePath.endsWith(".min.js")) && vertx.fileSystem().existsSync(basePath.substring(0, basePath.lastIndexOf(".min")) + basePath.substring(basePath.lastIndexOf(".")))*/)
			//file exists
			baseFileReady(req, basePath, isRespClosed);
		else
			//the file does not exist
			req.response().setStatusCode(404).end();
   }
}