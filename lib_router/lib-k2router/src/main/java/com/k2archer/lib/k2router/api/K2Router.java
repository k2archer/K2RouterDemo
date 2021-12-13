package com.k2archer.lib.k2router.api;

import android.app.Activity;
import android.app.Application;

import com.k2archer.lib.k2router.api.common.RouterConst;
import com.k2archer.lib.k2router.api.exception.RouteException;
import com.k2archer.lib.k2router.impl.K2RouterComponent;
import com.k2archer.lib.k2router.impl.K2RouterInit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.k2archer.lib.k2router.api.exception.RouteException.NONE_DESTINATION;

public class K2Router {

    private static Application mContext;

    private Map<String, Class<?>> routeMap = new HashMap<>();

    public K2Router() {
    }

    private volatile static K2Router mInstance;

    // 单例模式
    public static K2Router getInstance() {
        if (mInstance == null) {
            synchronized (K2Router.class) {
                if (mInstance == null) {
                    mInstance = new K2Router();
                }
            }
        }
        return mInstance;
    }

    public static void init(Application context) {
        mContext = context;
        try {
            K2RouterInit.init();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }

    public void add(String path, Class<?> destination) {
        routeMap.put(path, destination);
    }


    @SuppressWarnings("unchecked")
    public <T extends K2IService> T navigation(Class<? extends T> service) {
        Postcard postcard = Postcard.Builder.create()
                .setUri(service.getSimpleName()).build();

        return (T) navigation(postcard);
    }

    public Object navigation(Activity context, String path) {
        Postcard postcard = Postcard.Builder.create()
                .setContext(context)
                .setUri(path).build();

        return navigation(postcard);
    }

    @SuppressWarnings("unchecked")
    public Object navigation(Postcard postcard) {
        String path = postcard.getUri().getPath();
        Class destination = routeMap.get(path);
        if (destination == null) {
            throw new RouteException(path + NONE_DESTINATION);
        }

        Object object = null;

        if (Activity.class.isAssignableFrom(destination)) {
            K2RouterProcess.ActivityProcess.startActivity(destination, postcard);
        } else if (K2IService.class.isAssignableFrom(destination)) {
            Class<? extends K2IService> clz = (Class<? extends K2IService>) routeMap.get(path);
            object = K2RouterProcess.ServiceProcess.handler(clz);
        } else {
            throw new RouteException("目标类没有匹配");
        }

        return object;
    }

    public void navigationForActivityResult(Activity context, String path, int requestCode) {
        Postcard postcard = Postcard.Builder.create()
                .setContext(context)
                .setUri(path).build();

        navigationForActivityResult(postcard, requestCode);
    }

    public void navigationForActivityResult(Postcard postcard, int requestCode) {
        String path = postcard.getUri().getPath();
        Class destination = routeMap.get(path);
        if (destination == null) {
            throw new RouteException(path + NONE_DESTINATION);
        }

        K2RouterProcess.ActivityProcess.startActivityForResult(destination, postcard, requestCode);
    }
}
