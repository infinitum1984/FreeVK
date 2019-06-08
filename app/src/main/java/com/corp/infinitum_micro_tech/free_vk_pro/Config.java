package com.corp.infinitum_micro_tech.free_vk_pro;

import android.content.Context;

public class Config {

    public static String UserHost="";
    public static int UserPort;

    public static boolean SetProxy;
    public static boolean EnableSwipeRefresh;
    public static boolean Oauth;


    private final static String set_proxy_key="is_set_Proxy";
    private final static String enable_swipe_refresh_key="Is_refresh_page_down_swipe";
    private final static String oauth_key="oauth_is";
    private final static String user_host_key="Host";
    private final static String user_port_key="Port";

    public static void ConfigInit(Context c){
        initUserProxy(c);
        initEnableSwipeRefresh(c);
        initSetProxyState(c);
        initOauth(c);
    }

    //SetProxy bool
    public static void saveSetProxyState(Context c, boolean b){
        SharedPreferencesData.save_data(c,set_proxy_key,b);
        SetProxy=b;
    }
    public static void initSetProxyState(Context c){
        SetProxy=SharedPreferencesData.get_data(c,set_proxy_key,true);
    }

    //EnableSwipeRefresh
    public static void saveEnableSwipeRefresh(Context c, boolean b){
        SharedPreferencesData.save_data(c,enable_swipe_refresh_key,b);
        EnableSwipeRefresh=b;
    }
    public static void initEnableSwipeRefresh(Context c){
        EnableSwipeRefresh=SharedPreferencesData.get_data(c,enable_swipe_refresh_key,true);
    }

    //OAUTH
    public static void  initOauth(Context c){
        Oauth=SharedPreferencesData.get_data(c,oauth_key,false);

    }
    public static void saveOauth(Context c, boolean b){
        SharedPreferencesData.save_data(c,oauth_key,b);
        Oauth=b;
    }
    //User Proxy

    public static void initUserProxy(Context c){
        UserHost = SharedPreferencesData.get_data(c,user_host_key,"");
        UserPort=SharedPreferencesData.get_data(c,user_port_key,0);
    }
    public static void saveUserProxy(Context c, ProxyData proxyData){
        SharedPreferencesData.save_data(c,user_host_key,proxyData.Host);
        SharedPreferencesData.save_data(c,user_port_key,proxyData.Port);
    }
    public static void  deleteUserProxy(Context c){
        SharedPreferencesData.delete_data(c,user_host_key);
        SharedPreferencesData.delete_data(c,user_port_key);
        SharedPreferencesData.delete_data(c,set_proxy_key);

    }



}
