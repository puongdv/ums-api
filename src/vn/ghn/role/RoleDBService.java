/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.role;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.model.MongoCounter;
import com.mongodb.model.MongoModel;
import org.bson.Document;
import vn.common.lib.cache.NLocalCache;

/**
 *
 * @author Bee
 */
public class RoleDBService extends MongoModel<Role>{
    
    private static String collectionName = "role";

    public RoleDBService() {
        super("Role",Role.class);
    }

    @Override
    public void initDB(MongoDatabase db) {
 
        // NLocalCache is a LRU cache that supports expired time
        // cache maximum 1000 object, for maximum 3600 seconds
        // this.enableCache(new NLocalCache(1000, 3600));

        try {
            db.createCollection(collectionName);
        } catch (Exception ex) {
            //LOGGER.error(ex.getMessage(), ex);
        }

        try {
            MongoCollection<Document> col = db.getCollection(collectionName);
            col.createIndex(new Document("name", 1));
            this.setCollection(col).setCounter(new MongoCounter(db.getCollection("counter")));
        } catch (Exception ex) {
            //LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    public static RoleDBService getInstance() {
        return RoleDBServiceHolder.INSTANCE;
    }
    
    private static class RoleDBServiceHolder {
        private static final RoleDBService INSTANCE = new RoleDBService();
    }
    
}
