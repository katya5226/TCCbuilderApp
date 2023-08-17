package lahde.tccbuilder.client;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.user.client.Window;
import lahde.tccbuilder.client.util.Locale;

import java.lang.Math;
import java.util.*;

public class TwoDimComponent extends CircuitElm implements Comparable<TwoDimComponent> {
    double resistance;
    Color color;
    Color color2;
    double length, height;
    String name;
    int index;
    Material material, material2;
    int n;
    int m;
    double[] resistances;
    TwoDimComponent[] neighbours;
    int[] boundaries;
    double constRho, constCp, constK;
    Vector<TwoDimCV> cvs;
    boolean isDisabled;
    boolean field;
    int fieldIndex;
    Point point3, point4;

    TwoDimComponent(int xx, int yy) {
        super(xx, yy);
        initializeComponent();
        index = -1;
        for (TwoDimComponent c : sim.simTwoDimComponents) {
            if (c.index > index) index = c.index;
        }
        index++;
        material = sim.materialHashMap.get("100001-Inox");

        if (!material.isLoaded()) material.readFiles();

        isDisabled = false;
        field = false;
        fieldIndex = 1;
        for (TwoDimComponent twoDimComponent : sim.simTwoDimComponents) {
            if (twoDimComponent.x2 == xx && twoDimComponent.y2 == yy) {
                this.m = twoDimComponent.m;
                this.n = twoDimComponent.n;
            }

        }
        buildComponent();
        double tmpDx = this.length / this.n;
        if (!(tmpDx < 1e-6) || tmpDx == 0) {
            sim.simTwoDimComponents.add(this);
        }
    }

    TwoDimComponent(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super(xa, ya, xb, yb, f);
        initializeComponent();

        index = Integer.parseInt(st.nextToken());

        setPoints();
        point3 = new Point(Integer.parseInt(st.nextToken(" ")), Integer.parseInt(st.nextToken(" ")));
        point4 = new Point(Integer.parseInt(st.nextToken(" ")), Integer.parseInt(st.nextToken(" ")));

        material = sim.materialHashMap.get(st.nextToken(" "));
        if (!material.isLoaded()) {
            material.readFiles();
        }


        color = Color.translateColorIndex(Integer.parseInt(st.nextToken()));

        length = Double.parseDouble(st.nextToken());
        height = Double.parseDouble(st.nextToken());

        constRho = Integer.parseInt(st.nextToken());
        constCp = Integer.parseInt(st.nextToken());
        constK = Integer.parseInt(st.nextToken());

        name = st.nextToken();
        n = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());

