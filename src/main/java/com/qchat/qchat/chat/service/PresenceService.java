package com.qchat.qchat.chat.service;

import com.qchat.qchat.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceService {

    private static final String SESSION_KEY   = "ws:session:";
    private static final String PRESENCE_CNT  = "presence:count:";
    private static final String PRESENCE_ONLINE = "presence:online:";

    private final StringRedisTemplate stringRedisTemplate;

    @EventListener
    public void handleConnect(SessionConnectedEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        UUID userId = extractUserId(sha.getUser());
        if (userId == null || sessionId == null) return;

        stringRedisTemplate.opsForValue().set(SESSION_KEY + sessionId, userId.toString());
        Long count = stringRedisTemplate.opsForValue().increment(PRESENCE_CNT + userId);
        if (count != null && count == 1) {
            stringRedisTemplate.opsForValue().set(PRESENCE_ONLINE + userId, "1");
            log.debug("User {} came online", userId);
        }
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = sha.getSessionId();
        if (sessionId == null) return;

        String userIdStr = stringRedisTemplate.opsForValue().get(SESSION_KEY + sessionId);
        if (userIdStr == null) return;

        stringRedisTemplate.delete(SESSION_KEY + sessionId);
        Long count = stringRedisTemplate.opsForValue().decrement(PRESENCE_CNT + userIdStr);
        if (count == null || count <= 0) {
            stringRedisTemplate.delete(PRESENCE_CNT + userIdStr);
            stringRedisTemplate.delete(PRESENCE_ONLINE + userIdStr);
            log.debug("User {} went offline", userIdStr);
        }
    }

    public boolean isOnline(UUID userId) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(PRESENCE_ONLINE + userId));
    }

    private UUID extractUserId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getId();
        }
        return null;
    }
}
