import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Future;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.net.InetSocketAddress;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;

public class Client {

    public static void main (String [] args)
            throws Exception {
	
        new Client().go();
    }

    private void go()
            throws IOException, InterruptedException, ExecutionException {
	
        AsynchronousSocketChannel client = AsynchronousSocketChannel.open();
        InetSocketAddress hostAddress = new InetSocketAddress("localhost", 3883);
        Future future = client.connect(hostAddress);
        future.get(); // returns null

        System.out.println("Client is started: " + client.isOpen());
        System.out.println("Sending messages to server: ");
		
        String [] messages = new String [] {"Time goes fast.", "What now?", "Bye."};
		
		System.out.println("Reading from server after connecting");
		ByteBuffer buffer = ByteBuffer.allocate(2048);
        
		client.read(buffer).get();
		System.out.println(buffer.hasRemaining());
		String s = new String(buffer.array()).trim();
		System.out.println("Message: " + s);
		s = "FOP";
		buffer.clear();
		System.out.println(s);
		buffer = ByteBuffer.wrap(s.getBytes());
		client.write(buffer).get();
		System.out.println("Done");
		buffer.flip();
		
		while(true){}
		//client.close();
	}
}