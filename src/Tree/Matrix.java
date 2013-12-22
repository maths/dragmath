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
 * Class representing a Matrix, that has an n x n array of <code>MathObject</code> objects.
 * @author Alex Billingsley
 */
public class Matrix extends MathObject {
    
    private MathObject[][] array;
    private int m;
    private int n;
    
    /** Creates a new instance of Matrix
     * Initialises the fields from the parameters
     */
    public Matrix(int id, String name, int m, int n) {
        super(id, name);
        this.m=m;
        this.n=n;
        array = new MathObject[m][n];
        array[0][0] = null;
    }
    
    /** Sets an element in the array at the location from the parameters
     * @param m the int to specify which row to add the element in
     * @param n the int to specify which column to add the element in
     * @param element the <code>MathObject</code> to add at the location [m][n]
     */
    public void setElement(int m, int n, MathObject element) {
        array[m][n] = element;
    }
    
    /** Returns the array
     * @return The field <code>array</code> of type <code>MathObject[][]</code>
     */
    public MathObject[][] getArray() {
        return array;
    }
    
    /** Returns the element in the array at the location from the parameters
     * @param m the int to specify which row to get the element from
     * @param n the int to specify which column to get the element from
     * @return the element of type <code>MathObject</code> from the location in the array specified by the parameters
     */
    public MathObject getElement(int m, int n) {
        return array[m][n];
    }
    
    /** Returns the number of rows in the array
     * @return An int for the number of rows
     */
    public int getM() {
        return m;
    }
    
    /** Returns the number of columns in the array
     * @return An int for the number of columns
     */
    public int getN() {
        return n;
    }
}
