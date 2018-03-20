/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.auth;

import com.google.gson.Gson;
import common.api.APIResponse;
import org.apache.log4j.Logger;
import vn.ghn.user.User;
import vn.ghn.user.UserClient;
import vn.ghn.user.UserDBService;

/**
 *
 * @author Bee
 */
public class FBAuth {
    private static final Logger LOGGER = Logger.getLogger(SSOAuth.class); 
    private static final Gson GSON = new Gson();
    
    public static FBAuth getInstance() {
        return FBAuth.FBAuthHolder.INSTANCE;
    }
     
    private static class FBAuthHolder {
        private static final FBAuth INSTANCE = new FBAuth();
    }
    
    public APIResponse<User> auth(String facebookID, String email){
        try {            
            if(email != null && !email.isEmpty()){
                return UserClient.getByEmail(email);                
            }
            return UserClient.getByFacebookID(facebookID);            
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        }
    }
    
    public APIResponse<User> createUser(String facebookID, String email, String fullname){
        try {
            User user = new User();            
            user.facebookId = facebookID;
            user.email = email;
            user.fullname = fullname;
            return UserClient.create(user);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        }
    }
    
    public APIResponse<User> updateUser(User user){
        try {                                   
            return UserDBService.getInstance().update(user.id, user);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        }
    }
}
