/*
 * Copyright 2009 Red Hat, Inc.
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package org.hornetq.example;


import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;


import org.jboss.ejb3.annotation.ResourceAdapter;

/**
 * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
 */
@MessageDriven(name = "MDBSendExample",
                messageListenerInterface = MessageListener.class,
               activationConfig =
                     {
                        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
                        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/mdbQueue")
                     })
@TransactionManagement(value= TransactionManagementType.CONTAINER)
@TransactionAttribute(value= TransactionAttributeType.REQUIRED)
public class MDBSendExample implements MessageListener
{
   @Resource(mappedName = "java:/JmsXA")
   ConnectionFactory connectionFactory;

   @Resource(mappedName = "java:/queue/mdbReplyQueue")
   Queue replyQueue;

   public void onMessage(Message message)
   {
      Connection connection = null;
      try
      {
         TextMessage textMessage = (TextMessage)message;

         String text = textMessage.getText();

         System.out.println("message " + text);

         connection = connectionFactory.createConnection();

         System.out.println("connection created");

         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

         System.out.println("session created");

         MessageProducer producer = session.createProducer(replyQueue);

         System.out.println("sending reply");

         producer.send(session.createTextMessage("here is a reply"));

         System.out.println("reply sent");

      }
      catch (Throwable e)
      {
         e.printStackTrace();
      }
      finally
      {
         if(connection != null)
         {
            try {
               connection.close();
            } catch (JMSException e) {
               e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
         }
      }
   }
}