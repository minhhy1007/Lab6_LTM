package network;

import model.ChatMessage;
import model.TargetType;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

/**
 * Lớp chạy ngầm (Thread) liên tục lắng nghe và nhận gói tin UDP trên cổng quy ước
 */
public class UdpReceiver implements Runnable {
    private final int listenPort;
    private volatile boolean running = true;
    private DatagramSocket socket;
    private MessageListener listener;

    public UdpReceiver(int listenPort) {
        this.listenPort = listenPort;
    }

    public UdpReceiver(int listenPort, MessageListener listener) {
        this.listenPort = listenPort;
        this.listener = listener;
    }

    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(listenPort));
            System.out.println("[UdpReceiver] Đang lắng nghe cổng UDP " + listenPort + "...");
            byte[] buffer = new byte[2048];

            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String content = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                String senderIp = packet.getAddress().getHostAddress();

                ChatMessage receivedMsg = new ChatMessage(senderIp, "Local", content, TargetType.UNICAST);

                System.out.println("\n----------------------------------------");
                System.out.println("NHẬN TIN NHẮN TỪ: " + senderIp);
                System.out.println("NỘI DUNG: " + content);
                System.out.println("----------------------------------------");

                if (listener != null) {
                    listener.onMessageReceived(receivedMsg);
                }
            }
        } catch (Exception e) {
            if (running) {
                System.err.println("[UdpReceiver] Lỗi Socket lắng nghe: " + e.getMessage());
            } else {
                System.out.println("[UdpReceiver] Đã dừng lắng nghe.");
            }
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
    }

    public void stopListening() {
        this.running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public boolean isRunning() {
        return running && socket != null && !socket.isClosed();
    }
}