        isDisabled = false;
        field = false;
        fieldIndex = 1;
        buildComponent();
        sim.simTwoDimComponents.add(this);
    }

    void initializeComponent() {
        resistance = 1000;
        color = Color.blue;
        name = "TwoDimComponent";
        // n = 24;
        // m = 48;
        n = m = 12;
        cvs = new Vector<TwoDimCV>();
        resistances = new double[]{0.0, 0.0, 0.0, 0.0};
        neighbours = new TwoDimComponent[4];
        boundaries = new int[]{51, 51, 51, 51};
        constRho = -1;
        constCp = -1;
        constK = -1;
    }

    void calculateLengthHeight() {
        // as a rectangle with edge coordinates {(x, y), (x2, y), (x2, y2), (x, y2)},
        // where difference between x-es is length and difference between y-s is height.
        if (point3 == null | point4 == null) return;
        length = Math.abs(point1.x - point2.x) / sim.selectedLengthUnit.conversionFactor;
        height = Math.abs(point1.y - point4.y) / sim.selectedLengthUnit.conversionFactor;
        //gridSize px = 1 unit of measurement
        length /= sim.gridSize;
        height /= sim.gridSize;


        TwoDimTCCmanager.setdxdy(this.cvs, length / n, height / m);
    }

    void cvNeighbours() {
        for (int j = 0; j < m; j++) {
            for (int i = 0; i < n; i++) {
                int k = j * n + i;
                TwoDimCV cv = cvs.get(k);
                if (j == 0 && m != 1) {
                    cv.neighbours[3] = cvs.get(k + n);
                    if (i == 0) {
                        cv.neighbours[1] = cvs.get(k + 1);
                    } else if (i == n - 1) {
                        cv.neighbours[0] = cvs.get(k - 1);
                    } else {
                        cv.neighbours[0] = cvs.get(k - 1);
                        cv.neighbours[1] = cvs.get(k + 1);
                    }
                } else if ((0 < j) && (j < m - 1)) {
                    cv.neighbours[3] = cvs.get(k + n);
                    cv.neighbours[2] = cvs.get(k - n);
                    if (i == 0) {
                        cv.neighbours[1] = cvs.get(k + 1);
                    } else if (i == n - 1) {
                        cv.neighbours[0] = cvs.get(k - 1);
                    } else {
                        cv.neighbours[0] = cvs.get(k - 1);
                        cv.neighbours[1] = cvs.get(k + 1);
                    }
                } else if (j == m - 1) {
                    cv.neighbours[2] = cvs.get(k - n);
                    if (i == 0) {
                        cv.neighbours[1] = cvs.get(k + 1);
                    } else if (i == n - 1) {
                        cv.neighbours[0] = cvs.get(k - 1);
                    } else {
                        cv.neighbours[0] = cvs.get(k - 1);
                        cv.neighbours[1] = cvs.get(k + 1);
                    }
                }
            }
        }
    }

    void buildComponent() {
        cvs.clear();
        for (int i = 0; i < n * m; i++) {
            cvs.add(new TwoDimCV(i, this));
            if (constRho != -1) {
                cvs.get(i).constRho = constRho;
            }
            if (constCp != -1) {
                cvs.get(i).constCp = constCp;
            }
            if (constK != -1) {
                cvs.get(i).constK = constK;
            }
        }
        for (int j = 0; j < m; j++) {
            cvs.get(j * n).resistances[0] = resistances[0];
        }
        for (int j = 0; j < m; j++) {
            cvs.get((j + 1) * n - 1).resistances[1] = resistances[1];
        }
        for (int i = 0; i < n; i++) {
            cvs.get(i).resistances[2] = resistances[2];
        }
        for (int i = (m - 1) * n; i < n * m; i++) {
            cvs.get(i).resistances[3] = resistances[3];
        }
        cvNeighbours();
        //calculateConductivities();
        calculateLengthHeight();
        TwoDimTCCmanager.setxy(cvs, 0.0, 0.0);
        TwoDimTCCmanager.setdxdy(cvs, length / this.n, height / this.m);

    }

    @Override
    public int compareTo(TwoDimComponent o) {
        return index - o.index;
    }

    @Override
    void drag(int xx, int yy) {
        int oldX = xx > x ? sim.snapGrid(xx) : x;
        int oldY = yy > y ? sim.snapGrid(yy) : y;

        if (point1 != null) for (TwoDimComponent twoDimComponent : sim.simTwoDimComponents) {
            if (twoDimComponent.x2 == point1.x && twoDimComponent.y2 == point1.y) {
                oldY = twoDimComponent.point4.y;
            }
        }
        x2 = oldX;
        y2 = y;
        setPoints();
        point3 = new Point(point1.x, oldY);
        point4 = new Point(oldX, oldY);

    }

    @Override
    void move(int dx, int dy) {
        super.move(dx, dy);
        point3.x += dx;
        point4.x += dx;
        point3.y += dy;
        point4.y += dy;
    }

    @Override
    int getDumpType() {
        return 521;
    }

    @Override
    String dump() {
        StringBuilder sb = new StringBuilder();

        sb.append(super.dump()).append(" ");
        sb.append(index).append(" ");

        sb.append(point3.x).append(' ').append(point3.y).append(' ');
        sb.append(point4.x).append(' ').append(point4.y).append(' ');

        sb.append(material.materialName).append(' ');
        sb.append(Color.colorToIndex(color)).append(' ');

        sb.append(length).append(' ').append(height).append(' ');

        sb.append(constRho).append(' ');
        sb.append(constCp).append(' ');
        sb.append(constK).append(' ');


        sb.append(name).append(' ');
        sb.append(n).append(' ');
        sb.append(m).append(' ');

        return sb.toString();
    }


    @Override
    void draw(Graphics g) {
        boundingBox.setBounds(x, y, Math.abs(x - x2), Math.abs(y - point4.y));
        double tmpDx = length / n;
        double tmpDy = height / m;
        if (sim.viewTempsOverlay) drawCVTemperatures(g, point1, point4);
        else drawCVMaterials(g, point1, point4);

        if (tmpDx < 1e-6 && tmpDx != 0) {
            //Window.alert("TwoDimComponent can't have a dx < 1µ, current is " + tmpDx);
            isDisabled = true;
        } else {
            isDisabled = false;
        }
        TwoDimTCCmanager.setdxdy(cvs, tmpDx, tmpDy);

        doDots(g);

    }

    void drawCVMaterials(Graphics g, Point pa, Point pb) {
        Context2d ctx = g.context;
        double x = Math.min(pa.x, pb.x);
        double y = Math.min(pa.y, pb.y);
        double width = Math.abs(pa.x - pb.x);
        double cvWidth = width / n;
        double height = Math.abs(pa.y - pb.y);
        double cvHeight = height / m;
        ctx.setStrokeStyle(Color.deepBlue.getHexValue());
        ctx.setLineWidth(0.5);
        ctx.strokeRect(x, y, width, height);

        for (TwoDimCV cv : cvs) {
            double cvX = x + cv.xIndex * cvWidth;
            double cvY = y + cv.yIndex * cvHeight;
            ctx.setFillStyle(color.getHexValue());
            ctx.strokeRect(cvX, cvY, cvWidth, cvHeight);
            ctx.fillRect(cvX, cvY, cvWidth, cvHeight);
        }
    }

    void drawCVTemperatures(Graphics g, Point pa, Point pb) {
        Context2d ctx = g.context;
        double x = Math.min(pa.x, pb.x);
        double y = Math.min(pa.y, pb.y);
        double width = Math.abs(pa.x - pb.x);
        double cvWidth = width / n;
        double height = Math.abs(pa.y - pb.y);
        double cvHeight = height / m;
        ctx.setStrokeStyle(Color.deepBlue.getHexValue());
        ctx.setLineWidth(0.5);
        ctx.strokeRect(x, y, width, height);
        int i = 0;
        for (TwoDimCV cv : cvs) {
            double cvX = x + cv.xIndex * cvWidth;
            double cvY = y + cv.yIndex * cvHeight;

            double temperatureRange = sim.maxTemp - sim.minTemp;
            double temperatureRatio = (cv.temperature - sim.minTemp) / temperatureRange;

            // Just for testing of color mixing, comment out when not needed
            // temperatureRatio = ((double) i % n) / n;
            // i++;
            //

            Color color1 = Color.blue;
            Color color2 = Color.white;
            Color color3 = Color.red;

            int red = (int) (color1.getRed() * (1 - temperatureRatio) + color2.getRed() * temperatureRatio);
            int green = (int) (color1.getGreen() * (1 - temperatureRatio) + color2.getGreen() * temperatureRatio);
            int blue = (int) (color1.getBlue() * (1 - temperatureRatio) + color2.getBlue() * temperatureRatio);

            red = (int) (red * (1 - temperatureRatio) + color3.getRed() * temperatureRatio);
            green = (int) (green * (1 - temperatureRatio) + color3.getGreen() * temperatureRatio);
            blue = (int) (blue * (1 - temperatureRatio) + color3.getBlue() * temperatureRatio);

            String cvColor = "#" + Integer.toHexString(red) + Integer.toHexString(green) + Integer.toHexString(blue);
            ctx.setFillStyle(cvColor.equals("#000") ? color.getHexValue() : cvColor);
            ctx.strokeRect(cvX, cvY, cvWidth, cvHeight);
            ctx.fillRect(cvX, cvY, cvWidth, cvHeight);
        }

    }


    void getInfo(String[] arr) {
        arr[0] = name;
        // getBasicInfo(arr);
        arr[1] = "TwoDimComponent index = " + String.valueOf(index);
        arr[2] = "Material = " + material.materialName;
        arr[3] = "Length = " + sim.formatLength(length);
        arr[4] = "Height = " + sim.formatLength(height);
        arr[5] = "#CVs (x) = " + n;
        arr[6] = "#CVs (y) = " + m;
        arr[7] = "CV dx = " + sim.formatLength(cvs.get(0).dx);
        arr[8] = "CV dy = " + sim.formatLength(cvs.get(0).dy);
    }


    @Override
    String getScopeText(int v) {
        return lahde.tccbuilder.client.util.Locale.LS("component") + ", " + getUnitText(resistance, Locale.ohmString);
    }

    public EditInfo getEditInfo(int n) {
        switch (n) {
            case 0:
                return new EditInfo("Name", String.valueOf(name));
            case 1:
                return new EditInfo("Index", index);
            case 2:
                return new EditInfo("X-discretization number", this.n);
            case 3:
                return new EditInfo("Y-discretization number", this.m);
            case 4:
                EditInfo ei = new EditInfo("Material", 0);
                ei.choice = new Choice();
                for (String m : sim.materialNames) {
                    ei.choice.add(m);
                }
                ei.choice.select(sim.materialNames.indexOf(material.materialName));
                return ei;
            case 5:
                EditInfo ei2 = new EditInfo("Color", 0);
                ei2.choice = new Choice();
                for (int ch = 0; ch < sim.colorChoices.size(); ch++) {
                    ei2.choice.add(sim.colorChoices.get(ch));
                }
                ei2.choice.select(Color.colorToIndex(color));

                return ei2;
            case 6:
                return new EditInfo("Length (" + sim.selectedLengthUnit.unitName + ")", length * CircuitElm.sim.selectedLengthUnit.conversionFactor);
            case 7:
                return new EditInfo("Height (" + sim.selectedLengthUnit.unitName + ")", height * CircuitElm.sim.selectedLengthUnit.conversionFactor);
            case 8:
                return new EditInfo("Left contact resistance (mK/W)", resistances[0]);
            case 9:
                return new EditInfo("Right contact resistance (mK/W)", resistances[1]);
            default:
                return null;
        }
    }

    @Override
    public void setEditValue(int n, EditInfo ei) {
        switch (n) {
            case 0:
                name = ei.textf.getText();
                break;
            case 1:
                index = (int) ei.value;
                break;
            case 2:
                this.n = (int) ei.value;
                break;
            case 3:
                this.m = (int) ei.value;
                for (TwoDimComponent twoDimComponent : sim.simTwoDimComponents)
                    if (twoDimComponent.x2 == point1.x && twoDimComponent.y2 == point1.y || twoDimComponent.x == point2.x && twoDimComponent.y == point2.y)
                        if (twoDimComponent.m != m) Window.alert("Y-discretization numbers should not differ!");


                break;
            case 4:
                material = sim.materialHashMap.get(sim.materialNames.get(ei.choice.getSelectedIndex()));
                if (!material.isLoaded()) material.readFiles();

                break;
            case 5:
                color = Color.translateColorIndex(ei.choice.getSelectedIndex());
                break;
            case 6:
                double prevLength = length;
                length = (ei.value / sim.selectedLengthUnit.conversionFactor);

                double ratio = length / prevLength;
                int deltaX = (int) (Math.abs(point2.x - point1.x) * ratio);
                drag(sim.snapGrid(point1.x + deltaX), point4.y);
                break;
            case 7:
                double prevHeight = height;
                height = (ei.value / sim.selectedLengthUnit.conversionFactor);

                double ratio2 = height / prevHeight;
                int deltaY = (int) (Math.abs(point1.y - point4.y) * ratio2);

                drag(point4.x, sim.snapGrid(point1.y + deltaY));

                break;
            case 8:
                resistances[0] = ei.value;
                break;
            case 9:
                resistances[1] = ei.value;
                break;

        }
        buildComponent();
    }

    @Override
    void stamp() {
        sim.stampResistor(nodes[0], nodes[1], resistance);
    }
}
