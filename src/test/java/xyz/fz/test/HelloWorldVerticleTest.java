package xyz.fz.test;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import xyz.fz.docdoc.verticle.HttpVerticle;

@RunWith(VertxUnitRunner.class)
public class HelloWorldVerticleTest {
    private Vertx vertx;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        vertx.deployVerticle(new HttpVerticle(9981), context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testHelloWorldVerticle(TestContext context) {
        final Async async = context.async();

        vertx.createHttpClient().getNow(9981, "localhost", "/404", response -> {
            response.handler(body -> {
                context.assertTrue(body.toString().contains("docdocMapping"));
                async.complete();
            });
        });
    }
}
