HornetQ replicated example using the AS7 domain controller
============================================================
Author: Andy Taylor

What is it?
-----------

This example demonstrates to configure the JBoss AS7 server to set up and test failover using replication. The example
will deploy and start a live and backup server configured to replicate, we will the use the JBoss CLI to kill the live
server and observe the backup server take over, we will then restart the live server and observe fail back occurring.

_NOTE: In this example the live and backup server are on the same machine but in reality you would deploy each server in
a different slave host

Download and start the AS7 domain server
----------------------------------------

Download AS 7.2 or later and install as per the AS7 instructions. copy the configuration directory to the 'domain' directory
of the install to over write the one already there, you can then start the server from the bin directory via the command:

        ./domain.sh

you should then see 2 servers started once called hornetq-live and one called hornetq-backup and replication started, the
output would be something like:

        [Server:hornetq-live] 14:00:25,813 INFO  [org.hornetq.core.server] (Thread-77) HQ221030: Replication: sending JournalFileImpl: (hornetq-data-1.hq id = 1, recordID = 1) (size=102,400) to backup. AIOSequentialFile:/home/andy/projects/jboss-as/build/target/jboss-as-7.2.0.Alpha1-SNAPSHOT/domain/servers/hornetq-live/data/messagingjournal/hornetq-data-1.hq
        [Server:hornetq-live] 14:00:25,951 INFO  [org.hornetq.core.server] (Thread-77) HQ221030: Replication: sending JournalFileImpl: (hornetq-bindings-1.bindings id = 1, recordID = 1) (size=1,048,576) to backup. NIOSequentialFile /home/andy/projects/jboss-as/build/target/jboss-as-7.2.0.Alpha1-SNAPSHOT/domain/servers/hornetq-live/data/messagingbindings/hornetq-bindings-1.bindings
        [Server:hornetq-backup] 14:00:26,379 INFO  [org.hornetq.core.server] (Old I/O client worker ([id: 0x077cd18d, /127.0.0.1:37867 => localhost/127.0.0.1:5445])) HQ221028: Backup server HornetQServerImpl::serverUUID=72a55f6a-2d9a-11e2-a9fb-ebe8e3cb0426 is synchronized with live-server.
        [Server:hornetq-backup] 14:00:30,500 INFO  [org.hornetq.core.server] (Thread-1 (HornetQ-server-HornetQServerImpl::serverUUID=72b27e6b-2d9a-11e2-8b7c-2d0fc818df3f-488461919)) HQ221036: backup announced

Killing the live server
-----------------------

To kill the live server we use the JBoss CLI (Command Line Interface), firstly we start the CLI:

        ./jboss-cli.sh
        You are disconnected at the moment. Type 'connect' to connect to the server or 'help' for the list of supported commands.
        [disconnected /]

and then connect

        connect
        [domain@localhost:9999 /]

we can then stop the live server by stopping the process with the command:

        /host=master/server-config=hornetq-live:stop
        {
            "outcome" => "success",
            "result" => "STOPPING"
        }

we can view the status of the live server with the command:

        /host=master/server-config=hornetq-live:read-resource(include-runtime=true)
        {
            "outcome" => "success",
            "result" => {
                "auto-start" => true,
                "cpu-affinity" => undefined,
                "group" => "hornetq-live-server-group",
                "interface" => undefined,
                "jvm" => undefined,
                "name" => "hornetq-live",
                "path" => undefined,
                "priority" => undefined,
                "socket-binding-group" => undefined,
                "socket-binding-port-offset" => 0,
                "status" => "STOPPED",
                "system-property" => undefined
            }
        }

if you look in the servers console you should see the backup starting:

        [Server:hornetq-backup] 14:05:13,229 INFO  [org.hornetq.core.server] (HQ119001: Activation for server HornetQServerImpl::serverUUID=72b27e6b-2d9a-11e2-8b7c-2d0fc818df3f) HQ221024: Started Netty Acceptor version 3.4.5.Final-2da5b0e 127.0.0.1:5,595 for CORE protocol


Re starting the live server
---------------------------

Similar to stopping the server we can restart it ising the CLI with the command

        /host=master/server-config=hornetq-live:start
        {
            "outcome" => "success",
            "result" => "STARTING"
        }

you will then see in the server console the backup server stopping and the live server restarting

        [Server:hornetq-backup] 14:08:49,034 INFO  [org.hornetq.core.server] (Thread-77) HQ221004: HornetQ Server version 2.3.0.SNAPSHOT (HornetQ sting, 122) [72a55f6a-2d9a-11e2-a9fb-ebe8e3cb0426] stopped
        [Server:hornetq-backup] 14:08:49,034 WARN  [org.hornetq.core.server] (Thread-77) HQ222217: Server is being completely stopped, since this was a replicated backup there may be journal files that need cleaning up. The HornetQ server will have to be manually restarted.
        [Server:hornetq-live] 14:08:49,344 INFO  [org.hornetq.jms.server] (HQ119001: Activation for server HornetQServerImpl::serverUUID=72a55f6a-2d9a-11e2-a9fb-ebe8e3cb0426) HQ121004: JMS Server Manager Running cached command for createConnectionFactory for RemoteConnectionFactory
        [Server:hornetq-live] 14:08:49,353 INFO  [org.jboss.as.messaging] (HQ119001: Activation for server HornetQServerImpl::serverUUID=72a55f6a-2d9a-11e2-a9fb-ebe8e3cb0426) JBAS011601: Bound messaging object to jndi name java:jboss/exported/jms/RemoteConnectionFactory
        [Server:hornetq-live] 14:08:49,354 INFO  [org.hornetq.jms.server] (HQ119001: Activation for server HornetQServerImpl::serverUUID=72a55f6a-2d9a-11e2-a9fb-ebe8e3cb0426) HQ121004: JMS Server Manager Running cached command for createConnectionFactory for InVmConnectionFactory
        [Server:hornetq-live] 14:08:49,355 INFO  [org.jboss.as.messaging] (HQ119001: Activation for server HornetQServerImpl::serverUUID=72a55f6a-2d9a-11e2-a9fb-ebe8e3cb0426) JBAS011601: Bound messaging object to jndi name java:/ConnectionFactory
        [Server:hornetq-live] 14:08:49,373 INFO  [org.hornetq.core.server] (HQ119001: Activation for server HornetQServerImpl::serverUUID=72a55f6a-2d9a-11e2-a9fb-ebe8e3cb0426) HQ221024: Started Netty Acceptor version 3.4.5.Final-2da5b0e 127.0.0.1:5,455 for CORE protocol
        [Server:hornetq-live] 14:08:49,375 INFO  [org.hornetq.core.server] (HQ119001: Activation for server HornetQServerImpl::serverUUID=72a55f6a-2d9a-11e2-a9fb-ebe8e3cb0426) HQ221024: Started Netty Acceptor version 3.4.5.Final-2da5b0e 127.0.0.1:5,445 for CORE protocol

