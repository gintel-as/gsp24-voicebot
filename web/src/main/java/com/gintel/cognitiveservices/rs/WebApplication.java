package com.gintel.cognitiveservices.rs;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gintel.cognitiveservices.config.WebConfig;
import com.gintel.cognitiveservices.example.ExampleController;
import com.gintel.cognitiveservices.example.rs.ExampleResource;
import com.gintel.cognitiveservices.rs.filters.LogRequestFilter;

@ApplicationPath("/")
public class WebApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(WebApplication.class);

    @Context
    private ServletContext servletContext;

    public WebApplication() {
        logger.info("Starting rest services");
    }

    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> objects = new HashSet<>();
        objects.add(GenericExceptionHandler.class);
        return objects;
    }

    @Override
    public Set<Object> getSingletons() {
        ConfigFactory.setProperty("config_file", "web.properties");
        final WebConfig config = ConfigFactory.create(WebConfig.class);
        final ExampleResource authResource = new ExampleResource(new ExampleController(config));
        final LogRequestFilter logRequestFilter = new LogRequestFilter();

        Set<Object> singletons = new HashSet<>();
        singletons.add(authResource);
        singletons.add(logRequestFilter);
        return singletons;
    }
}
