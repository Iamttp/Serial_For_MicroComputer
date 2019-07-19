import gnu.io.PortInUseException;
import gnu.io.SerialPort;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author ttp
 */
public class MainForm extends JFrame {
    public JComboBox<String> comBoxCom = new JComboBox<>();
    public java.util.List<String> mCommList = null;
    public SerialPort mSerial;
    private JTextArea textArea = new JTextArea();
    public DataReceiver dataReceiver = new DataReceiver();
    private static byte[] data;
    private boolean isStop = false;
    JButton button2 = new JButton("暂停");
    JTextField jTextField = new JTextField();

    public MainForm() throws HeadlessException {
        setTitle("");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        comBoxCom.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                mCommList = SerialPortManager.findPorts();
                // 检查是否有可用串口，有则加入选项中
                if (mCommList == null || mCommList.size() < 1) {
                    Util.plainMessage("", "没有搜索到有效串口！");
                } else {
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

        JButton button = new JButton("开始读取");
        button.addActionListener(e -> openSerialPort());
        button.setFont(new Font("宋体", Font.BOLD, 30));

        button2.addActionListener(e -> stop());
        button2.setFont(new Font("宋体", Font.BOLD, 30));

        textArea.setFont(new Font("宋体", Font.BOLD, 30));
        JScrollPane js = new JScrollPane(textArea);
        js.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        jTextField.setText("3");
        jTextField.addActionListener(e -> dataReceiver.alpha = Double.parseDouble(jTextField.getText()));
        jTextField.setFont(new Font("宋体", Font.BOLD, 30));

        JSplitPane jp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, comBoxCom, button);
        JSplitPane jpp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jp, button2);
        JSplitPane jppp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jpp, jTextField);
        jp.setResizeWeight(0.6);
        jpp.setResizeWeight(0.7);
        jppp.setResizeWeight(0.8);
        JSplitPane jp2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, js, dataReceiver);
        JSplitPane jp4 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jppp, jp2);
        jp2.setResizeWeight(0.5);
        jp4.setResizeWeight(0.02);
        Container cp = getContentPane();
        cp.add(jp4, BorderLayout.CENTER);
        setVisible(true);

        mCommList = SerialPortManager.findPorts();
        // 检查是否有可用串口，有则加入选项中
        if (mCommList == null || mCommList.size() < 1) {
            Util.plainMessage("", "没有搜索到有效串口！");
        } else {
            int index = comBoxCom.getSelectedIndex();
            comBoxCom.removeAllItems();
            for (String s : mCommList) {
                comBoxCom.addItem(s);
            }
            comBoxCom.setSelectedIndex(index);
        }
    }

    private void stop() {
        if (!isStop) {
            button2.setText("开始");
        } else {
            button2.setText("暂停");
        }
        isStop = !isStop;
    }

    /**
     * 打开串口
     */
    private void openSerialPort() {
        // 获取串口名称
        String commName = (String) comBoxCom.getSelectedItem();
        // 波特率默认为115200
        int baudrate = 115200;
        // 检查串口名称是否获取正确
        if (commName == null || "".equals(commName)) {
            Util.plainMessage("", "没有搜索到有效串口！");
        } else {
            try {
                mSerial = SerialPortManager.openPort(commName, baudrate);
            } catch (PortInUseException e) {
                Util.plainMessage("", "串口已被占用！");
            }
        }
        // 添加串口监听
        assert mSerial != null;
        SerialPortManager.addListener(mSerial, () -> {
            if (mSerial == null) {
                Util.plainMessage("", "串口对象为空，监听失败！");
            } else {
                doData();
            }
        });
    }

    private void doData() {
        // 读取串口数据, TODO 传入结束标志 0xff
        data = SerialPortManager.readFromPort(mSerial, (byte) 0xff);
        System.out.println(Arrays.toString(data));
        ArrayList<Double> res = getRes(data);
        if (!isStop) {
            textArea.setText("");
            textArea.append("pit: " + res.get(0) + "\t");
            textArea.append("rol: " + res.get(1) + "\t");
            textArea.append("yaw: " + res.get(2) + "\t");
            textArea.append("\n");
            textArea.append("波特率为115200\n");
            textArea.append("线颜色顺序为：黑、红、蓝、绿\n");
            textArea.append("默认波形图比例为3.0");
            // TODO 调试开启
//        textArea.append(Arrays.toString(data));
        }
    }

    public ArrayList<Double> getRes(byte[] data) {
        ArrayList<Integer> res1 = new ArrayList<>();
        for (byte datum : data) {
            res1.add((datum & 0xFF));
        }

        ArrayList<Double> res = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("#.##");
        double getDouble1 = Double.parseDouble(df.format(((res1.get(1) << 8) + res1.get(2)) / 100.0 - 180));
        double getDouble2 = Double.parseDouble(df.format(((res1.get(3) << 8) + res1.get(4)) / 100.0 - 180));
        double getDouble3 = Double.parseDouble(df.format(((res1.get(5) << 8) + res1.get(6)) / 100.0 - 180));
        res.add(getDouble1);
        res.add(getDouble2);
        res.add(getDouble3);
        List<Integer> list = new ArrayList<>();
        list.add((int) (getDouble1));
        list.add((int) (getDouble2));
        list.add((int) (getDouble3));
        dataReceiver.addValue(list); // 产生一个数据，并模拟接收并放到容器里.
        if (!isStop) {
            repaint();
        }
        return res;
    }

    public static void main(String[] args) {
        new MainForm();
    }
}
