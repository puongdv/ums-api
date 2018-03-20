/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.session;

import com.google.gson.Gson;
import common.api.APIResponse;
import common.api.APIStatus;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import vn.ghn.user.User;
import vn.ghn.user.UserDBService;
import vn.ghn.user.UserInfo;

/**
 *
 * @author Bee
 */
public class SessionClient {
    private static final Logger LOGGER = Logger.getLogger(SessionClient.class);      
    private static final Gson GSON = new Gson();
    
    public static APIResponse<SessionInfo> getByValue(String value){
       
        try {            
            
            Session query = new Session();
            query.value = value;
            APIResponse<Session> apiResult = SessionDBService.getInstance().queryOne(query);
            if(apiResult == null){   
                LOGGER.error(value + GSON.toJson(apiResult));
                return new APIResponse(APIStatus.NOT_FOUND, "Management system is busy, please try again later.");
            }
            if(apiResult.status != APIStatus.OK){
                LOGGER.error(value + GSON.toJson(apiResult));
                return new APIResponse(APIStatus.NOT_FOUND, "Permission denied, session is not found");
            }
            
            Session session = apiResult.getFirst();
            
            if(session.expired <= System.currentTimeMillis()){                
                return new APIResponse(APIStatus.NOT_FOUND, "Permission denied, session is expired");
            }
            
//            Session sessionUpdate = new Session();
//            sessionUpdate.expired = sessionUpdate.expired + 14400000;
//            SessionDBService.getInstance().update(session.id, sessionUpdate);
            
            APIResponse<User> userResult = UserDBService.getInstance().get(session.userId);
            if(userResult == null || userResult.status != APIStatus.OK){
                LOGGER.error(GSON.toJson(userResult));
                String msg = "Management system is busy, please try again later.";
                if(userResult != null) msg = userResult.message;
                return new APIResponse(APIStatus.NOT_FOUND, msg);
            }
            UserInfo userInfo = UserInfo.getInstance().getByUser(userResult.getFirst());
            SessionInfo sessionInfo = SessionInfo.getInstance().getBySessionUser(session, userInfo);
            List<SessionInfo> tmpList = new ArrayList<>();
            tmpList.add(sessionInfo);
            APIResponse<SessionInfo> result = new APIResponse(APIStatus.OK, "Successfull");
            result.setContent(tmpList);
            return result;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return new APIResponse(APIStatus.ERROR, "An error occurred, please try again later.");
        }
    }
    
    
}
