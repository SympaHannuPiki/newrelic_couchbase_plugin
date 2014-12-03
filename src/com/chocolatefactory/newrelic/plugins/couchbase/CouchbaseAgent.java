package com.chocolatefactory.newrelic.plugins.couchbase;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.util.Logger;

/**
 * An agent for Wikipedia. This agent will log both Articles Created and Article
 * Count metrics.
 * 
 * @author jstenhouse
 */
public class CouchbaseAgent extends Agent {

	private static final Logger logger = Logger.getLogger(CouchbaseAgent.class);

	private String name, host, protocol, user, pass;
	private int port;
	private boolean debug, sslHostnameVerification, sslTrustCerts;

	public CouchbaseAgent(String name, String host, String port, String user,
			String pass, String protocol, boolean sslhv, boolean ssltc, boolean debug) {
		super(CouchbaseConstants.kCouchbaseAgentGuid,
				CouchbaseConstants.kCouchbaseAgentVersion);
		this.name = name;
		this.host = host;
		this.port = Integer.parseInt(port);
		this.user = user;
		this.pass = pass;
		this.protocol = protocol;
		this.sslHostnameVerification = sslhv;
		this.sslTrustCerts = ssltc;
		this.debug = debug;
		CouchbaseAgent.logger.info("Initializating Couchbase Agent: " + protocol + "://" + host + ":" + port);
	}

	public void addMetrics(Map<String, Number> metricMap) {
		for (Entry<String, Number> thisMetric : metricMap.entrySet()) {
			String metricName = thisMetric.getKey();
			double metricValue = (Double) thisMetric.getValue();
			String metricType = this.getMetricType(metricName);
			if (!metricType.equals(CouchbaseConstants.kSkipMetric)) {
				if (this.debug) {
					CouchbaseAgent.logger.info(metricName + ", " + metricType + ", " + metricValue);
				} else {
					this.reportMetric(metricName, metricType, metricValue);
				}
			}
		}
	}

	@Override
	public String getAgentName() {
		return this.name;
	}

	public void getBucketMetrics() {
		Map<String, Number> bucketMetrics = new HashMap<String, Number>();
		Object bucketObject = this
				.getJSONResponse(CouchbaseConstants.kCouchbaseBucketsURI);
		if ((bucketObject != null) && (bucketObject instanceof JSONArray)) {
			for (Object arrayObject : (JSONArray) bucketObject) {
				bucketMetrics.putAll(this.getMetricsFromJSON((JSONObject) arrayObject, 
					"buckets" + this.getObjectName((JSONObject) arrayObject)));
			}
			// Producing summary metrics by leaving out bucket name
			for (Object arrayObject : (JSONArray) bucketObject) {
				bucketMetrics.putAll(this.getMetricsFromJSON((JSONObject) arrayObject, "summary_buckets"));
			}
		}
		this.addMetrics(bucketMetrics);
	}

	public void getClusterMetrics() {
		Map<String, Number> clusterMetrics = new HashMap<String, Number>();
		Object clusterObject = this.getJSONResponse(CouchbaseConstants.kCouchbaseClusterURI);
		if ((clusterObject != null) && (clusterObject instanceof JSONObject)) {
			clusterMetrics.putAll(this.getMetricsFromJSON((JSONObject) ((JSONObject) clusterObject)
				.get("storageTotals"), "storageTotals"));
			JSONArray nodeObject = (JSONArray) ((JSONObject) clusterObject).get("nodes");
			for (Object arrayObject : nodeObject) {
				clusterMetrics.putAll(this.getMetricsFromJSON((JSONObject) arrayObject,
					"nodes" + this.getObjectName((JSONObject) arrayObject)));
			}
			// Producing summary metrics by leaving out node name
			for (Object arrayObject : nodeObject) {
				clusterMetrics.putAll(this.getMetricsFromJSON((JSONObject) arrayObject, "summary_nodes"));
			}
		}
		this.addMetrics(clusterMetrics);
	}

