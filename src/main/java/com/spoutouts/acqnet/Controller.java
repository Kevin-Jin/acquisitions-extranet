package com.spoutouts.acqnet;

import groovy.text.SimpleTemplateEngine;

import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.file.FileSystem;
import org.vertx.java.core.file.FileSystemException;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Container;

public class Controller {
	private final Logger logger;
	private final FileSystem fileSystem;

	public Controller(Container container, Vertx vertx) {
		logger = container.logger();
		fileSystem = vertx.fileSystem();
	}

	private void renderTemplate(final HttpServerResponse resp, String template, final Object... parameters) {
		/*fileSystem.open("./www/views/" + template, null, true, false, false, event -> {
			
		});*/
		fileSystem.readFile("./www/views/" + template, event -> {
			if (event.succeeded()) {
				String fileContents = event.result().getString(0, event.result().length());
				try {
					Map<Object, Object> bindings = new HashMap<Object, Object>();
					if (parameters.length % 2 != 0)
						throw new IllegalArgumentException("Template parameters is not divisible by two. Values must be listed after keys.");
					for (int i = 0; i < parameters.length; i += 2)
						bindings.put(parameters[i], parameters[i + 1]);
					resp.end(new SimpleTemplateEngine().createTemplate(fileContents).make(bindings).toString());
				} catch (CompilationFailedException | ClassNotFoundException | IOException e) {
					logger.warn("Failed to parse template file", e);
					resp.setStatusCode(500).end("The request failed");
				}
			} else if (((FileSystemException) event.cause()).getCause() instanceof NoSuchFileException) {
				resp.setStatusCode(404).end("File not found");
			} else {
				logger.warn("Failed to read template file", event.cause());
				resp.setStatusCode(500).end("The request failed");
			}
		});
	}

	public void index(final HttpServerRequest req) {
		renderTemplate(req.response(), "index.groovy.html", "title", "Front Page");
	}
}
