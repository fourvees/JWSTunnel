package async;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;

public class Server {

    public static void main (String [] args)
            throws Exception {
	
        new Server().go();
    }
	
    private void go()
            throws IOException, InterruptedException, ExecutionException {

        AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open();
        InetSocketAddress hostAddress = new InetSocketAddress("localhost", 3883);
        serverChannel.bind(hostAddress);
		
        System.out.println("Server channel bound to port: " + hostAddress.getPort());
        System.out.println("Waiting for client to connect... ");
		
        Future<AsynchronousSocketChannel> acceptResult = serverChannel.accept();
        AsynchronousSocketChannel clientChannel = acceptResult.get();

		ByteBuffer message = ByteBuffer.wrap("ping".getBytes());
		//buffer.wrap("GGGGG".getBytes());
		
		clientChannel.write(message).get();
		
		message.flip();
		message = ByteBuffer.allocate(5);
		clientChannel.read(message).get();
		String s = new String(message.array());
		System.out.println("Message: " + s.length());
		message.clear();
		
        /*System.out.println("Messages from client: ");

        if ((clientChannel != null) && (clientChannel.isOpen())) {

            while (true) {

                ByteBuffer buffer = ByteBuffer.allocate(32);
                Future result = clientChannel.read(buffer);

                while (! result.isDone()) {
  //                  System.out.print(".");
                }
				
                buffer.flip();
                String message = new String(buffer.array()).trim();
				//System.out.println(buffer.array().length);
				System.out.println(Arrays.toString(buffer.array()));
				
				byte [] message1 = new String("OVER").getBytes();
				ByteBuffer buffer1 = ByteBuffer.wrap(message1);
				Future result1 = clientChannel.write(buffer1);
		
            while (! result1.isDone()) {
                System.out.println("... ");
            }            
            buffer1.clear();
				
                //System.out.println(message.length());

                if (message.equals("Bye.")) {					
                    break; // while loop
                }

                buffer.clear();

            } // while()

           // clientChannel.close();
		
        } // end-if */
		
       // serverChannel.close();
	   while(true){}
    }
}