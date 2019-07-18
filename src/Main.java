import gnu.io.PortInUseException;
import gnu.io.SerialPort;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Main extends JFrame {
    private final JButton button = new JButton("读取");
    public JComboBox<String> comBoxCom = new JComboBox<>();
    public java.util.List<String> mCommList = null;
    public SerialPort mSerialport;
    private JTextArea textArea = new JTextArea();

    static byte[] data;

    public Main() throws HeadlessException {
        setTitle("");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        comBoxCom.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                mCommList = SerialPortManager.findPorts();
                // 检查是否有可用串口，有则加入选项中
                if (mCommList == null || mCommList.size() < 1) {
                    plainMessage("", "没有搜索到有效串口！");
                } else {
                    System.out.println("搜索到有效串口！\n");
                    int index = comBoxCom.getSelectedIndex();
                    comBoxCom.removeAllItems();
                    for (String s : mCommList) {
                        comBoxCom.addItem(s);
                    }
                    comBoxCom.setSelectedIndex(index);
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // NO OP
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                // NO OP
            }
        });
        comBoxCom.setFont(new Font("宋体", Font.BOLD, 30));

        button.addActionListener(e -> {
            openSerialPort();
        });
        button.setFont(new Font("宋体", Font.BOLD, 30));

        JSplitPane jp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, comBoxCom, button);

        textArea.setFont(new Font("宋体", Font.BOLD, 30));
        //分别设置水平和垂直滚动条自动出现
        JScrollPane js = new JScrollPane(textArea);
        js.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        JSplitPane jp2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jp, js);
        jp.setResizeWeight(0.5);
        Container cp = getContentPane();
        cp.add(jp2, BorderLayout.CENTER);
        setVisible(true);
    }

    /**
     * 自定义的消息提示
     */
    public static void plainMessage(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title,
                JOptionPane.PLAIN_MESSAGE);
    }

    public static int byteArrayToInt(byte[] bytes) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (3 - i) * 8;
            value += (bytes[i] & 0xFF) << shift;
        }
        return value;
    }

    /**
     * 打开串口
     */
    private void openSerialPort() {
        // 获取串口名称
        String commName = (String) comBoxCom.getSelectedItem();
        // 波特率默认为115200
        int baudrate = 115200;
        System.out.println("点击称重\n");
        // 检查串口名称是否获取正确
        if (commName == null || "".equals(commName)) {
            plainMessage("", "没有搜索到有效串口！");
        } else {
            try {
                mSerialport = SerialPortManager.openPort(commName, baudrate);
            } catch (PortInUseException e) {
                plainMessage("", "串口已被占用！");
                System.out.println("串口已被占用\n");
            }
        }
        // 添加串口监听
        assert mSerialport != null;
        SerialPortManager.addListener(mSerialport, () -> {
            if (mSerialport == null) {
                plainMessage("", "串口对象为空，监听失败！");
            } else {
                // 读取串口数据, 传入结束标志
                data = SerialPortManager.readFromPort(mSerialport, (byte) 0xff);
                System.out.println(Arrays.toString(data));
                ArrayList<Double> res = getRes(data);
                textArea.setText("");
                textArea.append("pit: " + res.get(0) + "\t");
                textArea.append("rol: " + res.get(1) + "\t");
                textArea.append("yaw: " + res.get(2) + "\t");
                textArea.append("\n");
            }
        });
    }

    public static ArrayList<Double> getRes(byte[] data) {
        ArrayList<Integer> res1 = new ArrayList<>();
        for (byte datum : data) {
            res1.add((datum & 0xFF));
        }

        ArrayList<Double> res = new ArrayList<>();
        res.add(((res1.get(1) << 8) + res1.get(2)) / 100.0);
        res.add(((res1.get(3) << 8) + res1.get(4)) / 100.0);
        res.add(((res1.get(5) << 8) + res1.get(6)) / 100.0);
        return res;
    }

    public static void main(String[] args) {
        new Main();
    }
}
