package main;

import java.util.Hashtable;

import javax.jms.*;
import javax.naming.*;

public class JMSProducerTrips {
 
    // connection factory
        private QueueConnectionFactory qconFactory;
 
        // connection to a queue
        private QueueConnection qcon;
 
        // session within a connection
        private QueueSession qsession;
 
        // queue sender that sends a message to the queue
        private QueueSender qsender;
 
        // queue where the message will be sent to
        private Queue queue;
 
        // a message that will be sent to the queue
        private TextMessage msg;
 
        // create a connection to the WLS using a JNDI context
        public void init(Context ctx, String queueName)
            throws NamingException, JMSException {
 
            // create connection factory based on JNDI and a connection
            qconFactory = (QueueConnectionFactory) ctx.lookup(Config3.JMS_FACTORY);
            qcon = qconFactory.createQueueConnection();
 
            // create a session within a connection
            qsession = qcon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
 
            // lookups the queue using the JNDI context
            queue = (Queue) ctx.lookup(queueName);
 
            // create sender and message
            qsender = qsession.createSender(queue);
            msg = qsession.createTextMessage();
        }
 
        // close sender, connection and the session
        public void close() throws JMSException {
            qsender.close();
            qsession.close();
            qcon.close();
        }
 
        // sends the message to the queue
        public void send(String queueName, String message) throws Exception {
 
            // create a JNDI context to lookup JNDI objects (connection factory and queue)
            Hashtable<String, String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, Config3.JNDI_FACTORY);
            env.put(Context.PROVIDER_URL, Config3.PROVIDER_URL);
 
            InitialContext ic = new InitialContext(env);
            init(ic, queueName);
 
            // send the message and close
            try {
                msg.setText(message);
                qsender.send(msg, DeliveryMode.PERSISTENT, 8, 0);
                System.out.println("The booking was sent to the destination " +
                        qsender.getDestination().toString());
            } finally {
                close();
            }
        }

        public static void main(String[] args) throws Exception {
            // input arguments
        	String msg = args[0];
            String queueName = "NewTripsQueue" ;
 
            // create the producer object and send the message
            JMSProducerTrips producer = new JMSProducerTrips();
            producer.send(queueName, msg);

        }
 
}