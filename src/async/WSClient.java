package async;

import org.glassfish.tyrus.client.ClientManager;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import java.net.URI;
import javax.websocket.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
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
import javax.xml.bind.DatatypeConverter;
import java.security.*;
import com.neovisionaries.ws.client.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class AsynCon
{
public AsynchronousSocketChannel channel;
//public Session session;
public WebSocket session;
public ByteBuffer buffer;
public boolean isFirst;

public AsynCon(AsynchronousSocketChannel channel,WebSocket session,ByteBuffer buffer)
{
	this.channel = channel;
	this.session = session;
	this.buffer = buffer;
	isFirst = true;
} 

public AsynCon()
{

}

}

public class WSClient {

public static ByteArrayOutputStream baos = new ByteArrayOutputStream();
    static boolean inprogress = false;
 private static  ReentrantLock lock;
    private static  Queue<ByteBuffer> queue;
	static String IV = "AAAAAAAAAAAAAAAA";
  static String plaintext = "test text 123\0\0\0"; /*Note null padding*/
  static String encryptionKey = "0123456789abcdef" + IV;
	
   static boolean write(byte[] data, AsynCon asy){ 
		System.out.println("Received " + data.length);		
		lock.lock();
		final ByteBuffer buffer = ByteBuffer.wrap(data);		
		try{	
			boolean wasEmpty = queue.isEmpty();
			queue.add( buffer );
			
            if (wasEmpty)
            {    						
				asy.isFirst	= true;
				//System.out.println("Before write");
		// callback 3
									asy.channel.write(buffer, asy, new CompletionHandler<Integer, AsynCon>() {		
										@Override
										public void completed(Integer result, AsynCon asy) {	
										lock.lock();										
										buffer.flip();										
										try{
										//System.out.println(asy.isFirst);
										//System.out.println("Is Buffer Fill .. " + !buffer.hasRemaining());
										System.out.println("Queue Size " + queue.size());
										if(asy.isFirst)
										{
											ByteBuffer byteBuffer = queue.peek();											
											queue.poll();
											asy.isFirst = false;											
										}
											
										if(!queue.isEmpty())
										{
											ByteBuffer byteBuffer = queue.peek();
											asy.channel.write(byteBuffer, asy,this);
											queue.poll();
										}
										
												if (buffer.hasRemaining()) {
													//System.out.println("Data Left");
													//connection.write(buffer, null, this);
												}
											}finally{
												lock.unlock();
											}
										}		
										@Override
										public void failed(Throwable t, AsynCon asy) {
											t.printStackTrace();
										}										
									});	
		
		}	
		}finally {			
			lock.unlock();
		}
		
		return true;		
	}
	
