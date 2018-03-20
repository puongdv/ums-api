/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.user;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import common.api.APIRequest;
import common.api.APIResource;
import common.api.APIResponder;
import common.api.APIResponse;
import common.api.APIStatus;
import common.api.annotation.APIMethod;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.bson.Document;
import vn.common.lib.utils.NNumberUtils;

/**
 *
 * @author Bee
 */
public class UserSearchAction extends APIResource{
    private static final Logger LOGGER = Logger.getLogger(UserSearchAction.class);
    private static final Gson GSON = new Gson();
    private Object doc;

    public UserSearchAction(String path) {
        super(path);
    }
    
    @APIMethod(name = "GET")
    public void onQuery(APIRequest req, APIResponder resp){
        try {
            // get query information
            String query = req.getParams("key");
            BasicDBObject dbObjQuery = new BasicDBObject();
            
            if(query != null && !query.isEmpty()){
                List<BasicDBObject> dbObjList = new ArrayList<>();
            
                BasicDBObject dbObj = new BasicDBObject();
                
                BasicDBObject regex = new BasicDBObject();
                regex.put("$regex", query);
                regex.put("$options", "i");                       
                
                dbObj.put("fullname",  regex);              
                dbObjList.add(dbObj);
                
                dbObj = new BasicDBObject();                      
                dbObj.put("email",  regex); 
                dbObjList.add(dbObj);
                
                dbObj = new BasicDBObject();                      
                dbObj.put("sso_id",  regex); 
                dbObjList.add(dbObj);
                
                dbObj = new BasicDBObject();                      
                dbObj.put("phone",  regex);
                dbObjList.add(dbObj);
                
                dbObjQuery.append("$or", dbObjList);
            }

            // pagination options
            long offset = NNumberUtils.getLong(req.getParams("offset"), 0);
            long limit = NNumberUtils.getLong(req.getParams("limit"), 20);
            if(limit > 1024) limit = 1024;
            boolean reverse = req.getParams("reverse") != null;
            
             // query to DB
            APIResponse<User> result = UserDBService.getInstance().queryMongoDB(Document.parse(dbObjQuery.toJson()), offset, limit, reverse);

            // response API result
            resp.respond(result);
            
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "Có lỗi xảy ra, vui lòng thử lại sau"));
        }
    }
}
