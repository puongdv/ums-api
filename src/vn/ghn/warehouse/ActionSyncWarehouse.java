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
public class ActionSyncWarehouse extends APIResource{
    
    private static final Logger LOGGER = Logger.getLogger(UserAction.class);
    private static final Gson GSON = new Gson();

    public ActionSyncWarehouse(String path) {
        super(path);
    }    
    
    @APIMethod(name = "GET")
    public void onQuery(APIRequest req, APIResponder resp){
        try {
            
            WarehouseClient.getInstance().syncWarehousesFromGHN();
            resp.respond(new APIResponse(APIStatus.OK, "Sync data successful."));
            
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, "An error occurred, please try again later."));
        }
    }
    
}
