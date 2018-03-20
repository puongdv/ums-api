/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.permission;

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
public class PermissionAction extends APIResource{
    private static final Logger LOGGER = Logger.getLogger(PermissionAction.class);
    private static final Gson GSON = new Gson();

    public PermissionAction(String path) {
        super(path);
    }
    
    @APIMethod(name = "GET")
    public void onQuery(APIRequest req, APIResponder resp){
        try {
            // get query information
            String query = req.getParams("q");
            Permission perQuery;
            if (query != null) {
                try {
                    perQuery = GSON.fromJson(query, Permission.class);
                    
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                    resp.respond(new APIResponse(APIStatus.INVALID, "Invalid query. Please put in JSON format."));
                    return;
                }
            } else {
                perQuery = new Permission();                
            }
            
            long offset = NNumberUtils.getLong(req.getParams("offset"), 0);
            long limit = NNumberUtils.getLong(req.getParams("limit"), 20);
            if(limit > 1024) limit = 1024;
            boolean reverse = req.getParams("reverse") != null;            
            APIResponse<Permission> result = PermissionDBService.getInstance().query(perQuery, offset, limit, reverse);
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
            Permission permission = null;
            if (content != null) {
                try {
                    permission = GSON.fromJson(content, Permission.class);
                } catch (Exception ex) {
                    LOGGER.error(ex.getMessage(), ex);
                }
            }             
            // reject if wrong format
            if (permission == null){
                resp.respond(new APIResponse(APIStatus.INVALID, "Invalid data. Please put in JSON format."));
                return;
            }
            Permission per = new Permission();
            per.key = permission.key;
            per.path = permission.path;
            APIResponse<Permission> result = PermissionDBService.getInstance().queryOne(per);
            if(result == null || (result.status != APIStatus.OK && result.status != APIStatus.NOT_FOUND)){
                resp.respond(new APIResponse(APIStatus.ERROR, "Management system is busy, please try again later."));
                return;
            }
            
            if(result.status == APIStatus.OK){
                resp.respond(new APIResponse(APIStatus.EXISTED, String.format("Permission %s existed.", permission.key)));
                return;
            }
            
            result = PermissionDBService.getInstance().create(permission);
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
