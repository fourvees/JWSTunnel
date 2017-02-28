package async;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import bayou.http.HttpClient;
import bayou.http.HttpClientConf;
import bayou.http.HttpRequest;
import bayou.http.HttpResponse;
import bayou.async.Async;
import bayou.http.HttpProxy;
import java.io.ByteArrayOutputStream;

public class EchoServer {
	
	static void action2(HttpResponse x, AsynchronousSocketChannel connection){ 
		System.out.println("TEST");
		
		Async<String> resp = x.bodyString(1024*500);
		resp.peek(val -> System.out.println(val))
		    .peek(val -> action3(val.getBytes(),connection));
		
		try{
			System.out.println(connection.getLocalAddress());
		}catch(Exception e){}
	}
	
	static void action3(byte[] data, AsynchronousSocketChannel connection){ 
		System.out.println(data);
		final ByteBuffer buffer = ByteBuffer.wrap(data);		
		// callback 3
									connection.write(buffer, null, new CompletionHandler<Integer, ByteBuffer>() {		
										@Override
										public void completed(Integer result, ByteBuffer bbAttachment) {
										System.out.println("Writing client completed");
										buffer.flip();
										System.out.println(result);
											/*if (buffer.hasRemaining()) {
												System.out.println("Data Left");
												connection.write(buffer, null, this);
											} else { */
											System.out.println("Writing client completed");
												//bbAttachment.clear();
												action4(connection);
											//}
											
										}		
										@Override
										public void failed(Throwable t, ByteBuffer bbAttachment) {
											t.printStackTrace();
										}										
									});	
		
	}
	
	static void action4(AsynchronousSocketChannel connection){	
	final ByteBuffer buffer = ByteBuffer.allocate(2);
	// callback 2
	System.out.println("Waiting for Client");
							connection.read(buffer, connection, new CompletionHandler<Integer, AsynchronousSocketChannel>() {
								@Override
								public void completed(Integer result, final AsynchronousSocketChannel scAttachment) {	
									System.out.println("Reading from client completed");
									System.out.println("Result for Read: " + result);
									System.out.println("Has Remaining " + buffer.remaining());
									System.out.println("Remaining " + buffer.remaining());
									
									buffer.clear();
									
									if (result == -1) {
										System.out.println("No data... ");																					
											connection.read(buffer, connection, this);										
								    }
									
										//try {																					
												String message = new String(buffer.array()).trim();		
												System.out.println(message);
												System.out.println(Arrays.toString(buffer.array()));
										
												HttpClient client = new HttpClientConf()
													.proxy(new HttpProxy("proxy.cognizant.com", 6050,"414612","Fourvees@19832712"))
													.trafficDump(System.err::print)
													.newClient();
													
												HttpRequest request = HttpRequest.toPost("http://s3.amazonaws.com/ssd","application/x-www-form-urlencoded",message.getBytes());
												Async<HttpResponse> asyncRes = client.send( request );
												asyncRes.peek( response -> action2(response,connection));																																	
											//} catch (IOException e) {
												//e.printStackTrace();
											//}																										

									
									//byte [] message1 = new String("OVER").getBytes();
									//buffer.wrap(message1);
																									
								}
								@Override
								public void failed(Throwable t, AsynchronousSocketChannel scAttachment) {
									t.printStackTrace();
								}								
							});	
	
	}
	public static void main(String[] args) {

		try (final AsynchronousServerSocketChannel listener = 
				AsynchronousServerSocketChannel.open()) { 
			
		    listener.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			listener.bind(new InetSocketAddress("localhost", 3883));
				
			while (true) {
				
				// callback 1
				listener.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
					@Override
					public void completed(AsynchronousSocketChannel connection, Void v) {							
							listener.accept(null, this); // get ready for next connection				
							final ByteBuffer buffer = ByteBuffer.allocate(32);
							System.out.println("Client connected...");
							
							HttpClient client = new HttpClientConf()
												.proxy(new HttpProxy("proxy.cognizant.com", 6050,"414612","Fourvees@19832712"))
												.trafficDump(System.err::print)
												.newClient();
												
							HttpRequest request = HttpRequest.toPost("http://s3.amazonaws.com/ssd","application/x-www-form-urlencoded", "a=1&b=2".getBytes());
							Async<HttpResponse> asyncRes = client.send( request );
							asyncRes.peek( response -> action2(response,connection));
																						
					}
					@Override
					public void failed(Throwable t, Void v) {
						t.printStackTrace();
					}					
				});

				System.in.read(); // so we don't exit before a connection is established
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} 

	}

}