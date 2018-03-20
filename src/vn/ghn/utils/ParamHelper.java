/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import common.api.APIRequest;
import org.apache.log4j.Logger;

/**
 *
 * @author Bee
 */
public class ParamHelper {
    private static final Logger LOGGER = Logger.getLogger(ParamHelper.class);
    private static final Gson GSON = new Gson();
    private ParamHelper() {
    }
    
    public static ParamHelper getInstance(){
        return new ParamHelper();
    }
    
    public String getByName(String name, APIRequest req){
        try {
            if(req.getVar(name) != null) return req.getVar(name);
            if(req.getParams(name) != null) return req.getParams(name);
            if(req.getHeader(name) != null) return req.getHeader(name);
            String content = req.getContent();
            String r = null;
            if(content != null){
                JsonElement json = GSON.fromJson(content, JsonElement.class);
                if(json != null){
                    JsonObject jsonObj = json.getAsJsonObject();
                    if(jsonObj != null){
                        r = jsonObj.get(name).getAsString();
                    }
                }
            }
            return r;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        }
    }
}
