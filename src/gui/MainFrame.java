package gui;

import model.ChatMessage;
import model.TargetType;
import network.MulticastReceiver;
import network.UdpReceiver;
import network.UdpSender;
import parser.CommandParser;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Giao diện đồ họa Swing hoàn chỉnh cho ứng dụng Net Send Multicast
 */
public class MainFrame extends JFrame {

    private static final int PORT = 9876;
    private String currentMulticastGroup = "225.1.2.3";

    // Thành phần mạng
    private UdpSender sender;
    private UdpReceiver udpReceiver;
    private MulticastReceiver multicastReceiver;
    private Thread udpThread;
    private Thread multicastThread;

    // Các thành phần GUI
    private JLabel lblStatus;
    private JLabel lblLocalIp;
    private JLabel lblPort;
    private JLabel lblMulticastGroup;

    private JTextArea txtHistory;
    private JTextArea txtLog;

    private JRadioButton rdoUnicast;
    private JRadioButton rdoMulticast;
    private JRadioButton rdoBroadcast;
    private JTextField txtDestination;
    private JTextField txtMessage;
    private JButton btnSend;
    private JButton btnClear;

    private JTextField txtRawCommand;
    private JButton btnExecuteRaw;

    private JTextField txtNewGroupIp;
    private JButton btnJoinGroup;
    private JButton btnLeaveGroup;
    private JButton btnToggleReceiver;

    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public MainFrame() {
        super("Chương Trình Net Send Java Multicast & UDP (Phase 6)");
        initNetwork();
        initGUI();
        startReceivers();
    }

    private void initNetwork() {
        sender = new UdpSender();
    }

    private void initGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // 1. TOP PANEL: Connection Status Bar
        JPanel pnlStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        pnlStatus.setBorder(BorderFactory.createTitledBorder("🟢 BẢNG TRẠNG THÁI MẠNG"));

