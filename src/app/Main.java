package app;

import model.ChatMessage;
import network.MulticastReceiver;
import network.UdpReceiver;
import network.UdpSender;
import parser.CommandParser;

import java.util.Scanner;

/**
 * Lớp khởi chạy chính thử nghiệm Console Phase 5 (Hỗ trợ net send 3 chế độ + help + clear + exit)
 */
public class Main {
    private static final int PORT = 9876;
    private static final String DEFAULT_MULTICAST_GROUP = "225.1.2.3";

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   CHƯƠNG TRÌNH NET SEND (PHASE 5: TÍCH HỢP HOÀN CHỈNH)");
        System.out.println("   Cú pháp: net send {IP | group | *} message");
        System.out.println("   Gõ 'help' để xem hướng dẫn, 'exit' để thoát");
        System.out.println("==================================================");

        // 1. Kích hoạt Unicast Receiver lắng nghe cổng 9876
        UdpReceiver udpReceiver = new UdpReceiver(PORT);
        Thread udpThread = new Thread(udpReceiver);
        udpThread.setDaemon(true);
        udpThread.start();

        // 2. Kích hoạt Multicast Receiver tự động Join nhóm mặc định 225.1.2.3
        MulticastReceiver multicastReceiver = new MulticastReceiver(DEFAULT_MULTICAST_GROUP, PORT);
        Thread multicastThread = new Thread(multicastReceiver);
        multicastThread.setDaemon(true);
        multicastThread.start();

        // 3. Khởi tạo UdpSender
        UdpSender sender = new UdpSender();
        Scanner scanner = new Scanner(System.in);

        // 4. Vòng lặp nhận câu lệnh từ người dùng
        while (true) {
            System.out.print("\nNhập lệnh > ");
            String input = scanner.nextLine();

            try {
                // Phân tích câu lệnh net send
                ChatMessage message = CommandParser.parse(input);

                // Gửi tin nhắn đi theo đúng phương thức (Unicast / Multicast / Broadcast)
                sender.send(message, PORT);
                System.out.println("[Main] Đã gửi thành công [" + message.getTargetType() + "] tới: " + message.getDestination());

            } catch (IllegalArgumentException e) {
                String errorMsg = e.getMessage();
                if ("[EXIT_APP]".equals(errorMsg)) {
                    System.out.println("Đang thoát chương trình...");
                    udpReceiver.stopListening();
                    multicastReceiver.leaveGroup();
                    break;
                } else if ("[CLEAR_SCREEN]".equals(errorMsg)) {
                    for (int i = 0; i < 50; i++) System.out.println();
                    System.out.println("=== ĐÃ XÓA MÀN HÌNH ===");
                } else {
                    System.out.println(errorMsg);
                }
            } catch (Exception e) {
                System.err.println("[Lỗi Truyền Nhận Mạng]: " + e.getMessage());
            }
        }

        scanner.close();
    }
}
