package network;

import model.ChatMessage;
import model.TargetType;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

/**
 * Lớp chạy ngầm (Thread) lắng nghe tin nhắn Multicast từ một IP Group chỉ định
 */
public class MulticastReceiver implements Runnable {
    private final String groupIp;
    private final int port;
    private volatile boolean running = true;
    private MulticastSocket socket;
    private InetAddress groupAddress;
    private MessageListener listener;

    public MulticastReceiver(String groupIp, int port) {
        this.groupIp = groupIp;
        this.port = port;
    }

    public MulticastReceiver(String groupIp, int port, MessageListener listener) {
        this.groupIp = groupIp;
        this.port = port;
        this.listener = listener;
    }

    public void setMessageListener(MessageListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            groupAddress = InetAddress.getByName(groupIp);

            socket = new MulticastSocket(null);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(port));

            socket.joinGroup(groupAddress);
            System.out.println("[MulticastReceiver] Đã tham gia nhóm Multicast: " + groupIp + " (Port " + port + ")");

            byte[] buffer = new byte[2048];

            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String content = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                String senderIp = packet.getAddress().getHostAddress();

                ChatMessage receivedMsg = new ChatMessage(senderIp, groupIp, content, TargetType.MULTICAST);

                System.out.println("\n----------------------------------------");
                System.out.println("NHẬN TIN NHẮN MULTICAST TỪ: " + senderIp + " [Nhóm " + groupIp + "]");
                System.out.println("NỘI DUNG: " + content);
                System.out.println("----------------------------------------");

                if (listener != null) {
                    listener.onMessageReceived(receivedMsg);
                }
            }
        } catch (Exception e) {
            if (running) {
                System.err.println("[MulticastReceiver] Lỗi Socket Multicast: " + e.getMessage());
            } else {
                System.out.println("[MulticastReceiver] Đã rời nhóm và dừng lắng nghe.");
            }
        } finally {
            leaveGroup();
        }
    }

    public void leaveGroup() {
        this.running = false;
        if (socket != null && !socket.isClosed()) {
            try {
                if (groupAddress != null) {
                    socket.leaveGroup(groupAddress);
                }
                socket.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    public boolean isRunning() {
        return running && socket != null && !socket.isClosed();
    }
}