        lblStatus = new JLabel("Trạng thái: Đang Lắng Nghe");
        lblStatus.setForeground(new Color(0, 128, 0));
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 12));

        String localIp = "127.0.0.1";
        try {
            localIp = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            // Ignore
        }
        lblLocalIp = new JLabel("IP Máy này: " + localIp);
        lblPort = new JLabel("Port: " + PORT);
        lblMulticastGroup = new JLabel("Nhóm Multicast: " + currentMulticastGroup);

        pnlStatus.add(lblStatus);
        pnlStatus.add(lblLocalIp);
        pnlStatus.add(lblPort);
        pnlStatus.add(lblMulticastGroup);
        add(pnlStatus, BorderLayout.NORTH);

        // 2. CENTER PANEL: Message History Area
        JPanel pnlCenter = new JPanel(new GridLayout(1, 2, 10, 10));
        
        txtHistory = new JTextArea();
        txtHistory.setEditable(false);
        txtHistory.setFont(new Font("Consolas", Font.PLAIN, 13));
        JScrollPane scrollHistory = new JScrollPane(txtHistory);
        scrollHistory.setBorder(BorderFactory.createTitledBorder("🔵 NHẬT KÝ TIN NHẮN NHẬN ĐƯỢC"));

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtLog.setForeground(Color.BLUE);
        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setBorder(BorderFactory.createTitledBorder("📜 LỊCH SỬ HOẠT ĐỘNG & TÁC VỤ (LOG)"));

        pnlCenter.add(scrollHistory);
        pnlCenter.add(scrollLog);
        add(pnlCenter, BorderLayout.CENTER);

        // 3. SOUTH PANEL: Control & Send Panels
        JPanel pnlSouth = new JPanel();
        pnlSouth.setLayout(new BoxLayout(pnlSouth, BoxLayout.Y_AXIS));

        // Form Gửi tin nhắn
        JPanel pnlSend = new JPanel(new GridBagLayout());
        pnlSend.setBorder(BorderFactory.createTitledBorder("🟡 BỘ GỬI TIN NHẮN (SEND CONTROLLER)"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Mode Radio Buttons
        JPanel pnlModes = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rdoUnicast = new JRadioButton("Unicast (1-1)", true);
        rdoMulticast = new JRadioButton("Multicast (Nhóm)");
        rdoBroadcast = new JRadioButton("Broadcast (*)");
        ButtonGroup bgMode = new ButtonGroup();
        bgMode.add(rdoUnicast);
        bgMode.add(rdoMulticast);
        bgMode.add(rdoBroadcast);
        pnlModes.add(rdoUnicast);
        pnlModes.add(rdoMulticast);
        pnlModes.add(rdoBroadcast);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        pnlSend.add(pnlModes, gbc);

        // Target Textfield
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        pnlSend.add(new JLabel("Địa chỉ Đích (Target):"), gbc);
        txtDestination = new JTextField("127.0.0.1", 15);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        pnlSend.add(txtDestination, gbc);

        // Message Textfield
        gbc.weightx = 0;
        gbc.gridx = 0; gbc.gridy = 2;
        pnlSend.add(new JLabel("Nội dung (Message):"), gbc);
        txtMessage = new JTextField("", 30);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        pnlSend.add(txtMessage, gbc);

        // Buttons Send / Clear
        JPanel pnlSendBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSend = new JButton("🚀 GỬI (NET SEND)");
        btnSend.setBackground(new Color(30, 144, 255));
        btnSend.setForeground(Color.WHITE);
        btnClear = new JButton("🧹 Xóa Nhật Ký");
        pnlSendBtns.add(btnSend);
        pnlSendBtns.add(btnClear);

        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0;
        pnlSend.add(pnlSendBtns, gbc);

        // Form Nhập Lệnh Thô net send
        JPanel pnlRaw = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlRaw.add(new JLabel("Hoặc gõ lệnh thô:"));
        txtRawCommand = new JTextField("net send 225.1.2.3 Hello Multicast GUI", 40);
        btnExecuteRaw = new JButton("Thực thi Lệnh");
        pnlRaw.add(txtRawCommand);
        pnlRaw.add(btnExecuteRaw);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        pnlSend.add(pnlRaw, gbc);

        // Panel Quản lý Multicast & Toggle Receiver
        JPanel pnlMulticastControl = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pnlMulticastControl.setBorder(BorderFactory.createTitledBorder("🔴 QUẢN LÝ NHÓM MULTICAST & TRẠNG THÁI"));

        pnlMulticastControl.add(new JLabel("IP Nhóm Multicast:"));
        txtNewGroupIp = new JTextField("225.1.2.3", 12);
        btnJoinGroup = new JButton("Join Group");
        btnLeaveGroup = new JButton("Leave Group");
        btnToggleReceiver = new JButton("Tắt Lắng Nghe");

        pnlMulticastControl.add(txtNewGroupIp);
        pnlMulticastControl.add(btnJoinGroup);
        pnlMulticastControl.add(btnLeaveGroup);
        pnlMulticastControl.add(btnToggleReceiver);

        pnlSouth.add(pnlSend);
        pnlSouth.add(pnlMulticastControl);
        add(pnlSouth, BorderLayout.SOUTH);

        // Event Listeners
        initEvents();
    }

    private void initEvents() {
        // Radio buttons auto fill target
        rdoUnicast.addActionListener(e -> txtDestination.setText("127.0.0.1"));
        rdoMulticast.addActionListener(e -> txtDestination.setText(currentMulticastGroup));
        rdoBroadcast.addActionListener(e -> txtDestination.setText("*"));

        // Button Send
        btnSend.addActionListener(this::handleSendAction);

        // Button Clear
        btnClear.addActionListener(e -> {
            txtHistory.setText("");
            txtLog.setText("");
            appendLog("Đã xóa sạch nhật ký hiển thị.");
        });

        // Button Execute Raw Command
        btnExecuteRaw.addActionListener(e -> {
            String raw = txtRawCommand.getText().trim();
            if (!raw.isEmpty()) {
                processAndSend(raw);
            }
        });

        // Enter key in txtMessage
        txtMessage.addActionListener(this::handleSendAction);

        // Multicast Group Control
        btnJoinGroup.addActionListener(e -> {
            String newGroup = txtNewGroupIp.getText().trim();
            if (!newGroup.isEmpty()) {
                switchMulticastGroup(newGroup);
            }
        });

        btnLeaveGroup.addActionListener(e -> {
            if (multicastReceiver != null) {
                multicastReceiver.leaveGroup();
                appendLog("Đã rời khỏi nhóm Multicast " + currentMulticastGroup);
                lblMulticastGroup.setText("Nhóm Multicast: [Đã Rời]");
            }
        });

        // Toggle Receiver
        btnToggleReceiver.addActionListener(e -> {
            if (udpReceiver != null && udpReceiver.isRunning()) {
                stopReceivers();
                btnToggleReceiver.setText("Bật Lắng Nghe");
                lblStatus.setText("Trạng thái: Đã Tắt");
                lblStatus.setForeground(Color.RED);
            } else {
                startReceivers();
                btnToggleReceiver.setText("Tắt Lắng Nghe");
                lblStatus.setText("Trạng thái: Đang Lắng Nghe");
                lblStatus.setForeground(new Color(0, 128, 0));
            }
        });
    }

    private void handleSendAction(ActionEvent e) {
        String dest = txtDestination.getText().trim();
        String msg = txtMessage.getText().trim();

        if (dest.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Địa chỉ Đích!", "Lỗi Input", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (msg.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập Nội dung Tin nhắn!", "Lỗi Input", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String rawCommand = "net send " + dest + " " + msg;
        processAndSend(rawCommand);
    }

    private void processAndSend(String rawCommand) {
        try {
            ChatMessage chatMsg = CommandParser.parse(rawCommand);
            sender.send(chatMsg, PORT);
            
            appendLog("GỬI THÀNH CÔNG [" + chatMsg.getTargetType() + "] ➔ Đích: " + chatMsg.getDestination() + " | Nội dung: " + chatMsg.getContent());
            txtMessage.setText("");
            txtMessage.requestFocus();
        } catch (Exception ex) {
            appendLog("❌ LỖI GỬI: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Lỗi Gửi Tin", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startReceivers() {
        // Callback cập nhật GUI an toàn trên Swing Event Dispatch Thread (EDT)
        network.MessageListener listener = message -> SwingUtilities.invokeLater(() -> {
            String timeStr = timeFormat.format(new Date(message.getTimestamp()));
            String formatted = String.format("[%s] [%s] Từ %s ➔ %s\n", timeStr, message.getTargetType(), message.getSenderIp(), message.getContent());
            txtHistory.append(formatted);
            txtHistory.setCaretPosition(txtHistory.getDocument().getLength());
            appendLog("NHẬN TIN NHẮN TỪ " + message.getSenderIp() + " (" + message.getTargetType() + ")");
        });

        // 1. UdpReceiver
        udpReceiver = new UdpReceiver(PORT, listener);
        udpThread = new Thread(udpReceiver);
        udpThread.setDaemon(true);
        udpThread.start();

        // 2. MulticastReceiver
        multicastReceiver = new MulticastReceiver(currentMulticastGroup, PORT, listener);
        multicastThread = new Thread(multicastReceiver);
        multicastThread.setDaemon(true);
        multicastThread.start();

        lblMulticastGroup.setText("Nhóm Multicast: " + currentMulticastGroup);
        appendLog("Đã bật Tiến trình Lắng nghe Unicast & Multicast Group (" + currentMulticastGroup + ")");
    }

    private void stopReceivers() {
        if (udpReceiver != null) udpReceiver.stopListening();
        if (multicastReceiver != null) multicastReceiver.leaveGroup();
        appendLog("Đã tắt toàn bộ tiến trình lắng nghe mạng.");
    }

    private void switchMulticastGroup(String newGroupIp) {
        if (multicastReceiver != null) {
            multicastReceiver.leaveGroup();
        }
        currentMulticastGroup = newGroupIp;
        network.MessageListener listener = message -> SwingUtilities.invokeLater(() -> {
            String timeStr = timeFormat.format(new Date(message.getTimestamp()));
            String formatted = String.format("[%s] [%s] Từ %s ➔ %s\n", timeStr, message.getTargetType(), message.getSenderIp(), message.getContent());
            txtHistory.append(formatted);
            appendLog("NHẬN TIN MULTICAST TỪ " + message.getSenderIp());
        });

        multicastReceiver = new MulticastReceiver(currentMulticastGroup, PORT, listener);
        multicastThread = new Thread(multicastReceiver);
        multicastThread.setDaemon(true);
        multicastThread.start();

        lblMulticastGroup.setText("Nhóm Multicast: " + currentMulticastGroup);
        appendLog("Đã chuyển sang Join nhóm Multicast mới: " + currentMulticastGroup);
    }

    private void appendLog(String logText) {
        String timeStr = timeFormat.format(new Date());
        txtLog.append("[" + timeStr + "] " + logText + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }
}
