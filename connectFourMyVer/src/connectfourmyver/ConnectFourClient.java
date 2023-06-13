package connectfourmyver;

import static connectfourmyver.ConnectFourMyVer.secretKeySpec;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
//import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
//import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import static java.lang.Integer.parseInt;
import java.net.Socket;
import java.net.SocketException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
//import java.net.SocketException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;



public class ConnectFourClient implements Runnable {
    boolean logged = false;
    boolean firstLogLine = false;
    //atributi koji se koriste za komunikaciju sa klijentom
    private Socket socket;
    private String username;
    private boolean dostupan;
    private String admin;
    private BufferedReader br;
    private PrintWriter pw;
    private ArrayList<ConnectFourClient> allClients;
    //private ObjectOutputStream outputStream;
    //private ObjectInputStream inputStream;
    private Cipher cipher;
    private IvParameterSpec ivParameterSpec;
    private int otherPlayerNumber;
    private int end=0;
    //getters and setters
    public String getUserName() {
        return username;
    }

    public void setUserName(String username) {
        this.username = username;
    }

    public boolean isDostupan() {
        return dostupan;
    }

    public void setDostupan(boolean dostupan) {
        this.dostupan = dostupan;
    }

    public PrintWriter getPw() {
        return pw;
    }

    public BufferedReader getBr() {
        return br;
    }

    public Socket getSocket() {
        return socket;
    }
    

    public void setOtherPlayerNumber(int otherPlayerNumber) {
        this.otherPlayerNumber = otherPlayerNumber;
    }
    
    private int findPlayer(String findUser){
        int i=0;
        for(ConnectFourClient client1:ConnectFourMyVer.clients){
            if(client1.username.equals(findUser)){
                break;
            }
            i++;
        }
        return i;
    }
    
    private int prvi=1;

    public void setPrvi(int prvi) {
        this.prvi = prvi;
    }
    
    
    
