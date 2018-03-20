/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.action;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import common.api.APIRequest;
import common.api.APIResource;
import common.api.APIResponder;
import common.api.APIResponse;
import common.api.APIStatus;
import common.api.annotation.APIMethod;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import vn.ghn.account.thrift.Status;
import vn.ghn.user.User;
import vn.ghn.user.UserClient;
import vn.ghn.user.UserInfo;

/**
 *
 * @author Bee
 */
public class LoginApiAcion extends APIResource{
    private static final Logger LOGGER = Logger.getLogger(LoginApiAcion.class);
    private static final Gson GSON = new Gson();

    public LoginApiAcion(String path) {
        super(path);
    }
    
    @APIMethod(name = "GET")
    public void onGet(APIRequest req, APIResponder resp){
        try {
            String ssoId = req.getParams("APIKEY");
            String secret = req.getParams("APISECRET");
            if(ssoId == null || ssoId.isEmpty() || secret == null || secret.isEmpty()){
                resp.respond(new APIResponse(APIStatus.NOT_FOUND, "Data not found, missing [APIKEY and APISECRET]."));
                return;
            }
            resp.respond(doLogin(ssoId, secret));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "An error occurred, please try again later."));
        }
    }
    
    @APIMethod(name = "POST")
    public void onPost(APIRequest req, APIResponder resp){
        try {
            String content = req.getContent();
            if(content == null || content.isEmpty()){
                resp.respond(new APIResponse(APIStatus.NOT_FOUND, "Data not found, missing body json [APIKEY and APISECRET]."));
                return;
            }
            JsonElement json = GSON.fromJson(content, JsonElement.class);
            JsonObject jsonObj = json.getAsJsonObject();  
            if(json.isJsonNull() || jsonObj.isJsonNull()){
                resp.respond(new APIResponse(APIStatus.NOT_FOUND, "Permission denied, missing body json [APIKEY and APISECRET]."));
                return;
            }                
            String ssoId = jsonObj.get("APIKEY").isJsonNull() ? null : jsonObj.get("APIKEY").getAsString();
            String secret = jsonObj.get("APISECRET").isJsonNull() ? null : jsonObj.get("APISECRET").getAsString();
            if(ssoId == null || secret == null){
                resp.respond(new APIResponse(APIStatus.NOT_FOUND, "Permission denied, missing body json [APIKEY and APISECRET]."));
                return;
            }
            resp.respond(doLogin(ssoId, secret));
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "An error occurred, please try again later."));
        }
    }
    
    public APIResponse<UserInfo> doLogin(String ssoId, String secret){
        try {
            
            APIResponse<User> userResult = UserClient.getBySSOID_SECRET(ssoId, secret);
            if(userResult == null || userResult.status != APIStatus.OK){   
                String msg = "Management system is busy, please try again later.";
                if(userResult != null) msg = userResult.message; 
                
                return new APIResponse(APIStatus.ERROR, msg);
            }
            User user = userResult.getFirst();            
            if(user.status == Status.BANNED.getValue()){                
                return new APIResponse(APIStatus.UNAUTHORIZED, "Unauthorized, this account is banned.");
            }
            if(user.status == Status.INITIAL.getValue()){
                return new APIResponse(APIStatus.UNAUTHORIZED, "Unauthorized, this account is not active.");
            }  
            UserInfo userInfo = UserInfo.getInstance().getByUser(user);
            List<UserInfo> tmpList = new ArrayList<>();
            tmpList.add(userInfo);
            APIResponse<UserInfo> apiResult = new APIResponse(APIStatus.OK, "Successfull");
            apiResult.setContent(tmpList);
            return apiResult;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return new APIResponse(APIStatus.ERROR, "An error occurred, please try again later.");
        }
    }
}
