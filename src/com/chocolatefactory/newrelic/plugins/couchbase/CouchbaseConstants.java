package com.chocolatefactory.newrelic.plugins.couchbase;

import java.util.HashMap;

public class CouchbaseConstants {

	public static final String kProtocolHTTP = "http";
	public static final String kProtocolHTTPS = "https";
	public static final String kDefaultMetricType = "items";
	public static final char kMetricTreeDivider = '/';
	public static final String kDefaultAgentName = "couchbase";
	public static final String kCategoryMetricName = "Component";
	public static final String kCouchbaseAgentVersion = "0.0.1";
	public static final String kCouchbaseAgentGuid = "com.chocolatefactory.newrelic.plugins.couchbase";
	public static final String kCouchbaseClusterURI = "/pools/default";
	public static final String kCouchbaseBucketsURI = "/pools/default/buckets";
	public static final String kSkipMetric = "SKIP";
	
	static final HashMap<String, String> CouchbaseMetrics = new HashMap<String, String>();
	
	static {	
		CouchbaseMetrics.put("clusterCompatibility", kSkipMetric);
		CouchbaseMetrics.put("cmd_get","commands");
		CouchbaseMetrics.put("couch_docs_actual_disk_size","bytes");
		CouchbaseMetrics.put("couch_docs_data_size","bytes");
		CouchbaseMetrics.put("couch_views_actual_disk_size","bytes");
		CouchbaseMetrics.put("couch_views_data_size","bytes");
		CouchbaseMetrics.put("cpu_utilization_rate","%");
		CouchbaseMetrics.put("curr_items_tot","items");
		CouchbaseMetrics.put("curr_items","items");
		CouchbaseMetrics.put("dataUsed","%");
		CouchbaseMetrics.put("direct", kSkipMetric);
		CouchbaseMetrics.put("diskFetches","fetches");
		CouchbaseMetrics.put("diskUsed","bytes");
		CouchbaseMetrics.put("ep_bg_fetched","fetches");
		CouchbaseMetrics.put("free","bytes");
		CouchbaseMetrics.put("get_hits","hits");
		CouchbaseMetrics.put("itemCount","items");
		CouchbaseMetrics.put("mcdMemoryAllocated","bytes");
		CouchbaseMetrics.put("mcdMemoryReserved","bytes");
		CouchbaseMetrics.put("mem_used","bytes");
		CouchbaseMetrics.put("memoryFree","bytes");
		CouchbaseMetrics.put("memoryTotal","bytes");
		CouchbaseMetrics.put("memUsed","bytes");
		CouchbaseMetrics.put("numReplicas","replicas");
		CouchbaseMetrics.put("ops","ops");
		CouchbaseMetrics.put("opsPerSec","ops");
		CouchbaseMetrics.put("proxy", kSkipMetric);
		CouchbaseMetrics.put("proxyPort", kSkipMetric);
		CouchbaseMetrics.put("quotaPercentUsed","%");
		CouchbaseMetrics.put("quotaTotal","bytes");
		CouchbaseMetrics.put("quotaTotalPerNode","bytes");
		CouchbaseMetrics.put("quotaUsed","bytes");
		CouchbaseMetrics.put("quotaUsedPerNode","bytes");
		CouchbaseMetrics.put("ram","bytes");
		CouchbaseMetrics.put("rawRAM","bytes");
		CouchbaseMetrics.put("replicaNumber", kSkipMetric);
		CouchbaseMetrics.put("replication", kSkipMetric);
		CouchbaseMetrics.put("swap_total","bytes");
		CouchbaseMetrics.put("swap_used","bytes");
		CouchbaseMetrics.put("threadsNumber", kSkipMetric);
		CouchbaseMetrics.put("total","bytes");
		CouchbaseMetrics.put("uptime","sec");
		CouchbaseMetrics.put("used","bytes");
		CouchbaseMetrics.put("usedByData","bytes");
		CouchbaseMetrics.put("vb_replica_curr_items","items");
	}
}

