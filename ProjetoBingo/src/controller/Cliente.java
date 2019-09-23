package controller;

import View.Bingo;
import View.SalaEspera;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import com.google.gson.Gson;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

public class Cliente extends Thread{   
    
    private Socket ClientSocket = null;
    private Gson gson = new Gson();
    private JSONMsg msg = new JSONMsg();
    String nome;
    OutputStream out;
    Writer outWriter;
    BufferedWriter bufferWriter;
    InputStream in;
    InputStreamReader inReader;
    BufferedReader bufferedReader;
    SalaEspera salaEspera;
    Bingo bingo = null;
    public static Timer timer = null, timerCont = null;
    public static int tempo, tempoCont;
    public ArrayList<Clientes> listaClientes = new ArrayList<Clientes>();
    public ArrayList<Clientes> listaClientesProntos = new ArrayList<Clientes>();
    DefaultListModel model = null;
    DefaultListModel modelList = new DefaultListModel();
    public int numSorteado;

    public void diminuiTimerEnvio(){
        this.bingo.lTempo.setText(Integer.toString(this.tempoCont));
        
        if(this.tempoCont > 0){
            --this.tempoCont;
        }else{
            this.tempoCont = 10;
        }
    }
    
    
    public void contagemEnvio(){
        int delay = 0;   // tempo de espera antes da 1ª execução da tarefa.
        int interval = 1000;  // intervalo no qual a tarefa será executada.
        this.tempoCont = 10;
        this.timerCont = new Timer();
        this.timerCont.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                diminuiTimerEnvio();
            }
          }, delay, interval);
    }
    
    
    public void diminuiTimer(){
        this.salaEspera.lTempo.setFont(new java.awt.Font("Arial Black", 0, 36));
        this.salaEspera.lTempo.setText(Integer.toString(this.tempo));

        if(this.tempo > 0){
            --this.tempo;
        }else{
            this.salaEspera.lTempo.setFont(new java.awt.Font("Arial Black", 0, 12));
            this.salaEspera.lTempo.setText("Iniciando jogo...");
            this.timer.cancel();

        }
        
    }
    
    public void tempo(){
        int delay = 0;   // tempo de espera antes da 1ª execução da tarefa.
        int interval = 1000;  // intervalo no qual a tarefa será executada.
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                diminuiTimer();
            }
          }, delay, interval);
    }
    
    public void conectarCliente(String serverIP, int serverPort, String nome, SalaEspera salaEspera) throws IOException 
    {
        try {
            this.salaEspera = salaEspera;
            this.ClientSocket = new Socket(serverIP, serverPort);
            out = ClientSocket.getOutputStream();
            outWriter = new OutputStreamWriter(out);
            bufferWriter = new BufferedWriter(outWriter);

            in = ClientSocket.getInputStream();
            inReader = new InputStreamReader(in);
            bufferedReader = new BufferedReader(inReader);
            
            this.msg.setNOME(nome);
            this.nome = nome; 
            this.msg.setCOD("login");
            String json = this.gson.toJson(msg);
            System.out.println("C. Enviado: " + json);
            this.bufferWriter.write(json + "\r\n");
            this.bufferWriter.flush();
            this.salaEspera.tMensagem.append("BEM VINDO A SALA DE CHAT " + this.nome.toUpperCase() + "\n");
            this.salaEspera.tMensagem.setCaretPosition(this.salaEspera.tMensagem.getDocument().getLength());
            this.start();
            
        } catch (UnknownHostException e) {
            System.err.println("Host desconhecido: ");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("IP ou Porta não existe ");
            System.exit(1);
        } catch (Exception e) {
            System.out.println("Falha na conexão com o servidor");
        }
    }
    
    
    
    public void limpaGSON(JSONMsg msg)
    {
        msg.setCOD(null);
        msg.setMSG(null);
        msg.setNOME(null);
        msg.setSTATUS(null);
        msg.CARTELA = null;
        msg.LISTACLIENTE = null;
    }
    
    public void gritarBingo(){
        try{
            this.limpaGSON(msg);
            this.msg.setCOD("bingo");
            this.msg.setNOME(this.nome);
            String jsonBingo = this.gson.toJson(msg);
            System.out.println("C. Enviado: " + jsonBingo);
            this.bufferWriter.write(jsonBingo + "\r\n");
            this.bufferWriter.flush();
        }catch(IOException ex){
            System.err.println("Erro ao gritar bingo");
        }
    }
    
    public boolean marcarNumero(int numero){
        try{
            if(numero == this.numSorteado){
                this.limpaGSON(msg);

                this.msg.setCOD("marca");
                this.msg.setNOME(this.nome);
                this.msg.setSTATUS("sucesso");
                this.msg.CARTELA = new ArrayList<Integer>();
                this.msg.CARTELA.add(numero);
                String json = this.gson.toJson(msg);
                System.out.println("C. Enviado: " + json);
                this.bufferWriter.write(json + "\r\n");
                this.bufferWriter.flush();
                return false;
            }else{
                System.out.println("Esse não é o número");
                return true;
            }
            
        }catch(IOException ex){
            System.err.println("Erro na habilitação do jogo");
            return true;
        }
    }
    public void habilitarJogo() throws IOException{
        try{
            this.limpaGSON(msg);
            this.msg.setCOD("pronto");
            this.msg.setNOME(this.nome);
            this.msg.setMSG(null);
            this.msg.setSTATUS("sucesso");
            String json = this.gson.toJson(msg);
            System.out.println("C. Enviado: " + json);
            this.bufferWriter.write(json + "\r\n");
            this.bufferWriter.flush();
            
        }catch(IOException ex){
            System.err.println("Erro na habilitação do jogo");
        }
    }
    
    public void enviarChat(String msgText) throws IOException{
        try{
            this.limpaGSON(msg);
            this.msg.setCOD("chat");
            this.msg.setNOME(this.nome);
            this.msg.setMSG(msgText);
            
            int selecionado = salaEspera.lOnline.getSelectedIndex();
            if (selecionado == -1) {
                this.msg.setSTATUS("broad");
                String json = this.gson.toJson(msg);
                System.out.println("C. Enviado: " + json);
                this.bufferWriter.write(json + "\r\n");
                this.bufferWriter.flush();
                
            } else {
                this.msg.setSTATUS("uni");
                Clientes clienteUni;
                clienteUni = this.listaClientes.get(selecionado);
                msg.LISTACLIENTE = new ArrayList<Clientes>();
                msg.LISTACLIENTE.add(clienteUni);
                
                String json = this.gson.toJson(msg);
                System.out.println("C. Enviado: " + json);
                this.bufferWriter.write(json + "\r\n");
                this.bufferWriter.flush();
                salaEspera.lOnline.clearSelection();
            }
            
        } catch (IOException e) {
            System.err.println("Erro no chat");
        }
    }
    
     public void desabilitarJogo() throws IOException{
        try{
            this.limpaGSON(msg);
        
            this.msg.setCOD("pronto");
            this.msg.setNOME(this.nome);
            this.msg.setMSG(null);
            this.msg.setSTATUS("falha");
            String json = this.gson.toJson(msg);
            System.out.println("C. Enviado: " + json);
            this.bufferWriter.write(json + "\r\n");
            this.bufferWriter.flush();
            
            
        } catch (IOException e) {
            System.err.println("Erro ao desabilitar do jogo");
            System.exit(1);
        }
    }
     
    public void logout() throws IOException{
        try{
            limpaGSON(msg);
            this.msg.setCOD("logout");
            this.msg.setNOME(this.nome);
            String json = this.gson.toJson(msg);
            System.out.println("C. Enviado: " + json);
            this.bufferWriter.write(json + "\r\n");
            this.bufferWriter.flush();
            
        } catch (IOException e) {
            System.err.println("Erro ao fazer logout");
            System.exit(1);
        }
    }
    
    public synchronized void run(){
        try {    
            while (true) {
                if(bufferedReader.ready()){
                    String received = bufferedReader.readLine();
                    System.out.println("C. Recebido: " + received);
                    JSONMsg msgServidor = gson.fromJson(received, JSONMsg.class);
                    
                    if(msgServidor.getCOD().equals("rlogin"))
                    {
                        if(msgServidor.getSTATUS().equals("true") || msgServidor.getSTATUS().equals("sucesso"))
                        {
                            System.out.println("Logado com sucesso!");
                            
                        }else{
                            System.out.println("Não foi possível logar.");
                        }    
                    }else if(msgServidor.getCOD().equals("rlogout")){

                        if(msgServidor.getSTATUS().equals("true") || msgServidor.getSTATUS().equals("sucesso"))
                        {
                            System.out.println("Logout realizado com sucesso!");
                            break;
                        }else{
                            System.out.println("Não foi possível realizar logout.");
                        }    

                    }else if(msgServidor.getCOD().equals("lista")){
                        try{
                            
                            System.out.println("Lista de usuários atualizada.");
                            for(int i = 0; i < msgServidor.LISTACLIENTE.size(); i++)
                            {
                                System.out.println("Cliente: " + msgServidor.LISTACLIENTE.get(i).getNOME() + " está conectado.");
                            }

                            this.listaClientes = msgServidor.LISTACLIENTE;
                            try{
                                
                                atualizaUsuarios();
                            }catch(ArrayIndexOutOfBoundsException ex){
                                System.out.println("--------------");
                            }

                        }catch (Exception e){
                            System.out.println("/");
                        }
                    }else if(msgServidor.getCOD().equals("listapronto")){
                        try{
                       
                            System.out.println("Lista de usuários PRONTOS atualizada.");

                            for(int i = 0; i < msgServidor.LISTACLIENTE.size(); i++)
                            {
                                System.out.println("Cliente: " + msgServidor.LISTACLIENTE.get(i).getNOME() + " está pronto.");
                            }

                            this.listaClientesProntos = msgServidor.LISTACLIENTE;

                            if(this.listaClientesProntos != null && this.listaClientesProntos.size() == 0 && this.timer != null){
                                this.timer.cancel();
                                this.salaEspera.lTempo.setText("");
                            }
                            try{
                                atualizaUsuarios();
                            }catch(ArrayIndexOutOfBoundsException ex){
                                System.out.println("--------------");
                            }
                            
                        }catch (Exception e){
                            System.out.println("/");
                        }
                    }else if(msgServidor.getCOD().equals("chat")){
                        
                        if(msgServidor.getSTATUS().equals("broad")){
                            this.salaEspera.tMensagem.append(msgServidor.LISTACLIENTE.get(0).getNOME() + "→ " + msgServidor.getMSG() + "\n");
                            this.salaEspera.tMensagem.setCaretPosition(this.salaEspera.tMensagem.getDocument().getLength());
                        
                        }else if(msgServidor.getSTATUS().equals("uni")){
                            this.salaEspera.tMensagem.append("(PRIVATE) " + msgServidor.LISTACLIENTE.get(0).getNOME() + " → " + msgServidor.getMSG() + "\n");
                            this.salaEspera.tMensagem.setCaretPosition(this.salaEspera.tMensagem.getDocument().getLength());
                            
                        }else{
                            System.out.println("Erro chat");
                        }
                    
                    }else if(msgServidor.getCOD().equals("rpronto")){
                        
                        if(msgServidor.getSTATUS().equals("true") || msgServidor.getSTATUS().equals("sucesso"))
                        {
                            System.out.println("Habilitado com sucesso!");
                            this.salaEspera.btnJogar.setVisible(false);
                            this.salaEspera.btnSair.setVisible(true);
                        }else if(msgServidor.getSTATUS().equals("falha")){
                            if(timerCont != null){
                                timerCont.cancel();
                            }
                            this.salaEspera.lTempo.setFont(new java.awt.Font("Arial Black", 0, 12));
                            this.salaEspera.lTempo.setText(msgServidor.getMSG());
                            this.salaEspera.btnJogar.setVisible(true);
                            
                            this.salaEspera.btnSair.setVisible(false);
                        }    
                        
                    }else if(msgServidor.getCOD().equals("tempo")){
                        this.tempo = 30;
                        if(this.timer != null){
                            this.timer.cancel();
                            this.salaEspera.lTempo.setText("");
                        }
                        tempo();
                    }else if(msgServidor.getCOD().equals("cartela")){
                        bingo = new Bingo(this.salaEspera.cliente);
                        bingo.bB1.setText(Integer.toString(msgServidor.CARTELA.get(0)));
                        bingo.bB2.setText(Integer.toString(msgServidor.CARTELA.get(1)));
                        bingo.bB3.setText(Integer.toString(msgServidor.CARTELA.get(2)));
                        bingo.bB4.setText(Integer.toString(msgServidor.CARTELA.get(3)));
                        bingo.bB5.setText(Integer.toString(msgServidor.CARTELA.get(4)));
                        bingo.bI1.setText(Integer.toString(msgServidor.CARTELA.get(5)));
                        bingo.bI2.setText(Integer.toString(msgServidor.CARTELA.get(6)));
                        bingo.bI3.setText(Integer.toString(msgServidor.CARTELA.get(7)));
                        bingo.bI4.setText(Integer.toString(msgServidor.CARTELA.get(8)));
                        bingo.bI5.setText(Integer.toString(msgServidor.CARTELA.get(9)));
                        bingo.bN1.setText(Integer.toString(msgServidor.CARTELA.get(10)));
                        bingo.bN2.setText(Integer.toString(msgServidor.CARTELA.get(11)));
                        //bingo.bN3.setText(Integer.toString(msgServidor.CARTELA.get(12)));
                        bingo.bN4.setText(Integer.toString(msgServidor.CARTELA.get(13)));
                        bingo.bN5.setText(Integer.toString(msgServidor.CARTELA.get(14)));
                        bingo.bG1.setText(Integer.toString(msgServidor.CARTELA.get(15)));
                        bingo.bG2.setText(Integer.toString(msgServidor.CARTELA.get(16)));
                        bingo.bG3.setText(Integer.toString(msgServidor.CARTELA.get(17)));
                        bingo.bG4.setText(Integer.toString(msgServidor.CARTELA.get(18)));
                        bingo.bG5.setText(Integer.toString(msgServidor.CARTELA.get(19)));
                        bingo.bO1.setText(Integer.toString(msgServidor.CARTELA.get(20)));
                        bingo.bO2.setText(Integer.toString(msgServidor.CARTELA.get(21)));
                        bingo.bO3.setText(Integer.toString(msgServidor.CARTELA.get(22)));
                        bingo.bO4.setText(Integer.toString(msgServidor.CARTELA.get(23)));
                        bingo.bO5.setText(Integer.toString(msgServidor.CARTELA.get(24)));
                        bingo.setVisible(true);
                        this.salaEspera.btnSair.setVisible(false);
                        
                    }else if(msgServidor.getCOD().equals("sorteado")){
                        this.numSorteado = msgServidor.CARTELA.get(0);
                        bingo.lNumero.setText(Integer.toString(msgServidor.CARTELA.get(0)));
                        
                        if(timerCont != null){
                            timerCont.cancel();
                        }
                        contagemEnvio();
                    
                    }else if(msgServidor.getCOD().equals("rbingo")){
                        
                        if(msgServidor.getSTATUS().equals("sucesso")){
                            this.timerCont.cancel();
                            JOptionPane.showMessageDialog(null, "Hoje sim, para a equipe: " + msgServidor.LISTACLIENTE.get(0).getNOME() + "! Parabéns ganhou!", "Jogo finalizado", JOptionPane.INFORMATION_MESSAGE);
                            this.salaEspera.btnJogar.setVisible(true);
                            this.salaEspera.btnJogar.setEnabled(true);
                            this.salaEspera.lTempo.setText("");
                            this.bingo.dispose();
                        }else if(msgServidor.getSTATUS().equals("falha")){
                            JOptionPane.showMessageDialog(null, "Hoje não, " + msgServidor.LISTACLIENTE.get(0).getNOME(), "Que coisa feia...", JOptionPane.INFORMATION_MESSAGE);
                            
                        }
                    }
                    else{
                        System.out.println("Código inválido.");
                    }
                }
            }
            this.in.close();
            this.out.close();
            this.ClientSocket.close();
            
        }catch (IOException ex) {
            System.out.println("Erro:" + ex);
        }catch(ArrayIndexOutOfBoundsException ex){
            System.out.println("--------------");
        }catch (Exception e){
            System.out.println("Erro:" + e);
        }
              
    }
    
    public int verificaUsuarioLista(Clientes c){
        if(this.listaClientes != null && this.listaClientes.size() > 0){
            Iterator<Clientes> itClientes = listaClientes.iterator();
            while(itClientes.hasNext()){
                Clientes cliente = itClientes.next();
                if(cliente.getIP().equals(c.getIP()) && cliente.getPORTA().equals(c.getPORTA())){
                    return 1;
                }
            }
            
        }
        return 0;
    }
    
    public int verificaUsuarioListaPronto(Clientes c){
        
        if(this.listaClientesProntos != null && this.listaClientesProntos.size() > 0){
            
            Iterator<Clientes> it = listaClientesProntos.iterator();
            
            while (it.hasNext()) { 
                Clientes cp = it.next();
                    
                if(c.getIP().equals(cp.getIP()) && c.getPORTA().equals(cp.getPORTA())){
                    return 1;
                }
            }
        }
        return 0;
    }
    
    public synchronized void atualizaUsuarios(){
        try{
            model = new DefaultListModel();
            this.model.clear();
            salaEspera.lOnline.setModel(this.model);
            int estaNaLista = 0;
            Iterator<Clientes> it = listaClientes.iterator(); 
            while (it.hasNext()) { 
                Clientes c = it.next();
                estaNaLista = verificaUsuarioListaPronto(c);
                if(estaNaLista == 1)
                {
                    model.addElement(c.getNOME() + " (" + (c.getIP()) + ") (" + 
                    c.getPORTA() + ")  ✔ READY"); 
                    this.salaEspera.lOnline.setModel(model);
                }else{
                    int online = verificaUsuarioLista(c);
                    if(online == 1){
                        model.addElement(c.getNOME() + " (" + (c.getIP()) + ") (" + 
                        c.getPORTA() + ")"); 
                        this.salaEspera.lOnline.setModel(model);
                    }else{
                        System.out.println("não está na lista");
                    }
                }
                
                TimeUnit.MILLISECONDS.sleep(10);
            }
        }catch(ArrayIndexOutOfBoundsException ex){
            System.out.println("");
        }catch (Exception e){
            System.out.println("");
        }
               
    }

}
