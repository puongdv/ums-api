/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.action;

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
import vn.common.lib.utils.NUtils;
import vn.ghn.account.thrift.Status;
import vn.ghn.auth.FBAuth;
import vn.ghn.session.Session;
import vn.ghn.session.SessionDBService;
import vn.ghn.session.SessionInfo;
import vn.ghn.user.User;
import vn.ghn.user.UserInfo;

/**
 * This resource handle "/login" path of the User API.
 * @author Bee
 */
public class LoginFbAction extends APIResource{
    private static final Logger LOGGER = Logger.getLogger(LoginT62Action.class);
    private static final Gson GSON = new Gson(); 

    public LoginFbAction(String path) {
        super(path);
    }
    
    @APIMethod(name = "GET")
    public void onGet(APIRequest req, APIResponder resp){
        try {
            String email = req.getParams("email");
            String facebookId = req.getParams("facebookId");
            String fullname = req.getParams("fullname");
            if(facebookId == null || facebookId.isEmpty()){
                resp.respond(new APIResponse(APIStatus.ERROR, "Require {facebookID}."));
                return;
            }
            User user;
            APIResponse<User> userResult = FBAuth.getInstance().auth(facebookId, email);
            if(userResult == null || (userResult.status != APIStatus.OK && userResult.status != APIStatus.NOT_FOUND)){
                resp.respond(new APIResponse(APIStatus.ERROR, "Management system is busy, please try again later."));
                return;
            }
            if(userResult.status == APIStatus.NOT_FOUND){
                userResult = FBAuth.getInstance().createUser(facebookId, email, fullname);
                if(userResult == null || userResult.status != APIStatus.OK){
                    resp.respond(new APIResponse(APIStatus.ERROR, "Management system is busy, please try again later."));
                    return;
                }
                user = userResult.getFirst();
            }else{
                user = userResult.getFirst();
                if(!facebookId.equals(user.facebookId)){
                    user.facebookId = facebookId;
                    FBAuth.getInstance().updateUser(user);
                }
            }
                        
            if(user.status == Status.BANNED.getValue()){
                resp.respond(new APIResponse(APIStatus.UNAUTHORIZED, "Unauthorized, this account is banned."));
                return;
            }
            if(user.status == Status.INITIAL.getValue()){
                resp.respond(new APIResponse(APIStatus.UNAUTHORIZED, "Unauthorized, this account is not active."));
                return;
            }
            Session s = new Session();
            s.userId = user.id;
            s.value = NUtils.md5(user.id + user.email + System.currentTimeMillis());
            s.userAgent = req.getHeader("User-Agent");
            s.expired = System.currentTimeMillis() + 86400000;
            APIResponse<Session> apiResult = SessionDBService.getInstance().create(s);
            if(apiResult == null || apiResult.status != APIStatus.OK){
                resp.respond(new APIResponse(APIStatus.ERROR, "Management system is busy, please try again later."));
                return;
            }
            
            SessionInfo sessionInfo = SessionInfo.getInstance().getBySessionUser(s,UserInfo.getInstance().getByUser(user));
            List<SessionInfo> tmpList = new ArrayList<>();
            tmpList.add(sessionInfo);
            
            APIResponse<SessionInfo> response = new APIResponse<>(APIStatus.OK, apiResult.message);
            response.setContent(tmpList);
            
            resp.respond(response);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "An error occurred, please try again later."));
        }
    }
}
