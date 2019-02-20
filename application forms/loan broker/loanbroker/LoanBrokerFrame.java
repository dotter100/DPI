package loanbroker;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.*;

import javax.jms.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Gateway.BankAppGateway;
import Gateway.LoanBrokerGateway;
import model.bank.*;
import model.loan.LoanReply;
import model.loan.LoanRequest;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;


public class LoanBrokerFrame extends JFrame implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private DefaultListModel<JListLine> listModel = new DefaultListModel<JListLine>();
	private JList<JListLine> list;
	private List<Message> messages = new ArrayList<>();
	private List<Message> messageclient = new ArrayList<>();

	private BankAppGateway BankGateway = null;
	private LoanBrokerGateway Gateway = null;

	Connection connectionBank = null;

	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoanBrokerFrame frame = new LoanBrokerFrame();

					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}


	/**
	 * Create the frame.
	 */
	public LoanBrokerFrame() {


		setTitle("Loan Broker");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{46, 31, 86, 30, 89, 0};
		gbl_contentPane.rowHeights = new int[]{233, 23, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 7;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		contentPane.add(scrollPane, gbc_scrollPane);
		Startup();
		
		list = new JList<JListLine>(listModel);
		BankGateway = new BankAppGateway(this);
		Gateway = new LoanBrokerGateway(this);
		Gateway.Receiver("request-LoanClient");
		//receiveMessageClient();
		scrollPane.setViewportView(list);

	}

	private void Startup(){
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
		factory.setTrustAllPackages(true);

		try {
			connectionBank = factory.createConnection("admin", "admin");
			connectionBank.start();
		} catch (JMSException e) {
			e.printStackTrace();
		}

	}
	
	 private JListLine getRequestReply(LoanRequest request){    
	     
	     for (int i = 0; i < listModel.getSize(); i++){
	    	 JListLine rr =listModel.get(i);
	    	 if (rr.getLoanRequest() == request){
	    		 return rr;
	    	 }
	     }
	     
	     return null;
	   }

	private void getRequestReply2(Message msg, BankInterestReply reply){
				int i = 0;
				for( Message m : messageclient){
					try {
							System.out.println(m.getJMSCorrelationID() + "   " + msg.getJMSCorrelationID());
						if(m.getJMSCorrelationID().equals( msg.getJMSCorrelationID())){

							add(listModel.get(i).getLoanRequest(),reply);
							return;
						}
					} catch (JMSException e) {
						e.printStackTrace();
					}
					i++;
				}


	}


	public void add(LoanRequest loanRequest){		
		listModel.addElement(new JListLine(loanRequest));		
	}
	

	public void add(LoanRequest loanRequest,BankInterestRequest bankRequest){
		JListLine rr = getRequestReply(loanRequest);
		if (rr!= null && bankRequest != null){
			rr.setBankRequest(bankRequest);
            list.repaint();
		}		
	}
	
	public void add(LoanRequest loanRequest, BankInterestReply bankReply){
		JListLine rr = getRequestReply(loanRequest);
		if (rr!= null && bankReply != null){
			rr.setBankReply(bankReply);;
            list.repaint();
		}		
	}

	private Message GetClientMessage(String ID){
		for(Message msg : messageclient){
			try {
				if(msg.getJMSCorrelationID().equals(ID)){
					return msg;
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void receiveMessageClient()  {
		MessageConsumer consumer = null;
			try {
				ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
				factory.setTrustAllPackages(true);
				Connection connection = factory.createConnection("admin", "admin");


				Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

				Destination destination = new ActiveMQQueue("request-LoanClient");
				consumer = session.createConsumer(destination);
				connection.start();

			} catch (JMSException e) {
				e.printStackTrace();
			}

		try {
			consumer.setMessageListener(msg -> {
				System.out.println("received message: " + msg);
				try {
				LoanRequest objmsg = (LoanRequest)((ObjectMessage) msg).getObject();

				add(objmsg);
				messageclient.add(msg);
				SendMessageBank(objmsg,msg.getJMSCorrelationID());




				} catch (JMSException e) {
					e.printStackTrace();
				}

				//add(msg);
			});

		} catch (JMSException e) {
			e.printStackTrace();
		}



	}

	public void SendMessageBank(LoanRequest request, String CorrelationID){
		BankInterestRequest Requestbank = new BankInterestRequest(request.getAmount(),request.getTime());

		try {
			BankGateway.SendBankRequest(Requestbank,CorrelationID);
		} catch (JMSException e) {
			e.printStackTrace();
		}

		/*try {

			Session session = connectionBank.createSession(false, Session.AUTO_ACKNOWLEDGE);

			Destination destination = new ActiveMQQueue("request-Intrestbank");
			MessageProducer producer = session.createProducer(destination);

			//need to ask activemq to create a temporary queue to hold replies
			Destination replyDestination = session.createTemporaryQueue();

			//now let's construct the message with a simple payload
			ObjectMessage  message = session.createObjectMessage(Requestbank);

			//load up the message with instructions on how to get back here (JMS Header)
			message.setJMSReplyTo(replyDestination);
			//give it a correlation ID to match (JMS Header)
			message.setJMSCorrelationID(CorrelationID);
			producer.send(message);
			messages.add(message);
			receiveMessageReplyBank(replyDestination);

*//*
					MessageConsumer consumer = session.createConsumer(replyDestination);
					TextMessage reply = (TextMessage)consumer.receive();
					System.out.println("RECEIVED: "+reply.getText());
*//*

			session.close();
			//connection.close();
		} catch (JMSException e) {
			e.printStackTrace();
		}*/
	}



	public void receiveMessageReplyBank(Destination replyDestination)  {
		MessageConsumer consumer = null;
		try {
			ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
			factory.setTrustAllPackages(true);


			Session session = connectionBank.createSession(false, Session.AUTO_ACKNOWLEDGE);

			//Destination destination = new ActiveMQQueue("Replay-bank");
			consumer = session.createConsumer(replyDestination);
			//connectionBank.start();

		} catch (JMSException e) {
			e.printStackTrace();
		}

		try {
			consumer.setMessageListener(msg -> {
				System.out.println("received message bank reply: " + msg);
				try {
					BankInterestReply objmsg = (BankInterestReply)((ObjectMessage) msg).getObject();
					//add(objmsg);
					System.out.println(objmsg.toString());
					getRequestReply2(msg,objmsg);
					LoanReply loanreply = new LoanReply();
					loanreply.setInterest(objmsg.getInterest());
					loanreply.setQuoteID(objmsg.getQuoteId());

					ReplyMessage(loanreply,GetClientMessage(msg.getJMSCorrelationID()));





				} catch (JMSException e) {
					e.printStackTrace();
				}

				//add(msg);
			});

		} catch (JMSException e) {
			e.printStackTrace();
		}



	}

	public void ReplyMessage(LoanReply reply, Message msg){


		try {
			ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
			Connection connection = factory.createConnection("admin", "admin");
			connection.start();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer producer = session.createProducer(msg.getJMSReplyTo());

			//need to ask activemq to create a temporary queue to hold replies
			Destination replyDestination = session.createTemporaryQueue();

			//now let's construct the message with a simple payload
			ObjectMessage  message = session.createObjectMessage(reply);

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


	@Override
	public void update(Observable msg, Object arg) {
		try {
			Message MSG = (Message) arg;
			if(((ObjectMessage) MSG).getObject() instanceof BankInterestReply) {
				BankInterestReply objmsg = (BankInterestReply) ((ObjectMessage) MSG).getObject();
				//add(objmsg);
				System.out.println(objmsg.toString());
				getRequestReply2(MSG, objmsg);
				LoanReply loanreply = new LoanReply();
				loanreply.setInterest(objmsg.getInterest());
				loanreply.setQuoteID(objmsg.getQuoteId());
				ReplyMessage(loanreply, GetClientMessage(MSG.getJMSCorrelationID()));
				//messageclient.add(MSG);
			}else if(((ObjectMessage) MSG).getObject() instanceof LoanRequest) {

				LoanRequest objmsg = (LoanRequest) ((ObjectMessage) MSG).getObject();

				add(objmsg);
				messageclient.add(MSG);
				SendMessageBank(objmsg, MSG.getJMSCorrelationID());


			}
		}catch (JMSException e) {
			e.printStackTrace();
		}

	}
}
