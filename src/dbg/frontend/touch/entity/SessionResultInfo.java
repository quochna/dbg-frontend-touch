/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dbg.frontend.touch.entity;

import java.io.Serializable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 *
 * @author bangdq
 */
public class SessionResultInfo extends BaseEntity
{
  public String appid;
  public String appserverid;
  public String urlredirect;
  public String transid;
  public String billed;
  public String apptransid;
  public String pl;
  public String netamount;
  public String pmcid;
  public String grossamount;

}