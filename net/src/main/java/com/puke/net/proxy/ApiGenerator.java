package com.puke.net.proxy;

import android.text.TextUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zijiao
 * @version 16/8/19
 */
@SuppressWarnings("unchecked")
public class ApiGenerator {

    private static final Map<Class, Object> sApiCache = new HashMap<>();

    private static INetExecutor sNetExecutor;

    private static class Handler<T> implements InvocationHandler {

        private Class<T> apiInterface;

        public Handler(Class<T> apiInterface) {
            this.apiInterface = apiInterface;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            IRequest request = resolveRequest(method, args, apiInterface);
            if (sNetExecutor == null) {
                sNetExecutor = defaultNetExecutor();
            }
            return sNetExecutor.execute(request);
        }
    }

    private static <T> IRequest resolveRequest(Method method, Object[] args, Class<T> apiInterface) {
        StringBuilder urlBuilder = new StringBuilder();
        Map<String, Object> params = null;
        if (apiInterface.isAnnotationPresent(URL.class)) {
            String baseUrl = apiInterface.getAnnotation(URL.class).value();
            if (!TextUtils.isEmpty(baseUrl)) {
                urlBuilder.append(baseUrl);
            }
        }
        if (method.isAnnotationPresent(URL.class)) {
            String subUrl = method.getAnnotation(URL.class).value();
            if (!TextUtils.isEmpty(subUrl)) {
                urlBuilder.append(subUrl);
            }
        }
        int index = 0;
        for (Annotation[] annotations : method.getParameterAnnotations()) {
            for (Annotation annotation : annotations) {
                if (annotation instanceof Param) {
                    String key = ((Param) annotation).value();
                    if (!TextUtils.isEmpty(key)) {
                        if (params == null) {
                            params = new HashMap<>();
                        }
                        params.put(key, args[index]);
                    }
                    break;
                }
            }
            index++;
        }
        return new Request(urlBuilder.toString(), params, method.getReturnType());
    }

    private static INetExecutor defaultNetExecutor() {
        return new DefaultNetExecutor();
    }

    public static <T> T generateApi(Class<T> apiInterface) {
        if (apiInterface == null || !apiInterface.isInterface()) {
            throw new RuntimeException("the apiInterface is null or isn`t interface.");
        }
        synchronized (ApiGenerator.class) {
            Object api = sApiCache.get(apiInterface);
            if (api == null) {
                api = Proxy.newProxyInstance(apiInterface.getClassLoader(),
                        new Class[]{apiInterface},
                        new Handler(apiInterface));
                sApiCache.put(apiInterface, api);
            }
            return (T) api;
        }
    }

    /**
     * 外部提供自定义执行器
     *
     * @param netExecutor 网络执行器
     */
    public static void setNetExecutor(INetExecutor netExecutor) {
        sNetExecutor = netExecutor;
    }

}
