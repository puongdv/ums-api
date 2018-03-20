/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.user;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import common.api.APIResponse;
import common.api.APIStatus;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import vn.common.lib.utils.NUtils;
import vn.ghn.account.thrift.Status;

/**
 *
 * @author Bee
 */
public class UserClient {
    private static final Logger LOGGER = Logger.getLogger(UserClient.class);
    private static final Gson GSON = new Gson();
    public static APIResponse<User> getById(String userID){
        try {
            return UserDBService.getInstance().get(userID);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        }
    }
    
    public static void createUserAdmin(){
        User u = new User();
        u.email = "admin@insidev2.ghn.vn";
        u.password = NUtils.md5("insidev2@2017" + u.email);
        u.fullname = "Supper Admin";
        u.isSupperUser = true;
        u.ssoId = "00100";
        u.status = Status.ACTIVE.getValue();
        APIResponse<User> apiResult = UserClient.getByEmail(u.email);
        if(apiResult == null || (apiResult.status != APIStatus.OK && apiResult.status != APIStatus.NOT_FOUND)){
            return;
        }
        if(apiResult.status == APIStatus.NOT_FOUND) UserClient.create(u);
    }
    
    public static APIResponse<BasicDBObject> getUserBySSOID(String[] ssoIds){
        try {
            return getUserBySSOID(Arrays.asList(ssoIds));
        } catch (Exception e) {
            return new APIResponse(APIStatus.ERROR, "Có lỗi xảy ra, vui lòng thử lại sau");
        }
    }
    
    public static APIResponse<User> pushUser(List<User> users, String roleId){
        try {
            for (User user : users) {
                
                if(user.password != null){
                    user.password = NUtils.md5(user.password + user.ssoId);
                }
                
                APIResponse<User> apiResult = getBySSOID(user.ssoId);
                if(apiResult == null || (apiResult.status != APIStatus.OK && apiResult.status != APIStatus.NOT_FOUND)){
                    LOGGER.error(GSON.toJson(apiResult));
                    continue;
                }
                
                user.roleId = roleId;                
                if(apiResult.status == APIStatus.NOT_FOUND){
                    UserDBService.getInstance().create(user);
                }else{
                    User userDb = apiResult.getFirst();                    
                    UserDBService.getInstance().update(userDb.id, user);
                }
            }
            return new APIResponse(APIStatus.OK, "");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new APIResponse(APIStatus.ERROR, "Có lỗi xảy ra, vui lòng thử lại sau");
        }
    }
    
    public static APIResponse<BasicDBObject> getUserBySSOID(List<String> ssoIds){
        try {
            List<String> ids = new ArrayList<>();
            for (String ssoId : ssoIds) {
                if(!ids.contains(ssoId)){
                    ids.add(ssoId);
                }
            }
            List<BasicDBObject> tmpList = new ArrayList<>();
            for (String ssoId : ids) {
                BasicDBObject basicDb = new BasicDBObject();
                User user = new User();
                APIResponse<User> userResult = getBySSOID(ssoId);
                if(userResult != null && userResult.status == APIStatus.OK){
                    user = userResult.getFirst();
                }
                basicDb.append(ssoId, user);
                tmpList.add(basicDb);
            }
            APIResponse<BasicDBObject> apiResult = new APIResponse<>();
            apiResult.setContent(tmpList);
            return apiResult;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return new APIResponse(APIStatus.ERROR, "Có lỗi xảy ra, vui lòng thử lại sau");
        }
    }
    
    public static APIResponse<User> getByFacebookID(String facebookId){
        try {
            return UserDBService.getInstance().queryOne("facebookId", facebookId);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return new APIResponse(APIStatus.ERROR, "Có lỗi xảy ra, vui lòng thử lại sau");
        }
    }
    
    public static APIResponse<User> getBySSOID_SECRET(String ssoId, String secret){
        try {
            User doc = new User();
            doc.ssoId = ssoId;
            doc.secret = secret;
            return UserDBService.getInstance().queryOne(doc);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return new APIResponse(APIStatus.ERROR, "Có lỗi xảy ra, vui lòng thử lại sau");
        }
    }
    
    public static APIResponse<User> getBySSOID(String ssoId){
        try {
            User query = new User();
            query.ssoId = ssoId.trim();
            return UserDBService.getInstance().queryOne(query);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return new APIResponse(APIStatus.ERROR, "Có lỗi xảy ra, vui lòng thử lại sau");
        }
    }
    
    public static APIResponse<User> getByEmail(String email){
        try {
            return UserDBService.getInstance().queryOne("email", email);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return new APIResponse(APIStatus.ERROR, "Có lỗi xảy ra, vui lòng thử lại sau");
        }
    }    
    
    public static APIResponse<User> create(User user){
        try {
            user.secret = createHash(50);
            if(user.ssoId == null || "".equals(user.ssoId)) user.ssoId = createSSOID(10);            
            return UserDBService.getInstance().create(user);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return new APIResponse(APIStatus.ERROR, "Có lỗi xảy ra, vui lòng thử lại sau");
        }
    }
    
    public static APIResponse<User> update(User userUpdate, String userId){
        try {
            APIResponse<User> result = UserDBService.getInstance().get(userId);
            if(result == null || result.status != APIStatus.OK){
                return result;
            }
            User user = result.getFirst();
            if(userUpdate.password != null){
                userUpdate.password = NUtils.md5(userUpdate.password + user.ssoId);
            }
            if(user.ssoId == null){
                user.ssoId = createSSOID(10);
            }else{
                user.ssoId = null;
            }
            return UserDBService.getInstance().update(userId, userUpdate);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return new APIResponse(APIStatus.ERROR, "Có lỗi xảy ra, vui lòng thử lại sau");
        }
    }
    
    private static StringBuilder sbd = new StringBuilder();    
    private static String tpl = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
    private static String tpl2 = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM0123456789";
    
    private static String createHash(int l) {
        char c = tpl.charAt((int) (Math.random() * 52));
        String triedHash = randomHash(c, l);  
        User query = new User();
        query.secret = triedHash;
        APIResponse<User> userResult  = UserDBService.getInstance().queryOne(query);
        while (userResult != null && userResult.status == APIStatus.OK) {
            triedHash = randomHash(c, l);
            userResult = UserDBService.getInstance().queryOne("secret", triedHash);
        }
        return triedHash;
    }
    
    private static String createSSOID(int l) {
        char c = tpl.charAt((int) (Math.random() * 52));
        String triedHash = randomHash(c, l);  
        User query = new User();
        query.ssoId = triedHash;
        APIResponse<User> userResult  = UserDBService.getInstance().queryOne(query);
        while (userResult != null && userResult.status == APIStatus.OK) {
            triedHash = randomHash(c, l);
            userResult = UserDBService.getInstance().queryOne("secret", triedHash);
        }
        return triedHash;
    }
    
    private static String randomHash(char startChar, int l) {
        sbd.setLength(0);
        sbd.append(startChar);
        for (int i = 0; i < l; i++) {
            sbd.append(tpl2.charAt((int) (62 * Math.random())));
        }
        return sbd.toString();
    }
        
}
