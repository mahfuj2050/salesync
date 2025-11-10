package com.business.salesync;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
   scanBasePackages = {"com.business.salesync"}
)
@EntityScan(
   basePackages = {"com.business.salesync.models"}
)
@EnableJpaRepositories(
   basePackages = {"com.business.salesync.repository"}
)
public class SalesyncApplication {
   private static int port;

   public static void main(String[] args) {
      System.setProperty("java.awt.headless", "false");
      SpringApplication app = new SpringApplication(new Class[]{SalesyncApplication.class});
      app.setDefaultProperties(Map.of("server.port", "8080"));
      app.run(args);
   }

   @EventListener
   public void onWebServerReady(WebServerInitializedEvent event) {
      port = event.getWebServer().getPort();
      String url = "http://localhost:" + port + "/";
      System.out.println("✅ Server started successfully on port: " + port);
      System.out.println("\ud83c\udf10 Opening browser at: " + url);
      this.openBrowser(url);
   }

   private void openBrowser(String url) {
      try {
         if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI(url));
            return;
         }

         String os = System.getProperty("os.name").toLowerCase();
         if (os.contains("linux")) {
            Runtime.getRuntime().exec(new String[]{"xdg-open", url});
         } else if (os.contains("mac")) {
            Runtime.getRuntime().exec(new String[]{"open", url});
         } else if (os.contains("win")) {
            Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler", url});
         }
      } catch (RuntimeException | IOException var3) {
         System.err.println("⚠️ Unable to open browser automatically: " + var3.getMessage());
      } catch (Exception var4) {
         var4.printStackTrace();
      }

   }
}
