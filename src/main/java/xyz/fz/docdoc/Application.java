package xyz.fz.docdoc;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
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

        /* deploy verticles */
        final Vertx vertx = Vertx.vertx();

        // business verticles
        int cpuCores = Runtime.getRuntime().availableProcessors();
        DeploymentOptions workerDeploymentOptions = new DeploymentOptions();
        workerDeploymentOptions.setWorker(true);
        workerDeploymentOptions.setWorkerPoolSize(cpuCores * 4);
        for (int i = 0; i < cpuCores * 2; i++) {
            // can't use workerDeploymentOptions.setInstances(cpuCores * 2) because of new Object not reflect Object
            vertx.deployVerticle(new ServiceVerticle(context), workerDeploymentOptions);
        }

        // http verticle
        int serverPort = Integer.parseInt(Objects.requireNonNull(context.getEnvironment().getProperty("server.port")));
        vertx.deployVerticle(new HttpVerticle(serverPort));

        LOGGER.info("Deployment done. Server is running at port: {}", serverPort);
    }
}