	static void read(AsynCon asy){	
	final ByteBuffer buffer = ByteBuffer.allocate(50000);
	//System.out.println(asy.channel);
	// callback 2
	//System.out.println("Waiting for Client");
							asy.channel.read(buffer, asy, new CompletionHandler<Integer, AsynCon>() {
								@Override
								public void completed(Integer result, final AsynCon scAttachment) {
									
									//System.out.println("Reading from client completed");
									//System.out.println("Result for Read: " + result);
									//System.out.println("Has Remaining " + buffer.remaining());
									//System.out.println("Buffer Limit " + buffer.limit());
									
									buffer.clear();
									
									/*if (result == -1) {
										System.out.println("No data... ");																					
											asy.channel.read(buffer, asy, this);										
								    }*/
									
										try {					
										
										  if(buffer.hasRemaining() && result>=0)
										  {
												
												byte arr[] = new byte[result];	
												ByteBuffer b = buffer.get(arr,0,result);												
												baos.write(arr,0,result);
												//ByteBuffer q = ByteBuffer.wrap(baos.toByteArray());
												asy.session.sendBinary(baos.toByteArray());
												System.out.println("Sent " + baos.size());
												String message = new String(buffer.array()).trim();		
												//System.out.println(new String(baos.toByteArray()));
												//System.out.println("HASH : " + DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(arr)));
												//System.out.println("HASH : " + DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(baos.toByteArray())));
												baos = new ByteArrayOutputStream();
												read(scAttachment);
												//asy.channel.read(buffer, asy, this);
												//System.out.println("From Client : " + message);
												//System.out.println(Arrays.toString(buffer.array()));
										
												/*HttpClient client = new HttpClientConf()
													.proxy(new HttpProxy("proxy.cognizant.com", 6050,"414612","Fourvees@19832712"))
													.trafficDump(System.err::print)
													.newClient();
													
												HttpRequest request = HttpRequest.toPost("http://s3.amazonaws.com/ssd","application/x-www-form-urlencoded",message.getBytes());
												Async<HttpResponse> asyncRes = client.send( request );
												asyncRes.peek( response -> action2(response,connection));																																	
												*/
											}else{
											if(result > 0)
												{
													byte arr[] = new byte[result];
													ByteBuffer b = buffer.get(arr,0,result);
													baos.write(arr,0,result);
													read(scAttachment);
												}
												
												//asy.channel.read(buffer, asy, this);
											}
											
											} catch (Exception e) {
												e.printStackTrace();
											}																										

									
									//byte [] message1 = new String("OVER").getBytes();
									//buffer.wrap(message1);
																									
								}
								@Override
								public void failed(Throwable t, AsynCon scAttachment) {
									t.printStackTrace();
								}								
							});	
	
	}
   
   
    public static String encrypt(String plainText, String encryptionKey) throws Exception {
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING", "SunJCE");
    SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
    cipher.init(Cipher.ENCRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));
	byte[] enc = cipher.doFinal(plainText.getBytes());
    return Base64.getEncoder().encodeToString(enc); 
  }

  public static String decrypt(String cipherText, String encryptionKey) throws Exception{
    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING", "SunJCE");
    SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes("UTF-8"), "AES");
    cipher.init(Cipher.DECRYPT_MODE, key,new IvParameterSpec(IV.getBytes("UTF-8")));	
    return new String(cipher.doFinal(Base64.getDecoder().decode(cipherText)));
  }
  
  public static void config() {
    try {
        Properties props = new Properties();
		//System.out.print("Server Address");
		//String ss = "121212";
		//System.out.println(encrypt(ss,encryptionKey));
		//System.out.println(decrypt(encrypt(ss,encryptionKey),encryptionKey));		
		String encKey = Long.toHexString(Double.doubleToLongBits(Math.random()));		
		System.out.println(encKey);	
		props.setProperty("EncryptionKey", encKey);		
        props.setProperty("WebSocketHost", System.console().readLine("WebSocket Host : ",null));
		props.setProperty("ProxyHost", System.console().readLine("Proxy Host : ",null));
		props.setProperty("ProxyPort", System.console().readLine("Proxy Port : ",null));
		props.setProperty("ProxyUser", System.console().readLine("Proxy User : ",null));		
		props.setProperty("ProxyPassword", new String(encrypt(new String(System.console().readPassword("Proxy Password : ",null)) , encKey) ));
		props.setProperty("ThreadDelay", System.console().readLine("Thread Delay : ",null));		
      //  props.setProperty("ServerPort", ""+serverPort);
      //  props.setProperty("ThreadCount", ""+threadCnt);
        File f = new File("config.properties");
        OutputStream out = new FileOutputStream( f );
        props.store(out, "Generated by JWSTunnel");
    }
    catch (Exception e ) {
        e.printStackTrace();
    }
}
   
   public static Properties load() {
   try{
		Properties prop = new Properties();
		InputStream  input = new FileInputStream("config.properties");
		prop.load(input);
		return prop;
   }catch(Exception e) {
		System.out.println(e.getMessage());
   }
   return null;
}
   
    public static void main(String [] args){
        try {
         
			Properties prop = load();
			
			if(prop==null)
			{
				System.out.println("Unable to load config file. Run with 'config' argument.");
				System.exit(1);
			}
			
			if(args.length>=1 && args[0].equals("config"))
			{
				config();
				System.exit(0);
			}
			
			if(args.length<3)
			{
				System.out.println("Invalid Arguments");
				System.exit(1);
			}
		 
			AsynCon asy=new AsynCon();
			lock = new ReentrantLock();
			queue = new LinkedList<ByteBuffer>();
			String SENT_MESSAGE = new String("Hello World".getBytes(),"US-ASCII");
			
			AsynchronousServerSocketChannel listener = AsynchronousServerSocketChannel.open();
			listener.setOption(StandardSocketOptions.SO_REUSEADDR, true);
			listener.bind(new InetSocketAddress("localhost", Integer.parseInt(args[0])));
					
            final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();

            ClientManager client = ClientManager.createClient();
			Session ses = null;			
            
			
			//Thread.sleep(5000);
           //ses.getBasicRemote().sendBinary(ByteBuffer.wrap("Java".getBytes(Charset.forName("US-ASCII" ))));
		   //ses.getBasicRemote().sendText("FFF");
		  // Thread.sleep(10000);
		  // ses.getBasicRemote().sendBinary(ByteBuffer.wrap("CPP".getBytes(Charset.forName("US-ASCII" ))));
		   //ses.getBasicRemote().sendText("GGG");
		  // Thread.sleep(15000);
		  // ses.getBasicRemote().sendBinary(ByteBuffer.wrap("SCALA".getBytes(Charset.forName("US-ASCII" ))));
		   //ses.getBasicRemote().sendText("KKK");		
		  
		   System.out.println("Listening for connection from client...");
		   while (true) {
				
				// callback 1
				listener.accept(asy, new CompletionHandler<AsynchronousSocketChannel, AsynCon>() {
					@Override
					public void completed(AsynchronousSocketChannel connection, AsynCon v) {							
							listener.accept(v, this); // get ready for next connection											
							final ByteBuffer buffer = ByteBuffer.allocate(32);
							System.out.println("Client connected...");
							byte[] emptyArray = new byte[0];							
							v.channel = connection;
							v.buffer = buffer;									
							try{
							
							WebSocketFactory factory = new WebSocketFactory();
			ProxySettings settings = factory.getProxySettings();
			settings.setHost(prop.getProperty("ProxyHost"));
			settings.setPort(Integer.parseInt(prop.getProperty("ProxyPort")));			
			settings.setCredentials(prop.getProperty("ProxyUser"), decrypt(prop.getProperty("ProxyPassword"),prop.getProperty("EncryptionKey")));
			//ws://ec2-34-195-77-61.compute-1.amazonaws.com
			WebSocket ws = factory.createSocket("ws://" + prop.getProperty("WebSocketHost") +"/websocket");
			int delay = Integer.parseInt(prop.getProperty("ThreadDelay"));
			
			ws.addListener(new WebSocketAdapter() {
			@Override
			public void onTextMessage(WebSocket websocket, String message) throws Exception {
					System.out.println("TXT MSG " + message);
				}
			
			public void onBinaryMessage(WebSocket websocket, byte[] binary)
			{
				try{
					Thread.sleep(delay);
					//System.out.println("HASH : " + DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(binary)));
				}catch(Exception e)
				{
					e.printStackTrace();
				}
				//while(!inprogress)
				//{
					//System.out.println(inprogress);
				//}
				write(binary,asy);
				
			}
			
			public void onDisconnected(WebSocket websocket,
                    WebSocketFrame serverCloseFrame,
                    WebSocketFrame clientCloseFrame,
                    boolean closedByServer)
			{
						System.out.println("Websocket connection closed");
			}
			
			
			public void onConnected(WebSocket websocket,
                 Map<String,List<String>> headers){
					System.out.println("Connected to Websocket");
			}

			public void onMessageError(WebSocket websocket,
                    WebSocketException cause,
                    List<WebSocketFrame> frames) {
					System.out.println("Error receiving message from Websocket");
			}
			
			public void onUnexpectedError(WebSocket websocket,
                       WebSocketException cause){
					System.out.println("Error ....");
			}
			
			});
			
			ws.addHeader("Origin", "http://" + prop.getProperty("WebSocketHost"));
		    ws.connect();		   
			v.session = ws;			
								//v.session.disconnect();
								//v.session.connect();
								//Thread.sleep(5000);
								//v.session.getBasicRemote().sendBinary(ByteBuffer.wrap("\n".getBytes()));							
								String msg11 = args[1] + "|" + args[2];
								//System.out.println(msg11);
								v.session.sendBinary(msg11.getBytes());
								read(v);
							}catch(Exception e) {  e.printStackTrace(); }
					}
					@Override
					public void failed(Throwable t, AsynCon v) {
						t.printStackTrace();
					}					
				});

				System.in.read(); // so we don't exit before a connection is established
				
			}
		   
		   
        } catch (Exception e) {
            e.printStackTrace();
        }
		
			
    }
}