package parser;

import model.ChatMessage;
import model.TargetType;
import java.net.InetAddress;

/**
 * Lớp chịu trách nhiệm phân tích cú pháp câu lệnh: net send {IP | group | *} message
 * cũng như các câu lệnh điều khiển: help, clear, exit
 */
public class CommandParser {

    /**
     * Tách câu lệnh thô net send thành đối tượng ChatMessage và tự động nhận diện TargetType
     */
    public static ChatMessage parse(String inputCommand) throws IllegalArgumentException {
        if (inputCommand == null || inputCommand.trim().isEmpty()) {
            throw new IllegalArgumentException("Lệnh không được để rỗng!");
        }

        String trimmed = inputCommand.trim();

        // Xử lý các câu lệnh tiện ích: help, clear, exit
        if (trimmed.equalsIgnoreCase("help")) {
            throw new IllegalArgumentException(getHelpText());
        }
        if (trimmed.equalsIgnoreCase("clear")) {
            throw new IllegalArgumentException("[CLEAR_SCREEN]");
        }
        if (trimmed.equalsIgnoreCase("exit")) {
            throw new IllegalArgumentException("[EXIT_APP]");
        }

        // Kiểm tra xem lệnh có bắt đầu bằng "net send " không
        if (!trimmed.toLowerCase().startsWith("net send ")) {
            throw new IllegalArgumentException("Cú pháp phải bắt đầu bằng 'net send {target} {message}'. Gõ 'help' để xem hướng dẫn.");
        }

        // Bỏ tiền tố "net send "
        String rest = trimmed.substring(9).trim();
        int firstSpaceIndex = rest.indexOf(' ');
        if (firstSpaceIndex == -1) {
            throw new IllegalArgumentException("Thiếu nội dung tin nhắn! Cú pháp: net send {IP | group | *} {message}");
        }

        // Tách tham số destination (IP/group/*) và nội dung message (giữ nguyên nhiều từ)
        String destination = rest.substring(0, firstSpaceIndex).trim();
        String messageContent = rest.substring(firstSpaceIndex + 1).trim();

        if (destination.isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ đích không được rỗng!");
        }
        if (messageContent.isEmpty()) {
            throw new IllegalArgumentException("Nội dung tin nhắn không được rỗng!");
        }
// dòng 58: Phân loại TargetType (BROADCAST '*', MULTICAST 'group IP Class D', UNICAST 'IP máy')
        // Phân loại TargetType (BROADCAST '*', MULTICAST 'group IP Class D', UNICAST 'IP máy')
        TargetType type;
        if (destination.equals("*")) {
            type = TargetType.BROADCAST;
        } else {
            try {
                InetAddress addr = InetAddress.getByName(destination);// phải là một chuỗi ip hoặc group
                if (addr.isMulticastAddress()) {// Kiểm tra xem có phải là địa chỉ multicast không
                    type = TargetType.MULTICAST; // Thuộc dải IP Class D: 224.0.0.0 -> 239.255.255.255
                } else {// nếu không phải là multicast thì là unicast
                    type = TargetType.UNICAST;   // Địa chỉ IP máy đơn lẻ (vd: 192.168.1.10)
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Địa chỉ IP hoặc Group không hợp lệ: '" + destination + "'");
            }
        }

        return new ChatMessage(destination, messageContent, type);
    }

    public static String getHelpText() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== HƯỚNG DẪN CÚ PHÁP LỆNH NET SEND ===\n");
        sb.append("1. Gửi 1-1 (Unicast):   net send {IP_máy} {nội_dung}\n");
        sb.append("   Ví dụ:              net send 192.168.1.10 Xin chào riêng\n");
        sb.append("2. Gửi Nhóm (Multicast): net send {Group_IP} {nội_dung}\n");
        sb.append("   Ví dụ:              net send 225.1.2.3 Chào cả nhóm\n");
        sb.append("3. Gửi Toàn Mạng (All):  net send * {nội_dung}\n");
        sb.append("   Ví dụ:              net send * Thông báo chung\n");
        sb.append("--------------------------------------------------\n");
        sb.append("Lệnh tiện ích: 'help' (Trợ giúp), 'clear' (Xóa màn hình), 'exit' (Thoát)");
        return sb.toString();
    }
}
