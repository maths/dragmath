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

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import Tree.*;
import java.text.ParseException;


/**
 * Class to control all editing of display, adding, deleting, copying components
 * @author Alex Billingsley
 */
public class AddComponent {
    
    private static InputComponent[] inputComponents;
    private Tree.BuildTree buildTree;
    private JPanel jPanelWorkspace;
    private MouseListener textBoxListener;
    private StatusBar statusBar;
    private LanguageManager langMan;
    private boolean implicitMult;
    private boolean keepAsDouble;
    
    private MathObject copyTree;
    private MathObject savedState;
    private MathObject[] savedStates;
    private int savedLocation;
    
    private boolean selectionComponentFound;
    private JPanel selectionLayer;
    private int firstLocation;
    private int lastLocation;
    
    public static final int LAYOUT=0;
    public static final int NARY=1;
    public static final int BINARY=2;
    public static final int FUNCTION=3;
    public static final int SYMBOL=4;
    public static final int GROUPING=5;
    
    public static final int BLANK_WORKSPACE=0;
    public static final int ONTO_BOX=1;
    public static final int ONTO_GRAPHIC=2;
    public static final int ONTO_SELECTION=3;
    
    
    /** Creates a new instance of addComponent
     * Initalises fields from the parameters
     */
    public AddComponent(InputComponent[] inputComponents, JPanel jPanelWorkspace, Tree.BuildTree buildTree, MouseListener textBoxListener, StatusBar statusBar, LanguageManager langMan, boolean implicitMult, boolean keepAsDouble) {
        this.inputComponents=inputComponents;
        this.statusBar = statusBar;
        this.jPanelWorkspace = jPanelWorkspace;
        this.buildTree = buildTree;
        this.textBoxListener=textBoxListener;
        this.langMan=langMan;
        this.implicitMult = implicitMult;
        this.keepAsDouble = keepAsDouble;
        copyTree=null;
        selectionComponentFound=false;
        savedStates = new MathObject[10];
        savedLocation=0;
    }
    
    public void setImplicitMult(boolean _implicitMult)
    {     
        implicitMult = _implicitMult;
    }
    
    
    // Returns the unique ID of the math component as an int
    public static int getID(String name) {
        int id = -1;
        try {
            int i = name.indexOf('-');
            id = Integer.parseInt(name.substring(0,i));
        } catch (NullPointerException ex) {
            // returns -1 if ID could not be obtained
        } catch (NumberFormatException ex) {
            // returns -1 if ID could not be obtained
        } catch (IndexOutOfBoundsException ex) {
            // returns -1 if ID could not be obtained
        }
        return id;
    }
    
