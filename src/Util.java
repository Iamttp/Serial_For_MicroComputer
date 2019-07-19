import javax.swing.*;

public class Util {
    /**
     * 自定义的消息提示
     */
    public static void plainMessage(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title,
                JOptionPane.PLAIN_MESSAGE);
    }
}
