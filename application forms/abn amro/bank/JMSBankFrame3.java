package bank;

import Gateway.LoanBrokerGateway;
import messaging.requestreply.RequestReply;
import model.bank.BankInterestReply;
import model.bank.BankInterestRequest;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

import javax.jms.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class JMSBankFrame3 extends JFrame  implements Observer {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField tfReply;
	private DefaultListModel<RequestReply<BankInterestRequest, BankInterestReply>> listModel = new DefaultListModel<RequestReply<BankInterestRequest, BankInterestReply>>();
	private List<Message> messages = new ArrayList<>();
	private JList<RequestReply<BankInterestRequest, BankInterestReply>> list;
	private LoanBrokerGateway Gateway = new LoanBrokerGateway(this);

	private String Name = "ING";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JMSBankFrame3 frame = new JMSBankFrame3();
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
	public JMSBankFrame3() {
		setTitle("JMS Bank - " + Name);
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
		gbc_scrollPane.gridwidth = 5;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		list = new JList<RequestReply<BankInterestRequest, BankInterestReply>>(listModel);
		//receiveMessageBroker();
		Gateway.Receiver(Name);
		scrollPane.setViewportView(list);
		
		JLabel lblNewLabel = new JLabel("type reply");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		contentPane.add(lblNewLabel, gbc_lblNewLabel);
		
		tfReply = new JTextField();
		GridBagConstraints gbc_tfReply = new GridBagConstraints();
		gbc_tfReply.gridwidth = 2;
		gbc_tfReply.insets = new Insets(0, 0, 0, 5);
		gbc_tfReply.fill = GridBagConstraints.HORIZONTAL;
		gbc_tfReply.gridx = 1;
		gbc_tfReply.gridy = 1;
		contentPane.add(tfReply, gbc_tfReply);
		tfReply.setColumns(10);
		
		JButton btnSendReply = new JButton("send reply");
		btnSendReply.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RequestReply<BankInterestRequest, BankInterestReply> rr = list.getSelectedValue();
				double interest = Double.parseDouble((tfReply.getText()));
				BankInterestReply reply = new BankInterestReply(interest,Name);
				if (rr!= null && reply != null){
					rr.setReply(reply);
	                list.repaint();
					// todo: sent JMS message with the reply to Loan Broker
					Gateway.Reply(rr.getReply(),messages.get(list.getSelectedIndex()));
					//ReplyMessage(rr.getReply(),messages.get(list.getSelectedIndex()));
					/*ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
					Connection connection = factory.createConnection("admin", "admin");
					connection.start();

					Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

					Destination destination = new ActiveMQQueue("request-reply-Loanbroker");
					MessageConsumer consumer = session.createConsumer(destination);

					TextMessage requestMessage = (TextMessage)consumer.receive();

					//now that we have the request message, let's construct a reply
					String replyPayload = String.format("Hello, %s", requestMessage.getText());
					TextMessage replyMessage = session.createTextMessage(replyPayload);
					replyMessage.setJMSDestination(requestMessage.getJMSReplyTo());
					replyMessage.setJMSCorrelationID(requestMessage.getJMSCorrelationID());

					MessageProducer producer = session.createProducer(requestMessage.getJMSReplyTo());
					producer.send(replyMessage);

					session.close();
					connection.close();
					*/
				}
			}
		});
		GridBagConstraints gbc_btnSendReply = new GridBagConstraints();
		gbc_btnSendReply.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnSendReply.gridx = 4;
		gbc_btnSendReply.gridy = 1;
		contentPane.add(btnSendReply, gbc_btnSendReply);
	}

	public void receiveMessage(){}


	public void receiveMessageBroker()  {
		//Gateway.Receiver("request-Intrestbank");

		//BankInterestRequest objmsg = (BankInterestRequest)((ObjectMessage) msg).getObject();
		//msg.getJMSReplyTo();
		//RequestReply rr = new RequestReply(objmsg,new BankInterestReply());
		//listModel.addElement(rr);
		//messages.add(msg);
		//System.out.println(objmsg.toString());

		MessageConsumer consumer = null;
		try {
			ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
			factory.setTrustAllPackages(true);
			Connection connection = factory.createConnection("admin", "admin");


			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			Destination destination = new ActiveMQQueue("request-Intrestbank");
			consumer = session.createConsumer(destination);
			connection.start();

		} catch (JMSException e) {
			e.printStackTrace();
		}

		try {
			consumer.setMessageListener(msg -> {
				System.out.println("received message: " + msg);
				try {
					BankInterestRequest objmsg = (BankInterestRequest)((ObjectMessage) msg).getObject();
					msg.getJMSReplyTo();
					RequestReply rr = new RequestReply(objmsg,new BankInterestReply());
					listModel.addElement(rr);
					messages.add(msg);
					System.out.println(objmsg.toString());



				} catch (JMSException e) {
					e.printStackTrace();
				}

				//add(msg);
			});

		} catch (JMSException e) {
			e.printStackTrace();
		}

	}

	public void ReplyMessage(BankInterestReply reply, Message msg){


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
			BankInterestRequest objmsg = (BankInterestRequest) ((ObjectMessage) MSG).getObject();
			MSG.getJMSReplyTo();
			RequestReply rr = new RequestReply(objmsg, new BankInterestReply());
			listModel.addElement(rr);
			messages.add(MSG);
			System.out.println(objmsg.toString());
		} catch (JMSException e) {
			e.printStackTrace();
		}

	}
}
