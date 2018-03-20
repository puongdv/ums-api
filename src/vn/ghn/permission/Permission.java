/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.permission;

import com.google.gson.annotations.SerializedName;
import common.api.APIModel;
import common.api.annotation.CreationRequire;
import common.api.annotation.DBIncrement;
import common.api.annotation.DBPersistent;
import common.api.annotation.Updatable;

/**
 *
 * @author Bee
 */
public class Permission extends APIModel<String>{
    
    @DBPersistent(fieldName = "key")
    @SerializedName(value = "key")
    @Updatable 
    @CreationRequire
    public String key;
    
    @DBPersistent(fieldName = "path")
    @SerializedName(value = "path")
    @Updatable 
    @CreationRequire
    public String path;
    
    @DBPersistent(fieldName = "description")
    @SerializedName(value = "description")
    @Updatable
    public String description;
    
    @DBPersistent(fieldName = "is_enable")
    @SerializedName(value = "isEnable")
    @Updatable
    public Boolean isEnable;
    
    /**
     * This value indicate the order number of this object in DB.
     * Ex: the first driver will have this = 1, next is 2, 3, ...
     */
    @DBPersistent(fieldName = "iindex")
    @SerializedName(value = "orderNumber")
    @DBIncrement
    public Long iIndex;

    @Override
    public String toString() {
        return "Permission{" + "key=" + key + ", description=" + description + ", iIndex=" + iIndex + '}';
    }
        
}
