package vn.ghn.user;

import com.google.gson.Gson;
import common.api.APIModel;
import java.util.List;

public class User extends APIModel<String>{
    
    public List<Integer> warehouseIds;
    public String email;
    public String password;
    public String fullname;
    public String phone;
    public String facebookId;
    public String ssoId;
    public String roleId;
    public String secret;
    public Integer status;
    public Boolean isSupperUser;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
