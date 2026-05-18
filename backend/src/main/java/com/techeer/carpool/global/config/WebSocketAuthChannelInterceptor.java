package com.techeer.carpool.global.config;

import com.techeer.carpool.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.messaging.MessagingException;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new MessagingException("WebSocket 연결 실패: Authorization 헤더가 없습니다.");
            }
            String token = authHeader.substring(7);
            if (!jwtTokenProvider.validateToken(token)) {
                throw new MessagingException("WebSocket 연결 실패: 유효하지 않은 토큰입니다.");
            }
            Long memberId = jwtTokenProvider.getMemberIdFromToken(token);
            accessor.setUser(() -> String.valueOf(memberId));
        }
        return message;
    }
}
