
package org.nfunk.jep.function;


import java.util.*;
import org.nfunk.jep.*;
/** * An example custom function class for JEP. */


public class Intersection extends PostfixMathCommand {
    
    /**	 * Constructor	 */
    public Intersection() {
        numberOfParameters = 2;
    }
    
    public void run(Stack inStack) throws ParseException {
// no evaluation needed
    }
    
}