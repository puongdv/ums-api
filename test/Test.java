
import com.google.gson.Gson;
import common.api.APIResponse;
import common.api.APIStatus;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import vn.common.lib.config.NConfig;
import vn.ghn.user.User;
import vn.ghn.user.UserDBService;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Bee
 */
public class Test {
    public static void main(String[] args) throws IOException, Exception {
        String env = System.getProperty("appenv");
        if (env == null) {
            env = "staging";
        }        
        NConfig.init("./conf/" + env + ".ini");  
        
        System.out.println(NConfig.getString("ghn-api.domain"));

//        initDB(NConfig.getString("db.host"), NConfig.getInt("db.port"), NConfig.getString("db.name"));  
//              
//        
//        
//        User query = new User();
//        query.warehouseIds = new ArrayList<>();
//        query.warehouseIds.add(7001);
//        APIResponse<User> apiResult = UserDBService.getInstance().query(query, 0, 1, true);
//        System.out.println(GSON.toJson(apiResult));
//        sync();
    }    
    
    public static List<Integer> get(List<Integer> integers){
        List<Integer> integers1 = new ArrayList<>();
        for (Integer integer : integers) {
            if(integer != 7001){
                integers1.add(integer);
            }
        }
        return integers1;
    }
    
    public static void sync(){
        try {
            User query = new User();
            query.warehouseIds = new ArrayList<>();
            query.warehouseIds.add(7001);
            System.out.println(query);
            APIResponse<User> apiResult = UserDBService.getInstance().query(query, 0, 100, true);           
            int skip = 0;
            long t2 = System.currentTimeMillis();
            while (apiResult != null && apiResult.status == APIStatus.OK && apiResult.data != null) {
                long t21 = System.currentTimeMillis();
                
                apiResult.data.parallelStream().forEach((User user) -> {
                    User userUpdate = new User();
                    userUpdate.warehouseIds = get(user.warehouseIds);
                    userUpdate.warehouseIds.add(1626);
                    System.out.println(userUpdate);
                    UserDBService.getInstance().update(user.id, userUpdate);
                });                
                skip += apiResult.data.size();
                System.out.println(skip);
                apiResult = UserDBService.getInstance().query(query, skip, 100, true); 
            }
            long t3 = System.currentTimeMillis();
            System.out.println("==============");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private final static Gson GSON = new Gson();
    private final static Logger LOGGER = Logger.getLogger(Test.class);
    
    private static void func(){
//        APIResponse<Permission> perResult = RestoreDb.getInstance().premission(0, 1);
//        System.out.println(GSON.toJson(perResult));
//        
//        APIResponse<Role> roleResult = RestoreDb.getInstance().role(0, 1);
//        System.out.println(GSON.toJson(roleResult));
//        
//        APIResponse<User> userResult = RestoreDb.getInstance().user(0, 1);
//        System.out.println(GSON.toJson(userResult));
//        
//        APIResponse<Warehouse> warehouseResult = RestoreDb.getInstance().warehouse(0, 1);
//        System.out.println(GSON.toJson(warehouseResult));
        
//        RestoreDb.getInstance().restoreWarehouse();
    }      
}
