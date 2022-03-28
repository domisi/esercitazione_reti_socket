import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * A simple Swing-based client for the chat server. Graphically it is a frame
 * with a text field for entering messages and a textarea to see the whole
 * dialog.
 *
 * The client follows the following Chat Protocol. When the server sends
 * "SUBMITNAME" the client replies with the desired screen name. The server will
 * keep sending "SUBMITNAME" requests as long as the client submits screen names
 * that are already in use. When the server sends a line beginning with
 * "NAMEACCEPTED" the client is now allowed to start sending the server
 * arbitrary strings to be broadcast to all chatters connected to the server.
 * When the server sends a line beginning with "MESSAGE" then all characters
 * following this string should be displayed in its message area.
 */
public class ChatClient_reti {

    String serverAddress;
    Scanner in;
    PrintWriter out;
    String name;
    String friend_list;
    JFrame frame = new JFrame("Chatter");
    JTextField textField = new JTextField(50);
    JTextArea messageArea = new JTextArea(16, 50);
    JTextArea usersArea = new JTextArea(16, 15);

    /**
     * Constructs the client by laying out the GUI and registering a listener with
     * the textfield so that pressing Return in the listener sends the textfield
     * contents to the server. Note however that the textfield is initially NOT
     * editable, and only becomes editable AFTER the client receives the
     * NAMEACCEPTED message from the server.
     */
    public ChatClient_reti(String serverAddress) {
        this.serverAddress = serverAddress;

        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.getContentPane().add(new JScrollPane(usersArea), BorderLayout.EAST);
        frame.pack();

        // Send on enter then clear to prepare for next message
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
    }

    private String getName() {
        return JOptionPane.showInputDialog(frame, "Choose a screen name:", "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void run() throws IOException {
        try {
            var socket = new Socket(serverAddress, 28993);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {
                var line = in.nextLine();
                if (line.startsWith("SUBMITNAME")) {
                    name = getName();
                    out.println(name); // passo name a server
                } else if (line.startsWith("NAMEACCEPTED")) {
                    this.frame.setTitle("Chatter - " + line.substring(13));
                    textField.setEditable(true);
                    out.println("getuserlist"); // invio la stringa "getuserlist" per ricevere lista partecipanti
                } else if (line.startsWith("MESSAGE")) {
                    messageArea.append(line.substring(8) + "\n");
                } else if (line.startsWith("LIST")){
                    usersArea.setText(null); // resetto la usersArea per contenere gli utenti attivi adesso
                    //System.out.println(line);
                    friend_list = line.substring(5).replace("[","").replace("]","").replace(" ","");
                    for(String curr_friend : friend_list.split(",")){
                        usersArea.append(curr_friend + "\n");
                    }
                } else if (line.startsWith("JOIN")){
                    // richiedo lista utenti
                    out.println("getuserlist");
                } else if (line.startsWith("EXT")){
                    // richiedo lista utenti
                    out.println("getuserlist");
                }
            }
        } finally {
            frame.setVisible(false);
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        String server_ip = "localhost";
        var client = new ChatClient_reti(server_ip);
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
        client.run();
    }
}