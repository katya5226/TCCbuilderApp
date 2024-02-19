/*    
    Copyright (C) Paul Falstad and Iain Sharp
    
    This file is part of CircuitJS1.

    CircuitJS1 is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 2 of the License, or
    (at your option) any later version.

    CircuitJS1 is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with CircuitJS1.  If not, see <http://www.gnu.org/licenses/>.
*/

// Test by Oskar

package lahde.tccbuilder.client;

// GWT conversion (c) 2015 by Iain Sharp

// For information about the theory behind this, see Electronic Circuit & System Simulation Methods by Pillage
// or https://github.com/sharpie7/circuitjs1/blob/master/INTERNALS.md

import java.util.*;
// import java.security.InvalidParameterException;
// import java.util.InputMismatchException;
import java.lang.Math;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.storage.client.Storage;

import static com.google.gwt.event.dom.client.KeyCodes.*;

import lahde.tccbuilder.client.util.Locale;
import lahde.tccbuilder.client.util.PerfMonitor;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.Window.Navigator;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.canvas.dom.client.TextMetrics;

@SuppressWarnings({"Convert2Lambda", "CStyleArrayDeclaration", "ReassignedVariable"})
public class CirSim implements MouseDownHandler, MouseMoveHandler, MouseUpHandler, ClickHandler, DoubleClickHandler, ContextMenuHandler, NativePreviewHandler, MouseOutHandler, MouseWheelHandler {

    Random random;
    Button resetButton;
    Button quickResetButton;
    Button testButton;
    Button runStopButton;
    MenuItem aboutItem;
    MenuItem importFromLocalFileItem, importFromTextItem, exportAsUrlItem, exportAsLocalFileItem, reportAsLocalFileItem, exportAsTextItem, reportAsTextItem, printItem, recoverItem, saveReportItem, saveFileItem;
    MenuItem importFromDropboxItem;
    MenuItem undoItem, redoItem, cutItem, copyItem, pasteItem, selectAllItem, optionsItem;
    MenuBar optionsMenuBar;
    CheckboxMenuItem showTemperaturesCheckItem;
    CheckboxMenuItem showOverlayCheckItem;
    CheckboxMenuItem customTempRangeCheckItem;
    CheckboxMenuItem outputIntervalItem;
    CheckboxMenuItem smallGridCheckItem;
    CheckboxMenuItem crossHairCheckItem;
    CheckboxMenuItem noEditCheckItem;
    CheckboxMenuItem mouseWheelEditCheckItem;
    Label titleLabel;
    Scrollbar speedBar;
    ListBox scale, dimensionality;

    MenuBar elmMenuBar;
    MenuItem elmEditMenuItem;
    MenuItem elmCutMenuItem;
    MenuItem elmCopyMenuItem;
    MenuItem elmDeleteMenuItem;
    MenuItem elmTempsMenuItem;
    MenuItem elmFlipMenuItem;
    MenuItem elmSplitMenuItem;
    MenuItem elmSliderMenuItem;
    MenuBar mainMenuBar, drawMenuBar;

    StartDialog startDialog;
    TemperaturesDialog tempsDialog;
    IntervalDialog intervalDialog;

    String lastCursorStyle;
    boolean mouseWasOverSplitter = false;

    PopupPanel contextPanel = null;
    int mouseMode = MODE_SELECT;
    int tempMouseMode = MODE_SELECT;
    String mouseModeStr = "Select";
    static final int MODE_ADD_ELM = 0;
    static final int MODE_DRAG_ALL = 1;
    static final int MODE_DRAG_ROW = 2;
    static final int MODE_DRAG_COLUMN = 3;
    static final int MODE_DRAG_SELECTED = 4;
    static final int MODE_DRAG_POST = 5;
    static final int MODE_SELECT = 6;
    static final int MODE_DRAG_SPLITTER = 7;
    int dragGridX, dragGridY, dragScreenX, dragScreenY, initDragGridX, initDragGridY;
    long mouseDownTime;
    long zoomTime;
    int mouseCursorX = -1;
    int mouseCursorY = -1;
    Rectangle selectedArea;
    int gridSize, gridMask, gridRound;
    boolean dragging;
    boolean dcAnalysisFlag;
    // boolean useBufferedImage;
    boolean isMac;
    String ctrlMetaKey;
    double t;
    int hintType = -1, hintItem1, hintItem2;
    String stopMessage;

    // current timestep (time between iterations)
    double timeStep;
    boolean elementToggled = false;
    // maximum timestep (== timeStep unless we reduce it because of trouble
    // converging)
    double maxTimeStep;
    double minTimeStep;

    // accumulated time since we incremented timeStepCount
    double timeStepAccum;

    // incremented each time we advance t by maxTimeStep
    int timeStepCount;

    boolean developerMode;
    Vector<CircuitElm> elmList;
    Vector<Adjustable> adjustables;
    CircuitElm dragElm, menuElm, stopElm;
    Graphics g;


    private CircuitElm mouseElm = null;
    int mousePost = -1;
    CircuitElm plotXElm, plotYElm;
    int draggingPost;
    boolean simRunning;
    ArrayList<ThermalControlElement> trackedTemperatures;
    boolean hideInfoBox;
    static Dialog editDialog, customLogicEditDialog, diodeModelEditDialog;
    static ScrollValuePopup scrollValuePopup;
    static Dialog dialogShowing;
    static AboutBox aboutBox;
    // Class dumpTypes[], shortcuts[];
    String shortcuts[];
    String clipboard;
    String recovery;
    Rectangle circuitArea;
    Vector<UndoItem> undoStack, redoStack;
    double transform[];
    boolean unsavedChanges;

    DockLayoutPanel layoutPanel;
    Element loadingScreen;
    MenuBar menuBar;
    MenuBar fileMenuBar;
    FlowPanel verticalPanel;
    FlowPanel simulationConsolePanel;
    FlowPanel cyclicPanel;
    FlowPanel buttonPanel;
    private boolean mouseDragging;

    Vector<CheckboxMenuItem> mainMenuItems = new Vector<CheckboxMenuItem>();
    Vector<String> mainMenuItemNames = new Vector<String>();

    LoadFile loadFileInput;
    Frame iFrame;

    Canvas canvas;
    Context2d cvcontext;

    // canvas width/height in px (before device pixel ratio scaling)
    int canvasWidth, canvasHeight;

    static final int MENUBARHEIGHT = 30;
    static int VERTICALPANELWIDTH = 272; // default
    static final int POSTGRABSQ = 25;
    static final int MINPOSTGRABSIZE = 256;
    final Timer timer = new Timer() {
        public void run() {
            updateCircuit();
        }
    };
    final int FASTTIMER = 16;

    // ************************************** Katni
    public Vector<String> materialNames;
    public HashMap<String, Material> materialHashMap;
    public Vector<String> colorChoices;
    public Simulation1D simulation1D;
    public Simulation2D simulation2D;


    boolean viewTempsInGraph = true;
    boolean viewTempsOverlay = false;
    public String materialFlagText;
    ArrayList<String> awaitedResponses;
    final Timer responseTimer = new Timer() {
        public void run() {
            loadingScreen.getStyle().setProperty("display", "flex");
            if (awaitedResponses.isEmpty()) {
                if (stopMessage != null) return;
                simRunning = true;
                runStopButton.setHTML(Locale.LSHTML("<strong>RUN</strong>&nbsp;/&nbsp;Stop"));
                runStopButton.removeStyleName("topButton-red");
                timer.scheduleRepeating(FASTTIMER);
                responseTimer.cancel();
                loadingScreen.getStyle().setProperty("display", "none");
            }

        }
    };
    final Timer displayTimer = new Timer() {
        public void run() {
            if (awaitedResponses.isEmpty()) {
                if (stopMessage != null) return;
                if (simulation1D.cyclic) {
                    fillCyclicPanel();
                }
                displayTimer.cancel();
            }

        }
    };
    int testCounter = 0;
    Timer testTimer;

    // Two-dimensional choice
    int simDimensionality = 1;

    public enum LengthUnit {
        MICROMETER(1e6, "µm", "micrometer"),
        MICROMETER_10(1e5, "10µm", "10 micrometers"),
        MICROMETER_20(5e4, "20µm", "20 micrometers"),
        MICROMETER_50(2e4, "50µm", "50 micrometers"),
        MICROMETER_100(1e4, "100µm", "100 micrometers"),
        MICROMETER_200(5e3, "200µm", "200 micrometers"),
        MICROMETER_500(2e3, "500µm", "500 micrometers"),
        MILLIMETER(1e3, "mm", "millimeter"),
        MILLIMETER_5(2e2, "5mm", "5 millimeters"),
        CENTIMETER(1e2, "cm", "centimeter"),
        CENTIMETER_5(2e1, "5cm", "5 centimeters"),
        DECIMETER(1e1, "dm", "decimeter"),
        METER(1, "m", "meter");

        final double conversionFactor;
        final String unitName;
        final String longName;

        LengthUnit(double conversionFactor, String unitName, String longName) {
            this.conversionFactor = conversionFactor;
            this.unitName = unitName;
            this.longName = longName;
        }

        @Override
        public String toString() {
            return longName;
        }
    }


    public LengthUnit selectedLengthUnit = LengthUnit.MILLIMETER;

    public String baseURL;
    static native float devicePixelRatio() /*-{
        return window.devicePixelRatio;
    }-*/;

    void checkCanvasSize() {
        if (canvas.getCoordinateSpaceWidth() != (int) (canvasWidth * devicePixelRatio())) setCanvasSize();
    }

    public void setCanvasSize() {
        int width, height;
        width = (int) RootLayoutPanel.get().getOffsetWidth();
        height = (int) RootLayoutPanel.get().getOffsetHeight();
        height = height - MENUBARHEIGHT;
        width = width - VERTICALPANELWIDTH;
        if (simulation1D != null && simulation1D.cyclic) width -= VERTICALPANELWIDTH;

        width = Math.max(width, 0); // avoid exception when setting negative width
        height = Math.max(height, 0);
        if (canvas != null) {
            canvas.setWidth(width + "PX");
            canvas.setHeight(height + "PX");
            canvasWidth = width;
            canvasHeight = height;
            float scale = devicePixelRatio();
            canvas.setCoordinateSpaceWidth((int) (width * scale));
            canvas.setCoordinateSpaceHeight((int) (height * scale));
        }

        setCircuitArea();

        // recenter circuit in case canvas was hidden at startup
        if (transform[0] == 0) centreCircuit();
    }

    void setCircuitArea() {
        int height = canvasHeight;
        int width = canvasWidth;
        int h = 0;
        circuitArea = new Rectangle(0, 0, width, height - h);
    }

    native String decompress(String dump) /*-{
        return $wnd.LZString.decompressFromEncodedURIComponent(dump);
    }-*/;

    // Circuit applet;

    CirSim() {
        // super("Circuit Simulator v1.6d");
        // applet = a;
        // useFrame = false;
        theSim = this;

    }

    String startCircuit = null;
    String startLabel = null;
    String startCircuitText = null;
    String startCircuitLink = null;
    // String baseURL = "http://www.falstad.com/circuit/";

    public void init() {

        boolean printable = false;
        boolean convention = true;
        boolean euroRes = false;
        boolean usRes = false;
        boolean running = false; // Katni
        boolean hideSidebar = false;
        boolean hideMenu = false;
        boolean noEditing = false;
        boolean mouseWheelEdit = false;
        MenuBar m;

        CircuitElm.initClass(this);
        readRecovery();

        QueryParameters qp = new QueryParameters();
        String positiveColor = null;
        String negativeColor = null;
        String neutralColor = null;
        String selectColor = null;
        String currentColor = null;
        String mouseModeReq = null;
        boolean euroGates = false;

        try {
            // baseURL = applet.getDocumentBase().getFile();
            // look for circuit embedded in URL
            // String doc = applet.getDocumentBase().toString();
            String cct = qp.getValue("cct");
            if (cct != null) startCircuitText = cct.replace("%24", "$");
            if (startCircuitText == null) startCircuitText = getElectronStartCircuitText();
            String ctz = qp.getValue("ctz");
            if (ctz != null) startCircuitText = decompress(ctz);
            startCircuit = qp.getValue("startCircuit");
            startLabel = qp.getValue("startLabel");
            startCircuitLink = qp.getValue("startCircuitLink");
            euroRes = qp.getBooleanValue("euroResistors", false);
            euroGates = qp.getBooleanValue("IECGates", getOptionFromStorage("euroGates", weAreInGermany()));
            usRes = qp.getBooleanValue("usResistors", false);
            running = qp.getBooleanValue("running", false); // Katni
            hideSidebar = qp.getBooleanValue("hideSidebar", false);
            hideMenu = qp.getBooleanValue("hideMenu", false);
            printable = qp.getBooleanValue("whiteBackground", getOptionFromStorage("whiteBackground", false));
            convention = qp.getBooleanValue("conventionalCurrent", getOptionFromStorage("conventionalCurrent", true));
            noEditing = !qp.getBooleanValue("editable", true);
            mouseWheelEdit = qp.getBooleanValue("mouseWheelEdit", getOptionFromStorage("mouseWheelEdit", true));
            positiveColor = qp.getValue("positiveColor");
            negativeColor = qp.getValue("negativeColor");
            neutralColor = qp.getValue("neutralColor");
            selectColor = qp.getValue("selectColor");
            currentColor = qp.getValue("currentColor");
            mouseModeReq = qp.getValue("mouseMode");
            hideInfoBox = qp.getBooleanValue("hideInfoBox", false);
        } catch (Exception e) {
        }

        boolean euroSetting = false;
        if (euroRes) euroSetting = true;
        else if (usRes) euroSetting = false;
        else euroSetting = getOptionFromStorage("euroResistors", !weAreInUS(true));

        transform = new double[6];
        String os = Navigator.getPlatform();
        isMac = (os.toLowerCase().contains("mac"));
        ctrlMetaKey = (isMac) ? Locale.LS("Cmd-") : Locale.LS("Ctrl-");

        shortcuts = new String[127];

//        layoutPanel = new DockLayoutPanel(Unit.PX);

        fileMenuBar = new MenuBar(true);

        if (isElectron())
            fileMenuBar.addItem(menuItemWithShortcut("window", "New Window", Locale.LS(ctrlMetaKey + "N"), new MyCommand("file", "newwindow")));

        fileMenuBar.addItem(iconMenuItem("doc-new", "New Blank Circuit", new MyCommand("file", "newblankcircuit")));
        importFromLocalFileItem = menuItemWithShortcut("folder", "Open File", Locale.LS(ctrlMetaKey + "O"), new MyCommand("file", "importfromlocalfile"));
        importFromLocalFileItem.setEnabled(LoadFile.isSupported());
        fileMenuBar.addItem(importFromLocalFileItem);
        importFromTextItem = iconMenuItem("doc-text", "Import From Text", new MyCommand("file", "importfromtext"));
        fileMenuBar.addItem(importFromTextItem);
        importFromDropboxItem = iconMenuItem("dropbox", "Import From Dropbox", new MyCommand("file", "importfromdropbox"));

//        fileMenuBar.addItem(importFromDropboxItem);
        if (isElectron()) {
            saveFileItem = fileMenuBar.addItem(menuItemWithShortcut("floppy", "Save", Locale.LS(ctrlMetaKey + "S"), new MyCommand("file", "save")));
            fileMenuBar.addItem(iconMenuItem("floppy", "Save Circuit As", new MyCommand("file", "saveas")));
            saveReportItem = fileMenuBar.addItem(menuItemWithShortcut("floppy", "Save", Locale.LS(ctrlMetaKey + "R"), new MyCommand("file", "save")));
            fileMenuBar.addItem(iconMenuItem("floppy", "Save Report As", new MyCommand("file", "savereport")));
        } else {
            exportAsLocalFileItem = menuItemWithShortcut("floppy", "Save Circuit As", Locale.LS(ctrlMetaKey + "S"), new MyCommand("file", "exportaslocalfile"));
            exportAsLocalFileItem.setEnabled(ExportAsLocalFileDialog.downloadIsSupported());
            fileMenuBar.addItem(exportAsLocalFileItem);
            reportAsLocalFileItem = menuItemWithShortcut("floppy", "Save Report As", "", new MyCommand("file", "reportaslocalfile"));
            fileMenuBar.addItem(reportAsLocalFileItem);
        }
        exportAsUrlItem = iconMenuItem("export", "Export Circuit As Link", new MyCommand("file", "exportasurl"));
        fileMenuBar.addItem(exportAsUrlItem);
        exportAsTextItem = iconMenuItem("export", "Export Circuit As Text", new MyCommand("file", "exportastext"));
        fileMenuBar.addItem(exportAsTextItem);
        reportAsTextItem = iconMenuItem("export", "Export Report As Text", new MyCommand("file", "reportastext"));
        fileMenuBar.addItem(reportAsTextItem);
        fileMenuBar.addItem(iconMenuItem("export", "Export Circuit As Image", new MyCommand("file", "exportasimage")));
        fileMenuBar.addItem(iconMenuItem("export", "Export Circuit As SVG", new MyCommand("file", "exportassvg")));
        recoverItem = iconMenuItem("back-in-time", "Recover Auto-Save", new MyCommand("file", "recover"));
        recoverItem.setEnabled(recovery != null);
        fileMenuBar.addItem(recoverItem);
        printItem = menuItemWithShortcut("print", "Print", Locale.LS(ctrlMetaKey + "P"), new MyCommand("file", "print"));
        fileMenuBar.addItem(printItem);
        fileMenuBar.addSeparator();
        fileMenuBar.addItem(iconMenuItem("resize-full-alt", "Toggle Full Screen", new MyCommand("view", "fullscreen")));
        fileMenuBar.addSeparator();
        aboutItem = iconMenuItem("info-circled", "About", (Command) null);
        fileMenuBar.addItem(aboutItem);
        aboutItem.setScheduledCommand(new MyCommand("file", "about"));


        loadingScreen = Document.get().getElementById("loadingScreen");
        loadingScreen.getStyle().setProperty("display", "none");

        menuBar = new MenuBar();
        menuBar.addItem(Locale.LS("File"), fileMenuBar);

        verticalPanel = new FlowPanel();
        verticalPanel.addStyleName("mainVertical");

        simulationConsolePanel = new FlowPanel();
        simulationConsolePanel.addStyleName("simulationConsole");

        cyclicPanel = new FlowPanel();
        cyclicPanel.addStyleName("cyclicPanel");

        verticalPanel.add(cyclicPanel);
        verticalPanel.add(simulationConsolePanel);

        buttonPanel = new FlowPanel();
        buttonPanel.addStyleName("buttonPanel");
        m = new MenuBar(true);
        m.addItem(undoItem = menuItemWithShortcut("ccw", "Undo", Locale.LS(ctrlMetaKey + "Z"), new MyCommand("edit", "undo")));
        m.addItem(redoItem = menuItemWithShortcut("cw", "Redo", Locale.LS(ctrlMetaKey + "Y"), new MyCommand("edit", "redo")));
        m.addSeparator();
        m.addItem(cutItem = menuItemWithShortcut("scissors", "Cut", Locale.LS(ctrlMetaKey + "X"), new MyCommand("edit", "cut")));
        m.addItem(copyItem = menuItemWithShortcut("copy", "Copy", Locale.LS(ctrlMetaKey + "C"), new MyCommand("edit", "copy")));
        m.addItem(pasteItem = menuItemWithShortcut("paste", "Paste", Locale.LS(ctrlMetaKey + "V"), new MyCommand("edit", "paste")));
        pasteItem.setEnabled(false);

        m.addItem(menuItemWithShortcut("clone", "Duplicate", Locale.LS(ctrlMetaKey + "D"), new MyCommand("edit", "duplicate")));

        m.addSeparator();
        m.addItem(selectAllItem = menuItemWithShortcut("select-all", "Select All", Locale.LS(ctrlMetaKey + "A"), new MyCommand("edit", "selectAll")));
        m.addSeparator();
        m.addItem(menuItemWithShortcut("search", "Find Component...", "/", new MyCommand("edit", "search")));
        m.addItem(iconMenuItem("target", weAreInUS(false) ? "Center Circuit" : "Centre Circuit", new MyCommand("edit", "centrecircuit")));
        m.addItem(menuItemWithShortcut("zoom-11", "Zoom 100%", "0", new MyCommand("zoom", "zoom100")));
        m.addItem(menuItemWithShortcut("zoom-in", "Zoom In", "+", new MyCommand("zoom", "zoomin")));
        m.addItem(menuItemWithShortcut("zoom-out", "Zoom Out", "-", new MyCommand("zoom", "zoomout")));
        menuBar.addItem(Locale.LS("Edit"), m);

        drawMenuBar = new MenuBar(true);
        drawMenuBar.setAutoOpen(true);

        menuBar.addItem(Locale.LS("Draw"), drawMenuBar);


        optionsMenuBar = m = new MenuBar(true);
        menuBar.addItem(Locale.LS("Options"), optionsMenuBar);
        m.addItem(showTemperaturesCheckItem = new CheckboxMenuItem(Locale.LS("Show Temperatures in Graph"), new Command() {
            public void execute() {
                viewTempsInGraph = !viewTempsInGraph;
            }
        }));
        showTemperaturesCheckItem.setState(true);
        m.addItem(showOverlayCheckItem = new CheckboxMenuItem(Locale.LS("Show Temperature Overlay"), new Command() {
            public void execute() {
                viewTempsOverlay = !viewTempsOverlay;
            }
        }));
        showOverlayCheckItem.setState(false);

        m.addItem(customTempRangeCheckItem = new CheckboxMenuItem(Locale.LS("Set custom temperature range"), new Command() {
            public void execute() {
                tempsDialog = new TemperaturesDialog(theSim);
                if (theSim.simDimensionality == 1) {
                    simulation1D.customTempRange = !simulation1D.customTempRange;
                    if (simulation1D.customTempRange == false) {
                        tempsDialog.closeDialog();
                        simulation1D.setTemperatureRange();
                    }
                } else if (theSim.simDimensionality == 2) {
                    simulation2D.customTempRange = !simulation2D.customTempRange;
                    if (simulation2D.customTempRange == false) {
                        tempsDialog.closeDialog();
                        simulation2D.setTemperatureRange();
                    }
                }
            }
        }));
        customTempRangeCheckItem.setState(false);

        smallGridCheckItem = new CheckboxMenuItem(Locale.LS("Small Grid"), new Command() {
            public void execute() {
                setGrid();
            }
        });
        crossHairCheckItem = new CheckboxMenuItem(Locale.LS("Show Cursor Cross Hairs"), new Command() {
            public void execute() {
                setOptionInStorage("crossHair", crossHairCheckItem.getState());
            }
        });
        crossHairCheckItem.setState(getOptionFromStorage("crossHair", false));


        noEditCheckItem = new CheckboxMenuItem(Locale.LS("Disable Editing"));
        noEditCheckItem.setState(noEditing);

        m.addItem(mouseWheelEditCheckItem = new CheckboxMenuItem(Locale.LS("Edit Values With Mouse Wheel"), new Command() {
            public void execute() {
                setOptionInStorage("mouseWheelEdit", mouseWheelEditCheckItem.getState());
            }
        }));
        mouseWheelEditCheckItem.setState(mouseWheelEdit);

        m.addItem(outputIntervalItem = new CheckboxMenuItem(Locale.LS("Set custom output interval"), new Command() {
            public void execute() {
                intervalDialog = new IntervalDialog(theSim);
            }
        }));
        mouseWheelEditCheckItem.setState(false);

        new CheckboxAlignedMenuItem(Locale.LS("Shortcuts..."), new MyCommand("options", "shortcuts"));
        optionsItem = new CheckboxAlignedMenuItem(Locale.LS("Other Options..."), new MyCommand("options", "other"));
        if (isElectron())
            m.addItem(new CheckboxAlignedMenuItem(Locale.LS("Toggle Dev Tools"), new MyCommand("options", "devtools")));

        mainMenuBar = new MenuBar(true);
        mainMenuBar.setAutoOpen(true);


        canvas = Canvas.createIfSupported();
        if (canvas == null) {
            RootPanel.get().add(new Label("Not working. You need a browser that supports the CANVAS element."));
            return;
        }

        updateDrawMenus();
        drawLayoutPanel(hideSidebar, hideMenu);
        Window.addResizeHandler(new ResizeHandler() {
            public void onResize(ResizeEvent event) {
                repaint();
            }
        });

        cvcontext = canvas.getContext2d();
        setCanvasSize();
//        layoutPanel.add(canvas);
        simulationConsolePanel.add(buttonPanel);
        //buttonPanel.add(resetButton = new Button(Locale.LS("Reset")));
        resetButton = new Button(Locale.LS("Build TCC"));
        quickResetButton = new Button(Locale.LS("Reset TCC"));
        testButton = new Button(Locale.LS("Test TCC"));
        runStopButton = new Button(Locale.LSHTML("<Strong>RUN</Strong>&nbsp;/&nbsp;Stop"));
        buttonPanel.add(resetButton);
        buttonPanel.add(quickResetButton);
        //buttonPanel.add(testButton);
        buttonPanel.add(runStopButton);
        resetButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                if (simDimensionality == 1) {
                    if (startDialog == null)
                        startDialog = new StartDialog(theSim);
                    startDialog.show();
                } else if (simDimensionality == 2) new StartDialog2D(theSim).show();
            }
        });
        quickResetButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                resetAction();
                setSimRunning(false);
            }
        });
        testButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
