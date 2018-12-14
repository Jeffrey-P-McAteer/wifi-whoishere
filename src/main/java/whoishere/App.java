/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package whoishere;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

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
                    final String output = getFormattedMACs();
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
        
        
        this.add(scrollPane, BorderLayout.CENTER);
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
    
    // We use the "ping" utility
    public static java.util.List<String> pingAndGetIpsUnix()
    {
        ArrayList<String> parsed_ips = new ArrayList<>();
        String ping_payload = execCmd("ping -c 3 224.0.0.1");
        for (String line : ping_payload.split(System.lineSeparator())) {
            // Line is like:
            // 64 bytes from 192.168.86.37: icmp_seq=1 ttl=64 time=86.5 ms (DUP!)
            if (!line.contains("bytes from")) {
                continue; // Ignore lines we don;t want (headers, empty lines, etc)
            }
            String[] chunks = line.split(" ");
            // Check to make sure we have at least 4 seperate words, if not continue
            if (chunks.length < 4) {
                continue;
            }
            String ip = chunks[3].replaceAll(":", ""); // A dumb way to get the semicolon at the end off
            // Only add IP if new
            if (!parsed_ips.contains(ip)) {
                parsed_ips.add(ip);
            }
        }
        
        return parsed_ips;
    }
    
    // We use the "arp" utility, this returns a map from IP => MAC
    public static HashMap<String, String> getArpTableUnix()
    {
        HashMap<String, String> map = new HashMap<>();
        String arp_payload = execCmd("arp -a");
        
        for (String line : arp_payload.split(System.lineSeparator())) {
            String[] chunks = line.split(" ");
            if (chunks.length < 4) {
                continue; // We ignore any line with fewer than 4 words
            }
            
            String hostname = chunks[0];
            String ip = chunks[1];
            String mac = chunks[3].toLowerCase();
            
            if (mac.equals("<incomplete>")) {
                continue; // Happens when you have ARP entries on the docker0 interface
            }
            
            if (!map.containsKey(ip)) {
                map.put(ip, mac);
            }
        }
        
        return map;
    }
    
    public static String getFormattedMACs()
    {
        if (isUnix()) {
            StringBuilder sb = new StringBuilder();
            // Ping everyone to fill the ARP table
            pingAndGetIpsUnix();
            HashMap<String, String> ips_and_macs = getArpTableUnix();
            for (String mac : ips_and_macs.values()) {
                sb.append(mac);
                String vendor = getVendorForMacFromCache(mac);
                sb.append(" - "+vendor);
                sb.append(System.lineSeparator());
            }
            
            return sb.toString();
        }
        else {
            return "Idk? Bluescreening in 3, 2, 1...";
        }
    }
    
    // We first try to get vendor from a filesystem cache, falling back to the API at api.macvendors.com when we don't know.
    // Ideally we will only call api.macvendors.com once or twice per run.
    public static String getVendorForMacFromCache(String mac)
    {
        try {
            String path_to_cache_dir = System.getProperty("user.home") + File.separator + ".wifi-whoishere-vendorcache";
            File cache_dir = new File(path_to_cache_dir);
            if (!cache_dir.exists()) {
                cache_dir.mkdirs();
            }
            
            File mac_cached_file = new File(path_to_cache_dir + File.separator + mac + ".txt");
            // ^^ We will appease the windows gods with a .txt file extension. Ideally you'd just use the MAC entirely.
            if (!mac_cached_file.exists()) {
                System.err.println("Querying API for vendor of unknown MAC "+mac+"...");
                String vendor = doHTTPGET("https://api.macvendors.com/" + mac).trim();
                System.err.println("Vendor "+mac+" is "+vendor);
                // Only record vendor when we know them
                if (vendor.length() > 1) {
                    writeStringToFile(mac_cached_file, vendor);
                }
                else {
                    // If we have an empty vendor return an empty string instead of writing emptystring to file
                    return "";
                }
            }
            
            // Read the cache; return contents of file
            return readFileToString(mac_cached_file).trim();
        }
        catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    
    public static String doHTTPGET(String urlToRead)
    {
        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlToRead);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            return result.toString();
        }
        catch (Exception e) {
            // Only if you are curious why you get no data
            //e.printStackTrace();
            return "";
        }
    }

    // Overload of readFileToString(String) taking a File
    public static String readFileToString(File file) {
        return readFileToString(file.getAbsolutePath());
    }
    
    // Stolen from https://howtodoinjava.com/java/io/java-read-file-to-string-examples/
    public static String readFileToString(String filePath)
    {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }
    
    public static void writeStringToFile(File target, String payload)
    {
        try (PrintWriter out = new PrintWriter(target.getAbsolutePath())) {
            out.print(payload);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
 
    
}
