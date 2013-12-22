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

package Tree;

import Display.*;
import java.text.ParseException;
import java.util.Stack;
import javax.swing.*;
import java.awt.*;
import org.nfunk.jep.*;
import org.nfunk.jep.function.*;
import Display.LanguageManager;

/**
 * Class to create tree structure of math expression, from the display or from a string
 * @author Alex Billingsley
 */
public class BuildTree {
    
    private static org.jdom.Element inpComps;
    private int copyStart;
    private int copyFinish;
    private JPanel copyLayer;
    private boolean copy;
    private LanguageManager langMan;
    
    /** Creates a new instance of BuildTree */
    public BuildTree(LanguageManager langMan, org.jdom.Element inpComps) {
        this.langMan=langMan;
        this.inpComps=inpComps;
    }
    
    /** Creates tree from display and returns root node of tree
     * @param layer the JPanel of the layer to start creating the tree from
     * @param copy boolean to state whether or not the tree is being created for a selection that the user is copying
     * @param copyStart int to state which component to start at in the layer
     * @param copyFinish into to state which component to stop at in the layer
     * @return A <code>MathObject</code> that is the root node of the tree
     * @throws ParseException If an error occurs during the parsing of the display
     */
    public MathObject generateTree(JPanel layer, boolean copy, int copyStart, int copyFinish) throws java.text.ParseException {
        this.copy=copy;
        if (copy) {
            this.copyStart = copyStart;
            this.copyFinish = copyFinish;
            this.copyLayer = layer;
        }
        Stack outputStack = toPostfix(layer, new Stack(), new Stack());
        MathObject tree;
        if (outputStack.empty() != true) {
            tree = (MathObject)outputStack.pop();
            toTree(tree, outputStack);
        } else {
            tree = null;
        }
        return tree;
    }
    
    
    