//                setupForTest();
            }
        });


        runStopButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                setSimRunning(!simIsRunning());
                //TwoDimTCCmanager.printTemps(twoDimTCE.cvs);
            }
        });


        if (LoadFile.isSupported()) verticalPanel.add(loadFileInput = new LoadFile(this));

        Label l;
        simulationConsolePanel.add(l = new Label(Locale.LS("Simulation Speed")));
        l.addStyleName("topSpace");

        simulationConsolePanel.add(speedBar = new Scrollbar(Scrollbar.HORIZONTAL, 3, 1, 0, 260));
        speedBar.addStyleName("topSpace");
        simulationConsolePanel.add(l = new Label(Locale.LS("Scale")));
        l.addStyleName("topSpace");
        scale = new ListBox();
        for (LengthUnit lu : LengthUnit.values())
            scale.addItem(lu.toString());

        scale.addStyleName("topSpace");
        scale.setSelectedIndex(selectedLengthUnit.ordinal());
        simulationConsolePanel.add(scale);
        simulationConsolePanel.add(l = new Label(Locale.LS("Dimensionality")));
        l.addStyleName("topSpace");
        dimensionality = new ListBox();
        dimensionality.addItem("1D");
        dimensionality.addItem("2D");
        dimensionality.addStyleName("topSpace");
        simulationConsolePanel.add(dimensionality);
        dimensionality.setSelectedIndex(0);
        dimensionality.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                simDimensionality = Integer.parseInt(String.valueOf(dimensionality.getSelectedItemText().charAt(0)));
                updateDrawMenus();
                readSetupFile("blank.txt", "Blank Circuit");
                if (simDimensionality == 2) {
                    viewTempsOverlay = true;
                    showOverlayCheckItem.setState(true);
                }
            }
        });

        scale.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                switch (scale.getSelectedItemText()) {
                    case "micrometer":
                        selectedLengthUnit = CirSim.LengthUnit.MICROMETER;
                        break;
                    case "10 micrometers":
                        selectedLengthUnit = LengthUnit.MICROMETER_10;
                        break;
                    case "20 micrometers":
                        selectedLengthUnit = LengthUnit.MICROMETER_20;
                        break;
                    case "50 micrometers":
                        selectedLengthUnit = LengthUnit.MICROMETER_50;
                        break;
                    case "100 micrometers":
                        selectedLengthUnit = LengthUnit.MICROMETER_100;
                        break;
                    case "200 micrometers":
                        selectedLengthUnit = LengthUnit.MICROMETER_200;
                        break;
                    case "500 micrometers":
                        selectedLengthUnit = LengthUnit.MICROMETER_500;
                        break;
                    case "millimeter":
                        selectedLengthUnit = LengthUnit.MILLIMETER;
                        break;
                    case "5 millimeters":
                        selectedLengthUnit = LengthUnit.MILLIMETER_5;
                        break;
                    case "centimeter":
                        selectedLengthUnit = LengthUnit.CENTIMETER;
                        break;
                    case "5 centimeters":
                        selectedLengthUnit = LengthUnit.CENTIMETER_5;
                        break;
                    case "decimeter":
                        selectedLengthUnit = LengthUnit.DECIMETER;
                        break;
                    case "meter":
                        selectedLengthUnit = LengthUnit.METER;
                        break;

                }
                calculateElementsLengths();
            }
        });


        titleLabel = new Label("Label");

//        verticalPanel.add(iFrame = new Frame("iframe.html"));
        iFrame = new Frame("iframe.html");
        iFrame.setWidth(VERTICALPANELWIDTH + "px");
        iFrame.setHeight("100 px");
        iFrame.getElement().setAttribute("scrolling", "no");
        setGrid();
        elmList = new Vector<CircuitElm>();
        adjustables = new Vector<Adjustable>();
        // setupList = new Vector();
        undoStack = new Vector<UndoItem>();
        redoStack = new Vector<UndoItem>();
        trackedTemperatures = new ArrayList<ThermalControlElement>();

        awaitedResponses = new ArrayList<String>();
        random = new Random();
        // cv.setBackground(Color.black);
        // cv.setForeground(Color.lightGray);

        elmMenuBar = new MenuBar(true);
        elmMenuBar.setAutoOpen(true);


        elmMenuBar.addItem(elmEditMenuItem = new MenuItem(Locale.LS("Edit..."), new MyCommand("elm", "edit")));
        elmMenuBar.addItem(elmTempsMenuItem = new MenuItem(Locale.LS("Toggle in Temperature Display"), new MyCommand("elm", "viewTemps")));
        elmMenuBar.addItem(elmCutMenuItem = new MenuItem(Locale.LS("Cut"), new MyCommand("elm", "cut")));
        elmMenuBar.addItem(elmCopyMenuItem = new MenuItem(Locale.LS("Copy"), new MyCommand("elm", "copy")));
        elmMenuBar.addItem(elmDeleteMenuItem = new MenuItem(Locale.LS("Delete"), new MyCommand("elm", "delete")));
        elmMenuBar.addItem(new MenuItem(Locale.LS("Duplicate"), new MyCommand("elm", "duplicate")));
        elmMenuBar.addItem(elmFlipMenuItem = new MenuItem(Locale.LS("Swap Terminals"), new MyCommand("elm", "flip")));
        elmMenuBar.addItem(elmSplitMenuItem = menuItemWithShortcut("", "Split Wire", Locale.LS(ctrlMetaKey + "click"), new MyCommand("elm", "split")));
        elmMenuBar.addItem(elmSliderMenuItem = new MenuItem(Locale.LS("Sliders..."), new MyCommand("elm", "sliders")));


        setColors(positiveColor, negativeColor, neutralColor, selectColor, currentColor);

        simulation1D = new Simulation1D();
        simulation2D = new Simulation2D();

        fillCyclicPanel();


        if (startCircuitText != null) {
            getSetupList(false);
            readCircuit(startCircuitText);
            unsavedChanges = false;
        } else {
            if (stopMessage == null && startCircuitLink != null) {
                readCircuit("");
                getSetupList(false);
                ImportFromDropboxDialog.setSim(this);
                ImportFromDropboxDialog.doImportDropboxLink(startCircuitLink, false);
            } else {
                readCircuit("");
                if (stopMessage == null && startCircuit != null) {
                    getSetupList(false);
                    readSetupFile(startCircuit, startLabel);
                } else getSetupList(true);
            }
        }

        if (mouseModeReq != null) menuPerformed("main", mouseModeReq);

        enableUndoRedo();
        enablePaste();
        setiFrameHeight();
        canvas.addMouseDownHandler(this);
        canvas.addMouseMoveHandler(this);
        canvas.addMouseOutHandler(this);
        canvas.addMouseUpHandler(this);
        canvas.addClickHandler(this);
        canvas.addDoubleClickHandler(this);
        doTouchHandlers(this, canvas.getCanvasElement());
        canvas.addDomHandler(this, ContextMenuEvent.getType());


        menuBar.addDomHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                doMainMenuChecks();
            }
        }, ClickEvent.getType());
        Event.addNativePreviewHandler(this);
        canvas.addMouseWheelHandler(this);

        Window.addWindowClosingHandler(new Window.ClosingHandler() {
            public void onWindowClosing(ClosingEvent event) {
                // there is a bug in electron that makes it impossible to close the app if this
                // warning is given
                if (unsavedChanges && !isElectron())
                    event.setMessage(Locale.LS("Are you sure?  There are unsaved changes."));
            }
        });


        setupJSInterface();
//        initHeatSimulation();


        materialNames = new Vector<String>();
        materialNames.add("000000-Constant properties");
        materialHashMap = new HashMap<String, Material>();
        materialHashMap.put("000000-Constant properties", new Material("000000-Constant properties", theSim));
        colorChoices = new Vector<String>();
        setSimRunning(running);
        colorChoices.add("white");  // I will fix this later.
        colorChoices.add("lightGray");
        colorChoices.add("gray");
        colorChoices.add("darkGray");
        colorChoices.add("red");
        colorChoices.add("pink");
        colorChoices.add("orange");
        colorChoices.add("yellow");
        colorChoices.add("green");
        colorChoices.add("magenta");
        colorChoices.add("blue");


//        String CORSproxy = "https://corsproxy.io/?";
//        String baseURL = CORSproxy + "http://materials.tccbuilder.org/";
        baseURL = GWT.getModuleBaseURL() + "TCCMaterialLibrary/";
        //TODO: Add a callback to setSimRunning()
        readMaterialFlags(baseURL + "materials_flags.csv");


    }

    public void fillCyclicPanel() {
        cyclicPanel.clear();
        Label l = new Label(Locale.LS("Cycle Parts: "));
        l.addStyleName("topSpace");
        cyclicPanel.add(l);

        for (CyclePart cp : simulation1D.cycleParts) {
            cyclicPanel.add(cp.toWidget(true));
        }
    }

    void setCyclic(boolean c) {
        simulation1D.cyclic = c;
        drawLayoutPanel(false, false);
        centreCircuit();
        setCanvasSize();
        repaint();
    }

    public void drawLayoutPanel(boolean hideSidebar, boolean hideMenu) {
        RootLayoutPanel.get().clear();
        layoutPanel = new DockLayoutPanel(Unit.PX);
        if (!hideMenu) layoutPanel.addNorth(menuBar, MENUBARHEIGHT);

        if (verticalPanel.isAttached()) {
            layoutPanel.remove(verticalPanel);
        }

        if (hideSidebar) {
            VERTICALPANELWIDTH = 0;
        } else {
            layoutPanel.addEast(verticalPanel, (simulation1D != null && simulation1D.cyclic) ? VERTICALPANELWIDTH * 2 : VERTICALPANELWIDTH);
        }

        cyclicPanel.setVisible(simulation1D != null && simulation1D.cyclic);
        layoutPanel.add(canvas);

        RootLayoutPanel.get().add(layoutPanel);

    }


    private void updateDrawMenus() {
        drawMenuBar.clearItems();
        composeMainMenu(drawMenuBar);
        mainMenuBar.clearItems();
        composeMainMenu(mainMenuBar);
        loadShortcuts();
    }

    void calculateElementsLengths() {
        HashSet<LengthUnit> set = new HashSet<LengthUnit>();
        if (simDimensionality == 1) for (ThermalControlElement c : simulation1D.simTCEs) {
            c.calculateLength();
            if (c.DEFINED_LENGTH_UNIT != null) set.add(c.DEFINED_LENGTH_UNIT);
        }
        else for (TwoDimComponent c : simulation2D.simTwoDimComponents)
            c.calculateLengthHeight();

        if (set.size() > 1) Window.alert("Differing length units");
        setSimRunning(false);
    }

    // ***********************************Katni**********************************************
