/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.warehouse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;
import common.api.APIResponse;
import common.api.APIStatus;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import vn.common.lib.config.NConfig;
import vn.ghn.utils.ContentTypeContant;
import vn.ghn.utils.HttpUtil;
import vn.ghn.utils.MethodContant;
import vn.ghn.utils.Utility;

/**
 *
 * @author Bee
 */
public class WarehouseClient {
    
    private static final Logger LOGGER = Logger.getLogger(WarehouseClient.class);
    private static final Gson GSON = new Gson();
    
    public static WarehouseClient getInstance() {
        return WarehouseClient.WarehouseClientHolder.INSTANCE;
    }
     
    private static class WarehouseClientHolder {
        private static final WarehouseClient INSTANCE = new WarehouseClient();
    }
    
    private List<String> removeDuplicate(String[] ids){
        
        try {         
            List<String> results = new ArrayList<>();
            for (String id : ids) {
                if (!results.contains(id)) {
                    results.add(id);
                }
            }
            return results;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return new ArrayList<>();
        }
    }
    
    public BasicDBObject getBasicDBObject(Warehouse warehouse) throws 
            IllegalArgumentException, 
            IllegalAccessException {
        
        BasicDBObject basicDBObject = new BasicDBObject();             
        for (Field field : warehouse.getClass().getDeclaredFields()) {            
            if(field.get(warehouse) != null){
                String fieldName = Utility.convertToUnderscore(field.getName());
                basicDBObject.put(fieldName, field.get(warehouse));               
            }
        }
        return basicDBObject;
    }   
    
    public APIResponse<Warehouse> get(int warehouseId){
        try {
            Warehouse query = new Warehouse();
            query.warehouseId = warehouseId;
            return WarehouseDBService.getInstance().queryOne(query);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new APIResponse(APIStatus.ERROR, "An error occurred, please try again later.");
        }
    }
    
    public APIResponse<Warehouse> get(int[] warehouseIds){
        try {
            List<Warehouse> warehouses = new ArrayList<>();
            for (int warehouseId : warehouseIds) {
                APIResponse<Warehouse> apiResult = get(warehouseId);
                if(apiResult == null || apiResult.status != APIStatus.OK) continue;
                warehouses.add(apiResult.getFirst());
            }
            if(warehouses.isEmpty()){
                return new APIResponse(APIStatus.NOT_FOUND, "Not found data.");
            }
            APIResponse<Warehouse> result = new APIResponse(APIStatus.OK, "Get warehouse successful.");
            result.setContent(warehouses);
            return result;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new APIResponse(APIStatus.ERROR, "An error occurred, please try again later.");
        }
    }
    
    public APIResponse<Warehouse> getByIds(String[] ids){
        try {
            List<String> idList = removeDuplicate(ids);
            List<Warehouse> warehouseList = new ArrayList<>();
            for (String warehouseId : idList) {
                Warehouse q = new Warehouse();
                q.warehouseId = Integer.parseInt(warehouseId);
                APIResponse<Warehouse> result = WarehouseDBService.getInstance().queryOne(q);
                if(result == null || result.status != APIStatus.OK){
                    continue;
                }
                warehouseList.add(result.getFirst());
            }
            APIResponse<Warehouse> result = new APIResponse(APIStatus.OK, "Successfull");
            result.setContent(warehouseList);
            return result;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return new APIResponse(APIStatus.ERROR, "An error occurred, please try again later.");
        }        
    }        
    
    public APIResponse<Warehouse> create(Warehouse warehouse){
        try {
            
            APIResponse<Warehouse> apiResult = get(warehouse.warehouseId);
            if(apiResult == null || 
                (apiResult.status != APIStatus.OK && apiResult.status != APIStatus.NOT_FOUND)
            ){
                String msg = apiResult == null ? 
                    "Management system is busy, please try again later." : apiResult.message;
                APIStatus aPIStatus = apiResult == null ? APIStatus.ERROR : apiResult.status;
                return new APIResponse(aPIStatus, msg);
            }
            
            if(apiResult.status == APIStatus.OK){                
                return new APIResponse(
                    APIStatus.EXISTED, 
                    String.format("Existed data, warehouse [%s] is existed.", warehouse.warehouseId));
            }
            
            return WarehouseDBService.getInstance().create(warehouse);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return new APIResponse(APIStatus.ERROR, "An error occurred, please try again later.");
        }        
    }
    
