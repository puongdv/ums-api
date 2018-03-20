/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.partner;

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
import vn.common.lib.config.NConfig;
import vn.common.lib.utils.NNumberUtils;

/**
 *
 * @author Bee
 */
public class ActionPartner extends APIResource{
    
    private static final Logger LOGGER = Logger.getLogger(ActionPartner.class);
    private static final Gson GSON = new Gson();

    public ActionPartner(String path) {
        super(path);
    }
    @APIMethod(name = "GET")
    public void onQuery(APIRequest req, APIResponder resp){
        try {
            String key = req.getParams("key");
            String q = req.getParams("q");
            Partner query;
            if (q != null) {
                try {
                    query = GSON.fromJson(q, Partner.class);                    
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                    resp.respond(new APIResponse(APIStatus.INVALID, "Invalid query. Please put in JSON format."));
                    return;
                }
            } else {
                query = new Partner();
            }
            
            BasicDBObject basicDBQuery = GSON.fromJson(GSON.toJson(query), BasicDBObject.class);
            if(key != null){
                List<BasicDBObject> dbObjList = new ArrayList<>();
            
                BasicDBObject dbObj = new BasicDBObject();
                
                BasicDBObject regex = new BasicDBObject();
                regex.put("$regex", key);
                regex.put("$options", "i");                                        
                                      
                dbObj.put("partnerCode",  regex); 
                dbObjList.add(dbObj);
                 
                dbObj = new BasicDBObject();                      
                dbObj.put("partnerName",  regex); 
                dbObjList.add(dbObj);  
                
                dbObj = new BasicDBObject();                      
                dbObj.put("description",  regex); 
                dbObjList.add(dbObj); 
                
                basicDBQuery.append("$or", dbObjList);                
            }
            long offset = NNumberUtils.getLong(req.getParams("offset"), 0);
            long limit = NNumberUtils.getLong(req.getParams("limit"), 20);
            int maxQuery = NConfig.getInt("validate.maxQuery");
            if(limit > maxQuery) limit = maxQuery;
            boolean reverse = req.getParams("reverse") != null;
            
            APIResponse<Partner> apiResult = PartnerDBService.getInstance().queryMongoDB(Document.parse(basicDBQuery.toJson()), offset, limit, reverse);
            if(apiResult == null){
                resp.respond(new APIResponse(APIStatus.ERROR, "Management system is busy, please try again later."));
                return;
            }
            resp.respond(apiResult);
            
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "An error occurred, please try again later." + ex.getMessage()));
        }
    }
    
    @APIMethod(name = "POST")
    public void onPost(APIRequest req, APIResponder resp){
        try {
            
            String content = req.getContent();
            Partner group = null;
            if (content != null) {
                try {
                    group = GSON.fromJson(content, Partner.class);
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            } 
            
            if (group == null){
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid data. Please put in JSON format."));
                return;
            }
            
            APIResponse<Partner> apiResult = PartnerClient.getInstance().create(group);
            if(apiResult == null){
                resp.respond(new APIResponse(APIStatus.ERROR, "Management system is busy, please try again later."));
                return;
            }                      
            resp.respond(apiResult);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "An error occurred, please try again later." + ex.getMessage()));
        }
    }
}
