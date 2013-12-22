
package org.nfunk.jep.function;


import java.util.*;
import org.nfunk.jep.*;
/** * An example custom function class for JEP. */


public class Determinant extends PostfixMathCommand {
    
    /**	 * Constructor	 */
    public Determinant() {
        numberOfParameters = 1;
    }
    
    public void run(Stack inStack) throws ParseException {
// no evaluation needed
    }
    
}