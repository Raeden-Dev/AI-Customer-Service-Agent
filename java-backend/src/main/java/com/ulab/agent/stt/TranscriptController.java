package com.ulab.agent.stt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class TranscriptController {
    @PostMapping("/transcript")
    public ResponseEntity<String> receiveTranscript(@RequestBody TranscriptRequest request) {
        String userSpeech = request.getTranscript();

        if (request.isFinal()) {
            System.out.println("\n[Java Backend] Final session transcript from Python: " + userSpeech);
        } else {
            System.out.println("\n[Java Backend] Received text chunk from Python: " + userSpeech);
        }
        return ResponseEntity.ok("Text successfully received by Java");
    }
}