    // Returns group the math component is in as an int
    public static int getGroup(String name) {
        int i = getID(name);
        if (i != -1) {
            return inputComponents[getID(name)].getGroup();
        } else {
            return -1;
        }
        
    }
    
    
    // Returns the unique name of the math component as a string
    public static String getName(String tag) {
        int i;
        String name = "";
        try {
            i = tag.indexOf('-');
            i++;
            name = tag.substring(i);
        } catch (NullPointerException ex) {
            // returns null if name could not be obtained
        } catch (NumberFormatException ex) {
            // returns null if name could not be obtained
        }
        return name;
    }
    
    
    // Creates a new text box and returns it
    public TextBox createBox(boolean small) {
        TextBox newBox = new TextBox();
        newBox.setBorder(new EtchedBorder());
        newBox.setHighlighter(null);
        
        if (small) {
            newBox.setFont(new java.awt.Font("Monospaced", 0, 10));
        } else {
            newBox.setFont(new java.awt.Font("Monospaced", 0, 16));
        }
        
        newBox.addMouseListener(textBoxListener);
        newBox.getDocument().addDocumentListener(new DocListener(newBox));
        newBox.addFocusListener(new FocListener(this));
        newBox.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_V && evt.isControlDown()) {
                    paste();
                }
                
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_X && evt.isControlDown()) {
                    try {
                        cut(jPanelWorkspace, buildTree);
                    } catch (ParseException ex) {
                        statusBar.println(ex.getMessage());
                    }
                }
                
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_C && evt.isControlDown()) {
                    try {
                        copy(jPanelWorkspace, buildTree);
                    } catch (ParseException ex) {
                        statusBar.println(ex.getMessage());
                    }
                }
            }
        });
        newBox.setColumns(1);
        return newBox;
    }
    
    
    // Creates a new panel and returns it
    public static JPanel createPanel(String tag) {
        JPanel newPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        newPanel.setBackground(new java.awt.Color(255, 255, 255));
        newPanel.setName(tag);
        return newPanel;
    }
    
    // Creates a new JLabel containing the characters for the notation of a math component
    // Returns the new JLabel
    public JLabel createSymbol(InputComponent newComponent) {
        
       String displayText = newComponent.getDisplayText();
       
       // If implicit mult is turned on and symbol is multiplication then display blank symbol
        if (implicitMult && newComponent.getID() == 0)
        {
            displayText = "";
        }
        
        JLabel newLabel= new JLabel(displayText, JLabel.RIGHT);
        newLabel.setFont(new Font("Lucida Sans Unicode", 0, 16));
        newLabel.setName(newComponent.getTag());
        return newLabel;
    }
    
    // Removes an operator from its current layer, and adds it to a new layer
    public static JPanel getOperator(JPanel layer, int n)  {
        JPanel newPanel = createPanel("");
        newPanel.add(layer.getComponent(n-1));
        newPanel.add(layer.getComponent(n-1));
        newPanel.add(layer.getComponent(n-1));
        return newPanel;
    }
    
    // Finds the position of a component on a panel and returns it's position as an int
    // Returns -1 if component can't be found
    public static int getComponentPosition(JPanel layer, JComponent comp) {
        int i=0;
        Component[] components = layer.getComponents();
        while (i < components.length) {
            if (comp.equals(components[i])) {
                return i;
            }
            i++;
        }
        return -1;
    }
    
    
    // Adds new operator
    public void addOperator(JPanel layer, int n, InputComponent newComponent, int status, boolean layoutPanel, JPanel argumentPanel) {
        
        // A blank workspace
        if (status==BLANK_WORKSPACE) {
            layer.add(createBox(false), n);
            layer.add(createSymbol(newComponent), n+1);
            layer.add(createBox(false), n+2);
            // Gives argument focus
            layer.getComponent(n).requestFocusInWindow();
        }
        
        // Component dropped onto box of other component
        if (status==ONTO_BOX) {
            // Add graphic symbol and second argument box to panel
            layer.add(createSymbol(newComponent), n+1);
            layer.add(createBox(false), n+2);
            // Gives argument focus
            layer.getComponent(n+2).requestFocusInWindow();
        }
        
        // Component dropped onto symbol of other component
        if (status == ONTO_GRAPHIC) {
            Component[] components = layer.getComponents();
            int oldSymbolGroup;
            if (layoutPanel) {
                oldSymbolGroup=0;
            } else {
                oldSymbolGroup = getGroup(components[n].getName());
            }
            
            // If label dropped onto is an operator
            if (oldSymbolGroup == 1 || oldSymbolGroup == 2) {
                // Add graphic symbol and second argument box to panel
                layer.add(createSymbol(newComponent), n+2);
                layer.add(createBox(false), n+3);
                // Gives argument focus
                layer.getComponent(n+3).requestFocusInWindow();
            } else {
                JPanel parent = (JPanel)layer.getParent();
                int j = getComponentPosition(parent, layer);
                // Add graphic symbol and second argument box to panel
                parent.add(createSymbol(newComponent), j+1);
                parent.add(createBox(false), j+2);
                // Gives argument focus
                parent.getComponent(j+2).requestFocusInWindow();
            }
        }
        
        if (status == ONTO_SELECTION) {
            int i=argumentPanel.getComponentCount();
            int j=0;
            while (argumentPanel.getComponentCount() > 0) {
                layer.add(argumentPanel.getComponent(0), n+j);
                j++;
            }
            // Add graphic symbol and second argument box to panel
            layer.add(createSymbol(newComponent), n+i);
            layer.add(createBox(false), n+i+1);
            // Gives argument focus
            layer.getComponent(n+i+1).requestFocusInWindow();
        }
        layer.revalidate();
    }
    
    
    // Adds a new function
    public void addFunction(JPanel layer, int n, InputComponent newComponent, int status, boolean layoutPanel, JPanel argument) {
        
        JLabel bracket1 = new JLabel("(");
        JLabel  bracket2 = new JLabel(")");
        bracket1.setName(newComponent.getTag());
        bracket2.setName(newComponent.getTag());
        
        JPanel newPanel = createPanel(newComponent.getTag());
        newPanel.add(createSymbol(newComponent));
        newPanel.add(bracket1);
        JPanel argumentPanel = null;
        
        
        // A blank workspace
        if (status == BLANK_WORKSPACE) {
            argumentPanel = createPanel("");
            argumentPanel.add(createBox(false));
            newPanel.add(argumentPanel);
            newPanel.add(bracket2);
            layer.add(newPanel, n);
            // Gives argument focus
            argumentPanel.getComponent(0).requestFocusInWindow();
        }
        
        // Component dropped onto box of other component
        if (status == ONTO_BOX) {
            argumentPanel = createPanel("");
            argumentPanel.add(layer.getComponent(n));
            newPanel.add(argumentPanel);
            newPanel.add(bracket2);
            layer.add(newPanel, n);
            // Gives argument focus
            argumentPanel.getComponent(0).requestFocusInWindow();
        }
        
        // Component dropped onto symbol of other component
        if (status == ONTO_GRAPHIC) {
            Component[] components = layer.getComponents();
            int oldSymbolGroup;
            if (layoutPanel) {
                oldSymbolGroup=0;
            } else {
                oldSymbolGroup = getGroup(components[n].getName());
            }
            
            // If label dropped onto is a binary operator
            if (oldSymbolGroup == 1 || oldSymbolGroup == 2) {
                argumentPanel = createPanel("");
                argumentPanel = getOperator(layer, n);
                newPanel.add(argumentPanel);
                newPanel.add(bracket2);
                layer.add(newPanel, n-1);
                
            } else {
                JPanel parentLayer = (JPanel)layer.getParent();
                argumentPanel = createPanel("");
                int j = getComponentPosition(parentLayer, layer);
                argumentPanel.add(layer);
                newPanel.add(argumentPanel);
                newPanel.add(bracket2);
                parentLayer.add(newPanel, j);
            }
        }
        
        if (status == ONTO_SELECTION) {
            argumentPanel = argument;
            newPanel.add(argumentPanel);
            newPanel.add(bracket2);
            layer.add(newPanel, n);
        }
        
        layer.revalidate();
        argumentPanel.addComponentListener(new ComListener(argumentPanel, 0, 1, 3));
    }
    
    
    // Adds a new grouping
    public void addGrouping(JPanel layer, int n, InputComponent newComponent, int status, boolean layoutPanel, JPanel argument){
        
        String left;
        String right;
        int i = newComponent.getDisplayText().indexOf(" ");
        left = newComponent.getDisplayText().substring(0, i);
        right = newComponent.getDisplayText().substring(i+1, i+2);
        
        JLabel bracket1 = new JLabel(left);
        JLabel bracket2 = new JLabel(right);
        
        bracket1.setName(newComponent.getTag());
        bracket2.setName(newComponent.getTag());
        
        JPanel newPanel = createPanel(newComponent.getTag());
        newPanel.add(bracket1);
        JPanel argumentPanel = null;
        
        // A blank workspace
        if (status == BLANK_WORKSPACE) {
            argumentPanel = createPanel("");
            argumentPanel.add(createBox(false));
            newPanel.add(argumentPanel);
            newPanel.add(bracket2);
            layer.add(newPanel, n);
            // Gives argument focus
            argumentPanel.getComponent(0).requestFocusInWindow();
        }
        
        // Component dropped onto box of other component
        if (status == ONTO_BOX) {
            argumentPanel = createPanel("");
            argumentPanel.add(layer.getComponent(n));
            newPanel.add(argumentPanel);
            newPanel.add(bracket2);
            layer.add(newPanel, n);
            // Gives argument focus
            argumentPanel.getComponent(0).requestFocusInWindow();
        }
        
        // Component dropped onto symbol of other component
        if (status == ONTO_GRAPHIC) {
            Component[] components = layer.getComponents();
            int oldSymbolGroup;
            if (layoutPanel) {
                oldSymbolGroup=0;
            } else {
                oldSymbolGroup = getGroup(components[n].getName());
            }
            
            // If label dropped onto is a binary operator
            if (oldSymbolGroup == 1 || oldSymbolGroup == 2) {
                argumentPanel = createPanel("");
                argumentPanel = getOperator(layer, n);
                newPanel.add(argumentPanel);
                newPanel.add(bracket2);
                layer.add(newPanel, n-1);
                
            } else {
                JPanel parentLayer = (JPanel)layer.getParent();
                argumentPanel = createPanel("");
                argumentPanel.add(layer);
                int j = getComponentPosition(parentLayer, layer);
                newPanel.add(argumentPanel);
                newPanel.add(bracket2);
                parentLayer.add(newPanel, j);
            }
        }
        
        if (status == ONTO_SELECTION) {
            argumentPanel = argument;
            newPanel.add(argumentPanel);
            newPanel.add(bracket2);
            layer.add(newPanel, n);
        }
        
        argumentPanel.addComponentListener(new ComListener(argumentPanel, 0, 0, 2));
        layer.revalidate();
    }
    
    
    // Adds a new symbol component
    public void addSymbol(JPanel layer, int n, InputComponent newComponent, int status) {
        
        // Component dropped onto box of other component
        if (status == ONTO_BOX) {
            JTextField temp = (JTextField)layer.getComponent(n);
            temp.setText(temp.getText() + newComponent.getDisplayText());
            temp.requestFocus();
        }
        layer.revalidate();
    }
    
    
    // Adds a new layout component
    public void addLayout(JPanel layer, int n, InputComponent newComponent, int status, boolean layoutPanel, int matrix_m, int matrix_n, JPanel selection) {
        
        int newSymbolID = newComponent.getID();
        
        // Create specific layout
        JPanel newPanel = new JPanel(new GridBagLayout());
        newPanel.setBackground(new java.awt.Color(255, 255, 255));
        newPanel.setName(newComponent.getTag());
        
        
        
        // If component is superscript or subscript
        if (newSymbolID == 7 || newSymbolID == 8) {
            GridBagConstraints arg = new GridBagConstraints();
            GridBagConstraints supscript = new GridBagConstraints();
            GridBagConstraints subscript = new GridBagConstraints();
            
            arg.gridx=0; subscript.gridx=1; supscript.gridx=1;
            arg.gridy=1; subscript.gridy=2; supscript.gridy=0;
            arg.gridheight=2; subscript.gridheight=2; supscript.gridheight=2;
            
            JPanel argumentPanel = createPanel("");;
            JPanel scriptPanel = createPanel("");
            scriptPanel.add(createBox(false));
            
            // Add in invisble component to get desired layout
            JTextField temp = new JTextField();
            temp.setName("");
            temp.setEditable(false);
            temp.setBorder(javax.swing.BorderFactory.createEmptyBorder());
            
            // A blank workspace
            if (status==0) {
                argumentPanel.add(createBox(false));
                if (newSymbolID == 8) {
                    newPanel.add(argumentPanel,arg);
                    newPanel.add(scriptPanel,subscript);
                    newPanel.add(temp, supscript);
                } else {
                    newPanel.add(argumentPanel,arg);
                    newPanel.add(scriptPanel,supscript);
                }
                layer.add(newPanel, n);
                // Gives argument focus
                argumentPanel.getComponent(0).requestFocusInWindow();
            }
            
            // Component dropped onto box of another component
            if (status==1) {
                argumentPanel.add(layer.getComponent(n));
                if (newSymbolID == 8) {
                    newPanel.add(argumentPanel,arg);
                    newPanel.add(scriptPanel,subscript);
                    newPanel.add(temp, supscript);
                } else {
                    newPanel.add(argumentPanel,arg);
                    newPanel.add(scriptPanel,supscript);
                }
                layer.add(newPanel, n);
                // Gives argument focus
                argumentPanel.getComponent(0).requestFocusInWindow();
            }
            
            // Component dropped onto symbol of another component
            if (status==2) {
                subscript.anchor=subscript.SOUTH;
                
                Component[] components = layer.getComponents();
                int oldSymbolGroup;
                if (layoutPanel) {
                    oldSymbolGroup=0;
                } else {
                    oldSymbolGroup = getGroup(components[n].getName());
                }
                
                
                // If label dropped onto is a binary operator
                if (oldSymbolGroup == 1 || oldSymbolGroup == 2) {
                    argumentPanel = getOperator(layer, n);
                    if (newSymbolID == 8) {
                        newPanel.add(argumentPanel,arg);
                        newPanel.add(scriptPanel,subscript);
                        newPanel.add(temp, supscript);
                    } else {
                        newPanel.add(argumentPanel,arg);
                        newPanel.add(scriptPanel,supscript);
                    }
                    layer.add(newPanel, n-1);
                } else{
                    JPanel parentLayer = (JPanel)layer.getParent();
                    int j = getComponentPosition(parentLayer, layer);
                    
                    argumentPanel.add(layer);
                    if (newSymbolID == 8) {
                        newPanel.add(argumentPanel,arg);
                        newPanel.add(scriptPanel,subscript);
                        newPanel.add(temp, supscript);
                    } else {
                        newPanel.add(argumentPanel,arg);
                        newPanel.add(scriptPanel,supscript);
                    }
                    parentLayer.add(newPanel, j);
                }
            }
            
            if (status == 3) {
                subscript.anchor=subscript.SOUTH;
                argumentPanel = selection;
                if (newSymbolID == 8) {
                    newPanel.add(argumentPanel,arg);
                    newPanel.add(scriptPanel,subscript);
                    newPanel.add(temp, supscript);
                } else {
                    newPanel.add(argumentPanel,arg);
                    newPanel.add(scriptPanel,supscript);
                }
                layer.add(newPanel, n);
            }
        }
        // Exponential
        else if (newSymbolID == 29) {
            GridBagConstraints arg = new GridBagConstraints();
            GridBagConstraints script = new GridBagConstraints();
            
            script.gridx=1; arg.gridx=0;
            script.gridy=0; arg.gridy=1;
            script.gridheight=2; arg.gridheight=2;
            
            newPanel.add(createSymbol(newComponent),arg);
            JPanel scriptPanel = createPanel("");
            
            
            // A blank workspace
            if (status==BLANK_WORKSPACE) {
                scriptPanel.add(createBox(false));
                newPanel.add(scriptPanel,script);
                layer.add(newPanel, n);
                // Gives argument focus
                scriptPanel.getComponent(0).requestFocusInWindow();
            }
            
            // Component dropped onto box of another component
            if (status==ONTO_BOX) {
                scriptPanel.add(layer.getComponent(n));
                newPanel.add(scriptPanel,script);
                layer.add(newPanel, n);
                // Gives argument focus
                scriptPanel.getComponent(0).requestFocusInWindow();
            }
        }
        // Matrix
        else if(newSymbolID == 9) {
            
            JLabel bracket1 = new JLabel("[");
            JLabel  bracket2 = new JLabel("]");
            bracket1.setName(newComponent.getTag());
            bracket2.setName(newComponent.getTag());
            
            JPanel parentLayer = createPanel(newComponent.getTag());
            JPanel matrix = new JPanel(new GridLayout(matrix_m, matrix_n, 4, 4));
            
            matrix.setBackground(new java.awt.Color(255, 255, 255));
            
            matrix.setName("");
            parentLayer.add(bracket1);
            
            int i=0;
            while (i < (matrix_m*matrix_n)) {
                JPanel box = createPanel("");
                box.add(createBox(false));
                matrix.add(box);
                i++;
            }
            
            if (status == BLANK_WORKSPACE) {
                parentLayer.add(matrix);
                parentLayer.add(bracket2);
                layer.add(parentLayer, n);
                matrix.addComponentListener(new ComListener(matrix, 0, 0, 2));
            }else if (status == ONTO_BOX) {
                layer.remove(n);
                parentLayer.add(matrix);
                parentLayer.add(bracket2);
                layer.add(parentLayer, n);
                matrix.addComponentListener(new ComListener(matrix, 0, 0 ,2));
            } else {
                JOptionPane.showMessageDialog(null, langMan.readLangFile("AddMatrix"), "DragMath", JOptionPane.ERROR_MESSAGE);
            }
        }
        
        else {
            
            GridBagConstraints arg1 = null;
            JPanel argPanel1 = createPanel("");
            
            
            // If component is a square root or n-th root
            if (newSymbolID == 5 || newSymbolID == 6) {
                GridBagConstraints nw = new GridBagConstraints();
                GridBagConstraints ne = new GridBagConstraints();
                GridBagConstraints sw = new GridBagConstraints();
                GridBagConstraints se = new GridBagConstraints();
                nw.gridx=0; ne.gridx=1; sw.gridx=0; se.gridx=1;
                nw.gridy=0; ne.gridy=0; sw.gridy=1; se.gridy=1;
                sw.anchor=sw.EAST;
                ne.fill=ne.HORIZONTAL;
                ne.anchor=ne.SOUTH;
                
                // Create bar along the top
                JSeparator bar = new JSeparator();
                bar.setForeground(new java.awt.Color(0, 0, 0));
                
                newPanel.add(createSymbol(newComponent), sw);
                newPanel.add(bar,ne);
                
                if (newSymbolID == 6) {
                    JPanel topPanel = createPanel("");
                    topPanel.add(createBox(false));
                    newPanel.add(topPanel, nw);
                }
                
                arg1 = se;
            }
            
            
            // If component is a fraction
            if (newSymbolID == 1) {
                GridBagConstraints top = new GridBagConstraints();
                GridBagConstraints mid = new GridBagConstraints();
                GridBagConstraints bot = new GridBagConstraints();
                top.insets = new Insets(0,5,0,5);
                bot.insets = new Insets(0,5,0,5);
                top.gridx=0; mid.gridx=0; bot.gridx=0;
                top.gridy=0; mid.gridy=1; bot.gridy=2;
                mid.fill=mid.HORIZONTAL;
                
                // Create math symbol
                JSeparator bar = new JSeparator();
                bar.setForeground(new java.awt.Color(0, 0, 0));
                bar.setName(newComponent.getTag());
                
                JPanel bottomPanel = createPanel("");
                bottomPanel.add(createBox(false));
                
                arg1 = top;
                
                newPanel.add(bar,mid);
                newPanel.add(bottomPanel ,bot);
            }
            
            
            // Factorial, Differential, Partial Differential, Sum, Product, Limit
            if (newSymbolID == 37 || newSymbolID == 46  || newSymbolID == 84 || newSymbolID == 54 || newSymbolID == 55 || newSymbolID == 56) {
                GridBagConstraints left = new GridBagConstraints();
                GridBagConstraints right = new GridBagConstraints();
                left.gridx=0; right.gridx=1;
                
                // Factorial
                if (newSymbolID == 37) {
                    newPanel.add(createSymbol(newComponent), right);
                    arg1 = left;
                }
                
                // Differential
                if (newSymbolID == 46) {
                    JPanel fraction = new JPanel(new GridBagLayout());
                    fraction.setBackground(new java.awt.Color(255, 255, 255));
                    fraction.setName("");
                    
                    GridBagConstraints top = new GridBagConstraints();
                    GridBagConstraints mid = new GridBagConstraints();
                    GridBagConstraints bot = new GridBagConstraints();
                    top.gridy=0; mid.gridy=1; bot.gridy=2;
                    mid.fill=mid.HORIZONTAL;
                    
                    // Create horizontal line
                    JSeparator bar = new JSeparator();
                    bar.setForeground(new java.awt.Color(0, 0, 0));
                    bar.setName("");
                    
                    JLabel newLabel = new JLabel(("d"), JLabel.RIGHT);
                    newLabel.setFont(new Font("Lucida Sans Unicode", 0, 16));
                    newLabel.setName("");
                    JLabel newLabel2 = new JLabel(("d"), JLabel.RIGHT);
                    newLabel2.setFont(new Font("Lucida Sans Unicode", 0, 16));
                    newLabel2.setName("");
                    fraction.add(newLabel, top);
                    fraction.add(bar,mid);
                    JPanel bottomPanel = createPanel("");
                    JPanel argPanel2 = createPanel("");
                    bottomPanel.add(newLabel2);
                    argPanel2.add(createBox(false));
                    bottomPanel.add(argPanel2);
                    fraction.add(bottomPanel, bot);
                    newPanel.add(fraction, left);
                    arg1=right;
                }
                
                // Partial Differential
                if (newSymbolID == 84) {
                    JPanel fraction = new JPanel(new GridBagLayout());
                    fraction.setBackground(new java.awt.Color(255, 255, 255));
                    fraction.setName("");
                    
                    GridBagConstraints top = new GridBagConstraints();
                    GridBagConstraints mid = new GridBagConstraints();
                    GridBagConstraints bot = new GridBagConstraints();
                    top.gridy=0; mid.gridy=1; bot.gridy=2;
                    mid.fill=mid.HORIZONTAL;
                    
                    // Create horizontal line
                    JSeparator bar = new JSeparator();
                    bar.setForeground(new java.awt.Color(0, 0, 0));
                    bar.setName("");
                    char partial = '\u2202';
                    JLabel newLabel = new JLabel((String.valueOf(partial)), JLabel.RIGHT);
                    newLabel.setFont(new Font("Lucida Sans Unicode", 0, 16));
                    newLabel.setName("");
                    JLabel newLabel2 = new JLabel((String.valueOf(partial)), JLabel.RIGHT);
                    newLabel2.setFont(new Font("Lucida Sans Unicode", 0, 16));
                    newLabel2.setName("");
                    fraction.add(newLabel, top);
                    fraction.add(bar,mid);
                    JPanel bottomPanel = createPanel("");
                    JPanel argPanel2 = createPanel("");
                    bottomPanel.add(newLabel2);
                    argPanel2.add(createBox(false));
                    bottomPanel.add(argPanel2);
                    fraction.add(bottomPanel, bot);
                    newPanel.add(fraction, left);
                    arg1=right;
                }
                // Limit
                if (newSymbolID == 56) {
                    GridBagConstraints top = new GridBagConstraints();
                    GridBagConstraints bot = new GridBagConstraints();
                    top.gridy=0; bot.gridy=1;
                    JPanel limit = new JPanel(new GridBagLayout());
                    limit.setBackground(new java.awt.Color(255, 255, 255));
                    limit.setName("");
                    JLabel newLabel = new JLabel(("lim"), JLabel.RIGHT);
                    newLabel.setFont(new Font("Lucida Sans Unicode", 0, 16));
                    newLabel.setName("");
                    char arrow = '\u2192';
                    JLabel newLabel2 = new JLabel((String.valueOf(arrow)), JLabel.RIGHT);
                    newLabel2.setFont(new Font("Lucida Sans Unicode", 0, 16));
                    newLabel2.setName("");
                    limit.add(newLabel, top);
                    JPanel bottomPanel = createPanel("");
                    JPanel argPanel2 = createPanel("");
                    JPanel argPanel3 = createPanel("");
                    argPanel2.add(createBox(false));
                    argPanel3.add(createBox(false));
                    bottomPanel.add(argPanel2);
                    bottomPanel.add(newLabel2);
                    bottomPanel.add(argPanel3);
                    limit.add(bottomPanel, bot);
                    left.gridheight=2;
                    newPanel.add(limit, left);
                    arg1=right;
                }
                
                // Product + Sum
                if (newSymbolID == 54 || newSymbolID == 55 ) {
                    newPanel.add(createLimitsLayout(newComponent), left);
                    arg1=right;
                }
                
            }
            
            // Integral, Definite Integral, Unknown function
            if(newSymbolID == 45 || newSymbolID == 50  || newSymbolID == 53) {
                
                GridBagConstraints pos1 = new GridBagConstraints();
                GridBagConstraints pos2 = new GridBagConstraints();
                GridBagConstraints pos3 = new GridBagConstraints();
                GridBagConstraints pos4 = new GridBagConstraints();
                pos1.gridx=0; pos2.gridx=1; pos3.gridx=2; pos4.gridx=3;
                
                JPanel argPanel2 = createPanel("");
                argPanel2.add(createBox(false));
                
                JLabel bracket1 = new JLabel("(");
                JLabel bracket2 = new JLabel(")");
                
                // Integration
                if (newSymbolID == 45) {
                    JLabel newLabel= new JLabel(newComponent.getDisplayText(), JLabel.RIGHT);
                    newLabel.setFont(new Font("Lucida Sans Unicode", 0, 25));
                    newLabel.setName("");
                    newPanel.add(newLabel,pos1);
                }
                
                // Definite Integral
                if (newSymbolID == 53) {
                    JPanel symbolPanel = new JPanel(new GridBagLayout());
                    symbolPanel.setBackground(new java.awt.Color(255, 255, 255));
                    symbolPanel.setName("");
                    
                    GridBagConstraints top = new GridBagConstraints();
                    GridBagConstraints mid = new GridBagConstraints();
                    GridBagConstraints bot = new GridBagConstraints();
                    top.gridy=0; mid.gridy=1; bot.gridy=2;
                    
                    JPanel argPanel3 = createPanel("");
                    JPanel argPanel4 = createPanel("");
                    argPanel3.add(createBox(false));
                    argPanel4.add(createBox(false));
                    symbolPanel.add(argPanel3, top);
                    
                    JLabel newLabel= new JLabel(newComponent.getDisplayText(), JLabel.RIGHT);
                    newLabel.setFont(new Font("Lucida Sans Unicode", 0, 25));
                    newLabel.setName("");
                    
                    symbolPanel.add(newLabel, mid);
                    symbolPanel.add(argPanel4, bot);
                    
                    newPanel.add(symbolPanel, pos1);
                }
                
                if (newSymbolID == 45  || newSymbolID == 53) {
                    JLabel newLabel= new JLabel(("d"), JLabel.RIGHT);
                    newLabel.setFont(new Font("Lucida Sans Unicode", 0, 16));
                    newLabel.setName(newComponent.getTag());
                    newPanel.add(newLabel,pos3);
                    newPanel.add(argPanel2,pos4);
                    arg1 = pos2;
                }
                
                // Unknown function
                if (newSymbolID == 50) {
                    bracket1.setName(newComponent.getTag());
                    bracket2.setName(newComponent.getTag());
                    newPanel.add(argPanel2, pos1);
                    newPanel.add(bracket1, pos2);
                    newPanel.add(bracket2, pos4);
                    arg1=pos3;
                }
                
            }
            
            // Evaluate at a point
            if (newSymbolID == 57) {
                GridBagConstraints arg = new GridBagConstraints();
                GridBagConstraints mid = new GridBagConstraints();
                GridBagConstraints subscript = new GridBagConstraints();
                
                arg.gridx=0; subscript.gridx=2; mid.gridx=1;
                arg.gridy=0; subscript.gridy=1; mid.gridy=0;
                mid.gridheight=2;
                mid.insets = new Insets(0,3,0,3);
                mid.fill=mid.VERTICAL;
                
                // Create vertical bar
                JSeparator bar = new JSeparator();
                bar.setOrientation(SwingConstants.VERTICAL);
                bar.setForeground(new java.awt.Color(0, 0, 0));
                bar.setName(newComponent.getTag());
                newPanel.add(bar,mid);
                
                JPanel argumentPanel = createPanel("");;
                JPanel scriptPanel = createPanel("");
                JPanel argumentPanel1 = createPanel("");
                JPanel argumentPanel2 = createPanel("");
                argumentPanel1.add(createBox(false));
                argumentPanel2.add(createBox(false));
                
                JLabel newLabel = new JLabel(("="), JLabel.RIGHT);
                newLabel.setFont(new Font("Lucida Sans Unicode", 0, 16));
                newLabel.setName("");
                
                scriptPanel.add(argumentPanel1);
                scriptPanel.add(newLabel);
                scriptPanel.add(argumentPanel2);
                newPanel.add(scriptPanel,subscript);
                
                arg1= arg;
            }
            
            // A blank workspace
            if (status == BLANK_WORKSPACE) {
                argPanel1.add(createBox(false));
                newPanel.add(argPanel1, arg1);
                layer.add(newPanel,n);
                argPanel1.getComponent(0).requestFocusInWindow();
            }
            
            
            // Component dropped onto box of another component
            if (status == ONTO_BOX) {
                argPanel1.add(layer.getComponent(n));
                newPanel.add(argPanel1, arg1);
                layer.add(newPanel,n);
                argPanel1.getComponent(0).requestFocusInWindow();
            }
            
            // Component dropped onto symbol of another component
            if (status==ONTO_GRAPHIC) {
                Component[] components = layer.getComponents();
                int oldSymbolGroup;
                if (layoutPanel) {
                    oldSymbolGroup=0;
                } else {
                    oldSymbolGroup = getGroup(components[n].getName());
                }
                
                // If label dropped onto is a operator
                if (oldSymbolGroup == 1 || oldSymbolGroup == 2) {
                    argPanel1 = getOperator(layer, n);
                    newPanel.add(argPanel1, arg1);
                    layer.add(newPanel, n-1);
                } else {
                    JPanel parentLayer = (JPanel)layer.getParent();
                    int j = getComponentPosition(parentLayer, layer);
                    argPanel1.add(layer);
                    newPanel.add(argPanel1, arg1);
                    parentLayer.add(newPanel, j);
                }
            }
            
            if (status == ONTO_SELECTION) {
                argPanel1 = selection;
                newPanel.add(argPanel1, arg1);
                layer.add(newPanel, n);
            }
            
            // square root or n-th root
            if (newSymbolID == 5 || newSymbolID == 6) {
                argPanel1.addComponentListener(new ComListener(argPanel1, 1, 0, 0));
            }
            // integral
            if (newSymbolID == 45){
                //argPanel1.addComponentListener(new ComListener(argPanel1, 1, 0, 0));
            }
            // def. integral
            if (newSymbolID == 53){
                //argPanel1.addComponentListener(new ComListener(argPanel1, 2, 0, 0));
            }
            // unknown function
            if (newSymbolID == 50){
                argPanel1.addComponentListener(new ComListener(argPanel1, 0, 1, 2));
            }
            
        }
        
        layer.revalidate();
    }
    
    
    public JPanel createLimitsLayout(InputComponent newComponent) {
        JPanel symbolPanel = new JPanel(new GridBagLayout());
        symbolPanel.setBackground(new java.awt.Color(255, 255, 255));
        symbolPanel.setName("");
        
        GridBagConstraints top = new GridBagConstraints();
        GridBagConstraints mid = new GridBagConstraints();
        GridBagConstraints bot = new GridBagConstraints();
        top.gridy=0; mid.gridy=1; bot.gridy=2;
        
        JPanel argPanel2 = createPanel("");
        JPanel argPanel3 = createPanel("");
        JPanel argPanel4 = createPanel("");
        JPanel bottomPanel = createPanel("");
        argPanel2.add(createBox(false));
        argPanel3.add(createBox(false));
        argPanel4.add(createBox(false));
        symbolPanel.add(argPanel2, top);
        bottomPanel.add(argPanel3);
        
        JLabel equalSign = new JLabel("=", JLabel.RIGHT);
        equalSign.setFont(new Font("Lucida Sans Unicode", 0, 16));
        equalSign.setName("");
        
        bottomPanel.add(equalSign);
        bottomPanel.add(argPanel4);
        
        JLabel newLabel= new JLabel(newComponent.getDisplayText(), JLabel.RIGHT);
        newLabel.setFont(new Font("Lucida Sans Unicode", 0, 25));
        newLabel.setName("");
        
        symbolPanel.add(newLabel, mid);
        symbolPanel.add(bottomPanel, bot);
        return symbolPanel;
    }
    
    
