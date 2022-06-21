package me.ductran.controllermapper;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.os.IBinder;
//import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.Log;
//import android.view.IWindowManager;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.MotionEvent;

public class MyInputManager {
    private static final String TAG = "MyIM";

    private static int INJECT_INPUT_EVENT_MODE_ASYNC;
    private static int INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT;
    private static int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH;

    public void getIM(Context c)
    {
        Object mInputManager = c.getSystemService(Context.INPUT_SERVICE);

        try
        {
            //printDeclaredMethods(mInputManager.getClass());

            //Unveil hidden methods
            Method mInjectEventMethod = mInputManager.getClass().getDeclaredMethod("injectInputEvent", new Class[] { InputEvent.class, Integer.TYPE });
            mInjectEventMethod.setAccessible(true);
            Field eventAsync = mInputManager.getClass().getDeclaredField("INJECT_INPUT_EVENT_MODE_ASYNC");
            Field eventResult = mInputManager.getClass().getDeclaredField("INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT");
            Field eventFinish = mInputManager.getClass().getDeclaredField("INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH");
            eventAsync.setAccessible(true);
            eventResult.setAccessible(true);
            eventFinish.setAccessible(true);
            INJECT_INPUT_EVENT_MODE_ASYNC = eventAsync.getInt(mInputManager.getClass());
            INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT = eventResult.getInt(mInputManager.getClass());
            INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = eventFinish.getInt(mInputManager.getClass());

            Log.d(TAG, "Sucessed");
        }
        catch (NoSuchMethodException nsme)
        {
            Log.e(TAG,  "Critical methods not available");
        }
        catch (NoSuchFieldException nsfe)
        {
            Log.e(TAG,  "Critical fields not available");
        }
        catch (IllegalAccessException iae)
        {
            Log.e(TAG,  "Critical fields not accessable");
        }
    }
}
