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
 * Class to store the precedence of each different math component
 * @author Alex Billingsley
 */
public class Precedence {
    
    
    public static final int NONE=0;
    public static final int ADD_SUB=1;
    public static final int MULT_DIV=2;
    public static final int EXPONENTS_ROOTS=3;
    
    public static int[] value = new int[100];
    static
    { int i=0;
      while (i < value.length) {
          value[i] = NONE;
          i++;
      }
      value[0] = MULT_DIV;
      value[1] = MULT_DIV;
      value[2] = ADD_SUB;
      value[3] = ADD_SUB;
      value[5] = EXPONENTS_ROOTS;
      value[6] = EXPONENTS_ROOTS;
      value[7] = EXPONENTS_ROOTS;
      value[8] = EXPONENTS_ROOTS;
      
    }

}
