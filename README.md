### 动态代理

#### 一、 动态代理实现方式

Java中的动态代理给我们提供一种动态生成类的方式，有很好的灵活性，这种技术一般会出现在一些第三方框架中，来降低接入方的使用成本。以下为常用的实现动态代理的几种方式：

1. JDK自带的Proxy方式

   优点：JDK亲儿子；无依赖；使用简单

   缺点：代理类必须继承至少一个接口；无法继承已有父类

2. asm方式，基于class字节码的操作

   优点：很底层到操作，性能高，对性能要求苛刻的建议使用

   ​缺点：使用成本高，要熟悉JVM汇编指令

3. javassist方式，基于class字节码的操作

   优点：Api简单，通熟易懂，使用成本低

   缺点：性能相对偏低

4. cglib方式，这个是基于ASM的

   ​优点：Api简单；高性能；高灵活性；支持继承父类；可以不用实现接口

   缺点：这个真的很强大，个人感觉比JDK自带的要强大很多，一定要说的话只能说使用这个需要加jar包依赖

#### 二、业务开发的尴尬

开门见山，我们直接来看一个业务场景：

```java
package com.puke.net;

/**
 * @author zijiao
 * @version 16/8/19
 */
public class User {

    public String username;
    public String uId;
    public String sex;
    public String address;

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", uId='" + uId + '\'' +
                ", sex='" + sex + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
```

这里就是一个用户信息的Entity类，不解释了。

```java
package com.puke.net;

import com.google.gson.Gson;

import java.util.Map;

/**
 * @author zijiao
 * @version 16/8/19
 */
public class VirtualHelper {

    private static final Gson sGson = new Gson();

    public static String request(String url, Map<String, Object> params) {
        if (params != null) {
            if ("123".equals(params.get("username"))
                    && "456".equals(params.get("password"))) {
                User user = new User();
                user.address = "杭州";
                user.sex = "男";
                user.uId = "Id";
                user.username = "啊啊";
                return sGson.toJson(user);
            }
        }
        return null;
    }

}
```

这里我们模拟一个简单的网络请求。

当我们业务场景需要调用网络请求执行登录操作的时候，会这样写：

```java
package com.puke.net;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zijiao
 * @version 16/8/19
 */
public class UserApi {

    private static final String API_LOGIN = "http://***.***.***";
    private static final Gson sGson = new Gson();

    public static User login(String username, String password) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        String response = VirtualHelper.request(API_LOGIN, params);
        //注，这里只是为了举例说明一下，就假设此时的数据结构就是跟User一致的
        return sGson.fromJson(response, User.class);
    }

}
```

那么，有什么问题呢。其实如果只是一个单单的login方法很难直观的反馈出来问题所在，很多时候我们为了去验证一个事物的合理性我们不妨去开始极端遐想一下这种情况：现在UserApi中又多了register方法，query方法，getToken方法，validate方法......甚至接下来一个UserApi已经满足不了我们了，我们开始有GoodsApi，OrderApi，MessageApi等等等等，试想一下N个Api的类，每个Api类都有N个类似于上面login的这种方法，而实际情况下我们request的入参还远远不止username，password两个这么简单。当我们业务场景扩大的时候，这些都是我们势必要面对的。这是一个问题，那能不能去以一种更优雅的方式去解决从而简化业务方的代码量并且降低使用成本呢。这里便引入了我们的动态代理~

#### 三、 动态代理的方式去解决

我们的目标是状态是这样的：让业务方写写接口，加加注解配置，就可以直接使用

##### 1. 先定义两个支持配置的注解

```java
package com.puke.net.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zijiao
 * @version 16/8/19
 */
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface URL {
    String value();
}
```

```java
package com.puke.net.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author zijiao
 * @version 16/8/19
 */
@Inherited
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@interface Param {
    String value();
}
```

这里不过多说明，就是两个可配字符串的注解。

##### 2. 定义一套接口

这一步，其实不是必需的。我们完全可以在动态代理中直接显式的调用VirtualHelper类，但既然抽象出来网络Api这块，那就干脆定义一套接口出来来解耦具体实现。

```java
package com.puke.net.proxy;

import java.util.Map;

/**
 * @author zijiao
 * @version 16/8/19
 */
public interface IRequest {

    String url();

    Map<String, Object> params();

    Class<?> responseCls();

}
```