    public APIResponse<Warehouse> update(int warehouseId, Warehouse warehouse){
        try {
            
            APIResponse<Warehouse> apiResult = get(warehouseId);
            if(apiResult == null || apiResult.status != APIStatus.OK){
                String msg = apiResult == null ? 
                    "Management system is busy, please try again later." : apiResult.message;
                APIStatus aPIStatus = apiResult == null ? APIStatus.ERROR : apiResult.status;
                return new APIResponse(aPIStatus, msg);
            }                        
            Warehouse warehouseDb = apiResult.getFirst();
            return WarehouseDBService.getInstance().update(warehouseDb.id, warehouseDb);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return new APIResponse(APIStatus.ERROR, "An error occurred, please try again later.");
        }        
    }
    
    public APIResponse<Warehouse> delete(int warehouseId){
        try {
            
            APIResponse<Warehouse> apiResult = get(warehouseId);
            if(apiResult == null || apiResult.status != APIStatus.OK){
                String msg = apiResult == null ? 
                    "Management system is busy, please try again later." : apiResult.message;
                APIStatus aPIStatus = apiResult == null ? APIStatus.ERROR : apiResult.status;
                return new APIResponse(aPIStatus, msg);
            }                        
            Warehouse warehouseDb = apiResult.getFirst();
            return WarehouseDBService.getInstance().delete(warehouseDb.id);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            return new APIResponse(APIStatus.ERROR, "An error occurred, please try again later.");
        }        
    }
    
    private Map<String, String> getHeader(){
        Map<String, String> m = new HashMap<>();            
        m.put("ApiKey", NConfig.getString("ghn-api.ApiKey"));
        m.put("ApiSecretKey", NConfig.getString("ghn-api.ApiSecretKey"));
        return m;
    }
    
    private boolean isNull(JsonElement obj){
        try {            
            return (obj == null || obj.isJsonNull() || "".equals(obj.getAsString()));
        } catch (Exception e) {
            return false;
        }
    }
  
    public void syncWarehousesFromGHN(){
        try {
            
            String apiUrl = String.format("%s/Ontime/Cache/GetWarehouses", NConfig.getString("ghn-api.domain"));               
            
            JsonElement el = HttpUtil.send(JsonElement.class, apiUrl, GSON.toJson(getHeader()), MethodContant.POST, ContentTypeContant.APPLICATION_JSON, null);
            JsonObject obj = el.getAsJsonObject();            
            if(obj == null ||                     
                    (!isNull(obj.get("ErrorMessage")) && !"0".equals(obj.get("ErrorMessage").getAsString())) ||
                    (!isNull(obj.get("Message")) && !obj.get("Message").getAsString().isEmpty())                    
                ){                
                LOGGER.error(apiUrl + " " + el);
                return;
            }            
            if(obj.get("Warehouses") != null){
                JsonArray jsonArray = obj.get("Warehouses").getAsJsonArray();
                if(jsonArray != null){
                    for (JsonElement jsonEl : jsonArray) {
                        Warehouse doc = convertObjToWarehouse(jsonEl.getAsJsonObject());
                        doc.warehouseCode = doc.warehouseCode != null ? doc.warehouseCode.trim() : null;
                        doc.warehouseName = doc.warehouseName != null ? doc.warehouseName.trim() : null;
                        Warehouse q = new Warehouse();
                        q.warehouseId = doc.warehouseId;                        
                        APIResponse<Warehouse> apiResult = WarehouseDBService.getInstance().queryOne(q);
                        if(apiResult == null || (apiResult.status != APIStatus.OK && apiResult.status != APIStatus.NOT_FOUND)){
                            LOGGER.error(q);
                            LOGGER.error(GSON.toJson(apiResult));
                            continue;
                        }
                        if(apiResult.status == APIStatus.OK){
                            Warehouse objDB = apiResult.getFirst();
                            Warehouse warehouseUpdate = new Warehouse();
                            warehouseUpdate.warehouseName = doc.warehouseName;
                            apiResult = WarehouseDBService.getInstance().update(objDB.id, warehouseUpdate);
                            if(apiResult == null || apiResult.status != APIStatus.OK){
                                LOGGER.error(q);
                                LOGGER.error(GSON.toJson(apiResult));                                
                            }
                            continue;
                        }
                        apiResult = WarehouseDBService.getInstance().create(doc);
                        if(apiResult == null || apiResult.status != APIStatus.OK){
                            LOGGER.error(q);
                            LOGGER.error(GSON.toJson(apiResult));                                
                        }
                    }
                }
            }           
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
    
    private Warehouse convertObjToWarehouse(JsonObject obj){
        try {
            Warehouse warehouse = new Warehouse();
            warehouse.warehouseName = obj.get("WarehouseName") == null ? null : obj.get("WarehouseName").getAsString();
            warehouse.warehouseCode = obj.get("WarehouseCode") == null ? null : obj.get("WarehouseCode").getAsString();
            warehouse.warehouseId = obj.get("WarehouseID") == null ? null : obj.get("WarehouseID").getAsInt();
            return warehouse;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }
}
