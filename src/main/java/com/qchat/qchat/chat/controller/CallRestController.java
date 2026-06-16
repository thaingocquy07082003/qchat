package com.qchat.qchat.chat.controller;

import com.qchat.qchat.chat.dto.response.CallLogResponse;
import com.qchat.qchat.chat.service.CallService;
import com.qchat.qchat.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations/{conversationId}/calls")
@RequiredArgsConstructor
public class CallRestController {

    private final CallService callService;

    @GetMapping
    public ResponseEntity<List<CallLogResponse>> getCallHistory(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(callService.getCallHistory(conversationId, userDetails.getId()));
    }
}