	private Object getJSONResponse(String uri) {
		Object response = null;
		InputStream inputStream = null;
		URLConnection connection = null;
		URL url = null;
		try {
			url = new URL(this.protocol, this.host, this.port, uri);
			if (this.protocol.equals(CouchbaseConstants.kProtocolHTTPS)) {
				SSLContext sslcontext = SSLContext.getInstance("TLS");
				if (this.sslTrustCerts) {
					CouchbaseAgent.logger.debug("Setting HTTPS to trust all Certificates.");
					X509TrustManager trustmanager = new X509TrustManager() {
						@Override
						public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException { }
						@Override
						public void checkServerTrusted(X509Certificate[] paramArrayOfX509Certificate, String paramString) throws CertificateException { }
						@Override
						public X509Certificate[] getAcceptedIssuers() { return null; }
					};
					sslcontext.init(null, new TrustManager[]{ trustmanager }, null);
					SSLContext.setDefault(sslcontext);
				}
				connection = url.openConnection();
				((HttpsURLConnection) connection).setRequestMethod("GET");
				if (!this.sslHostnameVerification) {
					CouchbaseAgent.logger.debug("Setting HTTPS Hostname Verification to always return true.");
					((HttpsURLConnection) connection).setHostnameVerifier(new HostnameVerifier() {
						@Override
						public boolean verify(String hostname, SSLSession session) { return true; }
					});
				}
			} else {
				connection = url.openConnection();
				((HttpURLConnection) connection).setRequestMethod("GET");
			}
			connection.addRequestProperty("Accept", "application/json");
			if (!((this.user == null) || (this.pass == null))) {
				String authString = this.user + ":" + this.pass;
				String authStringEnc = Base64.encodeBase64String(authString.getBytes());
				connection.addRequestProperty("Authorization", "Basic " + authStringEnc);
			}
			inputStream = connection.getInputStream();
			response = JSONValue.parse(new InputStreamReader(inputStream));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) { }
			}
			if (connection != null) {
				((HttpURLConnection) connection).disconnect();
			}
		}

		// Try to return it as a JSON Object or JSON Array.
		// If it's neither, then it's not what we're looking for!
		try {
			return response;
		} catch (ClassCastException cce) {
			try {
				return response;
			} catch (ClassCastException ccce) {
				CouchbaseAgent.logger.error(url.toString() + " did not return valid JSON data.");
				return null;
			}
		}
	}

	private Map<String, Number> getMetricsFromJSON(JSONObject jsonObject, String prefix) {
		String fullPrefix = prefix + CouchbaseConstants.kMetricTreeDivider;
		Map<String, Number> metrics = new HashMap<String, Number>();
		Iterator<?> keys = jsonObject.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String) keys.next();
			if (jsonObject.get(key) instanceof JSONObject) {
				metrics.putAll(this.getMetricsFromJSON((JSONObject) jsonObject.get(key), fullPrefix + key));
			} else if (jsonObject.get(key) instanceof JSONArray) {
				for (Object arrayObject : (JSONArray) jsonObject.get(key)) {
					if (arrayObject instanceof JSONObject) {
						metrics.putAll(this.getMetricsFromJSON((JSONObject) arrayObject,
							fullPrefix + key + this.getObjectName((JSONObject) arrayObject)));
					}
				}
			} else {
				try {
					double value = Double.parseDouble(jsonObject.get(key).toString());
					metrics.put(fullPrefix + key, value);
				} catch (NumberFormatException nfe) {
					// It's a string, so not a metric, so don't send.
				}
			}
		}
		return metrics;
	}

	public String getMetricType(String metricFullName) {
		String metricName;
		if ((metricFullName != null) && (metricFullName.length() > 0)) {
			int lastSlash = metricFullName.lastIndexOf(CouchbaseConstants.kMetricTreeDivider);
			if (lastSlash != -1) {
				metricName = metricFullName.substring(lastSlash + 1).trim();
			} else {
				metricName = metricFullName.trim();
			}
		} else {
			return CouchbaseConstants.kDefaultMetricType;
		}

		if (CouchbaseConstants.CouchbaseMetrics.containsKey(metricName)) {
			return CouchbaseConstants.CouchbaseMetrics.get(metricName);
		} else {
			metricName = metricName.toLowerCase();
			if (metricName.contains("bytes")
					|| metricName.contains("disk")
					|| metricName.contains("mem")
					|| metricName.contains("swap")) {
				return "bytes";
			} else if (metricName.contains("cmd")) {
				return "commands";
			} else if (metricName.contains("fetch")) {
				return "fetches";
			} else if (metricName.contains("items")) {
				return "items";
			} else if (metricName.contains("ops")) {
				return "ops";
			} else if (metricName.contains("percent")) {
				return "%";
			} else {
				return CouchbaseConstants.kDefaultMetricType;
			}
		}
	}

	public String getObjectName(JSONObject theObject) {
		if (theObject.containsKey("name")) {
			return CouchbaseConstants.kMetricTreeDivider + (String) theObject.get("name");
		} else if (theObject.containsKey("hostname")) {
			return CouchbaseConstants.kMetricTreeDivider + (String) theObject.get("hostname");
		} else {
			return "";
		}
	}

	@Override
	public void pollCycle() {
		this.getClusterMetrics();
		this.getBucketMetrics();
	}
}