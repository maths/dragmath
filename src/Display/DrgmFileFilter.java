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

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

/**
 *
 * @author Alex Billingsley
 */
public class DrgmFileFilter extends FileFilter {
    
    
    /** Creates a new instance of DrgmFileFilter */
    public DrgmFileFilter() {
    }
    
    
    public boolean accept(File f) {
        if(f != null) {
            if (f.isDirectory()) return true;
            String filename = f.getName();
            int i = filename.lastIndexOf('.');
            if(i > 0 && i < filename.length() - 1) {
                if (filename.substring(i+1).toLowerCase().equals("drgm")) return true;
            }
        }
        return false;
    }
    
    public static boolean isDrgmFile(File f) {
        if(f != null) {
            String filename = f.getName();
            int i = filename.lastIndexOf('.');
            if(i > 0 && i < filename.length() - 1) {
                if (filename.substring(i+1).toLowerCase().equals("drgm")) return true;
                if (f.isDirectory()) return true;
            }
        }
        return false;
    }
    
    public String getDescription(){
        return "DragMath expression (*.drgm)";
    }
}
