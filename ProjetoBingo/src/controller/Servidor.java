package controller;

import View.TelaServidor;
import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Servidor extends Thread
{
    int serverPort;
    Socket clientSocket = null;    
    TelaServidor ts;
    
    public Servidor(int serverPort, TelaServidor ts){
        this.serverPort = serverPort;
        this.ts = ts;
        this.start();
    }
    
    @Override
    public void run ()
    {
        ServerSocket server = null;   
    
      
        try
        {
            server = new ServerSocket(this.serverPort);
            //this.ts.tRecebido.append("############################## Bem vindo ao Servidor ##############################\n");
        } catch (IOException e){
            System.out.println(e);
        }
        
        try {
            while (true)
            {
                //System.out.println("Aguardando conexao");
                this.ts.tRecebido.append("Aguardando Conexão\n");
                this.ts.tRecebido.setCaretPosition(this.ts.tRecebido.getDocument().getLength());

                 this.clientSocket = server.accept();

                //System.out.println("Conectado");
                this.ts.tRecebido.append("Conectado\n");
                this.ts.tRecebido.setCaretPosition(this.ts.tRecebido.getDocument().getLength());
                Conexao c = new Conexao(this.clientSocket, this.ts);

            }
            
        } catch (IOException ex) {
            //Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("IO" + ex);
        }
   }
 
   
        
}
