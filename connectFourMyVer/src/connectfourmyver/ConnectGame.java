/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package connectfourmyver;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

/**
 *
 * @author Aleksa
 */
public class ConnectGame {
    boolean gameOver=false;
    private char [][] board;
    
    public void playMyGame(ConnectFourClient client1,ConnectFourClient client2) throws IOException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
        
        //client1.cryptoWrite("The Game has started");
        //client2.cryptoWrite("The Game has started");
        board = new char[6][7];
        initializeBoard();
        System.out.println("ULAZ U IGRU");
        System.out.println(client1.getUserName());
        System.out.println(client2.getUserName());
        
        while(true){
            while(true){
                client1.cryptoWrite("Your turn");
                System.out.println("Ceka se na unos prvog igraca");
                String moveMes;        
                //prvi igrac unosi
                moveMes=client1.cryptoRead();
                while(!moveMes.equals("move")){
                    moveMes=client1.cryptoRead();
                }
                System.out.println("Prvi igrac uneo");

                int chosenColumn=Integer.parseInt(client1.cryptoRead());
                /*while(chosenColumn<0 || chosenColumn>6){
                    chosenColumn=Integer.parseInt(client1.cryptoRead());
                }*/
                makeMove(chosenColumn,0,client1,client2);
                if(gameOver==true)
                {
                    client1.cryptoWrite("Game over");
                    client1.cryptoWrite("Winner");
                    client2.cryptoWrite("Game over");
                    client2.cryptoWrite("Loser");
                    break;
                }

                //drugi igrac unosi
                client2.cryptoWrite("Your turn");
                moveMes=client2.cryptoRead();
                while(!moveMes.equals("move")){
                    moveMes=client2.cryptoRead();
                }

                chosenColumn=Integer.parseInt(client2.cryptoRead());
                /*while(chosenColumn<0 || chosenColumn>6){
                    chosenColumn=Integer.parseInt(client2.cryptoRead());
                }*/
                makeMove(chosenColumn,1,client1,client2);
                if(gameOver==true)
                {
                    client1.cryptoWrite("Game over");
                    client1.cryptoWrite("Loser");
                    client2.cryptoWrite("Game over");
                    client2.cryptoWrite("Winner");
                    break;
                }
            }
            String line1;
            String line2;
            
            InputStream is1 = client1.getSocket().getInputStream();
            InputStream is2 = client2.getSocket().getInputStream();
            byte[] inputData = new byte[1024];
            int rej=0;
            while(true){
                if(is1.available()!=0){
                    line1=client1.cryptoRead();
                    System.out.println(line1);
                    if(line1.equals("REJ")){
                        rej=1;
                        break;
                    }
                    else if(line1.equals("ACC")){
                        //client2.cryptoWrite("Break");
                        line2=client2.cryptoRead();
                        if(line2.equals("REJ"))
                        {
                            rej=1;
                        }
                        break;
                    }
                };
                if(is2.available()!=0){
                    line2=client2.cryptoRead();
                    System.out.println(line2);
                    if(line2.equals("REJ")){
                        rej=1;
                        break;
                    }
                    else if(line2.equals("ACC")){
                        //client1.cryptoWrite("Break");
                        line1=client1.cryptoRead();
                        if(line1.equals("REJ"))
                        {
                            rej=1;
                        }
                        break;
                    }
                }
            }
            System.out.println("Exited reading loop");
            //line1=client1.cryptoRead();
            //line2=client2.cryptoRead();
            if(rej==1){
                System.out.println("End of the game");
                client1.cryptoWrite("Stop");
                client2.cryptoWrite("Stop");
                client1.setDostupan(true);
                client2.setDostupan(true);
                client1.setPrvi(1);
                client2.setPrvi(1);
                break;
            }
            else{
                System.out.println("Reading both");
                System.out.println("Another game");
                initializeBoard();
                ConnectFourClient clTemp;
                clTemp = client1;
                client1=client2;
                client2=clTemp;
                client1.cryptoWrite("Cont");
                client2.cryptoWrite("Cont");
            }
            //client1.cryptoWrite("Cont");
            //client2.cryptoWrite("Cont");
        }
    }
    
    public boolean makeMove(int column, int playerNum,ConnectFourClient client1,ConnectFourClient client2) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException{
        //int chosenColumn = Integer.parseInt(column);
        int chosenColumn = column;
        
        char playerChar;
        if(playerNum==0)
        {
            playerChar='A';
        }
        else
        {
            playerChar='B';
        }
        
        for(int i = 0; i < 6; i++) {
            if(board[i][chosenColumn]=='0'){
                //ima mesta u koloni
                System.out.println("Red i kolona "+i+chosenColumn );
                board[i][chosenColumn]=playerChar;
                ispisIgre();
                gameOver=checkState();
                client1.cryptoWrite("Turn ends");
                client2.cryptoWrite("Turn ends");
                if(playerNum==0){
                    client1.cryptoWrite("A");
                    client2.cryptoWrite("A");
                }
                else{
                    client1.cryptoWrite("B");
                    client2.cryptoWrite("B");
                }
                client1.cryptoWrite(Integer.toString(i));
                client1.cryptoWrite(Integer.toString(chosenColumn));
                
                client2.cryptoWrite(Integer.toString(i));
                client2.cryptoWrite(Integer.toString(chosenColumn));
                return true;
            }
        }
        //nema mesta u koloni
        return false;
    }
    
    public void ispisIgre(){
        
        for (int i = 5; i >= 0; i--) {
            for (int j = 0; j < 7; j++) 
            {
                System.out.print(board[i][j] + " ");
            }
            System.out.println();
        }
    }
    
    public boolean checkState(){
        
        int cntA=0;
        int cntB=0;
        //prvo provera cetiri vertikalno
        for (int i = 0; i < 6; i++) {
            cntA=0;
            cntB=0;
            for (int j = 0; j < 7; j++) 
            {
                if(board[i][j]=='A')
                {
                    cntA++;
                    cntB=0;
                    if(cntA==4){
                        return true;
                    }
                }
                else if(board[i][j]=='B')
                {
                    cntA=0;
                    cntB++;
                    if(cntB==4){
                        return true;
                    }
                }
                else{
                    cntA=0;
                    cntB=0;
                    break;
                }
            }
        }
        
        //pa provera cetiri horizontalno
        for (int j = 0; j < 7; j++) {
            cntA=0;
            cntB=0;
            for (int i = 0; i < 6; i++) 
            {
                if(board[i][j]=='A')
                {
                    cntA++;
                    cntB=0;
                    if(cntA==4){
                        return true;
                    }
                }
                else if(board[i][j]=='B')
                {
                    cntA=0;
                    cntB++;
                    if(cntB==4){
                        return true;
                    }
                }
                else{
                    cntA=0;
                    cntB=0;
                    break;
                }
            }
        }
        
        // dijagonala  dole levo ka gore desno
        int minColumnNum[]={0,0,0};
        int maxColumnNum[]={4,4,4};
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) 
            {
                if(board[i][j]=='A' && board[i+1][j+1]=='A' && board[i+2][j+2]=='A' && board[i+3][j+3]=='A')
                {
                        return true;
                }
                else if(board[i][j]=='B' && board[i+1][j+1]=='B' && board[i+2][j+2]=='B' && board[i+3][j+3]=='B')
                {
                        return true;
                }
            }
        }
        
        // dijagonala  gore levo ka dole desno
        int minColumnNum2[]={0,0,0};
        int maxColumnNum2[]={4,4,4};
        
        for (int i = 5; i >= 3; i--) {
            for (int j = 0; j < 4; j++) 
            {
                if(board[i][j]=='A' && board[i-1][j+1]=='A' && board[i-2][j+2]=='A' && board[i-3][j+3]=='A')
                {
                        return true;
                }
                else if(board[i][j]=='B' && board[i-1][j+1]=='B' && board[i-2][j+2]=='B' && board[i-3][j+3]=='B')
                {
                        return true;
                }
            }
        }
        
        return false;
    }
    
    public void initializeBoard() {
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                board[row][col] = '0';
            }
        }
    }
    
}
