package Gateway;

import model.bank.BankInterestReply;
import model.loan.LoanReply;
import model.loan.LoanRequest;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Observer;

public class ClientGateway {
    private MessageReceiver msgReceiver;

    private  MessageSender msgSender;

    public ClientGateway(Observer o) {
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
    public void Reply(LoanReply reply, Message msg) {
        if(reply instanceof LoanReply) {
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
