/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.ghn.user;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.model.MongoCounter;
import com.mongodb.model.MongoModel;
import org.bson.Document;

/**
 *
 * @author Bee
 */
public class UserDBService extends MongoModel<User>{
    
    private static String collectionName = "user";

    public UserDBService() {
        super("User",User.class);
    }

    @Override
    public void initDB(MongoDatabase db) {           
        
        try {
            db.createCollection(collectionName);
        } catch (Exception ex) {
            //LOGGER.error(ex.getMessage(), ex);
        }

        try {
            MongoCollection<Document> col = db.getCollection(collectionName);
            col.createIndex(new Document("email", 1));
            col.createIndex(new Document("fullname", 1));
            col.createIndex(new Document("sso_id", 1));
            this.setCollection(col).setCounter(new MongoCounter(db.getCollection("counter")));
        } catch (Exception ex) {
            //LOGGER.error(ex.getMessage(), ex);
        }
    }
    
    public static UserDBService getInstance() {
        return UserDBServiceHolder.INSTANCE;
    }
    
    private static class UserDBServiceHolder {
        private static final UserDBService INSTANCE = new UserDBService();
    }
    
}
