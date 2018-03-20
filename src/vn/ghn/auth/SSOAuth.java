/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.auth;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import common.api.APIResponse;
import common.api.APIStatus;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import vn.common.lib.config.NConfig;
import vn.ghn.account.common.Account;
import vn.ghn.account.thrift.Status;
import vn.ghn.user.User;
import vn.ghn.user.UserClient;
import vn.ghn.utils.ContentTypeContant;
import vn.ghn.utils.HttpUtil;
import vn.ghn.utils.MethodContant;

/**
 *
 * @author Bee
 */
public class SSOAuth {
    
    private static final Logger LOGGER = Logger.getLogger(SSOAuth.class); 
    private static final Gson GSON = new Gson();
    
    public static SSOAuth getInstance() {
        return SSOAuth.SSOAuthHolder.INSTANCE;
    }
     
    private static class SSOAuthHolder {
        private static final SSOAuth INSTANCE = new SSOAuth();
    }
    
    public APIResponse<User> auth(String t62) throws IOException{
        try {
            
            String result = HttpUtil.sendUrlencoded(NConfig.getString("service.sso-verify-url"), 
                    String.format("AppKey=%s&Token=%s", NConfig.getString("service.sso-appkey"), t62));              
            JsonElement json = GSON.fromJson(result, JsonElement.class);
            JsonObject jsonObj = json.getAsJsonObject();               
            if(jsonObj.isJsonNull() || (!jsonObj.get("ErrorMessage").isJsonNull() && !jsonObj.get("ErrorMessage").getAsString().equals(""))){
                LOGGER.error("Cannot get SSO user " + result);
                return null;
            }   
            
            String ssoID = !jsonObj.get("Username").isJsonNull() ? jsonObj.get("Username").getAsString() : null;            
            return this.getSSOUser(ssoID); 
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return null;
        }               
    }        
    
    public APIResponse<User> getSSOUser(String ssoId) throws IOException{
        try {                   
            String url = NConfig.getString("service.sso-user-detail-url");
            String urlParam = String.format("apikey=%s&apipass=%s&internalid=%s", NConfig.getString("service.sso-user-detail-apikey"),
                    NConfig.getString("service.sso-user-detail-apipass"), ssoId);
            String result = HttpUtil.sendUrlencoded(url, urlParam);
            if(result == null || result.isEmpty()) return null;            
            
            JsonElement json = GSON.fromJson(result, JsonElement.class);
            JsonObject jsonObj = json.getAsJsonObject();                                    
            if(jsonObj.isJsonNull() || jsonObj.get("code").isJsonNull() || jsonObj.get("code").getAsInt() != 1 || jsonObj.get("data").isJsonNull()){
                LOGGER.error("ERROR " + result);
                return null;
            }            
            JsonObject SSOUser = jsonObj.get("data").getAsJsonObject();
            APIResponse<User> userResult = UserClient.getBySSOID(ssoId);            
            if(userResult != null && (userResult.status == APIStatus.NOT_FOUND)){
                User user = new User();
                user.fullname = SSOUser.get("fullname").getAsString();                
                user.email = SSOUser.get("email").getAsString();
                user.ssoId = ssoId;
                user.phone = SSOUser.get("phone").getAsString();
                user.status = Status.ACTIVE.getValue();
                userResult = UserClient.create(user);
            }
            return userResult;
        } catch (JsonSyntaxException ex) {            
            LOGGER.error(ex.getMessage(), ex);
            return null;
        }
    }
    
    public String authStep1() {
        try {
            String url = NConfig.getString("sso.get-token") + NConfig.getString("sso.app-id");
            Map<String, List<String>> result = HttpUtil.makeRequestResultHeader(url, ContentTypeContant.APPLICATION_X_WWW_FORM_URLENCODED, null, MethodContant.GET);
            String coookie = "";
            if (result != null) {
                coookie = result.get(NConfig.getString("sso.token-field-step-1")).get(0);
            }
            return coookie;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return "";
        }
    }

    public String authStep2(Account acc, String token) {
        try {
            String url = NConfig.getString("sso.verify-url") + NConfig.getString("sso.app-id");
            Map<String, String> headers = new HashMap<>();
            headers.put(NConfig.getString("sso.token-field-step-2-1"), token);
            Map<String, String> formData = new HashMap<>();
            formData.put("username", acc.userid);
            formData.put("password", acc.password);
            formData.put(NConfig.getString("sso.app-id-field-name"), NConfig.getString("sso.app-id"));
            Map<String, List<String>> result = HttpUtil.sendPostRequestResultHeader(url, ContentTypeContant.APPLICATION_X_WWW_FORM_URLENCODED, formData, headers);
            String coookie = "";
            if (result != null) {
                try {
                    coookie = result.get(NConfig.getString("sso.token-field-step-2-2")).get(0);
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }
            return coookie;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return "";
        }
    }
}