    public String cryptoRead() throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
        System.out.println("Start decrypt");
        String encryptedMessage = br.readLine();           // Decrypt the message
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] decryptedMessage = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
        String message = new String(decryptedMessage);
        System.out.println("End decrypt");
        return message;
    }
    
    public void cryptoWrite(String message) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
        //System.out.println("Start decrypt");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] encryptedMessage = cipher.doFinal(message.getBytes());
        // Send the encrypted message to the server
        pw.println(Base64.getEncoder().encodeToString(encryptedMessage));
    }
    
    public void ispisIgraca() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
        for(ConnectFourClient client1:ConnectFourMyVer.clients){
            if(client1.isDostupan()==true){
                client1.cryptoWrite("Spisak igraca");
                for(ConnectFourClient client2:ConnectFourMyVer.clients){
                    if(client2.isDostupan()==true && !(client1.getUserName().equals(client2.getUserName()))){                    
                        client1.cryptoWrite(client2.getUserName());
                    }
                }
                client1.cryptoWrite("Done");
            }
        }
    }
    
    

    //Konstruktor klase, prima kao argument socket kao vezu sa uspostavljenim klijentom
    public ConnectFourClient(Socket socket, ArrayList<ConnectFourClient> allClients) throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.otherPlayerNumber = 0;
        this.socket = socket;
        this.allClients = allClients;

        try {
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();

            // Create object output and input streams
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
            this.pw = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()), true);
            this.username = "";
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");;
            // Send the secret key to the client
            //objectOutputStream.writeObject(secretKeySpec);
            pw.println(Base64.getEncoder().encodeToString(secretKeySpec.getEncoded()));
            
            byte[] ivBytes = new byte[cipher.getBlockSize()];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(ivBytes);
            ivParameterSpec = new IvParameterSpec(ivBytes);
            
            pw.println(Base64.getEncoder().encodeToString(ivBytes));
            
            /*this.br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
            this.pw = new PrintWriter(new OutputStreamWriter(this.socket.getOutputStream()), true);
            this.username = "";*/
            
        } catch (IOException ex) {
            Logger.getLogger(ConnectFourClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Metoda prolazi i pravi poruku sa trenutno povezanik korisnicima u formatu
     * Users: ImePrvog ImeDrugog ImeTreceg ... kada se napravi poruka tog
     * formata, ona se salje svim povezanim korisnicima
     */
    void connectedClientsUpdateStatus() {
        String connectedUsers = "Users:";
        for (ConnectFourClient c : this.allClients) {
            connectedUsers += " " + c.getUserName();
        }

        System.out.println(connectedUsers);
    }

    @Override
    
    public void run() {
        while (true) {
            
            try {             
                if (this.username.equals("")) {
                    System.out.println("Unos imena");
                    this.username = cryptoRead();
                    System.out.println(this.username);
                    if(username.equals("")){
                        System.out.println("LOGIN FAIL");
                        this.pw.println("FAIL");
                        break;
                    }
                    this.dostupan=true;
                    System.out.println(this.username + " povezan");
                    //this.pw.println(this.username);
                    ispisIgraca();
                    // ZAVRSENO POVEZIVANJE USPESNO                                 
                }               
                    // deo za pronalazak dva igraca
                while(this.dostupan==true)
                {    
                    String line;
                    try{
                         System.out.println("Pokusaj citanja na strani "+this.username);
                         line = this.cryptoRead();
                    }
                    catch (Exception e){
                         end=1;
                         break;                               
                    }
                     //System.out.println(line);
                     //System.out.println("stigla poruka");
                     // A salje ka B
                     /*if(line==null){
                         initializator=0;
                         break;                               
                     }*/
                     if(line.equals("Zahtev za drugog igraca")){
                         System.out.println("Trazi drugog igraca");
                         line = this.cryptoRead();
                         System.out.println("User "+line);
                         String username1 =line;
                         int i=0;

                         i=findPlayer(username1);

                         otherPlayerNumber=i;                               
                         System.out.println("Reached player no." + i);
                         ConnectFourMyVer.clients.get(i).cryptoWrite("Drugi igrac salje zahtev");
                         ConnectFourMyVer.clients.get(i).cryptoWrite(this.username);
                     }
                     //B odgovara A
                     if(line.equals("Odgovor na zahtev")){
                         System.out.println("Got answer to request");
                         line = this.cryptoRead();
                         if(line.equals("Prihvacen")){   
                             System.out.println("ACC");
                             prvi=0;
                             String user2 = this.cryptoRead();                          
                             System.out.println("ime igraca "+this.username);
                             System.out.println("Ime drugog igraca "+user2);
                             int pl2num=findPlayer(user2);
                             this.cryptoWrite("Pocetak igre");
                             ConnectFourMyVer.clients.get(pl2num).cryptoWrite("Pocetak igre");
                         }
                         else{
                             System.out.println("REJ");
                             String user2 = this.cryptoRead();
                             int pl2num=findPlayer(user2);
                             ConnectFourMyVer.clients.get(pl2num).cryptoWrite("Odbijen");
                         }
                     }
                     if(line.equals("Nije dostupan")){
                         System.out.println(this.username);
                         System.out.println("Nije dostupan");
                         this.dostupan=false;
                     }
                 }
                //disconnected
                if(end==1){
                    System.out.println("Disconnected user: " + this.username);
                    for (ConnectFourClient cl : this.allClients) {
                        if (cl.getUserName().equals(this.username)) {
                            this.allClients.remove(cl);
                            connectedClientsUpdateStatus();
                            try {
                                ispisIgraca();
                            } catch (InvalidKeyException ex1) {
                                Logger.getLogger(ConnectFourClient.class.getName()).log(Level.SEVERE, null, ex1);
                            } catch (IllegalBlockSizeException ex1) {
                                Logger.getLogger(ConnectFourClient.class.getName()).log(Level.SEVERE, null, ex1);
                            } catch (BadPaddingException ex1) {
                                Logger.getLogger(ConnectFourClient.class.getName()).log(Level.SEVERE, null, ex1);
                            } catch (InvalidAlgorithmParameterException ex1) {
                                Logger.getLogger(ConnectFourClient.class.getName()).log(Level.SEVERE, null, ex1);
                            }
                            this.socket.close();
                            return;
                        }
                    }
                }
                // PRELAZI U GAME MODE
                

                while(this.dostupan==false){                
                    if(prvi==1){
                        System.out.println("Reached start");
                        ConnectGame newGame = new ConnectGame();
                        int thisPlayer=findPlayer(this.username);
                        System.out.println("Broj drugog igraca "+otherPlayerNumber);
                        System.out.println("Broj prvog igraca "+thisPlayer);
                        //System.out.println("Broj p "+ConnectFourMyVer.clients.get(1).getUserName());
                        newGame.playMyGame(ConnectFourMyVer.clients.get(thisPlayer),ConnectFourMyVer.clients.get(otherPlayerNumber));
                        System.out.println("Ispis");
                        ispisIgraca();
                        System.out.println(ConnectFourMyVer.clients.get(otherPlayerNumber));
                        System.out.println(ConnectFourMyVer.clients.get(otherPlayerNumber).isDostupan());
                        System.out.println(ConnectFourMyVer.clients.get(thisPlayer));
                        System.out.println(ConnectFourMyVer.clients.get(thisPlayer).isDostupan());
                    }
                }
                    
                
            } catch (IOException ex) {
            } catch (InvalidKeyException ex) {
                Logger.getLogger(ConnectFourClient.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalBlockSizeException ex) {
                Logger.getLogger(ConnectFourClient.class.getName()).log(Level.SEVERE, null, ex);
            } catch (BadPaddingException ex) {
                Logger.getLogger(ConnectFourClient.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidAlgorithmParameterException ex) {
                Logger.getLogger(ConnectFourClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

