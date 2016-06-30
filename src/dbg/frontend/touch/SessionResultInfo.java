/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbg.frontend.touch;

import java.io.Serializable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 *
 * @author bangdq
 */
public class SessionResultInfo implements Serializable
{
  public String appid;
  public String appserverid;
  public String url_redirect;
  public String transid;
  public String billed;
  public String apptransid;
  public String pl;
  public String netamount;
  public String pmcid;
  public String grossamount;
  public String toJsonString()
    {
        // Serialize entity to Json string
        GsonBuilder gsonBuilder = new GsonBuilder();             
        Gson gson = gsonBuilder.create(); 
        return gson.toJson(this);
        
    }
}