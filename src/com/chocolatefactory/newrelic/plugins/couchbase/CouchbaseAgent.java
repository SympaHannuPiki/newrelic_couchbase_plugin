package com.chocolatefactory.newrelic.plugins.couchbase;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.util.Logger;

/**
 * An agent for Wikipedia.
 * This agent will log both Articles Created and Article Count metrics.
 * @author jstenhouse
 */
public class CouchbaseAgent extends Agent {

    private static final String GUID = "com.chocolatefactory.newrelic.plugins.couchbase";
    private static final String VERSION = "0.0.1";
    private static final String CLUSTER_URI = "/pools/default";
	private static final String BUCKETS_URI = "/pools/default/buckets";
	private static final String METRIC_DIV = "/";
	
    private static final Logger logger = Logger.getLogger(CouchbaseAgent.class);
    
    @SuppressWarnings("unused")
	private String name, host, user, pass, protocol;
    private int port;
    private boolean debug;
    URL url; 
    
    public CouchbaseAgent(String name, String host, String port, String user, String pass, String protocol, boolean debug) {
        super(GUID, VERSION);
        this.name = name;
        this.host = host;
        this.port = Integer.parseInt(port);
        this.user = user;
        this.pass = pass;
        this.protocol = protocol;
        this.debug = debug;
    }

    @Override
    public void pollCycle() {
        getClusterMetrics();
        getBucketMetrics();
    }
    
    public void addMetrics(Map<String, Number>metricMap) {
    	for (Entry<String, Number> thisMetric : metricMap.entrySet()) {
    	    String metricName = thisMetric.getKey();
    	    double metricValue = (Double) thisMetric.getValue();
    	    String metricType = getMetricType(metricName);
    	    if(this.debug)
    	    	logger.info(metricName + ", " + metricType + ", " + metricValue);
    	    else
    	    	reportMetric(metricName, metricType, metricValue);
    	}
    }
 
    public void getClusterMetrics() {
    	Map<String, Number> clusterMetrics = new HashMap<String, Number>();
    	Object clusterObject = getJSONResponse(CLUSTER_URI);
		if((clusterObject != null) && (clusterObject instanceof JSONObject)) {
    		clusterMetrics.putAll(getMetricsFromJSON((JSONObject)((JSONObject)clusterObject).get("storageTotals"), "storageTotals"));
    		JSONArray nodeObject = (JSONArray)((JSONObject)clusterObject).get("nodes");
    		for(Object arrayObject : nodeObject) {
    			clusterMetrics.putAll(getMetricsFromJSON((JSONObject)arrayObject, "nodes" + getObjectName((JSONObject)arrayObject)));
            }
       	}
		addMetrics(clusterMetrics);
    }
    
    public void getBucketMetrics() {
    	Map<String, Number> bucketMetrics = new HashMap<String, Number>();
    	Object bucketObject = getJSONResponse(BUCKETS_URI);
		if((bucketObject != null) && (bucketObject instanceof JSONArray)) {
        	for(Object arrayObject : (JSONArray)bucketObject) {
        		bucketMetrics.putAll(getMetricsFromJSON((JSONObject)arrayObject, "buckets" + getObjectName((JSONObject)arrayObject)));
        	}
       	}
		addMetrics(bucketMetrics);
    }
    
    public String getMetricType(String metricName) {
    	return "ms";
    }
    
    private Object getJSONResponse(String uri) {
        Object response = null;
        InputStream inputStream = null;
        HttpURLConnection connection = null;
        try {
        	new URL(this.protocol, this.host, port, uri);
        	URL thisURL = new URL(this.protocol, this.host, port, uri);
            connection = (HttpURLConnection) thisURL.openConnection();
            connection.addRequestProperty("Accept", "application/json");
            inputStream = connection.getInputStream();
            response = JSONValue.parse(new InputStreamReader(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {}
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        try {
            return (JSONObject) response;
        } catch (ClassCastException cce) {
        	try {
        		return (JSONArray) response;
        	} catch (ClassCastException ccce) {
        		return null;
        	}
        }
    }
    
    private Map<String, Number>getMetricsFromJSON(JSONObject theObject, String prefix) {
    	String thePrefix = prefix + METRIC_DIV;
    	Map<String, Number> theMetrics = new HashMap<String, Number>();
    	Iterator<?> keys = theObject.keySet().iterator();
        while(keys.hasNext()){
            String key = (String)keys.next();
            if( theObject.get(key) instanceof JSONObject) {
            	theMetrics.putAll(getMetricsFromJSON((JSONObject)theObject.get(key), thePrefix + key));
            } else if (theObject.get(key) instanceof JSONArray) {
            	for(Object arrayObject : (JSONArray)theObject.get(key)) {
            		if(arrayObject instanceof JSONObject) {
            			theMetrics.putAll(getMetricsFromJSON((JSONObject)arrayObject, thePrefix + key + getObjectName((JSONObject)arrayObject)));
            		}
            	}
            } else {
            	try  {  
            	    double value = Double.parseDouble(theObject.get(key).toString());
            	    theMetrics.put(thePrefix + key, value);
            	} catch(NumberFormatException nfe) { 
            		  // It's a string, so not a metric, so don't send.
            	}
            }
        }
    	
    	return theMetrics;
    }

	@Override
	public String getAgentName() {
        return name;
	}
	
	public String getObjectName(JSONObject theObject) {
		if (theObject.containsKey("name")) {
			return METRIC_DIV + (String)theObject.get("name");
		} else if (theObject.containsKey("hostname")) {
			return METRIC_DIV + (String)theObject.get("hostname");
		} else
			return "";
	}
}
