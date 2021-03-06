JMS Restful Bridge
=================
# Introduction

Annotation based MQ adapter, alternative of wmq resouce adaptor.
# Feature
1. Configurable backout queues and threads.
2. Full of start/Stop control via CDI event or JMX.
3. Standalone JMS to Restful transparent bridge.

# Getting Start

1. Run "etc/install_mq_dependencies.bat" to install mq dependencies to local repository;
2. Run "mvn package" to build;

## Integrate in JSE and JEE.

#### Run "etc/publish_jms_util.bat" to publish project to local repository;
#### Include maven depenceny:

	<dependency>
		<groupId>com.cheeray</groupId>
		<artifactId>jms-rest</artifactId>
		<version>1.0-SNAPSHOT</version>
	</dependency>
#### Configure system properties:

	JMS_UTIL_WAIT_INTERVAL: Timeout of reading a message, default is 1000, set to 0 will be waiting unlimitedly.
	JMS_UTIL_RETRY_INTERVAL: Interval to retry connection, default is 10000.
	JMS_UTIL_SLEEP_INTERVAL: Interval to wait for new message, default is 100ms.
#### Annotation (consumer):

	/**Annotate the class with @MAware and the method that consuming a string message with @MConsumer, for example:*/
	@MAware
	public class Consumer {
		...
		@MConsumer({ @MQueue(channel = "CLIENT.MQ.CHANNEL", host = "MQ_HOST",
				manager = "MQ_MANAGER", port = 3434, queue = "MQ.QUEUE.IN") })
		public void onInbound(String msg) throws InterruptedException {
			// TODO: process the string message.
		}
		...
	}
#### Annotation (producer):

	/**Annotate the class with @MAware and the method that consuming a string message with @MProducer, for example:*/
	@MAware
	public class Producer {
		...
		@MProducer(value = { @MQueue(channel = "CLIENT.MQ.CHANNEL",
			host = "MQ_HOST", manager = "MQ_MANAGER", port = 3434,
			queue = "MQ.QUEUE.IN") }, backouts = { @MQueue(
			channel = "CLIENT.MQ.CHANNEL.BO", host = "MQ_HOST_BO",
			manager = "MQ_MANAGER_BO", port = 3434, queue = "MQ.QUEUE.IN.BO") })
		public String send(...) throws InterruptedException {
			// TODO: process the string message.
		}
		...
	}
## Known issues:
Before CDI 1.2, producer is not auto intercepted, see <http://docs.jboss.org/weld/reference/latest/en-US/html_single/#_enabling_interceptors>. </br>

Alternatively, a DispatchEvent can be fired inside the producing method to send the message directly.
 
## Run JMS-Restful bridge:

#### Configuration

+ Copy "conf/jms-rest.yaml.example" to "conf/jms-rest.yaml" and modify below settings:

###### Inbound and Outbound MQ setting:

	Setting	|	Description
	----------|----------
	host      | MQ host
	port		| MQ port
	channel	| MQ channel
	manager	| MQ manager
	queues		| Queue names
	threads	| Number of threads to run
 


###### Inbound and Outbound RESTful setting:

	Setting	|	Description
	----------|-------------
	scheme		|HTTP scheme
	server		|RESTful server
	port		|Listen/Pubish port
	context	|RESTful service context


+ Run "etc/start-jms-rest.bat" to start as standalone mode.
+ Or copy "init.d/jms-rest.sh" to "/etc/init.d/" and run as Linux service.


#### Linux service

1. Create a nologin user (default "jms-rest") and set to "JMS_REST_USER" in "JMS_REST_CONF", default configuration file is "/etc/default/jms-rest.conf".
2. Set "JMS_REST_HOME" (default "/opt/jms-rest").
3. Copy "bin", "conf" and "target/jms-rest-bridge.jar" to "JMS_REST_HOME".
4. Copy "init.d/jms-rest.sh" to "/etc/init.d/" and "chmod" as runnable Linux service.
5. Start "service jms-rest start".

#### Log

Set "JMS_REST_LOG_DIR" to the directory for logs. Default is "/var/log/jms-rest" and "bridge.log" is the log file.

### License  
Apache License Version 2.0