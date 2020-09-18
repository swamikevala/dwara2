package org.ishafoundation.dwaraapi.utils;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientUtil {
	
	static Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
	
	public static String getIt(String endpointUrl) throws Exception{
		return getIt(endpointUrl, null);
	}
	
	public static String getItWithCookie(BasicClientCookie cookie, String endpointUrl, String authHeader) throws Exception{
		CloseableHttpClient httpClient = getHttpClientWithCookie(cookie);
        String responseBody = executeGet(httpClient, endpointUrl, authHeader);
        return responseBody;
	}	
	
	public static String getIt(String endpointUrl, String authHeader) throws Exception{
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String responseBody = executeGet(httpClient, endpointUrl, authHeader);
        return responseBody;
	}
	
	private static String executeGet(CloseableHttpClient httpClient, String endpointUrl, String authHeader) throws Exception {
        String responseBody = null;
        
        try {
	    	HttpGet httpGet = new HttpGet(endpointUrl);
	    	if(authHeader != null)
	    		httpGet.setHeader(HttpHeaders.AUTHORIZATION, authHeader);  
	    	 
	    	ResponseHandler<String> responseHandler = createResponseHandler();
	    	logger.trace("Executing request: " + httpGet.getRequestLine());
	        responseBody = httpClient.execute(httpGet, responseHandler);
	
	        
	        logger.trace("Response: " + responseBody);
	        logger.trace("----------------------------------------");
		} finally {
            httpClient.close();
        }

        return responseBody;	
	}
	
	public static String postItWithCookie(BasicClientCookie cookie, String endpointUrl, String authHeader, String postBodyPayload) throws Exception{
		CloseableHttpClient httpClient = getHttpClientWithCookie(cookie);
        String responseBody = executePost(httpClient, endpointUrl, authHeader, postBodyPayload);
        return responseBody;
	}
	
	public static String postIt(String endpointUrl, String authHeader, String postBodyPayload) throws Exception{
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String responseBody = executePost(httpClient, endpointUrl, authHeader, postBodyPayload);
        return responseBody;
	}
	
	private static String executePost(CloseableHttpClient httpClient, String endpointUrl, String authHeader, String postBodyPayload) throws Exception {
        String responseBody = null;
        try {
	        StringEntity reqEntity = new StringEntity(postBodyPayload, "UTF-8");
	
	        HttpPost httpPost = new HttpPost(endpointUrl);
	        httpPost.setEntity(reqEntity);
	        if(authHeader != null)
	        	httpPost.setHeader(HttpHeaders.AUTHORIZATION, authHeader);    
	        httpPost.setHeader("Content-Type", "application/json");        
	        
	        ResponseHandler<String> responseHandler = createResponseHandler();
	
	        logger.trace("Executing request: " + httpPost.getRequestLine());
	        if(!postBodyPayload.contains("password"))
	        	logger.trace("Payload: " + postBodyPayload);
	        
	        responseBody = httpClient.execute(httpPost, responseHandler);
	
	        logger.trace("----------------------------------------");
	        logger.trace(responseBody);
		}
		catch (Exception e) {
			logger.error("httpCall failed - " + e.getMessage(), e);
			throw e;
		} finally {
	        httpClient.close();
	    }
        return responseBody;		
	}

	public static String putItWithCookie(BasicClientCookie cookie, String endpointUrl, String authHeader, String bodyPayload) throws Exception{
		CloseableHttpClient httpClient = getHttpClientWithCookie(cookie);
        String responseBody = executePut(httpClient, endpointUrl, authHeader, bodyPayload);
        return responseBody;
	}
	
	public static String putIt(String endpointUrl, String authHeader, String bodyPayload) throws Exception{
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String responseBody = executePut(httpClient, endpointUrl, authHeader, bodyPayload);
        return responseBody;
	}	

	private static String executePut(CloseableHttpClient httpClient, String endpointUrl, String authHeader, String bodyPayload) throws Exception {		
        String responseBody = null;
        try {
	        StringEntity reqEntity = new StringEntity(bodyPayload, "UTF-8");
	
	        HttpPut httpPut = new HttpPut(endpointUrl);
	        httpPut.setEntity(reqEntity);
	        httpPut.setHeader(HttpHeaders.AUTHORIZATION, authHeader);    
	        httpPut.setHeader("Content-Type", "application/json");
	        
	        ResponseHandler<String> responseHandler = createResponseHandler();
	
	        logger.trace("Executing request: " + httpPut.getRequestLine());
	        logger.trace("Payload: " + bodyPayload);
	        
	        responseBody = httpClient.execute(httpPut, responseHandler);
	
	        logger.trace("----------------------------------------");
	        logger.trace(responseBody);
        } finally {
	        httpClient.close();
	    }
        return responseBody;
	}
	
	
	public static StatusLine deleteItWithCookie(BasicClientCookie cookie, String endpointUrl) throws Exception{
        BasicCookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookie(cookie);
        
        CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        return executeDelete(httpClient, endpointUrl, null);
	}
	
	public static StatusLine deleteIt(String endpointUrl) throws Exception{
		return deleteIt(endpointUrl, null);
	}
		
	public static StatusLine deleteIt(String endpointUrl, String authHeader) throws Exception{
		CloseableHttpClient httpClient = HttpClients.createDefault();
       	StatusLine status = executeDelete(httpClient, endpointUrl, authHeader);
        return status;
	}
	
	private static StatusLine executeDelete(CloseableHttpClient httpClient, String endpointUrl, String authHeader) throws Exception{
		StatusLine status = null;
		try {
			HttpDelete httpDelete = new HttpDelete(endpointUrl);
	    	if(authHeader != null)
	    		httpDelete.setHeader(HttpHeaders.AUTHORIZATION, authHeader);  
	    	
	    	CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpDelete);
	        try {
	            logger.trace("----------------------------------------");
	            status = response.getStatusLine();
	            logger.trace(status.toString());
	        } finally {
	            response.close();
	        } 		
		} finally {
	        httpClient.close();
	    }
        return status;
	}

	private static CloseableHttpClient getHttpClientWithCookie(BasicClientCookie cookie) {
	    BasicCookieStore cookieStore = new BasicCookieStore();
	    cookieStore.addCookie(cookie);
	    
	   return HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
	}
    
	private static ResponseHandler<String> createResponseHandler(){
        // Create a custom response handler
        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
            public String handleResponse(
                    final HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                logger.trace("Status: " + status);
                HttpEntity entity = response.getEntity();
                String resp = entity != null ? EntityUtils.toString(entity) : null;
                if (status >= 200 && status < 300) {
                    return resp;
                } else {
                    throw new ClientProtocolException("Unexpected response status : " + status + ". Resp body is : " + resp);
                }
            }
        };
		return responseHandler;
	}
	
    public static void main(String[] args) throws Exception {
        if (args.length != 1)  {
            System.out.println("File path not given");
            System.exit(1);
        }
        
    }

}
