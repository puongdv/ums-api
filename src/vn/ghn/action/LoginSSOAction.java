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
import vn.ghn.account.common.Account;
import vn.ghn.account.thrift.Status;
import vn.ghn.auth.SSOAuth;
import vn.ghn.session.Session;
import vn.ghn.session.SessionDBService;
import vn.ghn.session.SessionInfo;
import vn.ghn.user.User;
import vn.ghn.user.UserClient;
import vn.ghn.user.UserInfo;

/**
 *
 * @author Bee
 */
public class LoginSSOAction extends APIResource {

    private static final Logger LOGGER = Logger.getLogger(LoginSSOAction.class);
    private static final Gson GSON = new Gson();

    public LoginSSOAction(String path) {
        super(path);
    }
    
    private void responder(User user, APIRequest req, APIResponder resp){
        Session s = new Session();
        s.userId = user.id;
        s.value = NUtils.md5(user.id + user.email + System.currentTimeMillis());
        s.userAgent = req.getHeader("User-Agent");
        s.expired = System.currentTimeMillis() + (7*86400000);
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
    }
    
    private void LoginSSO(Account acc, APIRequest req, APIResponder resp){
        String athStep1 = SSOAuth.getInstance().authStep1();
        if (athStep1.isEmpty()) {
            resp.respond(new APIResponse(APIStatus.ERROR, "Đăng nhập không thành công. Vui lòng thử lại."));
            return;
        }
        String athStep2 = SSOAuth.getInstance().authStep2(acc, athStep1.split(";")[0]);
        if (athStep2.isEmpty()) {
            resp.respond(new APIResponse(APIStatus.ERROR, "Đăng nhập không thành công. Vui lòng thử lại."));
            return;
        }

        APIResponse<User> userResult = UserClient.getBySSOID(acc.userid);
        if (userResult == null || userResult.status != APIStatus.OK) {
            resp.respond(new APIResponse(APIStatus.ERROR, "Management system is busy, please try again later."));
            return;
        }
    }

    @APIMethod(name = "POST")
    public void onPost(APIRequest req, APIResponder resp) {
        try {
            String content = req.getContent();
            Account acc = null;
            try {
                acc = GSON.fromJson(content, Account.class);
            } catch (Exception ex) {
                resp.respond(new APIResponse(APIStatus.ERROR, "Please pass as json format."));
                return;
            }
            if (acc == null) {
                resp.respond(new APIResponse(APIStatus.ERROR, "The email and password is required."));
                return;
            }
            
            APIResponse<User> userResult = UserClient.getBySSOID(acc.userid);
            if(userResult == null || (userResult.status != APIStatus.OK && userResult.status != APIStatus.NOT_FOUND)){
                resp.respond(new APIResponse(APIStatus.ERROR, "Management system is busy, please try again later."));
                return;
            }
            
            if(userResult.status == APIStatus.OK){
                User user = userResult.getFirst();
                String md5 = NUtils.md5(acc.password + user.ssoId);         
                if(!md5.equals(user.password)){
                    resp.respond(new APIResponse(APIStatus.ERROR, "The email and password you entered did not match our records. Please double-check and try again."));
                    return;
                }
                if(user.status == Status.BANNED.getValue()){
                    resp.respond(new APIResponse(APIStatus.UNAUTHORIZED, "Unauthorized, this account is banned."));
                    return;
                }
                if(user.status == Status.INITIAL.getValue()){
                    resp.respond(new APIResponse(APIStatus.UNAUTHORIZED, "Unauthorized, this account is not active."));
                    return;
                }
                responder(user, req, resp);
                return;
            }
              
            User user = userResult.getFirst();
            if (user.status == Status.BANNED.getValue()) {
                resp.respond(new APIResponse(APIStatus.UNAUTHORIZED, "Unauthorized, this account is banned."));
                return;
            }
            if (user.status == Status.INITIAL.getValue()) {
                resp.respond(new APIResponse(APIStatus.UNAUTHORIZED, "Unauthorized, this account is not active."));
                return;
            }
            Session s = new Session();
            s.userId = user.id;
            s.value = NUtils.md5(user.id + user.email + System.currentTimeMillis());
            s.userAgent = req.getHeader("User-Agent");
            s.expired = System.currentTimeMillis() + 86400000;
            
            APIResponse<Session> apiResult = SessionDBService.getInstance().create(s);
            if (apiResult == null || apiResult.status != APIStatus.OK) {
                String msg = "Management system is busy, please try again later.";
                if(apiResult != null){
                    msg = apiResult.message;
                }
                resp.respond(new APIResponse(APIStatus.ERROR, msg));
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
