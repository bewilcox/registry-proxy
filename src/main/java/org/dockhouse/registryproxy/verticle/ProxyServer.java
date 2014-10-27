package org.dockhouse.registryproxy.verticle;

import com.jetdrone.vertx.yoke.Yoke;
import com.jetdrone.vertx.yoke.middleware.RequestProxy;
import com.jetdrone.vertx.yoke.middleware.Router;
import org.vertx.java.core.Handler;
import org.vertx.java.platform.Verticle;

/**
 * Proxy server for the Docker private registry
 * Created by bewilcox on 23/10/2014.
 */
public class ProxyServer extends Verticle {


    @Override
    public void start() {

        Yoke yoke = new Yoke(vertx);

        yoke.use(new Router()
            .all("^/", req -> {
                System.out.println("handle request " + req.normalizedPath());
                RequestProxy requestProxy = new RequestProxy("localhost", 9999, false);

                requestProxy.handle(req, new Handler<Object>() {
                    @Override
                    public void handle(Object o) {
                        System.out.println("handle Object from RequestProxy");
                        if (o != null) {
                            System.out.printf(o.toString());
                            o.toString();
                        }
                    }
                });
            })
        );

        yoke.listen(5001);

        Yoke yoke2 = new Yoke(vertx);
        yoke2.use(new Router().all("/", req -> {
            System.out.println("Client receive proxing res");
            req.response().end("This is the response ");
        })).listen(9999);


    }
}
