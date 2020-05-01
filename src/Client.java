import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends JFrame {
    private JTextField textField;
    private JTextArea display;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private Socket clientSocketConnection;
    private String message = "";
    private String connectionServer;

    public Client (String host) {
        super("Client");
        connectionServer = host;
        Container container = getContentPane();
        textField = new JTextField();
        textField.setEnabled(false);
        textField.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        sendData(actionEvent.getActionCommand());
                    }
                }
        );

        container.add(textField, BorderLayout.NORTH);
        display = new JTextArea();
        container.add(new JScrollPane(display), BorderLayout.CENTER);
        display.setEnabled(false);
        setSize(500, 300);
        setVisible(true);
    }

    private void sendData(String msg) {
        try {
            outputStream.writeObject(msg);
            outputStream.flush();
            display.append("> Client says: " + msg + "\n");
        } catch (EOFException eofException) {
            display.append("ERROR: Server is offline\n");
            eofException.printStackTrace();
        } catch (IOException ioException) {
            textField.setEnabled(false);
            display.append("ERROR: Server is offline\n");
            ioException.printStackTrace();
        }
    }

    public void runClientSocket() {
        try {
            while (true) {
                serverConnection();
                startIOStream();
                openConnection();
                closeConnection();
            }
        } catch (ConnectException connectException) {
            display.setText("ERROR: Cannot connect to the server.\n");
        }  catch (EOFException eofException) {
            textField.setEnabled(false);
            display.append("ERROR: Server is offline\n");
            eofException.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void serverConnection() throws IOException {
        display.setText("Stablishing connection...\n");
        clientSocketConnection = new Socket(InetAddress.getByName(connectionServer), 5000);
        display.append("Connecting to: " + clientSocketConnection.getInetAddress().getHostName() + "\n");
    }

    private void startIOStream() throws IOException {
        outputStream = new ObjectOutputStream(clientSocketConnection.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(clientSocketConnection.getInputStream());
        display.append("I/O stream started!\n");
    }

    private void openConnection() throws IOException {
        Boolean isConnected = true;
        textField.setEnabled(true);

        while (isConnected) {
            try {
                display.append(">> Server says: " + (String) inputStream.readObject() + "\n");
                display.setCaretPosition(display.getText().length());
            } catch (ClassNotFoundException e) {
                isConnected = false;
                display.append("ERROR: Unknown object\n");
                e.printStackTrace();
            }
        }
        outputStream = new ObjectOutputStream(clientSocketConnection.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(clientSocketConnection.getInputStream());
    }

    private void closeConnection() throws IOException {
        display.append("Connection closed");
        textField.setEnabled(false);
        inputStream.close();
        outputStream.close();
        clientSocketConnection.close();
    }

    public static void main(String[] args) {
        String host = "127.0.0.1";

        if (args.length != 0) {
            host = args[0];
        }

        Client client = new Client(host);

        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.runClientSocket();
    }
}
