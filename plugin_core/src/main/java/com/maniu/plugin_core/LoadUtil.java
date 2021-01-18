package com.maniu.plugin_core;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;
import dalvik.system.PathClassLoader;

/**
 * 加载插件？根据存储路径去加载插件
 */
public class LoadUtil {
    private String apkPath;
    private PackageInfo packageInfo;

    /**
     * 这个方法就是用来去加载插件的
     * @param context
     */
    public void loadClass(Context context,String apkPath) {
        this.apkPath = apkPath;
        if (context == null) {
            return;
        }
        try {
            //第一步  先获取到宿主的dexElements
            PathClassLoader classLoader = (PathClassLoader) context.getClassLoader();
            //获取到BasDexClassloader
            Class<?> baseDexClassloaderClazz = Class.forName("dalvik.system.BaseDexClassLoader");
            //获取到它里面的成员变量pathList
            Field pathListField = baseDexClassloaderClazz.getDeclaredField("pathList");
            pathListField.setAccessible(true);
            //获取到pathList在宿主类加载器中的值
            Object pathListValue = pathListField.get(classLoader);
            //获取到这个pathList中的dexElements
            Field dexElementsField = pathListValue.getClass().getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);
            //获取到dexElements在当前宿主类加载器中的值
            Object dexElementsValue = dexElementsField.get(pathListValue);


            //第二步  加载插件  然后去获取插件的类加载器中的dexElements
            DexClassLoader dexClassLoader = new DexClassLoader(apkPath,
                    context.getCacheDir().getAbsolutePath(), null, context.getClassLoader());
            //获取到插件的pathList
            Object pluginPathListValue = pathListField.get(dexClassLoader);
            //获取到插件的dexElements
            Object pluginDexElementsValue = dexElementsField.get(pluginPathListValue);

            //第三步  合并数组
            //获取到两个数组的长度
            int myLength = Array.getLength(dexElementsValue);
            int pluginLength = Array.getLength(pluginDexElementsValue);
            int newLength = myLength + pluginLength;
            //获取到数组的类型
            Class<?> componentType = dexElementsValue.getClass().getComponentType();
            //创建新数组
            Object newArray = Array.newInstance(componentType, newLength);
            System.arraycopy(dexElementsValue, 0, newArray, 0, myLength);
            System.arraycopy(pluginDexElementsValue, 0, newArray, myLength, pluginLength);

            //将新数组  赋值给宿主的类加载器
            dexElementsField.set(pathListValue, newArray);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 创建一个资源对象  让它能够管理到到插件的资源的方法
     * @return
     */
    public Resources loadPluginResource(Context context){
        Resources resources = null;
        try {
            //获取到插件的包信息类
            //获取到包管理器
            PackageManager packageManager = context.getPackageManager();
            packageInfo = packageManager.getPackageArchiveInfo(
                    apkPath, PackageManager.GET_ACTIVITIES);
            AssetManager assetManager = AssetManager.class.newInstance();
            //通过反射获取到addAssetPath
            Method addAssetPath = assetManager.getClass().getDeclaredMethod("addAssetPath", String.class);
            addAssetPath.setAccessible(true);
            //执行这个方法
            addAssetPath.invoke(assetManager,apkPath);
            resources = new Resources(assetManager,context.getResources().getDisplayMetrics(),
                    context.getResources().getConfiguration());
            return resources;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public PackageInfo getPackageInfo() {
        return packageInfo;
    }


    //    /**
//     * 将插件先移动到私有路径下面
//     * @param context
//     */
//    public static void copyOnPrivateFile(Context context){
//        //将这个修复包先移动到一个安全的位置-私有路径 /data/datd/com.maniu.mn_2020_12_15/
//        File odex = context.getDir("plugin", Context.MODE_PRIVATE);
//        //定义补丁的名字
//        String name = "plugin.apk";
//        //获取到补丁复制过去的完整File对象 /data/datd/com.maniu.mn_2020_12_15/odex/out.dex
//        File file = new File(odex.getAbsoluteFile(),name);
//        //复制过去的时候  要先判断一下  目的地是否已经存在这个名字的文件
//        if(file.exists()){
//            // 删除重名的
//            file.delete();
//        }
//        String filePath = file.getAbsolutePath();
//        //创建文件输入输出流
//        FileInputStream is =null;
//        FileOutputStream os = null;
//        try {
//            //起点
//            is = new FileInputStream(new File(Environment.getExternalStorageDirectory(), name));
//            //终点
//            os = new FileOutputStream(filePath);
//            int len = 0;
//            byte[] buffer = new byte[1024];
//            while ((len = is.read(buffer)) != -1) {
//                os.write(buffer, 0, len);
//            }
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } finally {
//            try {
//                os.close();
//                is.close();
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }
//    }
}