//    void initHeatSimulation() {
//
//        // simComponents = new Vector<Component>();
//        this.simTCEs = new Vector<ThermalControlElement>();
//        // this.num_cvs = 0;// this.circuit.num_cvs;
//        // this.special_boundaries = []
//        this.left_boundary = 41;
//        this.right_boundary = 42;
//        this.h_left = 100000.0;
//        this.h_right = 100000.0;
//        this.temp_left = 291.0;
//        this.temp_right = 290.0;
//        this.qIn = 0.0;
//        this.qOut = 0.0;
//        this.startTemp = 290.0;
//        this.ambient_temperature = 293.0;
//        // this.start_temperatures = new double[this.num_cvs];
//        this.times = new ArrayList<Double>();
//        this.temperatures = new ArrayList<Double[]>();
//        this.multipl = 1000;
//        this.tt = 0;
//        this.cpart_t = 0.0;
//        this.dt = maxTimeStep * 1e3;
//        this.total_time = timeStep;
//        this.reach_steady = false;
//        this.cyclic = false;
//        this.time = 0.0;
//        this.cycle = 0;
//        this.printing_interval = 1;
//        this.cycleParts = new Vector<CyclePart>();
//        this.cyclePartTime = 0.0;
//        this.cyclePartIndex = 0;
//
//        // 2D
//        simTwoDimComponents = new Vector<TwoDimComponent>();
//        HeatSimProps.BC bc = HeatSimProps.BC.CONVECTIVE;  // No way I am passing this to a constructor 4x.
//        twoDimBC = new TwoDimBC(new HeatSimProps.BC[]{bc, bc, bc, bc});
//
//        this.ud = 0;
//        x_prev = new Vector<Double>();
//        x_mod = new Vector<Double>();
//
//    }

    private void readMaterialFlags(String url) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    //Window.alert(Locale.LS("Can't load data!"));
                    GWT.log("File Error Response", exception);
                }

                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        materialFlagText = response.getText();
                        String[] lines = materialFlagText.split("\n");
                        for (String s : Arrays.copyOfRange(lines, 1, lines.length)) {
                            GWT.log(s);
                            String name = s.split(",")[0];
                            materialHashMap.put(name, new Material(name, theSim));
                            GWT.log(name);
                            GWT.log(String.valueOf(s.split(",")[11]));
                            materialNames.add(name);
                        }


                    } else {
                        //Window.alert(Locale.LS("Can't load data!"));
                        GWT.log("Bad file server response:" + response.getStatusText());
                    }
                }
            });
        } catch (RequestException e) {
            GWT.log("failed file reading", e);
        }
    }


    void setColors(String positiveColor, String negativeColor, String neutralColor, String selectColor, String currentColor) {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor != null) {
            if (positiveColor == null) positiveColor = stor.getItem("positiveColor");
            if (negativeColor == null) negativeColor = stor.getItem("negativeColor");
            if (neutralColor == null) neutralColor = stor.getItem("neutralColor");
            if (selectColor == null) selectColor = stor.getItem("selectColor");
            if (currentColor == null) currentColor = stor.getItem("currentColor");
        }


        CircuitElm.selectColor = Color.cyan;


    }

    MenuItem menuItemWithShortcut(String icon, String text, String shortcut, MyCommand cmd) {
        final String html = "<div style=\"display : flex; align-items:center; justify-content:space-between; \">" + "<div style=\"text-align:left;\">" + "<i class=\"cirjsicon-" + icon + "\"></i>" + Locale.LS(text) + "</div>" + "<div style=\"text-align:right;\">" + shortcut + "</div>" + "</div>";

        return new MenuItem(SafeHtmlUtils.fromTrustedString(html), cmd);
    }

    MenuItem iconMenuItem(String icon, String text, Command cmd) {
        String icoStr = "<i class=\"cirjsicon-" + icon + "\"></i>&nbsp;" + Locale.LS(text); // <i
        // class="cirjsicon-"></i>&nbsp;
        return new MenuItem(SafeHtmlUtils.fromTrustedString(icoStr), cmd);
    }

    boolean getOptionFromStorage(String key, boolean val) {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null) return val;
        String s = stor.getItem(key);
        if (s == null) return val;
        return s == "true";
    }

    void setOptionInStorage(String key, boolean val) {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null) return;
        stor.setItem(key, val ? "true" : "false");
    }

    // save shortcuts to local storage
    void saveShortcuts() {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null) return;
        String str = "1";
        int i;
        // format: version;code1=ClassName;code2=ClassName;etc
        for (i = 0; i != shortcuts.length; i++) {
            String sh = shortcuts[i];
            if (sh == null) continue;
            str += ";" + i + "=" + sh;
        }
        stor.setItem("shortcuts", str);
    }

    // load shortcuts from local storage
    void loadShortcuts() {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null) return;
        String str = stor.getItem("shortcuts");
        if (str == null) return;
        String keys[] = str.split(";");

        // clear existing shortcuts
        int i;
        for (i = 0; i != shortcuts.length; i++)
            shortcuts[i] = null;

        // clear shortcuts from menu
        for (i = 0; i != mainMenuItems.size(); i++) {
            CheckboxMenuItem item = mainMenuItems.get(i);
            // stop when we get to drag menu items
            if (item.getShortcut().length() > 1) break;
            item.setShortcut("");
        }

        // go through keys (skipping version at start)
        for (i = 1; i < keys.length; i++) {
            String arr[] = keys[i].split("=");
            if (arr.length != 2) continue;
            int c = Integer.parseInt(arr[0]);
            String className = arr[1];
            shortcuts[c] = className;

            // find menu item and fix it
            int j;
            for (j = 0; j != mainMenuItems.size(); j++) {
                if (mainMenuItemNames.get(j) == className) {
                    CheckboxMenuItem item = mainMenuItems.get(j);
                    item.setShortcut(Character.toString((char) c));
                    break;
                }
            }
        }
    }

    // install touch handlers
    // don't feel like rewriting this in java. Anyway, java doesn't let us create
    // mouse
    // events and dispatch them.
    native static void doTouchHandlers(CirSim sim, CanvasElement cv) /*-{
        // Set up touch events for mobile, etc
        var lastTap;
        var tmout;
        var lastScale;

        cv.addEventListener("touchstart", function (e) {
            mousePos = getTouchPos(cv, e);
            var touch = e.touches[0];
            var etype = "mousedown";
            lastScale = 1;
            clearTimeout(tmout);
            e.preventDefault();

            if (e.timeStamp - lastTap < 300) {
                etype = "dblclick";
            } else {
                tmout = setTimeout(function () {
                    sim.@lahde.tccbuilder.client.CirSim::longPress()();
                }, 500);
            }
            lastTap = e.timeStamp;

            var touch1 = e.touches[0];
            var touch2 = e.touches[e.touches.length - 1];
            var mouseEvent = new MouseEvent(etype, {
                clientX: .5 * (touch1.clientX + touch2.clientX),
                clientY: .5 * (touch1.clientY + touch2.clientY)
            });
            cv.dispatchEvent(mouseEvent);
            if (e.touches.length > 1)
                sim.@lahde.tccbuilder.client.CirSim::twoFingerTouch(II)(mouseEvent.clientX, mouseEvent.clientY - cv.getBoundingClientRect().y);
        }, false);
        cv.addEventListener("touchend", function (e) {
            var mouseEvent = new MouseEvent("mouseup", {});
            e.preventDefault();
            clearTimeout(tmout);
            cv.dispatchEvent(mouseEvent);
        }, false);
        cv.addEventListener("touchmove", function (e) {
            e.preventDefault();
            clearTimeout(tmout);
            if (e.touches.length > 1) {
                sim.@lahde.tccbuilder.client.CirSim::zoomCircuit(D)(40 * (Math.log(e.scale) - Math.log(lastScale)));
                lastScale = e.scale;
            }
            var touch1 = e.touches[0];
            var touch2 = e.touches[e.touches.length - 1];
            var mouseEvent = new MouseEvent("mousemove", {
                clientX: .5 * (touch1.clientX + touch2.clientX),
                clientY: .5 * (touch1.clientY + touch2.clientY)
            });
            cv.dispatchEvent(mouseEvent);
        }, false);

        // Get the position of a touch relative to the canvas
        function getTouchPos(canvasDom, touchEvent) {
            var rect = canvasDom.getBoundingClientRect();
            return {
                x: touchEvent.touches[0].clientX - rect.left,
                y: touchEvent.touches[0].clientY - rect.top
            };
        }

    }-*/;

    // this is called twice, once for the Draw menu, once for the right mouse popup
    // menu
    public void composeMainMenu(MenuBar mainMenuBar) {
        MenuItem menuItem;
        if (simDimensionality == 2) {
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add 2DComponent"), "2DComponent"));
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add ZigZagInterface"), "ZigZagInterface"));
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add CircularInterface"), "CircularInterface"));
        } else {
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Component"), "Component"));
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add TE Component"), "TEComponent"));
            mainMenuBar.addSeparator();
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Conduit"), "WireElm"));
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Resistor"), "ResistorElm"));
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Switch"), "SwitchElm"));
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Regulator"), "RegulatorElm"));
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Diode"), "DiodeElm"));
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Capacitor"), "CapacitorElm"));
            // mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Ground"), "GroundElm"));
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add Heat Source/Sink"), "HeatSourceSinkElm"));
            mainMenuBar.addSeparator();
            mainMenuBar.addItem(getClassCheckItem(Locale.LS("Add TE Heat Engine"), "TEHeatEngine"));
            mainMenuBar.addSeparator();

            MenuBar sampleElements = new MenuBar(true);

            sampleElements.addItem(menuItem = getClassCheckItem(Locale.LS("Add Switch-FM_01"), "SwitchElm_FM_01"));
            menuItem.setTitle("Operating range = 254-353 K");
            sampleElements.addItem(menuItem = getClassCheckItem(Locale.LS("Add Switch-MM_01"), "SwitchElm_MM_01"));
            menuItem.setTitle("Operating range = 254-353 K");
            sampleElements.addItem(menuItem = getClassCheckItem(Locale.LS("Add Switch-MM_02"), "SwitchElm_MM_02"));
            menuItem.setTitle("Operating range = 254-353 K");
            sampleElements.addItem(menuItem = getClassCheckItem(Locale.LS("Add Switch-MM_03"), "SwitchElm_MM_03"));
            menuItem.setTitle("Operating range = 273-373 K");
            sampleElements.addItem(menuItem = getClassCheckItem(Locale.LS("Add Switch-ME_01"), "SwitchElm_ME_01"));
            menuItem.setTitle("Operating range unknown");
            sampleElements.addItem(menuItem = getClassCheckItem(Locale.LS("Add Switch-SSE_01"), "SwitchElm_SSE_01"));
            menuItem.setTitle("Operating range = 300-500 K");
            sampleElements.addItem(menuItem = getClassCheckItem(Locale.LS("Add Switch-SSM_01"), "SwitchElm_SSM_01"));
            menuItem.setTitle("Operating range = 2-4 K");
            sampleElements.addItem(menuItem = getClassCheckItem(Locale.LS("Add Diode_SS_01"), "DiodeElm_SS_01"));
            menuItem.setTitle("Ideal operating range = 40-99 K");
            sampleElements.addItem(menuItem = getClassCheckItem(Locale.LS("Add Diode_SS_02"), "DiodeElm_SS_02"));
            menuItem.setTitle("Ideal operating range = 290-450 K");
            sampleElements.addItem(menuItem = getClassCheckItem(Locale.LS("Add Diode_F_01"), "DiodeElm_F_01"));
            menuItem.setTitle("Ideal operating range = 303-318 K");
            sampleElements.addItem(menuItem = getClassCheckItem(Locale.LS("Add Diode_F_02"), "DiodeElm_F_02"));
            menuItem.setTitle("Ideal operating range = 283-333 K");
            sampleElements.addItem(menuItem = getClassCheckItem(Locale.LS("Add Diode_T_01"), "DiodeElm_T_01"));
            sampleElements.addItem(menuItem = getClassCheckItem(Locale.LS("Add Regulator-F_01"), "RegulatorElm_F_01"));
            menuItem.setTitle("Ideal operating range = 300-350 K");
            mainMenuBar.addItem(SafeHtmlUtils.fromTrustedString(CheckboxMenuItem.checkBoxHtml + Locale.LS("&nbsp;</div>Samples")), sampleElements);
        }


        MenuBar otherMenuBar = new MenuBar(true);
        CheckboxMenuItem mi;
        otherMenuBar.addItem(mi = getClassCheckItem(Locale.LS("Drag All"), "DragAll"));
        mi.setShortcut(Locale.LS("(Alt-drag)"));
        otherMenuBar.addItem(mi = getClassCheckItem(Locale.LS("Drag Row"), "DragRow"));
        mi.setShortcut(Locale.LS("(A-S-drag)"));
        otherMenuBar.addItem(mi = getClassCheckItem(Locale.LS("Drag Column"), "DragColumn"));
        mi.setShortcut(isMac ? Locale.LS("(A-Cmd-drag)") : Locale.LS("(A-M-drag)"));
        otherMenuBar.addItem(getClassCheckItem(Locale.LS("Drag Selected"), "DragSelected"));
        otherMenuBar.addItem(mi = getClassCheckItem(Locale.LS("Drag Post"), "DragPost"));
        mi.setShortcut("(" + ctrlMetaKey + "drag)");

        mainMenuBar.addItem(SafeHtmlUtils.fromTrustedString(CheckboxMenuItem.checkBoxHtml + Locale.LS("&nbsp;</div>Drag")), otherMenuBar);

        mainMenuBar.addItem(mi = getClassCheckItem(Locale.LS("Select/Drag Sel."), "Select"));
    }


    public void setiFrameHeight() {
        if (iFrame == null) return;
        int i;
        int cumheight = 0;
        for (i = 0; i < verticalPanel.getWidgetIndex(iFrame); i++) {
            if (verticalPanel.getWidget(i) != loadFileInput) {
                cumheight = cumheight + verticalPanel.getWidget(i).getOffsetHeight();
                if (verticalPanel.getWidget(i).getStyleName().contains("topSpace")) cumheight += 12;
            }
        }
        int ih = RootLayoutPanel.get().getOffsetHeight() - MENUBARHEIGHT - cumheight;
        if (ih < 0) ih = 0;
        iFrame.setHeight(ih + "px");
    }

    CheckboxMenuItem getClassCheckItem(String s, String t) {
        // try {
        // Class c = Class.forName(t);
        String shortcut = "";
        CircuitElm elm = null;
        try {
            elm = constructElement(t, 0, 0);
        } catch (Exception e) {

            //FIXME: elements fail constructing, likely because materials aren't loaded
        }
        CheckboxMenuItem mi;
        if (shortcut == "") mi = new CheckboxMenuItem(s);
        else mi = new CheckboxMenuItem(s, shortcut);

        mi.setScheduledCommand(new MyCommand("main", t));
        // register(c, elm);
        if (elm != null) {

            if (elm.needsShortcut()) {
                shortcut += (char) elm.getShortcut();
                if (shortcuts[elm.getShortcut()] != null && !shortcuts[elm.getShortcut()].equals(t))
                    console("already have shortcut for " + (char) elm.getShortcut() + " " + elm);
                shortcuts[elm.getShortcut()] = t;
            }
            if (elm instanceof ThermalControlElement)
                mi.setTitle(mi.getTitle() + " " + ((ThermalControlElement) elm).hasOperatingRange);

            elm.delete();
        }

        mainMenuItems.add(mi);
        mainMenuItemNames.add(t);


        return mi;
    }

    void centreCircuit() {
        if (elmList == null) // avoid exception if called during initialization
            return;

        Rectangle bounds = getCircuitBounds();
        setCircuitArea();

        double scale = 1;
        int cheight = circuitArea.height;


        if (bounds != null)
            // add some space on edges because bounds calculation is not perfect
            scale = Math.min(circuitArea.width / (double) (bounds.width + 140), cheight / (double) (bounds.height + 100));
        scale = Math.min(scale, 1.5); // Limit scale so we don't create enormous circuits in big windows

        // calculate transform so circuit fills most of screen
        transform[0] = transform[3] = scale;
        transform[1] = transform[2] = transform[4] = transform[5] = 0;
        if (bounds != null) {
            transform[4] = (circuitArea.width - bounds.width * scale) / 2 - bounds.x * scale;
            transform[5] = (cheight - bounds.height * scale) / 2 - bounds.y * scale;
        }
    }

    // get circuit bounds. remember this doesn't use setBbox(). That is calculated
    // when we draw
    // the circuit, but this needs to be ready before we first draw it, so we use
    // this crude method
    Rectangle getCircuitBounds() {
        int i;
        int minx = 30000, maxx = -30000, miny = 30000, maxy = -30000;
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            // centered text causes problems when trying to center the circuit,
            // so we special-case it here
            if (!ce.isCenteredText()) {
                minx = min(ce.x, min(ce.x2, minx));
                maxx = max(ce.x, max(ce.x2, maxx));
            }
            miny = min(ce.y, min(ce.y2, miny));
            maxy = max(ce.y, max(ce.y2, maxy));
        }
        if (minx > maxx) return null;
        return new Rectangle(minx, miny, maxx - minx, maxy - miny);
    }

    long lastTime = 0, lastFrameTime, lastIterTime, secTime = 0;
    int frames = 0;
    int steps = 0;
    int framerate = 0, steprate = 0;
    static CirSim theSim;

    public void setSimRunning(boolean s) {

        if (s) {
            responseTimer.scheduleRepeating(FASTTIMER);
        } else {
            simRunning = false;
            runStopButton.setHTML(Locale.LSHTML("Run&nbsp;/&nbsp;<strong>STOP</strong>"));
            runStopButton.addStyleName("topButton-red");
            timer.cancel();
            responseTimer.cancel();
            repaint();
        }
    }

    public boolean simIsRunning() {
        return simRunning;
    }

    boolean needsRepaint;

    void repaint() {
        if (!needsRepaint) {
            needsRepaint = true;
            Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
                public boolean execute() {
                    updateCircuit();
                    needsRepaint = false;
                    return false;
                }
            }, FASTTIMER);
        }
    }

    // *****************************************************************
    // UPDATE CIRCUIT

    public void updateCircuit() {
        PerfMonitor perfmon = new PerfMonitor();
        perfmon.startContext("updateCircuit()");

        checkCanvasSize();

        // Analyze circuit

        if (stopElm != null && stopElm != mouseElm) stopElm.setMouseElm(true);


        g = new Graphics(cvcontext);


        g.setColor(Color.black);
        canvas.getElement().getStyle().setBackgroundColor(Color.gray.getHexValue());
//        if (mouseElm != null)
//            GWT.log("mouseElm" + mouseElm.toString());
//        if (dragElm != null)
//            GWT.log("dragElm" + dragElm.toString());
//        if (mouseMode > -1)
//            GWT.log("mouse mode: "+mouseMode);
//        noEditCheckItem.setState(simRunning);


        if (simRunning) {
            // Run circuit
            perfmon.startContext("runCircuit()");
            try {
                if (awaitedResponses.isEmpty()) runCircuit();
            } catch (Exception e) {
                console("exception in runCircuit " + e.toString());
                //debugger();
                e.printStackTrace();
            }
            perfmon.stopContext();
        }

        long sysTime = System.currentTimeMillis();
        if (simRunning) {

            lastTime = sysTime;
        } else {
            lastTime = 0;
        }

        if (sysTime - secTime >= 1000) {
            framerate = frames;
            steprate = steps;
            frames = 0;
            steps = 0;
            secTime = sysTime;
        }


        perfmon.startContext("graphics");


        if (noEditCheckItem.getState()) g.drawLock(20, 30);
        // Clear the frame
        g.fillRect(0, 0, canvasWidth, canvasHeight);

        g.setColor(Color.white);


        // Set the graphics transform to deal with zoom and offset
        double scale = devicePixelRatio();
        cvcontext.setTransform(transform[0] * scale, 0, 0, transform[3] * scale, transform[4] * scale, transform[5] * scale);

        // Draw each element
        perfmon.startContext("elm.draw()");
        for (CircuitElm elm : elmList) {
            if (elm instanceof ThermalControlElement) {
                if (viewTempsOverlay) ((ThermalControlElement) elm).drawCVTemperatures(g, elm.point1, elm.point2);
                else ((ThermalControlElement) elm).draw(g);
            } else elm.draw(g);
        }

        perfmon.stopContext();
        // Draw posts normally

        for (CircuitElm elm : elmList) {
            if (!elm.needsHighlight()) elm.drawPosts(g);
        }
        for (CircuitElm elm : elmList) {
            if (elm.needsHighlight()) elm.drawPosts(g);
        }

        // for some mouse modes, what matters is not the posts but the endpoints (which
        // are only the same for 2-terminal elements). We draw those now if needed
        if (tempMouseMode == MODE_DRAG_ROW || tempMouseMode == MODE_DRAG_COLUMN || tempMouseMode == MODE_DRAG_POST || tempMouseMode == MODE_DRAG_SELECTED) {
            for (int i = 0; i != elmList.size(); i++) {

                CircuitElm ce = getElm(i);
                // ce.drawPost(g, ce.x , ce.y );
                // ce.drawPost(g, ce.x2, ce.y2);
                if (!(ce != mouseElm || tempMouseMode != MODE_DRAG_POST)) {
                    ce.drawHandles(g, CircuitElm.selectColor);
                }
            }
        }

        // draw handles for elm we're creating
        if (tempMouseMode == MODE_SELECT && mouseElm != null) {
            mouseElm.drawHandles(g, CircuitElm.selectColor);
        }

        // draw handles for elm we're dragging
        if (dragElm != null && (dragElm.x != dragElm.x2 || dragElm.y != dragElm.y2)) {
            dragElm.draw(g);
            if (dragElm instanceof TwoDimComponent) ((TwoDimComponent) dragElm).calculateLengthHeight();
            if (dragElm instanceof ThermalControlElement) ((ThermalControlElement) dragElm).calculateLength();

            dragElm.drawHandles(g, CircuitElm.selectColor);
        }


        // draw the selection rect
        if (selectedArea != null) {
            g.setColor(CircuitElm.selectColor);
            g.drawRect(selectedArea.x, selectedArea.y, selectedArea.width, selectedArea.height);
        }

        // draw the crosshair cursor
        if (crossHairCheckItem.getState() && mouseCursorX >= 0 && mouseCursorX <= circuitArea.width && mouseCursorY <= circuitArea.height) {
            g.setColor(Color.gray);
            int x = snapGrid(inverseTransformX(mouseCursorX));
            int y = snapGrid(inverseTransformY(mouseCursorY));
            g.drawLine(x, inverseTransformY(0), x, inverseTransformY(circuitArea.height));
            g.drawLine(inverseTransformX(0), y, inverseTransformX(circuitArea.width), y);
        }

        // reset the graphics scale and translation
        cvcontext.setTransform(scale, 0, 0, scale, 0, 0);


        // draw the display
        if (viewTempsOverlay)
            drawTemperatureScale(g.context, 20, circuitArea.height - 50, canvasWidth - 40, 30, simDimensionality == 1 ? simulation1D.minTemp : simulation2D.minTemp, simDimensionality == 1 ? simulation1D.maxTemp : simulation2D.maxTemp, 10);
        else if (viewTempsInGraph) drawTemperatureGraphs(g);
        else drawTemperatureDisplays(g);

        perfmon.startContext("drawBottomArea()");
        drawBottomArea(g);
        perfmon.stopContext();
        g.setColor(Color.white);

        perfmon.stopContext(); // graphics

        if (stopElm != null && stopElm != mouseElm) stopElm.setMouseElm(false);

        frames++;

        // if we did DC analysis, we need to re-analyze the circuit with that flag
        // cleared.
        if (dcAnalysisFlag) {
            dcAnalysisFlag = false;
        }

        lastFrameTime = lastTime;

        perfmon.stopContext(); // updateCircuit

        if (developerMode) {
            int height = 15;
            int increment = 15;
            g.drawString("Framerate: " + CircuitElm.showFormat.format(framerate), 10, height);
            g.drawString("Steprate: " + CircuitElm.showFormat.format(steprate), 10, height += increment);
            g.drawString("Steprate/iter: " + CircuitElm.showFormat.format(steprate / getIterCount()), 10, height += increment);
            g.drawString("iterc: " + CircuitElm.showFormat.format(getIterCount()), 10, height += increment);
            g.drawString("Frames: " + frames, 10, height += increment);

            height += (increment * 2);

            String perfmonResult = PerfMonitor.buildString(perfmon).toString();
            String[] splits = perfmonResult.split("\n");
            for (int x = 0; x < splits.length; x++) {
                g.drawString(splits[x], 10, height + (increment * x));
            }
        }

        // This should always be the last
        // thing called by updateCircuit();
        callUpdateHook();
    }

    public void drawTemperatureScale(Context2d ctx, double x, double y, double width, double height, double minTemp, double maxTemp, int labelInterval) {
        int intervalNumber = 250;
        double temperatureRange = maxTemp - minTemp;
        double temperatureInterval = temperatureRange / (intervalNumber - 1);
        double[] temperatures = new double[intervalNumber];
        temperatures[0] = minTemp;

        for (int i = 1; i < temperatures.length; i++) {
            temperatures[i] = temperatures[i - 1] + temperatureInterval;
        }


        double cvX = x;
        for (int i = 0; i < temperatures.length; i++) {
            double temperatureRatio = (temperatures[i] - minTemp) / temperatureRange;
            String mixedColor = CirSim.getMixedColor(temperatureRatio);
            ctx.setFillStyle(mixedColor);
            ctx.setStrokeStyle(mixedColor);
            double temperatureWidth = (width / intervalNumber);
            cvX = x + i * temperatureWidth;
            ctx.strokeRect(cvX, y, temperatureWidth, height);
            ctx.fillRect(cvX, y, temperatureWidth, height);
        }
        cvX = x;


        double textWidth;
        ctx.setFillStyle(Color.white.getHexValue());
        ctx.setStrokeStyle(Color.white.getHexValue());
        ctx.setLineWidth(1.5);
        ctx.setFont("10px Roboto");
        for (int i = 0; i < temperatures.length; i++) {
            if (i != temperatures.length - 1 && i != 0 && i % (temperatures.length / labelInterval) != 0) continue;
            String text = NumberFormat.getFormat("0.00").format(temperatures[i]);

            textWidth = ctx.measureText(NumberFormat.getFormat("0.00").format(temperatures[i])).getWidth();

            ctx.fillText(text, cvX - (textWidth / 2), y - 16);
            ctx.beginPath();
            ctx.moveTo(cvX, y + 4);
            ctx.lineTo(cvX, y - 8);
            ctx.stroke();

            cvX += (width / labelInterval);
        }


        ctx.setStrokeStyle(Color.white.getHexValue());
        ctx.rect(x, y, width, height);
        ctx.stroke();
    }


    public static String getMixedColor(double temperatureRatio) {
        Color color1 = Color.blue;
        Color color2 = Color.white;
        Color color3 = Color.red;

        int red = (int) (color1.getRed() * (1 - temperatureRatio) + color2.getRed() * temperatureRatio);
        int green = (int) (color1.getGreen() * (1 - temperatureRatio) + color2.getGreen() * temperatureRatio);
        int blue = (int) (color1.getBlue() * (1 - temperatureRatio) + color2.getBlue() * temperatureRatio);

        red = (int) (red * (1 - temperatureRatio) + color3.getRed() * temperatureRatio);
        green = (int) (green * (1 - temperatureRatio) + color3.getGreen() * temperatureRatio);
        blue = (int) (blue * (1 - temperatureRatio) + color3.getBlue() * temperatureRatio);

        return "#" + Integer.toHexString(red) + Integer.toHexString(green) + Integer.toHexString(blue);
    }

    public void reorderByIndex() {
        if (simDimensionality == 2) return;
        Collections.sort(simulation1D.simTCEs);
        Collections.sort(trackedTemperatures);
        redrawElements(simulation1D.simTCEs);
    }

    public void reorderByPosition() {
        if (simDimensionality == 2) return;
        Comparator<ThermalControlElement> comparator = new Comparator<ThermalControlElement>() {
            @Override
            public int compare(ThermalControlElement tce1, ThermalControlElement tce2) {
                int x1 = (tce1.x + tce1.x2) / 2;
                int x2 = (tce2.x + tce2.x2) / 2;
                int y1 = (tce1.y + tce1.y2) / 2;
                int y2 = (tce2.y + tce2.y2) / 2;

                return x1 == x2 ? y1 - y2 : x1 - x2;
            }
        };
        simulation1D.simTCEs.sort(comparator);
        trackedTemperatures.sort(comparator);
        int i = 0;
        for (ThermalControlElement tce : simulation1D.simTCEs)
            tce.index = i++;

        redrawElements(simulation1D.simTCEs);
    }


    void redrawElements(Vector<ThermalControlElement> simTCEs) {
        int x = Integer.MAX_VALUE;
        int y = Integer.MAX_VALUE;
        ArrayList<Integer> lengths = new ArrayList<>();
        for (ThermalControlElement tce : simTCEs) {
            if (tce.x == tce.x2)
                lengths.add(Math.abs(tce.y - tce.y2));
            if (tce.y == tce.y2)
                lengths.add(Math.abs(tce.x - tce.x2));

            x = Math.min(tce.x2, Math.min(x, tce.x));
            y = Math.min(tce.y2, Math.min(y, tce.y));

        }


        for (int i = 0; i < simTCEs.size(); i++) {
            ThermalControlElement tce = simTCEs.get(i);
            if (tce.y == tce.y2) {
                if (tce.x < tce.x2) {
                    tce.x = x;
                    x += lengths.get(i);
                    tce.x2 = x;
                } else {
                    tce.x2 = x;
                    x += lengths.get(i);
                    tce.x = x;
                }
                tce.y = tce.y2 = canvasHeight / 2;
                tce.setPoints();
            }
            if (tce.x == tce.x2) {
                if (tce.y < tce.y2) {
                    tce.y = y;
                    y += lengths.get(i);
                    tce.y2 = y;
                } else {
                    tce.y2 = y;
                    y += lengths.get(i);
                    tce.y = y;
                }
                tce.x = tce.x2 = canvasWidth / 2;
                tce.setPoints();
            }
        }
    }


    void drawTemperatureDisplays(Graphics g) {
        Context2d ctx = g.context;
        double maxElementHeight = 30.0;
        int h = (int) (trackedTemperatures.size() * maxElementHeight < 200 ? trackedTemperatures.size() * maxElementHeight : 200);
        g.setColor(Color.black.getHexValue());
        g.fillRect(0, circuitArea.height - h, circuitArea.width, canvasHeight - circuitArea.height + h);

        // drawing the index and calculating the sizes of the elements
        double indexWidth = (circuitArea.width * 1.00) * .05;
        double elementHeight = h / trackedTemperatures.size();
        double YOffset = circuitArea.height - h;
        double XOffSet = 0;
        // drawing the displays
        for (ThermalControlElement tce : trackedTemperatures) {
            XOffSet = 0;
            double[] temps = tce.listTemps();
            double elementWidth = (circuitArea.width * 1.00 - indexWidth) / (temps.length);
            drawDisplaySegment(g, XOffSet, YOffset, indexWidth, elementHeight, tce.index + "", tce.color.getHexValue());
            XOffSet += indexWidth;
            for (double temp : temps) {
                drawDisplaySegment(g, XOffSet, YOffset, elementWidth, elementHeight, NumberFormat.getFormat("0.00").format(temp), tce.color.getHexValue());
                XOffSet += elementWidth;
            }

            YOffset += elementHeight;

        }

    }

    void drawTemperatureGraphs(Graphics g) {
        class GraphPoint {
            double x;
            double y;
            Color color;

            int outOfRange;

            public GraphPoint(double x, double y, Color color, int outOfRange) {
                this.x = x;
                this.y = y;
                this.color = color;
                this.outOfRange = outOfRange;
            }
        }
        int h = 250;
        if (simulation1D.heatCircuit == null) return;

        double elementWidth = (double) (circuitArea.width) / trackedTemperatures.size();
        double elementHeight = h * .9;

        double YOffset = circuitArea.height - h;
        double XOffSet = 100;

        //drawing the graph's background
        Context2d ctx = g.context;
        ctx.setFillStyle(Color.white.getHexValue());
        //ctx.fillRect(XOffSet, YOffset, circuitArea.width, h);
        ctx.setStrokeStyle(Color.lightGray.getHexValue());
        ctx.setLineWidth(0.5);

        double tempDiff = Math.abs(simulation1D.maxTemp - simulation1D.minTemp);

        int numberOfLines = 10;
        for (int i = 0; i < numberOfLines + 1; i++) {
            ctx.beginPath();
            double y = YOffset + ((elementHeight / numberOfLines) * i);
            String text = NumberFormat.getFormat("#.00").format((simulation1D.maxTemp - (tempDiff / (numberOfLines)) * i));

            ctx.moveTo(XOffSet * .75, y);
            ctx.lineTo(circuitArea.width - XOffSet * .25, YOffset + ((elementHeight / numberOfLines) * i));
            ctx.stroke();

            double fontSize = Math.min(XOffSet / text.length(), (elementHeight / numberOfLines) / 2);
            ctx.setFont(fontSize + "px Roboto");

            TextMetrics metrics = ctx.measureText(text);
            double textWidth = metrics.getWidth();

            double textX = 0 + (XOffSet - textWidth) / 2;
            double textY = y + (fontSize) / 2;
            ctx.fillText(text, textX, textY);
        }

        double tempWidth = ((double) (circuitArea.width - 1.5 * XOffSet) / ((simulation1D.heatCircuit.cvs.size()) - 1));
        double innerX = XOffSet - tempWidth / 2;
        ArrayList<GraphPoint> points = new ArrayList<>();
        for (ThermalControlElement tce : trackedTemperatures) {
            for (double temp : tce.listTemps()) {
                double valueRange = simulation1D.maxTemp - simulation1D.minTemp;
                double relativeValue = (temp - simulation1D.minTemp) / valueRange;
                double pointY = (YOffset + elementHeight) - (elementHeight * relativeValue);
                double pointX = innerX + tempWidth / 2.0;
                int outOfRange = 0;
                if (pointY < YOffset) {
                    pointY = YOffset;
                    outOfRange = 1;
                }
                if (pointY > YOffset + elementHeight) {
                    pointY = YOffset + elementHeight;
                    outOfRange = -1;
                }

                points.add(new GraphPoint(pointX, pointY, tce.color, outOfRange));
                innerX += tempWidth;
            }


            XOffSet += elementWidth;

        }
        for (int i = 0; i < points.size() - 1; i++) {
            GraphPoint current = points.get(i);
            GraphPoint right = points.get(i + 1);
            ctx.setStrokeStyle(current.color.getHexValue());
            ctx.beginPath();
            ctx.setLineWidth(2.5);
            ctx.moveTo(current.x, current.y);
            if (!current.color.equals(right.color)) {
                double midX = (current.x + right.x) / 2;
                double midY = (current.y + right.y) / 2;
                ctx.lineTo(midX, midY);
                ctx.stroke();
                ctx.beginPath();
                ctx.moveTo(midX, midY);
                ctx.setStrokeStyle(right.color.getHexValue());

            }
            ctx.lineTo(right.x, right.y);
            ctx.stroke();
            ctx.closePath();
        }
        for (GraphPoint point : points) {
            ctx.setStrokeStyle(point.color.getHexValue());
            ctx.setFillStyle(point.color.getHexValue());
            //draw a triangle above point if out of range
            double radius = 5;
            if (point.outOfRange != 0) {
                ctx.beginPath();
                ctx.moveTo(point.x - radius * point.outOfRange, point.y - radius * point.outOfRange);
                ctx.lineTo(point.x, point.y - radius * 2 * point.outOfRange);
                ctx.lineTo(point.x + radius * point.outOfRange, point.y - radius * point.outOfRange);
                ctx.stroke();
                ctx.closePath();
            }
            ctx.beginPath();
            ctx.arc(point.x, point.y, radius, 0, 2 * Math.PI);
            ctx.fill();
            ctx.closePath();

        }

    }


    private void drawDisplaySegment(Graphics g, double x, double y, double width, double height, String text, String color) {
        Context2d ctx = g.context;
        ctx.setFillStyle(color);
        ctx.setStrokeStyle(color);
        ctx.setLineWidth(1);
        // Draw the rectangle
        ctx.strokeRect(x, y, width, height);

        // Set the font size according to the rectangle size
        double fontSize = Math.min(width / text.length(), height / 2);
        ctx.setFont("bold " + fontSize + "px Roboto");

        // Measure the text width
        TextMetrics metrics = ctx.measureText(text);
        double textWidth = metrics.getWidth();

        // Calculate the position to center the text
        double textX = x + (width - textWidth) / 2;
        double textY = y + (height + (fontSize * .85) / 2) / 2;

        // Draw the text
        ctx.fillText(text, textX, textY);
    }

    void drawBottomArea(Graphics g) {
        g.setColor(Color.white);

        // in JS it doesn't matter how big this is, there's no out-of-bounds exception
        String[] info = new String[10];
        if (dragElm != null) {
            dragElm.getInfo(info);
        } else if (mouseElm != null) {
            mouseElm.getInfo(info);
        } else {
            if (simDimensionality == 1) {
                info[0] = Locale.LS("t = ") + NumberFormat.getFormat("0.000000").format(simulation1D.time) + " s";
                info[1] = Locale.LS("time step = ") + simulation1D.dt + " s";
                info[2] = Locale.LS("TCEs = " + simulation1D.printTCEs());
            } else {
                info[0] = Locale.LS("t = ") + NumberFormat.getFormat("0.000000").format(simulation2D.time) + " s";
                info[1] = Locale.LS("time step = ") + simulation2D.dt + " s";
                info[2] = Locale.LS("components = " + simulation2D.printTCEs());


            }
        }
        for (int i = 0; info[i] != null; i++)
            g.drawString(info[i], 8, 8 + 16 * (i + 1));

    }

    void needAnalyze() {
        repaint();
    }

    public CircuitElm getElm(int n) {
        if (n >= elmList.size()) return null;
        return elmList.elementAt(n);
    }

    public Adjustable findAdjustable(CircuitElm elm, int item) {
        int i;
        for (i = 0; i != adjustables.size(); i++) {
            Adjustable a = adjustables.get(i);
            if (a.elm == elm && a.editItem == item) return a;
        }
        return null;
    }

    public static native void console(String text)
        /*-{
            console.log(text);
        }-*/;

    public static native void debugger() /*-{
        debugger;
    }-*/;


    public static String formatLength(double value) {
        if (value < 1e-3) { // less than 1 millimeter
            value *= 1e6; // convert to micrometers
            return (Math.round(value * 1000) / 1000.0) + " µm";
        } else if (value < 1) { // less than 1 meter
            value *= 1e3; // convert to millimeters
            return (Math.round(value * 1000) / 1000.0) + " mm";
        } else {
            return (Math.round(value * 1000) / 1000.0) + " m";
        }
    }


    double getIterCount() {
        // IES - remove interaction
        if (speedBar.getValue() == 0) return 0;

        return .1 * Math.exp((speedBar.getValue() - 61) / 24.);

    }


    void runCircuit() {

        //int maxIter = getIterCount();
        long steprate = (long) (160 * getIterCount());
        long tm = System.currentTimeMillis();
        long lit = lastIterTime;
        if (lit == 0) {
            lastIterTime = tm;
            return;
        }
        // Check if we don't need to run simulation (for very slow simulation speeds).
        // If the circuit changed, do at least one iteration to make sure everything is consistent.
        if (steprate * (tm - lastIterTime) <= 1000) return;
        int timeStepCountAtFrameStart = timeStepCount;

        int iter = 0;
        while (iter < speedBar.getValue() && simRunning) {
            // *************************** Katni *******************************
            if (simDimensionality == 1) {
                if (!simulation1D.cyclic) {

                    simulation1D.heatTransferStep();
                    simulation1D.time += simulation1D.dt;
                } else {
                    if (simulation1D.cycleParts.isEmpty()) Window.alert("Sim set to cyclic but cycle parts undefined");

                    simulation1D.cyclePart.execute();  // executes cycle part for time dt at most

                    if (simulation1D.cyclePart.duration > 0.0) {  // maybe here should be > dt
                        simulation1D.time += simulation1D.dt;  // if the duration of current cycle part is 0.0 < duration, advance for dt
                    }
                    simulation1D.cyclePartTime += simulation1D.dt;
                    simulation1D.cyclePart.partTime += simulation1D.dt;
                    if (simulation1D.cyclePartTime >= simulation1D.cyclePart.duration) {
                        /*if (simulation1D.cyclePart.partType == CyclePart.PartType.MAGNETIC_FIELD_CHANGE && simulation1D.cyclePart.duration > 0.0) {
                            for (ThermalControlElement tce : simulation1D.cyclePart.TCEs) {
                                tce.field = !tce.field;
                                // GWT.log("FIELD: " + String.valueOf(tce.field));
                            }
                        }*/
                        simulation1D.cyclePartTime = 0.0;
                        simulation1D.cyclePart.partTime = 0.0;
                        simulation1D.cyclePartIndex = (simulation1D.cyclePartIndex + 1) % simulation1D.numCycleParts;
                        simulation1D.cyclePart = simulation1D.cycleParts.get(simulation1D.cyclePartIndex);
                    }
                }
                // if (simulation1D.westBoundary == Simulation.BorderCondition.PERIODIC)
                //     simulation1D.heatCircuit.temperatureWest = simulation1D.heatCircuit.temperatureWest +
                //         simulation1D.heatCircuit.amplitudeWest * Math.sin(simulation1D.heatCircuit.frequencyWest * simulation1D.time);
                // if (simulation1D.eastBoundary == Simulation.BorderCondition.PERIODIC)
                //     simulation1D.heatCircuit.temperatureEast = simulation1D.heatCircuit.temperatureEast +
                //         simulation1D.heatCircuit.amplitudeEast * Math.sin(simulation1D.heatCircuit.frequencyEast * simulation1D.time);
            }
            if (simDimensionality == 2) {
                simulation2D.heatTransferStep();
//                simulation2D.time += simulation2D.dt;
            }

            // *****************************************************************
            t += timeStep;
            timeStepAccum += timeStep;
            if (timeStepAccum >= maxTimeStep) {
                timeStepAccum -= maxTimeStep;
                timeStepCount++;
            }

            // Check whether enough time has elapsed to perform an *additional* iteration after those we have already completed.
            // But limit total computation time to 33.34ms (30fps)

            tm = System.currentTimeMillis();
            lit = tm;

            if ((steprate * (tm - lastIterTime)) <= ((timeStepCount - timeStepCountAtFrameStart) * 1000L)) {
                break;
            }
            if (tm - lastFrameTime > 16.67) {
                break;
            }
            iter++;
        }
        lastIterTime = lit;

    }


    int min(int a, int b) {
        return (a < b) ? a : b;
    }

    int max(int a, int b) {
        return (a > b) ? a : b;
    }

    public void resetAction() {
        int i;
        if (t == 0) setSimRunning(true);
        t = timeStepAccum = 0;
        timeStepCount = 0;
        for (i = 0; i != elmList.size(); i++)
            getElm(i).reset();
        repaint();
        if (simDimensionality == 1) {
            simulation1D.makeTCC();
        } else if (simDimensionality == 2) {
            simulation2D.makeTCC();
        }
    }

    static void electronSaveAsCallback(String s) {
        s = s.substring(s.lastIndexOf('/') + 1);
        s = s.substring(s.lastIndexOf('\\') + 1);
        theSim.setCircuitTitle(s);
        theSim.allowSave(true);
        theSim.repaint();
    }

    static void electronSaveCallback() {
        theSim.repaint();
    }

    static native void electronSaveAs(String dump) /*-{
        $wnd.showSaveDialog().then(function (file) {
            if (file.canceled)
                return;
            $wnd.saveFile(file, dump);
            @lahde.tccbuilder.client.CirSim::electronSaveAsCallback(Ljava/lang/String;)(file.filePath.toString());
        });
    }-*/;

    static native void electronSave(String dump) /*-{
        $wnd.saveFile(null, dump);
        @lahde.tccbuilder.client.CirSim::electronSaveCallback()();
    }-*/;

    static void electronOpenFileCallback(String text, String name) {
        LoadFile.doLoadCallback(text, name);
        theSim.allowSave(true);
    }

    static native void electronOpenFile() /*-{
        $wnd.openFile(function (text, name) {
            @lahde.tccbuilder.client.CirSim::electronOpenFileCallback(Ljava/lang/String;Ljava/lang/String;)(text, name);
        });
    }-*/;

    static native void toggleDevTools() /*-{
        $wnd.toggleDevTools();
    }-*/;

    static native boolean isElectron() /*-{
        return ($wnd.openFile != undefined);
    }-*/;

    static native String getElectronStartCircuitText() /*-{
        return $wnd.startCircuitText;
    }-*/;

    void allowSave(boolean b) {
        if (saveFileItem != null) saveFileItem.setEnabled(b);
    }

    public void menuPerformed(String menu, String item) {
        if ((menu == "edit" || menu == "main" || menu == "scopes") && noEditCheckItem.getState()) {
            Window.alert(Locale.LS("Editing disabled.  Re-enable from the Options menu."));
            return;
        }
        if (item == "about") aboutBox = new AboutBox(TCCBuilder.versionString);
        if (item == "importfromlocalfile") {
            pushUndo();
            if (isElectron()) electronOpenFile();
            else loadFileInput.click();
        }
        if (item == "newwindow") {
            Window.open(Document.get().getURL(), "_blank", "");
        }
        if (item == "save") electronSave(dumpCircuit());
        if (item == "saveas") electronSaveAs(dumpCircuit());
        if (item == "importfromtext") {
            dialogShowing = new ImportFromTextDialog(this);
        }
        if (item == "importfromdropbox") {
            dialogShowing = new ImportFromDropboxDialog(this);
        }
        if (item == "exportasurl") {
            doExportAsUrl();
            unsavedChanges = false;
        }
        if (item == "exportaslocalfile") {
            doExportAsLocalFile();
            unsavedChanges = false;
        }
        if (item == "reportaslocalfile") {
            doReportAsLocalFile();
            unsavedChanges = false;
        }
        if (item == "exportastext") {
            doExportAsText();
            unsavedChanges = false;
        }
        if (item == "reportastext") {
            getReportAsText();
            unsavedChanges = false;
        }
        if (item == "exportasimage") doExportAsImage();
        if (item == "exportassvg") doExportAsSVG();


        if (item == "print") doPrint();
        if (item == "recover") doRecover();

        if ((menu == "elm" || menu == "scopepop") && contextPanel != null) contextPanel.hide();
        if (menu == "options" && item == "shortcuts") {
            dialogShowing = new ShortcutsDialog(this);
            dialogShowing.show();
        }
        if (item == "search") {
            dialogShowing = new SearchDialog(this);
            dialogShowing.show();
        }
        if (menu == "options" && item == "other") doEdit(new EditOptions(this));
        if (item == "devtools") toggleDevTools();
        if (item == "undo") doUndo();
        if (item == "redo") doRedo();

        // if the mouse is hovering over an element, and a shortcut key is pressed,
        // operate on that element (treat it like a context menu item selection)
        if (menu == "key" && mouseElm != null) {
            menuElm = mouseElm;
            menu = "elm";
        }

        if (item == "cut") {
            if (menu != "elm") menuElm = null;
            doCut();
        }
        if (item == "copy") {
            if (menu != "elm") menuElm = null;
            doCopy();
        }
        if (item == "paste") doPaste(null);
        if (item == "duplicate") {
            if (menu != "elm") menuElm = null;
            doDuplicate();
        }
        if (item == "flip") doFlip();
        if (item == "split") doSplit(menuElm);
        if (item == "selectAll") doSelectAll();
        // if (e.getSource() == exitItem) {
        // destroyFrame();
        // return;
        // }

        if (item == "centrecircuit") {
            pushUndo();
            centreCircuit();
        }


        if (item == "zoomin") zoomCircuit(20, true);
        if (item == "zoomout") zoomCircuit(-20, true);
        if (item == "zoom100") setCircuitScale(1, true);
        if (menu == "elm" && item == "edit") doEdit(menuElm);
        if (item == "delete") {
            if (menu != "elm") menuElm = null;
            pushUndo();
            doDelete(true);
        }
        if (item == "sliders") doSliders(menuElm);


        if (item == "viewTemps" && menuElm != null) {
            ThermalControlElement tce = (ThermalControlElement) menuElm;
            if (trackedTemperatures.contains(tce)) {
                trackedTemperatures.remove(tce);
            } else {
                trackedTemperatures.add((ThermalControlElement) tce);
            }
        }


        if (menu == "circuits" && item.indexOf("setup ") == 0) {
            pushUndo();
            int sp = item.indexOf(' ', 6);
            readSetupFile(item.substring(6, sp), item.substring(sp + 1));
        }
        if (item == "newblankcircuit") {
            pushUndo();
            readSetupFile("blank.txt", "Blank Circuit");
        }

        // if (ac.indexOf("setup ") == 0) {
        // pushUndo();
        // readSetupFile(ac.substring(6),
        // ((MenuItem) e.getSource()).getLabel());
        // }

        // IES: Moved from itemStateChanged()
        if (menu == "main") {
            if (contextPanel != null) contextPanel.hide();
            // MenuItem mmi = (MenuItem) mi;
            // int prevMouseMode = mouseMode;
            setMouseMode(MODE_ADD_ELM);
            String s = item;
            if (s.length() > 0) mouseModeStr = s;
            if (s.compareTo("DragAll") == 0) setMouseMode(MODE_DRAG_ALL);
            else if (s.compareTo("DragRow") == 0) setMouseMode(MODE_DRAG_ROW);
            else if (s.compareTo("DragColumn") == 0) setMouseMode(MODE_DRAG_COLUMN);
            else if (s.compareTo("DragSelected") == 0) setMouseMode(MODE_DRAG_SELECTED);
            else if (s.compareTo("DragPost") == 0) setMouseMode(MODE_DRAG_POST);
            else if (s.compareTo("Select") == 0) setMouseMode(MODE_SELECT);
            // else if (s.length() > 0) {
            // try {
            // addingClass = Class.forName(s);
            // } catch (Exception ee) {
            // ee.printStackTrace();
            // }
            // }
            // else
            // setMouseMode(prevMouseMode);
            tempMouseMode = mouseMode;
        }
        if (item == "fullscreen") {
            if (!Graphics.isFullScreen) Graphics.viewFullScreen();
            else Graphics.exitFullScreen();
            centreCircuit();
        }
        if (item == "toggleOverlay") {
            viewTempsOverlay = !viewTempsOverlay;
            showOverlayCheckItem.setState(viewTempsOverlay);
        }
        repaint();
    }

    void doEdit(Editable eable) {
        clearSelection();
        pushUndo();
        if (editDialog != null) {
            // requestFocus();
            editDialog.setVisible(false);
            editDialog = null;
        }

        editDialog = new EditDialog(eable, this);
        editDialog.show();
    }

    void doSliders(CircuitElm ce) {
        clearSelection();
        pushUndo();
        dialogShowing = new SliderDialog(ce, this);
        dialogShowing.show();
    }

    void doExportAsUrl() {
        String dump = dumpCircuit();
        dialogShowing = new ExportAsUrlDialog(dump);
        dialogShowing.show();
    }

    void doExportAsText() {
        // String dump = dumpCircuit();
        String dump = dumpCircuit(); // Katni
        dialogShowing = new ExportAsTextDialog(this, dump);
        dialogShowing.show();
    }

    void getReportAsText() {
        String dump = "An error occurred.";
        try {
            if (simDimensionality == 1)
                dump = simulation1D.getReport();
            else
                dump = simulation2D.getReport();
        } catch (Exception ignore) {
        }

        dialogShowing = new ExportAsTextDialog(this, dump);
        dialogShowing.show();
    }

    void doExportAsImage() {
        dialogShowing = new ExportAsImageDialog(CAC_IMAGE);
        dialogShowing.show();
    }

    void doExportAsLocalFile() {
        String dump = dumpCircuit();
        dialogShowing = new ExportAsLocalFileDialog(dump);
        dialogShowing.show();
    }

    void doReportAsLocalFile() {
        String dump = "An error occurred.";
        try {
            if (simDimensionality == 1)
                dump = simulation1D.getReport();
            else
                dump = simulation2D.getReport();
        } catch (Exception ignore) {
        }

        dialogShowing = new ExportAsLocalFileDialog(dump);
        dialogShowing.show();
    }

    public void importCircuitFromText(String circuitText, boolean subcircuitsOnly) {
        int flags = subcircuitsOnly ? (CirSim.RC_SUBCIRCUITS | CirSim.RC_RETAIN) : 0;
        if (circuitText != null) {
            readCircuit(circuitText, flags);
            allowSave(false);
        }
    }

    String dumpOptions() {
        int f = (smallGridCheckItem.getState()) ? 2 : 0;
        // 32 = linear scale in afilter
        String dump = "$ " + f + " " + maxTimeStep + " " + getIterCount() + " " + " " + minTimeStep + "\n";
        return dump;
    }

    String dumpCircuit() {
        int i;

        String dump = dumpOptions();
        dump += simulation1D.dump();

        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            String m = ce.dumpModel();
            if (m != null && !m.isEmpty()) dump += m + "\n";
            dump += ce.dump() + "\n";
        }

        for (i = 0; i != adjustables.size(); i++) {
            Adjustable adj = adjustables.get(i);
            dump += "38 " + adj.dump() + "\n";
        }
        if (hintType != -1) dump += "h " + hintType + " " + hintItem1 + " " + hintItem2 + "\n";
        dump += simulation1D.dumpSimulationCycleParts();
        return dump;
    }

    void getSetupList(final boolean openDefault) {

        String url;
        url = GWT.getModuleBaseURL() + "setuplist.txt" + "?v=" + random.nextInt();
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    Window.alert(Locale.LS("Can't load circuit list!"));
                    GWT.log("File Error Response", exception);
                }

                public void onResponseReceived(Request request, Response response) {
                    // processing goes here
                    if (response.getStatusCode() == Response.SC_OK) {
                        String text = response.getText();
                        processSetupList(text.getBytes(), openDefault);
                        // end or processing
                    } else {
                        Window.alert(Locale.LS("Can't load circuit list!"));
                        GWT.log("Bad file server response:" + response.getStatusText());
                    }
                }
            });
        } catch (RequestException e) {
            GWT.log("failed file reading", e);
        }
    }

    void processSetupList(byte b[], final boolean openDefault) {
        int len = b.length;
        MenuBar currentMenuBar;
        MenuBar stack[] = new MenuBar[6];
        int stackptr = 0;
        currentMenuBar = new MenuBar(true);
        currentMenuBar.setAutoOpen(true);
        menuBar.addItem(Locale.LS("Circuits"), currentMenuBar);
        stack[stackptr++] = currentMenuBar;
        int p;
        for (p = 0; p < len; ) {
            int l;
            for (l = 0; l != len - p; l++)
                if (b[l + p] == '\n' || b[l + p] == '\r') {
                    l++;
                    break;
                }
            String line = new String(b, p, l - 1);
            if (line.charAt(0) == '#') ;
            else if (line.charAt(0) == '+') {
                // MenuBar n = new Menu(line.substring(1));
                MenuBar n = new MenuBar(true);
                n.setAutoOpen(true);
                currentMenuBar.addItem(Locale.LS(line.substring(1)), n);
                currentMenuBar = stack[stackptr++] = n;
            } else if (line.charAt(0) == '-') {
                currentMenuBar = stack[--stackptr - 1];
            } else {
                int i = line.indexOf(' ');
                if (i > 0) {
                    String title = Locale.LS(line.substring(i + 1));
                    boolean first = false;
                    if (line.charAt(0) == '>') first = true;
                    String file = line.substring(first ? 1 : 0, i);
                    currentMenuBar.addItem(new MenuItem(title, new MyCommand("circuits", "setup " + file + " " + title)));
                    if (file.equals(startCircuit) && startLabel == null) {
                        startLabel = title;
                        titleLabel.setText(title);
                    }
                    if (first && startCircuit == null) {
                        startCircuit = file;
                        startLabel = title;
                        if (openDefault && stopMessage == null) readSetupFile(startCircuit, startLabel);
                    }
                }
            }
            p += l;
        }
    }

    void readCircuit(String text, int flags) {
        readCircuit(text.getBytes(), flags);
        if ((flags & RC_KEEP_TITLE) == 0) titleLabel.setText(null);
    }

    void readCircuit(String text) {
        readCircuit(text.getBytes(), 0);
        titleLabel.setText(null);
    }

    void setCircuitTitle(String s) {
        if (s != null) titleLabel.setText(s);
    }

    void readSetupFile(String str, String title) {
        // TODO: Maybe think about some better approach to cache management!
        String url = GWT.getModuleBaseURL() + "circuits/" + str + "?v=" + random.nextInt();
        loadFileFromURL(url);
        if (title != null) titleLabel.setText(title);
        unsavedChanges = false;

    }

