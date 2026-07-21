package com.ulab.agent.api;

import com.ulab.agent.managers.CallManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/transcript — the full session transcript.
 * Python sends this once, when the call is being shut down (isFinal = true).
 * Not a live channel; the live channel is /api/chat-message.
 */
@RestController
@RequestMapping("/api")
public class TranscriptController {

    private final CallManager callManager;

    public TranscriptController(CallManager callManager) {
        this.callManager = callManager;
    }

    @PostMapping("/transcript")
    public ResponseEntity<String> receiveTranscript(@RequestBody TranscriptRequest request) {
        callManager.handleTranscript(request.getTranscript(), request.getTranslatedTranscript(), request.isFinal());
        return ResponseEntity.ok("Text successfully received by Java");
    }
}
