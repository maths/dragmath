
package org.nfunk.jep.function;


import java.util.*;
import org.nfunk.jep.*;
/** * An example custom function class for JEP. */


public class Product extends PostfixMathCommand {
    
    /**	 * Constructor	 */
    public Product() {
        numberOfParameters = 4;
    }
    
    public void run(Stack inStack) throws ParseException {
// no evaluation needed
    }
    
}