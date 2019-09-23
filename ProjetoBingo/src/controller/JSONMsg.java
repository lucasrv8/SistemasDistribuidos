package controller;


import java.util.ArrayList;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author lucas
 */
public class JSONMsg {

    private String COD = null;
    private String NOME = null;
    private String MSG = null;
    private String STATUS = null;
    public ArrayList<Integer> CARTELA = null;
    public ArrayList<Clientes> LISTACLIENTE = null;
    
   
    /**
     * @return the STATUS
     */
    public String getSTATUS() {
        return STATUS;
    }

    /**
     * @param STATUS the STATUS to set
     */
    public void setSTATUS(String STATUS) {
        this.STATUS = STATUS;
    }
    /**
     * @return the COD
     */
    public String getCOD() {
        return COD;
    }

    /**
     * @param COD the COD to set
     */
    public void setCOD(String COD) {
        this.COD = COD;
    }

    /**
     * @return the NOME
     */
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
     * @return the MSG
     */
    public String getMSG() {
        return MSG;
    }

    /**
     * @param MSG the MSG to set
     */
    public void setMSG(String MSG) {
        this.MSG = MSG;
    }

    /**
     * @return the CARTELA
     */
  
    
}
