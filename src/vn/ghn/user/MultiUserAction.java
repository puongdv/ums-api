/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.user;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.BasicDBObject;
import common.api.APIRequest;
import common.api.APIResource;
import common.api.APIResponder;
import common.api.APIResponse;
import common.api.APIStatus;
import common.api.annotation.APIMethod;
import java.lang.reflect.Type;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Bee
 */
public class MultiUserAction extends APIResource{
    
    private static final Logger LOGGER = Logger.getLogger(MultiUserAction.class);
    private static final Gson GSON = new Gson();
    
    public MultiUserAction(String path) {
        super(path);
    }
    
    @APIMethod(name = "GET")
    public void onGet(APIRequest req, APIResponder resp){
        try {
            // parse information from request
            String ids = req.getParams("ids");
            if (ids == null || ids.isEmpty()) {
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid id."));
                return;
            }
            String[] ssoIds = ids.split(",");       
            APIResponse<BasicDBObject> apiResult = UserClient.getUserBySSOID(ssoIds);
            resp.respond(apiResult);
            
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "An error occurred, please try again later."));
        }
    }    
    
    @APIMethod(name = "POST")
    public void onPost(APIRequest req, APIResponder resp){
        try {
            // parse information from request
            String content = req.getContent();
            if (content == null || content.isEmpty()) {
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid data. Please put in JSON format"));
                return;
            }
            
            Type type = new TypeToken<List<User>>() {}.getType();
            List<User> users = GSON.fromJson(content, type);
            
            if (users == null){
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid data. Please put in JSON format."));
                return;
            }
            
            String roleId = req.getParams("roleId");
            if(roleId == null){
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid data. Required {roleId}."));
                return;
            }
            APIResponse<User> apiResult = UserClient.pushUser(users, roleId);
            resp.respond(apiResult);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "An error occurred, please try again later."));
        }
    }
    
}