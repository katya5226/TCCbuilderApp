package lahde.tccbuilder.client;

import java.util.Iterator;
import java.util.Vector;


public class MCComponent extends Component {
    public boolean field;
    public Vector<Double> temperaturesST;
    public Vector<Double> lowFieldEntropies;
    public Vector<Double> highFieldEntropies;
    public Vector<Double> derSTlow;
    public Vector<Double> derSThigh;

    public MCComponent(int xx, int yy) {
        super(xx, yy);
    }

    public MCComponent(int xa, int ya, int xb, int yb, int f, StringTokenizer st) {
        super(xa, ya, xb, yb, f, st);

    }

    @Override
    public void initializeComponent() {
        resistance = 1000;
        this.color = Color.white;
        calculateLength();
        this.name = "MCComponent";
        this.num_cvs = 3;
        this.cvs = new Vector<ControlVolume>();
        this.left_resistance = 0.0; // This is yet to be linked to the CV.
        this.right_resistance = 0.0;
        this.left_neighbour = null;
        this.right_neighbour = null;
        this.left_boundary = 51;
        this.right_boundary = 52;
        this.material = sim.materialHashMap.get("Gd");
        this.color = Color.orange;
        this.field = false;
/*        this.temps_rho = new Vector<Double>();
        this.rho_values = new Vector<Double>();
        this.temps_cp = new Vector<Double>();
        this.cp_values = new Vector<Double>();
        this.temps_k = new Vector<Double>();
        this.k_values = new Vector<Double>();*/
        double tmpDx = this.length / this.num_cvs;
        if (!(tmpDx < 1e-6) || tmpDx == 0) {
            this.set_dx(tmpDx);
            sim.simComponents.add(this);
            sim.trackedTemperatures.add(this);
        }
    }

    @Override
    public void buildComponent() {
        //super.buildComponent();
        //readThermalFile(this.material.name);
        material.readFiles();
        this.temperaturesST = new Vector<Double>();
        this.lowFieldEntropies = new Vector<Double>();
        this.highFieldEntropies = new Vector<Double>();
        this.derSTlow = new Vector<Double>();
        this.derSThigh = new Vector<Double>();
        this.cvs.clear();
        for (int i = 0; i < this.num_cvs; i++) {
            this.cvs.add(new MC_CV(i));
        }
        for (int i = 0; i < this.num_cvs; i++) {
            this.cvs.get(i).parent = (MCComponent) this;
        }
    }

/*
    @Override
    void readThermalFile(String str) {
        //Window.alert("In readThermalFile");
        super.readThermalFile(str);
        */
/*
        String url_s_low = GWT.getModuleBaseURL() + "material_data/materials/" + str + "/s_low.txt";
        String url_s_high = GWT.getModuleBaseURL() + "material_data/materials/" + str + "/s_high.txt";
        String url_sder_low = GWT.getModuleBaseURL() + "material_data/materials/" + str + "/sder_low.txt";
        String url_sder_high = GWT.getModuleBaseURL() + "material_data/materials/" + str + "/sder_high.txt";
        *//*

        String url_s_low = GWT.getModuleBaseURL() + "material_data/materials/" + str + "/s_low.txt";
        String url_s_high = GWT.getModuleBaseURL() + "material_data/materials/" + str + "/s_high.txt";
        String url_sder_low = GWT.getModuleBaseURL() + "material_data/materials/" + str + "/sder_low.txt";
        String url_sder_high = GWT.getModuleBaseURL() + "material_data/materials/" + str + "/sder_high.txt";
        loadThermalFileFromURL(url_s_low, "s_low");
        loadThermalFileFromURL(url_s_high, "s_high");
        loadThermalFileFromURL(url_sder_low, "sder_low");
        loadThermalFileFromURL(url_sder_high, "sder_high");
        //Window.alert("Gd Cp: " + String.valueOf(cp_values.get(1)));
    }

    @Override
    void readThermalData(byte b[], String property) {
        int i;
        int len = b.length;
        //boolean subs = (flags & RC_SUBCIRCUITS) != 0;
        //cv.repaint();
        int p;
        int linePos = 0;
        if (property.equals("rho")) {
            this.temps_rho.clear();
            this.rho_values.clear();
        }
        if (property.equals("cp")) {
            this.temps_cp.clear();
            this.cp_values.clear();
        }
        if (property.equals("k")) {
            this.temps_k.clear();
            this.k_values.clear();
        }
        if (property.equals("s_low")) {
            this.temperaturesST.clear();
            this.lowFieldEntropies.clear();
        }
        if (property.equals("s_high")) {
            this.highFieldEntropies.clear();
        }
        if (property.equals("sder_low")) {
            this.derSTlow.clear();
        }
        if (property.equals("sder_high")) {
            this.derSThigh.clear();
        }

        for (p = 0; p < len; ) {
            int l;
            int linelen = len - p; // IES - changed to allow the last line to not end with a delim.
            for (l = 0; l != len - p; l++)
                if (b[l + p] == '\n' || b[l + p] == '\r') {
                    linelen = l++;
                    if (l + p < b.length && b[l + p] == '\n')
                        l++;
                    break;
                }
            String line = new String(b, p, linelen);
            StringTokenizer st = new StringTokenizer(line, " +\t\n\r\f");
            while (st.hasMoreTokens()) {
                // String type = st.nextToken();
                // int tint = type.charAt(0);
                try {
                    double x1 = new Double(st.nextToken()).doubleValue();
                    double y1 = new Double(st.nextToken()).doubleValue();
                    if (property.equals("rho")) {
                        this.temps_rho.add(x1);
                        this.rho_values.add(y1);
                    }
                    if (property.equals("cp")) {
                        this.temps_cp.add(x1);
                        this.cp_values.add(y1);
                    }
                    if (property.equals("k")) {
                        this.temps_k.add(x1);
                        this.k_values.add(y1);
                    }
                    if (property.equals("s_low")) {
                        this.temperaturesST.add(x1);
                        this.lowFieldEntropies.add(y1);
                    }
                    if (property.equals("s_high")) {
                        this.highFieldEntropies.add(y1);
                    }
                    if (property.equals("sder_low")) {
                        this.derSTlow.add(y1);
                    }
                    if (property.equals("sder_high")) {
                        this.derSThigh.add(y1);
                    }

                } catch (Exception ee) {
                    ee.printStackTrace();
                    //console("exception while undumping " + ee);
                    break;
                }
                break;
            }
            p += l;
            linePos++;

        }
*/
/*        String T = "Temperatures: " + String.valueOf(this.temps_k.get(0));
        T += ", " + String.valueOf(this.temps_k.get(1)) + ", " + String.valueOf(this.temps_k.get(2)) + "\n";
        T += "Conductivities: " + String.valueOf(this.k_values.get(0)) + ", " + String.valueOf(this.k_values.get(1)) + ", " + String.valueOf(this.k_values.get(2)) + "\n";*//*

        //Window.alert(T);
    }
*/

    public void magnetize() {
        Iterator i = cvs.iterator();
        while (i.hasNext()) {
            MC_CV cv = (MC_CV) i.next();
            cv.magnetize();
        }
        this.field = !this.field;
    }
}