/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.role;

import com.google.gson.Gson;
import common.api.APIRequest;
import common.api.APIResource;
import common.api.APIResponder;
import common.api.APIResponse;
import common.api.APIStatus;
import common.api.annotation.APIMethod;
import org.apache.log4j.Logger;
import vn.common.lib.utils.NNumberUtils;

/**
 *
 * @author Bee
 */
public class RoleAction extends APIResource{
    
    private static final Logger LOGGER = Logger.getLogger(RoleAction.class);
    private static final Gson GSON = new Gson();

    public RoleAction(String path) {
        super(path);
    }
    @APIMethod(name = "GET")
    public void onQuery(APIRequest req, APIResponder resp){
        try {
            // get query information
            String query = req.getParams("q");
            Role roleQuery;
            if (query != null) {
                try {
                    roleQuery = GSON.fromJson(query, Role.class);
                    
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                    resp.respond(new APIResponse(APIStatus.INVALID, "Invalid query. Please put in JSON format."));
                    return;
                }
            } else {
                roleQuery = new Role();
            }
            // pagination options
            long offset = NNumberUtils.getLong(req.getParams("offset"), 0);
            long limit = NNumberUtils.getLong(req.getParams("limit"), 20);
            if(limit > 1024) limit = 1024;
            boolean reverse = req.getParams("reverse") != null;
            
             // query to DB
            APIResponse<Role> result = RoleDBService.getInstance().query(roleQuery, offset, limit, reverse);

            // response API result
            resp.respond(result);
            
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            resp.respond(new APIResponse(APIStatus.ERROR, ex.getMessage()));
        }
    }
    
    @APIMethod(name = "POST")
    public void onPost(APIRequest req, APIResponder resp){
        try {
            // get driver information
            String content = req.getContent();
            Role role = null;
            if (content != null) {
                try {
                    role = GSON.fromJson(content, Role.class);
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            } 
            
            // reject if wrong format
            if (role == null){
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid data. Please put in JSON format."));
                return;
            }
            
            APIResponse<Role> result = RoleDBService.getInstance().create(role);
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