//    public void setupForTest() {
//        cyclic = true;
//        temp_left = 291;
//        h_left = 100000.0;
//        h_right = 100000.0;
//        temp_right = 290;
//        qIn = 0;
//        qOut = 0;
//        dt = 0.005;
//        startTemp = 290;
//        left_boundary = 41;
//        right_boundary = 42;
//
//
//        CyclePart cyclePartHeatTransfer = new CyclePart(cycleParts.size(), this);
//        cyclePartHeatTransfer.partType = CyclePart.PartType.HEAT_TRANSFER;
//        cyclePartHeatTransfer.duration = 1.0;
//        cycleParts.add(cyclePartHeatTransfer);
//
//        CyclePart cyclePartMagneticFieldChange = new CyclePart(cycleParts.size(), this);
//        cyclePartMagneticFieldChange.partType = CyclePart.PartType.MAGNETIC_FIELD_CHANGE;
//        cyclePartMagneticFieldChange.TCEs.add(simTCEs.get(2));
//        simTCEs.get(2).fieldIndex = 2;
//        cycleParts.add(cyclePartMagneticFieldChange);
//
//        // CyclePart cyclePartPropertiesChange = new CyclePart(cycleParts.size(), this);
//        // cyclePartPropertiesChange.partType = CyclePart.PartType.PROPERTIES_CHANGE;
//        // cyclePartPropertiesChange.components.add(simComponents.get(1));
//        // cyclePartPropertiesChange.components.add(simComponents.get(3));
//
////        tukaj neki smrdi :{ ( ne dela )
///*        cyclePartPropertiesChange.newProperties.add(new Vector<Double>());
//        cyclePartPropertiesChange.newProperties.lastElement().add(-1.0);
//        cyclePartPropertiesChange.newProperties.lastElement().add(-1.0);
//        cyclePartPropertiesChange.newProperties.lastElement().add(-1.0);
//        */
////        cycleParts.add(cyclePartPropertiesChange);
//
//        for (CyclePart cp : cycleParts) {
//            CyclicDialog cd = new CyclicDialog(this);
//            cd.printCyclePart(cp, cyclicOperationLabel);
//            cd.closeDialog();
//        }
//    }
//
//    void runTestSuite() {
//        if (testTimer == null) {
//            testCounter = 0;
//            setSimRunning(true);
//            //materials to use
//            for (Material m : materialHashMap.values()) {
//                if (!m.isLoaded()) m.readFiles();
//            }
//            //FIXME: An error can occur if the timer starts before the files have been read!
//            testTimer = new Timer() {
//                public void run() {
//                    switch (testCounter) {
//                        //both sides same
//                        case 0:
//                            temp_left = 290;
//                            temp_right = 290;
//                            loadRandomComponents("100001-Inox", "100001-Inox", "100001-Inox");
//                            testCounter++;
//                            break;
//
//                        //left side higher
//                        case 1:
//                            temp_left = 291;
//                            temp_right = 290;
//                            loadRandomComponents("100001-Inox", "100001-Inox", "100001-Inox");
//                            testCounter++;
//                            break;
//
//                        //right side higher
//                        case 2:
//                            temp_left = 290;
//                            temp_right = 295.124;
//                            loadRandomComponents("100001-Inox", "100001-Inox", "100001-Inox");
//                            testCounter++;
//                            break;
//                        case 3:
//                            temp_left = 350;
//                            temp_right = 290;
//                            loadRandomComponents("100001-Inox", "500001-Si", "100001-Inox");
//                            testCounter++;
//                            break;
//                        case 4:
//                            temp_left = 291;
//                            temp_right = 290;
//                            loadRandomComponents("500001-Si", "100001-Inox", "500001-Si");
//                            testCounter++;
//                            break;
//                        default:
//                            testTimer.cancel();
//                            testTimer = null;
//                            testCounter = 0;
//                            setSimRunning(false);
//                            break;
//                    }
//                }
//
//
//            };
//            testTimer.scheduleRepeating(1000); // 5000 milliseconds = 5 seconds
//        }
//    }

    void loadRandomComponents(String material0, String material1, String material2) {
        ArrayList<Integer> points = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            points.add(snapGrid((int) (Math.random() * canvasWidth)));

        }
        Collections.sort(points);
        GWT.log(materialHashMap.get("100001-Inox").cp.get(0).get(0) + "");
        String text = "$ 1 5.0E-6 10 50 5.0\n" + "520 " + points.get(0) + " 192 " + points.get(1) + " 192 0 0 " + material0 + " 4 " + Math.abs(points.get(0) - points.get(1)) + "\n" + "520 " + points.get(1) + " 192 " + points.get(2) + " 192 0 1 " + material1 + "  8 " + Math.abs(points.get(1) - points.get(2)) + "\n" + "520 " + points.get(2) + " 192 " + points.get(3) + " 192 0 2 " + material2 + " 10 " + Math.abs(points.get(2) - points.get(3)) + "\n";
        readCircuit(text, RC_KEEP_TITLE);
        allowSave(false);
        unsavedChanges = false;
        resetAction();
    }

    void loadFileFromURL(String url) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);

        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    Window.alert(Locale.LS("Can't load circuit!"));
                    GWT.log("File Error Response", exception);
                }

                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        String text = response.getText();
                        readCircuit(text, RC_KEEP_TITLE);
                        allowSave(false);
                        unsavedChanges = false;
                    } else {
                        Window.alert(Locale.LS("Can't load circuit!"));
                        GWT.log("Bad file server response:" + response.getStatusText());
                    }
                }
            });
        } catch (RequestException e) {
            GWT.log("failed file reading", e);
        }

    }

    // ************************ Katni *****************************
    // void readThermalFile(String str) {
    // System.out.println(str);
    // String url=GWT.getModuleBaseURL()+"circuits/inox_k.txt";// +
    // "?v="+random.nextInt(); ;
    // loadThermalFileFromURL(url);
    // }

    // void loadThermalFileFromURL(String url) {
    // RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, url);

    // try {
    // requestBuilder.sendRequest(null, new RequestCallback() {
    // public void onError(Request request, Throwable exception) {
    // Window.alert(Locale.LS("Can't load data!"));
    // GWT.log("File Error Response", exception);
    // }

    // public void onResponseReceived(Request request, Response response) {
    // if (response.getStatusCode()==Response.SC_OK) {
    // String text = response.getText();
    // readThermalData(text.getBytes(), RC_KEEP_TITLE);
    // allowSave(false);
    // unsavedChanges = false;
    // }
    // else {
    // Window.alert(Locale.LS("Can't load data!"));
    // GWT.log("Bad file server response:"+response.getStatusText() );
    // }
    // }
    // });
    // } catch (RequestException e) {
    // GWT.log("failed file reading", e);
    // }

    // }
    // ************************************************************

    static final int RC_RETAIN = 1;
    static final int RC_NO_CENTER = 2;
    static final int RC_SUBCIRCUITS = 4;
    static final int RC_KEEP_TITLE = 8;

    void readCircuit(byte b[], int flags) {
        int i;
        int len = b.length;
        if ((flags & RC_RETAIN) == 0) {
            clearMouseElm();
            for (i = 0; i != elmList.size(); i++) {
                CircuitElm ce = getElm(i);
                ce.delete();
            }
            t = timeStepAccum = 0;
            elmList.removeAllElements();
            simulation1D = new Simulation1D();
            trackedTemperatures = new ArrayList<ThermalControlElement>();
            hintType = -1;
            maxTimeStep = 5e-6;
            minTimeStep = 50e-12;
            smallGridCheckItem.setState(false);
            setGrid();
            speedBar.setValue(100); // 57
            lastIterTime = 0;


        }
        boolean subs = (flags & RC_SUBCIRCUITS) != 0;
        // cv.repaint();
        int p;
        for (p = 0; p < len; ) {
            int l;
            int linelen = len - p; // IES - changed to allow the last line to not end with a delim.
            for (l = 0; l != len - p; l++)
                if (b[l + p] == '\n' || b[l + p] == '\r') {
                    linelen = l++;
                    if (l + p < b.length && b[l + p] == '\n') l++;
                    break;
                }
            String line = new String(b, p, linelen);
            StringTokenizer st = new StringTokenizer(line, " +\t\n\r\f");
            while (st.hasMoreTokens()) {

                String type = st.nextToken();
                int tint = type.charAt(0);
                try {
                    if (subs && tint != '.') continue;
                    if (tint == 'h') {
                        readHint(st);
                        break;
                    }
                    if (tint == '$') {
                        readOptions(st, flags);
                        break;
                    }

                    if (tint == '!') {
                        simulation1D.undump(st);
                        break;
                    }
                    if (tint == '@') {

                        simulation1D.loadCycleParts(st);
                        break;
                    }


                    // do not add new symbols here without testing export as link

                    // if first character is a digit then parse the type as a number
                    if (tint >= '0' && tint <= '9') tint = Integer.parseInt(type);


                    if (tint == 38) {
                        Adjustable adj = new Adjustable(st, this);
                        if (adj.elm != null) adjustables.add(adj);
                        break;
                    }

                    int x1 = Integer.parseInt(st.nextToken());
                    int y1 = Integer.parseInt(st.nextToken());
                    int x2 = Integer.parseInt(st.nextToken());
                    int y2 = Integer.parseInt(st.nextToken());
                    int f = Integer.parseInt(st.nextToken());

                    CircuitElm newce = createCe(tint, x1, y1, x2, y2, f, st);
                    if (newce == null) {
                        GWT.log("unrecognized dump type: " + type);
                        break;
                    }
                    /*
                     * debug code to check if allocNodes() is called in constructor. It gets called
                     * in setPoints() but that doesn't get called for subcircuits. double vv[] =
                     * newce.volts; int vc = newce.getPostCount() + newce.getInternalNodeCount(); if
                     * (vv.length != vc) console("allocnodes not called! " + tint);
                     */
                    newce.setPoints();
                    elmList.addElement(newce);
                    if (newce instanceof ThermalControlElement) {
                        simulation1D.simTCEs.add((ThermalControlElement) newce);
                        trackedTemperatures.add((ThermalControlElement) newce);
                    }
                    if (newce instanceof TwoDimComponent) simulation2D.simTwoDimComponents.add((TwoDimComponent) newce);

                } catch (Exception ee) {
                    ee.printStackTrace();
                    console("exception while undumping " + ee);
                    break;
                }
                break;
            }
            p += l;

        }
        if ((flags & RC_RETAIN) == 0) {
            enableItems();
            // create sliders as needed
            for (i = 0; i < adjustables.size(); i++) {
                if (!adjustables.get(i).createSlider(this)) adjustables.remove(i--);
            }
        }
        // if (!retain)
        // handleResize(); // for scopes
        needAnalyze();
        if ((flags & RC_NO_CENTER) == 0) centreCircuit();

    }

    // ***************************************************** Katni
    // ***********************************
    // void readThermalData(byte b[], int flags) {
    // int i;
    // int len = b.length;
    // boolean subs = (flags & RC_SUBCIRCUITS) != 0;
    // //cv.repaint();
    // int p;
    // int linePos = 0;
    // for (p = 0; p < len; ) {
    // int l;
    // int linelen = len-p; // IES - changed to allow the last line to not end with
    // a delim.
    // for (l = 0; l != len-p; l++)
    // if (b[l+p] == '\n' || b[l+p] == '\r') {
    // linelen = l++;
    // if (l+p < b.length && b[l+p] == '\n')
    // l++;
    // break;
    // }
    // String line = new String(b, p, linelen);
    // StringTokenizer st = new StringTokenizer(line, " +\t\n\r\f");
    // while (st.hasMoreTokens()) {
    // // String type = st.nextToken();
    // // int tint = type.charAt(0);
    // try {
    // double x1 = new Double(st.nextToken()).doubleValue();
    // double y1 = new Double(st.nextToken()).doubleValue();
    // tempsFromFile[linePos] = x1;
    // kValuesFromFile[linePos] = y1;
    // } catch (Exception ee) {
    // ee.printStackTrace();
    // console("exception while undumping " + ee);
    // break;
    // }
    // break;
    // }
    // p += l;
    // linePos++;

    // }
    // String T = "Temperatures: " + String.valueOf(tempsFromFile[0]);
    // T += ", " + String.valueOf(tempsFromFile[1]) + ", " +
    // String.valueOf(tempsFromFile[2]) + "\n";
    // T += "Conductivities: " + String.valueOf(kValuesFromFile[0]) + ", " +
    // String.valueOf(kValuesFromFile[1]) + ", " +
    // String.valueOf(kValuesFromFile[2]) + "\n";
    // //Window.alert(T);
    // }
    // ***********************************************************************************************

    // delete sliders for an element
    void deleteSliders(CircuitElm elm) {
        int i;
        if (adjustables == null) return;
        for (i = adjustables.size() - 1; i >= 0; i--) {
            Adjustable adj = adjustables.get(i);
            if (adj.elm == elm) {
                adj.deleteSlider(this);
                adjustables.remove(i);
            }
        }
    }

    void readHint(StringTokenizer st) {
        hintType = Integer.parseInt(st.nextToken());
        hintItem1 = Integer.parseInt(st.nextToken());
        hintItem2 = Integer.parseInt(st.nextToken());
    }

    void readOptions(StringTokenizer st, int importFlags) {
        int flags = Integer.parseInt(st.nextToken());

        if ((importFlags & RC_RETAIN) != 0) {
            // need to set small grid if pasted circuit uses it
            if ((flags & 2) != 0) smallGridCheckItem.setState(true);
            return;
        }

        smallGridCheckItem.setState((flags & 2) != 0);
        maxTimeStep = timeStep = Double.parseDouble(st.nextToken());
        double sp = Double.parseDouble(st.nextToken());
        int sp2 = (int) (Math.log(10 * sp) * 24 + 61.5);
        // int sp2 = (int) (Math.log(sp)*24+1.5);
        speedBar.setValue(57);

        try {
            minTimeStep = Double.parseDouble(st.nextToken());
        } catch (Exception e) {
        }
        setGrid();
    }

    int snapGrid(int n) {
        return (n + gridRound) & gridMask;
    }

    boolean doSwitch(int x, int y) {
        if (mouseElm == null || !(mouseElm instanceof SwitchElm)) return false;
        SwitchElm se = (SwitchElm) mouseElm;
        if (!se.getSwitchRect().contains(x, y)) return false;
        se.toggle();
        elementToggled = true;
        return true;
    }

    int locateElm(CircuitElm elm) {
        int i;
        for (i = 0; i != elmList.size(); i++)
            if (elm == elmList.elementAt(i)) return i;
        return -1;
    }

    public void mouseDragged(MouseMoveEvent e) {
        // ignore right mouse button with no modifiers (needed on PC)
        if (e.getNativeButton() == NativeEvent.BUTTON_RIGHT) {
            if (!(e.isMetaKeyDown() || e.isShiftKeyDown() || e.isControlKeyDown() || e.isAltKeyDown())) return;
        }

        if (tempMouseMode == MODE_DRAG_SPLITTER) {
            dragSplitter(e.getX(), e.getY());
            return;
        }
        int gx = inverseTransformX(e.getX());
        int gy = inverseTransformY(e.getY());
        if (!circuitArea.contains(e.getX(), e.getY())) return;
        boolean changed = false;
        if (dragElm != null && dragElm.resizable) {
            dragElm.drag(gx, gy);
        }
        boolean success = true;
        switch (tempMouseMode) {
            case MODE_DRAG_ALL:
                dragAll(e.getX(), e.getY());
                break;
            case MODE_DRAG_ROW:
                dragRow(snapGrid(gx), snapGrid(gy));
                changed = true;
                break;
            case MODE_DRAG_COLUMN:
                dragColumn(snapGrid(gx), snapGrid(gy));
                changed = true;
                break;
            case MODE_DRAG_POST:
                if (mouseElm != null && mouseElm.resizable) {
                    dragPost(snapGrid(gx), snapGrid(gy), e.isShiftKeyDown());
                    if (mouseElm instanceof TwoDimComponent) ((TwoDimComponent) mouseElm).calculateLengthHeight();
                    else if (mouseElm instanceof ThermalControlElement)
                        ((ThermalControlElement) mouseElm).calculateLength();
                    changed = true;
                }
                break;
            case MODE_SELECT:
                if (mouseElm == null) selectArea(gx, gy, e.isShiftKeyDown());
                else if (!noEditCheckItem.getState()) {
                    // wait short delay before dragging. This is to fix problem where switches were
                    // accidentally getting
                    // dragged when tapped on mobile devices
                    if (System.currentTimeMillis() - mouseDownTime < 150) return;

                    tempMouseMode = MODE_DRAG_SELECTED;
                    changed = success = dragSelected(gx, gy);

                }
                break;
            case MODE_DRAG_SELECTED:
                changed = success = dragSelected(gx, gy);

                break;

        }
        dragging = true;
        if (success) {
            dragScreenX = e.getX();
            dragScreenY = e.getY();
            // console("setting dragGridx in mousedragged");
            dragGridX = inverseTransformX(dragScreenX);
            dragGridY = inverseTransformY(dragScreenY);
            if (!(tempMouseMode == MODE_DRAG_SELECTED && onlyGraphicsElmsSelected())) {
                dragGridX = snapGrid(dragGridX);
                dragGridY = snapGrid(dragGridY);
            }

        }
        if (changed) writeRecoveryToStorage();
        repaint();
    }

    void dragSplitter(int x, int y) {
        double h = (double) canvasHeight;
        if (h < 1) h = 1;
        setCircuitArea();
        repaint();
    }

    void dragAll(int x, int y) {
        int dx = x - dragScreenX;
        int dy = y - dragScreenY;
        if (dx == 0 && dy == 0) return;
        transform[4] += dx;
        transform[5] += dy;
        dragScreenX = x;
        dragScreenY = y;
    }

    void dragRow(int x, int y) {
        int dy = y - dragGridY;
        if (dy == 0) return;
        int i;
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce.y == dragGridY) ce.movePoint(0, 0, dy);
            if (ce.y2 == dragGridY) ce.movePoint(1, 0, dy);
        }
        removeZeroLengthElements();
    }

    void dragColumn(int x, int y) {
        int dx = x - dragGridX;
        if (dx == 0) return;
        int i;
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce.x == dragGridX) ce.movePoint(0, dx, 0);
            if (ce.x2 == dragGridX) ce.movePoint(1, dx, 0);
        }
        removeZeroLengthElements();
    }

    boolean onlyGraphicsElmsSelected() {
        if (mouseElm != null) return false;
        int i;
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce.isSelected()) return false;
        }
        return true;
    }

    boolean dragSelected(int x, int y) {
        boolean me = false;
        int i;
        if (mouseElm != null && !mouseElm.isSelected()) mouseElm.setSelected(me = true);

        if (!onlyGraphicsElmsSelected()) {
            // console("Snapping x and y");
            x = snapGrid(x);
            y = snapGrid(y);
        }

        int dx = x - dragGridX;
        // console("dx="+dx+"dragGridx="+dragGridX);
        int dy = y - dragGridY;
        if (dx == 0 && dy == 0) {
            // don't leave mouseElm selected if we selected it above
            if (me) mouseElm.setSelected(false);
            return false;
        }
        boolean allowed = true;

        // check if moves are allowed
        for (i = 0; allowed && i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            if (ce.isSelected() && !ce.allowMove(dx, dy)) allowed = false;
        }

        if (allowed) {
            for (i = 0; i != elmList.size(); i++) {
                CircuitElm ce = getElm(i);
                if (ce.isSelected()) ce.move(dx, dy);
            }
            needAnalyze();
        }

        // don't leave mouseElm selected if we selected it above
        if (me) mouseElm.setSelected(false);


        return allowed;
    }

    void dragPost(int x, int y, boolean all) {
        if (draggingPost == -1) {
            draggingPost = (Graphics.distanceSq(mouseElm.x, mouseElm.y, x, y) > Graphics.distanceSq(mouseElm.x2, mouseElm.y2, x, y)) ? 1 : 0;
        }
        int dx = x - dragGridX;
        int dy = y - dragGridY;
        if (dx == 0 && dy == 0) return;

        if (all) {
            // go through all elms
            int i;
            for (i = 0; i != elmList.size(); i++) {
                CircuitElm e = elmList.get(i);

                // which post do we move?
                int p = 0;
                if (e.x == dragGridX && e.y == dragGridY) p = 0;
                else if (e.x2 == dragGridX && e.y2 == dragGridY) p = 1;
                else continue;
                e.movePoint(p, dx, dy);
            }
        } else mouseElm.movePoint(draggingPost, dx, dy);
        needAnalyze();
    }

    void doFlip() {
        menuElm.flipPosts();
        needAnalyze();
    }

    void doSplit(CircuitElm ce) {
        int x = snapGrid(inverseTransformX(menuX));
        int y = snapGrid(inverseTransformY(menuY));
        if (ce == null || !(ce instanceof ConduitElm)) return;
        if (ce.x == ce.x2) x = ce.x;
        else y = ce.y;

        // don't create zero-length wire
        if (x == ce.x && y == ce.y || x == ce.x2 && y == ce.y2) return;

        ConduitElm newWire = new ConduitElm(x, y);
        newWire.drag(ce.x2, ce.y2);
        ce.drag(x, y);
        elmList.addElement(newWire);
        needAnalyze();
    }

    void selectArea(int x, int y, boolean add) {
        int x1 = min(x, initDragGridX);
        int x2 = max(x, initDragGridX);
        int y1 = min(y, initDragGridY);
        int y2 = max(y, initDragGridY);
        selectedArea = new Rectangle(x1, y1, x2 - x1, y2 - y1);
        int i;
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            ce.selectRect(selectedArea, add);
        }
    }

    // void setSelectedElm(CircuitElm cs) {
    // int i;
    // for (i = 0; i != elmList.size(); i++) {
    // CircuitElm ce = getElm(i);
    // ce.setSelected(ce == cs);
    // }
    // mouseElm = cs;
    // }

    void setMouseElm(CircuitElm ce) {
        if (ce != mouseElm) {
            if (mouseElm != null) mouseElm.setMouseElm(false);
            if (ce != null) ce.setMouseElm(true);
            mouseElm = ce;
            int i;
            for (i = 0; i < adjustables.size(); i++)
                adjustables.get(i).setMouseElm(ce);
        }
    }

    void removeZeroLengthElements() {
        int i;
        boolean changed = false;
        for (i = elmList.size() - 1; i >= 0; i--) {
            CircuitElm ce = getElm(i);
            if (ce.x == ce.x2 && ce.y == ce.y2) {
                elmList.remove(ce);
                simulation1D.simTCEs.remove((ThermalControlElement) ce);
                trackedTemperatures.remove((ThermalControlElement) ce);
                Collections.sort(trackedTemperatures);
                ce.delete();
                changed = true;
            }
        }
        needAnalyze();
    }

    boolean mouseIsOverSplitter(int x, int y) {
        boolean isOverSplitter;

        isOverSplitter = ((x >= 0) && (x < circuitArea.width) && (y >= circuitArea.height - 5) && (y < circuitArea.height));
        if (isOverSplitter != mouseWasOverSplitter) {
            if (isOverSplitter) setCursorStyle("cursorSplitter");
            else setMouseMode(mouseMode);
        }
        mouseWasOverSplitter = isOverSplitter;
        return isOverSplitter;
    }

    public void onMouseMove(MouseMoveEvent e) {
        e.preventDefault();
        mouseCursorX = e.getX();
        mouseCursorY = e.getY();
        if (mouseDragging) {
            mouseDragged(e);
            return;
        }
        mouseSelect(e);
    }

    // convert screen coordinates to grid coordinates by inverting circuit transform
    int inverseTransformX(double x) {
        return (int) ((x - transform[4]) / transform[0]);
    }

    int inverseTransformY(double y) {
        return (int) ((y - transform[5]) / transform[3]);
    }

    // convert grid coordinates to screen coordinates
    int transformX(double x) {
        return (int) ((x * transform[0]) + transform[4]);
    }

    int transformY(double y) {
        return (int) ((y * transform[3]) + transform[5]);
    }

    // need to break this out into a separate routine to handle selection,
    // since we don't get mouse move events on mobile
    public void mouseSelect(MouseEvent<?> e) {
        // The following is in the original, but seems not to work/be needed for GWT
        // if (e.getNativeButton()==NativeEvent.BUTTON_LEFT)
        // return;
        CircuitElm newMouseElm = null;
        mouseCursorX = e.getX();
        mouseCursorY = e.getY();
        int sx = e.getX();
        int sy = e.getY();
        int gx = inverseTransformX(sx);
        int gy = inverseTransformY(sy);
        // console("Settingd draggridx in mouseEvent");
        dragGridX = snapGrid(gx);
        dragGridY = snapGrid(gy);
        dragScreenX = sx;
        dragScreenY = sy;
        draggingPost = -1;
        int i;
        // CircuitElm origMouse = mouseElm;

        mousePost = -1;
        plotXElm = plotYElm = null;

        if (mouseIsOverSplitter(sx, sy)) {
            setMouseElm(null);
            return;
        }

        if (circuitArea.contains(sx, sy)) {
            if (mouseElm != null && (mouseElm.getHandleGrabbedClose(gx, gy, POSTGRABSQ, MINPOSTGRABSIZE) >= 0)) {
                newMouseElm = mouseElm;
            } else {
                int bestDist = 100000000;
                int bestArea = 100000000;
                for (i = 0; i != elmList.size(); i++) {
                    CircuitElm ce = getElm(i);
                    if (ce.boundingBox.contains(gx, gy)) {
                        int j;
                        int area = ce.boundingBox.width * ce.boundingBox.height;
                        int jn = ce.getPostCount();
                        if (jn > 2) jn = 2;
                        for (j = 0; j != jn; j++) {
                            Point pt = ce.getPost(j);
                            int dist = Graphics.distanceSq(gx, gy, pt.x, pt.y);

                            // if multiple elements have overlapping bounding boxes,
                            // we prefer selecting elements that have posts close
                            // to the mouse pointer and that have a small bounding
                            // box area.
                            if (dist <= bestDist && area <= bestArea) {
                                bestDist = dist;
                                bestArea = area;
                                newMouseElm = ce;
                            }
                        }
                        // prefer selecting elements that have small bounding box area (for
                        // elements with no posts)
                        if (ce.getPostCount() == 0 && area <= bestArea) {
                            newMouseElm = ce;
                            bestArea = area;
                        }
                    }
                } // for
            }
        }
        if (newMouseElm == null) {
            // // the mouse pointer was not in any of the bounding boxes, but we
            // // might still be close to a post
            for (i = 0; i != elmList.size(); i++) {
                CircuitElm ce = getElm(i);
                if (mouseMode == MODE_DRAG_POST) {
                    if (ce.getHandleGrabbedClose(gx, gy, POSTGRABSQ, 0) > 0) {

                        newMouseElm = ce;
                        break;
                    }
                }
                int j;
                int jn = ce.getPostCount();
                for (j = 0; j != jn; j++) {
                    Point pt = ce.getPost(j);
                    // int dist = Graphics.distanceSq(x, y, pt.x, pt.y);
                    if (Graphics.distanceSq(pt.x, pt.y, gx, gy) < 26) {
                        newMouseElm = ce;
                        mousePost = j;
                        break;
                    }
                }
            }
        } else {
            mousePost = -1;
            // look for post close to the mouse pointer
            for (i = 0; i != newMouseElm.getPostCount(); i++) {
                Point pt = newMouseElm.getPost(i);
                if (Graphics.distanceSq(pt.x, pt.y, gx, gy) < 26) mousePost = i;
            }
        }
        repaint();
        setMouseElm(newMouseElm);
    }

    public void onContextMenu(ContextMenuEvent e) {
        e.preventDefault();
        if (!dialogIsShowing()) {
            menuClientX = e.getNativeEvent().getClientX();
            menuClientY = e.getNativeEvent().getClientY();
            doPopupMenu();
        }
    }

    @SuppressWarnings("deprecation")
    void doPopupMenu() {
        if (noEditCheckItem.getState() || dialogIsShowing()) return;
        menuElm = mouseElm;
        int x, y;
        if (mouseElm != null) {
            elmTempsMenuItem.setEnabled(false);

            elmEditMenuItem.setEnabled(mouseElm.getEditInfo(0) != null);
            elmFlipMenuItem.setEnabled(mouseElm.getPostCount() == 2);
            elmSplitMenuItem.setEnabled(canSplit(mouseElm));
            elmSliderMenuItem.setEnabled(sliderItemEnabled(mouseElm));
            contextPanel = new PopupPanel(true);
            contextPanel.add(elmMenuBar);
            contextPanel.setPopupPosition(menuClientX, menuClientY);
            contextPanel.show();

        } else {
            doMainMenuChecks();
            contextPanel = new PopupPanel(true);
            contextPanel.add(mainMenuBar);
            x = Math.max(0, Math.min(menuClientX, canvasWidth - 400));
            y = Math.max(0, Math.min(menuClientY, canvasHeight - 450));
            contextPanel.setPopupPosition(x, y);
            contextPanel.show();
        }
    }

    boolean canSplit(CircuitElm ce) {
        if (!(ce instanceof ConduitElm)) return false;
        ConduitElm we = (ConduitElm) ce;
        if (we.x == we.x2 || we.y == we.y2) return true;
        return false;
    }

    // check if the user can create sliders for this element
    boolean sliderItemEnabled(CircuitElm elm) {
        int i;


        for (i = 0; ; i++) {
            EditInfo ei = elm.getEditInfo(i);
            if (ei == null) return false;
            if (ei.canCreateAdjustable()) return true;
        }
    }

    void longPress() {
        doPopupMenu();
    }

    void twoFingerTouch(int x, int y) {
        tempMouseMode = MODE_DRAG_ALL;
        dragScreenX = x;
        dragScreenY = y;
    }

    // public void mouseClicked(MouseEvent e) {
    public void onClick(ClickEvent e) {
        e.preventDefault();
        // //IES - remove inteaction
        //// if ( e.getClickCount() == 2 && !didSwitch )
        //// doEditMenu(e);
        // if (e.getNativeButton() == NativeEvent.BUTTON_LEFT) {
        // if (mouseMode == MODE_SELECT || mouseMode == MODE_DRAG_SELECTED)
        // clearSelection();
        // }
        if ((e.getNativeButton() == NativeEvent.BUTTON_MIDDLE))
            scrollValues(e.getNativeEvent().getClientX(), e.getNativeEvent().getClientY(), 0);
    }

    public void onDoubleClick(DoubleClickEvent e) {
        e.preventDefault();
        // if (!didSwitch && mouseElm != null)
        if (mouseElm != null && !noEditCheckItem.getState()) doEdit(mouseElm);
    }

    // public void mouseEntered(MouseEvent e) {
    // }

    public void onMouseOut(MouseOutEvent e) {
        mouseCursorX = -1;
    }

    void clearMouseElm() {
        setMouseElm(null);
        plotXElm = plotYElm = null;
    }

    int menuClientX, menuClientY;
    int menuX, menuY;

    public void onMouseDown(MouseDownEvent e) {
        // public void mousePressed(MouseEvent e) {
        e.preventDefault();

        // make sure canvas has focus, not stop button or something else, so all
        // shortcuts work
        canvas.setFocus(true);

        stopElm = null; // if stopped, allow user to select other elements to fix circuit
        menuX = menuClientX = e.getX();
        menuY = menuClientY = e.getY();
        mouseDownTime = System.currentTimeMillis();

        // maybe someone did copy in another window? should really do this when
        // window receives focus
        enablePaste();

        if (e.getNativeButton() != NativeEvent.BUTTON_LEFT && e.getNativeButton() != NativeEvent.BUTTON_MIDDLE) return;

        // set mouseElm in case we are on mobile
        mouseSelect(e);

        mouseDragging = true;

        if (mouseWasOverSplitter) {
            tempMouseMode = MODE_DRAG_SPLITTER;
            return;
        }
        if (e.getNativeButton() == NativeEvent.BUTTON_LEFT) {
            // // left mouse
            tempMouseMode = mouseMode;
            if (e.isAltKeyDown() && e.isMetaKeyDown()) tempMouseMode = MODE_DRAG_COLUMN;
            else if (e.isAltKeyDown() && e.isShiftKeyDown()) tempMouseMode = MODE_DRAG_ROW;
            else if (e.isShiftKeyDown()) tempMouseMode = MODE_SELECT;
            else if (e.isAltKeyDown()) tempMouseMode = MODE_DRAG_ALL;
            else if (e.isControlKeyDown() || e.isMetaKeyDown()) tempMouseMode = MODE_DRAG_POST;
        } else tempMouseMode = MODE_DRAG_ALL;

        if (noEditCheckItem.getState()) tempMouseMode = MODE_SELECT;


        int gx = inverseTransformX(e.getX());
        int gy = inverseTransformY(e.getY());
        // do this BEFORE we change the mouse mode to MODE_DRAG_POST! Or else logic
        // inputs
        // will add dots to the whole circuit when we click on them!
        if (doSwitch(gx, gy)) {
            return;
        }

        // IES - Grab resize handles in select mode if they are far enough apart and you
        // are on top of them
        if (tempMouseMode == MODE_SELECT && mouseElm != null && !noEditCheckItem.getState() && mouseElm.getHandleGrabbedClose(gx, gy, POSTGRABSQ, MINPOSTGRABSIZE) >= 0 && !anySelectedButMouse())
            tempMouseMode = MODE_DRAG_POST;

        if (tempMouseMode != MODE_SELECT && tempMouseMode != MODE_DRAG_SELECTED) clearSelection();

        pushUndo();
        initDragGridX = gx;
        initDragGridY = gy;
        dragging = true;
        if (tempMouseMode != MODE_ADD_ELM) return;
        //
        int x0 = snapGrid(gx);
        int y0 = snapGrid(gy);
        if (!circuitArea.contains(e.getX(), e.getY())) return;

        try {
            dragElm = constructElement(mouseModeStr, x0, y0);
        } catch (Exception ex) {
            GWT.log(ex.toString());
        }
    }

    // check/uncheck/enable/disable menu items as appropriate when menu bar clicked
    // on, or when
    // right mouse menu accessed. also displays shortcuts as a side effect
    void doMainMenuChecks() {
        int c = mainMenuItems.size();
        int i;
        for (i = 0; i < c; i++) {
            String s = mainMenuItemNames.get(i);
            mainMenuItems.get(i).setState(s == mouseModeStr);

            // Code to disable draw menu items when cct is not editable, but no used in this
            // version as it
            // puts up a dialog box instead (see menuPerformed).
            // if (s.length() > 3 && s.substring(s.length()-3)=="Elm")
            // mainMenuItems.get(i).setEnabled(!noEditCheckItem.getState());
        }


    }

    public void onMouseUp(MouseUpEvent e) {
        e.preventDefault();
        mouseDragging = false;

        if (elementToggled) {
            elementToggled = false;
            return;
        }
        // click to clear selection
        if (tempMouseMode == MODE_SELECT && selectedArea == null) clearSelection();

        // cmd-click = split wire
        if (tempMouseMode == MODE_DRAG_POST && draggingPost == -1) doSplit(mouseElm);

        tempMouseMode = mouseMode;
        selectedArea = null;
        dragging = false;
        boolean circuitChanged = false;


        if (dragElm == null)
            reorderByPosition();
        else {
//            GWT.log(String.valueOf(dragElm.direction));
            // if the element is zero size then don't create it
            // IES - and disable any previous selection
            if (dragElm.creationFailed()) {
                dragElm.delete();
                if (mouseMode == MODE_SELECT || mouseMode == MODE_DRAG_SELECTED) clearSelection();
            } else {
                elmList.addElement(dragElm);
                if (dragElm instanceof ThermalControlElement) {
                    simulation1D.simTCEs.add((ThermalControlElement) dragElm);
                    trackedTemperatures.add((ThermalControlElement) dragElm);

                }
                if (dragElm instanceof TwoDimComponent) simulation2D.simTwoDimComponents.add((TwoDimComponent) dragElm);


                reorderByPosition();
                dragElm.draggingDone();
                circuitChanged = true;
                writeRecoveryToStorage();
                unsavedChanges = true;
            }
            dragElm = null;
        }
        if (circuitChanged) {
            needAnalyze();
            pushUndo();
        }
        if (dragElm != null) dragElm.delete();
        dragElm = null;
        repaint();
    }

    public void onMouseWheel(MouseWheelEvent e) {
        e.preventDefault();

        // once we start zooming, don't allow other uses of mouse wheel for a while
        // so we don't accidentally edit a resistor value while zooming
        boolean zoomOnly = System.currentTimeMillis() < zoomTime + 1000;

        if (noEditCheckItem.getState() || !mouseWheelEditCheckItem.getState()) zoomOnly = true;

        if (!zoomOnly) scrollValues(e.getNativeEvent().getClientX(), e.getNativeEvent().getClientY(), e.getDeltaY());

        if (mouseElm instanceof MouseWheelHandler && !zoomOnly) ((MouseWheelHandler) mouseElm).onMouseWheel(e);
        else if (!dialogIsShowing()) {
            mouseCursorX = e.getX();
            mouseCursorY = e.getY();
            zoomCircuit(-e.getDeltaY(), false);
            zoomTime = System.currentTimeMillis();
        }
        repaint();
    }

    void zoomCircuit(double dy) {
        zoomCircuit(dy, false);
    }

    void zoomCircuit(double dy, boolean menu) {
        double newScale;
        double oldScale = transform[0];
        double val = dy * .01;
        newScale = Math.max(oldScale + val, .2);
        newScale = Math.min(newScale, 2.5);
        setCircuitScale(newScale, menu);
    }

    void setCircuitScale(double newScale, boolean menu) {
        int constX = !menu ? mouseCursorX : circuitArea.width / 2;
        int constY = !menu ? mouseCursorY : circuitArea.height / 2;
        int cx = inverseTransformX(constX);
        int cy = inverseTransformY(constY);
        transform[0] = transform[3] = newScale;

        // adjust translation to keep center of screen constant
        // inverse transform = (x-t4)/t0
        transform[4] = constX - cx * newScale;
        transform[5] = constY - cy * newScale;
    }


    void scrollValues(int x, int y, int deltay) {

    }

    void enableItems() {
    }

    void setGrid() {
        gridSize = (smallGridCheckItem.getState()) ? 8 : 16;
        gridMask = ~(gridSize - 1);
        gridRound = gridSize / 2 - 1;
    }

    void pushUndo() {
        redoStack.removeAllElements();
        String s = dumpCircuit();
        if (undoStack.size() > 0 && s.compareTo(undoStack.lastElement().dump) == 0) return;
        undoStack.add(new UndoItem(s));
        enableUndoRedo();
    }

    void doUndo() {
        if (undoStack.size() == 0) return;
        redoStack.add(new UndoItem(dumpCircuit()));
        UndoItem ui = undoStack.remove(undoStack.size() - 1);
        loadUndoItem(ui);
        enableUndoRedo();
    }

    void doRedo() {
        if (redoStack.size() == 0) return;
        undoStack.add(new UndoItem(dumpCircuit()));
        UndoItem ui = redoStack.remove(redoStack.size() - 1);
        loadUndoItem(ui);
        enableUndoRedo();
    }

    void loadUndoItem(UndoItem ui) {
        readCircuit(ui.dump, RC_NO_CENTER);
        transform[0] = transform[3] = ui.scale;
        transform[4] = ui.transform4;
        transform[5] = ui.transform5;
    }

    void doRecover() {
        pushUndo();
        readCircuit(recovery);
        allowSave(false);
        recoverItem.setEnabled(false);
    }

    void enableUndoRedo() {
        redoItem.setEnabled(redoStack.size() > 0);
        undoItem.setEnabled(undoStack.size() > 0);
    }

    void setMouseMode(int mode) {
        mouseMode = mode;
        if (mode == MODE_ADD_ELM) {
            setCursorStyle("cursorCross");
        } else {
            setCursorStyle("cursorPointer");
        }
    }

    void setCursorStyle(String s) {
        if (lastCursorStyle != null) canvas.removeStyleName(lastCursorStyle);
        canvas.addStyleName(s);
        lastCursorStyle = s;
    }

    void setMenuSelection() {
        if (menuElm != null) {
            if (menuElm.selected) return;
            clearSelection();
            menuElm.setSelected(true);
        }
    }

    void doCut() {
        int i;
        pushUndo();
        setMenuSelection();
        clipboard = "";
        for (i = elmList.size() - 1; i >= 0; i--) {
            CircuitElm ce = getElm(i);
            // ScopeElms don't cut-paste well because their reference to a parent
            // elm by number get's messed up in the dump. For now we will just ignore them
            // until I can be bothered to come up with something better
            if (willDelete(ce)) {
                clipboard += ce.dump() + "\n";
            }
        }
        writeClipboardToStorage();
        doDelete(true);
        enablePaste();
    }

    void writeClipboardToStorage() {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null) return;
        stor.setItem("circuitClipboard", clipboard);
    }

    void readClipboardFromStorage() {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null) return;
        clipboard = stor.getItem("circuitClipboard");
    }

    void writeRecoveryToStorage() {
        //console("write recovery");
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null) return;
        String s = dumpCircuit();
        stor.setItem("circuitRecovery", s);
    }

    void readRecovery() {
        Storage stor = Storage.getLocalStorageIfSupported();
        if (stor == null) return;
        recovery = stor.getItem("circuitRecovery");
    }


    ArrayList<CircuitElm> tmp = new ArrayList<CircuitElm>();


    void doDelete(boolean pushUndoFlag) {
        int i;
        if (pushUndoFlag) pushUndo();
        boolean hasDeleted = false;

        for (i = elmList.size() - 1; i >= 0; i--) {
            CircuitElm ce = getElm(i);
            if (willDelete(ce)) {
                if (ce.isMouseElm()) setMouseElm(null);
                ce.delete();
                elmList.removeElementAt(i);

                if (ce instanceof ThermalControlElement) {
                    simulation1D.simTCEs.remove((ThermalControlElement) ce);
                    simulation1D.resetHeatSim();
                    trackedTemperatures.remove(ce);
                    Collections.sort(trackedTemperatures);
                }
                if (ce instanceof TwoDimComponent) {
                    simulation2D.simTwoDimComponents.remove((TwoDimComponent) ce);
                    simulation2D.resetHeatSim();
                }
                reorderByPosition();
                hasDeleted = true;
            }
        }
        if (hasDeleted) {
            needAnalyze();
            writeRecoveryToStorage();
        }
    }

    boolean willDelete(CircuitElm ce) {
        // Is this element in the list to be deleted.
        // This changes the logic from the previous version which would initially only
        // delete selected elements (which could include the mouseElm) and then delete
        // the
        // mouseElm if there were no selected elements. Not really sure this added
        // anything useful
        // to the user experience.
        //
        // BTW, the old logic could also leave mouseElm pointing to a deleted element.
        return ce.isSelected() || ce.isMouseElm();
    }

    String copyOfSelectedElms() {
        String r = dumpOptions();
        for (int i = elmList.size() - 1; i >= 0; i--) {
            CircuitElm ce = getElm(i);
            String m = ce.dumpModel();
            if (m != null && !m.isEmpty()) r += m + "\n";
            // See notes on do cut why we don't copy ScopeElms.
            if (ce.isSelected()) r += ce.dump() + "\n";
        }
        return r;
    }

    void doCopy() {
        // clear selection when we're done if we're copying a single element using the
        // context menu
        boolean clearSel = (menuElm != null && !menuElm.selected);

        setMenuSelection();
        clipboard = copyOfSelectedElms();

        if (clearSel) clearSelection();

        writeClipboardToStorage();
        enablePaste();
    }

    void enablePaste() {
        if (clipboard == null || clipboard.length() == 0) readClipboardFromStorage();
        pasteItem.setEnabled(clipboard != null && clipboard.length() > 0);
    }

    void doDuplicate() {
        String s;
        setMenuSelection();
        s = copyOfSelectedElms();
        doPaste(s);
    }

    void doPaste(String dump) {
        pushUndo();
        clearSelection();
        int i;
        Rectangle oldbb = null;

        // get old bounding box
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            Rectangle bb = ce.getBoundingBox();
            if (oldbb != null) oldbb = oldbb.union(bb);
            else oldbb = bb;
        }

        // add new items
        int oldsz = elmList.size();
        int flags = RC_RETAIN;

        // don't recenter circuit if we're going to paste in place because that will
        // change the transform
        // if (mouseCursorX > 0 && circuitArea.contains(mouseCursorX, mouseCursorY))

        // in fact, don't ever recenter circuit, unless old circuit was empty
        if (oldsz > 0) flags |= RC_NO_CENTER;

        if (dump != null) readCircuit(dump, flags);
        else {
            readClipboardFromStorage();
            readCircuit(clipboard, flags);
        }

        // select new items and get their bounding box
        Rectangle newbb = null;
        for (i = oldsz; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            ce.setSelected(true);
            Rectangle bb = ce.getBoundingBox();
            if (newbb != null) newbb = newbb.union(bb);
            else newbb = bb;
        }

        if (oldbb != null && newbb != null /* && oldbb.intersects(newbb) */) {
            // find a place on the edge for new items
            int dx = 0, dy = 0;
            int spacew = circuitArea.width - oldbb.width - newbb.width;
            int spaceh = circuitArea.height - oldbb.height - newbb.height;

            if (!oldbb.intersects(newbb)) {
                // old coordinates may be really far away so move them to same origin as current
                // circuit
                dx = snapGrid(oldbb.x - newbb.x);
                dy = snapGrid(oldbb.y - newbb.y);
            }

            if (spacew > spaceh) {
                dx = snapGrid(oldbb.x + oldbb.width - newbb.x + gridSize);
            } else {
                dy = snapGrid(oldbb.y + oldbb.height - newbb.y + gridSize);
            }

            // move new items near the mouse if possible
            if (mouseCursorX > 0 && circuitArea.contains(mouseCursorX, mouseCursorY)) {
                int gx = inverseTransformX(mouseCursorX);
                int gy = inverseTransformY(mouseCursorY);
                int mdx = snapGrid(gx - (newbb.x + newbb.width / 2));
                int mdy = snapGrid(gy - (newbb.y + newbb.height / 2));
                for (i = oldsz; i != elmList.size(); i++) {
                    if (!getElm(i).allowMove(mdx, mdy)) break;
                }
                if (i == elmList.size()) {
                    dx = mdx;
                    dy = mdy;
                }
            }

            // move the new items
            for (i = oldsz; i != elmList.size(); i++) {
                CircuitElm ce = getElm(i);
                ce.move(dx, dy);
            }

            // center circuit
            // handleResize();
        }
        needAnalyze();
        writeRecoveryToStorage();
    }

    void clearSelection() {
        int i;
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            ce.setSelected(false);
        }
    }

    void doSelectAll() {
        int i;
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            ce.setSelected(true);
        }
    }

    boolean anySelectedButMouse() {
        for (int i = 0; i != elmList.size(); i++)
            if (getElm(i) != mouseElm && getElm(i).selected) return true;
        return false;
    }

    // public void keyPressed(KeyEvent e) {}
    // public void keyReleased(KeyEvent e) {}

    boolean dialogIsShowing() {
        if (editDialog != null && editDialog.isShowing()) return true;
        if (customLogicEditDialog != null && customLogicEditDialog.isShowing()) return true;
        if (diodeModelEditDialog != null && diodeModelEditDialog.isShowing()) return true;
        if (dialogShowing != null && dialogShowing.isShowing()) return true;
        if (contextPanel != null && contextPanel.isShowing()) return true;
        if (scrollValuePopup != null && scrollValuePopup.isShowing()) return true;
        if (aboutBox != null && aboutBox.isShowing()) return true;
        return false;
    }

    public void onPreviewNativeEvent(NativePreviewEvent e) {
        int cc = e.getNativeEvent().getCharCode();
        int t = e.getTypeInt();
        int code = e.getNativeEvent().getKeyCode();
        if (dialogIsShowing()) {
            if (scrollValuePopup != null && scrollValuePopup.isShowing() && (t & Event.ONKEYDOWN) != 0) {
                if (code == KEY_ESCAPE || code == KEY_SPACE) scrollValuePopup.close(false);
                if (code == KEY_ENTER) scrollValuePopup.close(true);
            }

            // process escape/enter for dialogs
            // multiple edit dialogs could be displayed at once, pick the one in front
            Dialog dlg = editDialog;
            if (diodeModelEditDialog != null) dlg = diodeModelEditDialog;
            if (customLogicEditDialog != null) dlg = customLogicEditDialog;
            if (dialogShowing != null) dlg = dialogShowing;
            if (dlg != null && dlg.isShowing() && (t & Event.ONKEYDOWN) != 0) {
                if (code == KEY_ESCAPE) dlg.closeDialog();
                if (code == KEY_ENTER) dlg.enterPressed();
            }
            return;
        }

        if ((t & Event.ONKEYPRESS) != 0) {
            if (cc == '-') {
                menuPerformed("key", "zoomout");
                e.cancel();
            }
            if (cc == '+' || cc == '=') {
                menuPerformed("key", "zoomin");
                e.cancel();
            }
            if (cc == '0') {
                menuPerformed("key", "zoom100");
                e.cancel();
            }
            if (cc == '/' && shortcuts['/'] == null) {
                menuPerformed("key", "search");
                e.cancel();
            }
        }

        // all other shortcuts are ignored when editing disabled
        if (noEditCheckItem.getState()) return;

        if ((t & Event.ONKEYDOWN) != 0) {
            if (code == KEY_BACKSPACE || code == KEY_DELETE) {
                menuElm = null;
                pushUndo();
                doDelete(true);
                e.cancel();

            }
            if (code == KEY_ESCAPE) {
                setMouseMode(MODE_SELECT);
                mouseModeStr = "Select";
                tempMouseMode = mouseMode;
                e.cancel();
            }
            if (code == KEY_T) {
                menuPerformed("key", "toggleOverlay");
                e.cancel();
            }
            if (e.getNativeEvent().getCtrlKey() || e.getNativeEvent().getMetaKey()) {
                if (code == KEY_C) {
                    menuPerformed("key", "copy");
                    e.cancel();
                }
                if (code == KEY_X) {
                    menuPerformed("key", "cut");
                    e.cancel();
                }
                if (code == KEY_V) {
                    menuPerformed("key", "paste");
                    e.cancel();
                }
                if (code == KEY_Z) {
                    menuPerformed("key", "undo");
                    e.cancel();
                }
                if (code == KEY_Y) {
                    menuPerformed("key", "redo");
                    e.cancel();
                }
                if (code == KEY_D) {
                    menuPerformed("key", "duplicate");
                    e.cancel();
                }
                if (code == KEY_A) {
                    menuPerformed("key", "selectAll");
                    e.cancel();
                }
                if (code == KEY_P) {
                    menuPerformed("key", "print");
                    e.cancel();
                }
                if (code == KEY_N && isElectron()) {
                    menuPerformed("key", "newwindow");
                    e.cancel();
                }
                if (code == KEY_S) {
                    String cmd = "exportaslocalfile";
                    if (isElectron()) cmd = saveFileItem.isEnabled() ? "save" : "saveas";
                    menuPerformed("key", cmd);
                    e.cancel();
                }
                if (code == KEY_O) {
                    menuPerformed("key", "importfromlocalfile");
                    e.cancel();
                }

            }
        }
        if ((t & Event.ONKEYPRESS) != 0) {
            if (cc > 32 && cc < 127) {
                String c = shortcuts[cc];
                e.cancel();
                if (c == null) return;
                setMouseMode(MODE_ADD_ELM);
                mouseModeStr = c;
                tempMouseMode = mouseMode;
            }
            if (cc == 32) {
                setMouseMode(MODE_SELECT);
                mouseModeStr = "Select";
                tempMouseMode = mouseMode;
                e.cancel();
            }
        }

    }


    void createNewLoadFile() {
        // This is a hack to fix what IMHO is a bug in the <INPUT FILE element
        // reloading the same file doesn't create a change event so importing the same
        // file twice
        // doesn't work unless you destroy the original input element and replace it
        // with a new one
        int idx = verticalPanel.getWidgetIndex(loadFileInput);
        LoadFile newlf = new LoadFile(this);
        verticalPanel.insert(newlf, idx);
        verticalPanel.remove(idx + 1);
        loadFileInput = newlf;
    }

    void addWidgetToVerticalPanel(Widget w) {
        if (iFrame != null) {
            int i = verticalPanel.getWidgetIndex(iFrame);
            verticalPanel.insert(w, i);
            setiFrameHeight();
        } else verticalPanel.add(w);
    }

    void removeWidgetFromVerticalPanel(Widget w) {
        verticalPanel.remove(w);
        if (iFrame != null) setiFrameHeight();
    }

    public static CircuitElm createCe(int tint, int x1, int y1, int x2, int y2, int f, StringTokenizer st) {
        switch (tint) {

            //TODO: add cases for samples based on their I

            case 520:
                return new Component(x1, y1, x2, y2, f, st);
            case 530:
                return new TEComponent(x1, y1, x2, y2, f, st);
            case 'c':
                return new CapacitorElm(x1, y1, x2, y2, f, st);
            case 'd':
                return new DiodeElm(x1, y1, x2, y2, f, st);
            case 'h':
                return new HeatSourceSinkElm(x1, y1, x2, y2, f, st);
            case 'e':
                return new RegulatorElm(x1, y1, x2, y2, f, st);
            case 'g':
                return new GroundElm(x1, y1, x2, y2, f, st);
            case 'r':
                return new ResistorElm(x1, y1, x2, y2, f, st);
            case 's':
                return new SwitchElm(x1, y1, x2, y2, f, st);
            case 'w':
                return new ConduitElm(x1, y1, x2, y2, f, st);
            case 700:
                return new TEHeatEngine(x1, y1, x2, y2, f, st);
            //Samples
            case 600:
                return new DiodeElm_SS_01(x1, y1);
            case 601:
                return new DiodeElm_SS_02(x1, y1);
            case 602:
                return new DiodeElm_F_01(x1, y1);
            case 603:
                return new DiodeElm_F_02(x1, y1);
            case 604:
                return new DiodeElm_T_01(x1, y1);
            case 610:
                return new SwitchElm_FM_01(x1, y1);
            case 611:
                return new SwitchElm_MM_01(x1, y1);
            case 612:
                return new SwitchElm_MM_02(x1, y1);
            case 613:
                return new SwitchElm_ME_01(x1, y1);
            case 614:
                return new SwitchElm_MM_03(x1, y1);
            case 615:
                return new SwitchElm_SSE_01(x1, y1);
            case 616:
                return new SwitchElm_SSM_01(x1, y1);
            case 620:
                return new RegulatorElm_F_01(x1, y1);

            //2D
            case 521:
                return new TwoDimComponent(x1, y1, x2, y2, f, st);
            case 522:
                return new ZigZagInterface(x1, y1, x2, y2, f, st);
            case 523:
                return new CircularInterface(x1, y1, x2, y2, f, st);


        }
        return null;
    }

    public static CircuitElm constructElement(String n, int x1, int y1) {
        switch (n) {

            case "Component":
                return new Component(x1, y1);
            case "TEComponent":
                return new Component(x1, y1);
            case "CapacitorElm":
                return new CapacitorElm(x1, y1);
            case "DiodeElm":
                return new DiodeElm(x1, y1);
            case "HeatSourceSinkElm":
                return new HeatSourceSinkElm(x1, y1);
            case "RegulatorElm":
                return new RegulatorElm(x1, y1);
            case "GroundElm":
                return new GroundElm(x1, y1);
            case "ResistorElm":
                return new ResistorElm(x1, y1);
            case "SwitchElm":
                return new SwitchElm(x1, y1);
            case "WireElm":
                return new ConduitElm(x1, y1);
            case "TEHeatEngine":
                return new TEHeatEngine(x1, y1);
            //Samples
            case "DiodeElm_SS_01":
                return new DiodeElm_SS_01(x1, y1);
            case "DiodeElm_SS_02":
                return new DiodeElm_SS_02(x1, y1);
            case "DiodeElm_F_01":
                return new DiodeElm_F_01(x1, y1);
            case "DiodeElm_F_02":
                return new DiodeElm_F_02(x1, y1);
            case "DiodeElm_T_01":
                return new DiodeElm_T_01(x1, y1);
            case "SwitchElm_FM_01":
                return new SwitchElm_FM_01(x1, y1);
            case "SwitchElm_MM_01":
                return new SwitchElm_MM_01(x1, y1);
            case "SwitchElm_MM_02":
                return new SwitchElm_MM_02(x1, y1);
            case "SwitchElm_MM_03":
                return new SwitchElm_MM_03(x1, y1);
            case "SwitchElm_ME_01":
                return new SwitchElm_ME_01(x1, y1);
            case "SwitchElm_SSE_01":
                return new SwitchElm_SSE_01(x1, y1);
            case "SwitchElm_SSM_01":
                return new SwitchElm_SSM_01(x1, y1);
            case "RegulatorElm_F_01":
                return new RegulatorElm_F_01(x1, y1);

            //2D
            case "2DComponent":
                return new TwoDimComponent(x1, y1);
            case "ZigZagInterface":
                return new ZigZagInterface(x1, y1);
            case "CircularInterface":
                return new CircularInterface(x1, y1);
            default:
                return null;
        }
    }


    native boolean weAreInUS(boolean orCanada) /*-{
        try {
            l = navigator.languages ? navigator.languages[0] : (navigator.language || navigator.userLanguage);
            if (l.length > 2) {
                l = l.slice(-2).toUpperCase();
                return (l == "US" || (l == "CA" && orCanada));
            } else {
                return 0;
            }

        } catch (e) {
            return 0;
        }
    }-*/;

    native boolean weAreInGermany() /*-{
        try {
            l = navigator.languages ? navigator.languages[0] : (navigator.language || navigator.userLanguage);
            return (l.toUpperCase().startsWith("DE"));
        } catch (e) {
            return 0;
        }
    }-*/;


    native void printCanvas(CanvasElement cv) /*-{
        var img = cv.toDataURL("image/png");
        var win = window.open("", "print", "height=500,width=500,status=yes,location=no");
        win.document.title = "Print Circuit";
        win.document.open();
        win.document.write('<img src="' + img + '"/>');
        win.document.close();
        setTimeout(function () {
            win.print();
        }, 1000);
    }-*/;


    void doPrint() {
        Canvas cv = getCircuitAsCanvas(CAC_PRINT);
        printCanvas(cv.getCanvasElement());
    }

    boolean loadedCanvas2SVG = false;

    boolean initializeSVGScriptIfNecessary(final String followupAction) {
        // load canvas2svg if we haven't already
        if (!loadedCanvas2SVG) {
            ScriptInjector.fromUrl("canvas2svg.js").setCallback(new Callback<Void, Exception>() {
                public void onFailure(Exception reason) {
                    Window.alert("Can't load canvas2svg.js.");
                }

                public void onSuccess(Void result) {
                    loadedCanvas2SVG = true;
                    if (followupAction.equals("doExportAsSVG")) {
                        doExportAsSVG();
                    } else if (followupAction.equals("doExportAsSVGFromAPI")) {
                        doExportAsSVGFromAPI();
                    }
                }
            }).inject();
            return false;
        }
        return true;
    }

    void doExportAsSVG() {
        if (!initializeSVGScriptIfNecessary("doExportAsSVG")) {
            return;
        }
        dialogShowing = new ExportAsImageDialog(CAC_SVG);
        dialogShowing.show();
    }

    public void doExportAsSVGFromAPI() {
        if (!initializeSVGScriptIfNecessary("doExportAsSVGFromAPI")) {
            return;
        }
        String svg = getCircuitAsSVG();
        callSVGRenderedHook(svg);
    }

    static final int CAC_PRINT = 0;
    static final int CAC_IMAGE = 1;
    static final int CAC_SVG = 2;

    public Canvas getCircuitAsCanvas(int type) {
        // create canvas to draw circuit into
        Canvas cv = Canvas.createIfSupported();
        Rectangle bounds = getCircuitBounds();

        // add some space on edges because bounds calculation is not perfect
        int wmargin = 140;
        int hmargin = 100;
        int w = (bounds.width * 2 + wmargin);
        int h = (bounds.height * 2 + hmargin);
        cv.setCoordinateSpaceWidth(w);
        cv.setCoordinateSpaceHeight(h);

        Context2d context = cv.getContext2d();
        drawCircuitInContext(context, type, bounds, w, h);
        return cv;
    }

    // create SVG context using canvas2svg
    native static Context2d createSVGContext(int w, int h) /*-{
        return new C2S(w, h);
    }-*/;

    native static String getSerializedSVG(Context2d context) /*-{
        return context.getSerializedSvg();
    }-*/;

    public String getCircuitAsSVG() {
        Rectangle bounds = getCircuitBounds();

        // add some space on edges because bounds calculation is not perfect
        int wmargin = 140;
        int hmargin = 100;
        int w = (bounds.width + wmargin);
        int h = (bounds.height + hmargin);
        Context2d context = createSVGContext(w, h);
        drawCircuitInContext(context, CAC_SVG, bounds, w, h);
        return getSerializedSVG(context);
    }

    void drawCircuitInContext(Context2d context, int type, Rectangle bounds, int w, int h) {
        Graphics g = new Graphics(context);
        context.setTransform(1, 0, 0, 1, 0, 0);
        double oldTransform[] = Arrays.copyOf(transform, 6);

        double scale = 1;

        // turn on white background, turn off current display
        boolean print = (type == CAC_PRINT);
        if (print) {
            g.setColor(Color.white);
        } else {
            g.setColor(Color.black);
            g.fillRect(0, 0, w, h);
        }

        int wmargin = 140;
        int hmargin = 100;
        if (bounds != null)
            scale = Math.min(w / (double) (bounds.width + wmargin), h / (double) (bounds.height + hmargin));

        // ScopeElms need the transform array to be updated
        transform[0] = transform[3] = scale;
        transform[4] = -(bounds.x - wmargin / 2);
        transform[5] = -(bounds.y - hmargin / 2);
        context.scale(scale, scale);
        context.translate(transform[4], transform[5]);
        context.setLineCap(Context2d.LineCap.ROUND);

        // draw elements
        for (CircuitElm elm : elmList) {
            elm.draw(g);
            if (elm.needsHighlight()) elm.drawPosts(g);
        }
        for (CircuitElm elm : elmList) {
            if (!elm.needsHighlight()) elm.drawPosts(g);
        }

        // restore everything
        transform = oldTransform;
    }


    native JsArray<JavaScriptObject> getJSArray() /*-{
        return [];
    }-*/;

    JsArray<JavaScriptObject> getJSElements() {
        int i;
        JsArray<JavaScriptObject> arr = getJSArray();
        for (i = 0; i != elmList.size(); i++) {
            CircuitElm ce = getElm(i);
            ce.addJSMethods();
            arr.push(ce.getJavaScriptObject());
        }
        return arr;
    }

    native void setupJSInterface() /*-{
        var that = this;
        $wnd.CircuitJS1 = {
            setSimRunning: $entry(function (run) {
                that.@lahde.tccbuilder.client.CirSim::setSimRunning(Z)(run);
            }),
            getTime: $entry(function () {
                return that.@lahde.tccbuilder.client.CirSim::t;
            }),
            getTimeStep: $entry(function () {
                return that.@lahde.tccbuilder.client.CirSim::timeStep;
            }),
            setTimeStep: $entry(function (ts) {
                that.@lahde.tccbuilder.client.CirSim::timeStep = ts;
            }),
            isRunning: $entry(function () {
                return that.@lahde.tccbuilder.client.CirSim::simIsRunning()();
            }),
            getElements: $entry(function () {
                return that.@lahde.tccbuilder.client.CirSim::getJSElements()();
            }),
            getCircuitAsSVG: $entry(function () {
                return that.@lahde.tccbuilder.client.CirSim::doExportAsSVGFromAPI()();
            }),
            exportCircuit: $entry(function () {
                return that.@lahde.tccbuilder.client.CirSim::dumpCircuit()();
            }),
            importCircuit: $entry(function (circuit, subcircuitsOnly) {
                return that.@lahde.tccbuilder.client.CirSim::importCircuitFromText(Ljava/lang/String;Z)(circuit, subcircuitsOnly);
            })
        };
        var hook = $wnd.oncircuitjsloaded;
        if (hook)
            hook($wnd.CircuitJS1);
    }-*/;

    native void callUpdateHook() /*-{
        var hook = $wnd.CircuitJS1.onupdate;
        if (hook)
            hook($wnd.CircuitJS1);
    }-*/;

    native void callAnalyzeHook() /*-{
        var hook = $wnd.CircuitJS1.onanalyze;
        if (hook)
            hook($wnd.CircuitJS1);
    }-*/;

    native void callTimeStepHook() /*-{
        var hook = $wnd.CircuitJS1.ontimestep;
        if (hook)
            hook($wnd.CircuitJS1);
    }-*/;

    native void callSVGRenderedHook(String svgData) /*-{
        var hook = $wnd.CircuitJS1.onsvgrendered;
        if (hook)
            hook($wnd.CircuitJS1, svgData);
    }-*/;

    class UndoItem {
        public String dump;
        public double scale, transform4, transform5;

        UndoItem(String d) {
            dump = d;
            scale = transform[0];
            transform4 = transform[4];
            transform5 = transform[5];
        }
    }

}