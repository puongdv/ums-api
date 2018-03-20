/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.account.common;

/**
 *
 * @author Bee
 */
public class MStatus {    
    public String status; 
    public String name;
    public String msg;

    public MStatus(String status, String name, String msg) {
        this.status = status;
        this.name = name;
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "MStatus{" + "status=" + status + ", name=" + name + ", msg=" + msg + '}';
    }        
}
