package org.dockhouse.registryproxy.verticle;

import org.dockhouse.registryproxy.communication.CommunicationUtils;
import org.vertx.java.core.http.HttpClient;
import org.vertx.java.core.http.HttpClientRequest;
import org.vertx.java.platform.Verticle;

/**
 * Proxy server for the Docker private registry
 * Created by bewilcox on 23/10/2014.
 */
public class ProxyServer extends Verticle {


    @Override
    public void start() {

        // Private registry Host
        final String registryHost = this.container.env().getOrDefault(CommunicationUtils.PRIVATE_REGISTRY_HOST, "localhost");
        final String registryPort = this.container.env().getOrDefault(CommunicationUtils.PRIVATE_REGISTRY_PORT, "5000");

        // Init the Http client for the private registry Docker
        this.container.logger().info("Connection initialisation to the Docker private registry {" + registryHost+":"+registryPort+"}");
        final HttpClient registryHttpClient = vertx.createHttpClient().setHost(registryHost).setPort(Integer.parseInt(registryPort));

        // Init the Proxy server
        this.container.logger().info("Initialisation of the proxy server");
        vertx.createHttpServer().requestHandler(requestReceiveByProxy -> {
            this.container.logger().info("Proxing request : " + requestReceiveByProxy.uri());

            // Pass the request to the private registry and process the response
            final HttpClientRequest requestForRegistry = registryHttpClient.request(requestReceiveByProxy.method(), requestReceiveByProxy.uri(), responseFromRegistry -> {
                this.container.logger().info("Processing response " + responseFromRegistry.statusCode());
                requestReceiveByProxy.response().setStatusCode(responseFromRegistry.statusCode());
                requestReceiveByProxy.response().headers().set(responseFromRegistry.headers());
                requestReceiveByProxy.response().setChunked(true);

                // Pass the registry datas to the client
                responseFromRegistry.dataHandler(data -> {
                    this.container.logger().info("Proxying response body : " + data);
                    requestReceiveByProxy.response().write(data);
                });

                // Flag the end of the registry response
                responseFromRegistry.endHandler(aVoid -> requestReceiveByProxy.response().end());
            });

            // Process the request received by the proxy server and process the request for the private registry
            requestForRegistry.headers().set(requestReceiveByProxy.headers());
            requestForRegistry.setChunked(true);
            requestReceiveByProxy.dataHandler( data ->  {
                this.container.logger().info("Proxing request body" + data);
                requestForRegistry.write(data);
            });
            requestReceiveByProxy.endHandler(aVoid -> {
                this.container.logger().info("end of the request received by the proxy");
                requestForRegistry.end();
            });

        }).listen(5001);

    }
}
