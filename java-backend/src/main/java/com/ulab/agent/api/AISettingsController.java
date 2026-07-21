package com.ulab.agent.api;

import com.ulab.agent.managers.AISettingsManager;
import com.ulab.agent.models.AISettings;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * GET/POST /api/ai-settings — the AI persona of the active business
 * (model name, role instructions, reply instructions, organization info).
 */
@RestController
@RequestMapping("/api")
public class AISettingsController {

    private final AISettingsManager aiSettingsManager;

    public AISettingsController(AISettingsManager aiSettingsManager) {
        this.aiSettingsManager = aiSettingsManager;
    }

    @GetMapping("/ai-settings")
    public ResponseEntity<AISettings> getSettings() {
        return ResponseEntity.ok(aiSettingsManager.getActiveSettings());
    }

    @PostMapping("/ai-settings")
    public ResponseEntity<AISettings> updateSettings(@RequestBody AISettings settings) {
        aiSettingsManager.updateActiveSettings(settings);
        return ResponseEntity.ok(aiSettingsManager.getActiveSettings());
    }
}