```java
package com.puke.net.proxy;

import java.util.Map;

/**
 * @author zijiao
 * @version 16/8/19
 */
public class Request implements IRequest {

    String url;
    Map<String, Object> params;
    Class<?> responseCls;

    public Request(String url, Map<String, Object> params, Class<?> responseCls) {
        this.url = url;
        this.params = params;
        this.responseCls = responseCls;
    }

    @Override
    public String url() {
        return url;
    }

    @Override
    public Map<String, Object> params() {
        return params;
    }

    @Override
    public Class<?> responseCls() {
        return responseCls;
    }

}
```

这是请求接口和实现类，粗略写下，比较简单

```java
package com.puke.net.proxy;

/**
 * @author zijiao
 * @version 16/8/19
 */
public interface INetExecutor {

    <T> T execute(IRequest request);

}
```

```
package com.puke.net.proxy;

import com.google.gson.Gson;
import com.puke.net.VirtualHelper;

/**
 * @author zijiao
 * @version 16/8/19
 */
@SuppressWarnings("unchecked")
public class DefaultNetExecutor implements INetExecutor {

    private static final Gson sGson = new Gson();

    @Override
    public <T> T execute(IRequest request) {
        String response = VirtualHelper.request(request.url(), request.params());
        return (T) sGson.fromJson(response, request.responseCls());
    }

}
```

这个是网络执行器已经默认的实现方式（使用上面的VirtualHelper），主要是抽象出接口可以让业务方自主定制真正的执行操作。

##### 3. 动态代理器

这个就直接上代码了

```java
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
```

到此，我们的动态代理的编码部分就结束了。我们可以看一下ApiGenerator这个类有个sApiCache的静态变量，他缓存了动态代理生成的对象，这里这样做还是很有必要的，防止重复创建Api的代理类造成额外的性能消耗。

##### 4. 使用姿势

业务方只需要按照我们约束的一套标准来写一个interface即可

```java
package com.puke.dynamicproxy;

import com.puke.net.User;
import com.puke.net.proxy.Param;
import com.puke.net.proxy.URL;

/**
 * @author zijiao
 * @version 16/8/19
 */
@URL("http://***.***.***")
public interface LoginApi {

    User login(@Param("username") String username,
               @Param("password") String password);

}
```

这个是业务方要写的接口，以及对应的一些注解配置。

接下来就可以直接使用LoginApi生成的具体实例了

```java
package com.puke.dynamicproxy;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.puke.net.User;
import com.puke.net.proxy.ApiGenerator;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginApi loginApi = ApiGenerator.generateApi(LoginApi.class);
                User user = loginApi.login("123", "456");
                Toast.makeText(MainActivity.this, user.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
```

由于此处模拟的网络请求，就不考虑主线程的进行这个操作了。

##### 5. 使用对比

这里我再贴一下两种使用前后的代码对比

```java
public class UserApi {

    private static final String API_LOGIN = "http://***.***.***";
    private static final Gson sGson = new Gson();

    public static User login(String username, String password) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);
        String response = VirtualHelper.request(API_LOGIN, params);
        //注，这里只是为了举例说明一下，就假设此时的数据结构就是跟User一致的
        return sGson.fromJson(response, User.class);
    }

}
```

这个是传统的Api方式下业务方要写的代码

```java
@URL("http://***.***.***")
public interface LoginApi {
    User login(@Param("username") String username,
               @Param("password") String password);
}
```

这个是使用了动态代理之后业务方要写的代码

对比一下，明显能感觉到我们的代码精简了一大圈，看上去清晰明了~

这里我们再回归到最近开始提到的问题，当业务逐渐扩大的时候，这两种模式下，无论是开发效率上还是代码精简度上根本不具有可比性。

#### 四、 一些想法

其实，基于动态代理我们还可以做很多事情，当某一类事物有一些共性，我们一直重复去写一堆“孪生”代码，不仅降低了我们的开发效率，还容易让我产生一种思维定式，按照一个固有的模式去重复做一类事太容易固化我们的思维。不仅仅是动态代理，还有很多很多，开发本该是件轻松的事。工欲善其事，必先利其器。