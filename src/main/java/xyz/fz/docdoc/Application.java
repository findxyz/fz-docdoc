package xyz.fz.docdoc;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import xyz.fz.docdoc.configuration.ComponentConfiguration;
import xyz.fz.docdoc.configuration.DataSourceConfiguration;
import xyz.fz.docdoc.run.H2InitRunner;
import xyz.fz.docdoc.util.ThreadUtil;
import xyz.fz.docdoc.verticle.HttpVerticle;
import xyz.fz.docdoc.verticle.ServiceVerticle;

import java.util.Objects;

public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {

        /* init spring context */
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                DataSourceConfiguration.class,
                ComponentConfiguration.class
        );

        LOGGER.debug("Spring context is running: {}", context.isRunning());

        /* init h2database */
        ThreadUtil.execute(new H2InitRunner(context));

        System.setProperty("vertx.disableFileCaching", Objects.requireNonNull(context.getEnvironment().getProperty("vertx.disableFileCaching")));

        /* deploy verticles */
        // default VertxOptions's shared worker pool size is 20 (see VertxOptions)
        final Vertx vertx = Vertx.vertx();

        int serverPort = Integer.parseInt(Objects.requireNonNull(context.getEnvironment().getProperty("server.port")));
        DeploymentOptions workerDeploymentOptions = new DeploymentOptions();
        // use default shared worker pool if work pool name not set (see vertx.deploy src)
        workerDeploymentOptions.setWorker(true);
        for (int i = 0; i < VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE; i++) {
            // one verticle instance bind one event loop thread

            // business verticles
            // can't use workerDeploymentOptions.setInstances(cpuCores * 2) because of new Object not reflect Object
            vertx.deployVerticle(new ServiceVerticle(context), workerDeploymentOptions);

            // http verticle
            // see HttpServer.listen src --> sharedHttpServer (one acceptor and more worker)
            vertx.deployVerticle(new HttpVerticle(serverPort));
        }

        LOGGER.info("Deployment done. Server is running at port: {}", serverPort);
    }
}
