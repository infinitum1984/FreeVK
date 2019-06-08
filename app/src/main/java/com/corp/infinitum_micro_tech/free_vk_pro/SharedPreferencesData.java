package com.corp.infinitum_micro_tech.free_vk_pro;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesData {
    //save data
    public static void save_data(Context c, String name, boolean data){
        SharedPreferences sharedPreferences = c.getSharedPreferences("config", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =sharedPreferences.edit();
        editor.putBoolean(name,data);
        editor.commit();
    }
    public static void save_data(Context c, String name, String data ){
        SharedPreferences.Editor editor = c.getSharedPreferences("config", Context.MODE_PRIVATE).edit();
        editor.putString(name,data);
        editor.commit();
    }
    public static void save_data(Context c, String name, int data){
       SharedPreferences.Editor editor= c.getSharedPreferences("config", Context.MODE_PRIVATE).edit();
       editor.putInt(name,data);
       editor.commit();
    }

    //get data
    public static boolean get_data(Context c, String name, boolean def_value){
        SharedPreferences sharedPreferences = c.getSharedPreferences("config", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(name,def_value);
    }
    public static String get_data(Context c, String name, String def_value){
        SharedPreferences sharedPreferences=c.getSharedPreferences("config", Context.MODE_PRIVATE);
        return sharedPreferences.getString(name,def_value);

    }
    public static int get_data(Context c, String name, int def_value){
        SharedPreferences sharedPreferences=c.getSharedPreferences("config", Context.MODE_PRIVATE);
        return sharedPreferences.getInt(name,def_value);
    }
    //delete data
    public static void delete_data(Context c, String name){
       SharedPreferences.Editor editor= c.getSharedPreferences("config", Context.MODE_PRIVATE).edit();
       editor.remove(name);
       editor.commit();
    }
}
