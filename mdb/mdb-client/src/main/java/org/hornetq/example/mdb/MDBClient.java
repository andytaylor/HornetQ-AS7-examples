package org.hornetq.example.mdb;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

/**
 * @author <a href="mailto:andy.taylor@jboss.com">Andy Taylor</a>
 *         Date: 8/31/11
 *         Time: 3:14 PM
 */
public class MDBClient
{
   public static void main(String[] args) throws Exception
   {
      Connection connection = null;
      try
      {
         //Step 2. Perfom a lookup on the queue
         Queue queue = HornetQJMSClient.createQueue("mdbQueue");

         //Step 3. Perform a lookup on the Connection Factory
         TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName());

         ConnectionFactory cf = (ConnectionFactory) HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfiguration);

         //Step 4.Create a JMS Connection
         connection = cf.createConnection();

         //Step 5. Create a JMS Session
         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

         //Step 6. Create a JMS Message Producer
         MessageProducer producer = session.createProducer(queue);

         //Step 7. Create a Text Message
         TextMessage message = session.createTextMessage("This is a text message");

         System.out.println("Sent message: " + message.getText());

         //Step 8. Send the Message
         producer.send(message);

         //Step 15. We lookup the reply queue
         queue = HornetQJMSClient.createQueue("mdbReplyQueue");

         //Step 16. We create a JMS message consumer
         MessageConsumer messageConsumer = session.createConsumer(queue);

         //Step 17. We start the connedction so we can receive messages
         connection.start();

         //Step 18. We receive the message and print it out
         message = (TextMessage) messageConsumer.receive(5000);

         System.out.println("message.getText() = " + message.getText());

      }
      finally
      {
         if(connection != null)
         {
            connection.close();
         }
      }
   }
}
