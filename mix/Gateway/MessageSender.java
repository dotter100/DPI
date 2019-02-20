package Gateway;

import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanReply;
import model.loan.LoanRequest;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

import javax.jms.*;
import java.io.Serializable;

public class MessageSender {


    private Connection connectionBank = null;
    public Connection getConnectionBank() {
        return connectionBank;
    }

    public MessageSender(){
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        factory.setTrustAllPackages(true);

        try {
            connectionBank = factory.createConnection("admin", "admin");
            connectionBank.start();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }



    public void ReplyMessage(Object reply, Message msg){


        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            Connection connection = factory.createConnection("admin", "admin");
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(msg.getJMSReplyTo());

            //need to ask activemq to create a temporary queue to hold replies
            Destination replyDestination = session.createTemporaryQueue();

            //now let's construct the message with a simple payload
            ObjectMessage  message = session.createObjectMessage((Serializable) reply);

            //load up the message with instructions on how to get back here (JMS Header)
            message.setJMSReplyTo(replyDestination);
            message.setJMSDestination(msg.getJMSReplyTo());
            message.setJMSCorrelationID(msg.getJMSCorrelationID());
            //give it a correlation ID to match (JMS Header)
            producer.send(message);
/*
					MessageConsumer consumer = session.createConsumer(replyDestination);
					TextMessage reply = (TextMessage)consumer.receive();
					System.out.println("RECEIVED: "+reply.getText());
*/

            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }





    public Message SendMessage(Object request, String CorrelationID,String destinationstring){


        try {

            Session session = connectionBank.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination destination = new ActiveMQQueue(destinationstring);
            MessageProducer producer = session.createProducer(destination);

            //need to ask activemq to create a temporary queue to hold replies
            Destination replyDestination = session.createTemporaryQueue();

            //now let's construct the message with a simple payload
            ObjectMessage  message = session.createObjectMessage((Serializable) request);

            //load up the message with instructions on how to get back here (JMS Header)
            message.setJMSReplyTo(replyDestination);
            //give it a correlation ID to match (JMS Header)
            message.setJMSCorrelationID(CorrelationID);
            producer.send(message);
            //messages.add(message);
            //receiveMessageReplyBank(replyDestination);

/*
					MessageConsumer consumer = session.createConsumer(replyDestination);
					TextMessage reply = (TextMessage)consumer.receive();
					System.out.println("RECEIVED: "+reply.getText());
*/

            session.close();
            return message;
            //connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
            return null;
        }
    }


}
