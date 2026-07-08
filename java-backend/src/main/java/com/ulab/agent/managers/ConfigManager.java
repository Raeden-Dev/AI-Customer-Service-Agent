package com.ulab.agent.managers;

import com.ulab.agent.Main;
import com.ulab.agent.models.Config;
import com.ulab.agent.utils.FileUtils;
import com.ulab.agent.utils.Lang;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class ConfigManager {

    private Config config;

    @PostConstruct
    public void init() {
        reload();
    }

    public void reload() {
        FileUtils.createDirectory(Main.DATA_DIRECTORY, false);
        Config loaded = FileUtils.loadJsonFile("config.json", Main.CONFIG_FILE, Config.class, true);
        if (loaded == null) {
            config = Config.defaults();
            FileUtils.saveJsonFile("config.json", Main.CONFIG_FILE, config, false);
        } else {
            config = loaded;
        }
        Main.console.setDebugLevel(config.getDebugLevel());
        Main.console.info(String.format(Lang.CONFIG_LOADED, config.getDebugLevel()));
    }

    public Config getConfig() { return config; }

    public void updateConfig(Config newConfig) {
        this.config = newConfig;
        Main.console.setDebugLevel(config.getDebugLevel());
        FileUtils.saveJsonFile("config.json", Main.CONFIG_FILE, config, false);
    }
}
