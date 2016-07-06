/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbg.frontend.touch.entity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Serializable;

/**
 *
 * @author cpu10859
 */
public class BaseEntity implements Serializable{
    public static final String yyyyMMddhhmmssSSS = "yyyy-MM-dd HH:mm:ss.SSS";
     public  String toJsonString() {
       GsonBuilder gsonBuilder = new GsonBuilder();
       gsonBuilder.disableHtmlEscaping();
       gsonBuilder.setDateFormat(yyyyMMddhhmmssSSS);
       Gson gson = gsonBuilder.disableHtmlEscaping().create();
       return gson.toJson(this);
   }

}
