/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.user;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import common.api.APIRequest;
import common.api.APIResource;
import common.api.APIResponder;
import common.api.APIResponse;
import common.api.APIStatus;
import common.api.annotation.APIMethod;
import org.apache.log4j.Logger;
import org.bson.Document;

/**
 *
 * @author Bee
 */
public class OneUserAction extends APIResource{
    private static final Logger LOGGER = Logger.getLogger(OneUserAction.class);
    private static final Gson GSON = new Gson();

    public OneUserAction(String path) {
        super(path);
    }
    
    @APIMethod(name = "GET")
    public void onGet(APIRequest req, APIResponder resp) {
        try {
            // parse information from request
            String id = req.getVar("id");
            if (id == null || id.isEmpty()) {
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid id."));
                return;
            }
            
            String[] ssoIds = id.split(",");
            if(ssoIds.length == 1){
                APIResponse<User> apiResult = UserClient.getBySSOID(id);
                if(apiResult == null || (apiResult.status != APIStatus.OK && apiResult.status != APIStatus.NOT_FOUND)){
                    LOGGER.error(GSON.toJson(apiResult));
                    resp.respond(apiResult);
                    return;
                }
                if(apiResult.status == APIStatus.NOT_FOUND){
                    apiResult = UserDBService.getInstance().get(id);
                }
                resp.respond(apiResult);
                return;
            }
            DBObject dbObject = QueryBuilder.start("sso_id").in(ssoIds).get();
            BasicDBObject basicQuery = new BasicDBObject();
            basicQuery.append("sso_id", dbObject.get("sso_id"));
            
            APIResponse<User> userResult = UserDBService.getInstance().queryMongoDB(Document.parse(basicQuery.toJson()), 0, ssoIds.length, true);
            if(userResult == null || userResult.status != APIStatus.OK){
                resp.respond(userResult);
                return;
            }
            
            resp.respond(userResult);                             
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "An error occurred, please try again later."));
        }
    }
    
    @APIMethod(name = "PUT")
    public void onPut(APIRequest req, APIResponder resp) {
        try {
            // parse information from request
            String id = req.getVar("id");
            if (id == null || id.isEmpty()) {
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid id."));
                return;
            }

            // get user information
            String content = req.getContent();
            User user = null;
            if (content != null) {
                try {
                    user = GSON.fromJson(content, User.class);
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            } 
            
            if (user == null){
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid data. Please put in JSON format."));
                return;
            }
            
            LOGGER.info(req.getMethod() + user);            
            APIResponse<User> apiResult = UserClient.update(user, id);            
            if(apiResult == null){
                resp.respond(new APIResponse(APIStatus.ERROR, "Management system is busy, please try again later."));
                return;
            }     
                                 
            resp.respond(apiResult);            
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "An error occurred, please try again later."));
        }
    }
    
    @APIMethod(name = "DELETE")
    public void onDelete(APIRequest req, APIResponder resp) {
        try {
            // parse information from request
            String id = req.getVar("id");
            if (id == null || id.isEmpty()) {
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid id."));
                return;
            }

            // call DB
            APIResponse<User> result = UserDBService.getInstance().delete(id);            
            resp.respond(result); 
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "An error occurred, please try again later."));
        }
    }
}
