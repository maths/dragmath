/*
 * WSHelper.java
 *
 * Created on 11 September 2010, 20:58
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Display;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 *
 * @author Alex Billingsley
 */
public class WSHelper {
    
    static private BASE64Encoder encode = new BASE64Encoder();
    static private BASE64Decoder decode = new BASE64Decoder();
    
    static public String OToS(Object obj) {
        
        long start=System.currentTimeMillis();
        String out = null;
        if (obj != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(obj);
                out = encode.encode(baos.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        long end=System.currentTimeMillis();
        System.out.println("Encode:"+(end-start));
        return out;
    }
    
    static public Object SToO(String str) {
        long start=System.currentTimeMillis();
        Object out = null;
        if (str != null) {
            try {
                ByteArrayInputStream bios = new
                        ByteArrayInputStream(decode.decodeBuffer(str));
                ObjectInputStream ois = new ObjectInputStream(bios);
                out = ois.readObject();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
        long end=System.currentTimeMillis();
        System.out.println("Decode:"+(end-start));
        return out;
    }
}
