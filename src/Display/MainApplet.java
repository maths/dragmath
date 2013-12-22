/*
Copyright (C) 2010 Alex Billingsley, email@alexbillingsley.co.uk
www.dragmath.bham.ac.uk
 
 This file is part of DragMath.
 
    DragMath is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
 
    DragMath is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with DragMath. If not, see <http://www.gnu.org/licenses/>.
 
 */


package Display;


import Output.OutputFormat;
import Tree.BuildTree;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import javax.swing.border.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.util.Stack;
import java.io.*;

import org.jdom.*;
import org.jdom.input.*;

/**
 * Class to display applet and control all GUI interactions
 * @author Alex Billingsley
 */
public class MainApplet extends javax.swing.JApplet {
    
    private AddComponent addComponent;
    private OutputFormat output;
    private BuildTree buildTree;
    private StatusBar statusBar;
    
    private MseSelectListener mouseSelectListener;
    private MseMotionSelectListener motionSelectListener;
    
    private boolean dragging;
    private InputComponent newComponent;
    private InputComponent[] inputComponents;
    
    private JPanel selectionObjects;
    private int firstLocation;
    private JPanel selectionLayer;
    
    private SAXBuilder builder;
    
    private Document componentFile;
    private Element inpComps;
    
    private java.net.URL appletCodeBase;
    private java.applet.AppletContext appletContext;
    private String language;
    private String appletPath;
    private String outputFormat;
    private String openWithExpression;
    private String openWithObject;
    private boolean implicitMult;
    
    private LanguageManager langMan;
    
    private WSHelper xstream = new WSHelper();
    
