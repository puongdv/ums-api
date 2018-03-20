package vn.ghn.utils;

import common.api.APIResponder;
import common.api.APIResponse;
import common.api.APIStatus;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.log4j.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author cuongtc
 */
public class Utility {

    private static final Logger LOGGER = Logger.getLogger(Utility.class);

    private Utility() {
    }

    public static Utility getInstance() {
        return UltilityHolder.INSTANCE;
    }

    private static final List<String> ERRORS = new ArrayList<String>();
    private static final Boolean HASHERROR = false;

    public static List<String> GetErrors() {
        return ERRORS;
    }

    public static Boolean getHashError() {
        return ERRORS.isEmpty() == false;
    }

    public static List<String> SetErrors(String sError) {
        if (sError.isEmpty() == false) {
            ERRORS.add(sError);
        }
        return ERRORS;
    }

    public static Utility process(TobeExcute func) {
        try {
            if (HASHERROR == false) {
                func.func.call();
            }
        } catch (Exception ex) {
            SetErrors(ex.toString());
            LOGGER.error(ex, ex);
            func.resp.respond(new APIResponse(APIStatus.ERROR, ex.getMessage()));
        }

        return getInstance();
    }

    public static Utility process(Callable<APIResponder> func) {
        try {
            if (HASHERROR == false) {
                func.call();
            }
        } catch (Exception ex) {
            SetErrors(ex.toString());
            LOGGER.error(ex, ex);
        }

        return getInstance();
    }

    private final static class UltilityHolder {

        private final static Utility INSTANCE = new Utility();
    }

    public interface ITobeExcute {

    }

    public final class TobeExcute implements ITobeExcute {

        public TobeExcute(Callable func, APIResponder resp) {
            this.func = func;
            this.resp = resp;
        }

        public final APIResponder resp;
        public final Callable func;
    }

    public final boolean isNullOrEmpty(String str) {
        return str == null || str == "" || str.isEmpty();
    }
    
    public static String convertToUnderscore(String input) {
        String result = "";
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= 96) {
                result += String.valueOf(c);
            } else {
                result += "_" + String.valueOf(c);
            }
        }
        return result.toLowerCase();
    }
    
    public static int parseInt(String v){
        try {
            return Integer.parseInt(v);
        } catch (Exception e) {
            return 0;
        }
    }
    
    public static int[] stringArrToIntArr(String[] s) {
        int[] result = new int[s.length];
        for (int i = 0; i < s.length; i++) {
           result[i] = parseInt(s[i]);
        }
        return result;
    }
}
