/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.user;

import com.google.gson.Gson;
import common.api.APIRequest;
import common.api.APIResource;
import common.api.APIResponder;
import common.api.APIResponse;
import common.api.APIStatus;
import common.api.annotation.APIMethod;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import vn.common.lib.utils.NNumberUtils;
import vn.common.lib.utils.NUtils;

/**
 *
 * @author Bee
 */
public class UserAction extends APIResource{
    private static final Logger LOGGER = Logger.getLogger(UserAction.class);
    private static final Gson GSON = new Gson();

    public UserAction(String path) {
        super(path);
    }
  
    @APIMethod(name = "GET")
    public void onQuery(APIRequest req, APIResponder resp){
        try {
            // get query information
            String query = req.getParams("q");
            User userQuery;
            if (query != null) {
                try {
                    userQuery = GSON.fromJson(query, User.class);
                    
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                    resp.respond(new APIResponse(APIStatus.INVALID, "Invalid query. Please put in JSON format."));
                    return;
                }
            } else {
                userQuery = new User();
            }
            // pagination options
            long offset = NNumberUtils.getLong(req.getParams("offset"), 0);
            long limit = NNumberUtils.getLong(req.getParams("limit"), 20);
            if(limit > 1024) limit = 1024;
            boolean reverse = req.getParams("reverse") != null;
                         
            APIResponse<User> apiResult = UserDBService.getInstance().query(userQuery, offset, limit, reverse);
            LOGGER.info(GSON.toJson(apiResult));
            resp.respond(apiResult);            
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, ex.getMessage()));
        }
    }
    
    @APIMethod(name = "POST")
    public void onPost(APIRequest req, APIResponder resp){
        try {
            // get driver information
            String content = req.getContent();
            User user = null;
            if (content != null) {
                try {
                    user = GSON.fromJson(content, User.class);
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            } 
            
            // reject if wrong format
            if (user == null){
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid data. Please put in JSON format."));
                return;
            }
            //user.email 
            APIResponse<User> result = UserClient.getByEmail(user.email);
            if(result == null){
                resp.respond(new APIResponse(APIStatus.ERROR, "Management system is busy, please try again later."));
                return;
            }
            
            if(result.status == APIStatus.OK){
                resp.respond(new APIResponse(APIStatus.EXISTED, "Data existed, ["+ user.email +"] is existed."));
                return;
            }
            
            user.password = NUtils.md5(user.password + user.email);
            // query to DB
            APIResponse<User> apiResult = UserClient.create(user);
            if(apiResult == null){
                resp.respond(new APIResponse(APIStatus.ERROR, "Management system is busy, please try again later."));
                return;
            }                 
                                             
            resp.respond(apiResult);
            
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, ex.getMessage()));
        }
    }
}
