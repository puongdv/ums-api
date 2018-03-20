/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.role;

import com.google.gson.Gson;
import common.api.APIModel;
import java.util.List;

/**
 *
 * @author Bee
 */
public class Role extends APIModel<String>{
    
    public String name;
    public String description;
    public List<String> permissionList;
    
    @Override
    public String toString() {
        return new Gson().toJson(this);
    } 
}
