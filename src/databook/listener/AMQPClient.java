package databook.listener;

import java.io.IOException;

import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.message.impl.*;
import org.apache.qpid.proton.messenger.Messenger;
import org.apache.qpid.proton.messenger.impl.MessengerImpl;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.Data;

import databook.listener.service.MessagingService;

public class AMQPClient {
	
	public static void sendMessage(String host, String queueName, String message) throws IOException {
		try {
			Messenger mng = new MessengerImpl();
			String a = "amqp://" + host + "/" + queueName;
			mng.start();
			Message msg = new MessageImpl();
			msg.setAddress(a);
			msg.setBody(new AmqpValue(message));
			mng.put(msg);
			mng.send();
            mng.stop();
		} catch(Exception e) {
			e.printStackTrace();
		}

//		ConnectionFactory factory = new ConnectionFactory();
//	    factory.setHost(host);
//	    com.rabbitmq.client.Connection connection = factory.newConnection();
//	    Channel channel = connection.createChannel();
//	    
//	    channel.queueDeclare(queueName, false, false, false, null);
//	    channel.basicPublish("", queueName, null, message.getBytes());
//	    System.out.println("Sent '" + message + "'");
//	    
//	    channel.close();
//	    connection.close();
	}
	
	public static void receiveMessage(String host, String queueName, final MessagingService handler) {
		try {
			Messenger mng = new MessengerImpl();
			String a = "amqp://" + host + "/" + queueName;
			mng.start();
            mng.subscribe(a);
            int ct = 0;
            boolean done = false;
            while (!done) {
        	    System.out.println("Waiting for message");
                mng.recv();
                while (mng.incoming() > 0) {
                    Message message = mng.get();
			        System.out.println("Received '" + message + "'");
                    ++ct;
			if(message.getBody() instanceof Data) {
			        handler.handle(((Data) message.getBody()).getValue().toString().replaceAll("\\\\x..", ""));
} else {
			        handler.handle(((AmqpValue) message.getBody()).getValue().toString().replaceAll("\\\\x..", ""));
}
                }
            }
            mng.stop();
		} catch(Exception e) {
			e.printStackTrace();
		}
	    
//	    Consumer consumer = new DefaultConsumer(channel) {
//
//			@Override
//			public void handleDelivery(String consumerTag, Envelope envelope,
//					BasicProperties properties, byte[] body) throws IOException {
//			        String message = new String(body);
//		    }
//	    	
//	    };
//	    
//	    channel.basicConsume(queueName, true, consumer);
	    		
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		sendMessage("localhost", "queue", "hello");
		receiveMessage("localhost", "queue", new MessagingService() {

			@Override
			public void handle(String message) {
				
			}});
	}

}
