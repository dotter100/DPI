package loanclient;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.*;

import javax.jms.*;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import Gateway.ClientGateway;
import Gateway.LoanBrokerGateway;
import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
import model.loan.*;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

public class LoanClientFrame extends JFrame implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField tfSSN;
	private DefaultListModel<RequestReply<LoanRequest,LoanReply>> listModel = new DefaultListModel<RequestReply<LoanRequest,LoanReply>>();
	private JList<RequestReply<LoanRequest,LoanReply>> requestReplyList;

	private JTextField tfAmount;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	private JTextField tfTime;
	private Connection connection = null;
	private List<Message> messages = new ArrayList<>();
	private LoanBrokerGateway client = null;

	/**
	 * Create the frame.
	 */
	public LoanClientFrame() {
		setTitle("Loan Client");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 684, 619);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] {0, 0, 30, 30, 30, 30, 0};
		gbl_contentPane.rowHeights = new int[] {30,  30, 30, 30, 30};
		gbl_contentPane.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JLabel lblBody = new JLabel("ssn");
		GridBagConstraints gbc_lblBody = new GridBagConstraints();
		gbc_lblBody.insets = new Insets(0, 0, 5, 5);
		gbc_lblBody.gridx = 0;
		gbc_lblBody.gridy = 0;
		contentPane.add(lblBody, gbc_lblBody);
		
		tfSSN = new JTextField();
		GridBagConstraints gbc_tfSSN = new GridBagConstraints();
		gbc_tfSSN.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfSSN.insets = new Insets(0, 0, 5, 5);
		gbc_tfSSN.gridx = 1;
		gbc_tfSSN.gridy = 0;
		contentPane.add(tfSSN, gbc_tfSSN);
		tfSSN.setColumns(10);
		
		lblNewLabel = new JLabel("amount");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		contentPane.add(lblNewLabel, gbc_lblNewLabel);
		
		tfAmount = new JTextField();
		GridBagConstraints gbc_tfAmount = new GridBagConstraints();
		gbc_tfAmount.anchor = GridBagConstraints.NORTH;
		gbc_tfAmount.insets = new Insets(0, 0, 5, 5);
		gbc_tfAmount.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfAmount.gridx = 1;
		gbc_tfAmount.gridy = 1;
		contentPane.add(tfAmount, gbc_tfAmount);
		tfAmount.setColumns(10);
		
		lblNewLabel_1 = new JLabel("time");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 2;
		contentPane.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		tfTime = new JTextField();
		GridBagConstraints gbc_tfTime = new GridBagConstraints();
		gbc_tfTime.insets = new Insets(0, 0, 5, 5);
		gbc_tfTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfTime.gridx = 1;
		gbc_tfTime.gridy = 2;
		contentPane.add(tfTime, gbc_tfTime);
		tfTime.setColumns(10);
		client = new LoanBrokerGateway(this);
		JButton btnQueue = new JButton("send loan request");
		btnQueue.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int ssn = Integer.parseInt(tfSSN.getText());
				int amount = Integer.parseInt(tfAmount.getText());
				int time = Integer.parseInt(tfTime.getText());				
				
				LoanRequest request = new LoanRequest(ssn,amount,time);
				listModel.addElement( new RequestReply<LoanRequest,LoanReply>(request, null));	
				// to do:  send the JMS with request to Loan Broker
				//SendMessage(request);
				try {
					Message m = client.SendRequest(request);
					messages.add(m);
				} catch (JMSException e) {
					e.printStackTrace();
				}


			}
		});
		GridBagConstraints gbc_btnQueue = new GridBagConstraints();
		gbc_btnQueue.insets = new Insets(0, 0, 5, 5);
		gbc_btnQueue.gridx = 2;
		gbc_btnQueue.gridy = 2;
		contentPane.add(btnQueue, gbc_btnQueue);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 7;
		gbc_scrollPane.gridwidth = 6;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 4;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		requestReplyList = new JList<RequestReply<LoanRequest,LoanReply>>(listModel);
		scrollPane.setViewportView(requestReplyList);
       
	}
	
	/**
	 * This method returns the RequestReply line that belongs to the request from requestReplyList (JList). 
	 * You can call this method when an reply arrives in order to add this reply to the right request in requestReplyList.
	 * @param request
	 * @return
	 */
   private RequestReply<LoanRequest,LoanReply> getRequestReply(LoanRequest request){
     
     for (int i = 0; i < listModel.getSize(); i++){
    	 RequestReply<LoanRequest,LoanReply> rr =listModel.get(i);
    	 if (rr.getRequest() == request){
    		 return rr;
    	 }
     }
     
     return null;
   }
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoanClientFrame frame = new LoanClientFrame();
					
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	public void SendMessage(LoanRequest request){


		try {
			ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
			factory.setTrustAllPackages(true);
			connection = factory.createConnection("admin", "admin");
			connection.start();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			Destination destination = new ActiveMQQueue("request-LoanClient");
			MessageProducer producer = session.createProducer(destination);

			//need to ask activemq to create a temporary queue to hold replies
			Destination replyDestination = session.createTemporaryQueue();

			//now let's construct the message with a simple payload
			ObjectMessage  message = session.createObjectMessage(request);

			//load up the message with instructions on how to get back here (JMS Header)
			message.setJMSReplyTo(replyDestination);
			//give it a correlation ID to match (JMS Header)
			message.setJMSCorrelationID(Long.toHexString(new Random(System.currentTimeMillis()).nextLong()));
			producer.send(message);
			messages.add(message);
			receiveMessageReplyBroker(replyDestination);
/*
					MessageConsumer consumer = session.createConsumer(replyDestination);
					TextMessage reply = (TextMessage)consumer.receive();
					System.out.println("RECEIVED: "+reply.getText());
*/

			session.close();

		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void receiveMessageReplyBroker(Destination replyDestination)  {
		MessageConsumer consumer = null;
		try {
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			//Destination destination = new ActiveMQQueue("Replay-bank");
			consumer = session.createConsumer(replyDestination);
			//connectionBank.start();

		} catch (JMSException e) {
			e.printStackTrace();
		}

		try {
			consumer.setMessageListener(msg -> {
				System.out.println("received message Broker reply: " + msg);
				try {
					LoanReply objmsg = (LoanReply)((ObjectMessage) msg).getObject();
					//add(objmsg);
					System.out.println(objmsg.toString());

					getRequestReply(GetClientMessage(msg.getJMSCorrelationID()),objmsg);







				} catch (JMSException e) {
					e.printStackTrace();
				}

				//add(msg);
			});

		} catch (JMSException e) {
			e.printStackTrace();
		}



	}
	private Message GetClientMessage(String ID){
		for(Message msg : messages){
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

	private void getRequestReply(Message msg, LoanReply reply){
		int i = 0;
		for( Message m : messages){
			try {
				System.out.println(m.getJMSCorrelationID() + "   " + msg.getJMSCorrelationID());
				if(m.getJMSCorrelationID().equals( msg.getJMSCorrelationID())){

					listModel.get(i).setReply(reply);
					return;
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}
			i++;
		}


	}

	@Override
	public void update(Observable msg, Object arg) {
		try {
			Message MSG = (Message) arg;
			LoanReply objmsg = (LoanReply)((ObjectMessage) MSG).getObject();
			//add(objmsg);
			System.out.println(objmsg.toString());
			getRequestReply(GetClientMessage(MSG.getJMSCorrelationID()),objmsg);

		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}
