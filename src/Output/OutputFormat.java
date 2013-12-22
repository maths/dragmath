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

package Output;

import Tree.*;

import org.jdom.*;
import org.jdom.input.*;
import java.net.URL;
import java.io.*;
import javax.swing.*;
import Display.StatusBar;
import Display.LanguageManager;

/**
 * Class to use configuration files to convert expression to different syntax
 * @author Alex Billingsley
 */
public class OutputFormat {
    
    private String output="";
    private boolean autoBrackets;
    private boolean implicitMult;
    private boolean keepAsDouble;
  
    private Document formatDoc;
    private Element root;
    private Element bracket;
    private SAXBuilder builder;
    
    private String outputFormat;
    
    private URL appletCodeBase;
    private StatusBar status;
    
    private LanguageManager langMan;
    
    /** Creates a new instance of OutputAlgorithm
     * Reads in config. files bundled into JAR file of applet
     */
    public OutputFormat(StatusBar status, LanguageManager langMan, URL appletCodeBase, boolean implicitMult, boolean keepAsDouble) {
        this.status=status;
        autoBrackets=false;
        builder = new SAXBuilder();
        this.appletCodeBase=appletCodeBase;
        this.langMan=langMan;
        this.implicitMult = implicitMult;
        this.keepAsDouble = keepAsDouble;
    }
    
    public void setImplictMult(boolean _implicitMult) {
        implicitMult = _implicitMult;
    }
    
    public String getOutputFormat() {
        return outputFormat;
    }
    
    /** Converts expression to format and copies the string to the system clipboard
     */
    public String outputToClipboard(MathObject startNode) {
        autoBrackets=false;
        String syntax="";;
        try {
            root = formatDoc.getRootElement();
            
            bracket = root.getChild("BracketsRnd");
            
            if (startNode != null) {
                Element initial = root.getChild("Initial");
                output = output + initial.getChild("output1").getText();
                if (root.getChild("name").getAttributeValue("AutoBrackets").equals("true")) {
                    autoBrackets=true;
                }
                convert(startNode);
                output = output + initial.getChild("output2").getText();
                JTextField temp = new JTextField(output);
                syntax = output;
                temp.selectAll();
                temp.copy();
                output = "";
                status.println(langMan.readLangFile("Clipboard"));
            } else {
                status.println(langMan.readLangFile("NoExpression"));
            }
            
        } catch (java.lang.NullPointerException err) {
            JOptionPane.showMessageDialog(null, langMan.readLangFile("ReadingFile"), "DragMath", JOptionPane.ERROR_MESSAGE);
        }
        return syntax;
    }
    
