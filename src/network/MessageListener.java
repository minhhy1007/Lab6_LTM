package network;

import model.ChatMessage;

/**
 * Interface Callback để truyền dữ liệu tin nhắn nhận được từ Network Layer lên GUI Layer
 */
@FunctionalInterface
public interface MessageListener {
    void onMessageReceived(ChatMessage message);
}
