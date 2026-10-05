package model;

/**
 * Enum phân loại 3 chế độ gửi gói tin trong ứng dụng Net Send
 */
public enum TargetType {
    UNICAST,    // Gửi 1 - 1 tới 1 IP cụ thể
    MULTICAST,  // Gửi 1 - Nhóm tới Multicast Group IP (224.0.0.0 - 239.255.255.255)
    BROADCAST   // Gửi 1 - Tất cả qua ký hiệu '*'
}
