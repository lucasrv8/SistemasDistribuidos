package controller;

import View.TelaServidor;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.text.DefaultCaret;


public class Conexao extends Thread {
    Socket clientSocket;
    public static ArrayList<Socket> listaSocket = new ArrayList<Socket>();
    public static ArrayList<Clientes> listaClientes = new ArrayList<Clientes>();
    public static ArrayList<Clientes> listaClientesProntos = new ArrayList<Clientes>();
    public static ArrayList<Cartela> listaCartelas = new ArrayList<Cartela>();
    
    public int numIndex = 0;
    public static ArrayList<Integer> numerosSorteio = new ArrayList<Integer>();
    
    //Vetores
    public static ArrayList<Integer> B = new ArrayList<Integer>(){
        {
        add(1);add(2);add(3);add(4);add(5);add(6);add(7);add(8);add(9);add(10);add(11);add(12);add(13);add(14);add(15);
        }
    };
    public static ArrayList<Integer> I = new ArrayList<Integer>(){
        {
        add(16);add(17);add(18);add(19);add(20);add(21);add(22);add(23);add(24);add(25);add(26);add(27);add(28);add(29);add(30);
        }
    };
    public static ArrayList<Integer> N = new ArrayList<Integer>(){
        {
        add(31);add(32);add(33);add(34);add(35);add(36);add(37);add(38);add(39);add(40);add(41);add(42);add(43);add(44);add(45);    
        }
    };
    public static ArrayList<Integer> G = new ArrayList<Integer>(){
        {
        add(46);add(47);add(48);add(49);add(50);add(51);add(52);add(53);add(54);add(55);add(56);add(57);add(58);add(59);add(60);
        }
    };
    public static ArrayList<Integer> O = new ArrayList<Integer>(){
        {
        add(61);add(62);add(63);add(64);add(65);add(66);add(67);add(68);add(69);add(70);add(71);add(72);add(73);add(74);add(75);
        }
    };
    
    OutputStream out;
    Writer outWriter;
    BufferedWriter bufferWriter;
    InputStream in;
    InputStreamReader inReader;
    BufferedReader bufferedReader;
    OutputStream outVariavel;
    Writer outWriterVariavel;
    BufferedWriter bufferWriterVariavel;
    TelaServidor ts;
    DefaultListModel model = new DefaultListModel();
    static Timer timer = null, timerCont = null, timerEnviar = new Timer();
    int tempo = 30, delay = 30000, interval = 30000;
    static int numeroSorteado;   // tempo de espera antes da 1ª execução da tarefa.
    static int jogoAtivo = 0;                                                   // intervalo no qual a tarefa será executada.;
    
    public Conexao (Socket aClientSocket, TelaServidor ts) {
        try {
            //Recebe a instância tela servidor
            this.ts = ts;
            
            //Recebe o socket do cliente
            clientSocket = aClientSocket;
            out = clientSocket.getOutputStream();
            outWriter = new OutputStreamWriter(out);
            bufferWriter = new BufferedWriter(outWriter);

            in = clientSocket.getInputStream();
            inReader = new InputStreamReader(in);
            bufferedReader = new BufferedReader(inReader);
            atualizaOnline();
            this.start();
        } catch(IOException e){ 
            System.out.println("Connection:"+e.getMessage());
        }
        
    }
    
    
    public void inicializarNumeros(){
        for(int i = 1; i < 76; i++){
            this.numerosSorteio.add(i);
        }
    }
    
    public void mexerNumeros(){
        Collections.shuffle(numerosSorteio);
    }
    
    public void inicializaVetores(){
        Collections.shuffle(B);
        Collections.shuffle(I);
        Collections.shuffle(N);
        Collections.shuffle(G);
        Collections.shuffle(O);
    }
    
