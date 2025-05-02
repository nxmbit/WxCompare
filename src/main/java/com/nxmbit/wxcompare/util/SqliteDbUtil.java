package com.nxmbit.wxcompare.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.ResourceBundle;

public class SqliteDbUtil {
    private static SessionFactory sessionFactory;

    private SqliteDbUtil() {
    }

    public static synchronized SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();

                Properties hibernateProps = loadDatabaseProperties();
                processPlaceholders(hibernateProps);
                ensureDbDirectoryExists(hibernateProps.getProperty("hibernate.connection.url"));

                configuration.setProperties(hibernateProps);

                // Add entity classes
                configuration.addAnnotatedClass(com.nxmbit.wxcompare.model.User.class);
                configuration.addAnnotatedClass(com.nxmbit.wxcompare.model.Location.class);

                sessionFactory = configuration.buildSessionFactory();
            } catch (Exception e) {
                throw new ExceptionInInitializerError("Failed to initialize Hibernate: " + e.getMessage());
            }
        }
        return sessionFactory;
    }

    private static Properties loadDatabaseProperties() {
        Properties hibernateProps = new Properties();
        ResourceBundle bundle = ResourceBundle.getBundle("com.nxmbit.wxcompare.application");

        bundle.keySet().stream()
                .filter(key -> key.startsWith("hibernate."))
                .forEach(key -> hibernateProps.setProperty(key, bundle.getString(key)));

        return hibernateProps;
    }

    private static void processPlaceholders(Properties properties) {
        String url = properties.getProperty("hibernate.connection.url");
        if (url != null && url.contains("${user.home}")) {
            String userHome = System.getProperty("user.home");
            url = url.replace("${user.home}", userHome);
            properties.setProperty("hibernate.connection.url", url);
        }
    }

    private static void ensureDbDirectoryExists(String jdbcUrl) {
        if (jdbcUrl != null && jdbcUrl.startsWith("jdbc:sqlite:")) {
            String filePath = jdbcUrl.substring("jdbc:sqlite:".length());
            Path dbPath = Paths.get(filePath);
            Path dbDir = dbPath.getParent();

            if (dbDir != null && !Files.exists(dbDir)) {
                try {
                    Files.createDirectories(dbDir);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
        }
    }
}