// Takes tree structure and adds components onto the display
    public void pasteTree(JPanel layer, int n, MathObject start, int status) {
        
        if (start != null) {
            
            if (start.getClass().getName().equals("Tree.Text")) {
                Text textObj = (Text)start;
                layer.add(createBox(false), n);
                JTextField temp = (JTextField)layer.getComponent(n);
                temp.setText(textObj.getText());
                
                if (temp.getText().length() > 0) {
                    temp.setBorder(new EmptyBorder(temp.getInsets()));
                }
            }
            
            if (start.getClass().getName().equals("Tree.Variable")) {
                Variable variableObj = (Variable)start;
                layer.add(createBox(false), n);
                JTextField temp = (JTextField)layer.getComponent(n);
                temp.setText(String.valueOf(variableObj.getVarName()));
                if (temp.getText().length() > 0) {
                    temp.setBorder(new EmptyBorder(temp.getInsets()));
                }
            }
            
            if (start.getClass().getName().equals("Tree.RealNumber")) {
                RealNumber numberObj = (RealNumber)start;
                layer.add(createBox(false), n);
                JTextField temp = (JTextField)layer.getComponent(n);
                // Gets number as double or int depending on user settings
                temp.setText(numberObj.getNumber(keepAsDouble));
                if (temp.getText().length() > 0) {
                    temp.setBorder(new EmptyBorder(temp.getInsets()));
                }
            }
            
            if (start.getClass().getName().equals("Tree.BinaryOperator")) {
                BinaryOperator binaryObj = (BinaryOperator)start;
                
                // Decide if brackets are required using precedence table and location in the tree
                boolean brackets = false;
                // If parent is not fraction - as precedence is explicit in display
                if (binaryObj.getParent() != null && binaryObj.getParent().getID() != 1) {
                    // If precedence is lower
                    if (Precedence.value[binaryObj.getID()] < Precedence.value[binaryObj.getParent().getID()]) {
                        brackets=true;
                    }
                }
                if (brackets) {
                    Grouping newBrackets = new Grouping(31, "BracketsRnd");
                    newBrackets.setChild(binaryObj);
                    binaryObj.setParent(newBrackets);
                    pasteTree(layer, n, newBrackets, 0);
                    
                } else {
                    
                    JPanel tempArgument = null;
                    
                    int first = -1;
                    int second = -1;
                    // Fraction
                    if (binaryObj.getID() == 1) {
                        first=2;
                        second=1;
                    }
                    // Unknown function
                    if (binaryObj.getID() == 50) {
                        first=0;
                        second=3;
                    }
                    
                    // Superscript or Subscript
                    if (binaryObj.getID() == 7 || binaryObj.getID() == 8) {
                        first=0;
                        second=1;
                    }
                    // N-th root
                    if (binaryObj.getID() == 6) {
                        first=3;
                        second=2;
                    }
                    
                    if (first != -1) {
                        addLayout(layer, n, inputComponents[binaryObj.getID()], status, false, 0, 0, null);
                        JPanel tempLayer = (JPanel)layer.getComponent(n);
                        tempArgument = (JPanel)tempLayer.getComponent(first);
                        tempArgument.remove(0);
                        pasteTree(tempArgument, 0, binaryObj.getLeftChild(), 0);
                        tempArgument = (JPanel)tempLayer.getComponent(second);
                        tempArgument.remove(0);
                        pasteTree(tempArgument, 0, binaryObj.getRightChild(), 0);
                    } else {
                        addOperator(layer, n, inputComponents[binaryObj.getID()], status, false, null);
                        JComponent temp = (JComponent)layer.getComponent(n+2);
                        layer.remove(n);
                        pasteTree(layer, n, binaryObj.getLeftChild(), 0);
                        
                        int i = getComponentPosition(layer, temp);
                        layer.remove(i);
                        pasteTree(layer, i, binaryObj.getRightChild(), 0);
                    }
                }
            }
            
            if (start.getClass().getName().equals("Tree.NaryFunction")) {
                NaryFunction naryFunctionObj = (NaryFunction)start;
                // Product + Sum
                if (naryFunctionObj.getID() == 54 || naryFunctionObj.getID() == 55) {
                    addLayout(layer, n, inputComponents[naryFunctionObj.getID()], status, false, 0, 0, null);
                    JPanel tempLayer = (JPanel)layer.getComponent(n);
                    
                    JPanel tempArgument = (JPanel)tempLayer.getComponent(1);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, naryFunctionObj.getChild(0), 0);
                    
                    tempArgument = (JPanel)tempLayer.getComponent(0);
                    tempArgument = (JPanel)tempArgument.getComponent(2);
                    tempArgument = (JPanel)tempArgument.getComponent(0);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, naryFunctionObj.getChild(1), 0);
                    
                    tempArgument = (JPanel)tempLayer.getComponent(0);
                    tempArgument = (JPanel)tempArgument.getComponent(2);
                    tempArgument = (JPanel)tempArgument.getComponent(2);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, naryFunctionObj.getChild(2), 0);
                    
                    tempArgument = (JPanel)tempLayer.getComponent(0);
                    tempArgument = (JPanel)tempArgument.getComponent(0);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, naryFunctionObj.getChild(3), 0);
                }
                // Definite Integral
                if (naryFunctionObj.getID() == 53) {
                    addLayout(layer, n, inputComponents[naryFunctionObj.getID()], status, false, 0, 0, null);
                    JPanel tempLayer = (JPanel)layer.getComponent(n);
                    
                    JPanel tempArgument = (JPanel)tempLayer.getComponent(3);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, naryFunctionObj.getChild(0), 0);
                    
                    tempArgument = (JPanel)tempLayer.getComponent(2);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, naryFunctionObj.getChild(1), 0);
                    
                    tempArgument = (JPanel)tempLayer.getComponent(0);
                    tempArgument = (JPanel)tempArgument.getComponent(2);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, naryFunctionObj.getChild(2), 0);
                    
                    tempArgument = (JPanel)tempLayer.getComponent(0);
                    tempArgument = (JPanel)tempArgument.getComponent(0);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, naryFunctionObj.getChild(3), 0);
                }
                // Limit
                if (naryFunctionObj.getID() == 56) {
                    addLayout(layer, n, inputComponents[naryFunctionObj.getID()], status, false, 0, 0, null);
                    JPanel tempLayer = (JPanel)layer.getComponent(n);
                    
                    JPanel tempArgument = (JPanel)tempLayer.getComponent(1);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, naryFunctionObj.getChild(0), 0);
                    
                    tempArgument = (JPanel)tempLayer.getComponent(0);
                    tempArgument = (JPanel)tempArgument.getComponent(1);
                    tempArgument = (JPanel)tempArgument.getComponent(0);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, naryFunctionObj.getChild(1), 0);
                    
                    tempArgument = (JPanel)tempLayer.getComponent(0);
                    tempArgument = (JPanel)tempArgument.getComponent(1);
                    tempArgument = (JPanel)tempArgument.getComponent(2);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, naryFunctionObj.getChild(2), 0);
                }
                // Evaluate
                if (naryFunctionObj.getID() == 57) {
                    addLayout(layer, n, inputComponents[naryFunctionObj.getID()], status, false, 0, 0, null);
                    JPanel tempLayer = (JPanel)layer.getComponent(n);
                    
                    JPanel tempArgument = (JPanel)tempLayer.getComponent(2);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, naryFunctionObj.getChild(0), 0);
                    
                    tempArgument = (JPanel)tempLayer.getComponent(1);
                    tempArgument = (JPanel)tempArgument.getComponent(0);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, naryFunctionObj.getChild(1), 0);
                    
                    tempArgument = (JPanel)tempLayer.getComponent(1);
                    tempArgument = (JPanel)tempArgument.getComponent(2);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, naryFunctionObj.getChild(2), 0);
                }
                // Integral
                if (naryFunctionObj.getID() == 45) {
                    addLayout(layer, n, inputComponents[naryFunctionObj.getID()], status, false, 0, 0, null);
                    JPanel tempLayer = (JPanel)layer.getComponent(n);
                    JPanel tempArgument = (JPanel)tempLayer.getComponent(3);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, naryFunctionObj.getChild(0), 0);
                    tempArgument = (JPanel)tempLayer.getComponent(2);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, naryFunctionObj.getChild(1), 0);
                }
                // Differential, Partial Differential
                if (naryFunctionObj.getID() == 46 || naryFunctionObj.getID() == 84) {
                    addLayout(layer, n, inputComponents[naryFunctionObj.getID()], status, false, 0, 0, null);
                    JPanel tempLayer = (JPanel)layer.getComponent(n);
                    JPanel tempArgument = (JPanel)tempLayer.getComponent(1);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, naryFunctionObj.getChild(0), 0);
                    tempArgument = (JPanel)tempLayer.getComponent(0);
                    tempArgument = (JPanel)tempArgument.getComponent(2);
                    tempArgument = (JPanel)tempArgument.getComponent(1);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, naryFunctionObj.getChild(1), 0);
                }
            }
            
            
            if (start.getClass().getName().equals("Tree.Function")) {
                Function functionObj = (Function)start;
                // Square root
                if (functionObj.getID() == 5) {
                    addLayout(layer, n, inputComponents[functionObj.getID()], status, false, 0, 0, null);
                    JPanel tempLayer = (JPanel)layer.getComponent(n);
                    JPanel tempArgument = (JPanel)tempLayer.getComponent(2);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, functionObj.getChild(), 0);
                    // Exponential
                } else  if (functionObj.getID() == 29) {
                    addLayout(layer, n, inputComponents[functionObj.getID()], status, false, 0, 0, null);
                    JPanel tempLayer = (JPanel)layer.getComponent(n);
                    JPanel tempArgument = (JPanel)tempLayer.getComponent(1);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, functionObj.getChild(), 0);
                    // Factorial
                } else  if (functionObj.getID() == 37) {
                    addLayout(layer, n, inputComponents[functionObj.getID()], status, false, 0, 0, null);
                    JPanel tempLayer = (JPanel)layer.getComponent(n);
                    JPanel tempArgument = (JPanel)tempLayer.getComponent(1);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, functionObj.getChild(), 0);
                } else {
                    addFunction(layer, n, inputComponents[functionObj.getID()], status, false, null);
                    JPanel tempLayer = (JPanel)layer.getComponent(n);
                    JPanel tempArgument = (JPanel)tempLayer.getComponent(2);
                    tempArgument.remove(0);
                    pasteTree(tempArgument, 0, functionObj.getChild(), 0);
                }
            }
            
            if (start.getClass().getName().equals("Tree.Matrix")) {
                Matrix matrixObj = (Matrix)start;
                
                int matrix_m = matrixObj.getM();
                int matrix_n = matrixObj.getN();
                addLayout(layer, n, inputComponents[matrixObj.getID()], status, false, matrix_m, matrix_n, null);
                JPanel tempLayer = (JPanel)layer.getComponent(n);
                JPanel argumentLayer = (JPanel)tempLayer.getComponent(1);
                Component[] components = argumentLayer.getComponents();
                
                MathObject[][] array = matrixObj.getArray();
                
                int x=0; int y=0;
                while (y < matrix_m) {
                    while (x < matrix_n) {
                        int i = x + (y*matrix_n);
                        JPanel temp = (JPanel)components[i];
                        temp.remove(0);
                        pasteTree(temp, 0, matrixObj.getElement(y,x), 0);
                        x++;
                    }
                    x=0;
                    y++;
                }
            }
            
            if (start.getClass().getName().equals("Tree.Grouping")) {
                Grouping groupingObj = (Grouping)start;
                addGrouping(layer, n, inputComponents[groupingObj.getID()], status, false, null);
                JPanel tempLayer = (JPanel)layer.getComponent(n);
                JPanel tempArgument = (JPanel)tempLayer.getComponent(1);
                tempArgument.remove(0);
                pasteTree(tempArgument, 0, groupingObj.getChild(), 0);
            }
            
            if (start.getClass().getName().equals("Tree.NaryOperator")) {
                NaryOperator naryObj = (NaryOperator)start;
                
                // Decide if brackets are required using precedence table and location in the tree
                boolean brackets = false;
                // If parent is not fraction - as precedence is explicit in display
                if (naryObj.getParent() != null  && naryObj.getParent().getID() != 1) {
                    // If precedence is lower
                    if (Precedence.value[naryObj.getID()] < Precedence.value[naryObj.getParent().getID()]) {
                        brackets=true;
                    }
                }
                if (brackets) {
                    Grouping newBrackets = new Grouping(31, "BracketsRnd");
                    newBrackets.setChild(naryObj);
                    naryObj.setParent(newBrackets);
                    pasteTree(layer, n, newBrackets, 0);
                    
                } else {
                    
                    addOperator(layer, n, inputComponents[naryObj.getID()], status, false, null);
                    int i = naryObj.getSize()-2;
                    int j=n;
                    while (i > 0) {
                        j=j+2;
                        addOperator(layer, j, inputComponents[naryObj.getID()], 1, false, null);
                        i--;
                    }
                    
                    i = naryObj.getSize();
                    JComponent temp = null;
                    while (i > 1) {
                        temp = (JComponent)layer.getComponent(n+2);
                        layer.remove(n);
                        pasteTree(layer, n, naryObj.getChild(i-1), 0);
                        n = getComponentPosition(layer, temp);
                        i--;
                    }
                    layer.remove(n);
                    pasteTree(layer, n, naryObj.getChild(i-1), 0);
                }
            }
        }
        layer.repaint();
        layer.revalidate();
    }
    
    
    