    /** Reads in a particular format file specified in the parameters
     */
    public void readFormatFile(String fileName) {
        try {
            URL path = new URL(appletCodeBase + "formats/" + fileName  + ".xml");
            formatDoc = builder.build(path);
            outputFormat = fileName;
        } catch (java.io.FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, langMan.readLangFile("ReadingFile2") + " " + fileName, "DragMath", JOptionPane.ERROR_MESSAGE);
        } catch (JDOMException ex) {
            JOptionPane.showMessageDialog(null, langMan.readLangFile("ReadingFile2") + " " + fileName, "DragMath", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, langMan.readLangFile("ReadingFile2") + " " + fileName, "DragMath", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void readMathTranFile() {
        try {
            formatDoc = builder.build(this.getClass().getResourceAsStream("MathTran.xml"));
        } catch (java.io.FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, langMan.readLangFile("ReadingFile2") + "MathTran", "DragMath", JOptionPane.ERROR_MESSAGE);
        } catch (JDOMException ex) {
            JOptionPane.showMessageDialog(null, langMan.readLangFile("ReadingFile2") + "MathTran", "DragMath", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, langMan.readLangFile("ReadingFile2") + "MathTran", "DragMath", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /** Traverses the tree left-to-right to build up the expression using data from the format file
     * as the syntax for each component
     */
    public void convert(MathObject start) throws java.lang.NullPointerException {
        
        if (start.getClass().getName().equals("Tree.Text")) {
            Tree.Text textObj = (Tree.Text)start;
            
            if (textObj.getText().equals(("Infinity"))) {
                Element var = root.getChild("Infinity");
                output = output + var.getChild("output").getText();
            } else {
                
                Element text = root.getChild("Text");
                try {
                    output = output + text.getChild("initial").getText();
                } catch (java.lang.NullPointerException err) {
                    // do nothing, as this tag is optional in file
                }
                output = output + text.getChild("output1").getText() + textObj.getText() + text.getChild("output2").getText();
            }
        }
        
        if (start.getClass().getName().equals("Tree.RealNumber")) {
            Tree.RealNumber numberObj = (Tree.RealNumber)start;
            Element number = root.getChild("RealNumber");
            try {
                output = output + number.getChild("initial").getText();
            } catch (java.lang.NullPointerException err) {
                // do nothing, as this tag is optional in file
            }
            output = output + number.getChild("output1").getText() + numberObj.getNumber(keepAsDouble) + number.getChild("output2").getText();
        }
        
        if (start.getClass().getName().equals("Tree.Variable")) {
            Tree.Variable variableObj = (Tree.Variable)start;
            Element var;
            
            try {
                // If there is entry in output file containing symbol e.g. greek letter
                var = root.getChild(String.valueOf(variableObj.getVarName()));
                output = output + var.getChild("output").getText();
            } catch (java.lang.NullPointerException err) {
                var = root.getChild(String.valueOf(variableObj.getName()));
                if (variableObj.getName() == "Variable") {
                    output = output + var.getChild("output1").getText();
                    output = output + variableObj.getVarName();
                    output = output + var.getChild("output2").getText();
                } else {
                    output = output + var.getChild("output").getText();
                }
            }
        }
        
        if (start.getClass().getName().equals("Tree.BinaryOperator")) {
            BinaryOperator binaryObj = (BinaryOperator)start;
            Element binary = root.getChild(binaryObj.getName());
            
            try {
                output = output + binary.getChild("initial").getText();
            } catch (java.lang.NullPointerException err) {
                // do nothing, as this tag is optional in file
            }
            
            boolean brackets = false;
            
            // Checks if user wants auto bracketing
            if (autoBrackets) {
                // Check if brackets are turned on/off in xml
                try {
                    if (binary.getAttributeValue("brackets").equals("true")) brackets=true;
                } catch (java.lang.NullPointerException err)  {
                    // If bracket option is not specified, then default is to include brackets
                    brackets=true;
                }
            }
            // Decide if brackets are required using precedence table and location in the tree
            if (brackets) {
                brackets=false;
                if (binaryObj.getParent() != null) {
                    // If precedence is lower
                    if (Precedence.value[binaryObj.getID()] <= Precedence.value[binaryObj.getParent().getID()]) {
                        brackets=true;
                    }
                }
            }
            
            if (brackets) {
                output = output + bracket.getChild("output1").getText();
            }
            output = output + binary.getChild("output1").getText();
            
            // If reverse='true' then swap right and left child
            try {
                if (binary.getAttributeValue("reverse").equals("true")) {
                    convert(binaryObj.getRightChild());
                } else {
                    convert(binaryObj.getLeftChild());
                }
            } catch (NullPointerException ex) {
                // default is set to false
                convert(binaryObj.getLeftChild());
            }
            
            output = output + binary.getChild("output2").getText();
            
            // If reverse='true' then swap right and left child
            try {
                if (binary.getAttributeValue("reverse").equals("true")) {
                    convert(binaryObj.getLeftChild());
                } else {
                    convert(binaryObj.getRightChild());
                }
            } catch (NullPointerException ex) {
                // default is set to false
                convert(binaryObj.getRightChild());
            }
            
            output = output + binary.getChild("output3").getText();
            if (brackets) {
                output = output + bracket.getChild("output2").getText();
            }
        }
        
        if (start.getClass().getName().equals("Tree.Function")) {
            Function functionObj = (Function)start;
            Element function = root.getChild(functionObj.getName());
            
            try {
                output = output + function.getChild("initial").getText();
            } catch (java.lang.NullPointerException err) {
                // do nothing, as this tag is optional in file
            }
            
            output = output + function.getChild("output1").getText();
            
            try {
                if (function.getAttributeValue("brackets").equals("true")) {
                    output = output + bracket.getChild("output1").getText();
                }
            } catch (java.lang.NullPointerException err)  {
                // If bracket option is not specified, then default is to include brackets
                output = output + bracket.getChild("output1").getText();
            }
            
            convert(functionObj.getChild());
            output = output + function.getChild("output2").getText();
            
            try {
                if (function.getAttributeValue("brackets").equals("true")) {
                    output = output + bracket.getChild("output2").getText();
                }
            } catch (java.lang.NullPointerException err)  {
                // If bracket option is not specified, then default is to include brackets
                output = output + bracket.getChild("output2").getText();
            }
        }
        
        if (start.getClass().getName().equals("Tree.Matrix")) {
            Matrix matrixObj = (Matrix)start;
            Element matrix = root.getChild(matrixObj.getName());
            int matrix_m = matrixObj.getM();
            int matrix_n = matrixObj.getN();
            
            output = output + matrix.getChild("matrixStart").getText();
            
            int x=0; int y=0;
            while (y < matrix_m) {
                output = output + matrix.getChild("rowStart").getText();
                while (x < matrix_n) {
                    output = output + matrix.getChild("elementStart").getText();
                    convert(matrixObj.getElement(y,x));
                    output = output + matrix.getChild("elementEnd").getText();
                    if (x != matrix_n-1) {
                        output = output + matrix.getChild("elementSeparator").getText();
                    }
                    x++;
                }
                output = output + matrix.getChild("rowEnd").getText();
                if (y != matrix_m-1) {
                    output = output + matrix.getChild("rowSeparator").getText();
                }
                x=0;
                y++;
            }
            output = output + matrix.getChild("matrixEnd").getText();
        }
        
        if (start.getClass().getName().equals("Tree.Grouping")) {
            Grouping groupingObj = (Grouping)start;
            Element grouping = root.getChild(groupingObj.getName());
            
            try {
                output = output + grouping.getChild("initial").getText();
            } catch (java.lang.NullPointerException err) {
                // do nothing, as this tag is optional in file
            }
            
            output = output + grouping.getChild("output1").getText();
            convert(groupingObj.getChild());
            output = output + grouping.getChild("output2").getText();
        }
        
        if (start.getClass().getName().equals("Tree.NaryOperator")) {
            NaryOperator naryObj = (NaryOperator)start;
            Element nary = root.getChild(naryObj.getName());
            
            try {
                output = output + nary.getChild("initial").getText();
            } catch (java.lang.NullPointerException err) {
                // do nothing, as this tag is optional in file
            }
            
            boolean brackets = false;
            
            // Checks if user wants auto bracketing
            if (autoBrackets) {
                // Check if brackets are turned on/off in xml
                try {
                    if (nary.getAttributeValue("brackets").equals("true")) brackets=true;
                } catch (java.lang.NullPointerException err)  {
                    // If bracket option is not specified, then default is to include brackets
                    brackets=true;
                }
            }
            // Decide if brackets are required using precedence table and location in the tree
            if (brackets) {
                brackets=false;
                if (naryObj.getParent() != null) {
                    // Precedence is lower
                    if (Precedence.value[naryObj.getID()] <= Precedence.value[naryObj.getParent().getID()]) {
                        brackets=true;
                    }
                }
            }
            
            if (brackets) {
                output = output + bracket.getChild("output1").getText();
            }
            int i = naryObj.getSize()-1;
            while (i >= 1) {
                convert(naryObj.getChild(i));
                
                // Code added to make multiplication implicit if user chooses so
                // If implict mult turned on and operator is multiplication then do not output it's display
                if (implicitMult && naryObj.getID() == 0) {
                    // do nothing - as above
                } else {
                    output = output + nary.getChild("output").getText();
                }
                i--;
            }
            convert(naryObj.getChild(i));
            if (brackets) {
                output = output + bracket.getChild("output2").getText();
            }
            
            try {
                output = output + nary.getChild("final").getText();
            } catch (java.lang.NullPointerException err) {
                // do nothing, as this tag is optional in file
            }
        }
        
        if (start.getClass().getName().equals("Tree.NaryFunction")) {
            NaryFunction naryFunctionObj = (NaryFunction)start;
            Element naryFunction = root.getChild(naryFunctionObj.getName());
            
            try {
                output = output + naryFunction.getChild("initial").getText();
            } catch (java.lang.NullPointerException err) {
                // do nothing, as this tag is optional in file
            }
            
            output = output + naryFunction.getChild("output1").getText();
            
            String[] orders = null;
            String order = null;
            try {
                order = naryFunction.getAttributeValue("order");
                if (order != null) {
                    orders = order.split(",");
                }
            } catch (java.lang.NullPointerException err)  {
                // do nothing, as this tag is optional in file
            }
            
            int i=0;
            if (order != null) {
                while (i < naryFunctionObj.getSize()) {
                    convert(naryFunctionObj.getChild(Integer.parseInt(orders[i])));
                    int x = i+2;
                    output = output + naryFunction.getChild("output" + String.valueOf(x)).getText();
                    i++;
                }
            } else {
                while (i < naryFunctionObj.getSize()) {
                    convert(naryFunctionObj.getChild(i));
                    int x = i+2;
                    output = output + naryFunction.getChild("output" + String.valueOf(x)).getText();
                    i++;
                }
            }
        }
    }
}


