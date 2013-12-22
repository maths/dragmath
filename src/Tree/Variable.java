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

/**
 * Class to represent a Variable object, consisting of a single character
 * @author Alex Billingsley
 */
public class Variable extends MathObject {
    
    private char varName;
    
    /** Creates a new instance of Variable
     * Initialises the parameters
     */
    public Variable(char varName, String type) {
        super(-1, type);
        this.varName = varName;
    }
    
    /** Returns the variable name
     * @return the variable name
     */
    public char getVarName() {
        return varName;
    }
    
}