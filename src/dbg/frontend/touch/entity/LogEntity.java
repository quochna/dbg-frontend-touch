/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbg.frontend.touch.entity;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author cpu10859
 */
public class LogEntity extends BaseEntity{
    public long startTime = 0;
    public long endTime = 0;
    public String requestUrl = "";
    public String exception = "";
    public String userAgent = "";
    
    
    
    public static String getFullURL(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }
}
