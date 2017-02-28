

package async;

import bayou.http.*;
import bayou.websocket.*;
import bayou.async.*;
import java.time.Duration;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.nio.charset.Charset;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.io.ByteArrayOutputStream;
import javax.xml.bind.DatatypeConverter;
import java.security.*;
import org.glassfish.tyrus.server.Server;
import javax.websocket.*;
import javax.websocket.server.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

public class MyWsHandler extends Endpoint
{

	 //AsynchronousSocketChannel client = null;
     //InetSocketAddress hostAddress = new InetSocketAddress("localhost", 22);
	 boolean init = false;
	 int i =0;
	 int counter =0;
	 ByteArrayOutputStream baos = new ByteArrayOutputStream();
	 ReentrantLock lock = new ReentrantLock();
	 HashMap<String,AsynchronousSocketChannel> map = new HashMap<String,AsynchronousSocketChannel>();
	 
   /* @Override
    public Async<WebSocketResponse> handle(WebSocketRequest request)
    {
        // note: same-origin has already been enforced (default configuration)        
			init = true;			
			i=0;
			System.out.println("Got WebSocket Request");
            return WebSocketResponse.accept( this::handleChannel );
    }*/
	
	class Attach {
		public AsynchronousSocketChannel client;
		public Session channel;
	}
		
   void readFromServer(Session channel,AsynchronousSocketChannel client){	
	final ByteBuffer buffer = ByteBuffer.allocate(50000);
	Attach attach = new Attach();
	attach.client = client;
	attach.channel = channel;
	// callback 2
	//System.out.println("Waiting for Client");
							client.read(buffer, attach, new CompletionHandler<Integer, Attach>() {
								@Override
								public void completed(Integer result, final Attach scAttachment) {	
									//System.out.println("Reading from server completed");
									//System.out.println("Result for Read: " + result);
									//System.out.println("Has Remaining " + buffer.remaining());
									//System.out.println("Buffer Limit " + buffer.limit());
									counter++;
									buffer.clear();
										try {					
										
										  if(buffer.hasRemaining() && result>=0)
										  {												
												byte arr[] = new byte[result];	
												ByteBuffer b = buffer.get(arr,0,result);												
												baos.write(arr,0,result);												
												//scAttachment.writeBinary(baos.toByteArray());
												ByteBuffer q = ByteBuffer.wrap(baos.toByteArray());		
												/*if(counter>5)
												{
													Thread.sleep(1000);
													counter = 0;
												}*/
												scAttachment.channel.getBasicRemote().sendBinary(q);
												System.out.println("Sent " + baos.size());
												String message = new String(buffer.array()).trim();		
												//System.out.println(new String(baos.toByteArray()));
												//System.out.println("HASH : " + DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(baos.toByteArray())));
												baos = new ByteArrayOutputStream();						
												//System.out.println("Client is connected: " + client.isOpen());			
												readFromServer(scAttachment.channel,scAttachment.client);
												//client.read(buffer, scAttachment, this);
											}else{
												if(result > 0)
												{
													byte arr[] = new byte[result];
													ByteBuffer b = buffer.get(arr,0,result);
													baos.write(arr,0,result);													
												}
												readFromServer(scAttachment.channel,scAttachment.client);
												//client.read(buffer, scAttachment, this);
											}
											} catch (Exception e) {
												e.printStackTrace();
											}																										

									
									//byte [] message1 = new String("OVER").getBytes();
									//buffer.wrap(message1);
																									
								}
								@Override
								public void failed(Throwable t, Attach scAttachment) {
									t.printStackTrace();
								}								
							});	
	
	}

	
	void writeToServer(ByteBuffer z,Session channel,AsynchronousSocketChannel client){
		System.out.println("Received " + z.capacity());
		client.write(z, z, new CompletionHandler<Integer, ByteBuffer>() {		
										@Override
										public void completed(Integer result, ByteBuffer attach) {
											z.flip();											
											try{
											//System.out.println("HASH : " + DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(attach.array())));
											}catch(Exception e){}
											//System.out.println("Sent to Server " + result);											
											//readFromServer(channel);
										}
										
										@Override
										public void failed(Throwable t, ByteBuffer asy) {
											t.printStackTrace();
										}	
		});
	}
	
	void process(ByteBuffer z,Session channel)
	{
		lock.lock();
		try{
			if(i>1)
			{
				//System.out.println("DATA FROM WS " +new String(z.array()).trim());
				AsynchronousSocketChannel client = (AsynchronousSocketChannel) map.get(channel.getId());
				writeToServer(z,channel,client);										
			}
			else if(i==1)
			{
				String values = new String(z.array());
				String[] array = values.split("\\|"); 				
				System.out.println("Connected to " + array[0] + " on port " + array[1]);
				AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
				int po = Integer.parseInt(array[1]);
				InetSocketAddress hostAddress = new InetSocketAddress(array[0], po);
				Future future = client.connect(hostAddress);
				future.get(); // returns null
				System.out.println("Client is started: " + client.isOpen());
				map.put(channel.getId(), client);
				init=false;
				//writeToServer(z,channel);
				readFromServer(channel,client);
				/*ByteBuffer buffer = ByteBuffer.allocate(5000); 
				System.out.println("BEFORE READ");
				int result = client.read(buffer).get();
				buffer.clear();
				byte arr[] = new byte[result];
				ByteBuffer b = buffer.get(arr,0,result);
				System.out.println("READ FROM SERVER " + (buffer.capacity() - buffer.remaining()));				
				//channel.writeBinary(b.array());
				 channel.getBasicRemote().sendBinary(b);
				//handleChannel(channel);*/
			}
			}catch(Exception e){
				e.printStackTrace();
			  }finally{
				lock.unlock();
			  }
	}
		
	 @Override
    public void onOpen(final Session session, EndpointConfig config) {
		i=0;
		System.out.println(session.getId());
        session.addMessageHandler(new MessageHandler.Whole<ByteBuffer>() {			
            @Override
            public void onMessage(ByteBuffer message) {
                try {
					message.clear();
					i++;
					//System.out.println("VALUE OF " + i);
					//System.out.println("READ FROM WS " + message.capacity());
					process(message,session);					
					/*for(int i=0;i<=100;i++)
					{
						session.getBasicRemote().sendText("MSG IS " + String.valueOf(i));
					}*/
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
	
}
