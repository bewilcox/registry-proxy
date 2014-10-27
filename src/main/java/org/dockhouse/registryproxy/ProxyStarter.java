
package org.dockhouse.registryproxy;

import org.dockhouse.registryproxy.verticle.ProxyServer;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Start the application environment.
 * @author bewilcox
 *
 */
public class ProxyStarter extends Verticle {

    /**
     * Main Method to deploy all the needed Verticle for the application.
     */
	@Override
	public void start() {
		
		//Load config for all modules
		JsonObject appConfig = this.container.config();
		// Config for the http verticle
		JsonObject proxyConfig = appConfig.getObject(ProxyServer.class.getSimpleName());

        // Number of instances to deploy
        int nbInstances = proxyConfig.getInteger("instances");

		// Deploy the Private registry Proxy
		this.container.deployVerticle(ProxyServer.class.getCanonicalName(), proxyConfig, nbInstances,asyncResult -> {
            if (asyncResult.succeeded()) {
                container.logger().info("The ProxyServer has been deployed : deployment ID is " + asyncResult.result());
            } else {
                container.logger().error(asyncResult.cause().getMessage());
            }
        });
	}
}
