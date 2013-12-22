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
 * Class to represent a RealNumber object, consisting of a double
 * @author Alex Billingsley
 */
public class RealNumber extends MathObject {
    
    private double number;
    
    /** Creates a new instance of RealNumber
     * Initialises the parameters
     */
    public RealNumber(double number) {
        super(-1, "");
        this.number = number;
    }
    
    /** Returns the number
     * @return the number
     */
    public String getNumber(boolean keepAsDouble) 
    {
       String strNum = "";
        if (Double.compare(Math.floor(number), number) == 0 && keepAsDouble == false) {
            int intNum = (int)number;
            strNum = Integer.toString(intNum);
        } else {
            strNum = Double.toString(number);
        }

       return strNum;
    }
    
}