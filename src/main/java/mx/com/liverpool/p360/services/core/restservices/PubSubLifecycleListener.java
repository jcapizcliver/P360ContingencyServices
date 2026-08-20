package mx.com.liverpool.p360.services.core.restservices;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import mx.com.liverpool.dataprofiling.preparison.envioproductos.PruebaEnvioPubSubMediaAssets;

/** Cierra el Publisher compartido cuando se detiene o redeploya la aplicación. */
@WebListener
public class PubSubLifecycleListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        PruebaEnvioPubSubMediaAssets.closeSharedPublisher();
    }
}
