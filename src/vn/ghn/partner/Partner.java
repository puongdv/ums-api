/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.partner;

import com.google.gson.Gson;
import common.api.APIModel;

/**
 *
 * @author Bee
 */
public class Partner extends APIModel<String>{
    
    public String partnerCode;
    public String partnerName;
    public String description;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
