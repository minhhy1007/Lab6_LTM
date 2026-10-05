package model;

/**
 * Model chứa dữ liệu tin nhắn truyền qua mạng
 */
public class ChatMessage {
    private String senderIp;     // IP của máy gửi (trích xuất từ gói tin)
    private String destination;  // Địa chỉ đích (IP, group, hoặc *)
    private String content;      // Nội dung tin nhắn
    private TargetType targetType; // Phân loại: UNICAST, MULTICAST, BROADCAST
    private long timestamp;      // Thời gian gửi/nhận

    public ChatMessage(String destination, String content, TargetType targetType) {
        this.destination = destination;
        this.content = content;
        this.targetType = targetType;
        this.timestamp = System.currentTimeMillis();
    }

    public ChatMessage(String senderIp, String destination, String content, TargetType targetType) {
        this.senderIp = senderIp;
        this.destination = destination;
        this.content = content;
        this.targetType = targetType;
        this.timestamp = System.currentTimeMillis();
    }

    public String getSenderIp() {
        return senderIp;
    }

    public void setSenderIp(String senderIp) {
        this.senderIp = senderIp;
    }

    public String getDestination() {
        return destination;
    }

    public String getContent() {
        return content;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        if (senderIp != null && !senderIp.isEmpty()) {
            return "[" + targetType + "] [" + senderIp + "]: " + content;
        }
        return content;
    }
}
