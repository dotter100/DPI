package Gateway;

import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import model.loan.LoanRequest;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Observer;
import java.util.Random;

public class LoanBrokerGateway {
    private MessageReceiver msgReceiver;

    private  MessageSender msgSender;

    public LoanBrokerGateway(Observer o) {
        this.msgReceiver = new MessageReceiver();
        this.msgSender = new MessageSender();
        this.msgReceiver.addObserver(o);
    }

    public void SendRequest(LoanRequest request, String CorrelationID) throws JMSException {
        if(request instanceof LoanRequest) {
            Message msg = msgSender.SendMessage(request, CorrelationID, "request-LoanClient");
            ReplyReceiver(msg.getJMSReplyTo());
        }

    }
    public Message SendRequest(LoanRequest request) throws JMSException {
        if(request instanceof LoanRequest) {
            Message msg = msgSender.SendMessage(request, Long.toHexString(new Random(System.currentTimeMillis()).nextLong()), "request-LoanClient");
            ReplyReceiver(msg.getJMSReplyTo());
            return msg;

        }
        return null;

    }
    public void Reply(BankInterestReply reply, Message msg) {
        if(reply instanceof BankInterestReply) {
            msgSender.ReplyMessage(reply,msg);
        }

    }

    public void ReplyReceiver(Destination replyDestination){
        msgReceiver.receiveMessageReply(replyDestination,this.msgSender.getConnectionBank());
    }

    public void Receiver(String queue){
        msgReceiver.receiveMessageBroker(queue);
    }
}
