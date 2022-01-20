j2ee_messaging_apps: JMS Messaging Apps using common Java EE patterns and Resource Adapter JCA patterns
=========================================================================================================

Author: Fabien Sanglier [github profile](https://github.com/lanimall)

Project Source: <https://github.com/lanimall/j2ee_messaging_apps>

What is it?
--------------------------------------------
Modular J2EE messaging applications that rely on common JCA Resource Adapters to interact with Messaging provider such as SoftwareAG Universal Messaging.
Using these applications, it's easy to create different message pub/sub designs by "plugging" multiple apps together via EJB lookups.

These J2EE apps are compliant with EJB specs and JCA specs, and have been succesfully tested on both JBOSS EAP 6.x and IBM Websphere 8.x platforms,
interacting with the the [Software AG Universal Messaging Server](http://www2.softwareag.com/it/products/terracotta/universal_messaging.aspx) via its JCA-compliant Resource Adapter.

**For any issue**: 
Please create a ticket on github and the developper will make every attempts to address it as soon as possible.
Or even better: feel free to fork, fix it and submit a PR.

Licensing - Apache-2.0
--------------------------------------------

This project is Licensed under the Apache License, Version 2.0 (the "License");
You may not use this project except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Code Content
--------------------------------------------

* SimpleJmsSend
  * Sends JMS messages to UM queues/topics via JCA proxies (JCA Admin objects and Conection Factories) using the Resource Adapter:
    * "Send And Forget"
    * "Send And Wait For Reply"
  * Exposes web front end to drive load
  * Implements remote interface to plug this into other app via EJB Remoting
* SimpleJmsConsume
  * Consumes JMS messages from queues/topics using the Resource Adapter,
  * Ability to perform some mock processing (eg. sleep time, mock exceptions at intervals, etc...),
  * Ability to call remote EJBs that implement the MessageInterop interface (eg. the ones in the SimpleJmsSend application) in order to chain message comsumption with extra message sending,
  * Ability to reply if the "replyTo" field is specified in the message (or if a default "replyTo" is set)
* SimpleSoapJmsClient
  * JAX-WS client that works with the SimpleSoapJmsServer to communicate with the SOAP Endpoint
  * Implements both SOAP-over-HTTP and SOAP-over-JMS mechanisms to test and compare
  * Exposes web front end to drive load
  * Implements remote interface to plug this into other app via EJB Remoting
* SimpleSoapJmsServer
  * Creates a simple SOAP endpoint (JAX-WS) for OneWay and TwoWay SOAP requests
  * Ability to use either HTTP or JMS to communicate with SOAP to this endpoint
* libs
  * Shared library that contains global code and especially the custom-built JMSHelper that encapsulate simple JMS contructs
  * A simple counter (accessible via servlet) is also made available on every apps to track various metrics (message sent, message consumed, processing success, processing errors, etc...)
  

Pre-requisites for building the apps
--------------------------------------------

* [Apache Ant](https://ant.apache.org/)
* [Java](http://openjdk.java.net)
* [Apache Maven](http://apache.spinellicreations.com/maven/maven-3/3.5.4/binaries/apache-maven-3.5.4-bin.zip)

In order to build, the maven home path must be provided in either:
* An environment property "MAVEN_HOME" or "M2_HOME", OR
* Specified in the "maven.home" property in the [build.properties](./build.properties) file

Resource Adapter Guides
--------------------------------------------

Detail guides to setup Software AG Universal Messaging Resource Adapter on both JBOSS EAP 6 and IBM Websphere 8.x are availble at:

* [Integration and Configuration of SoftwareAG’s Universal Messaging with JBOSS EAP 6.1](http://techcommunity.softwareag.com/web/guest/pwiki/-/wiki/Main/Integration+and+Configuration+of+SoftwareAG’s+Universal+Messaging+with+JBOSS+EAP+6.1)
* [Integration and configuration of sofwareag’s universal messaging with ibm websphere application server](http://techcommunity.softwareag.com/web/guest/pwiki/-/wiki/Main/integration+and+configuration+of+sofwareag’s+universal+messaging+with+ibm+websphere+application+server)

Quick Start Guide with Different Samples
--------------------------------------------

All samples below require a JCA Resource Adapter installed on the Application Server. 
Please refer to the section "Resource Adapter Guides" for detailed guides.

*NOTE1*: All the samples to be deployed on JBOSS rely on the following server resources:
1. UM Resource Adapter deployed with name "umra.rar" (this name is explicitely used in the jboss config to map the MDBs to the right resource adapter)
2. A resource bean pool should be created with name "umra-strict-max-pool" (this name is explicitely used in the jboss config to make the MDBs used this pool)

If you wanted to change these names, simply update the [SimpleJmsConsume/build.default.properties](./SimpleJmsConsume/build.default.properties) file.
* Consumer.jboss.resource-adapter-name=umra.rar
* Consumer.jboss.bean-instance-pool=

*NOTE2*: For each profile, you have several build flag available for customization:

1. Build for JBOSS or Websphere (to apply the right EJB lookup properties)

Defined by build variable "j2ee-jms-examples.deployment_target" with following possible values
```
-Dj2ee-jms-examples.deployment_target={jboss|websphere}
```
Default if not specified: jboss

2. Set logging level for the built application

Defined by build variable "j2ee-jms-examples.logging.level" with following possible values:
```
j2ee-jms-examples.logging.level={info|debug|trace|warn|error}
```
Default if not specified: info

### Sample Profile 1a: Simple JMS "Send and Forget" + Consume

#### Description

This sample sends JMS messages to a queue, and consume from that same queue (2 apps will be built and deployed)
The apps come with pre-configured resource names that must be created on the application server(s). 

#### Building the apps

Jboss:
```
   ant -Dj2ee-jms-examples.deployment_target=jboss -Dj2ee-jms-examples.deployment_profilename=profile1a assemble
```

Websphere:
```
   ant -Dj2ee-jms-examples.deployment_target=websphere -Dj2ee-jms-examples.deployment_profilename=profile1a assemble
```

2 applications should be created:
* dist/SimpleJmsSend.ear
* dist/SimpleJmsConsume.ear

#### Configuration pre-requisites before deploying the apps on the Application Server

The applications requires some JCA Resource Adapter resources that must be created on the application server.
While all the resource names can be easily modified, the current apps require the following resources to be created as-is.
Failure to create a resource or missing a resource attrribute will result in the apps not starting due to errors. 

##### Objects to be created on the Messaging provider

* Queues:
  * JMSSamples/SimpleQueue
* Connection Factories:
  * SimpleJmsSendConnectionFactory
  * SimpleJmsConsumerConnectionFactory

##### RA Objects to be created on the Application servers

* Resource Adapter
  * Managed Connection Factory:
    * SimpleJmsSendConnectionFactory (name="SimpleJmsSendConnectionFactory", jndi-name="java:/jms/SimpleJmsSendConnectionFactory", class-name="com.sun.genericra.outbound.ManagedJMSConnectionFactory")
      * Properties:
        * Key=ConnectionFactoryJndiName // Value=SimpleJmsSendConnectionFactory
  * Managed Admin Object:
    * SimpleJmsSendDestination (name="SimpleJmsSendDestination", jndi-name="java:/jms/SimpleJmsSendDestination", class-name="com.sun.genericra.outbound.QueueProxy")
      * Properties:
        * Key=DestinationJndiName // Value=JMSSamples/SimpleQueue
  * Managed Activation Specification (not available in Jboss -- see note below)
    * SimpleJmsConsumer (name="SimpleJmsConsumer", jndi-name="java:/jms/SimpleJmsConsumer", class-name="com.sun.genericra.inbound.ActivationSpec")
      * Properties:
        * Key=connectionFactoryJndiName // Value=SimpleJmsConsumerConnectionFactory
        * Key=destinationJndiName // Value=JMSSamples/SimpleQueue

**NOTE**: Not all application servers provide abstraction for activation specs. Please refer to your application server documentation.
For example, Websphere provides such abstraction in the Admin Console.
Whereas for Jboss, the activation specs are directly provided by the application in the jboss descriptor (jboss-ejb3.xml),
and I abstracted these settings in the application build.properties for easy modification.

The application supports and will work with both mechanisms.

#### Deploy the apps and run

Deploy the EARs using the standard mechanism for the application server of choice.
The deployment can use all the defaults...and should not require any configuration set at runtimte.

To send messages, simply call the following url:
* http://APP_SERVER_HOST:PORT/SimpleJmsSend/JmsSendAndForget

To access the various implemented counters that track the sends and consumes, here is the URL:
* http://APP_SERVER_HOST:PORT/SimpleJmsSend/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsume/messagecounters

**NOTE**: In the event that the application does not start, it's very likely that 
something was not setup right with the application server configuration related to the resource adapter configurations 
(eg. a managed connection factory or managed admin object not present or configured right etc...)
In most case, reviewing the application server logs is a good start to see what may have gone wrong.

### Sample Profile 2a: Simple JMS "Send with Reply" + consume the reply (either synchronously or asynchronously)

#### Description

This sample demonstrates a "request / reply" use case with JMS messaging constructs 
(which is a common data exchange pattern that adds various benefits like data delivery reliability, failure recovery, built-in load balancing, etc...)

3 applications are created in this use case:
* SimpleJmsSendWithReply.ear:
  * Sends JMS messages to a queue with a ReplyTo header.
  * 2 options for sends: 
    * "Send and Forget" (Asynchronous send and reply - Reply consumed by another app)
    * "Send and Wait" for reply (Synchronous send and wait - reply consumed in the same waiting thread)
* SimpleJmsConsumeAndReply.ear:
  * receives the message, processes it and reply to the appropriate destination based on the ReplyTo header embedded in the received message.
* SimpleJmsConsumeTheReply.ear
  * consumes the Asynchronous reply message sent by the "Send and Forget" option

#### Building the apps

Jboss:
```
   ant -Dj2ee-jms-examples.deployment_target=jboss -Dj2ee-jms-examples.deployment_profilename=profile2a assemble
```

Websphere:
```
   ant -Dj2ee-jms-examples.deployment_target=websphere -Dj2ee-jms-examples.deployment_profilename=profile2a assemble
```

3 applications should be created:
* dist/SimpleJmsSendWithReply.ear
* dist/SimpleJmsConsumeAndReply.ear
* dist/SimpleJmsConsumeTheReply.ear

#### Configuration pre-requisites before deploying the apps on the Application Server

The built applications require some JCA Resource Adapter resources that must be created on the application server.
While all the resource names can be easily modified, the current apps require the following resources to be created as-is.
Failure to create a resource or missing a resource attrribute will result in the apps not starting due to errors.

##### Objects to be created on the Messaging provider

* Queues:
  * JMSSamples/RequestQueue
  * JMSSamples/ReplyQueueSync
  * JMSSamples/ReplyQueueAsync
* Connection Factories:
  * SimpleJmsSendWithReplyConnectionFactory
  * SimpleJmsConsumerConnectionFactory

##### RA Objects to be created on the Application servers

* Resource Adapter
  * Managed Connection Factory:
    * SimpleJmsSendWithReplyConnectionFactory (name="SimpleJmsSendWithReplyConnectionFactory", jndi-name="java:/jms/SimpleJmsSendWithReplyConnectionFactory", class-name="com.sun.genericra.outbound.ManagedJMSConnectionFactory")
      * Properties:
        * Key=ConnectionFactoryJndiName // Value=SimpleJmsSendWithReplyConnectionFactory
        * Key=useProxyMessages // Value=true
  * Managed Admin Object:
    * SimpleJmsSendWithReplyDestination (name="SimpleJmsSendWithReplyDestination", jndi-name="java:/jms/SimpleJmsSendWithReplyDestination", class-name="com.sun.genericra.outbound.QueueProxy")
      * Properties:
        * Key=DestinationJndiName // Value=JMSSamples/RequestQueue
    * SimpleJmsAsyncReplyDestination (name="SimpleJmsAsyncReplyDestination", jndi-name="java:/jms/SimpleJmsAsyncReplyDestination", class-name="com.sun.genericra.outbound.QueueProxy")
      * Properties:
        * Key=DestinationJndiName // Value=JMSSamples/ReplyQueueAsync
    * SimpleJmsSyncReplyDestination (name="SimpleJmsSyncReplyDestination", jndi-name="java:/jms/SimpleJmsSyncReplyDestination", class-name="com.sun.genericra.outbound.QueueProxy")
      * Properties:
        * Key=DestinationJndiName // Value=JMSSamples/ReplyQueueSync
  * Managed Activation Specification:
    * SimpleJmsConsumeRequest (name="SimpleJmsConsumeRequest", jndi-name="java:/jms/SimpleJmsConsumeRequest", class-name="com.sun.genericra.inbound.ActivationSpec")
      * connectionFactoryJndiName=SimpleJmsConsumerConnectionFactory
      * destinationJndiName=JMSSamples/RequestQueue
    * SimpleJmsConsumeAsyncReply (name="SimpleJmsConsumeAsyncReply", jndi-name="java:/jms/SimpleJmsConsumeAsyncReply", class-name="com.sun.genericra.inbound.ActivationSpec")
      * connectionFactoryJndiName=SimpleJmsConsumerConnectionFactory
      * destinationJndiName=JMSSamples/ReplyQueueAsync
 
**NOTE**: Not all application servers provide abstraction for activation specs. Please refer to your application server documentation.
For example, Websphere provides such abstraction in the Admin Console.
Whereas for Jboss, the activation specs are directly provided by the application in the jboss descriptor (jboss-ejb3.xml),
and I abstracted these settings in the application build.properties for easy modification.

The application supports and will work with both mechanisms.

#### Deploy the apps and run

Deploy the EARs using the standard mechanism for the application server of choice.
The deployment can use all the defaults...and should not require any configuration set at runtimte.

To send messages (using either "Send And Forget" or "Send and Wait"), simply call the following url:
* http://APP_SERVER_HOST:PORT/SimpleJmsSendWithReply/JmsSendAndForget
* http://APP_SERVER_HOST:PORT/SimpleJmsSendWithReply/JmsSendAndWait

To access the various implemented counters that track the sends and consumes, here is the URL:
* http://APP_SERVER_HOST:PORT/SimpleJmsSendWithReply/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsumeAndReply/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsumeTheReply/messagecounters


### Sample Profile 1b: JMS "Send to a Driver queue" which drives profile 1a ("Send And Forget" operations)

#### Description

This sample sends JMS messages to a "driver" queue
Then, a consumer app consumes the driver queue messages, and calls the "Send And Forget" EJB from the "SimpleJmsSendWithReply.ear" app of Profile 1a.
Finally, as in profile 1a, a 3rd app consumes the message.

**NOTE**: In order to better customize what you send on that driver queue, you could use any JMS-capable program (eg. jmeter)
to send the desired JMS message with the right payload and headers to that driver queue (instead of the sample "SimpleJmsSendAndForgetToDriver.ear" app)
That way, the right message profile can flow through the various apps (as opposed to built-in sample messages)

#### Building the apps

Jboss:
```
   ant -Dj2ee-jms-examples.deployment_target=jboss -Dj2ee-jms-examples.deployment_profilename=profile1b assemble
```

Websphere:
```
   ant -Dj2ee-jms-examples.deployment_target=websphere -Dj2ee-jms-examples.deployment_profilename=profile1b assemble
```

4 applications should be created:
* dist/SimpleJmsSendAndForgetToDriver.ear
* dist/SimpleJmsConsumeDriverAndForwardToSendNoReply.ear
* dist/SimpleJmsSend.ear
* dist/SimpleJmsConsume.ear

#### Configuration pre-requisites before deploying the apps on the Application Server

The application built in the previous stage requires some JCA Resource Adapter resources to be created on the application server.
While all the resource names can be easily modified, for the purpose of this quick start, the default apps require the following resources:

##### Objects to be created on the Messaging provider

Same as profile1a + the following extra resources:

* Queues:
  * JMSSamples/DriverQueue

##### RA Objects to be created on the Application servers

Same as profile1a + the following extra resources:

* Managed Admin Object:
  * SimpleJmsSendDriverDestination
    * DestinationJndiName=JMSSamples/DriverQueue

* Managed Activation Specification:
  * SimpleJmsConsumerDriverQueue
    * connectionFactoryJndiName=SimpleJmsConsumerConnectionFactory
    * destinationJndiName=JMSSamples/DriverQueue

#### Deploy the app and run

Deploy the EARs using the standard mechanism for the application server of choice.
The deployment can use all the defaults...and should not require any configuration set at runtimte.

To send messages (using either "Send And Forget" or "Send and Wait"), simply call the following url:
* http://APP_SERVER_HOST:PORT/SimpleJmsSendAndForgetToDriver/JmsSendAndForget

To access the various implemented counters that track the sends and consumes, here are the URLs:
* http://APP_SERVER_HOST:PORT/SimpleJmsSendAndForgetToDriver/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsume_DriverAndSend/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsSend/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsume/messagecounters


### Sample Profile 2b: JMS "Send to a Driver queue" which drives profile 2a with Asynchronous Reply ("Send And Forget" with reply operations)

#### Description

This sample sends JMS messages to a "driver" queue 
Then, a consumer app consumes the driver queue messages, and calls the "Send And Forget" EJB from the "SimpleJmsSendWithReply.ear" app of Profile 2a.
Then, as in profile 2a, a consume app receives the message, processes it and reply to the appropriate destination basded on the ReplyTo header embedded in the message.
Finally, a 3rd app consumes the Asynchronous reply message (used only by the "Asynchronous send and reply" case)

**NOTE**: In order to better customize better what you send on that driver queue, you could use any JMS-capable program (eg. jmeter)
to send the desired JMS message with the right payload and headers to that driver queue (instead of the sample "SimpleJmsSendAndForgetToDriver.ear" app)
That way, the right message profile can flow through the various apps (as opposed to built-in sample messages)

#### Build the apps

Jboss:
```
   ant -Dj2ee-jms-examples.deployment_target=jboss -Dj2ee-jms-examples.deployment_profilename=profile2b assemble
```

Websphere:
```
   ant -Dj2ee-jms-examples.deployment_target=websphere -Dj2ee-jms-examples.deployment_profilename=profile2b assemble
```

5 applications should be created:
* dist/SimpleJmsSendDriver.ear
* dist/SimpleJmsConsumeDriverAndForwardToSendWithReplyAsync.ear
* dist/SimpleJmsSendWithReply.ear
* dist/SimpleJmsConsumeAndReply.ear
* dist/SimpleJmsConsumeTheReply.ear

#### Configuration pre-requisites before deploying the apps on the Application Server

The application built in the previous stage requires some JCA Resource Adapter resources to be created on the application server.
While all the resource names can be easily modified, for the purpose of this quick start, the default apps require the following resources:

##### Objects to be created on the Messaging provider

* Queues:
  * JMSSamples/DriverQueue
  * JMSSamples/RequestQueue
  * JMSSamples/ReplyQueueAsync
* Connection Factories:
  * SimpleJmsSendConnectionFactory
  * SimpleJmsConsumerConnectionFactory

##### RA Objects to be created on the Application servers

* Managed Connection Factory:
  * SimpleJmsSendConnectionFactory
    * ConnectionFactoryJndiName=SimpleJmsSendConnectionFactory
* Managed Admin Object:
  * SimpleJmsSendDriverDestination
    * DestinationJndiName=JMSSamples/DriverQueue
  * SimpleJmsSendWithReplyDestination
    * DestinationJndiName=JMSSamples/RequestQueue
  * SimpleJmsSendReplyToDestination
    * DestinationJndiName=JMSSamples/ReplyQueueAsync

* Managed Activation Specification:
  * SimpleJmsConsumerDriverQueue
    * connectionFactoryJndiName=SimpleJmsConsumerConnectionFactory
    * destinationJndiName=JMSSamples/DriverQueue
  * SimpleJmsConsumeRequest
    * connectionFactoryJndiName=SimpleJmsConsumerConnectionFactory
    * destinationJndiName=JMSSamples/RequestQueue
  * SimpleJmsConsumeAsyncReply
    * connectionFactoryJndiName=SimpleJmsConsumerConnectionFactory
    * destinationJndiName=JMSSamples/ReplyQueueAsync

**NOTE**: Not all application servers provide abstraction for activation specs. Please refer to your application server documentation.
For example, Websphere provides such abstraction, whereas for Jboss, the activation specs are directly provided in the MDB descriptor (eg. jboss-ejb3.xml)
The applications support and will work with both mechanisms.

#### Deploy the app and run

Deploy the EARs using the standard mechanism for the application server of choice.
The deployment can use all the defaults...and should not require any configuration set at runtimte.

To send messages to the driver queue, simply call the following url:
* http://APP_SERVER_HOST:PORT/SimpleJmsSendAndForgetToDriver/JmsSendAndForget

OR use any JMS-capable program (eg. jmeter) to directly send the desired JMS message with the right payload and headers to that driver queue.
   
To access the various implemented counters that track the sends and consumes, here is the URL:
* http://APP_SERVER_HOST:PORT/SimpleJmsSendAndForgetToDriver/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsumeAndForwardToEJB/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsSendWithReply/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsumeAndReply/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsumeTheReply/messagecounters


### Sample Profile 2c: JMS "Send to a Driver queue" which drives profile 2a with Synchronous Reply ("Send And Wait" for reply operations)

#### Description

Same as profile 2b, but in this case, the consumer app which consumes the driver queue messages will calls
the "Send And Wait" EJB from the "SimpleJmsSendWithReply.ear" app.

#### Build

Jboss:
```
   ant -Dj2ee-jms-examples.deployment_target=jboss -Dj2ee-jms-examples.deployment_profilename=profile2c assemble
```

Websphere:
```
   ant -Dj2ee-jms-examples.deployment_target=websphere -Dj2ee-jms-examples.deployment_profilename=profile2c assemble
```

5 applications should be created:
* dist/SimpleJmsSendDriver.ear
* dist/SimpleJmsConsumeDriverAndForwardToSendWithReplySync.ear
* dist/SimpleJmsSendWithReply.ear
* dist/SimpleJmsConsumeAndReply.ear
* dist/SimpleJmsConsumeTheReply.ear

#### Configuration Pre-requisites before deploying the apps on the Application Server

The application built in the previous stage requires some JCA Resource Adapter resources to be created on the application server.
While all the resource names can be easily modified, for the purpose of this quick start, the default apps require the following resources:

##### Objects to be created on the Messaging provider

* Queues:
  * JMSSamples/DriverQueue
  * JMSSamples/RequestQueue
  * JMSSamples/ReplyQueueSync

* Connection Factories:
  * SimpleJmsSendConnectionFactory
  * SimpleJmsConsumerConnectionFactory

##### RA Objects to be created on the Application servers

* Managed Connection Factory:
  * SimpleJmsSendConnectionFactory
    * ConnectionFactoryJndiName=SimpleJmsSendConnectionFactory

* Managed Admin Object:
  * SimpleJmsSendDriverDestination
    * DestinationJndiName=JMSSamples/DriverQueue
  * SimpleJmsSendWithReplyDestination
    * DestinationJndiName=JMSSamples/RequestQueue
  * SimpleJmsSendReplyToDestination
     * DestinationJndiName=JMSSamples/ReplyQueueSync

* Managed Activation Specification:
  * SimpleJmsConsumerDriverQueue
    * connectionFactoryJndiName=SimpleJmsConsumerConnectionFactory
    * destinationJndiName=JMSSamples/DriverQueue
  * SimpleJmsConsumeRequest
    * connectionFactoryJndiName=SimpleJmsConsumerConnectionFactory
    * destinationJndiName=JMSSamples/RequestQueue
  * SimpleJmsConsumeAsyncReply
    * connectionFactoryJndiName=SimpleJmsConsumerConnectionFactory
    * destinationJndiName=JMSSamples/ReplyQueueAsync

**NOTE**: Not all application servers provide abstraction for activation specs. Please refer to your application server documentation.
For example, Websphere provides such abstraction, whereas for Jboss, the activation specs are directly provided in the MDB descriptor (eg. jboss-ejb3.xml)
The applications support and will work with both mechanisms.

#### Deploy the app and run

Deploy the EARs using the standard mechanism for the application server of choice.
The deployment can use all the defaults...and should not require any configuration set at runtimte.

To send messages to the driver queue, simply call the following url:
* http://APP_SERVER_HOST:PORT/SimpleJmsSendAndForgetToDriver/JmsSendAndForget

OR use any JMS-capable program (eg. jmeter) to directly send the desired JMS message with the right payload and headers to that driver queue.

To access the various implemented counters that track the sends and consumes, here is the URL:
* http://APP_SERVER_HOST:PORT/SimpleJmsSendAndForgetToDriver/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsumeAndForwardToEJB/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsSendWithReply/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsumeAndReply/messagecounters
* http://APP_SERVER_HOST:PORT/SimpleJmsConsumeTheReply/messagecounters




### Sample Profile 4: SOAP-over-JMS

Instructions TBD...
______________________
For more information you can Ask a Question in the [TECHcommunity Forums](https://tech.forums.softwareag.com/tags/c/forum/1/universal-messaging).

You can find additional information in the [Software AG TECHcommunity](https://tech.forums.softwareag.com/tag/universal-messaging).
______________________
These tools are provided as-is and without warranty or support. They do not constitute part of the Software AG product suite. Users are free to use, fork and modify them, subject to the license agreement. While Software AG welcomes contributions, we cannot guarantee to include every contribution in the master project.
______________________
Contact us at [TECHcommunity](mailto:technologycommunity@softwareag.com?subject=Github/SoftwareAG) if you have any questions.
