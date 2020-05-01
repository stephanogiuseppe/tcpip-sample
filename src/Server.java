import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends JFrame {
    private JTextField textField;
    private JTextArea display;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private ServerSocket serverSocket;
    private Socket socketConnection;
    private int count = 1;


    public Server() {
        super("Server");
        Container containerContentPane = getContentPane();
        textField = new JTextField();
        textField.addActionListener(
            new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    sendData(actionEvent.getActionCommand());
                }
            }
        );

        containerContentPane.add(textField, BorderLayout.NORTH);
        display = new JTextArea();
        containerContentPane.add(new JScrollPane(display), BorderLayout.CENTER);
        display.setEnabled(false);
        textField.setEnabled(false);
        setSize(500, 300);
        setVisible(true);

        System.out.println(textField);
    }

    private void sendData(String msg) {
        try {
            outputStream.writeObject(msg);
            outputStream.flush();
            display.append(">> Server says: " + msg + "\n");
        } catch (IOException ioException) {
            display.append("Write error");
        }
    }

    private void runServerSocket() throws IOException {
        try {
            serverSocket = new ServerSocket(5000, 2);
            while (true) {
                waitConnection();
                startIOStream();
                openConnection();
                closeConnection();
                ++count;
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void waitConnection() throws IOException {
        display.setText("Waiting connection\n");
        socketConnection = serverSocket.accept();

        display.append("Connection " + count + ". Received from: " + socketConnection.getInetAddress().getHostName());
    }

    private void startIOStream() throws IOException {
        outputStream = new ObjectOutputStream(socketConnection.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(socketConnection.getInputStream());
        display.append("I/O stream started!\n");
    }

    private void openConnection() throws IOException {
        Boolean isConnected = true;
        outputStream.writeObject("Connection success");
        outputStream.flush();
        textField.setEnabled(true);

        while (isConnected) {
            try {
                display.append("> Client says: " + (String) inputStream.readObject() + "\n");
                display.setCaretPosition(display.getText().length());
            } catch (ClassNotFoundException | EOFException e) {
                isConnected = false;
                display.append(">>> Unknown object\n");
                e.printStackTrace();
            }
        }
    }

    private void closeConnection() throws IOException {
        try {
            display.append("Close connection\n");
            textField.setEnabled(false);
            inputStream.close();
            outputStream.close();
            socketConnection.close();
        } catch (IOException e) {
            System.out.println("ERROR");
        }
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        server.runServerSocket();
    }
}
