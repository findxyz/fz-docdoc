package xyz.fz.docdoc;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import xyz.fz.docdoc.configuration.ComponentConfiguration;
import xyz.fz.docdoc.configuration.DataSourceConfiguration;
import xyz.fz.docdoc.run.H2InitRunner;
import xyz.fz.docdoc.service.ServiceReplyFactory;
import xyz.fz.docdoc.util.BaseProperties;
import xyz.fz.docdoc.util.ThreadUtil;
import xyz.fz.docdoc.verticle.HttpVerticle;
import xyz.fz.docdoc.verticle.ServiceVerticle;

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

        /* init service reply factory */
        ServiceReplyFactory.init(context);

        /* deploy verticles */
        final Vertx vertx = Vertx.vertx();
        DeploymentOptions workerDeploymentOptions = new DeploymentOptions();
        workerDeploymentOptions.setWorker(true);
        vertx.deployVerticle(new ServiceVerticle(), workerDeploymentOptions);
        vertx.deployVerticle(new HttpVerticle());

        LOGGER.info("Deployment done. Server is running at port: {}", BaseProperties.get("server.port"));
    }
}
