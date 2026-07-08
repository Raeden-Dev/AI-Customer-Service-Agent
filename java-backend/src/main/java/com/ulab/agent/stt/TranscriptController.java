package com.ulab.agent.stt;

import com.ulab.agent.managers.CallManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