    /** Creates a postfix expression from the display
     * @param layer the layer in the display to start creating the postfix expression from
     * @param operatorStack a stack for storing operators in during parsing
     * @param outStack a stack for storing the postfix expression in
     * @return A Stack containing the postfix expression
     * @throws ParseException if an error occurs during parsing
     */
    public Stack toPostfix(Container layer, Stack operatorStack, Stack outStack) throws java.text.ParseException {
        int lastAdded=0;
        // 0 - Nothing
        // 1 - Operator
        // 2 - Panel/TextBox
        
        Component[] components = layer.getComponents();
        int i = 0;
        int x = components.length;
        
        if (x > 0) {
            
            if (copy && layer==copyLayer) {
                i = copyStart;
                x = copyFinish+1;
            }
            
            while (i < x) {
                
                // If component is a text box
                if (components[i].getClass().getName().equals("Display.TextBox")) {
                    JTextField temp = (JTextField)components[i];
                    
                    if (lastAdded == 2) {
                        throw new java.text.ParseException(langMan.readLangFile("Operator") + i, i);
                    }
                    Stack textBoxStack;
                    try {
                        textBoxStack = parseString(temp.getText(), new Stack());
                        int j=0;
                        while (j < textBoxStack.size()) {
                            outStack.push(textBoxStack.get(j));
                            j++;
                        }
                    } catch (org.nfunk.jep.ParseException ex) {
                        outStack.push(new Text(temp.getText()));
                    }
                    lastAdded=2;
                }
                // If component is an operator
                else  if (components[i].getClass().getName().equals("javax.swing.JLabel")) {
                    
                    if (lastAdded != 2) {
                        throw new java.text.ParseException(langMan.readLangFile("Argument") + i, i);
                    }
                    
                    lastAdded=1;
                    
                    int ID = AddComponent.getID(components[i].getName());
                    int group = AddComponent.getGroup(components[i].getName());
                    String name = AddComponent.getName(components[i].getName());
                    
                    MathObject temp = null;
                    if (operatorStack.empty() == false) {
                        temp = (MathObject)operatorStack.peek();
                    }
                    
                    while(operatorStack.empty() == false && Precedence.value[ID] <= Precedence.value[temp.getID()]) {
                        outStack.push(operatorStack.pop());
                        if (operatorStack.empty() == false) {
                            temp = (MathObject)operatorStack.peek();
                        }
                    }
                    
                    // N-ary
                    if (group == 1) {
                        operatorStack.push(new NaryOperator(ID, name));
                    }
                    // Binary
                    else {
                        operatorStack.push(new BinaryOperator(ID, name));
                    }
                }
                // If component is a panel
                else  if (components[i].getClass().getName().equals("javax.swing.JPanel")) {
                    
                    if (components[i].getName() != "") {
                        
                        int ID = AddComponent.getID(components[i].getName());
                        int group = AddComponent.getGroup(components[i].getName());
                        String name = AddComponent.getName(components[i].getName());
                        
                        lastAdded=2;
                        
                        // Function
                        if (group == AddComponent.FUNCTION) {
                            Container functionLayer = (Container)components[i];
                            Component[] functionComponents = functionLayer.getComponents();
                            Stack functionOutStack = toPostfix((Container)functionComponents[2], new Stack(), new Stack());
                            outStack.push(functionOutStack);
                            outStack.push(new Function(ID, name));
                        }
                        // Grouping
                        if (group == AddComponent.GROUPING) {
                            Container groupLayer = (Container)components[i];
                            Component[] groupComponents = groupLayer.getComponents();
                            Stack groupOutStack = toPostfix((Container)groupComponents[1], new Stack(), new Stack());
                            outStack.push(groupOutStack);
                            outStack.push(new Grouping(ID, name));
                        }
                        // Layout
                        if (group == AddComponent.LAYOUT) {
                            Container layoutLayer = (Container)components[i];
                            Component[] layoutComponents = layoutLayer.getComponents();
                            
                            // Matrix
                            if (ID == 9) {
                                JPanel matrix = (JPanel)layoutComponents[1];
                                GridLayout layout = (GridLayout)matrix.getLayout();
                                Component[] matrixElements = matrix.getComponents();
                                int j=0;
                                while (j < matrixElements.length) {
                                    outStack.push(toPostfix((Container)matrixElements[j], new Stack(), new Stack()));
                                    j++;
                                }
                                outStack.push(new Matrix(ID, name, layout.getRows(), layout.getColumns()));
                            }
                            
                            int first = -1;
                            int second = -1;
                            
                            
                            // Fraction
                            if (ID == 1) {
                                first = 1;
                                second = 2;
                            }
                            //  Unknown function
                            if (ID == 50) {
                                first = 3;
                                second = 0;
                            }
                            // N-th Root
                            if (ID == 6) {
                                first = 2;
                                second = 3;
                            }
                            // Superscript or Subscript
                            if (ID == 7 || ID == 8) {
                                first = 1;
                                second = 0;
                            }
                            // Exponential or Factorial
                            if (ID == 29 || ID == 37) {
                                first = 1;
                            }
                            // Squareroot
                            if (ID == 5) {
                                first = 2;
                            }
                            // Evaluate
                            if (ID == 57) {
                                JPanel temp = (JPanel)layoutComponents[1];
                                Stack layoutOutStack3 = toPostfix((Container)temp.getComponent(2), new Stack(), new Stack());
                                outStack.push(layoutOutStack3);
                                Stack layoutOutStack2 = toPostfix((Container)temp.getComponent(0), new Stack(), new Stack());
                                outStack.push(layoutOutStack2);
                                Stack layoutOutStack1 = toPostfix((Container)layoutComponents[2], new Stack(), new Stack());
                                outStack.push(layoutOutStack1);
                                outStack.push(new NaryFunction(ID, name, 3));
                            }
                            // Limit
                            if (ID == 56) {
                                JPanel temp = (JPanel)layoutComponents[0];
                                temp = (JPanel)temp.getComponent(1);
                                Stack layoutOutStack3 = toPostfix((Container)temp.getComponent(2), new Stack(), new Stack());
                                outStack.push(layoutOutStack3);
                                Stack layoutOutStack2 = toPostfix((Container)temp.getComponent(0), new Stack(), new Stack());
                                outStack.push(layoutOutStack2);
                                Stack layoutOutStack1 = toPostfix((Container)layoutComponents[1], new Stack(), new Stack());
                                outStack.push(layoutOutStack1);
                                outStack.push(new NaryFunction(ID, name, 3));
                            }
                            // Def. Integral
                            if (ID == 53) {
                                JPanel temp = (JPanel)layoutComponents[0];
                                Stack layoutOutStack4 = toPostfix((Container)temp.getComponent(0), new Stack(), new Stack());
                                outStack.push(layoutOutStack4);
                                Stack layoutOutStack3 = toPostfix((Container)temp.getComponent(2), new Stack(), new Stack());
                                outStack.push(layoutOutStack3);
                                Stack layoutOutStack2 = toPostfix((Container)layoutComponents[2], new Stack(), new Stack());
                                outStack.push(layoutOutStack2);
                                Stack layoutOutStack1 = toPostfix((Container)layoutComponents[3], new Stack(), new Stack());
                                outStack.push(layoutOutStack1);
                                outStack.push(new NaryFunction(ID, name, 4));
                            }
                            // Product + Sum
                            if (ID == 54 || ID == 55) {
                                JPanel temp = (JPanel)layoutComponents[0];
                                Stack layoutOutStack4 = toPostfix((Container)temp.getComponent(0), new Stack(), new Stack());
                                outStack.push(layoutOutStack4);
                                temp = (JPanel)temp.getComponent(2);
                                Stack layoutOutStack3 = toPostfix((Container)temp.getComponent(2), new Stack(), new Stack());
                                outStack.push(layoutOutStack3);
                                Stack layoutOutStack2 = toPostfix((Container)temp.getComponent(0), new Stack(), new Stack());
                                outStack.push(layoutOutStack2);
                                Stack layoutOutStack1 = toPostfix((Container)layoutComponents[1], new Stack(), new Stack());
                                outStack.push(layoutOutStack1);
                                outStack.push(new NaryFunction(ID, name, 4));
                            }
                            // Differential, Partial Differential
                            if (ID == 46 || ID == 84) {
                                JPanel temp = (JPanel)layoutComponents[0];
                                JPanel temp2 = (JPanel)temp.getComponent(2);
                                Stack layoutOutStack2 = toPostfix((Container)temp2.getComponent(1), new Stack(), new Stack());
                                outStack.push(layoutOutStack2);
                                Stack layoutOutStack1 = toPostfix((Container)layoutComponents[1], new Stack(), new Stack());
                                outStack.push(layoutOutStack1);
                                outStack.push(new NaryFunction(ID, name, 2));
                            }
                            // Integral
                            if (ID == 45) {
                                Stack layoutOutStack2 = toPostfix((Container)layoutComponents[2], new Stack(), new Stack());
                                outStack.push(layoutOutStack2);
                                Stack layoutOutStack1 = toPostfix((Container)layoutComponents[3], new Stack(), new Stack());
                                outStack.push(layoutOutStack1);
                                outStack.push(new NaryFunction(ID, name, 2));
                            }
                            
                            if (second != -1) {
                                Stack layoutOutStack2 = toPostfix((Container)layoutComponents[first], new Stack(), new Stack());
                                outStack.push(layoutOutStack2);
                                Stack layoutOutStack1 = toPostfix((Container)layoutComponents[second], new Stack(), new Stack());
                                outStack.push(layoutOutStack1);
                                outStack.push(new BinaryOperator(ID, name));
                            } else {
                                if (first != -1) {
                                    Stack layoutOutStack = toPostfix((Container)layoutComponents[first], new Stack(), new Stack());
                                    outStack.push(layoutOutStack);
                                    outStack.push(new Function(ID, name));
                                }
                            }
                        }
                    } else {
                        // Argument panel
                        JPanel temp =  (JPanel)components[i];
                        components = temp.getComponents();
                        i = -1;
                        x = components.length;
                    }
                    
                }
                // If none of these ignore component and move on
                i++;
            }
            
            if (lastAdded != 2) {
                throw new java.text.ParseException(langMan.readLangFile("Argument") + i, i);
            }
            
            while (operatorStack.empty() != true) {
                outStack.push(operatorStack.pop());
            }
        }
        return outStack;
    }
    
    
    public static Stack parseString(String expression, Stack outputStack) throws org.nfunk.jep.ParseException {
        if (expression.equals("")) {
            outputStack.add(new Tree.Text(expression));
        } else {
            
            expression = validateBeforeParsing(expression);
            
            JEP parser = new JEP();
            parser.addStandardFunctions();
            parser.setAllowUndeclared(true);
            parser.setImplicitMul(true);
            parser.addFunction("union", new Union());
            parser.addFunction("intersection", new Intersection());
            parser.addFunction("det", new Determinant());
            parser.addFunction("trace", new Trace());
            parser.addFunction("subset", new Subset());
            parser.addFunction("integrate", new Integral());
            parser.addFunction("diff", new Differential());
            parser.addFunction("sum", new Sum());
            parser.addFunction("product", new Product());
            parser.addFunction("int", new DefiniteIntegral());
            
            parser.parseExpression(expression);
            Node node = parser.getTopNode();
            
            if (node == null) {
                throw new org.nfunk.jep.ParseException();
            } else {
                outputStack = convertJEPTree(node, new Stack(), expression);
            }
        }
        return outputStack;
    }
    
    
    public static String validateBeforeParsing(String theText) {
        /* Fixes problems with JEP parser that cannot be fixed directly, by parsing greek letters
         * and '=' so that it conforms with JEP.
         */
        
        int i=0;
        while (i < theText.length()) {
            int ascii_value = (int)theText.charAt(i);
            // if character not ascii, then will be greek letter
            if (ascii_value > 127 && i < theText.length()-1) {
                int nextChar = theText.charAt(i+1);
                // if next character is letter, digit or greek letter
                if (Character.isLetterOrDigit(theText.charAt(i+1)) || nextChar > 127) {
                    String left = theText.substring(0, i+1);
                    String right = theText.substring(i+1, theText.length());
                    theText = left + "*" + right;
                }
            }
            i++;
        }
        
        i=0;
        while (i < theText.length()) {
            char token = theText.charAt(i);
            char infinity = '\u221e';
            // if character is '=' change to '=='
            if (token == '='
                    && i > 0
                    && i < theText.length() - 1
                    && theText.charAt(i-1) != '='
                    && theText.charAt(i+1) != '='
                    && theText.charAt(i-1) != '<'
                    && theText.charAt(i-1) != '>') {
                theText = theText.substring(0, i+1) + "=" + theText.substring(i+1, theText.length());
                i++;
            }
            if (token == infinity) {
                theText = theText.substring(0, i) + "Infinity" + theText.substring(i+1, theText.length());
            }
            i++;
        }
        return theText;
    }
    
    
    public static Stack convertJEPTree(Node currentNode, Stack outputStack, String expression) {
        // Node is function/operator
        if (currentNode.getClass().getName().equals("org.nfunk.jep.ASTFunNode")) {
            ASTFunNode funNode = (ASTFunNode)currentNode;
            String name = funNode.getPFMC().getClass().getName();
            int i = name.lastIndexOf(".");
            String type = name.substring(i+1);
            org.jdom.Element comp = null;
            
            if (type.equals("Integral")) {
                if  (funNode.jjtGetNumChildren() >= 4) {
                    type = "DefiniteIntegral";
                }
                if  (funNode.jjtGetNumChildren() == 1) {
                    VariableFactory varFactory = new VariableFactory();
                    ASTVarNode newChild = new ASTVarNode(0);
                    newChild.setVar(varFactory.createVariable("x"));
                    funNode.jjtAddChild(newChild, 1);
                }
            }
            if (type.equals("DefiniteIntegral")) {
                if  (funNode.jjtGetNumChildren() == 1) {
                    VariableFactory varFactory = new VariableFactory();
                    ASTVarNode newChild = new ASTVarNode(0);
                    newChild.setVar(varFactory.createVariable("x"));
                    funNode.jjtAddChild(newChild, 1);
                }
                
                if  (funNode.jjtGetNumChildren() == 2 || funNode.jjtGetNumChildren() == 3) {
                    type = "Integral";
                }
            }
            
            comp = inpComps.getChild(type);
            
            // Function is in CompConfig list
            
            if (comp != null) {
                int group = Integer.parseInt(comp.getAttributeValue("group"));
                // N-ary
                if (group == 1) {
                    convertJEPTree(funNode.jjtGetChild(0), outputStack, expression);
                    convertJEPTree(funNode.jjtGetChild(1), outputStack, expression);
                    outputStack.push(new NaryOperator(Integer.parseInt(comp.getAttributeValue("ID")), type));
                }
                
                // Binary
                if (group == 2) {
                    convertJEPTree(funNode.jjtGetChild(0), outputStack, expression);
                    convertJEPTree(funNode.jjtGetChild(1), outputStack, expression);
                    outputStack.push(new BinaryOperator(Integer.parseInt(comp.getAttributeValue("ID")), type));
                }
                
                // Function
                if (group == 3) {
                    Stack functionStack = convertJEPTree(funNode.jjtGetChild(0), new Stack(), expression);
                    outputStack.push(functionStack);
                    outputStack.push(new Function(Integer.parseInt(comp.getAttributeValue("ID")), type));
                }
                
                // Grouping
                if (group == 5) {
                    Stack groupingStack = convertJEPTree(funNode.jjtGetChild(0), new Stack(), expression);
                    outputStack.push(groupingStack);
                    outputStack.push(new Grouping(Integer.parseInt(comp.getAttributeValue("ID")), type));
                }
                
                // Layout
                if (group == 0) {
                    if (funNode.isOperator()) {
                        // All layout operators are binary currently
                        convertJEPTree(funNode.jjtGetChild(0), outputStack, expression);
                        convertJEPTree(funNode.jjtGetChild(1), outputStack, expression);
                        outputStack.push(new BinaryOperator(Integer.parseInt(comp.getAttributeValue("ID")), type));
                    }  else {
                        int j=funNode.jjtGetNumChildren() - 1;
                        while (j >= 0) {
                            Stack functionStack = convertJEPTree(funNode.jjtGetChild(j), new Stack(), expression);
                            outputStack.push(functionStack);
                            j--;
                        }
                        if (funNode.jjtGetNumChildren() > 1) {
                            outputStack.push(new NaryFunction(Integer.parseInt(comp.getAttributeValue("ID")), type, funNode.jjtGetNumChildren()));
                        } else {
                            outputStack.push(new Function(Integer.parseInt(comp.getAttributeValue("ID")), type));
                        }
                    }
                }
            } else {
                if (type.equals("Comparative")) {
                    if (funNode.getName().equals("<")) {
                        type = "LessThan";
                    } else if (funNode.getName().equals(">")) {
                        type = "GreaterThan";
                    } else if (funNode.getName().equals("<=")) {
                        type = "LTEQ";
                    } else if (funNode.getName().equals(">=")) {
                        type = "GTEQ";
                    } else if (funNode.getName().equals("!=")) {
                        type = "NotEqual";
                    } else if (funNode.getName().equals("==")) {
                        type = "Equals";
                    }
                    comp = inpComps.getChild(type);
                }
                
                try {
                    convertJEPTree(funNode.jjtGetChild(0), outputStack, expression);
                    convertJEPTree(funNode.jjtGetChild(1), outputStack, expression);
                    outputStack.push(new BinaryOperator(Integer.parseInt(comp.getAttributeValue("ID")), type));
                } catch (NullPointerException ex) {
                    outputStack.add(new Tree.Text(expression));
                }
            }
        }
        // Node is constant
        if (currentNode.getClass().getName().equals("org.nfunk.jep.ASTConstant")) {
            ASTConstant constantNode = (ASTConstant)currentNode;
            
            double d = Double.parseDouble(constantNode.getValue().toString());
            //int x = (int)d;
            outputStack.push(new RealNumber(d));
        }
        // Node is variable
        if (currentNode.getClass().getName().equals("org.nfunk.jep.ASTVarNode")) {
            ASTVarNode varNode = (ASTVarNode)currentNode;
            if (varNode.getName().length() > 1) {
                outputStack.push(new Tree.Text(varNode.getName()));
            } else {
//                if (varNode.getName().charAt(0) == '\u03ac') {
//                    outputStack.push(new Variable('\u221e', "Variable"));
                //} else {
                    outputStack.push(new Variable(varNode.getName().charAt(0), "Variable"));
                //}
            }
        }
        return outputStack;
    }
    
    
    /** Creates a tree structure from the postfix expression
     * @param currentNode an object of the expression to place into the tree
     * @param currentStack a stack containing the postfix expression
     */
    public static void toTree(MathObject currentNode, Stack currentStack) {
        
        if (currentStack.empty() != true) {
            
            if (currentNode.getClass().getName().equals("Tree.NaryFunction")) {
                NaryFunction naryFunctionObj = (NaryFunction)currentNode;
                
                Stack stack;
                
                int i=0;
                while (i < naryFunctionObj.getSize()) {
                    stack = (Stack)currentStack.pop();
                    MathObject child1 = (MathObject)stack.pop();
                    naryFunctionObj.setChild(child1, i);
                    naryFunctionObj.getChild(i).setParent(naryFunctionObj);
                    toTree(child1, stack);
                    i++;
                }
            } else if (currentNode.getClass().getName().equals("Tree.BinaryOperator")) {
                BinaryOperator binaryObj = (BinaryOperator)currentNode;
                
                if (binaryObj.getID() == 1 || binaryObj.getID() == 6 || binaryObj.getID() == 7 || binaryObj.getID() == 8
                        || binaryObj.getID() == 45  || binaryObj.getID() == 46 || binaryObj.getID() == 50 || binaryObj.getID() == 84) {
                    Object obj = (Object)currentStack.pop();
                    
                    if (obj.getClass().getName() != ("java.util.Stack")) {
                        
                        binaryObj.setRightChild((MathObject)obj);
                        binaryObj.getRightChild().setParent(binaryObj);
                        toTree(binaryObj.getRightChild(), currentStack);
                        
                        binaryObj.setLeftChild((MathObject)currentStack.pop());
                        binaryObj.getLeftChild().setParent(binaryObj);
                        toTree(binaryObj.getLeftChild(), currentStack);
                        
                    } else {
                        Stack leftStack = (Stack)obj;
                        Stack rightStack = (Stack)currentStack.pop();
                        
                        MathObject leftChild = (MathObject)leftStack.pop();
                        binaryObj.setLeftChild(leftChild);
                        binaryObj.getLeftChild().setParent(binaryObj);
                        toTree(leftChild, leftStack);
                        
                        MathObject rightChild = (MathObject)rightStack.pop();
                        binaryObj.setRightChild(rightChild);
                        binaryObj.getRightChild().setParent(binaryObj);
                        toTree(rightChild, rightStack);
                    }
                } else {
                    
                    binaryObj.setRightChild((MathObject)currentStack.pop());
                    binaryObj.getRightChild().setParent(binaryObj);
                    toTree(binaryObj.getRightChild(), currentStack);
                    
                    binaryObj.setLeftChild((MathObject)currentStack.pop());
                    binaryObj.getLeftChild().setParent(binaryObj);
                    toTree(binaryObj.getLeftChild(), currentStack);
                }
                
            } else if (currentNode.getClass().getName().equals("Tree.NaryOperator")) {
                NaryOperator naryObj = (NaryOperator)currentNode;
                
                int i=-1;
                MathObject nextObject = (MathObject)currentStack.peek();
                
                while (nextObject.getID() == naryObj.getID()) {
                    currentStack.remove(currentStack.size() -1);
                    // Add extra
                    i++;
                    naryObj.addChild((MathObject)currentStack.pop(), i);
                    naryObj.getChild(i).setParent(naryObj);
                    toTree(naryObj.getChild(i), currentStack);
                    nextObject = (MathObject)currentStack.peek();
                }
                i++;
                // Add first
                naryObj.addChild((MathObject)currentStack.pop(), i);
                naryObj.getChild(i).setParent(naryObj);
                toTree(naryObj.getChild(i), currentStack);
                
                nextObject = (MathObject)currentStack.peek();
                
                while (nextObject.getID() == naryObj.getID()) {
                    currentStack.remove(currentStack.size() -1);
                    // Add extra
                    i++;
                    naryObj.addChild((MathObject)currentStack.pop(), i);
                    naryObj.getChild(i).setParent(naryObj);
                    toTree(naryObj.getChild(i), currentStack);
                    nextObject = (MathObject)currentStack.peek();
                }
                i++;
                // Add second
                naryObj.addChild((MathObject)currentStack.pop(), i);
                naryObj.getChild(i).setParent(naryObj);
                toTree(naryObj.getChild(i), currentStack);
                
            } else if (currentNode.getClass().getName().equals("Tree.Function")) {
                Function functionObj = (Function)currentNode;
                Stack functionStack = (Stack)currentStack.pop();
                MathObject root = (MathObject)functionStack.pop();
                toTree(root, functionStack);
                functionObj.setChild(root);
                
            } else if (currentNode.getClass().getName().equals("Tree.Grouping")) {
                Grouping groupingObj = (Grouping)currentNode;
                Stack groupingStack = (Stack)currentStack.pop();
                MathObject root = (MathObject)groupingStack.pop();
                toTree(root, groupingStack);
                groupingObj.setChild(root);
                
            } else if (currentNode.getClass().getName().equals("Tree.Matrix")) {
                Matrix matrixObj = (Matrix)currentNode;
                
                int m = matrixObj.getM();
                int n= matrixObj.getN();
                
                int i=m-1; int j=n-1;
                while (i >= 0) {
                    while (j >= 0) {
                        Stack matrixStack = (Stack)currentStack.pop();
                        MathObject element = (MathObject)matrixStack.pop();
                        matrixObj.setElement(i,j,element);
                        matrixObj.getElement(i,j).setParent(matrixObj);
                        toTree(element, matrixStack);
                        j--;
                    }
                    j=n-1;
                    i--;
                }
            } else {
                
            }
        }
    }
    
    
    public int getCopyFinish() {
        return copyFinish;
    }
    
}
