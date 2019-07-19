import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

class DataReceiver extends JPanel {
    //    private List<Integer> values = new ArrayList<>(); // 保存接收到的数据的容器.
    private List<List<Integer>> values = new ArrayList<>(); // 保存接收到的数据的容器.
    public static final int MAX_VALUE = 200;          // 接收到的数据的最大值.
    public static final int MAX_COUNT_OF_VALUES = 50; // 最多保存数据的个数.
    private int count;

    DataReceiver() {
        this.count = 1;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int xDelta = w / MAX_COUNT_OF_VALUES;
        if (values.isEmpty()) {
            return;
        }
        int length = values.size();

        for (int j = 0; j < count; j++) {
            if (j == 1) {
                g2d.setColor(Color.red);
            }
            if (j == 2) {
                g2d.setColor(Color.blue);
            }
            if (j == 3) {
                g2d.setColor(Color.green);
            }
            for (int i = 0; i < length - 1; ++i) {
                g2d.drawLine(xDelta * (MAX_COUNT_OF_VALUES - length + i),
                        h - normalizeValueForYAxis(values.get(i).get(j), h) - MAX_VALUE / 2,
                        xDelta * (MAX_COUNT_OF_VALUES - length + i + 1),
                        h - normalizeValueForYAxis(values.get(i + 1).get(j), h) - MAX_VALUE / 2);
            }
        }
    }

    /**
     * 接收到的数据放入内存.
     */
    public void addValue(List<Integer> value) {
        count = value.size();
        // 循环的使用一个接收数据的空间.
        // 最好是实现一个循环数组，而不是偷懒的使用ArrayList.
        if (values.size() > MAX_COUNT_OF_VALUES) {
            values.remove(0);
        }
        values.add(value);
    }

    /**
     * 规一化y轴方向的值. 使得value在y轴的值为[0, height]之间.
     */
    private int normalizeValueForYAxis(int value, int height) {
        return (int) ((double) height / MAX_VALUE * value);
    }
}