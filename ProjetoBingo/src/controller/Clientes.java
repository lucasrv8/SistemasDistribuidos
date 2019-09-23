package controller;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author lucas
 */
public class Clientes {
    private String NOME;
    private String IP;
    private String PORTA;
    /**
     * @return the NOME
     */
    public Clientes(){
        
    }
     
    
    public Clientes(String nome, String ip, String porta){
        this.NOME = nome;
        this.IP = ip;
        this.PORTA = porta;
    }
    public String getNOME() {
        return NOME;
    }

    /**
     * @param NOME the NOME to set
     */
    public void setNOME(String NOME) {
        this.NOME = NOME;
    }

    /**
     * @return the IP
     */
    public String getIP() {
        return IP;
    }

    /**
     * @param IP the IP to set
     */
    public void setIP(String IP) {
        this.IP = IP;
    }

    /**
     * @return the PORTA
     */
    public String getPORTA() {
        return PORTA;
    }

    /**
     * @param PORTA the PORTA to set
     */
    public void setPORTA(String PORTA) {
        this.PORTA = PORTA;
    }

    void setPORTA(int serverPort) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void printarMSG(String msg)
    {
        System.out.println(this.IP + " " + this.NOME);
    }
}
