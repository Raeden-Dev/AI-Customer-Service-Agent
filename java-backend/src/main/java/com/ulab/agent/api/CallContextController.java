package com.ulab.agent.api;

import com.ulab.agent.ai.CallMode;
import com.ulab.agent.managers.BusinessManager;
import com.ulab.agent.managers.CallManager;
import com.ulab.agent.managers.IntelligenceManager;
import com.ulab.agent.models.Business;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The call-scenario API used by the Python AI:
 *
 *   GET  /api/call-context — everything about the current call: which mode it
 *        is in, the mode instructions, the business knowledge and the client
 *        profile (as ready-to-paste text), and the greeting line.
 *
 *   POST /api/call-mode — Python reports a mode switch here when the AI
 *        flags [MODE:WRONG_NUMBER] or [MODE:COMPLEX_REQUEST] in a reply.
 */
@RestController
@RequestMapping("/api")
public class CallContextController {

    private final CallManager callManager;
    private final BusinessManager businessManager;
    private final IntelligenceManager intelligenceManager;

    public CallContextController(CallManager callManager,
                                 BusinessManager businessManager,
                                 IntelligenceManager intelligenceManager) {
        this.callManager = callManager;
        this.businessManager = businessManager;
        this.intelligenceManager = intelligenceManager;
    }

    @GetMapping("/call-context")
    public ResponseEntity<CallContextResponse> getCallContext() {
        CallContextResponse response = new CallContextResponse();
        Business active = businessManager.getActiveBusiness();

        response.setCallActive(callManager.isCallActive());
        response.setModeSwitchInstructions(CallMode.MODE_SWITCH_INSTRUCTIONS);

        // Default to NEW_CUSTOMER so Python always gets usable instructions,
        // even if it asks before a call has started.
        CallMode mode = callManager.getActiveMode() != null ? callManager.getActiveMode() : CallMode.NEW_CUSTOMER;
        response.setMode(mode.name());
        response.setModeName(mode.getDisplayName());
        response.setModeInstructions(mode.getAiInstructions());

        if (active != null) {
            response.setBusinessName(active.getBusinessName());
            response.setBusinessInfo(intelligenceManager.buildBusinessInfoText(active.getBusinessName()));
        } else {
            response.setBusinessName("");
            response.setBusinessInfo("");
        }

        response.setClientInfo(intelligenceManager.buildClientInfoText(callManager.getActiveClient()));
        response.setGreeting(callManager.getGreeting() == null ? "" : callManager.getGreeting());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/call-mode")
    public ResponseEntity<String> changeCallMode(@RequestBody CallModeRequest request) {
        CallMode mode = CallMode.fromString(request.getMode());
        if (mode == null) {
            return ResponseEntity.badRequest().body("Unknown mode: " + request.getMode());
        }
        String reason = request.getReason() == null || request.getReason().isBlank()
                ? "reported by AI" : request.getReason();
        boolean changed = callManager.changeMode(mode, reason);
        return changed ? ResponseEntity.ok("Mode changed")
                       : ResponseEntity.badRequest().body("No active call");
    }
}
