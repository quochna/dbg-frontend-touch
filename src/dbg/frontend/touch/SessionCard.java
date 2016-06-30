/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbg.frontend.touch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Serializable;

/**
 *
 * @author bangdq
 */
public class SessionCard  implements Serializable{
    public String CardSerial;
    public String pmcID;
    public String transID;
    public String toJsonString()
    {
        // Serialize entity to Json string
        GsonBuilder gsonBuilder = new GsonBuilder();             
        Gson gson = gsonBuilder.create(); 
        return gson.toJson(this);
        
    }
    
}
