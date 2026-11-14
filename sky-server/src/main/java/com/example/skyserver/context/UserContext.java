package com.example.skyserver.context;


public class UserContext {


    private static ThreadLocal<Long>
            threadLocal = new ThreadLocal<>();


    public static void setUserId(Long id){

        threadLocal.set(id);

    }


    public static Long getUserId(){

        return threadLocal.get();

    }


    public static void remove(){

        threadLocal.remove();

    }
}