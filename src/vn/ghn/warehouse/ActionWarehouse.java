/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.warehouse;

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
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.bson.Document;
import vn.common.lib.utils.NNumberUtils;
import vn.ghn.user.UserAction;
import vn.ghn.utils.Utility;

/**
 *
 * @author Bee
 */
public class ActionWarehouse extends APIResource{
    
    private static final Logger LOGGER = Logger.getLogger(UserAction.class);
    private static final Gson GSON = new Gson();

    public ActionWarehouse(String path) {
        super(path);
    }    
    
    @APIMethod(name = "GET")
    public void onQuery(APIRequest req, APIResponder resp){
        try {
            
            String key = req.getParams("key");            
            String q = req.getParams("q");
            Warehouse query;
            if (q != null) {
                try {
                    query = GSON.fromJson(q, Warehouse.class);
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                    resp.respond(new APIResponse(APIStatus.INVALID, "Invalid query. Please put in JSON format."));
                    return;
                }
            } else {
                query = new Warehouse();
            }
            
            BasicDBObject basicDbQuery = WarehouseClient.getInstance().getBasicDBObject(query);
            if(key != null && !key.isEmpty()){
                List<BasicDBObject> dbObjList = new ArrayList<>();
            
                BasicDBObject basicDb = new BasicDBObject();
                
                BasicDBObject regex = new BasicDBObject();
                regex.put("$regex", key);
                regex.put("$options", "i");                        
                
                int[] ids = new  int[]{Utility.parseInt(key)};
                DBObject dbObj = QueryBuilder.start("warehouse_id").in(ids).get();                
                basicDb.put("warehouse_id",  dbObj.get("warehouse_id"));              
                dbObjList.add(basicDb);
                
                basicDb = new BasicDBObject();
                basicDb.put("warehouse_code",  regex); 
                dbObjList.add(basicDb);                
                
                basicDb = new BasicDBObject();                      
                basicDb.put("warehouse_name",  regex);
                dbObjList.add(basicDb);
                
                basicDb = new BasicDBObject();
                basicDb.put("partner_code",  regex); 
                dbObjList.add(basicDb);
                
                basicDbQuery.append("$or", dbObjList);                
            }
            
            // pagination options
            long offset = NNumberUtils.getLong(req.getParams("offset"), 0);
            long limit = NNumberUtils.getLong(req.getParams("limit"), 20);            
            boolean reverse = req.getParams("reverse") != null;
            
            if(limit > 1024) limit = 1024;
                         
            APIResponse<Warehouse> apiResult = WarehouseDBService
                    .getInstance()
                    .queryMongoDB(Document.parse(basicDbQuery.toJson()), offset, limit, reverse);
            resp.respond(apiResult);
            
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "An error occurred, please try again later."));
        }
    }
    
    @APIMethod(name = "POST")
    public void onPost(APIRequest req, APIResponder resp){
        try {
            // get driver information
            String content = req.getContent();
            Warehouse warehouse = null;
            if (content != null) {
                try {
                    warehouse = GSON.fromJson(content, Warehouse.class);
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            } 
            
            if (warehouse == null){
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid data. Please put in JSON format."));
                return;
            }            
            
            APIResponse<Warehouse> result = WarehouseClient.getInstance().create(warehouse);            
            if(result == null){
                resp.respond(new APIResponse(APIStatus.ERROR, "Management system is busy, please try again later."));
                return;
            } 
            
            resp.respond(result);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, ex.getMessage()));
        }
    }
}