    public void iniciaJogo() throws IOException{
        //Condição do timer
        if(this.timer != null && this.timerCont != null){
            this.timer.cancel();
            this.timerCont.cancel();
        }
        
        //Se tiver alguém pronto para jogar inicia o jogo
        if(this.listaClientesProntos.size() > 0){
            this.jogoAtivo = 1;
            this.inicializarNumeros();
            
            JSONMsg msg = new JSONMsg();
            Gson gson = new Gson();
            limpaGSON(msg);
            for (int i = 0; i < listaSocket.size(); i++) {
                for (int j = 0; j < listaClientesProntos.size(); j++) {
                    if(listaClientesProntos.get(j).getIP().equals(listaSocket.get(i).getInetAddress().toString().replace("/", "")) && 
                            listaClientesProntos.get(j).getPORTA().equals(Integer.toString(listaSocket.get(i).getPort()))){
                        msg.setCOD("cartela");
                        Cartela cartela = new Cartela();
                        cartela.setIp(clientSocket.getInetAddress().toString());
                        cartela.setPorta(Integer.toString(clientSocket.getPort()));
                        inicializaVetores();
                        for(int k = 0; k < 25; k++){
                            if(numIndex == 15){
                                numIndex = 0;
                            }
                            if(k >= 0 && k < 5){
                                cartela.cartela.add(B.get(numIndex));
                                numIndex++;
                            }
                            if(k > 4 && k < 10){
                                cartela.cartela.add(I.get(numIndex));
                                numIndex++;
                            }
                            if(k > 9 && k < 15){
                                if(k != 12){
                                    cartela.cartela.add(N.get(numIndex));
                                    numIndex++;
                                }else{
                                    cartela.cartela.add(0);
                                }
                            }
                            if(k > 14 && k < 20){
                                cartela.cartela.add(G.get(numIndex));
                                numIndex++;
                            }
                            if(k > 19 && k < 25){
                                cartela.cartela.add(O.get(numIndex));
                                numIndex++;
                            }
                        }
                         
                        cartela.setIp(listaClientesProntos.get(j).getIP());
                        cartela.setPorta(listaClientesProntos.get(j).getPORTA());
                        msg.CARTELA = cartela.cartela;
                        
                        //add ca cartela a lista de controle de cartelas
                        listaCartelas.add(cartela);
                        String jsonJogo = gson.toJson(msg);
                        this.ts.tEnviado.append("S. Enviado: " + jsonJogo + "\n");
                        this.ts.tEnviado.setCaretPosition(this.ts.tEnviado.getDocument().getLength());
                        outVariavel = listaSocket.get(i).getOutputStream();
                        outWriterVariavel = new OutputStreamWriter(outVariavel);
                        bufferWriterVariavel = new BufferedWriter(outWriterVariavel);
                        bufferWriterVariavel.write(jsonJogo + "\r\n");
                        bufferWriterVariavel.flush();
                        break;

                    }

                }
            }

            limpaGSON(msg);
            mexerNumeros();
            this.delay = 0;
            this.interval = 10000;
            tempoNumeros();
            contagemEnvio();
        }
        
    }
    
    public void enviaNumero() throws IOException{
        if(numerosSorteio.size() == 0 || this.listaClientesProntos.size() == 0){
            jogoAtivo = 0;
            timerEnviar.cancel();
        }else{
            Gson gson = new Gson();
            JSONMsg msg = new JSONMsg();

            limpaGSON(msg);
            msg.setCOD("sorteado");
            msg.CARTELA = new ArrayList<Integer>();
            msg.CARTELA.add(numerosSorteio.get(0));
            this.numeroSorteado = numerosSorteio.get(0);
            numerosSorteio.remove(0);
            String json = gson.toJson(msg);
            this.ts.tEnviado.append("S. Enviado: " + json + "\n");
            this.ts.tEnviado.setCaretPosition(this.ts.tEnviado.getDocument().getLength());

            for (int i = 0; i < listaSocket.size(); i++) {
                for (int j = 0; j < listaClientesProntos.size(); j++) {
                    if(listaClientesProntos.get(j).getIP().equals(listaSocket.get(i).getInetAddress().toString().replace("/", "")) && 
                            listaClientesProntos.get(j).getPORTA().equals(Integer.toString(listaSocket.get(i).getPort()))){

                        outVariavel = listaSocket.get(i).getOutputStream();
                        outWriterVariavel = new OutputStreamWriter(outVariavel);
                        bufferWriterVariavel = new BufferedWriter(outWriterVariavel);
                        bufferWriterVariavel.write(json + "\r\n");
                        bufferWriterVariavel.flush();
                        break;
                    }
                }
            }
        }   
    }
    
