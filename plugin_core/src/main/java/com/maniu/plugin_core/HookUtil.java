package com.maniu.plugin_core;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 让我们用Hook实现没有注册的Activity也能被启动
 * 1.首先要反射到AMS的实例，然后创建一个动态代理对象
 */
public class HookUtil {
    private Context context;
    //用来瞒天过海的Activity
    private Class<? extends Activity> mProxyActivityClass;
    public static final String EXTRA_ORIGIN_INTENT = "EXTRA_ORIGIN_INTENT";

    public HookUtil(Context context, Class<? extends Activity> mProxyActivityClass) {
        this.context = context;
        this.mProxyActivityClass = mProxyActivityClass;
    }

    /**
     * HOOK AMS
     * 得到AMS的实例
     * 然后生成AMS的代理对象
     * 然后拦截它的startActivity的方法
     */
    public void hookStartActivity() throws Exception{
        //首先  获取到ActivityManagerNative这个类的class对象
        Class<?> amnClass = Class.forName("android.app.ActivityManagerNative");
        //获取到ActivityManagerNative的gDefault的成员变量
        Field gDefault = amnClass.getDeclaredField("gDefault");
        gDefault.setAccessible(true);
        //获取到gDefault这个静态变量的值Singleton<IActivityManager>
        Object gDefaultValue = gDefault.get(null);

        //获取到Singleton的类对象
        Class<?> singletonClass = Class.forName("android.util.Singleton");
        //获取到的是mInstance的成员变量
        Field mInstance = singletonClass.getDeclaredField("mInstance");
        mInstance.setAccessible(true);
        //获取到AMS
        Object amsObject = mInstance.get(gDefaultValue);

        //创建AMS的代理对象
        //首先要获取到它的接口的class对象
        Class<?> IActivityManagerClass = Class.forName("android.app.IActivityManager");
        Object amsProxy = Proxy.newProxyInstance(HookUtil.class.getClassLoader(), new Class[]{IActivityManagerClass},
                new StartActvityInvocationHandler(amsObject));
        //通过反射  将代理对象替换原来的ams
        mInstance.set(gDefaultValue,amsProxy);
    }

    public class StartActvityInvocationHandler implements InvocationHandler{
        //持有要代理的对象的对象
        private Object ams;

        public StartActvityInvocationHandler(Object ams) {
            this.ams = ams;
        }

        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            if(method.getName().equals("startActivity")){
                //将startActivity方法中传过来的意图取到
                int position = 0;
                for (int x=0;x<objects.length;x++) {
                    if(objects[x] instanceof Intent){
                        position = x;
                    }
                }
                Intent oldIntent = (Intent) objects[position];
                //创建一个新的意图
                Intent newIntent = new Intent(context,mProxyActivityClass);
                //将旧的意图放入到新的意图的
                newIntent.putExtra(EXTRA_ORIGIN_INTENT,oldIntent);
                //将新的意图放入到objects里面
                objects[position] = newIntent;
            }
            //调用了AMS的startActivity的方法
            return method.invoke(ams,objects);
        }
    }

    public void hookLaunchActivity() throws Exception{
        //首先获取到ActivityThread的对象
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        //获取到这个类的实例sCurrentActivityThread;
        Field sCurrentActivityThread = activityThreadClass.getDeclaredField("sCurrentActivityThread");
        sCurrentActivityThread.setAccessible(true);
        //获取到ActviityThread的实例
        Object activityThreadValue = sCurrentActivityThread.get(null);

        //获取到mH的成员变量
        Field mHField = activityThreadClass.getDeclaredField("mH");
        mHField.setAccessible(true);
        //获取到mH在ActivityThread中的值
        Object mHValue = mHField.get(activityThreadValue);

        //获取到ActivityThread里面用来发送消息的Handler
        Class<?> handlerClass = Class.forName("android.os.Handler");
        //获取到Handler中用来处理消息的mCallBack变量
        Field mCallBackField = handlerClass.getDeclaredField("mCallback");
        mCallBackField.setAccessible(true);
        //重新赋值
        mCallBackField.set(mHValue,new HandlerCallBack());
    }


    private class HandlerCallBack implements Handler.Callback{

        @Override
        public boolean handleMessage(Message message) {
            if(message.what == 100){
                hanldeLaunchActvity(message);
            }
            return false;
        }

        private void hanldeLaunchActvity(Message message) {

            try {
                //获取到ActivityClientRecord
                Object r = message.obj;
                Field intentField = r.getClass().getDeclaredField("intent");
                intentField.setAccessible(true);
                //从r实例中将intent这个变量的值拿出来
                Intent newIntent = (Intent) intentField.get(r);
                //从这个newInteent把我们真正的intent拿出来
                Intent oldIntent = newIntent.getParcelableExtra(EXTRA_ORIGIN_INTENT);
                if(oldIntent != null){
                    //重新把旧意图设置到r中
                    intentField.set(r,oldIntent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
