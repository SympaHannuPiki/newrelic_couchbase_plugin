package com.chocolatefactory.newrelic.plugins.couchbase;

import java.util.Map;

import com.newrelic.metrics.publish.Agent;
import com.newrelic.metrics.publish.AgentFactory;
import com.newrelic.metrics.publish.configuration.ConfigurationException;

public class CouchbaseAgentFactory extends AgentFactory {

    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    
    @Override
    public Agent createConfiguredAgent(Map<String, Object> properties) throws ConfigurationException {
        String name = (String) properties.get("name");
        String host = (String) properties.get("host");
        String port = (String) properties.get("port");
        String user = (String) properties.get("username");
        String pass = (String) properties.get("password");
        String protocol = properties.containsKey("ssl_enabled") && properties.get("ssl_enabled").equals("true") ? HTTPS : HTTP;
        boolean debug = properties.containsKey("debug") && properties.get("debug").equals("true") ? true : false;
        
        if (name == null || host == null || port == null || user == null || pass == null) {
            throw new ConfigurationException("Ensure that name, host, port, username & password are set for your Couchbase Server in config/plugin.json.");
        }
                
        return new CouchbaseAgent(name, host, port, user, pass, protocol, debug);
    }
}
