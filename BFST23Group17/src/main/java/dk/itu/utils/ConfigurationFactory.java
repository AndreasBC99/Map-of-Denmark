package dk.itu.utils;

import dk.itu.App;

import java.io.InputStream;
import java.util.Properties;

public class ConfigurationFactory {
    private static Properties config;
    private static Properties appStateConfig;

    // Loads all configurations - more configurations can be added in the future
    public static void loadConfigurations() throws RuntimeException {
        ConfigurationFactory.loadConfig();
        ConfigurationFactory.loadAppStateConfig();
    }

    // Loads main configuration file
    private static void loadConfig() {
        try {
            InputStream input = App.class.getClassLoader().getResourceAsStream("config.properties");
            if (input == null) throw new Exception(""); // config.properties not found, throws error
            ConfigurationFactory.config = new Properties();
            ConfigurationFactory.config.load(input);
            input.close();
        } catch (Exception e) {
            throw new RuntimeException("Unable to load config.properties");
        }
    }

    // Loads App State configuration - contains RunTime configurations
    private static void loadAppStateConfig() {
        ConfigurationFactory.appStateConfig = new Properties();
        int defaultColorMode = Integer.parseInt((String) ConfigurationFactory.config.get("app.state.defaults.colorMode"));
        ConfigurationFactory.appStateConfig.put("colorMode", defaultColorMode);
        ThemesFactory.setColorMode(defaultColorMode);
    }

    public static void setColorMode(int colorMode) {
        ConfigurationFactory.appStateConfig.put("colorMode", colorMode);
        ThemesFactory.setColorMode(colorMode);
    }

    // Returns Configuration from file
    public static Properties getConfig() {
        if (ConfigurationFactory.config == null) {
            loadConfigurations();
        }
        return ConfigurationFactory.config;
    }

    // Returns App State Configuration
    public static Properties getAppStateConfig() {
        if (ConfigurationFactory.appStateConfig == null) {
            loadConfigurations();
        }
        return ConfigurationFactory.appStateConfig;
    }

}
