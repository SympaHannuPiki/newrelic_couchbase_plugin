package com.chocolatefactory.newrelic.plugins.couchbase;

import java.util.Map;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.ConfigurationException;

public class CouchbaseAgentFactory extends AgentFactory {

	@Override
	public Agent createConfiguredAgent(Map<String, Object> properties) throws ConfigurationException {
		// host & port are required
		final String host = (String) properties.get("host");
		final String port = (String) properties.get("port");

		// name, user, pass, protocol & debug are optional
		final String name = properties.containsKey("name") && !((String)properties.get("name")).isEmpty() ?
			(String) properties.get("name") : (String) properties.get("host");
		final String user = properties.containsKey("username") ? (String) properties.get("username") : null;
		final String pass = properties.containsKey("password") ? (String) properties.get("password") : null;
		final String protocol = properties.containsKey("ssl_enabled") && properties.get("ssl_enabled").equals("true") ? 
			CouchbaseConstants.kProtocolHTTPS : CouchbaseConstants.kProtocolHTTP;
		final boolean sslhv = properties.containsKey("ssl_hostname_verification") && 
			properties.get("ssl_hostname_verification").equals("false") ? false : true;
		final boolean ssltc = properties.containsKey("ssl_trust_all_certs") && 
			properties.get("ssl_trust_all_certs").equals("true") ? true : false;
		final boolean debug = properties.containsKey("debug") && properties.get("debug").equals("true") ? true : false;

		if ((host == null) || (port == null)) {
			throw new ConfigurationException("Ensure that at least host and port are set for your Couchbase Server in config/plugin.json.");
		}
		
		// public CouchbaseAgent(String name, String host, String port, String user, String pass, String protocol, boolean sslhv, boolean debug)
		return new CouchbaseAgent(name, host, port, user, pass, protocol, sslhv, ssltc, debug);
	}
}