package config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class UploadInitListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            System.setProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileCountMax", "-1");
            System.setProperty("org.apache.tomcat.util.http.fileupload.impl.FileUploadBase.fileCountMax", "-1");
            System.setProperty("org.apache.tomcat.util.http.Parameters.MAX_COUNT", "20000");
        } catch (Throwable ignored) {
        }
    }
}

