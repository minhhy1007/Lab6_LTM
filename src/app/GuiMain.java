package app;

import gui.MainFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Điểm khởi chạy ứng dụng GUI Swing
 */
public class GuiMain {
    public static void main(String[] args) {
        // Thiết lập giao diện hệ thống LookAndFeel cho mượt mà
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Ignore
        }

        // Đưa việc khởi tạo GUI lên Swing Event Dispatch Thread (EDT) an toàn
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