    /** Initializes the applet MainApplet
     * Initialises all fields
     */
    public void init() {
        try {
            java.awt.EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    
                    // Set output format parameter
                    outputFormat = getParameter("outputFormat");
                    
                    // Open with object parameter
                    openWithObject = getParameter("openWithObject");
                    
                    // Open with expression parameter
                    openWithExpression = getParameter("openWithExpression");
                    
                    // Set language parameter
                    language = getParameter("language");
                    
                    // Set hide menu parameter
                    String hideMenu = getParameter("hideMenu");
                    
                    // Set hide menu parameter
                    String hideButtons = getParameter("hideToolbar");
                    
                    // Set custom toolbar
                    String customToolBar = getParameter("customToolBar");
                    
                    String implicitMultStr = getParameter("implicitMultiplication");
                    
                    String keepDoubleNumbersStr = getParameter("displayDecimalNumbers");
                    
                    if (language == null) {
                        language = "en";
                    }
                    if (outputFormat == null) {
                        outputFormat = "Latex";
                    }
                    
                    implicitMult = false;
                    if (implicitMultStr != null && (implicitMultStr.equals("True") || implicitMultStr.equals("true"))) {
                        implicitMult = true;
                    }
                    
                    boolean keepDoubleNumbers = false;
                    if (keepDoubleNumbersStr != null && (keepDoubleNumbersStr.equals("True") || keepDoubleNumbersStr.equals("true"))) {
                        keepDoubleNumbers = true;
                    }
                    
                    boolean hideDropDownMenu = false;
                    if (hideMenu != null && (hideMenu.equals("True") || hideMenu.equals("true"))) {
                        hideDropDownMenu = true;
                    }
                    
                    boolean hideToolbar = false;
                    if (hideButtons != null && (hideButtons.equals("True") || hideButtons.equals("true"))) {
                        hideToolbar = true;
                    }
                    if (customToolBar == null){
                        customToolBar = "0 1 2 | 3 4 | 5 6 7 | 8";
                    }
                    
                    appletCodeBase = getCodeBase();
                    appletContext = getAppletContext();
                    
                    initComponents();
                    
                    statusBar = new StatusBar(jLabelStatus);
                    langMan = new LanguageManager(appletCodeBase, statusBar);
                    loadConfigFile();
                    langMan.loadLanguageFile(language);
                    
                    inputComponents = new InputComponent[100];
                    
                    addPaletteToolbarListeners(jTabbedPaneInput.getComponents());
                    addExtraComponents();
                    
                    // Perform custom toolbar code
                    parseCustomToolbarParam(customToolBar);
                    
                    addCommandToolbarListeners(jToolBarEdit.getComponents());
                    
                    output = new OutputFormat(statusBar, langMan, appletCodeBase, implicitMult, keepDoubleNumbers);
                    buildTree = new BuildTree(langMan, inpComps);
                    output.readFormatFile(outputFormat);
                    
                    MouseListener textBoxListener = new MouseListenerTextBox();
                    addComponent = new AddComponent(inputComponents, jPanelWorkspace, buildTree, textBoxListener, statusBar, langMan, implicitMult, keepDoubleNumbers);
                    
                    // Set tabs icons
                    jTabbedPaneInput.setTitleAt(0, "");
                    jTabbedPaneInput.setIconAt(0, new javax.swing.ImageIcon(getClass().getResource("/Display/Images/operators.gif")));
                    jTabbedPaneInput.setTitleAt(1, "");
                    jTabbedPaneInput.setIconAt(1, new javax.swing.ImageIcon(getClass().getResource("/Display/Images/layout.gif")));
                    jTabbedPaneInput.setTitleAt(2, "");
                    jTabbedPaneInput.setIconAt(2, new javax.swing.ImageIcon(getClass().getResource("/Display/Images/fences.gif")));
                    jTabbedPaneInput.setTitleAt(3, "");
                    jTabbedPaneInput.setIconAt(3, new javax.swing.ImageIcon(getClass().getResource("/Display/Images/sin.gif")));
                    jTabbedPaneInput.setTitleAt(4, "");
                    jTabbedPaneInput.setIconAt(4, new javax.swing.ImageIcon(getClass().getResource("/Display/Images/calculus.gif")));
                    jTabbedPaneInput.setTitleAt(5, "");
                    jTabbedPaneInput.setIconAt(5, new javax.swing.ImageIcon(getClass().getResource("/Display/Images/greek.gif")));
                    jTabbedPaneInput.setTitleAt(6, "");
                    jTabbedPaneInput.setIconAt(6, new javax.swing.ImageIcon(getClass().getResource("/Display/Images/arrows.gif")));
                    
                    dragging=false;
                    
                    mouseSelectListener = new MseSelectListener();
                    motionSelectListener = new MseMotionSelectListener(jPanelWorkspace, mouseSelectListener);
                    
                    jPanelWorkspace.addMouseListener(mouseSelectListener);
                    jPanelWorkspace.addMouseMotionListener(motionSelectListener);
                    
                    // class listens for mouse clicks on the workspace area.
                    jPanelWorkspace.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent evt) {
                            if (evt.getClickCount() == 2) {
                                Point mousePos = evt.getPoint();
                                if (mousePos != null) {
                                    // Find the component where the mouse has been clicked
                                    JComponent component = (JComponent)jPanelWorkspace.findComponentAt(mousePos);
                                    motionSelectListener.clickSelect(component);
                                }
                            } else {
                                jPanelWorkspace.requestFocus();
                                addComponent(false, SwingUtilities.convertPoint(evt.getComponent(), evt.getPoint(), jPanelWorkspace));
                            }
                        }
                    });
                    
                    jPanelWorkspace.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 5));
                    jPanelWorkspace.requestFocus();
                    loadMenuText();
                    hideMenus(hideDropDownMenu, hideToolbar);
                    System.out.println("DragMath v0.7.9 successfully loaded");
                    
                    // Set implicit mult in menu
                    jCheckBoxImplicitMult.setSelected(implicitMult);
                    
                    if (openWithExpression != null) {
                        openWithExpression(openWithExpression);
                    } else //added by zmn to load from object
                    {
                        if(openWithObject != null) {
                            setFile(openWithObject);
                        }
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
    
    public void hideMenus(boolean hideMenu, boolean hideToolbar) {
        
        if (hideMenu) {
            jMenuBar.setVisible(false);
        }
        if (hideToolbar) {
            jPanelToolbar.setVisible(false);
        }
        jToolBarEdit2.setVisible(false);
    }
    
    public void parseCustomToolbarParam(String toolbarParam) {
        
        String curNumber = "";
        
        for (int i=0; i < toolbarParam.length(); i++) {
            
            char nextChar = toolbarParam.charAt(i);
            
            if (nextChar == '|') {
                
                if (curNumber != "") {
                    // Add button for current number
                    addButtonForNumber(curNumber);
                    curNumber = "";
                }
                // Add separator
                JSeparator jsept = new JSeparator(SwingConstants.VERTICAL);
                jsept.setMaximumSize(new Dimension(2, 20));
                jToolBarEdit.add(jsept);
                
                
            } else if (nextChar == ' ') {
                // Space
                if (curNumber != "") {
                    // Add button for current number
                    addButtonForNumber(curNumber);
                    curNumber = "";
                }
            } else {
                curNumber = curNumber + nextChar;
            }
        }
        
        if (curNumber != "") {
            // Add button for current number
            addButtonForNumber(curNumber);
            curNumber = "";
        }
    }
    
    public void addButtonForNumber(String number) {
        try {
            
            int bttnNum = Integer.parseInt(number);
            
            if (bttnNum == 0)
                jToolBarEdit.add(jButtonClear);
            
            if (bttnNum == 1)
                jToolBarEdit.add(jButtonLoad);
            
            if (bttnNum == 2)
                jToolBarEdit.add(jButtonSave);
            
            if (bttnNum == 3)
                jToolBarEdit.add(jButtonUndo);
            
            if (bttnNum == 4)
                jToolBarEdit.add(jButtonRedo);
            
            if (bttnNum == 5)
                jToolBarEdit.add(jButtonCut);
            
            if (bttnNum == 6)
                jToolBarEdit.add(jButtonCopy);
            
            if (bttnNum == 7)
                jToolBarEdit.add(jButtonPaste);
            
            if (bttnNum == 8)
                jToolBarEdit.add(jButtonExport);
            
        } catch (NumberFormatException ex) {
            // Not a parseable number
        }
    }
    
    public void openWithExpression(String expression) {
        Stack outputStack;
        try {
            outputStack = BuildTree.parseString(expression, new Stack());
            if (outputStack.size() > 1) {
                Tree.MathObject tree = (Tree.MathObject)outputStack.pop();
                BuildTree.toTree(tree, outputStack);
                addComponent.pasteTree(jPanelWorkspace, 0, tree, 0);
                System.out.println("Expression loaded");
            }
        } catch (org.nfunk.jep.ParseException ex) {
            TextBox newBox = addComponent.createBox(false);
            jPanelWorkspace.add(newBox);
            newBox.requestFocusInWindow();
            newBox.setText(expression);
            jPanelWorkspace.revalidate();
            statusBar.println(langMan.readLangFile("ParseExp"));
        }
    }
    
    public String getFile() {
        String retstr = "";
        try {
            Tree.MathObject mobj =
                    buildTree.generateTree(jPanelWorkspace, false, 0, 0);
            retstr = xstream.OToS(mobj);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(),
                    "DragMath", JOptionPane.ERROR_MESSAGE);
        }
        return retstr;
    }
    
    public void setFile(String mathstr) {
        Tree.MathObject tree = null;
        try {
            
            tree = (Tree.MathObject) xstream.SToO(mathstr);
            jPanelWorkspace.removeAll();
            addComponent.pasteTree(jPanelWorkspace, 0, tree, 0);
            addComponent.resetUndoRedo();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.toString(), "DragMath",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    public void loadConfigFile() {
        builder = new SAXBuilder();
        try{
            componentFile = builder.build(this.getClass().getResourceAsStream("/Display/CompConfig.xml"));
            inpComps = componentFile.getRootElement();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error loading internal component file - please correct", "DragMath", JOptionPane.ERROR_MESSAGE);
        } catch (JDOMException ex) {
            JOptionPane.showMessageDialog(null, "Error loading internal component file - please correct", "DragMath", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Adds a MouseListener to each JButton in each toolbar in the palette, and creates a JLabel for each JButton,
     * and stores it in the <code>labels</code> array.
     * @param components the array of toolbars, each containing JButtons
     */
    public void addPaletteToolbarListeners(Component[] components) {
        int i=0;
        
        while ( i < components.length) {
            if (components[i].getClass().getName().equals("javax.swing.JPanel")) {
                JPanel temp = (JPanel)components[i];
                addPaletteToolbarListeners(temp.getComponents());
            }
            if (components[i].getClass().getName().equals("javax.swing.JToolBar")) {
                JToolBar temp = (JToolBar)components[i];
                addPaletteToolbarListeners(temp.getComponents());
            }
            if (components[i].getClass().getName().equals("javax.swing.JButton")) {
                JButton button = (JButton)components[i];
                addToComponentArray(button.getName());
                components[i].addMouseListener(new MouseListenerPaletteToolbar());
            }
            i++;
        }
    }
    
    /** Adds a MouseListener to each JButton in the commands toolbar
     *@param components the array of components on the toolbar
     */
    public void addCommandToolbarListeners(Component[] components) {
        int i=0;
        while ( i < components.length) {
            if (components[i].getClass().getName().equals("javax.swing.JButton") ||
                    components[i].getClass().getName().equals("javax.swing.JCheckBox")) {
                
                //components[i].setName(langMan.readLangFile(components[i].getName()));
                components[i].addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        jLabelTooltip.setText(langMan.readLangFile(evt.getComponent().getName()));
                    }
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        jLabelTooltip.setText("");
                    }
                });
                
            }
            i++;
        }
    }
    
    public void addToComponentArray(String name) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Element comp = inpComps.getChild(addComponent.getName(name));
        
        BufferedImage originalImage = null;
        Cursor newCursor = null;
        if (comp.getAttributeValue("iconFileName").equals("null")) {
        } else {
            try {
                originalImage = ImageIO.read(getClass().getResource("/Display/Images/" + comp.getAttributeValue("iconFileName")));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            
            int w = originalImage.getWidth(this);
            int h = originalImage.getHeight(this);
            int bestW = toolkit.getBestCursorSize(w, h).width;
            int bestH = toolkit.getBestCursorSize(w, h).height;
            
            BufferedImage resizedImage = new BufferedImage(bestW,bestH,BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = resizedImage.createGraphics();
            g.drawImage(originalImage,0,0,null);
            g.dispose();
            Point centerPoint = new Point(w/2,h/2);
            newCursor = toolkit.createCustomCursor(resizedImage, centerPoint, "Cursor");
        }
        
        inputComponents[addComponent.getID(name)] = new InputComponent(Integer.parseInt(comp.getAttributeValue("ID")),
                Integer.parseInt(comp.getAttributeValue("group")),
                comp.getText(),
                langMan.readLangFile(addComponent.getName(name)),
                newCursor, name,
                originalImage);
    }
    
    
    public void addExtraComponents() {
        addToComponentArray("24-ArcSine");
        addToComponentArray("25-ArcCosine");
        addToComponentArray("26-ArcTangent");
        addToComponentArray("39-SineH");
        addToComponentArray("40-CosineH");
        addToComponentArray("41-TanH");
        addToComponentArray("42-ArcSineH");
        addToComponentArray("43-ArcCosineH");
        addToComponentArray("44-ArcTanH");
        
        InputComponent unaryMinus = new InputComponent(30,3,"-", "Unary Minus", null, "30-UMinus", null);
        inputComponents[30]= unaryMinus;
    }
    
    
    public String getMathExpression() {
        String expression = "Failed to get expression";
        try {
            expression = output.outputToClipboard(buildTree.generateTree(jPanelWorkspace, false, 0, 0));
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(null, langMan.readLangFile(output.getOutputFormat()) + ex.getMessage(), "DragMath", JOptionPane.ERROR_MESSAGE);
        }
        return expression;
    }
    
    public String getMathExpressionForURL() {
        String expression = "Failed to get expression";
        try {
            expression = output.outputToClipboard(buildTree.generateTree(jPanelWorkspace, false, 0, 0));
            try {
                expression = java.net.URLEncoder.encode(expression, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                //ex.printStackTrace();
            }
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(null, langMan.readLangFile(output.getOutputFormat()) + ex.getMessage(), "DragMath", JOptionPane.ERROR_MESSAGE);
        }
        return expression;
    }
    
    public void loadMenuText() {
        jMenuFile.setText(langMan.readLangFile("FileMenu"));
        jMenuItemOpen.setText(langMan.readLangFile("OpenMenu"));
        jMenuItemSaveAs.setText(langMan.readLangFile("SaveAsMenu"));
        jMenuItemExport.setText(langMan.readLangFile("ExportMenu"));
        jMenuItemExportToImage.setText(langMan.readLangFile("ImageMenu"));
        jMenuEdit.setText(langMan.readLangFile("EditMenu"));
        jMenuItemUndo.setText(langMan.readLangFile("UndoMenu"));
        jMenuItemRedo.setText(langMan.readLangFile("RedoMenu"));
        jMenuItemCut.setText(langMan.readLangFile("CutMenu"));
        jMenuItemCopy.setText(langMan.readLangFile("CopyMenu"));
        jMenuItemPaste.setText(langMan.readLangFile("PasteMenu"));
        jMenuItemClear.setText(langMan.readLangFile("ClearMenu"));
        jMenuItemSelectAll.setText(langMan.readLangFile("SelectMenu"));
        jMenuOptions.setText(langMan.readLangFile("OptionsMenu"));
        jMenuItemSetExport.setText(langMan.readLangFile("SetExportMenu"));
        jMenuItemSetLang.setText(langMan.readLangFile("SetLanguageMenu"));
        jMenuHelp.setText(langMan.readLangFile("HelpMenu"));
        jMenuDebug.setText(langMan.readLangFile("DebugMenu"));
        jMenuItemShowTree.setText(langMan.readLangFile("TreeMenu"));
        jCheckBoxMenuItemShowOutline.setText(langMan.readLangFile("OutlineMenu"));
        jMenuItemOnlineHelp.setText(langMan.readLangFile("OnlineHelpMenu"));
        jMenuItemAbout.setText(langMan.readLangFile("AboutMenu"));
        jCheckBoxImplicitMult.setText(langMan.readLangFile("MultMenu"));
    }
    
    
    /** This method is called from within the init() method to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroupExportLang = new javax.swing.ButtonGroup();
        buttonGroupLang = new javax.swing.ButtonGroup();
        jLabelStatus = new javax.swing.JLabel();
        jLabelTooltip = new javax.swing.JLabel();
        jPanelAppMain = new javax.swing.JPanel();
        jPanelToolbar = new javax.swing.JPanel();
        jToolBarEdit = new javax.swing.JToolBar();
        jPanelMain = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanelWorkspace = new javax.swing.JPanel();
        jTabbedPaneInput = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        jButton11 = new javax.swing.JButton();
        jButton91 = new javax.swing.JButton();
        jButton92 = new javax.swing.JButton();
        jButton26 = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jButton29 = new javax.swing.JButton();
        jButton32 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton24 = new javax.swing.JButton();
        jButton25 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton90 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton93 = new javax.swing.JButton();
        jButton94 = new javax.swing.JButton();
        jButton65 = new javax.swing.JButton();
        jToolBar10 = new javax.swing.JToolBar();
        jButton101 = new javax.swing.JButton();
        jButton85 = new javax.swing.JButton();
        jButton86 = new javax.swing.JButton();
        jButton87 = new javax.swing.JButton();
        jButton88 = new javax.swing.JButton();
        jButton89 = new javax.swing.JButton();
        jButton100 = new javax.swing.JButton();
        jButton102 = new javax.swing.JButton();
        jButton103 = new javax.swing.JButton();
        jButton104 = new javax.swing.JButton();
        jButton108 = new javax.swing.JButton();
        jButton105 = new javax.swing.JButton();
        jButton106 = new javax.swing.JButton();
        jButton107 = new javax.swing.JButton();
        jToolBar3 = new javax.swing.JToolBar();
        jButton19 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        jButton23 = new javax.swing.JButton();
        jButtonMatrix1 = new javax.swing.JButton();
        jButton77 = new javax.swing.JButton();
        jButtonMatrix = new javax.swing.JButton();
        jButtonMatrix2 = new javax.swing.JButton();
        jButtonMatrix3 = new javax.swing.JButton();
        jToolBar4 = new javax.swing.JToolBar();
        jButton15 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jButton34 = new javax.swing.JButton();
        jButton33 = new javax.swing.JButton();
        jButton73 = new javax.swing.JButton();
        jButton74 = new javax.swing.JButton();
        jToolBar6 = new javax.swing.JToolBar();
        jButtonSin = new javax.swing.JButton();
        jButtonCos = new javax.swing.JButton();
        jButtonTan = new javax.swing.JButton();
        jCheckBoxInverse = new javax.swing.JCheckBox();
        jCheckBoxHyp = new javax.swing.JCheckBox();
        jButton30 = new javax.swing.JButton();
        jButton31 = new javax.swing.JButton();
        jButton36 = new javax.swing.JButton();
        jToolBar8 = new javax.swing.JToolBar();
        jButton21 = new javax.swing.JButton();
        jButton75 = new javax.swing.JButton();
        jButton37 = new javax.swing.JButton();
        jButton84 = new javax.swing.JButton();
        jButton76 = new javax.swing.JButton();
        jButtonMatrix5 = new javax.swing.JButton();
        jButtonMatrix4 = new javax.swing.JButton();
        jButton35 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jToolBar7 = new javax.swing.JToolBar();
        jButton38 = new javax.swing.JButton();
        jButton39 = new javax.swing.JButton();
        jButton40 = new javax.swing.JButton();
        jButton41 = new javax.swing.JButton();
        jButton42 = new javax.swing.JButton();
        jButton43 = new javax.swing.JButton();
        jButton44 = new javax.swing.JButton();
        jButton45 = new javax.swing.JButton();
        jButton46 = new javax.swing.JButton();
        jButton47 = new javax.swing.JButton();
        jToolBar2 = new javax.swing.JToolBar();
        jButton48 = new javax.swing.JButton();
        jButton49 = new javax.swing.JButton();
        jButton50 = new javax.swing.JButton();
        jButton51 = new javax.swing.JButton();
        jButton52 = new javax.swing.JButton();
        jButton53 = new javax.swing.JButton();
        jButton54 = new javax.swing.JButton();
        jButton55 = new javax.swing.JButton();
        jButton56 = new javax.swing.JButton();
        jButton57 = new javax.swing.JButton();
        jButton58 = new javax.swing.JButton();
        jButton59 = new javax.swing.JButton();
        jButton60 = new javax.swing.JButton();
        jButton61 = new javax.swing.JButton();
        jButton62 = new javax.swing.JButton();
        jButton63 = new javax.swing.JButton();
        jButton64 = new javax.swing.JButton();
        jButton66 = new javax.swing.JButton();
        jButton67 = new javax.swing.JButton();
        jButton68 = new javax.swing.JButton();
        jButton69 = new javax.swing.JButton();
        jButton70 = new javax.swing.JButton();
        jButton71 = new javax.swing.JButton();
        jButton72 = new javax.swing.JButton();
        jToolBar5 = new javax.swing.JToolBar();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jButton27 = new javax.swing.JButton();
        jButton28 = new javax.swing.JButton();
        jButton78 = new javax.swing.JButton();
        jButton79 = new javax.swing.JButton();
        jButton80 = new javax.swing.JButton();
        jButton81 = new javax.swing.JButton();
        jButton82 = new javax.swing.JButton();
        jButton83 = new javax.swing.JButton();
        jToolBarEdit2 = new javax.swing.JToolBar();
        jButtonClear = new javax.swing.JButton();
        jButtonLoad = new javax.swing.JButton();
        jButtonSave = new javax.swing.JButton();
        jButtonUndo = new javax.swing.JButton();
        jButtonRedo = new javax.swing.JButton();
        jButtonCut = new javax.swing.JButton();
        jButtonCopy = new javax.swing.JButton();
        jButtonPaste = new javax.swing.JButton();
        jButtonExport = new javax.swing.JButton();
        jMenuBar = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuItemOpen = new javax.swing.JMenuItem();
        jMenuItemSaveAs = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JSeparator();
        jMenuItemExport = new javax.swing.JMenuItem();
        jMenuItemExportToImage = new javax.swing.JMenuItem();
        jMenuEdit = new javax.swing.JMenu();
        jMenuItemUndo = new javax.swing.JMenuItem();
        jMenuItemRedo = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItemCut = new javax.swing.JMenuItem();
        jMenuItemCopy = new javax.swing.JMenuItem();
        jMenuItemPaste = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuItemClear = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jMenuItemSelectAll = new javax.swing.JMenuItem();
        jMenuOptions = new javax.swing.JMenu();
        jMenuItemSetExport = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JSeparator();
        jMenuItemSetLang = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        jCheckBoxImplicitMult = new javax.swing.JCheckBoxMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuDebug = new javax.swing.JMenu();
        jMenuItemShowTree = new javax.swing.JMenuItem();
        jCheckBoxMenuItemShowOutline = new javax.swing.JCheckBoxMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        jMenuItemOnlineHelp = new javax.swing.JMenuItem();
        jMenuItemAbout = new javax.swing.JMenuItem();

        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        setForeground(new java.awt.Color(255, 255, 255));
        setStub(null);
        jLabelStatus.setFont(new java.awt.Font("Arial", 0, 12));
        jLabelStatus.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(jLabelStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(270, 280, 270, 30));

        jLabelTooltip.setFont(new java.awt.Font("Arial", 0, 12));
        jLabelTooltip.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(jLabelTooltip, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 280, 270, 30));

        jPanelAppMain.setLayout(new java.awt.BorderLayout());

        jPanelToolbar.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jToolBarEdit.setBackground(new java.awt.Color(255, 255, 255));
        jToolBarEdit.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        jToolBarEdit.setFloatable(false);
        jPanelToolbar.add(jToolBarEdit, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 540, 30));

        jPanelAppMain.add(jPanelToolbar, java.awt.BorderLayout.NORTH);

        jPanelMain.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(javax.swing.BorderFactory.createCompoundBorder());
        jPanelWorkspace.setBackground(new java.awt.Color(255, 255, 255));
        jPanelWorkspace.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelWorkspace.addContainerListener(new java.awt.event.ContainerAdapter() {
            public void componentAdded(java.awt.event.ContainerEvent evt) {
                jPanelWorkspaceComponentAdded(evt);
            }
        });
        jPanelWorkspace.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jPanelWorkspaceKeyPressed(evt);
            }
        });

        jScrollPane1.setViewportView(jPanelWorkspace);

        jPanelMain.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jTabbedPaneInput.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        jPanel3.setLayout(new java.awt.GridBagLayout());

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jToolBar1.setBackground(new java.awt.Color(255, 255, 255));
        jToolBar1.setBorder(null);
        jToolBar1.setFloatable(false);
        jButton11.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton11.setText("+");
        jButton11.setToolTipText("");
        jButton11.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton11.setFocusable(false);
        jButton11.setName("2-Add");
        jButton11.setOpaque(false);
        jToolBar1.add(jButton11);

        jButton91.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton91.setText("\u00d7");
        jButton91.setToolTipText("");
        jButton91.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton91.setFocusable(false);
        jButton91.setName("72-Multiply2");
        jButton91.setOpaque(false);
        jToolBar1.add(jButton91);

        jButton92.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton92.setText("\u00f7");
        jButton92.setToolTipText("");
        jButton92.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton92.setFocusable(false);
        jButton92.setName("73-Divide2");
        jButton92.setOpaque(false);
        jToolBar1.add(jButton92);

        jButton26.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton26.setText("\u2212");
        jButton26.setToolTipText("");
        jButton26.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton26.setFocusable(false);
        jButton26.setName("3-Subtract");
        jButton26.setOpaque(false);
        jToolBar1.add(jButton26);

        jButton20.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton20.setText("\u00b7");
        jButton20.setToolTipText("");
        jButton20.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton20.setFocusable(false);
        jButton20.setName("0-Multiply");
        jButton20.setOpaque(false);
        jToolBar1.add(jButton20);

        jButton29.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton29.setText("\u00b1");
        jButton29.setToolTipText("");
        jButton29.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton29.setFocusable(false);
        jButton29.setName("58-PlusMinus");
        jButton29.setOpaque(false);
        jToolBar1.add(jButton29);

        jButton32.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton32.setText(",");
        jButton32.setToolTipText("");
        jButton32.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton32.setFocusable(false);
        jButton32.setName("4-Comma");
        jButton32.setOpaque(false);
        jToolBar1.add(jButton32);

        jButton5.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton5.setText("<");
        jButton5.setToolTipText("");
        jButton5.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton5.setFocusable(false);
        jButton5.setName("10-LessThan");
        jButton5.setOpaque(false);
        jToolBar1.add(jButton5);

        jButton6.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton6.setText(">");
        jButton6.setToolTipText("");
        jButton6.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton6.setFocusable(false);
        jButton6.setName("11-GreaterThan");
        jButton6.setOpaque(false);
        jToolBar1.add(jButton6);

        jButton24.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton24.setText("\u2264");
        jButton24.setToolTipText("");
        jButton24.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton24.setFocusable(false);
        jButton24.setName("12-LTEQ");
        jButton24.setOpaque(false);
        jToolBar1.add(jButton24);

        jButton25.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton25.setText("\u2265");
        jButton25.setToolTipText("");
        jButton25.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton25.setFocusable(false);
        jButton25.setName("13-GTEQ");
        jButton25.setOpaque(false);
        jToolBar1.add(jButton25);

        jButton7.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton7.setText("=");
        jButton7.setToolTipText("");
        jButton7.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton7.setFocusable(false);
        jButton7.setName("14-Equals");
        jButton7.setOpaque(false);
        jToolBar1.add(jButton7);

        jButton90.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton90.setText("\u2260");
        jButton90.setToolTipText("");
        jButton90.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton90.setFocusable(false);
        jButton90.setName("59-NotEqual");
        jButton90.setOpaque(false);
        jToolBar1.add(jButton90);

        jButton12.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton12.setText(":=");
        jButton12.setToolTipText("");
        jButton12.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton12.setFocusable(false);
        jButton12.setName("49-Assignment");
        jButton12.setOpaque(false);
        jToolBar1.add(jButton12);

        jButton93.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton93.setText("\u2261");
        jButton93.setToolTipText("");
        jButton93.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton93.setFocusable(false);
        jButton93.setName("74-Equiv");
        jButton93.setOpaque(false);
        jToolBar1.add(jButton93);

        jButton94.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton94.setText("\u2243");
        jButton94.setToolTipText("");
        jButton94.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton94.setFocusable(false);
        jButton94.setName("80-SimEq");
        jButton94.setOpaque(false);
        jToolBar1.add(jButton94);

        jButton65.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton65.setText("!");
        jButton65.setToolTipText("");
        jButton65.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton65.setFocusable(false);
        jButton65.setName("37-Factorial");
        jButton65.setOpaque(false);
        jToolBar1.add(jButton65);

        jPanel3.add(jToolBar1, new java.awt.GridBagConstraints());

        jToolBar10.setBackground(new java.awt.Color(255, 255, 255));
        jToolBar10.setBorder(null);
        jToolBar10.setFloatable(false);
        jButton101.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton101.setText("\u222a");
        jButton101.setToolTipText("");
        jButton101.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton101.setFocusable(false);
        jButton101.setName("15-Union");
        jButton101.setOpaque(false);
        jToolBar10.add(jButton101);

        jButton85.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton85.setText("\u2229");
        jButton85.setToolTipText("");
        jButton85.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton85.setFocusable(false);
        jButton85.setName("16-Intersection");
        jButton85.setOpaque(false);
        jToolBar10.add(jButton85);

        jButton86.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton86.setText("\u2282");
        jButton86.setToolTipText("");
        jButton86.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton86.setFocusable(false);
        jButton86.setName("17-Subset");
        jButton86.setOpaque(false);
        jToolBar10.add(jButton86);

        jButton87.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton87.setText("\u2286");
        jButton87.setToolTipText("");
        jButton87.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton87.setFocusable(false);
        jButton87.setName("18-SubsetEq");
        jButton87.setOpaque(false);
        jToolBar10.add(jButton87);

        jButton88.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton88.setText("\u2284");
        jButton88.setToolTipText("");
        jButton88.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton88.setFocusable(false);
        jButton88.setName("19-NSubset");
        jButton88.setOpaque(false);
        jToolBar10.add(jButton88);

        jButton89.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton89.setText("\u2288");
        jButton89.setToolTipText("");
        jButton89.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton89.setFocusable(false);
        jButton89.setName("20-NSubsetEq");
        jButton89.setOpaque(false);
        jToolBar10.add(jButton89);

        jButton100.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton100.setText("\u2208");
        jButton100.setToolTipText("");
        jButton100.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton100.setFocusable(false);
        jButton100.setName("77-IsIn");
        jButton100.setOpaque(false);
        jToolBar10.add(jButton100);

        jButton102.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton102.setText("\u2209");
        jButton102.setToolTipText("");
        jButton102.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton102.setFocusable(false);
        jButton102.setName("79-NotIn");
        jButton102.setOpaque(false);
        jToolBar10.add(jButton102);

        jButton103.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton103.setText("\u2200");
        jButton103.setToolTipText("");
        jButton103.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton103.setFocusable(false);
        jButton103.setName("76-ForAll");
        jButton103.setOpaque(false);
        jToolBar10.add(jButton103);

        jButton104.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton104.setText("\u2203");
        jButton104.setToolTipText("");
        jButton104.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton104.setFocusable(false);
        jButton104.setName("75-Exists");
        jButton104.setOpaque(false);
        jToolBar10.add(jButton104);

        jButton108.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton108.setText("\u2204");
        jButton108.setToolTipText("");
        jButton108.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton108.setFocusable(false);
        jButton108.setName("78-NotExists");
        jButton108.setOpaque(false);
        jToolBar10.add(jButton108);

        jButton105.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton105.setText("\u00ac");
        jButton105.setToolTipText("");
        jButton105.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton105.setFocusable(false);
        jButton105.setName("83-Not");
        jButton105.setOpaque(false);
        jToolBar10.add(jButton105);

        jButton106.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton106.setText("\u2228");
        jButton106.setToolTipText("");
        jButton106.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton106.setFocusable(false);
        jButton106.setName("81-Or");
        jButton106.setOpaque(false);
        jToolBar10.add(jButton106);

        jButton107.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton107.setText("\u2227");
        jButton107.setToolTipText("");
        jButton107.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton107.setFocusable(false);
        jButton107.setName("82-And");
        jButton107.setOpaque(false);
        jToolBar10.add(jButton107);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel3.add(jToolBar10, gridBagConstraints);

        jTabbedPaneInput.addTab("tab7", jPanel3);

        jToolBar3.setBackground(new java.awt.Color(255, 255, 255));
        jToolBar3.setFloatable(false);
        jButton19.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton19.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/division.gif")));
        jButton19.setToolTipText("");
        jButton19.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton19.setFocusable(false);
        jButton19.setName("1-Divide");
        jButton19.setOpaque(false);
        jToolBar3.add(jButton19);

        jButton3.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/square root.gif")));
        jButton3.setToolTipText("");
        jButton3.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton3.setFocusable(false);
        jButton3.setName("5-SquareRoot");
        jButton3.setOpaque(false);
        jToolBar3.add(jButton3);

        jButton4.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/n-th root.gif")));
        jButton4.setToolTipText("");
        jButton4.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton4.setFocusable(false);
        jButton4.setName("6-NthRoot");
        jButton4.setOpaque(false);
        jToolBar3.add(jButton4);

        jButton22.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton22.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/superscript.gif")));
        jButton22.setToolTipText("");
        jButton22.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton22.setFocusable(false);
        jButton22.setName("7-Power");
        jButton22.setOpaque(false);
        jToolBar3.add(jButton22);

        jButton23.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton23.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/subscript.gif")));
        jButton23.setToolTipText("");
        jButton23.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton23.setFocusable(false);
        jButton23.setName("8-Subscript");
        jButton23.setOpaque(false);
        jToolBar3.add(jButton23);

        jButtonMatrix1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/function.gif")));
        jButtonMatrix1.setToolTipText("");
        jButtonMatrix1.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButtonMatrix1.setFocusable(false);
        jButtonMatrix1.setName("50-Function");
        jButtonMatrix1.setOpaque(false);
        jButtonMatrix1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonMatrix1MouseClicked(evt);
            }
        });

        jToolBar3.add(jButtonMatrix1);

        jButton77.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton77.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/evaluate.gif")));
        jButton77.setToolTipText("");
        jButton77.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton77.setFocusable(false);
        jButton77.setName("57-Evaluate");
        jButton77.setOpaque(false);
        jToolBar3.add(jButton77);

        jButtonMatrix.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/matrix.gif")));
        jButtonMatrix.setToolTipText("");
        jButtonMatrix.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButtonMatrix.setFocusable(false);
        jButtonMatrix.setName("9-Matrix");
        jButtonMatrix.setOpaque(false);
        jButtonMatrix.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonMatrixMouseClicked(evt);
            }
        });

        jToolBar3.add(jButtonMatrix);

        jButtonMatrix2.setFont(new java.awt.Font("Tahoma", 0, 14));
        jButtonMatrix2.setText("det");
        jButtonMatrix2.setToolTipText("");
        jButtonMatrix2.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButtonMatrix2.setFocusable(false);
        jButtonMatrix2.setName("51-Determinant");
        jButtonMatrix2.setOpaque(false);
        jButtonMatrix2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonMatrix2MouseClicked(evt);
            }
        });

        jToolBar3.add(jButtonMatrix2);

        jButtonMatrix3.setFont(new java.awt.Font("Tahoma", 0, 14));
        jButtonMatrix3.setText("Tr");
        jButtonMatrix3.setToolTipText("");
        jButtonMatrix3.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButtonMatrix3.setFocusable(false);
        jButtonMatrix3.setName("52-Trace");
        jButtonMatrix3.setOpaque(false);
        jButtonMatrix3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonMatrix3MouseClicked(evt);
            }
        });

        jToolBar3.add(jButtonMatrix3);

        jTabbedPaneInput.addTab("tab3", jToolBar3);

        jToolBar4.setBackground(new java.awt.Color(255, 255, 255));
        jToolBar4.setFloatable(false);
        jButton15.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/roundbr.gif")));
        jButton15.setToolTipText("");
        jButton15.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton15.setFocusable(false);
        jButton15.setName("31-BracketsRnd");
        jButton15.setOpaque(false);
        jToolBar4.add(jButton15);

        jButton16.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/curlybr.gif")));
        jButton16.setToolTipText("");
        jButton16.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton16.setFocusable(false);
        jButton16.setName("32-BracketsCurl");
        jButton16.setOpaque(false);
        jToolBar4.add(jButton16);

        jButton34.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton34.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/squarebr.gif")));
        jButton34.setToolTipText("");
        jButton34.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton34.setFocusable(false);
        jButton34.setName("33-BracketsSq");
        jButton34.setOpaque(false);
        jToolBar4.add(jButton34);

        jButton33.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton33.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/absolute.gif")));
        jButton33.setToolTipText("");
        jButton33.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton33.setFocusable(false);
        jButton33.setName("34-Abs");
        jButton33.setOpaque(false);
        jToolBar4.add(jButton33);

        jButton73.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton73.setText("max");
        jButton73.setToolTipText("");
        jButton73.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton73.setFocusable(false);
        jButton73.setName("47-Max");
        jButton73.setOpaque(false);
        jToolBar4.add(jButton73);

        jButton74.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton74.setText("min");
        jButton74.setToolTipText("");
        jButton74.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton74.setFocusable(false);
        jButton74.setName("48-Min");
        jButton74.setOpaque(false);
        jToolBar4.add(jButton74);

        jTabbedPaneInput.addTab("tab4", jToolBar4);

        jToolBar6.setBackground(new java.awt.Color(255, 255, 255));
        jToolBar6.setFloatable(false);
        jButtonSin.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButtonSin.setText("sin");
        jButtonSin.setToolTipText("");
        jButtonSin.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButtonSin.setFocusable(false);
        jButtonSin.setName("21-Sine");
        jButtonSin.setOpaque(false);
        jToolBar6.add(jButtonSin);

        jButtonCos.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButtonCos.setText("cos");
        jButtonCos.setToolTipText("");
        jButtonCos.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButtonCos.setFocusable(false);
        jButtonCos.setName("22-Cosine");
        jButtonCos.setOpaque(false);
        jToolBar6.add(jButtonCos);

        jButtonTan.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButtonTan.setText("tan");
        jButtonTan.setToolTipText("");
        jButtonTan.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButtonTan.setFocusable(false);
        jButtonTan.setName("23-Tangent");
        jButtonTan.setOpaque(false);
        jToolBar6.add(jButtonTan);

        jCheckBoxInverse.setText("Inverse");
        jCheckBoxInverse.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jCheckBoxInverse.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBoxInverse.setOpaque(false);
        jCheckBoxInverse.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCheckBoxInverseMouseClicked(evt);
            }
        });

        jToolBar6.add(jCheckBoxInverse);

        jCheckBoxHyp.setText("Hyperbolic");
        jCheckBoxHyp.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jCheckBoxHyp.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jCheckBoxHyp.setOpaque(false);
        jCheckBoxHyp.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jCheckBoxHypMouseClicked(evt);
            }
        });

        jToolBar6.add(jCheckBoxHyp);

        jButton30.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton30.setText("log");
        jButton30.setToolTipText("");
        jButton30.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton30.setFocusable(false);
        jButton30.setName("27-Logarithm");
        jButton30.setOpaque(false);
        jToolBar6.add(jButton30);

        jButton31.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton31.setText("ln");
        jButton31.setToolTipText("");
        jButton31.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton31.setFocusable(false);
        jButton31.setName("28-NaturalLogarithm");
        jButton31.setOpaque(false);
        jToolBar6.add(jButton31);

        jButton36.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton36.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/e.gif")));
        jButton36.setToolTipText("");
        jButton36.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton36.setFocusable(false);
        jButton36.setName("29-Exp");
        jButton36.setOpaque(false);
        jToolBar6.add(jButton36);

        jTabbedPaneInput.addTab("tab6", jToolBar6);

        jToolBar8.setBackground(new java.awt.Color(255, 255, 255));
        jToolBar8.setFloatable(false);
        jButton21.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton21.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/integral.gif")));
        jButton21.setToolTipText("");
        jButton21.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));
        jButton21.setFocusable(false);
        jButton21.setName("45-Integral");
        jButton21.setOpaque(false);
        jToolBar8.add(jButton21);

        jButton75.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton75.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/def_integral.gif")));
        jButton75.setToolTipText("");
        jButton75.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));
        jButton75.setFocusable(false);
        jButton75.setName("53-DefiniteIntegral");
        jButton75.setOpaque(false);
        jToolBar8.add(jButton75);

        jButton37.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton37.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/differential.gif")));
        jButton37.setToolTipText("");
        jButton37.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));
        jButton37.setFocusable(false);
        jButton37.setName("46-Differential");
        jButton37.setOpaque(false);
        jToolBar8.add(jButton37);

        jButton84.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton84.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/partialDifferential.gif")));
        jButton84.setToolTipText("");
        jButton84.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));
        jButton84.setFocusable(false);
        jButton84.setName("84-PartialDifferential");
        jButton84.setOpaque(false);
        jToolBar8.add(jButton84);

        jButton76.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton76.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/limit.gif")));
        jButton76.setToolTipText("");
        jButton76.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));
        jButton76.setFocusable(false);
        jButton76.setName("56-Limit");
        jButton76.setOpaque(false);
        jToolBar8.add(jButton76);

        jButtonMatrix5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/sum.gif")));
        jButtonMatrix5.setToolTipText("");
        jButtonMatrix5.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButtonMatrix5.setFocusable(false);
        jButtonMatrix5.setName("55-Sum");
        jButtonMatrix5.setOpaque(false);
        jButtonMatrix5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonMatrix5MouseClicked(evt);
            }
        });

        jToolBar8.add(jButtonMatrix5);

        jButtonMatrix4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/product.gif")));
        jButtonMatrix4.setToolTipText("");
        jButtonMatrix4.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButtonMatrix4.setFocusable(false);
        jButtonMatrix4.setName("54-Product");
        jButtonMatrix4.setOpaque(false);
        jButtonMatrix4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonMatrix4MouseClicked(evt);
            }
        });

        jToolBar8.add(jButtonMatrix4);

        jButton35.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton35.setText("\u221e");
        jButton35.setToolTipText("");
        jButton35.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton35.setFocusable(false);
        jButton35.setName("36-Infinity");
        jButton35.setOpaque(false);
        jToolBar8.add(jButton35);

        jTabbedPaneInput.addTab("tab8", jToolBar8);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jToolBar7.setBackground(new java.awt.Color(255, 255, 255));
        jToolBar7.setFloatable(false);
        jButton38.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton38.setText("\u0393");
        jButton38.setToolTipText("");
        jButton38.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton38.setFocusable(false);
        jButton38.setName("35-GreekLetter");
        jButton38.setOpaque(false);
        jToolBar7.add(jButton38);

        jButton39.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton39.setText("\u0394");
        jButton39.setToolTipText("");
        jButton39.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton39.setFocusable(false);
        jButton39.setName("35-GreekLetter");
        jButton39.setOpaque(false);
        jToolBar7.add(jButton39);

        jButton40.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton40.setText("\u0398");
        jButton40.setToolTipText("");
        jButton40.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton40.setFocusable(false);
        jButton40.setName("35-GreekLetter");
        jButton40.setOpaque(false);
        jToolBar7.add(jButton40);

        jButton41.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton41.setText("\u039e");
        jButton41.setToolTipText("");
        jButton41.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton41.setFocusable(false);
        jButton41.setName("35-GreekLetter");
        jButton41.setOpaque(false);
        jToolBar7.add(jButton41);

        jButton42.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton42.setText("\u03a0");
        jButton42.setToolTipText("");
        jButton42.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton42.setFocusable(false);
        jButton42.setName("35-GreekLetter");
        jButton42.setOpaque(false);
        jToolBar7.add(jButton42);

        jButton43.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton43.setText("\u03a3");
        jButton43.setToolTipText("");
        jButton43.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton43.setFocusable(false);
        jButton43.setName("35-GreekLetter");
        jButton43.setOpaque(false);
        jToolBar7.add(jButton43);

        jButton44.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton44.setText("\u03b3");
        jButton44.setToolTipText("");
        jButton44.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton44.setFocusable(false);
        jButton44.setName("35-GreekLetter");
        jButton44.setOpaque(false);
        jToolBar7.add(jButton44);

        jButton45.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton45.setText("\u03a6");
        jButton45.setToolTipText("");
        jButton45.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton45.setFocusable(false);
        jButton45.setName("35-GreekLetter");
        jButton45.setOpaque(false);
        jToolBar7.add(jButton45);

        jButton46.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton46.setText("\u03a8");
        jButton46.setToolTipText("");
        jButton46.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton46.setFocusable(false);
        jButton46.setName("35-GreekLetter");
        jButton46.setOpaque(false);
        jToolBar7.add(jButton46);

        jButton47.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton47.setText("\u03a9");
        jButton47.setToolTipText("");
        jButton47.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton47.setFocusable(false);
        jButton47.setName("35-GreekLetter");
        jButton47.setOpaque(false);
        jToolBar7.add(jButton47);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel1.add(jToolBar7, gridBagConstraints);

        jToolBar2.setBackground(new java.awt.Color(255, 255, 255));
        jToolBar2.setFloatable(false);
        jButton48.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton48.setText("\u03b1");
        jButton48.setToolTipText("");
        jButton48.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton48.setFocusable(false);
        jButton48.setName("35-GreekLetter");
        jButton48.setOpaque(false);
        jToolBar2.add(jButton48);

        jButton49.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton49.setText("\u03b2");
        jButton49.setToolTipText("");
        jButton49.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton49.setFocusable(false);
        jButton49.setName("35-GreekLetter");
        jButton49.setOpaque(false);
        jToolBar2.add(jButton49);

        jButton50.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton50.setText("\u03b3");
        jButton50.setToolTipText("");
        jButton50.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton50.setFocusable(false);
        jButton50.setName("35-GreekLetter");
        jButton50.setOpaque(false);
        jToolBar2.add(jButton50);

        jButton51.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton51.setText("\u03b4");
        jButton51.setToolTipText("");
        jButton51.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton51.setFocusable(false);
        jButton51.setName("35-GreekLetter");
        jButton51.setOpaque(false);
        jToolBar2.add(jButton51);

        jButton52.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton52.setText("\u03b5");
        jButton52.setToolTipText("");
        jButton52.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton52.setFocusable(false);
        jButton52.setName("35-GreekLetter");
        jButton52.setOpaque(false);
        jToolBar2.add(jButton52);

        jButton53.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton53.setText("\u03b6");
        jButton53.setToolTipText("");
        jButton53.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton53.setFocusable(false);
        jButton53.setName("35-GreekLetter");
        jButton53.setOpaque(false);
        jToolBar2.add(jButton53);

        jButton54.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton54.setText("\u03b7");
        jButton54.setToolTipText("");
        jButton54.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton54.setFocusable(false);
        jButton54.setName("35-GreekLetter");
        jButton54.setOpaque(false);
        jToolBar2.add(jButton54);

        jButton55.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton55.setText("\u03b8");
        jButton55.setToolTipText("");
        jButton55.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton55.setFocusable(false);
        jButton55.setName("35-GreekLetter");
        jButton55.setOpaque(false);
        jToolBar2.add(jButton55);

        jButton56.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton56.setText("\u03b9");
        jButton56.setToolTipText("");
        jButton56.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton56.setFocusable(false);
        jButton56.setName("35-GreekLetter");
        jButton56.setOpaque(false);
        jToolBar2.add(jButton56);

        jButton57.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton57.setText("\u03ba");
        jButton57.setToolTipText("");
        jButton57.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton57.setFocusable(false);
        jButton57.setName("35-GreekLetter");
        jButton57.setOpaque(false);
        jToolBar2.add(jButton57);

        jButton58.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton58.setText("\u03bb");
        jButton58.setToolTipText("");
        jButton58.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton58.setFocusable(false);
        jButton58.setName("35-GreekLetter");
        jButton58.setOpaque(false);
        jToolBar2.add(jButton58);

        jButton59.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton59.setText("\u03bc");
        jButton59.setToolTipText("");
        jButton59.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton59.setFocusable(false);
        jButton59.setName("35-GreekLetter");
        jButton59.setOpaque(false);
        jToolBar2.add(jButton59);

        jButton60.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton60.setText("\u03bd");
        jButton60.setToolTipText("");
        jButton60.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton60.setFocusable(false);
        jButton60.setName("35-GreekLetter");
        jButton60.setOpaque(false);
        jToolBar2.add(jButton60);

        jButton61.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton61.setText("\u03be");
        jButton61.setToolTipText("");
        jButton61.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton61.setFocusable(false);
        jButton61.setName("35-GreekLetter");
        jButton61.setOpaque(false);
        jToolBar2.add(jButton61);

        jButton62.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton62.setText("\u03bf");
        jButton62.setToolTipText("");
        jButton62.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton62.setFocusable(false);
        jButton62.setName("35-GreekLetter");
        jButton62.setOpaque(false);
        jToolBar2.add(jButton62);

        jButton63.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton63.setText("\u03c0");
        jButton63.setToolTipText("");
        jButton63.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton63.setFocusable(false);
        jButton63.setName("35-GreekLetter");
        jButton63.setOpaque(false);
        jToolBar2.add(jButton63);

        jButton64.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton64.setText("\u03c1");
        jButton64.setToolTipText("");
        jButton64.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton64.setFocusable(false);
        jButton64.setName("35-GreekLetter");
        jButton64.setOpaque(false);
        jToolBar2.add(jButton64);

        jButton66.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton66.setText("\u03c3");
        jButton66.setToolTipText("");
        jButton66.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton66.setFocusable(false);
        jButton66.setName("35-GreekLetter");
        jButton66.setOpaque(false);
        jToolBar2.add(jButton66);

        jButton67.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton67.setText("\u03c4");
        jButton67.setToolTipText("");
        jButton67.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton67.setFocusable(false);
        jButton67.setName("35-GreekLetter");
        jButton67.setOpaque(false);
        jToolBar2.add(jButton67);

        jButton68.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton68.setText("\u03c5");
        jButton68.setToolTipText("");
        jButton68.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton68.setFocusable(false);
        jButton68.setName("35-GreekLetter");
        jButton68.setOpaque(false);
        jToolBar2.add(jButton68);

        jButton69.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton69.setText("\u03c6");
        jButton69.setToolTipText("");
        jButton69.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton69.setFocusable(false);
        jButton69.setName("35-GreekLetter");
        jButton69.setOpaque(false);
        jToolBar2.add(jButton69);

        jButton70.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton70.setText("\u03c7");
        jButton70.setToolTipText("");
        jButton70.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton70.setFocusable(false);
        jButton70.setName("35-GreekLetter");
        jButton70.setOpaque(false);
        jToolBar2.add(jButton70);

        jButton71.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton71.setText("\u03c8");
        jButton71.setToolTipText("");
        jButton71.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton71.setFocusable(false);
        jButton71.setName("35-GreekLetter");
        jButton71.setOpaque(false);
        jToolBar2.add(jButton71);

        jButton72.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton72.setText("\u03c9");
        jButton72.setToolTipText("");
        jButton72.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        jButton72.setFocusable(false);
        jButton72.setName("35-GreekLetter");
        jButton72.setOpaque(false);
        jToolBar2.add(jButton72);

        jPanel1.add(jToolBar2, new java.awt.GridBagConstraints());

        jTabbedPaneInput.addTab("tab6", jPanel1);

        jToolBar5.setBackground(new java.awt.Color(255, 255, 255));
        jToolBar5.setFloatable(false);
        jButton13.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton13.setText("\u2190");
        jButton13.setToolTipText("");
        jButton13.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton13.setFocusable(false);
        jButton13.setName("60-LeftArrow");
        jButton13.setOpaque(false);
        jToolBar5.add(jButton13);

        jButton14.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton14.setText("\u2192");
        jButton14.setToolTipText("");
        jButton14.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton14.setFocusable(false);
        jButton14.setName("61-RightArrow");
        jButton14.setOpaque(false);
        jToolBar5.add(jButton14);

        jButton17.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton17.setText("\u2191");
        jButton17.setToolTipText("");
        jButton17.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton17.setFocusable(false);
        jButton17.setName("62-UpArrow");
        jButton17.setOpaque(false);
        jToolBar5.add(jButton17);

        jButton18.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton18.setText("\u2193");
        jButton18.setToolTipText("");
        jButton18.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton18.setFocusable(false);
        jButton18.setName("63-DownArrow");
        jButton18.setOpaque(false);
        jToolBar5.add(jButton18);

        jButton27.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton27.setText("\u2196");
        jButton27.setToolTipText("");
        jButton27.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton27.setFocusable(false);
        jButton27.setName("68-NorthWestArrow");
        jButton27.setOpaque(false);
        jToolBar5.add(jButton27);

        jButton28.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton28.setText("\u2199");
        jButton28.setToolTipText("");
        jButton28.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton28.setFocusable(false);
        jButton28.setName("69-SouthWestArrow");
        jButton28.setOpaque(false);
        jToolBar5.add(jButton28);

        jButton78.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton78.setText("\u2197");
        jButton78.setToolTipText("");
        jButton78.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton78.setFocusable(false);
        jButton78.setName("70-NorthEastArrow");
        jButton78.setOpaque(false);
        jToolBar5.add(jButton78);

        jButton79.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton79.setText("\u2198");
        jButton79.setToolTipText("");
        jButton79.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton79.setFocusable(false);
        jButton79.setName("71-SouthEastArrow");
        jButton79.setOpaque(false);
        jToolBar5.add(jButton79);

        jButton80.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton80.setText("\u2194");
        jButton80.setToolTipText("");
        jButton80.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton80.setFocusable(false);
        jButton80.setName("66-LeftRightArrow");
        jButton80.setOpaque(false);
        jToolBar5.add(jButton80);

        jButton81.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton81.setText("\u21d0");
        jButton81.setToolTipText("");
        jButton81.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton81.setFocusable(false);
        jButton81.setName("64-LeftDblArrow");
        jButton81.setOpaque(false);
        jToolBar5.add(jButton81);

        jButton82.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton82.setText("\u21d2");
        jButton82.setToolTipText("");
        jButton82.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton82.setFocusable(false);
        jButton82.setName("65-RightDblArrow");
        jButton82.setOpaque(false);
        jToolBar5.add(jButton82);

        jButton83.setFont(new java.awt.Font("Lucida Sans Unicode", 0, 14));
        jButton83.setText("\u21d4");
        jButton83.setToolTipText("");
        jButton83.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        jButton83.setFocusable(false);
        jButton83.setName("67-LeftRightDblArrow");
        jButton83.setOpaque(false);
        jToolBar5.add(jButton83);

        jTabbedPaneInput.addTab("tab7", jToolBar5);

        jPanelMain.add(jTabbedPaneInput, java.awt.BorderLayout.NORTH);

        jPanelAppMain.add(jPanelMain, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanelAppMain, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 540, 280));

        jToolBarEdit2.setBackground(new java.awt.Color(255, 255, 255));
        jToolBarEdit2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jToolBarEdit2.setFloatable(false);
        jToolBarEdit2.setEnabled(false);
        jButtonClear.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/new_document_24_h.png")));
        jButtonClear.setToolTipText("");
        jButtonClear.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        jButtonClear.setFocusable(false);
        jButtonClear.setName("ClearMenu");
        jButtonClear.setOpaque(false);
        jButtonClear.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonClearMouseClicked(evt);
            }
        });

        jToolBarEdit2.add(jButtonClear);

        jButtonLoad.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/open_document_24_h.png")));
        jButtonLoad.setToolTipText("");
        jButtonLoad.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        jButtonLoad.setFocusable(false);
        jButtonLoad.setName("OpenButton");
        jButtonLoad.setOpaque(false);
        jButtonLoad.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonLoadMouseClicked(evt);
            }
        });
        jButtonLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadActionPerformed(evt);
            }
        });

        jToolBarEdit2.add(jButtonLoad);

        jButtonSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/save_24_h.png")));
        jButtonSave.setToolTipText("");
        jButtonSave.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        jButtonSave.setFocusable(false);
        jButtonSave.setName("SaveButton");
        jButtonSave.setOpaque(false);
        jButtonSave.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonSaveMouseClicked(evt);
            }
        });

        jToolBarEdit2.add(jButtonSave);

        jButtonUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/undo_24_h.png")));
        jButtonUndo.setToolTipText("");
        jButtonUndo.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        jButtonUndo.setFocusable(false);
        jButtonUndo.setName("UndoButton");
        jButtonUndo.setOpaque(false);
        jButtonUndo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonUndoMouseClicked(evt);
            }
        });

        jToolBarEdit2.add(jButtonUndo);

        jButtonRedo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/redo_24_h.png")));
        jButtonRedo.setToolTipText("");
        jButtonRedo.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        jButtonRedo.setFocusable(false);
        jButtonRedo.setName("RedoButton");
        jButtonRedo.setOpaque(false);
        jButtonRedo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonRedoMouseClicked(evt);
            }
        });

        jToolBarEdit2.add(jButtonRedo);

        jButtonCut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/cut_clipboard_24_h.png")));
        jButtonCut.setToolTipText("");
        jButtonCut.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        jButtonCut.setFocusable(false);
        jButtonCut.setName("CutButton");
        jButtonCut.setOpaque(false);
        jButtonCut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonCutMouseClicked(evt);
            }
        });

        jToolBarEdit2.add(jButtonCut);

        jButtonCopy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/copy_clipboard_24_h.png")));
        jButtonCopy.setToolTipText("");
        jButtonCopy.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        jButtonCopy.setFocusable(false);
        jButtonCopy.setName("CopyButton");
        jButtonCopy.setOpaque(false);
        jButtonCopy.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonCopyMouseClicked(evt);
            }
        });
        jButtonCopy.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jButtonCopyKeyPressed(evt);
            }
        });

        jToolBarEdit2.add(jButtonCopy);

        jButtonPaste.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/paste_clipboard_24_h.png")));
        jButtonPaste.setToolTipText("");
        jButtonPaste.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        jButtonPaste.setFocusable(false);
        jButtonPaste.setName("PasteButton");
        jButtonPaste.setOpaque(false);
        jButtonPaste.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonPasteMouseClicked(evt);
            }
        });
        jButtonPaste.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jButtonPasteFocusGained(evt);
            }
        });

        jToolBarEdit2.add(jButtonPaste);

        jButtonExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Display/Images/move_to_folder_24_h.png")));
        jButtonExport.setToolTipText("");
        jButtonExport.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        jButtonExport.setFocusable(false);
        jButtonExport.setName("ExportButton");
        jButtonExport.setOpaque(false);
        jButtonExport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonExportMouseClicked(evt);
            }
        });
        jButtonExport.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jButtonExportFocusGained(evt);
            }
        });

        jToolBarEdit2.add(jButtonExport);

        getContentPane().add(jToolBarEdit2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 310, 310, 30));

        jMenuFile.setText("File");
        jMenuItemOpen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemOpen.setText("Open...");
        jMenuItemOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOpenActionPerformed(evt);
            }
        });

        jMenuFile.add(jMenuItemOpen);

        jMenuItemSaveAs.setText("Save As...");
        jMenuItemSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveAsActionPerformed(evt);
            }
        });

        jMenuFile.add(jMenuItemSaveAs);

        jMenuFile.add(jSeparator9);

        jMenuItemExport.setText("Export to clipboard");
        jMenuItemExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExportActionPerformed(evt);
            }
        });

        jMenuFile.add(jMenuItemExport);

        jMenuItemExportToImage.setText("Export to image");
        jMenuItemExportToImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemExportToImageActionPerformed(evt);
            }
        });

        jMenuFile.add(jMenuItemExportToImage);

        jMenuBar.add(jMenuFile);

        jMenuEdit.setText("Edit");
        jMenuItemUndo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemUndo.setText("Undo");
        jMenuItemUndo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemUndoActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemUndo);

        jMenuItemRedo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemRedo.setText("Redo");
        jMenuItemRedo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRedoActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemRedo);

        jMenuEdit.add(jSeparator1);

        jMenuItemCut.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemCut.setText("Cut");
        jMenuItemCut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCutActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemCut);

        jMenuItemCopy.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemCopy.setText("Copy");
        jMenuItemCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemCopyActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemCopy);

        jMenuItemPaste.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemPaste.setText("Paste");
        jMenuItemPaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPasteActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemPaste);

        jMenuEdit.add(jSeparator2);

        jMenuItemClear.setText("Clear workspace");
        jMenuItemClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemClearActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemClear);

        jMenuEdit.add(jSeparator3);

        jMenuItemSelectAll.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemSelectAll.setText("Select All");
        jMenuItemSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSelectAllActionPerformed(evt);
            }
        });

        jMenuEdit.add(jMenuItemSelectAll);

        jMenuBar.add(jMenuEdit);

        jMenuOptions.setText("Options");
        jMenuItemSetExport.setText("Set export format");
        jMenuItemSetExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSetExportActionPerformed(evt);
            }
        });

        jMenuOptions.add(jMenuItemSetExport);

        jMenuOptions.add(jSeparator8);

        jMenuItemSetLang.setText("Set language");
        jMenuItemSetLang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSetLangActionPerformed(evt);
            }
        });

        jMenuOptions.add(jMenuItemSetLang);

        jMenuOptions.add(jSeparator4);

        jCheckBoxImplicitMult.setText("Implicit Multiplication");
        jCheckBoxImplicitMult.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxImplicitMultActionPerformed(evt);
            }
        });

        jMenuOptions.add(jCheckBoxImplicitMult);

        jMenuBar.add(jMenuOptions);

        jMenuHelp.setText("Help");
        jMenuDebug.setText("Debug");
        jMenuItemShowTree.setText("Show expression tree");
        jMenuItemShowTree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemShowTreeActionPerformed(evt);
            }
        });

        jMenuDebug.add(jMenuItemShowTree);

        jCheckBoxMenuItemShowOutline.setText("Show outline");
        jCheckBoxMenuItemShowOutline.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxMenuItemShowOutlineActionPerformed(evt);
            }
        });

        jMenuDebug.add(jCheckBoxMenuItemShowOutline);

        jMenuHelp.add(jMenuDebug);

        jMenuHelp.add(jSeparator6);

        jMenuItemOnlineHelp.setText("Online Help");
        jMenuItemOnlineHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemOnlineHelpActionPerformed(evt);
            }
        });

        jMenuHelp.add(jMenuItemOnlineHelp);

        jMenuItemAbout.setText("About DragMath");
        jMenuItemAbout.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenuItemAboutMouseClicked(evt);
            }
        });
        jMenuItemAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemAboutActionPerformed(evt);
            }
        });

        jMenuHelp.add(jMenuItemAbout);

        jMenuBar.add(jMenuHelp);

        setJMenuBar(jMenuBar);

    }// </editor-fold>//GEN-END:initComponents
    
    private void jCheckBoxImplicitMultActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxImplicitMultActionPerformed
        implicitMult = jCheckBoxImplicitMult.isSelected();
        output.setImplictMult(implicitMult);
        addComponent.setImplicitMult(implicitMult);
    }//GEN-LAST:event_jCheckBoxImplicitMultActionPerformed
    
    private void jButtonLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_jButtonLoadActionPerformed
    
    private void jButtonClearMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonClearMouseClicked
        jPanelWorkspace.removeAll();
        jPanelWorkspace.revalidate();
        jPanelWorkspace.repaint();
    }//GEN-LAST:event_jButtonClearMouseClicked
    
    private void jMenuItemExportToImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExportToImageActionPerformed
        String expression = "Failed to get expression";
        
        output.readMathTranFile();
        try {
            expression = output.outputToClipboard(buildTree.generateTree(jPanelWorkspace, false, 0, 0));
            
            try {
                expression = java.net.URLEncoder.encode(expression, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                //ex.printStackTrace();
            }
            
            String[] values = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
            String answer = null;
            answer = (String)JOptionPane.showInputDialog(null, langMan.readLangFile("ImageSize"), "DragMath", JOptionPane.QUESTION_MESSAGE, null, values, "1");
            
            if (answer != null) {
                try {
                    appletContext.showDocument(new java.net.URL("http://www.mathtran.org/cgi-bin/mathtran?" + "D=" + answer + ";tex=" + expression), "_blank");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, langMan.readLangFile("Image"), "DragMath", JOptionPane.ERROR_MESSAGE);
                }
            }
            
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(null, langMan.readLangFile("Latex") + ex.getMessage(), "DragMath", JOptionPane.ERROR_MESSAGE);
        }
        
        output.readFormatFile(output.getOutputFormat());
    }//GEN-LAST:event_jMenuItemExportToImageActionPerformed
    
    private void jMenuItemSetLangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSetLangActionPerformed
        FormatChooser formatChooser = new FormatChooser(appletCodeBase, 1, output, langMan);
        formatChooser.setVisible(true);
        formatChooser.addWindowListener(new java.awt.event.WindowListener() {
            public void windowActivated(WindowEvent e) {
            }
            public void windowClosed(WindowEvent e) {
                loadMenuText();
            }
            public void windowClosing(WindowEvent e) {
            }
            public void windowDeactivated(WindowEvent e) {
            }
            public void windowDeiconified(WindowEvent e) {
            }
            public void windowIconified(WindowEvent e) {
            }
            public void windowOpened(WindowEvent e) {
            }
        });
    }//GEN-LAST:event_jMenuItemSetLangActionPerformed
    
    private void jMenuItemSetExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSetExportActionPerformed
        FormatChooser formatChooser = new FormatChooser(appletCodeBase, 0, output, langMan);
        formatChooser.setVisible(true);
    }//GEN-LAST:event_jMenuItemSetExportActionPerformed
    
    private void jMenuItemExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExportActionPerformed
        output();
    }//GEN-LAST:event_jMenuItemExportActionPerformed
    
    private void jCheckBoxMenuItemShowOutlineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxMenuItemShowOutlineActionPerformed
        setBorders(jCheckBoxMenuItemShowOutline.isSelected(), jPanelWorkspace);
    }//GEN-LAST:event_jCheckBoxMenuItemShowOutlineActionPerformed
    
    private void jMenuItemShowTreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemShowTreeActionPerformed
        try {
            TreeDisplay treeDisplay = new TreeDisplay(buildTree.generateTree(jPanelWorkspace, false, 0, 0), inputComponents);
            treeDisplay.setVisible(true);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(null, langMan.readLangFile("Tree") + ex.getMessage(), "DragMath", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItemShowTreeActionPerformed
    
    private void jButtonExportFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButtonExportFocusGained
// TODO add your handling code here:
    }//GEN-LAST:event_jButtonExportFocusGained
    
    private void jButtonExportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonExportMouseClicked
        output();
    }//GEN-LAST:event_jButtonExportMouseClicked
    
    private void jButtonLoadMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonLoadMouseClicked
        openFile();
    }//GEN-LAST:event_jButtonLoadMouseClicked
    
    private void jButtonSaveMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonSaveMouseClicked
        saveAsFile();
    }//GEN-LAST:event_jButtonSaveMouseClicked
    
    private void jMenuItemSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSelectAllActionPerformed
        motionSelectListener.highlight(jPanelWorkspace, null, true);
    }//GEN-LAST:event_jMenuItemSelectAllActionPerformed
    
    private void jMenuItemClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemClearActionPerformed
        jPanelWorkspace.removeAll();
        jPanelWorkspace.revalidate();
        jPanelWorkspace.repaint();
    }//GEN-LAST:event_jMenuItemClearActionPerformed
    
    private void jMenuItemPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPasteActionPerformed
        addComponent.paste();
    }//GEN-LAST:event_jMenuItemPasteActionPerformed
    
    private void jMenuItemCopyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCopyActionPerformed
        try {
            addComponent.copy(jPanelWorkspace, buildTree);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(null, langMan.readLangFile("Copy") + ex.getMessage(), "DragMath", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItemCopyActionPerformed
    
    private void jMenuItemCutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemCutActionPerformed
        try {
            addComponent.cut(jPanelWorkspace, buildTree);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(null, langMan.readLangFile("Cut") + ex.getMessage(), "DragMath", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jMenuItemCutActionPerformed
    
    private void jMenuItemRedoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRedoActionPerformed
        addComponent.redoState();
    }//GEN-LAST:event_jMenuItemRedoActionPerformed
    
    private void jMenuItemSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveAsActionPerformed
        saveAsFile();;
    }//GEN-LAST:event_jMenuItemSaveAsActionPerformed
    
    private void jMenuItemOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOpenActionPerformed
        openFile();
    }//GEN-LAST:event_jMenuItemOpenActionPerformed
    
    private void jMenuItemUndoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemUndoActionPerformed
        addComponent.undoState();
    }//GEN-LAST:event_jMenuItemUndoActionPerformed
    
    private void jMenuItemOnlineHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemOnlineHelpActionPerformed
        try {
            appletContext.showDocument(new java.net.URL("http://www.dragmath.bham.ac.uk/doc/index.html"), "_blank");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_jMenuItemOnlineHelpActionPerformed
    
    private void jMenuItemAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemAboutActionPerformed
        AboutFrame aboutFrame = new AboutFrame(appletContext);
        aboutFrame.setVisible(true);
    }//GEN-LAST:event_jMenuItemAboutActionPerformed
    
    private void jMenuItemAboutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuItemAboutMouseClicked
        
    }//GEN-LAST:event_jMenuItemAboutMouseClicked
    
    private void jButtonMatrix1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonMatrix1MouseClicked
// TODO add your handling code here:
    }//GEN-LAST:event_jButtonMatrix1MouseClicked
    
    private void jButtonMatrixMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonMatrixMouseClicked
        
    }//GEN-LAST:event_jButtonMatrixMouseClicked
    
    private void jButtonMatrix2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonMatrix2MouseClicked
// TODO add your handling code here:
    }//GEN-LAST:event_jButtonMatrix2MouseClicked
    
    private void jButtonMatrix3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonMatrix3MouseClicked
// TODO add your handling code here:
    }//GEN-LAST:event_jButtonMatrix3MouseClicked
    
    private void jCheckBoxInverseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCheckBoxInverseMouseClicked
        changeTrigButtons();
    }//GEN-LAST:event_jCheckBoxInverseMouseClicked
    
    private void jCheckBoxHypMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jCheckBoxHypMouseClicked
        changeTrigButtons();
    }//GEN-LAST:event_jCheckBoxHypMouseClicked
    
    private void jButtonMatrix5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonMatrix5MouseClicked
// TODO add your handling code here:
    }//GEN-LAST:event_jButtonMatrix5MouseClicked
    
    private void jButtonMatrix4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonMatrix4MouseClicked
// TODO add your handling code here:
    }//GEN-LAST:event_jButtonMatrix4MouseClicked
    
    private void jButtonCutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonCutMouseClicked
        try {
            addComponent.cut(jPanelWorkspace, buildTree);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(null, langMan.readLangFile("Cut") + ex.getMessage(), "DragMath", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonCutMouseClicked
    
    private void jButtonCopyMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonCopyMouseClicked
        try {
            addComponent.copy(jPanelWorkspace, buildTree);
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(null, langMan.readLangFile("Copy") + ex.getMessage(), "DragMath", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_jButtonCopyMouseClicked
    
    private void jButtonCopyKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jButtonCopyKeyPressed
        
    }//GEN-LAST:event_jButtonCopyKeyPressed
    
    private void jButtonPasteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonPasteMouseClicked
        addComponent.paste();
    }//GEN-LAST:event_jButtonPasteMouseClicked
    
    private void jButtonPasteFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jButtonPasteFocusGained
        
    }//GEN-LAST:event_jButtonPasteFocusGained
    
    private void jButtonUndoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonUndoMouseClicked
        addComponent.undoState();
    }//GEN-LAST:event_jButtonUndoMouseClicked
    
    private void jButtonRedoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonRedoMouseClicked
        addComponent.redoState();
    }//GEN-LAST:event_jButtonRedoMouseClicked
    
    private void jPanelWorkspaceComponentAdded(java.awt.event.ContainerEvent evt) {//GEN-FIRST:event_jPanelWorkspaceComponentAdded
        
    }//GEN-LAST:event_jPanelWorkspaceComponentAdded
    
    private void jPanelWorkspaceKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jPanelWorkspaceKeyPressed
//        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_A && evt.isControlDown()) {
//            motionSelectListener.highlight(jPanelWorkspace, null, true);
//        } else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_Z && evt.isControlDown()) {
//            addComponent.undoState();
//        } else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_V && evt.isControlDown()) {
//            addComponent.paste();
//        } else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_X && evt.isControlDown()) {
//            try {
//                addComponent.cut(jPanelWorkspace, buildTree);
//            } catch (ParseException ex) {
//                JOptionPane.showMessageDialog(null, langMan.readLangFile("Cut") + ex.getMessage(), "DragMath", JOptionPane.ERROR_MESSAGE);
//            }
//        }
//        else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_C && evt.isControlDown()) {
//            try {
//                addComponent.copy(jPanelWorkspace, buildTree);
//            } catch (ParseException ex) {
//                JOptionPane.showMessageDialog(null, langMan.readLangFile("Copy") + ex.getMessage(), "DragMath", JOptionPane.ERROR_MESSAGE);
//            }
//
// }
        // else
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_DELETE || evt.getKeyCode() == java.awt.event.KeyEvent.VK_BACK_SPACE) {
            addComponent.delete(jPanelWorkspace);
        } else {
            // If workspace is blank
            if (jPanelWorkspace.getComponentCount() == 0) {
                TextBox newBox = addComponent.createBox(false);
                jPanelWorkspace.add(newBox);
                newBox.requestFocusInWindow();
                newBox.setText(String.valueOf(evt.getKeyChar()));
                jPanelWorkspace.revalidate();
            }
        }
    }//GEN-LAST:event_jPanelWorkspaceKeyPressed
    
    public void changeTrigButtons() {
        if (jCheckBoxInverse.isSelected()) {
            if (jCheckBoxHyp.isSelected()) {
                jButtonCos.setText(inputComponents[43].getDisplayText());
                jButtonTan.setText(inputComponents[44].getDisplayText());
                jButtonSin.setText(inputComponents[42].getDisplayText());
                jButtonCos.setText(inputComponents[43].getDisplayText());
                jButtonTan.setText(inputComponents[44].getDisplayText());
                jButtonSin.setName(inputComponents[42].getTag());
                jButtonCos.setName(inputComponents[43].getTag());
                jButtonTan.setName(inputComponents[44].getTag());
            } else {
                jButtonSin.setText(inputComponents[24].getDisplayText());
                jButtonCos.setText(inputComponents[25].getDisplayText());
                jButtonTan.setText(inputComponents[26].getDisplayText());
                jButtonSin.setName(inputComponents[24].getTag());
                jButtonCos.setName(inputComponents[25].getTag());
                jButtonTan.setName(inputComponents[26].getTag());
            }
        } else {
            if (jCheckBoxHyp.isSelected()) {
                jButtonSin.setText(inputComponents[39].getDisplayText());
                jButtonCos.setText(inputComponents[40].getDisplayText());
                jButtonTan.setText(inputComponents[41].getDisplayText());
                jButtonSin.setName(inputComponents[39].getTag());
                jButtonCos.setName(inputComponents[40].getTag());
                jButtonTan.setName(inputComponents[41].getTag());
            } else {
                jButtonSin.setText(inputComponents[21].getDisplayText());
                jButtonCos.setText(inputComponents[22].getDisplayText());
                jButtonTan.setText(inputComponents[23].getDisplayText());
                jButtonSin.setName(inputComponents[21].getTag());
                jButtonCos.setName(inputComponents[22].getTag());
                jButtonTan.setName(inputComponents[23].getTag());
            }
        }
    }
    
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupExportLang;
    private javax.swing.ButtonGroup buttonGroupLang;
    private javax.swing.JButton jButton100;
    private javax.swing.JButton jButton101;
    private javax.swing.JButton jButton102;
    private javax.swing.JButton jButton103;
    private javax.swing.JButton jButton104;
    private javax.swing.JButton jButton105;
    private javax.swing.JButton jButton106;
    private javax.swing.JButton jButton107;
    private javax.swing.JButton jButton108;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton27;
    private javax.swing.JButton jButton28;
    private javax.swing.JButton jButton29;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton30;
    private javax.swing.JButton jButton31;
    private javax.swing.JButton jButton32;
    private javax.swing.JButton jButton33;
    private javax.swing.JButton jButton34;
    private javax.swing.JButton jButton35;
    private javax.swing.JButton jButton36;
    private javax.swing.JButton jButton37;
    private javax.swing.JButton jButton38;
    private javax.swing.JButton jButton39;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton40;
    private javax.swing.JButton jButton41;
    private javax.swing.JButton jButton42;
    private javax.swing.JButton jButton43;
    private javax.swing.JButton jButton44;
    private javax.swing.JButton jButton45;
    private javax.swing.JButton jButton46;
    private javax.swing.JButton jButton47;
    private javax.swing.JButton jButton48;
    private javax.swing.JButton jButton49;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton50;
    private javax.swing.JButton jButton51;
    private javax.swing.JButton jButton52;
    private javax.swing.JButton jButton53;
    private javax.swing.JButton jButton54;
    private javax.swing.JButton jButton55;
    private javax.swing.JButton jButton56;
    private javax.swing.JButton jButton57;
    private javax.swing.JButton jButton58;
    private javax.swing.JButton jButton59;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton60;
    private javax.swing.JButton jButton61;
    private javax.swing.JButton jButton62;
    private javax.swing.JButton jButton63;
    private javax.swing.JButton jButton64;
    private javax.swing.JButton jButton65;
    private javax.swing.JButton jButton66;
    private javax.swing.JButton jButton67;
    private javax.swing.JButton jButton68;
    private javax.swing.JButton jButton69;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton70;
    private javax.swing.JButton jButton71;
    private javax.swing.JButton jButton72;
    private javax.swing.JButton jButton73;
    private javax.swing.JButton jButton74;
    private javax.swing.JButton jButton75;
    private javax.swing.JButton jButton76;
    private javax.swing.JButton jButton77;
    private javax.swing.JButton jButton78;
    private javax.swing.JButton jButton79;
    private javax.swing.JButton jButton80;
    private javax.swing.JButton jButton81;
    private javax.swing.JButton jButton82;
    private javax.swing.JButton jButton83;
    private javax.swing.JButton jButton84;
    private javax.swing.JButton jButton85;
    private javax.swing.JButton jButton86;
    private javax.swing.JButton jButton87;
    private javax.swing.JButton jButton88;
    private javax.swing.JButton jButton89;
    private javax.swing.JButton jButton90;
    private javax.swing.JButton jButton91;
    private javax.swing.JButton jButton92;
    private javax.swing.JButton jButton93;
    private javax.swing.JButton jButton94;
    private javax.swing.JButton jButtonClear;
    private javax.swing.JButton jButtonCopy;
    private javax.swing.JButton jButtonCos;
    private javax.swing.JButton jButtonCut;
    private javax.swing.JButton jButtonExport;
    private javax.swing.JButton jButtonLoad;
    private javax.swing.JButton jButtonMatrix;
    private javax.swing.JButton jButtonMatrix1;
    private javax.swing.JButton jButtonMatrix2;
    private javax.swing.JButton jButtonMatrix3;
    private javax.swing.JButton jButtonMatrix4;
    private javax.swing.JButton jButtonMatrix5;
    private javax.swing.JButton jButtonPaste;
    private javax.swing.JButton jButtonRedo;
    private javax.swing.JButton jButtonSave;
    private javax.swing.JButton jButtonSin;
    private javax.swing.JButton jButtonTan;
    private javax.swing.JButton jButtonUndo;
    private javax.swing.JCheckBox jCheckBoxHyp;
    private javax.swing.JCheckBoxMenuItem jCheckBoxImplicitMult;
    private javax.swing.JCheckBox jCheckBoxInverse;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemShowOutline;
    private javax.swing.JLabel jLabelStatus;
    private javax.swing.JLabel jLabelTooltip;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JMenu jMenuDebug;
    private javax.swing.JMenu jMenuEdit;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuItemAbout;
    private javax.swing.JMenuItem jMenuItemClear;
    private javax.swing.JMenuItem jMenuItemCopy;
    private javax.swing.JMenuItem jMenuItemCut;
    private javax.swing.JMenuItem jMenuItemExport;
    private javax.swing.JMenuItem jMenuItemExportToImage;
    private javax.swing.JMenuItem jMenuItemOnlineHelp;
    private javax.swing.JMenuItem jMenuItemOpen;
    private javax.swing.JMenuItem jMenuItemPaste;
    private javax.swing.JMenuItem jMenuItemRedo;
    private javax.swing.JMenuItem jMenuItemSaveAs;
    private javax.swing.JMenuItem jMenuItemSelectAll;
    private javax.swing.JMenuItem jMenuItemSetExport;
    private javax.swing.JMenuItem jMenuItemSetLang;
    private javax.swing.JMenuItem jMenuItemShowTree;
    private javax.swing.JMenuItem jMenuItemUndo;
    private javax.swing.JMenu jMenuOptions;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelAppMain;
    private javax.swing.JPanel jPanelMain;
    private javax.swing.JPanel jPanelToolbar;
    private javax.swing.JPanel jPanelWorkspace;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JTabbedPane jTabbedPaneInput;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar10;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private javax.swing.JToolBar jToolBar4;
    private javax.swing.JToolBar jToolBar5;
    private javax.swing.JToolBar jToolBar6;
    private javax.swing.JToolBar jToolBar7;
    private javax.swing.JToolBar jToolBar8;
    private javax.swing.JToolBar jToolBarEdit;
    private javax.swing.JToolBar jToolBarEdit2;
    // End of variables declaration//GEN-END:variables
    
    
    public void saveAsFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new DrgmFileFilter());
        chooser.setDialogTitle(langMan.readLangFile("SaveExpression"));
        int returnVal = chooser.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = chooser.getSelectedFile().getPath();
                if (DrgmFileFilter.isDrgmFile(chooser.getSelectedFile()) == false) {
                    filePath = filePath + ".drgm";
                }
                ObjectOutputStream expressionFile = new ObjectOutputStream(new FileOutputStream(filePath));
                expressionFile.writeObject(buildTree.generateTree(jPanelWorkspace, false, 0, 0));
                expressionFile.close();
                statusBar.println("Expression saved");
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(null, langMan.readLangFile("SavingExp"), "DragMath", JOptionPane.ERROR_MESSAGE);
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(null, langMan.readLangFile("SavingExp"), "DragMath", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, langMan.readLangFile("SavingExp"), "DragMath", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void openFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new DrgmFileFilter());
        chooser.setDialogTitle(langMan.readLangFile("LoadExpression"));
        int returnVal = chooser.showOpenDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            Tree.MathObject tree = null;
            try {
                String temp = chooser.getSelectedFile().getPath();
                ObjectInputStream expressionFile = new ObjectInputStream(new FileInputStream(chooser.getSelectedFile().getPath()));
                tree = (Tree.MathObject) expressionFile.readObject();
                expressionFile.close();
                jPanelWorkspace.removeAll();
                addComponent.pasteTree(jPanelWorkspace, 0, tree, 0);
                addComponent.resetUndoRedo();
            } catch (FileNotFoundException ex) {
                JOptionPane.showMessageDialog(null, langMan.readLangFile("LoadingExp"), "DragMath", JOptionPane.ERROR_MESSAGE);
            } catch (ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(null, langMan.readLangFile("LoadingExp"), "DragMath", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, langMan.readLangFile("LoadingExp"), "DragMath", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    
    /** Sets the borders of all components in the display to red for JPanel, green for JLabel
     * and blue for <code>TextBox</code>, or sets all the borders to none
     * @param borders boolean to say whether to add borders or remove them
     * @param layer JComponent that contains the components to set the borders on
     */
    public void setBorders(boolean borders, JComponent layer) {
        Component[] components = layer.getComponents();
        int i=0;
        boolean panel=false;
        
        while ( i < components.length) {
            java.awt.Color colour = null;
            if (components[i].getClass().getName().equals("javax.swing.JPanel")) {
                colour = new java.awt.Color(java.awt.Color.RED.getRGB());
                panel=true;
            } else if (components[i].getClass().getName().equals("Display.TextBox")) {
                colour = new java.awt.Color(java.awt.Color.BLUE.getRGB());
            } else {
                colour = new java.awt.Color(java.awt.Color.GREEN.getRGB());
            }
            
            JComponent component = (JComponent)components[i];
            if (components[i].getClass().getName().equals("javax.swing.JTextField")) {
                // ignore component, not for user to see
            } else {
                if (borders) {
                    component.setBorder(new LineBorder(colour));
                } else {
                    component.setBorder(javax.swing.BorderFactory.createEmptyBorder());
                    if (component.getClass().getName().equals("Display.TextBox")) {
                        TextBox temp = (TextBox)component;
                        temp.setBorder(new EtchedBorder());
                        if (temp.getText().length() > 0) {
                            temp.setBorder(new EmptyBorder(temp.getInsets()));
                        }
                    }
                }
            }
            if (panel) {
                panel=false;
                setBorders(borders, component);
            }
            i++;
        }
    }
    
    
    public void output() {
        try {
            Tree.MathObject tree = addComponent.checkSelection(jPanelWorkspace, buildTree, null);
            // If a selection has been made
            if (tree != null) {
                output.outputToClipboard(tree);
            } else {
                output.outputToClipboard(buildTree.generateTree(jPanelWorkspace, false, 0, 0));
            }
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage(), "DragMath", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    
    /** Determines where to add the chosen component in the display
     * depending upon where the mouse has been clicked or released.
     */
    public void addComponent(boolean emptyWorkspace, Point mousePos) {
        setCursor(Cursor.getDefaultCursor());
        if (emptyWorkspace) {
            mousePos = new Point(0,0);
        }
        if (mousePos.getX() < 0 || mousePos.getX() > jPanelWorkspace.getWidth() || mousePos.getY() < 0 || mousePos.getY() > jPanelWorkspace.getHeight()) {
            mousePos=null;
        }
        if (dragging && mousePos != null) {
            
            // Find the component where the mouse has been clicked
            JComponent component = (JComponent)jPanelWorkspace.findComponentAt(mousePos);
            JPanel layer = null;
            if (component != jPanelWorkspace) {
                layer = (JPanel)component.getParent();
            } else {
                layer = jPanelWorkspace;
            }
            
            int n=0;
            int status=AddComponent.BLANK_WORKSPACE;
            boolean add=false;
            boolean layoutPanel=false;
            
            // If workspace isn't empty
            if (jPanelWorkspace.getComponentCount() > 0) {
                
                // If a blank space on workspace hasn't been clicked
                if (component != jPanelWorkspace) {
                    
                    Component[] components = layer.getComponents();
                    int i=0,j=0;
                    // Find component n-th order
                    while (i < components.length) {
                        if (component.equals(components[i])) j=i;
                        i++;
                    }
                    
                    // If component isn't a JPanel
                    if (component.getClass().getName() != "javax.swing.JPanel")  {
                        
                        if (component.getClass().getName() == "Display.TextBox") {
                            // Component is a box
                            n=j;
                            status=AddComponent.ONTO_BOX;
                            add=true;
                        } else {
                            // Component is label
                            // If component is meant to be added onto
                            if (component.getName() != "") {
                                n=j;
                                status=AddComponent.ONTO_GRAPHIC;
                                add=true;
                            }
                        }
                    }
                    // A layer has been clicked
                    else {
                        // If layer is part of a layout component
                        if (component.getName() != "" && addComponent.getGroup(component.getName()) == 0) {
                            n=j;
                            status=AddComponent.ONTO_GRAPHIC;
                            add=true;
                            layer = (JPanel)component;
                            layoutPanel=true;
                        }
                    }
                }
                // Blank space clicked
                else {
                    // If operator, add operator to end of expression
                    if (newComponent.getGroup() == 1 || newComponent.getGroup() == 2) {
                        status=AddComponent.ONTO_BOX;
                        add=true;
                        n=layer.getComponentCount()-1;
                    }
                }
            }
            // Workspace is empty
            else {
                n=0;
                status=AddComponent.BLANK_WORKSPACE;
                add=true;
            }
            
            if (add) {
                int group = newComponent.getGroup();
                int ID = newComponent.getID();
                
                // Save current state for undo
                addComponent.saveState(true);
                
                // Component is layout
                if (group == AddComponent.LAYOUT) {
                    // Symbol is matrix
                    if (ID == 9) {
                        String[] values = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
                        boolean cancel=false;
                        String matrix_m = null;
                        String matrix_n = null;
                        matrix_m = (String)JOptionPane.showInputDialog(null, langMan.readLangFile("EnterRows"), langMan.readLangFile("MatrixDim"),JOptionPane.QUESTION_MESSAGE, null, values, "1");
                        if (matrix_m == null) {
                            cancel = true;
                        }
                        
                        if (cancel == false){
                            matrix_n = (String)JOptionPane.showInputDialog(null, langMan.readLangFile("EnterColumns"), langMan.readLangFile("MatrixDim"),JOptionPane.QUESTION_MESSAGE, null, values, "1");
                            if (matrix_n == null) {
                                cancel = true;
                            }
                        }
                        if (cancel == false) {
                            addComponent.addLayout(layer, n, newComponent, status, layoutPanel, Integer.parseInt(matrix_m), Integer.parseInt(matrix_n), null);
                        }
                    } else {
                        addComponent.addLayout(layer, n, newComponent, status, layoutPanel, 0, 0, null);
                    }
                }
                // Component is operator
                if (group == AddComponent.NARY || group == AddComponent.BINARY) {
                    addComponent.addOperator(layer, n, newComponent, status, layoutPanel, null);
                }
                // Component is function
                if (group == AddComponent.FUNCTION) {
                    addComponent.addFunction(layer, n, newComponent, status, layoutPanel, null);
                }
                // Component is symbol
                if (group == AddComponent.SYMBOL) {
                    addComponent.addSymbol(layer, n, newComponent, status);
                }
                // Component is grouping
                if (group == AddComponent.GROUPING) {
                    addComponent.addGrouping(layer, n, newComponent, status, layoutPanel, null);
                }
            }
        }
        dragging=false;
    }
    
    
    
    /** This class listens for mouse clicks on the TextBox object, and sets the cursor icon
     */
    class MouseListenerTextBox extends MouseAdapter {
        
        public MouseListenerTextBox() {
        }
        
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                motionSelectListener.clickSelect((JComponent)e.getSource());
            } else {
                addComponent(false, SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), jPanelWorkspace));
            }
        }
        
        public void mouseEntered(MouseEvent e) {
            if (dragging) {
                TextBox temp = (TextBox)e.getComponent();
                temp.setCursor(newComponent.getCursor());
                //temp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        }
        
        public void mouseExited(MouseEvent e) {
            TextBox temp = (TextBox)e.getComponent();
            temp.setCursor(Cursor.getDefaultCursor());
        }
    }
    
    
    /** Finds all the components in the display that have been selected
     * @param layer The current JPanel to look for selected components in
     * @param componentFound Boolean to say whether or not any selected components have been found,
     * set to false when first called
     */
    public void getSelection(JPanel layer, boolean componentFound) {
        Component[] components = layer.getComponents();
        int i=0;
        while (i < components.length) {
            Color colour = new Color(Color.LIGHT_GRAY.getRGB());
            
            // If components are highlighted i.e selected
            if (components[i].getBackground().equals(colour)) {
                if (componentFound == false) {
                    firstLocation = i;
                    selectionLayer = layer;
                    componentFound=true;
                }
                
                // If component is an argument panel, leave panel there and remove all components on it
                if (components[i].getClass().getName().equals("javax.swing.JPanel") && components[i].getName() == "") {
                    JPanel temp = (JPanel)components[i];
                    while (temp.getComponentCount() > 0) {
                        selectionObjects.add(temp.getComponent(0));
                    }
                    selectionLayer = temp;
                    firstLocation=0;
                } else {
                    selectionObjects.add(components[i]);
                }
            } else {
                // If component is JPanel that is not selected, search inside this JPanel
                if (components[i].getClass().getName().equals("javax.swing.JPanel")) {
                    getSelection((JPanel)components[i], componentFound);
                }
            }
            i++;
        }
    }
    
    
    /** Class that extends MouseAdapter and listens for MouseEvents on the JButtons in the toolbars
     */
    class MouseListenerPaletteToolbar extends MouseAdapter {
        
        private Point xy1;
        
        public MouseListenerPaletteToolbar() {
        }
        
        public void mousePressed(MouseEvent e) {
            xy1 = e.getPoint();
            newComponent = inputComponents[addComponent.getID(e.getComponent().getName())];
            
            dragging=true;
            
            // If workspace is empty
            if (jPanelWorkspace.getComponentCount() == 0) {
                addComponent(true, SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), jPanelWorkspace));
            } else if (newComponent.getGroup() == AddComponent.SYMBOL) {
                dragging=false;
                JComponent focusComp = (JComponent)KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                if (focusComp.getClass().getName().equals("Display.TextBox")) {
                    TextBox temp = (TextBox)focusComp;
                    JButton tmp = (JButton)e.getComponent();
                    temp.setText(temp.getText() + tmp.getText());
                }
            } else {
                // If component is not matrix or exponential
                if (newComponent.getID() != 9 && newComponent.getID() != 29) {
                    selectionObjects = addComponent.createPanel("");
                    
                    boolean selectionValid=true;
                    // Check selection is valid expression by trying to create tree from it
                    try {
                        addComponent.checkSelection(jPanelWorkspace, buildTree, newComponent);
                    } catch (ParseException ex) {
                        if (ex.getMessage().equals(("Replaced operator"))) {
                            
                        } else {
                            JOptionPane.showMessageDialog(null, langMan.readLangFile("Action") + ex.getMessage(), "DragMath", JOptionPane.ERROR_MESSAGE);
                        }
                        selectionValid=false;
                        dragging=false;
                    }
                    
                    if (selectionValid) {
                        
                        // Capture state before getSelection() may remove some components from the display
                        Tree.MathObject savedTree = null;
                        try {
                            savedTree = buildTree.generateTree(jPanelWorkspace, false, 0, 0);
                        } catch (ParseException ex) {
                        }
                        
                        getSelection(jPanelWorkspace, false);
                        
                        // If panel containting selected component isn't empty
                        if (selectionObjects.getComponentCount() > 0) {
                            
                            // Save the state previously captured
                            addComponent.saveState(savedTree);
                            
                            int group = newComponent.getGroup();
                            
                            // group is layout
                            if (group == AddComponent.LAYOUT) {
                                addComponent.addLayout(selectionLayer, firstLocation, newComponent, 3, false,0, 0, selectionObjects);
                            }
                            // group is operator
                            if (group == AddComponent.NARY || group == AddComponent.BINARY) {
                                // Minus
                                if (newComponent.getID() == 3) {
                                    
                                    // If component before minus is addition, then remove addition and treat as minus
                                    if (firstLocation > 0 && addComponent.getID(selectionLayer.getComponent(firstLocation - 1).getName()) == 2) {
                                        selectionLayer.remove(firstLocation - 1);
                                        selectionLayer.add(addComponent.createSymbol(inputComponents[3]), firstLocation - 1);
                                        addComponent.addGrouping(selectionLayer, firstLocation, inputComponents[31], 3, false, selectionObjects);
                                    } // Add unary minus
                                    else {
                                        newComponent = inputComponents[30];
                                        addComponent.addFunction(selectionLayer, firstLocation, newComponent, 3, false, selectionObjects);
                                    }
                                } else {
                                    addComponent.addOperator(selectionLayer, firstLocation, newComponent, 3, false, selectionObjects);
                                }
                            }
                            // group is function
                            if (group == AddComponent.FUNCTION) {
                                addComponent.addFunction(selectionLayer, firstLocation, newComponent, 3, false, selectionObjects);
                            }
                            // group is grouping
                            if (group == AddComponent.GROUPING) {
                                addComponent.addGrouping(selectionLayer, firstLocation, newComponent, 3, false, selectionObjects);
                            }
                            dragging=false;
                            MseSelectListener.deSelect(jPanelWorkspace);
                        }
                    }
                }
                if (dragging) {
                    setCursor(newComponent.getCursor());
                    //setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
                
            }
        }
        
        
        public void mouseReleased(MouseEvent e) {
            if (e.getX() != xy1.getX() || e.getY() != xy1.getY()) {
                addComponent(false, SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), jPanelWorkspace));
            }
        }
        
        public void mouseEntered(MouseEvent e) {
            JButton button = (JButton)e.getComponent();
            jLabelTooltip.setText(langMan.readLangFile(addComponent.getName(button.getName())));
            //.setText(inputComponents[addComponent.getID()].getTooltip());
        }
        
        public void mouseExited(MouseEvent e) {
            jLabelTooltip.setText("");
        }
    }
}