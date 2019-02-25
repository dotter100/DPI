package Gateway;

import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanReply;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

import javax.jms.*;
import java.util.Observable;

public class MessageReceiver extends Observable {

    public Object var;
    private Connection connection = null;
    private Connection connTopic = null;


    public MessageReceiver(){
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        factory.setTrustAllPackages(true);

        try {
            connection = factory.createConnection("admin", "admin");
            connTopic =  factory.createTopicConnection("admin", "admin");
            connTopic.start();
            connection.start();
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public void receiveMessageBroker(String Desinationstring)  {
        MessageConsumer consumer = null;
        try {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination destination = new ActiveMQQueue(Desinationstring);
            consumer = session.createConsumer(destination);


        } catch (JMSException e) {
            e.printStackTrace();
        }

        try {
            consumer.setMessageListener(msg -> {
                System.out.println("received message: " + msg);
                try {
                    //BankInterestRequest objmsg = (BankInterestRequest)((ObjectMessage) msg).getObject();
                    msg.getJMSReplyTo();
                    //RequestReply rr = new RequestReply(objmsg,new BankInterestReply());
                    ObjectMessage objmsg = ((ObjectMessage) msg);
                    setChanged();
                    notifyObservers(msg);

                    System.out.println(objmsg.toString());



                } catch (JMSException e) {
                    e.printStackTrace();
                }

            });

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    public void receiveMessageReply(Destination replyDestination, Connection conn)  {
        MessageConsumer consumer = null;
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            factory.setTrustAllPackages(true);


            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

            //Destination destination = new ActiveMQQueue("Replay-bank");
            consumer = session.createConsumer(replyDestination);
            //connectionBank.start();

        } catch (JMSException e) {
            e.printStackTrace();
        }

        try {
            consumer.setMessageListener(msg -> {
                System.out.println("received message bank reply: " + msg);
                setChanged();
                notifyObservers(msg);
                try {
                    ObjectMessage objmsg = ((ObjectMessage) msg);


                    /*if(objmsg instanceof BankInterestReply){
                        BankInterestReply reply = (BankInterestReply) objmsg.getObject();
                        LoanReply loanreply = new LoanReply();
                        loanreply.setInterest(reply.getInterest());
                        loanreply.setQuoteID(reply.getQuoteId());
                        System.out.println(reply.toString());

                    }
                    if(objmsg instanceof LoanReply){
                        LoanReply reply = (LoanReply) objmsg.getObject();
                        System.out.println(reply.toString());

                    }*/
                    //add(objmsg);

                    //getRequestReply2(msg,objmsg);


                    //ReplyMessage(loanreply,GetClientMessage(msg.getJMSCorrelationID()));





                } catch (Exception e) {
                    e.printStackTrace();
                }

                //add(msg);
            });

        } catch (JMSException e) {
            e.printStackTrace();
        }



    }
}
