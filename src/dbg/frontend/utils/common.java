/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbg.frontend.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

/**
 *
 * @author cpu10859
 */
public class common {
    private static final Logger logger = Logger.getLogger(common.class);
    public static String getRequestUrl(HttpServletRequest request) {
        StringBuilder stringBuilder = new StringBuilder(request.getRequestURL());
        Enumeration enu = request.getParameterNames();
        stringBuilder.append("?");
        while (enu.hasMoreElements()) {
            String paramName = (String) enu.nextElement();
            stringBuilder.append(paramName).append("=")
                    .append(request.getParameter(paramName))
                    .append("&");
        }
        return stringBuilder.toString();
    }

    public static String sendPost(List<NameValuePair> nvps, String postUrl, int timeout) throws UnsupportedEncodingException, IOException {
        String result;
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(timeout)
                .setConnectTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .setStaleConnectionCheckEnabled(true)
                .build();
        try (CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build()) {
            HttpPost httpPost = new HttpPost(postUrl);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new IOException("Failed : HTTP getStatusCode: "
                            + response.getStatusLine().getStatusCode() + " HTTP getReasonPhrase: " + response.getStatusLine().getReasonPhrase());
                }
                HttpEntity entity = response.getEntity();
                try (InputStream inputStream = entity.getContent()) {
                    result = IOUtils.toString(inputStream, "UTF-8");
                }
            }
        }
        return result;
    }
    public static String getClientIP(HttpServletRequest request) {
        String ipAddress = "";
        try {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null) {
                ipAddress = request.getRemoteAddr();
            }
            //mot so truong hop ipAddress co dang "118.71.76.149, 198.23.90.140";
            int i = ipAddress.indexOf(",");
            if (i > -1) {
                if (i > 0) {
                    ipAddress = ipAddress.substring(0, i).trim();
                } else {
                    ipAddress = ipAddress.substring(i + 1, ipAddress.length()).trim();
                }
            }
        } catch (Exception ex) {
            logger.error("getClientIP exception " + ex.getMessage(), ex);
        }
        return ipAddress;
    }
}
