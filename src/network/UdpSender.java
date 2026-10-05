package network;

import model.ChatMessage;
import model.TargetType;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

/**
 * Lớp chịu trách nhiệm gửi tin nhắn theo đúng chế độ: Unicast, Multicast, hoặc Broadcast
 */
public class UdpSender {

    /**
     * Gửi ChatMessage đi qua mạng theo đúng TargetType
     */
    public void send(ChatMessage message, int port) throws Exception {
        byte[] data = message.getContent().getBytes(StandardCharsets.UTF_8);

        if (message.getTargetType() == TargetType.BROADCAST) {
            // Gửi Broadcast tới 255.255.255.255
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                InetAddress broadcastIP = InetAddress.getByName("255.255.255.255");
                DatagramPacket packet = new DatagramPacket(data, data.length, broadcastIP, port);
                socket.send(packet);
            }
        } else if (message.getTargetType() == TargetType.MULTICAST) {
            // Gửi Multicast tới IP Group (Class D)
            try (MulticastSocket socket = new MulticastSocket()) {
                InetAddress groupIP = InetAddress.getByName(message.getDestination());
                DatagramPacket packet = new DatagramPacket(data, data.length, groupIP, port);
                socket.send(packet);
            }
        } else {
            // Gửi Unicast 1-1 tới IP máy cụ thể
            try (DatagramSocket socket = new DatagramSocket()) {
                InetAddress destIP = InetAddress.getByName(message.getDestination());
                DatagramPacket packet = new DatagramPacket(data, data.length, destIP, port);
                socket.send(packet);
            }
        }
    }
}
