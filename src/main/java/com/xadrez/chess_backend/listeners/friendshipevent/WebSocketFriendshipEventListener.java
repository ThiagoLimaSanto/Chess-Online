package com.xadrez.chess_backend.listeners.friendshipevent;

import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.xadrez.chess_backend.record.FriendshipChangedEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WebSocketFriendshipEventListener {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFriendshipChanged(FriendshipChangedEvent event) {
        Map<String, String> notification = Map.of("type", "FRIENDSHIP_UPDATED");
        messagingTemplate.convertAndSend("/topic/players/" + event.requesterId(), notification);
        messagingTemplate.convertAndSend("/topic/players/" + event.receiverId(), notification);
    }
}
