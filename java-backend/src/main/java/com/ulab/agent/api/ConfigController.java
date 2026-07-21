package com.ulab.agent.api;

import com.ulab.agent.managers.ConfigManager;
import com.ulab.agent.models.Config;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET/POST /api/config — the global settings.
 * Python calls GET at the start of every call to learn the STT + TTS settings.
 */
@RestController
@RequestMapping("/api")
public class ConfigController {

    private final ConfigManager configManager;

    public ConfigController(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @GetMapping("/config")
    public ResponseEntity<Config> getConfig() {
        return ResponseEntity.ok(configManager.getConfig());
    }

    @PostMapping("/config")
    public ResponseEntity<Config> updateConfig(@RequestBody Config config) {
        configManager.updateConfig(config);
        return ResponseEntity.ok(configManager.getConfig());
    }
}
