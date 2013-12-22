
package org.nfunk.jep.function;


import java.util.*;
import org.nfunk.jep.*;
/** * An example custom function class for JEP. */


public class Differential extends PostfixMathCommand {
    
    /**	 * Constructor	 */
    public Differential() {
        numberOfParameters = 2;
    }
    
    public void run(Stack inStack) throws ParseException {
// no evaluation needed
    }
    
}