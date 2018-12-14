/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package whoishere;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class App extends JFrame {
    /**
     * App init point
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new App().init();
        });
    }
    
    private JTextArea output = null;
    
    public void init() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setLayout(new BorderLayout());
        this.setTitle("Who the hell is here?");
        this.setPreferredSize(new Dimension(400, 300));
        
        this.output = new JTextArea(5, 20);
        this.output.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(this.output); 
        
        JButton button = new JButton("Who is here?");
        button.addActionListener((evt) -> {
            // Hop off GUI thread to do work
            new Thread(() -> {
                if (isUnix()) {
                    final String output = execCmd("ping -c 10 224.0.0.1") + System.lineSeparator();
                    // Hop back on GUI thread to set text
                    SwingUtilities.invokeLater(() -> {
                        this.output.append(output);
                    });
                }
                else if (isWindows()) {
                    final String output = execCmd("ping /n 1 224.0.0.1") + System.lineSeparator();
                    // Hop back on GUI thread to set text
                    SwingUtilities.invokeLater(() -> {
                        this.output.append(output);
                    });
                }
                else {
                    System.err.println("Unknown OS!");
                }

                // Roomba nonsense
                com.maschel.roomba.RoombaJSSC roomba = new com.maschel.roomba.RoombaJSSCSerial();
                System.err.println("roomba="+roomba);

            }).start();
        });
        
        
        this.add(this.output, BorderLayout.CENTER);
        this.add(button, BorderLayout.SOUTH);
        
        this.pack();
        
        this.setVisible(true);
        
    }
    
    public static boolean isWindows() {
        return osName().contains("win");
    }
    
    public static boolean isMac() {
        return osName().contains("mac");
    }
    
    public static boolean isUnix() {
        return osName().contains("nux");
    }
    
    public static String osName() {
        return System.getProperty("os.name").toLowerCase();
    }
    
    public static String execCmd(String cmd) {
        try {
          // Magic. It's Magic.
          java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
          return s.hasNext() ? s.next() : "";
        }
        catch (Exception e) {
          e.printStackTrace();
          return "";
        }
    }
    
    public static java.util.List<String> pingAndGetIpsUnix() {
        ArrayList<String> parsed_ips = new ArrayList<>();
        String ping_payload = execCmd("ping -c 2 224.0.0.1");
        
        // TODO
        
        return parsed_ips;
    }
    
}
