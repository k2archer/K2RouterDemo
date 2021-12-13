package com.k2archer.lib.k2router.api;

import android.content.Intent;

import com.k2archer.lib.k2router.api.exception.RouteException;

import java.lang.reflect.Constructor;

class K2RouterProcess {
    static class ServiceProcess {
        public static Object handler(Class<? extends K2IService> clz) {
            if (clz == null) {
                return null;
            }
            K2IService provider = null;
            try {
                Constructor<?>[] constructors = clz.getConstructors();

                if (constructors.length == 0) {
                    provider = clz.newInstance();
                } else {
                    provider = clz.getConstructor().newInstance();
                }

                Constructor constructor = null;
                for (Constructor<?> item : constructors) {
                    if (item.getParameterTypes().length == 0) {
                        constructor = item;
                    }
                }
                if (constructor == null) {
                    throw new RouteException("目标类没有无参数构造函数");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return provider;
        }
    }

    static class ActivityProcess {
        public void handler() {

        }

        public static void startActivity(Class destination, Postcard postcard) {
            Intent intent = new Intent(postcard.getContext(), destination);
            if (postcard.getBundle() != null) {
                intent.putExtras(postcard.getBundle());
            }
            if (postcard.getFlag() != Postcard.FLAG_DEFAULT) {
                intent.setFlags(postcard.getFlag());
            }
            postcard.getContext().startActivity(intent);
        }


        public static void startActivityForResult(Class destination, Postcard postcard, int requestCode) {
            Intent intent = new Intent(postcard.getContext(), destination);
            if (postcard.getBundle() != null) {
                intent.putExtras(postcard.getBundle());
            }
            if (postcard.getFlag() != Postcard.FLAG_DEFAULT) {
                intent.setFlags(postcard.getFlag());
            }

            if (postcard.getBundle() != null) {
                postcard.getContext().startActivityForResult(intent, requestCode, postcard.getBundle());
            } else {
                postcard.getContext().startActivityForResult(intent, requestCode);
            }
        }
    }


}
