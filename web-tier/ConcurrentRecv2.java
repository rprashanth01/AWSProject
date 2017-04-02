
// Prashanth: This code has been partly taken from RabbitMQ async //consumption by Wheleph

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeoutException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.io.UnsupportedEncodingException;
import com.cloud_prml.EC2Commands;


public class ConcurrentRecv2 {

    private final static String QUEUE_NAME = "rpc_queue";

    public static void main(String[] args) throws IOException, InterruptedException, TimeoutException {
        int threadNumber = 10;
        final ExecutorService threadPool =  new ThreadPoolExecutor(threadNumber, threadNumber,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());

        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        final Connection connection = connectionFactory.newConnection();
        final Channel channel = connection.createChannel();


        registerConsumer(channel, 50000, threadPool);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                threadPool.shutdown();
                try {
                    while(!threadPool.awaitTermination(10, TimeUnit.SECONDS));
                } catch (InterruptedException e) {
                }
            }
        });
    }

    private static void registerConsumer(final Channel channel, final int timeout, final ExecutorService threadPool)
            throws IOException {
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,
                    Envelope envelope,
                    AMQP.BasicProperties properties,
                    final byte[] body) throws IOException {
                try {
                     AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                  .Builder()
                  .correlationId(properties.getCorrelationId())
                  .build();

                    threadPool.submit(new Runnable() {
                        public void run() {
                            String response = "Some Exception was caught";
                            try {
                                String message = new String(body,"UTF-8");
                                System.out.println(" Before starting to create the client for " + message); 
                             //   Thread.sleep(timeout);
                           EC2Commands ec = new EC2Commands();
                           System.out.println("created Instance");
                           String id = ec.createInstance(message);
                           ec.checkStatus(message);
                           ec.terminateInstance(id);
                           response = ec.getObject(message);
                           System.out.println("Completed Execution for " + message);
                             //   logger.info(String.format("Processed %s", new String(body)));
                            } catch ( UnsupportedEncodingException e) {
                                System.out.println("in Unspported" + e);
                                response = "error";
                            }catch( Exception e){
                               response = "error";
                               System.out.println("caught Exception while printing" +e);
                            }finally{
                               try{
                                   System.out.println("Inside finally");
                                    channel.basicPublish( "", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                                    //channel.basicAck(envelope.getDeliveryTag(), false);
                               } catch ( Exception e ){
                                   System.out.println("caught Exception in finally"+ e);

                               }
                        }
                      }
                    });
                } catch (Exception e) {
                }
            }
        };
        System.out.println("Before consume");
        channel.basicConsume(QUEUE_NAME,true/* auto-ack */, consumer);
        System.out.println("After Consume");
    }
}