// Saves current state, then calls remove() to remove components
    public void delete(JPanel layer) {
        saveState(true);
        remove(layer);
    }
    
    
// Removes selected components from layer
    public void remove(JPanel layer) {
        
        //Component[] components = layer.getComponents();
        int i=0;
        boolean last = false;
        while (i < layer.getComponentCount()) {
            
            if (layer.getComponent(i).getBackground().equals(MseSelectListener.SELECT)) {
                
                // Last component on layer
                if (i == layer.getComponentCount()-1) {
                    if (i > 0 && layer.getComponent(i-1).getBackground() != MseSelectListener.SELECT) {
                        if (layer.getComponent(i-1).getClass().getName().equals("javax.swing.JLabel")) {
                            last=true;
                        }
                    }
                } else {
                    // If next component has white background, then this is the end of the selection
                    if (layer.getComponent(i+1).getBackground() != MseSelectListener.SELECT) {
                        
                        // First component on layer, check if next component is a label
                        if (i==0) {
                            if (layer.getComponent(i+1).getClass().getName().equals("javax.swing.JLabel")) {
                                last=true;
                            }
                        } else {
                            // If component is 0 < x < n
                            // Check if panel/box either side
                            if ((layer.getComponent(i-1).getClass().getName().equals("javax.swing.JPanel") ||
                                    layer.getComponent(i-1).getClass().getName().equals("Display.TextBox")) &&
                                    (layer.getComponent(i+1).getClass().getName().equals("javax.swing.JPanel") ||
                                    layer.getComponent(i+1).getClass().getName().equals("Display.TextBox"))) {
                                last=true;
                            }
                            // Check if either side is labels
                            if (layer.getComponent(i-1).getClass().getName().equals("javax.swing.JLabel") &&
                                    layer.getComponent(i+1).getClass().getName().equals("javax.swing.JLabel")) {
                                last=true;
                            }
                        }
                    }
                }
                
                if (layer.getComponent(i).getClass().getName().equals("javax.swing.JPanel")) {
                    // Argument panel
                    if (layer.getComponent(i).getName().equals("")) {
                        JPanel temp = (JPanel)layer.getComponent(i);
                        temp.removeAll();
                        temp.add(createBox(false));
                    } else if (last) {
                        last=false;
                        layer.remove(i);
                        layer.add(createBox(false), i);
                        i=-1;
                    } else {
                        layer.remove(i);
                        if (layer.getComponentCount() == 0 && layer != jPanelWorkspace) {
                            layer.add(createBox(false));
                        }
                        i=-1;
                    }
                } else if (layer.getComponent(i).getClass().getName().equals("javax.swing.JLabel")) {
                    if (last == false) {
                        layer.remove(i);
                        i=-1;
                    }
                    if (last) {
                        last=false;
                        JLabel temp = (JLabel)layer.getComponent(i);
                        temp.setOpaque(false);
                        temp.setBackground(MseSelectListener.DESELECT);
                    }
                }
                // TextBox
                else {
                    layer.remove(i);
                    if (last) {
                        layer.add(createBox(false),i);
                        last=false;
                    }
                    i=-1;
                }
                
            } else {
                if (layer.getComponent(i).getClass().getName().equals("javax.swing.JPanel")) {
                    remove((JPanel)layer.getComponent(i));
                }
            }
            i++;
        }
        layer.repaint();
        layer.revalidate();
    }
    
    
    public void cut(JPanel layer, Tree.BuildTree buildTree) throws ParseException  {
        selectionComponentFound = false;
        getSelection(layer);
        copyTree=null;
        if (selectionComponentFound) {
            copyTree = buildTree.generateTree(selectionLayer, true, firstLocation, lastLocation);
            statusBar.println("Cut");
            delete(layer);
        } else {
            statusBar.println(langMan.readLangFile("NoCut"));
        }
    }
    
    public void getSelection(JPanel layer) {
        Component[] components = layer.getComponents();
        int i=0;
        while (i < components.length) {
            Color colour = new Color(Color.LIGHT_GRAY.getRGB());
            
            if (components[i].getBackground().equals(colour)) {
                if (selectionComponentFound == false) {
                    firstLocation = i;
                    selectionLayer = (JPanel)components[i].getParent();
                    selectionComponentFound=true;
                }
                lastLocation=i;
            } else {
                if (components[i].getClass().getName().equals("javax.swing.JPanel")) {
                    getSelection((JPanel)components[i]);
                }
            }
            i++;
        }
    }
    
    public void paste() {
        JComponent focusComp = (JComponent)KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (focusComp.getClass().getName().equals("Display.TextBox")) {
            if (copyTree != null) {
                saveState(true);
                JPanel parent = (JPanel)focusComp.getParent();
                int i = getComponentPosition(parent, focusComp);
                parent.remove(focusComp);
                pasteTree(parent, i, copyTree, 0);
            } else {
                statusBar.println(langMan.readLangFile("NoPaste"));
            }
        } else {
            statusBar.println(langMan.readLangFile("NoBox"));
        }
    }
    
    
    public void copy(JPanel layer, Tree.BuildTree buildTree) throws ParseException {
        selectionComponentFound = false;
        copyTree=null;
        getSelection(layer);
        if (selectionComponentFound) {
            copyTree = buildTree.generateTree(selectionLayer, true, firstLocation, lastLocation);
            statusBar.println(langMan.readLangFile("Copied"));
            MseSelectListener.deSelect(jPanelWorkspace);
        } else {
            statusBar.println(langMan.readLangFile("NoCopy"));
        }
    }
    
    
    public MathObject checkSelection(JPanel layer, Tree.BuildTree buildTree, Display.InputComponent newComponent) throws ParseException {
        selectionComponentFound = false;
        MathObject tree = null;
        getSelection(layer);
        if (selectionComponentFound) {
            try {
                tree = buildTree.generateTree(selectionLayer, true, firstLocation, lastLocation);
            } catch (ParseException err) {
                int i = newComponent.getGroup();
                if (i == 1 || i == 2) {
                    selectionLayer.remove(firstLocation);
                    selectionLayer.add(createSymbol(newComponent), firstLocation);
                    selectionLayer.revalidate();
                    throw new ParseException("Replaced operator", 0);
                } else {
                    throw new ParseException(err.getMessage(), err.getErrorOffset());
                }
            }
        }
        return tree;
    }
    
    public void saveState(MathObject tree) {
        savedStates[savedLocation] = tree;
        if (savedLocation==4) {
            deleteSavedState();
        } else {
            savedLocation++;
        }
        int i = savedLocation;
        while (i < 5) {
            savedStates[i] = null;
            i++;
        }
    }
    
    public void saveState(boolean clearRedo) {
        try {
            savedStates[savedLocation] = buildTree.generateTree(jPanelWorkspace, false, 0, 0);
            if (savedLocation==4) {
                deleteSavedState();
            } else {
                savedLocation++;
            }
            if (clearRedo) {
                int i = savedLocation;
                while (i < 5) {
                    savedStates[i] = null;
                    i++;
                }
            }
        } catch (ParseException ex) {
        }
    }
    
    public void deleteSavedState() {
        int i=0;
        while (i < 4) {
            savedStates[i] = savedStates[i+1];
            i++;
        }
        savedStates[4] = null;
    }
    
    public void undoState() {
        if (savedLocation > 0) {
            saveState(false);
            savedLocation--;
            jPanelWorkspace.removeAll();
            jPanelWorkspace.repaint();
            savedLocation--;
            pasteTree(jPanelWorkspace, 0, savedStates[savedLocation], 0);
        }
    }
    
    public void redoState() {
        if (savedLocation < 4 && savedStates[savedLocation+1] != null) {
            jPanelWorkspace.removeAll();
            jPanelWorkspace.repaint();
            savedLocation++;
            pasteTree(jPanelWorkspace, 0, savedStates[savedLocation], 0);
        }
    }
    
    public void resetUndoRedo() {
        savedLocation=0;
        int i = 0;
        while (i < 5) {
            savedStates[i] = null;
            i++;
        }
    }
    
}


