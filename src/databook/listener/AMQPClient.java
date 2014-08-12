package databook.listener;

import java.io.IOException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;;

import databook.listener.service.MessageHandler;

public class AMQPClient {
	
	public static void sendMessage(String host, String queueName, String message) throws IOException {
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost(host);
	    com.rabbitmq.client.Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();
	    
	    channel.queueDeclare(queueName, false, false, false, null);
	    channel.basicPublish("", queueName, null, message.getBytes());
	    System.out.println("Sent '" + message + "'");
	    
	    channel.close();
	    connection.close();
	}
	
	public static void receiveMessage(String host, String queueName, final MessageHandler handler) throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException {
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost(host);
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();
	    
	    channel.queueDeclare(queueName, false, false, false, null);
	    System.out.println("Waiting for message");
	    
	    Consumer consumer = new DefaultConsumer(channel) {

			@Override
			public void handleDelivery(String consumerTag, Envelope envelope,
					BasicProperties properties, byte[] body) throws IOException {
			        String message = new String(body);
			        System.out.println("Received '" + message + "'");
			        handler.handle(message);
		    }
	    	
	    };
	    
	    channel.basicConsume(queueName, true, consumer);
	    		
	}
	
	public static void main(String[] args) throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException {
		sendMessage("localhost", "queue", "hello");
		receiveMessage("localhost", "queue", new MessageHandler() {

			@Override
			public void handle(String message) {
				
			}});
	}

}