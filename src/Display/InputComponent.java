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

import java.awt.Cursor;
import java.awt.*;

/**
 *
 * @author Alex Billingsley
 */
public class InputComponent {
    
    private String tooltip;
    private Cursor cursor;
    private int ID;
    private int group;
    private String displayText;
    private String alternativeText;
    private boolean useAlternativeText;
    private String tag;
    private Image image;
    
    /** Creates a new instance of InputComponent */
    public InputComponent(int ID, int group, String displayText, String tooltip, Cursor cursor, String tag, Image image) {
        this.tooltip=tooltip;
        this.cursor=cursor;
        this.ID=ID;
        this.group=group;
        this.displayText=displayText;
        this.tag=tag;
        this.image=image;
    }
    
    
    public String getTooltip() {
        return tooltip;
    }
    
    public Image getImage() {
        return image;
    }
    
    public Cursor getCursor() {
        return cursor;
    }
    
    public int getID() {
        return ID;
    }
    
    public int getGroup() {
        return group;
    }
    
    public String getTag() {
        return tag;
    }
    
    public String getDisplayText() {
        return displayText;
    }
    
    
}