    public void tempoNumeros(){

        this.timerEnviar = new Timer();
        
        this.timerEnviar.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                try {
                    enviaNumero();
                } catch (IOException ex) {
                    System.out.println("Erro ao enviar número sorteado!");
                }
            }
          }, this.delay, this.interval);
    }
    
    public void tempo(){
        this.delay = 30000;
        this.interval = 30000;
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                try {
                    iniciaJogo();
                } catch (IOException ex) {
                    System.out.println("Erro ao inicializar jogo");
                }
            }
          }, this.delay, this.interval);
    }
    
    public void diminuiTimer(){
        this.ts.lTimer.setText(Integer.toString(this.tempo));
        if(this.tempo > 0){
            --this.tempo;
        }else{
            timerCont.cancel();
            this.ts.lTimer.setText("Iniciando Jogo...");
        }
    }
    
     public void contagem(){
        int delay = 0;   // tempo de espera antes da 1ª execução da tarefa.
        int interval = 1000;  // intervalo no qual a tarefa será executada.
        this.tempo = 30;
        this.timerCont = new Timer();
        this.timerCont.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                diminuiTimer();
            }
          }, delay, interval);
    }
     
    public void diminuiTimerEnvio(){
        this.ts.lTimer.setText(Integer.toString(this.tempo));
        if(this.tempo > 1){
            --this.tempo;
        }else{
            this.tempo = 10;
        }
    }
    
    public void contagemEnvio(){
        int delay = 0;   // tempo de espera antes da 1ª execução da tarefa.
        int interval = 1000;  // intervalo no qual a tarefa será executada.
        this.tempo = 10;
        this.timerCont = new Timer();
        this.timerCont.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                diminuiTimerEnvio();
            }
          }, delay, interval);
    }
     
    
    public void cancelaTempo(){
        this.tempo = 30;
        if(this.timer != null && this.timerCont != null){
            //System.out.println("entrei");
            this.timer.cancel();
            this.timerCont.cancel();
            this.ts.lTimer.setText("");
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
    
    public void atualizaOnline(){
        try{
            
            model.clear();
            ts.lOnline.setModel(this.model);

            for (int i = 0; i < this.listaClientes.size(); i++) {
                model.addElement(this.listaClientes.get(i).getNOME() + " (" + (this.listaClientes.get(i).getIP()) + ")" + " (" + 
                        this.listaClientes.get(i).getPORTA() + ")"); 
                this.ts.lOnline.setModel(this.model);
            }
            
            TimeUnit.MILLISECONDS.sleep(10);
            
        }catch(ArrayIndexOutOfBoundsException ex){
            System.out.println("");
        }catch (Exception e){
            System.out.println("");
        }
       
    }
    
    public void removerCliente(Gson gson, int flag){
            try{
            JSONMsg msgLogout = new JSONMsg();

            for (int i = 0; i < listaClientes.size(); i++) {
                if(listaClientes.get(i).getIP().equals(clientSocket.getInetAddress().toString().replace("/", "")) && 
                        listaClientes.get(i).getPORTA().equals(Integer.toString(clientSocket.getPort()))){

                    this.ts.tEnviado.append("S. Removendo: " + listaClientes.get(i).getNOME() + "\n");
                    this.ts.tEnviado.setCaretPosition(this.ts.tEnviado.getDocument().getLength());
                    listaClientes.remove(i);
                }
            }

            for (int i = 0; i < listaSocket.size(); i++) {
                 if(listaSocket.get(i).getInetAddress().toString().equals(clientSocket.getInetAddress().toString()) && 
                        Integer.toString(listaSocket.get(i).getPort()).equals(Integer.toString(clientSocket.getPort()))){

                    listaSocket.remove(i);
                    break;
                }
            }
            
            atualizaOnline();
            limpaGSON(msgLogout);
            String jsonBroadLogout;
            
            if(flag == 0){
                msgLogout.setCOD("rlogout");
                msgLogout.setSTATUS("sucesso");
                jsonBroadLogout = gson.toJson(msgLogout);
                this.ts.tEnviado.append("S. Enviado: " + jsonBroadLogout + "\n");
                this.ts.tEnviado.setCaretPosition(this.ts.tEnviado.getDocument().getLength());
                this.bufferWriter.write(jsonBroadLogout + "\r\n");
                this.bufferWriter.flush();
                this.out.close();
                this.in.close();
                this.clientSocket.close();
            }else{
                
                limpaGSON(msgLogout);
                msgLogout.LISTACLIENTE = new ArrayList<Clientes>();
                for (int i = 0; i < listaClientes.size(); i++) {
                    msgLogout.LISTACLIENTE.add(listaClientes.get(i));
                }

                
                msgLogout.setCOD("lista");
                jsonBroadLogout = gson.toJson(msgLogout);
                enviaLista(jsonBroadLogout);
                
                JSONMsg msgDesabilitar = new JSONMsg();
                int flagPronto = 0;
                for (int i = 0; i < listaClientesProntos.size(); i++) {

                    if(listaClientesProntos.get(i).getIP().equals(clientSocket.getInetAddress().toString().replace("/", "")) && 
                            listaClientesProntos.get(i).getPORTA().equals(Integer.toString(clientSocket.getPort()))){

                        this.ts.tEnviado.append("S. Removendo: " + listaClientesProntos.get(i).getNOME() + "\n");
                        this.ts.tEnviado.setCaretPosition(this.ts.tEnviado.getDocument().getLength());
                        listaClientesProntos.remove(i);
                        flagPronto = 1;
                        break;
                    }
                }

                limpaGSON(msgDesabilitar);

                msgDesabilitar.LISTACLIENTE = new ArrayList<Clientes>();

                for (int i = 0; i < listaClientesProntos.size(); i++) {
                    msgDesabilitar.LISTACLIENTE.add(listaClientesProntos.get(i));
                }

                msgDesabilitar.setCOD("listapronto");
                String json = gson.toJson(msgDesabilitar);
                enviaLista(json);
                
                limpaGSON(msgDesabilitar);
                if(flagPronto == 1){
                    
                    if(this.timer != null && this.timerCont != null && listaClientesProntos.size() > 0 && this.jogoAtivo == 0){
                        cancelaTempo();
                        msgDesabilitar.setCOD("tempo");
                        json = gson.toJson(msgDesabilitar);
                        enviaLista(json);

                        tempo();
                        contagem();
                    }
                    
                }
                
                if(listaClientesProntos.size() == 0){
                    jogoAtivo = 0;
                    
                    if(this.timer != null && this.timerCont != null){
                        
                        cancelaTempo();
                    }
                    if(this.timerEnviar != null){
                        this.timerEnviar.cancel();
                    }
                }
            }
        }catch(EOFException e){
            System.out.println("EOF:"+e.getMessage());
            this.ts.tRecebido.append("EOF:"+e.getMessage() + "\n");
            this.ts.tRecebido.setCaretPosition(this.ts.tRecebido.getDocument().getLength());
        } catch(IOException e){
            this.ts.tRecebido.append("S. Cliente Desconectado pelo logout. \n");
            this.ts.tRecebido.setCaretPosition(this.ts.tRecebido.getDocument().getLength());
        }catch(ArrayIndexOutOfBoundsException ex){
            System.out.println("");
        }catch (Exception e){
            System.out.println("");
        }
    }
    public synchronized void run(){
       
        Gson gson = new Gson();
        try {
            
            while(true){
                
                String data = bufferedReader.readLine();
                this.ts.tRecebido.append("S. recebido " + data + "\n");
                this.ts.tRecebido.setCaretPosition(this.ts.tRecebido.getDocument().getLength());
                JSONMsg msg = gson.fromJson(data, JSONMsg.class);
                
                if(msg == null || data == null){
                    
                    removerCliente(gson, 1);
                    break;
                    
                }
                
                if(msg.getCOD().equals("login")){
                    listaSocket.add(this.clientSocket);
                    String ip = clientSocket.getInetAddress().toString();
                    Clientes cliente;
                    cliente = new Clientes(msg.getNOME(), ip.replace("/", ""), Integer.toString(clientSocket.getPort()));
                    listaClientes.add(cliente);
                    
                    limpaGSON(msg);
                    msg.setCOD("rlogin");
                    msg.setSTATUS("sucesso");
                    String json = gson.toJson(msg);
                    this.ts.tEnviado.append("S. Enviado: " + json + "\n");
                    this.ts.tEnviado.setCaretPosition(this.ts.tEnviado.getDocument().getLength());
                    this.bufferWriter.write(json + "\r\n");
                    this.bufferWriter.flush();
                    
                    atualizaOnline();
                    limpaGSON(msg);
                    
                    msg.LISTACLIENTE = new ArrayList<Clientes>();
                    
                    for (int i = 0; i < listaClientes.size(); i++) {
                        msg.LISTACLIENTE.add(listaClientes.get(i));
                    }
                    
                    
                    msg.setCOD("lista");
                    json = gson.toJson(msg);
                    enviaLista(json);
                    
                    limpaGSON(msg);
                    msg.LISTACLIENTE = new ArrayList<Clientes>();

                    for (int i = 0; i < listaClientesProntos.size(); i++) {
                        msg.LISTACLIENTE.add(listaClientesProntos.get(i));
                    }

                    msg.setCOD("listapronto");
                    json = gson.toJson(msg);
                    enviaLista(json);
                    
                }else if(msg.getCOD().equals("logout")){
                    
                    removerCliente(gson, 0);
                    
                    
                }else if(msg.getCOD().equals("chat")){
                  
                    if(msg.getSTATUS().equals("uni")){
                        int indice = encontraDestinatario(msg);
                        if(indice != -1){
                            
                            outVariavel = listaSocket.get(indice).getOutputStream();
                            outWriterVariavel = new OutputStreamWriter(outVariavel);
                            bufferWriterVariavel = new BufferedWriter(outWriterVariavel);
                            msg.LISTACLIENTE.get(0).setIP(clientSocket.getInetAddress().toString().replace("/", ""));
                            msg.LISTACLIENTE.get(0).setNOME(msg.getNOME());
                            msg.LISTACLIENTE.get(0).setPORTA(Integer.toString(clientSocket.getPort()));
                            msg.setNOME(null);
                            String jsonUni = gson.toJson(msg);
                            this.ts.tEnviado.append("S. Enviado: " + jsonUni + "\n");
                            this.ts.tEnviado.setCaretPosition(this.ts.tEnviado.getDocument().getLength());
                            bufferWriterVariavel.write(jsonUni + "\r\n");
                            bufferWriterVariavel.flush();
                            
                            this.bufferWriter.write(jsonUni + "\r\n");
                            this.bufferWriter.flush();
                        }
                    }else if(msg.getSTATUS().equals("broad")){
                        String ip = clientSocket.getInetAddress().toString();
                        Clientes clienteMsg;
                        clienteMsg = new Clientes(msg.getNOME(), ip.replace("/", ""), Integer.toString(clientSocket.getPort()));
                        msg.setNOME(null);
                        msg.setSTATUS("broad");
                        msg.LISTACLIENTE = new ArrayList<Clientes>();
                        msg.LISTACLIENTE.add(clienteMsg);
                        String jsonBroad = gson.toJson(msg);
                        enviaLista(jsonBroad);
                    }else{
                        System.out.println("erro");
                    }
                    
                }else if(msg.getCOD().equals("pronto")){
                    
                    if(msg.getSTATUS().equals("sucesso")){
                        if(jogoAtivo == 0){
                            String ip = clientSocket.getInetAddress().toString();
                            Clientes cliente;
                            cliente = new Clientes(msg.getNOME(), ip.replace("/", ""), Integer.toString(clientSocket.getPort()));
                            listaClientesProntos.add(cliente);

                            limpaGSON(msg);
                            msg.setCOD("rpronto");
                            msg.setSTATUS("sucesso");

                            String json = gson.toJson(msg);
                            this.ts.tEnviado.append("S. Enviado: " + json + "\n");
                            this.ts.tEnviado.setCaretPosition(this.ts.tEnviado.getDocument().getLength());
                            this.bufferWriter.write(json + "\r\n");
                            this.bufferWriter.flush();

                            limpaGSON(msg);

                            msg.LISTACLIENTE = new ArrayList<Clientes>();

                            for (int i = 0; i < listaClientesProntos.size(); i++) {
                                msg.LISTACLIENTE.add(listaClientesProntos.get(i));
                            }

                            msg.setCOD("listapronto");
                            json = gson.toJson(msg);
                            enviaLista(json);

                            limpaGSON(msg);
                            msg.setCOD("tempo");
                            json = gson.toJson(msg);
                            enviaLista(json);

                            if(this.timer != null && this.timerCont != null){
                                cancelaTempo();
                            }
                            tempo();
                            contagem();
                        }else{
                            limpaGSON(msg);
                            msg.setCOD("rpronto");
                            msg.setSTATUS("falha");
                            msg.setMSG("Já há um jogo ativo! Aguarde...");
                            String json = gson.toJson(msg);
                            this.ts.tEnviado.append("S. Enviado: " + json + "\n");
                            this.ts.tEnviado.setCaretPosition(this.ts.tEnviado.getDocument().getLength());
                            this.bufferWriter.write(json + "\r\n");
                            this.bufferWriter.flush();

                        }
                        
                    }else if(msg.getSTATUS().equals("falha")){
                        JSONMsg msgDesabilitar = new JSONMsg();

                        for (int i = 0; i < listaClientesProntos.size(); i++) {
                            
                            if(listaClientesProntos.get(i).getIP().equals(clientSocket.getInetAddress().toString().replace("/", "")) && 
                                    listaClientesProntos.get(i).getPORTA().equals(Integer.toString(clientSocket.getPort()))){

                                this.ts.tEnviado.append("S. Removendo: " + listaClientesProntos.get(i).getNOME() + "\n");
                                this.ts.tEnviado.setCaretPosition(this.ts.tEnviado.getDocument().getLength());
                                listaClientesProntos.remove(i);
                            }
                        }
                        
                        msgDesabilitar.setCOD("rpronto");
                        msgDesabilitar.setSTATUS("falha");
                        msgDesabilitar.setMSG("Usuario Desabilitado do jogo.");
                        String json = gson.toJson(msgDesabilitar);
                        this.ts.tEnviado.append("S. Enviado: " + json + "\n");
                        this.ts.tEnviado.setCaretPosition(this.ts.tEnviado.getDocument().getLength());
                        this.bufferWriter.write(json + "\r\n");
                        this.bufferWriter.flush();
                        limpaGSON(msgDesabilitar);
                        
                        
                        msgDesabilitar.LISTACLIENTE = new ArrayList<Clientes>();
                    
                        for (int i = 0; i < listaClientesProntos.size(); i++) {
                            msgDesabilitar.LISTACLIENTE.add(listaClientesProntos.get(i));
                        }

                        msgDesabilitar.setCOD("listapronto");
                        json = gson.toJson(msgDesabilitar);
                        enviaLista(json);
                        
                        
                        if(this.timer != null && this.timerCont != null){
                            cancelaTempo();
                        }
                        
                        if(this.jogoAtivo == 0)
                        {
                            if(this.listaClientesProntos.size() > 0){
                                limpaGSON(msg);
                                msg.setCOD("tempo");
                                json = gson.toJson(msg);
                                enviaLista(json);
                                tempo();
                                contagem();
                            }
                        }
                    }
                }else if(msg.getCOD().equals("marca")){
                    
                    if(msg.getSTATUS().equals("sucesso")){
                        if(this.numeroSorteado == msg.CARTELA.get(0)){             
                            String ip = clientSocket.getInetAddress().toString().replace("/", "");
                            String port = Integer.toString(clientSocket.getPort());
                            Cartela ct = new Cartela();
                            for(int i = 0; i < this.listaCartelas.size(); i++){
                                if(ip.equals(this.listaCartelas.get(i).getIp()) &&  port.equals(this.listaCartelas.get(i).getPorta())){
                                    ct.cartela = this.listaCartelas.get(i).cartela;
                                }
                            }
                            for(int j = 0; j < ct.cartela.size(); j++){
                                if(ct.cartela.get(j) == msg.CARTELA.get(0)){
                                    ct.cartela.remove(j);
                                    this.ts.tNumeros.append("O número " + msg.CARTELA.get(0) + " foi marcado por: " + msg.getNOME() + "\n");
                                    this.ts.tNumeros.setCaretPosition(this.ts.tNumeros.getDocument().getLength());                                
                                }
                            } 
                        }else if(msg.getSTATUS().equals("falha")){
                            //System.out.println("Não usado mais");
                        }else{
                            //System.out.println(msg.CARTELA.get(0) + " Não é o número");
                        }
                    }
                }else if(msg.getCOD().equals("bingo")){
                    
                    String ip = clientSocket.getInetAddress().toString().replace("/", "");
                    String port = Integer.toString(clientSocket.getPort());
                    Cartela ct = new Cartela();
                    for(int i = 0; i < this.listaCartelas.size(); i++){
                        if(ip.equals(this.listaCartelas.get(i).getIp()) &&  port.equals(this.listaCartelas.get(i).getPorta())){
                            ct.cartela = this.listaCartelas.get(i).cartela;
                        }
                    }
                    if(ct.cartela.size() < 23 && msg.getNOME() != null){
                        Clientes clienteMsg;
                        clienteMsg = new Clientes(msg.getNOME(), ip.replace("/", ""), Integer.toString(clientSocket.getPort()));
                        limpaGSON(msg);
                        msg.setCOD("rbingo");
                        msg.setSTATUS("sucesso");
                        msg.LISTACLIENTE = new ArrayList<Clientes>();
                        msg.LISTACLIENTE.add(clienteMsg);
                        String json = gson.toJson(msg);
                        this.ts.tEnviado.append("S. Enviado: " + json + "\n");
                        this.ts.tEnviado.setCaretPosition(this.ts.tEnviado.getDocument().getLength());
                        enviaBingo(json);
                        if(timerEnviar != null){ this.timerEnviar.cancel();}
                        if(timer != null){ this.timer.cancel();}
                        if(timerCont != null){ this.timerCont.cancel();}
                        this.ts.lTimer.setText("");
                        this.jogoAtivo = 0;
                        this.delay = 30000;
                        this.interval = 30000;
                        
                        limpaGSON(msg);
                        this.listaClientesProntos.clear();
                        msg.setCOD("listapronto");
                        msg.LISTACLIENTE = new ArrayList<Clientes>();
                        json = gson.toJson(msg);
                        enviaLista(json);
                        
                    }else{
                        Clientes clienteMsg;
                        clienteMsg = new Clientes(msg.getNOME(), ip.replace("/", ""), Integer.toString(clientSocket.getPort()));
                        limpaGSON(msg);
                        msg.setCOD("rbingo");
                        msg.setSTATUS("falha");
                        msg.LISTACLIENTE = new ArrayList<Clientes>();
                        msg.LISTACLIENTE.add(clienteMsg);
                        String json = gson.toJson(msg);
                        this.ts.tEnviado.append("S. Enviado: " + json + "\n");
                        this.ts.tEnviado.setCaretPosition(this.ts.tEnviado.getDocument().getLength());
                        this.bufferWriter.write(json + "\r\n");
                        this.bufferWriter.flush();
                    }
                }
                else{
                    this.ts.tRecebido.append("Código Inválido \n");
                    this.ts.tRecebido.setCaretPosition(this.ts.tRecebido.getDocument().getLength());
                } 
            }
        } catch(EOFException e){
            System.out.println("EOF:"+e.getMessage());
            this.ts.tRecebido.append("EOF:"+e.getMessage() + "\n");
            this.ts.tRecebido.setCaretPosition(this.ts.tRecebido.getDocument().getLength());
        } catch(IOException e){
            removerCliente(gson, 1);
            this.ts.tRecebido.append("S. Cliente Desconectado. \n");
            this.ts.tRecebido.setCaretPosition(this.ts.tRecebido.getDocument().getLength());
            
        }catch(ArrayIndexOutOfBoundsException ex){
            System.out.println("");
        }catch (Exception e){
            System.out.println("");
        }
    }
    
    private void enviaLista(String json) throws IOException {
        try{
            this.ts.tEnviado.append("S. Broadcast Enviado: " + json + "\n");
            this.ts.tEnviado.setCaretPosition(this.ts.tEnviado.getDocument().getLength());
        
            for (int i = 0; i < listaSocket.size(); i++) {
                outVariavel = listaSocket.get(i).getOutputStream();
                outWriterVariavel = new OutputStreamWriter(outVariavel);
                bufferWriterVariavel = new BufferedWriter(outWriterVariavel);
                bufferWriterVariavel.write(json + "\r\n");
                bufferWriterVariavel.flush();
            }
        }catch(EOFException e){
            System.out.println("EOF:"+e.getMessage());
            this.ts.tRecebido.append("EOF:"+e.getMessage() + "\n");
            this.ts.tRecebido.setCaretPosition(this.ts.tRecebido.getDocument().getLength());
        } catch(IOException e){
            this.ts.tRecebido.append("IOException \n");
            this.ts.tRecebido.setCaretPosition(this.ts.tRecebido.getDocument().getLength());    
        }catch(ArrayIndexOutOfBoundsException ex){
            System.out.println("");
        }catch (Exception e){
            System.out.println("");
        }
    } 
    
    private void enviaBingo(String json) throws IOException {
        try{
            this.ts.tEnviado.append("S. Broadcast Enviado: " + json + "\n");
            this.ts.tEnviado.setCaretPosition(this.ts.tEnviado.getDocument().getLength());
        
            for (int i = 0; i < listaSocket.size(); i++) {
                for(int j = 0; j < listaClientesProntos.size(); j++){
                    if(listaClientesProntos.get(j).getIP().equals(listaSocket.get(i).getInetAddress().toString().replace("/", "")) && 
                            listaClientesProntos.get(j).getPORTA().equals(Integer.toString(listaSocket.get(i).getPort()))){
                        outVariavel = listaSocket.get(i).getOutputStream();
                        outWriterVariavel = new OutputStreamWriter(outVariavel);
                        bufferWriterVariavel = new BufferedWriter(outWriterVariavel);
                        bufferWriterVariavel.write(json + "\r\n");
                        bufferWriterVariavel.flush();
                        break;
                    }
                }
            }
        }catch(EOFException e){
            System.out.println("EOF:"+e.getMessage());
            this.ts.tRecebido.append("EOF:"+e.getMessage() + "\n");
            this.ts.tRecebido.setCaretPosition(this.ts.tRecebido.getDocument().getLength());
        } catch(IOException e){
            this.ts.tRecebido.append("IOException \n");
            this.ts.tRecebido.setCaretPosition(this.ts.tRecebido.getDocument().getLength());
        }catch (Exception e){
            System.out.println("");
        }
    }

    public int encontraDestinatario(JSONMsg msg) {
        for (int i = 0; i < listaSocket.size(); i++) {
            
            if(listaSocket.get(i).getInetAddress().toString().replace("/", "").equals(msg.LISTACLIENTE.get(0).getIP()) &&
                    Integer.toString(listaSocket.get(i).getPort()).equals(msg.LISTACLIENTE.get(0).getPORTA())){
                return i;
            }
        }
        return -1;
    }
}
