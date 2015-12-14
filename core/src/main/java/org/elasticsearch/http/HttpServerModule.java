/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.http;

import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.http.netty.NettyHttpServerTransport;

import java.util.Objects;

/**
 *
 */
public class HttpServerModule extends AbstractModule {

    private final Settings settings;
    private final ESLogger logger;

    private Class<? extends HttpServerTransport> httpServerTransportClass;

    public HttpServerModule(Settings settings) {
        this.settings = settings;
        this.logger = Loggers.getLogger(getClass(), settings);
        this.httpServerTransportClass = NettyHttpServerTransport.class;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected void configure() {
        Class<? extends HttpServerTransport> httpServerTransport = null;
        String http_type = settings.get("http.type");
        if (http_type != null) {
            try {
                httpServerTransport = (Class<? extends HttpServerTransport>) Class.forName(http_type);
            }
            catch (ClassNotFoundException e) {
                logger.error("Can´t use [{}] as http transport. Class Not found Exception.", http_type);
            }
        }
        else {
            httpServerTransport = NettyHttpServerTransport.class;
        }
        
        this.httpServerTransportClass = httpServerTransport;
        logger.info("Using [{}] as http transport.", this.httpServerTransportClass.getName());
        
        bind(HttpServerTransport.class).to(httpServerTransportClass).asEagerSingleton();
        bind(HttpServer.class).asEagerSingleton();
    }

    public void setHttpServerTransport(Class<? extends HttpServerTransport> httpServerTransport, String source) {
        Objects.requireNonNull(httpServerTransport, "Configured http server transport may not be null");
        Objects.requireNonNull(source, "Plugin, that changes transport may not be null");
        logger.info("Using [{}] as http transport, overridden by [{}]", httpServerTransportClass.getName(), source);
        this.httpServerTransportClass = httpServerTransport;
    }
}
