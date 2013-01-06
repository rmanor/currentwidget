package com.manor.currentwidget.library;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.media.AudioManager;
import android.os.PowerManager;

public class Compatibility {
	private static Method method_isScreenOn;
	//private static PowerManager powerManager;	
	
	private static Method method_isWiredHeadsetOn;
	
	static {
		initCompatibility();
	};

	private static void initCompatibility() {
		try {
			method_isScreenOn = PowerManager.class.getMethod(
					"isScreenOn", new Class[]{} );
			//System.out.println("isScreenOn: supported	");

		} catch (NoSuchMethodException nsme) {
			/* failure, must be older device */
			//	   System.out.println("isScreenOn: NOT supported");
			method_isScreenOn = null;
		} catch (Exception e) {
			method_isScreenOn = null;
		}      
		
		try {
			method_isWiredHeadsetOn = AudioManager.class.getMethod("isWiredHeadsetOn", new Class[]{});
			//Log.d("CurrentWidget", "isWiredHeadsetOn supported");
		}
		catch (Exception e) {
			method_isWiredHeadsetOn = null;
		}
	}
	
	public static boolean isScreenOn(Context main) {
	
		if (method_isScreenOn != null) {
			try {
				PowerManager powerManager = (PowerManager)main.getSystemService(
						Context.POWER_SERVICE);


				Boolean b = (Boolean)(method_isScreenOn.invoke(powerManager,(Object[])null));

				return b;
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {

			}

		}

		return true;
	}
	
	public static boolean isWiredHeadsetOn(Context context) {
		
		if (method_isWiredHeadsetOn != null) {
			try {
				AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
				Boolean result = (Boolean)(method_isWiredHeadsetOn.invoke(audioManager, (Object[])null));
				return result;
			}
			catch (Exception e) {
				
			}
		}
		
		return false;
	}
}