package bg.sofia.uni.fmi.mjt.splitwise.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Client {

    private static final int SERVER_PORT = 6757;
    private static final String SERVER_HOST = "localhost";
    private static final int BUFFER_SIZE = 1024;

    private static final String DISCONNECT_MESSAGE = "Disconnected from server" + System.lineSeparator();
    private static final String SHUTDOWN_SERVER_MESSAGE = "Server is shutting down" + System.lineSeparator();

    private static ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);

    public static void main(String[] args) {

        try (SocketChannel socketChannel = SocketChannel.open();
                Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT));

            System.out.println("Connected to the server.");

            while (true) {
                System.out.print("$ ");
                String message = scanner.nextLine();

                if (message.isBlank() || message.isEmpty()) {
                    continue;
                }

                buffer.clear();
                buffer.put(message.getBytes());
                buffer.flip();
                socketChannel.write(buffer);

                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();

                byte[] byteArray = new byte[buffer.remaining()];
                buffer.get(byteArray);
                String reply = new String(byteArray, "UTF-8");

                if (reply.equals(DISCONNECT_MESSAGE) ||
                        reply.equals(SHUTDOWN_SERVER_MESSAGE)) {
                    System.out.println(reply);
                    break;
                }

                System.out.println(reply);
            }

        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the network communication", e);
        }
    }
}
