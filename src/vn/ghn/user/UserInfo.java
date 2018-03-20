package vn.ghn.user;

import com.google.gson.Gson;
import common.api.APIResponse;
import common.api.APIStatus;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import vn.ghn.warehouse.Warehouse;
import vn.ghn.warehouse.WarehouseDBService;
import vn.ghn.role.Role;
import vn.ghn.role.RoleDBService;

public class UserInfo{
    private static final Logger LOGGER = Logger.getLogger(UserInfo.class);
    
    public String id;
    public String fullname;
    public String email;
    public String phone; 
    public String facebookId;
    public String ssoId;
    public Role role;
    public List<Integer> warehouseIds;
    public String secret;
    public Integer status; 
    public Boolean isSupperUser;
    
    public static UserInfo getInstance(){
        return new UserInfo();
    }
    
    public UserInfo getByUser(User user){
        
        try {    
            this.id = user.id;
            this.email = user.email;
            this.facebookId = user.facebookId;
            this.fullname = user.fullname;
            this.phone = user.phone;                    
            this.secret = user.secret;
            this.ssoId = user.ssoId;
            this.status = user.status;     
            this.isSupperUser = user.isSupperUser;
            this.warehouseIds = user.warehouseIds;
            if(user.roleId != null){                
                APIResponse<Role> roleResult = RoleDBService.getInstance().get(user.roleId);                
                if(roleResult != null && roleResult.status == APIStatus.OK){
                    this.role = roleResult.getFirst();                    
                }
            }
            
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return this;
    }
}