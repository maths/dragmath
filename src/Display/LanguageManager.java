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

import org.jdom.*;
import org.jdom.input.*;
import java.net.URL;
import javax.swing.*;
import java.io.*;

/**
 *
 * @author Alex Billingsley
 */
public class LanguageManager {
    
    private Document languageFile;
    private Element lang;
    private StatusBar statusBar;
    private URL appletCodeBase;
    private SAXBuilder builder;
    
    /** Creates a new instance of Language */
    public LanguageManager(URL appletCodeBase, StatusBar statusBar) {
        this.appletCodeBase = appletCodeBase;
        this.statusBar=statusBar;
        builder = new SAXBuilder();
    }
    
    public String readLangFile(String childName) {
        Element child = null;
        if (lang != null) {
            child = lang.getChild(childName);
        }
        String text="";
        if (child != null) text = child.getText();
        else statusBar.println("Error: Missing data in language file");
        return text;
    }
    
    
    public void loadLanguageFile(String language) {
        try {
            URL path = new URL(appletCodeBase + "lang/" + language + ".xml");
            languageFile = builder.build(path);
        } catch (java.io.FileNotFoundException ex) {
            JOptionPane.showMessageDialog(null, "Error reading language file", "DragMath", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error reading language file", "DragMath", JOptionPane.ERROR_MESSAGE);
        } catch (JDOMException ex) {
            JOptionPane.showMessageDialog(null, "Error reading language file", "DragMath", JOptionPane.ERROR_MESSAGE);
        }
        if (languageFile != null) {
            lang = languageFile.getRootElement();
        }
    }
}
