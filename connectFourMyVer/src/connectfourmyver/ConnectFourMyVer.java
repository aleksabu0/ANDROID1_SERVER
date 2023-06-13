package connectfourmyver;

//server prati sve prikljucene igrace
//igraci salju ip i port da bi nasli server
//nakon sto je povezan sa serverom, igrac salje useraname
//server salje igracu spisak sa svim dostupnim igracima
//igrac salje serveru zahtev da pocne igru sa drugim igracem, server obavestava drugog igraca i pita ga da li zeli da igra
//ako prihvati, igraci pocinju igru i nedostupni su, u suprotnom salje poruku prvom igracu da je drugi odbio
//server kontrolise igru i prati svaki potez, prvi igra prvi igrac
//server apdejtuje stanje i salje ga igracima nakon poteza
//pokusaj poteza za vreme poteza drugog igraca izaziva poruku upozerenja 
//poruka nakon kraja igre, mogu igrati ponovo, tada je obrnut red igranja
//ako ne igraju ponovo, vraca ih medju dostupne igrace

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.Integer.parseInt;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;



public class ConnectFourMyVer {
    private static final String SECRET_KEY = "abcdefghijklmnop";
    private ServerSocket ssocket;
    private int port;
    public static SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");;

    public ServerSocket getSsocket() {
        return ssocket;
    }

    public void setSsocket(ServerSocket ssocket) {
        this.ssocket = ssocket;
    }
    
    public static ArrayList<ConnectFourClient> clients;

    

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Prihvata u petlji klijente i za svakog novog klijenta kreira novu nit. Iz
     * petlje se moze izaci tako sto se na tastaturi otkuca Exit.
     */
    public void acceptClients() throws NoSuchAlgorithmException, NoSuchPaddingException {
        Socket client = null;
        Thread thr;
        while (true) {
            try {
                System.out.println("Waiting for new clients..");
                client = this.ssocket.accept();
            } catch (IOException ex) {
                Logger.getLogger(ConnectFourMyVer.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (client != null) {
                //Povezao se novi klijent, kreiraj objekat klase ConnectedChatRoomClient
                //koji ce biti zaduzen za komunikaciju sa njim
                ConnectFourClient clnt = new ConnectFourClient(client, clients);
                //i dodaj ga na listu povezanih klijenata jer ce ti trebati kasnije
                clients.add(clnt);
                //kreiraj novu nit (konstruktoru prosledi klasu koja implementira Runnable interfejs)
                thr = new Thread(clnt);
                //..i startuj ga
                thr.start();
            } else {
                break;
            }
        }
    }

    public ConnectFourMyVer(int port) {
        this.clients = new ArrayList<>();
        try {
            this.port = port;
            this.ssocket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(ConnectFourMyVer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    

    public static void main(String[] args) throws FileNotFoundException, IOException, NoSuchAlgorithmException, NoSuchPaddingException {

        //ConnectFourClient cl1 = new ConnectFourClient();
        //System.out.println(sviPredmeti.get(0).getImePredmeta());
        //System.out.println(studenti.get(0).getPredmeti().get(1).getBodovi()[2]);
        // KRAJ UCITAVANJA KORISNIKA
        ConnectFourMyVer server = new ConnectFourMyVer(8008);

        System.out.println("Server pokrenut, slusam na portu 8008");
        //SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        System.out.println(InetAddress.getLocalHost());
        //Prihvataj klijente u beskonacnoj petlji
        server.acceptClients();

        

    }

}
