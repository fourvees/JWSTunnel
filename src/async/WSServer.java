
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
import java.io.*;


public class WSServer
{
public static void main(String[] args) throws Exception
{
    Server server = new Server("0.0.0.0", 80, "/", null, MyApplicationConfig.class);
	
    try {
        server.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Please press a key to stop the server.");
        reader.readLine();
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        server.stop();
    }
}

}