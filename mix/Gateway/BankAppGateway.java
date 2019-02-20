package Gateway;

import model.bank.BankInterestRequest;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Observer;

public class BankAppGateway {
    private MessageReceiver msgReceiver;

    private  MessageSender msgSender;


    public BankAppGateway(Observer o) {
        this.msgReceiver = new MessageReceiver();
        this.msgSender = new MessageSender();
        this.msgReceiver.addObserver(o);
    }

    public void SendBankRequest(BankInterestRequest request,String CorrelationID) throws JMSException {

        Message msg = msgSender.SendMessage(request, CorrelationID, "request-Intrestbank");
        ReplyReceiver(msg.getJMSReplyTo());

    }

    public void ReplyReceiver(Destination replyDestination){
        msgReceiver.receiveMessageReply(replyDestination, this.msgSender.getConnectionBank());
    }
}
