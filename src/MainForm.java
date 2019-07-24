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
    public DataReceiver dataReceiver = new DataReceiver(3, 1);
    public DataReceiver dataReceiver2 = new DataReceiver(0.1, 30);
    private static byte[] data;
    private boolean isStop = false;
    JButton button2 = new JButton("暂停");
    JTextField jTextField = new JTextField();

    public MainForm() throws HeadlessException {
        setTitle("");
        setSize(1100, 700);
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
        button.addActionListener(e -> {
            openSerialPort();
            button.setText("正在读取");
        });
        button.setFont(new Font("宋体", Font.BOLD, 30));

        button2.addActionListener(e -> stop());
        button2.setFont(new Font("宋体", Font.BOLD, 30));

        textArea.setFont(new Font("宋体", Font.BOLD, 30));
        JScrollPane js = new JScrollPane(textArea);
        js.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        js.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        jTextField.setText("3");
        jTextField.addActionListener(e -> dataReceiver2.alpha = Double.parseDouble(jTextField.getText()));
        jTextField.setFont(new Font("宋体", Font.BOLD, 30));

        JSplitPane jp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, comBoxCom, button);
        JSplitPane jpp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jp, button2);
        JSplitPane jppp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, jpp, jTextField);
        jp.setResizeWeight(0.6);
        jpp.setResizeWeight(0.7);
        jppp.setResizeWeight(0.8);
        JSplitPane jpd = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, dataReceiver, dataReceiver2);
        JSplitPane jp2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, js, jpd);
        JSplitPane jp4 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jppp, jp2);
        jpd.setResizeWeight(0.5);
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
        // 读取串口数据, TODO 传入结束标志 长度改变
        data = SerialPortManager.readFromPort(mSerial, (byte) 0xaa, (byte) 0xfe, 44);
        System.out.println(Arrays.toString(data));
        ArrayList<Double> res = getRes(data);
        if (res == null) {
            return;
        }
        if (!isStop) {
            // -------------------------------------  TODO 显示框部分
            textArea.setText("");
            textArea.append("pit: " + res.get(0) + "\t");
            textArea.append("rol: " + res.get(1) + "\t");
            textArea.append("yaw: " + res.get(2) + "\t\n");
            textArea.append("CH_ROL: " + res.get(3) + "\t");
            textArea.append("CH_PIT: " + res.get(4) + "\t");
            textArea.append("CH_THR: " + res.get(5) + "\t");
            textArea.append("CH_YAW: " + res.get(6) + "\t\n");
            textArea.append("pos_x: " + res.get(7) + "\t");
            textArea.append("pos_y: " + res.get(8) + "\t\n");
            textArea.append("ground_pos_err_h_cm[0]: " + res.get(9) + "\t");
            textArea.append("ground_pos_err_h_cm[1]: " + res.get(10) + "\t\n");
            textArea.append("speed_set_h[X]: " + res.get(11) + "\t");
            textArea.append("speed_set_h[Y]: " + res.get(12) + "\t\n");
            textArea.append("motor[0]: " + res.get(13) + "\t");
            textArea.append("motor[1]: " + res.get(14) + "\t");
            textArea.append("motor[2]: " + res.get(15) + "\t");
            textArea.append("motor[3]: " + res.get(16) + "\t\n");
            textArea.append("mc.ct_val_thr: " + res.get(17) + "\t");
            textArea.append("mc.ct_val_yaw: " + res.get(18) + "\t");
            textArea.append("mc.ct_val_rol: " + res.get(19) + "\t");
            textArea.append("mc.ct_val_pit: " + res.get(20) + "\t\n");
            textArea.append("my_jig: " + res.get(21) + " mm\t");
            textArea.append("ref_height_used: " + res.get(22) + " cm\t");
            textArea.append("\n");
            textArea.append("波特率为115200\n");
            textArea.append("线颜色顺序为：黑、红、蓝、绿\n");
            textArea.append("默认波形图顺序为：四轴角度、四轴油门\n");
            textArea.append("默认波形图比例为3.0");
//        textArea.append(Arrays.toString(data));
        }
    }

    public ArrayList<Double> getRes(byte[] data) {
        if (data == null) {
            return null;
        }
        ArrayList<Integer> res1 = new ArrayList<>();
        for (byte datum : data) {
//            res1.add((datum & 0xFF));
            res1.add((int) datum);
        }
        System.out.println(res1);
        // -------------------------------------  TODO 显示框部分
        ArrayList<Double> res = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("#.##");
        double getDouble1 = Double.parseDouble(df.format(((res1.get(1) << 8) + res1.get(2)) / 100.0 - 180));
        double getDouble2 = Double.parseDouble(df.format(((res1.get(3) << 8) + res1.get(4)) / 100.0 - 180));
        double getDouble3 = Double.parseDouble(df.format(((res1.get(5) << 8) + res1.get(6)) / 100.0 - 180));
        res.add(getDouble1);
        res.add(getDouble2);
        res.add(getDouble3);
        // TODO 运算符优先级
        res.add((double) ((res1.get(7) << 8) + res1.get(8)));
        res.add((double) ((res1.get(9) << 8) + res1.get(10)));
        res.add((double) ((res1.get(11) << 8) + res1.get(12)));
        res.add((double) ((res1.get(13) << 8) + res1.get(14)));
        res.add((double) ((res1.get(15) << 8) + res1.get(16)));
        res.add((double) ((res1.get(17) << 8) + res1.get(18)));
        res.add((double) ((res1.get(19) << 8) + res1.get(20)) / 100.0);
        res.add((double) ((res1.get(21) << 8) + res1.get(22)) / 100.0);
        res.add((double) ((res1.get(23) << 8) + res1.get(24)) / 100.0);
        res.add((double) ((res1.get(25) << 8) + res1.get(26)) / 100.0);
        res.add((double) ((res1.get(27) << 8) + res1.get(28)));
        res.add((double) ((res1.get(29) << 8) + res1.get(30)));
        res.add((double) ((res1.get(31) << 8) + res1.get(32)));
        res.add((double) ((res1.get(33) << 8) + res1.get(34)));
        res.add((double) ((res1.get(35) << 8) + res1.get(36)));
        res.add((double) ((res1.get(37) << 8) + res1.get(38)));
        res.add((double) ((res1.get(39) << 8) + res1.get(40)));
        res.add((double) ((res1.get(41) << 8) + res1.get(42)));
        res.add((double) (((res1.get(43) & 0xFF) << 8) + (res1.get(44) & 0xFF)));
        res.add((double) ((res1.get(45) << 8) + res1.get(46)));
        // --------------------------------------- TODO 波形图部分
        List<Integer> list = new ArrayList<>();
        list.add((int) (getDouble1));
        list.add((int) (getDouble2));
//        list.add((int) (getDouble3));
        dataReceiver.addValue(list); // 产生一个数据，并模拟接收并放到容器里.

        // --------------------------------------- TODO 波形图部分2
        List<Integer> list2 = new ArrayList<>();
        // 电机数据波形
//        list2.add((res1.get(27) << 8) + res1.get(28));
//        list2.add((res1.get(29) << 8) + res1.get(30));
//        list2.add((res1.get(31) << 8) + res1.get(32));
//        list2.add((res1.get(33) << 8) + res1.get(34));
        // 电机单个组成数据波形
//        list2.add((res1.get(35) << 8) + res1.get(36));
//        list2.add((res1.get(37) << 8) + res1.get(38));
//        list2.add((res1.get(39) << 8) + res1.get(40));
//        list2.add((res1.get(41) << 8) + res1.get(42));
        list2.add((res1.get(43) << 8) + res1.get(44));
        list2.add(((res1.get(45) << 8) + res1.get(46)) * 10);
        dataReceiver2.addValue(list2); // 产生一个数据，并模拟接收并放到容器里.
        if (!isStop) {
            repaint();
        }
        return res;
    }

    public static void main(String[] args) {
        new MainForm();
    }
}
