# New Relic Plugin for Couchbase 
## Top Key Performance Indicators for Couchbase


----

### Requirements

- A New Relic account. Sign up for a free account [here](http://newrelic.com)
- A couchbase server that you want to monitor
- Java Runtime (JRE) environment Version 1.6 or later
- Network access to New Relic (proxies are supported, see details below)

----

### Installation & Usage Overview

1. Download the latest version of the agent: [newrelic_couchbase_plugin.tar.gz](https://github.com/sschwartzman/newrelic-couchbase-plugin/blob/master/dist/newrelic_couchbase_plugin.tar.gz?raw=true)
2. Gunzip & untar on couchbase server that you want to monitor your Couchbase server from
3. Configure `config/newrelic.json` 
  * [Click here for newrelic.json config details](#nrjson)
4. OPTIONAL: Copy `config/plugin.json` from the OS-specific templates in `config` and configure that file. 
  * [Click here for plugin.json config details](#pluginjson)
5. OPTIONAL: Configure `pluginctl.sh` to have the correct paths to Java and your plugin location
  * Set `PLUGIN_JAVA` to the location of Java on your server (including the "java" filename)
  * Set `PLUGIN_PATH` to the location of the couchbase Plugin
6. Run `chmod +x pluginctl.sh` to make the startup script executable (if it isn't already)
7. Run `./pluginctl.sh start` from command-line
8. Check logs (in `logs` directory by default) for errors
9. Login to New Relic UI and find your plugin instance
  * In the New Relic UI, select "Plugins" from the top level accordion menu 
  * Check for the "couchbase" plugin in left-hand column.  Click on it, your instance should appear in the list.

----

### <a name="nrjson"></a> Configuring the `newrelic.json` file

The `newrelic.json` is a standardized file containing configuration information that applies to any plugin (e.g. license key, logging, proxy settings), so going forward you will be able to copy a single `newrelic.json` file from one plugin to another.  Below is a list of the configuration fields that can be managed through this file:

#### Configuring your New Relic License Key

* Your New Relic license key is the only required field in the `newrelic.json` file as it is used to determine what account you are reporting to.
* Your license key can be found in New Relic UI, on 'Account settings' page.

##### Example

```
{
  "license_key": "YOUR_LICENSE_KEY_HERE"
}
```

#### Logging configuration

By default, this plugins will have logging turned on; however, you can manage these settings with the following configurations:

* `log_level` - The log level. Valid values: [`debug`, `info`, `warn`, `error`, `fatal`]. Defaults to `info`.
	* `debug` will expose the metrics being collected by each command.
* `log_file_name` - The log file name. Defaults to `newrelic_plugin.log`.
* `log_file_path` - The log file path. Defaults to `logs`.
* `log_limit_in_kbytes` - The log file limit in kilobytes. Defaults to `25600` (25 MB). If limit is set to `0`, the log file size will not be limited.

##### Example

```
{
  "license_key": "YOUR_LICENSE_KEY_HERE"
  "log_level": "info",
  "log_file_path": "/var/log/newrelic",
  "log_limit_in_kbytes": "4096"
}
```

#### <a name="proxyconfig"></a> Proxy configuration

If you are running your plugin from a machine that runs outbound traffic through a proxy, you can use the following optional configurations in your `newrelic.json` file:

* `proxy_host` - The proxy host (e.g. `webcache.example.com`)
* `proxy_port` - The proxy port (e.g. `8080`).  Defaults to `80` if a `proxy_host` is set
* `proxy_username` - The proxy username
* `proxy_password` - The proxy password

##### Examples

```
{
  "license_key": "YOUR_LICENSE_KEY_HERE",
  "proxy_host": "proxy.mycompany.com",
  "proxy_port": 9000
}
```

```
{
  "license_key": "YOUR_LICENSE_KEY_HERE",
  "proxy_host": "proxy.mycompany.com",
  "proxy_port": "9000",
  "proxy_username": "my_user",
  "proxy_password": "my_password"
}
```

----

###  <a name="pluginjson"></a> Configuring the `plugin.json` file

The `plugin.json` file contains the list of Couchbase servers name, host, port, username, and password. Configuration supports multiple Couchbase databases, or run the New Relic plugin per Couchbase server.

<a name="globalconf">
#### Global Configurations (NEW!)

Each plugin.json file now has a `global` object, which contains the optional configurations to be applied across all of the commands.

* Configurations in the `agents` array override what's in the `global` object.
  - I.e. If you want to turn on debug for one statement, you can set the `debug` object to false in the `global` object, and set it to true in that command's `agent` object.
* If you choose to use the old versions of plugin.json (without a `global` option), those will work fine.


#### Examples 

With the optional configurations left as the defaults, this is what your plugin.json might look like:
```
{
  "agents": [
    {
      "name" : "Couchbase01",
      "host" : "host1234",
      "port" : "8091",
      "username" : "serviceaccount",
      "password" : "serviceaccountpassword"
    }
  ]
}
```
