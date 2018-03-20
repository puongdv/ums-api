/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.warehouse;

import com.google.gson.Gson;
import common.api.APIModel;

/**
 *
 * @author Bee
 */
public class Warehouse extends APIModel<String>{  
    
    public Integer warehouseId;
    public String warehouseCode;
    public String warehouseName;    

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
       
}
