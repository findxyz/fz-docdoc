package xyz.fz.docdoc;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import xyz.fz.docdoc.configuration.DataSourceConfiguration;
import xyz.fz.docdoc.configuration.ServiceConfiguration;
import xyz.fz.docdoc.run.H2InitRunner;
import xyz.fz.docdoc.util.BaseProperties;
import xyz.fz.docdoc.util.ThreadUtil;
import xyz.fz.docdoc.verticle.ServerVerticle;
import xyz.fz.docdoc.verticle.UserVerticle;

public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                DataSourceConfiguration.class,
                ServiceConfiguration.class
        );
        ThreadUtil.execute(new H2InitRunner());
        LOGGER.debug("Spring context is running: {}", context.isRunning());
        boolean proEnv = "pro".equals(BaseProperties.get("server.mode"));
        final Vertx vertx = Vertx.vertx();
        DeploymentOptions workerDeploymentOptions = new DeploymentOptions();
        workerDeploymentOptions.setWorker(true);
        vertx.deployVerticle(new ServerVerticle(proEnv));
        vertx.deployVerticle(new UserVerticle(), workerDeploymentOptions);
        LOGGER.info("Deployment done. Server is running at port: {}", BaseProperties.get("server.port"));
    }
}
