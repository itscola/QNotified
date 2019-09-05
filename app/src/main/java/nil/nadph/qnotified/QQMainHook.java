package nil.nadph.qnotified;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import nil.nadph.qnotified.record.ConfigManager;
import nil.nadph.qnotified.pk.FriendChunk;
import nil.nadph.qnotified.ui.DebugDrawable;
import nil.nadph.qnotified.util.ClazzExplorer;
import nil.nadph.qnotified.util.Initiator;
import nil.nadph.qnotified.util.QThemeKit;
import nil.nadph.qnotified.util.Utils;

import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.util.HashSet;

import static android.widget.LinearLayout.LayoutParams.MATCH_PARENT;
import static android.widget.LinearLayout.LayoutParams.WRAP_CONTENT;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static nil.nadph.qnotified.ActProxyMgr.*;
import static nil.nadph.qnotified.util.Initiator.load;
import static nil.nadph.qnotified.util.Utils.*;

/*TitleKit:Lcom/tencent/mobileqq/widget/navbar/NavBarCommon*/


public class QQMainHook<SlideDetectListView extends ViewGroup> implements IXposedHookLoadPackage {

    public static final int VIEW_ID_DELETED_FRIEND = 0x00EE77AA;


    public static final String QN_FULL_TAG = "qn_full_tag";
    public HashSet addedListView = new HashSet();
    private boolean __state_mini_app_hidden = false;


    XC_LoadPackage.LoadPackageParam lpparam;

    public static WeakReference<Activity> splashActivityRef;

    TextView exfriend;
    public static WeakReference<TextView> redDotRef;

    /*XC_MethodHook.Unhook[] unhook=new XC_MethodHook.Unhook[3];*/

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam _lpparam) throws Throwable {
        try {
            this.lpparam = _lpparam;
            XC_MethodHook startup = new XC_MethodHook(51) {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        FileInputStream fin = new FileInputStream("/proc/" + android.os.Process.myPid() + "/cmdline");
                        byte[] b = new byte[64];
                        int len = fin.read(b, 0, b.length);
                        fin.close();
                        Activity a;
                        String procName = new String(b, 0, len).trim();
                        //XposedBridge.log(procName);
                        if (procName.endsWith(":peak")) return;
                        if (procName.endsWith(":qzone")) return;
                        if (procName.endsWith(":tool")) return;
                        if (procName.endsWith(":MSF")) return;
                        Utils.checkLogFlag();
                        Context ctx = null;
                        Class clz = param.thisObject.getClass().getClassLoader().loadClass("com.tencent.common.app.BaseApplicationImpl");
                        Field f = hasField(clz, "sApplication");
                        if (f == null) ctx = (Context) sget_object(clz, "a", clz);
                        else ctx = (Context) f.get(null);
                        performHook(ctx.getClassLoader());
                    } catch (Throwable e) {
                        log(e);
                        throw e;
                    }
                }
            };
			/*XposedHelpers.
			 /*findAndHookMethodIfExists("com.tencent.mobileqq.app.InjectUtils",lpparam.classLoader,"injectExtraDexes",Application.class,boolean.class,startup);
			 findAndHookMethodIfExists("com.tencent.mobileqq.app.InjectUtils",lpparam.classLoader,"a",Application.class,boolean.class,startup);*/
            Class loadDex = lpparam.classLoader.loadClass("com.tencent.mobileqq.startup.step.LoadDex");
            Method[] ms = loadDex.getDeclaredMethods();
            Method m = null;
            for (int i = 0; i < ms.length; i++) {
                //log(""+ms[i].getReturnType());
                if (ms[i].getReturnType().equals(boolean.class) && ms[i].getParameterTypes().length == 0) {
                    m = ms[i];
                    break;
                }
            }
            XposedBridge.hookMethod(m, startup);
            findAndHookMethodIfExists("com.tencent.common.app.QFixApplicationImpl", lpparam.classLoader, "isAndroidNPatchEnable", XC_MethodReplacement.returnConstant(500, false));
        } catch (Throwable e) {
            log(e);
            throw e;
        }
    }

    /*
     private class SearchEntrance implements Runnable,TextWatcher,View.OnClickListener{

     View.OnClickListener mOriginalOnClickListener;
     int id;
     @Override
     public void onClick(View v){
     log("onClick");
     mOriginalOnClickListener.onClick(v);
     EditText t=splashActivity.findViewById(id);
     //t.addTextChangedListener(this);
     }

     EditText et=null;
     EditText ptet=null;
     ScrollView l;
     @Override
     public void run(){
     if(et==null){
     id=splashActivity.getResources().getIdentifier("et_search_keyword","id",splashActivity.getPackageName());
     //log("id="+id);
     for(int i=0;i<10;i++){
     try{
     Thread.sleep(1000);
     if((et=splashActivity.findViewById(id))!=null){
     splashActivity.runOnUiThread(this);
     //log(et.toString());
     return;
     }
     }catch(InterruptedException e){}
     }
     }else{
     //log(et.getClass().toString());
     mOriginalOnClickListener=Utils.getOnClickListener(et);
     et.setOnClickListener(this);
     }
     }

     @Override
     public void beforeTextChanged(CharSequence s,int start,int count,int after){
     // to: Implement this method
     }

     @Override
     public void onTextChanged(CharSequence s,int start,int before,int count){
     // TO DO: Implement this method
     }

     @Override
     public void afterTextChanged(Editable s){
     log(s.toString());
     if(s.toString().toLowerCase().equals("#ex")){
     startProxyActivity(splashActivity,ACTION_EXFRIEND_LIST);
     }
     }
     }


     */
    private void performHook(ClassLoader classLoader) {
        if (Utils.DEBUG) {
            if ("true".equals(System.getProperty(QN_FULL_TAG))) {
                log("Err:QNotified reloaded??");
                System.exit(-1);
                //QNotified updated(in HookLoader mode),kill QQ to make user restart it.
            }
            System.setProperty(QN_FULL_TAG, "true");
        }
        Initiator.init(classLoader);
        log("Clases init done");
        log("App:" + Utils.getApplication());
        assert classLoader != null : "ERROR:classLoader==null";
		/*try{
		 Thread.sleep(5000);
		 }catch(InterruptedException e){}*
		 try{
		 findAndHookMethod(load("com.tencent.mobileqq.activity.SplashActivity"),"doOnCreate",Bundle.class,new XC_MethodHook(200){
		 @Override
		 protected void afterHookedMethod(MethodHookParam param){
		 splashActivity=(Activity)param.thisObject;
		 new Thread(new SearchEntrance()).start();
		 }
		 });
		 }catch(Exception e){}*/
        findAndHookMethod(load("com.tencent.mobileqq.data.MessageForQQWalletMsg"), "doParse", new XC_MethodHook(200) {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedHelpers.setObjectField(param.thisObject, "isread", true);
                int istroop = (Integer) iget_object(param.thisObject, "istroop");
                if (istroop != 1) return;
                String frienduin = (String) iget_object(param.thisObject, "frienduin");
                long troop = Long.parseLong(frienduin);

                Field[] fs = param.thisObject.getClass().getFields();
                String ret = "";
                for (int i = 0; i < fs.length; i++) {
                    fs[i].setAccessible(true);
                    if (Modifier.isFinal(fs[i].getModifiers())) continue;
                    ret += (i < fs.length - 1 ? "├" : "↓") + fs[i].getName() + "=" + ClazzExplorer.en_toStr(fs[i].get(param.thisObject)) + "\n";
                }
                android.util.Log.i("QNdump", ret);
                //dump(param.thisObect);
            }
        });



		/*XposedBridge.hookAllMethods(load("com/tencent/mobileqq/util/FaceDecoder"),"a",
		 });*/

        Class clazz;// = load(".activity.contacts.fragment.FriendFragment");//".activity.Contacts");
		/*findAndHookMethod(clazz,"i",pastEntry);
		 findAndHookMethod(clazz,"j",pastEntry);*/
        findAndHookMethod(load("com/tencent/widget/PinnedHeaderExpandableListView"), "setAdapter", ExpandableListAdapter.class, exfriendEntryHook);
        //log("Will load");
        clazz = load(ActProxyMgr.STUB_ACTIVITY);
        //log(""+clazz);
        if (clazz != null) {
            ActProxyMgr mgr = ActProxyMgr.getInstance();
            findAndHookMethod(clazz, "onCreate", Bundle.class, mgr);
            //findAndHookMethod(clazz,"doOnCreate",Bundle.class,proxyActivity_doOnCreate);
            findAndHookMethodIfExists(clazz, "doOnDestroy", mgr);
            findAndHookMethodIfExists(clazz, "onActivityResult", int.class, int.class, Intent.class, mgr);
            findAndHookMethodIfExists(clazz, "doOnPause", mgr);
            findAndHookMethodIfExists(clazz, "doOnResume", mgr);
            findAndHookMethodIfExists(clazz, "isWrapContent", mgr);
        }
        XposedHelpers.findAndHookMethod(load("com/tencent/mobileqq/activity/SplashActivity"), "doOnResume", new XC_MethodHook(700) {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    if (Utils.getLongAccountUin() > 10000) {
                        ExfriendManager ex = ExfriendManager.getCurrent();
                        ex.timeToUpdateFl();
                    }
                } catch (Throwable e) {
                    log(e);
                    throw e;
                }
            }
        });

		/*findAndHookMethod("com.tencent.mobileqq.activity.contact.newfriend.NewFriendActivity",classLoader,"doOnCreate",android.os.Bundle.class,new XC_MethodHook(200){
		 @Override
		 protected void afterHookedMethod(MethodHookParam param){
		 log("NewFriendActivity->doOnCreate");
		 ClazzExplorer ce=ClazzExplorer.get();
		 ce.rootEle=ce.currEle=param.thisObject;
		 ce.track.removeAllElements();
		 ce.init((Activity)param.thisObject);
		 }
		 });
		 XC_MethodHook.Unhook unh=findAndHookMethod(Classes.Contacts,"o",new XC_MethodHook(200) {
		 @Override
		 protected void afterHookedMethod(MethodHookParam param) throws Throwable{
		 RelativeLayout newFriendEntry = getObject(param.thisObject,View.class,"a");
		 //View createTroopEntry = getObject(param.thisObject,View.class,"b");
		 //View searchBox = ((LinearLayout) (newFriendEntry.getParent())).getChildAt(0);
		 // 搜索框
		 View tag=newFriendEntry.getChildAt(0);
		 Utils.ref_setText(tag,"故旧-新欢");
		 //log("Setting secondary element...");

		 }
		 });*/


		/*
		 findAndHookMethod(load("friendlist/DelFriendReq"),"readFrom",load("com/qq/taf/jce/JceInputStream"),invokeRecord);
		 *
		 findAndHookMethod(load("friendlist/DelFriendReq"),"writeTo",load("com/qq/taf/jce/JceOutputStream"),new XC_MethodHook(200){
		 @Override
		 protected void beforeHookedMethod(MethodHookParam param) throws Throwable{
		 splashActivity.runOnUiThread(new Runnable(){
		 @Override
		 public void run(){
		 try{
		 Utils.showToast(Utils.getApplication(),Utils.TOAST_TYPE_ERROR,"拒绝访问: 非法操作",0);
		 }catch(Throwable e){
		 log(e);
		 }
		 }
		 });
		 param.setThrowable(new IOException("Permission denied"));
		 }
		 });

		 /*findAndHookMethod(load("friendlist/DelFriendResp"),"readFrom",load("com/qq/taf/jce/JceInputStream"),invokeRecord);
		 findAndHookMethod(load("friendlist/DelFriendResp"),"writeTo",load("com/qq/taf/jce/JceOutputStream"),invokeRecord);
		 *
		 findAndHookMethod(load("friendlist/GetFriendListReq"),"writeTo",load("com/qq/taf/jce/JceOutputStream"),invokeRecord);

		 findAndHookMethod(load("com/tencent/mobileqq/service/friendlist/FriendListService"),"n",load("com/tencent/qphone/base/remote/ToServiceMsg"),load("com/qq/jce/wup/UniPacket"),invokeRecord);

		 // XposedBridge.hookAllConstructors
		 XposedHelpers.findAndHookConstructor(load("com/tencent/mobileqq/activity/fling/FlingGestureHandler"),Activity.class,invokeRecord);
		 findAndHookMethod(load("com/tencent/mobileqq/activity/fling/FlingGestureHandler"),"a",invokeRecord);
		 findAndHookMethod(load("com/tencent/mobileqq/activity/fling/FlingHandler"),"onStart",invokeRecord);
		 */

        /*findAndHookMethod(load("friendlist/GetFriendListResp"),"readFrom",load("com/qq/taf/jce/JceInputStream"),invokeRecord);
         */


        findAndHookMethod(load("friendlist/GetFriendListResp"), "readFrom", load("com/qq/taf/jce/JceInputStream"), new XC_MethodHook(200) {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    FriendChunk fc = new FriendChunk(param.thisObject);
                    ExfriendManager.onGetFriendListResp(fc);
						/*String ret="dump object:"+param.thisObject.getClass().getCanonicalName()+"\n";
						 Field[] fs=param.thisObject.getClass().getDeclaredFields();
						 for(int i=0;i<fs.length;i++){
						 fs[i].setAccessible(true);
						 ret+=(i<fs.length-1?"├":"└")+fs[i].getName()+"="+ClazzExplorer.en_toStr(fs[i].get(param.thisObject))+"\n";
						 }
						 log(ret);*/
                } catch (Throwable e) {
                    log(e);
                    throw e;
                }
            }
        });

        findAndHookMethod(load("friendlist/DelFriendResp"), "readFrom", load("com/qq/taf/jce/JceInputStream"), new XC_MethodHook(200) {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    long uin = (Long) iget_object(param.thisObject, "uin");
                    long deluin = (Long) iget_object(param.thisObject, "deluin");
                    int result = (Integer) iget_object(param.thisObject, "result");
                    short errorCode = (Short) iget_object(param.thisObject, "errorCode");
                    if (result == 0 && errorCode == 0) ExfriendManager.get(uin).markActiveDelete(deluin);
						/*String ret="dump object:"+param.thisObject.getClass().getCanonicalName()+"\n";
						 Field[] fs=param.thisObject.getClass().getDeclaredFields();
						 for(int i=0;i<fs.length;i++){
						 fs[i].setAccessible(true);
						 ret+=(i<fs.length-1?"├":"└")+fs[i].getName()+"="+ClazzExplorer.en_toStr(fs[i].get(param.thisObject))+"\n";
						 }
						 log(ret);*/
                } catch (Throwable e) {
                    log(e);
                    throw e;
                }
            }
        });

        try {
            ConfigManager cfg = ConfigManager.getDefault();
            if (cfg.getBooleanOrFalse(qn_hide_msg_list_miniapp)) {
                int lastVersion = cfg.getIntOrDefault("qn_hide_msg_list_miniapp_version_code", 0);
                if (getQQVersionCode(getApplication()) == lastVersion) {
                    String methodName = cfg.getString("qn_hide_msg_list_miniapp_method_name");
                    findAndHookMethod(load("com/tencent/mobileqq/activity/Conversation"), methodName, XC_MethodReplacement.returnConstant(null));
                } else {
                    findAndHookMethod(load("mqq/os/MqqHandler"), "dispatchMessage", Message.class, new XC_MethodReplacement(51) {
                        @Override
                        protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                            try {
                                Method m = (Method) param.method;
                                m.setAccessible(true);
                                XposedBridge.invokeOriginalMethod(m, param.thisObject, param.args);
                            } catch (UnsupportedOperationException e) {
                            } catch (Throwable t) {
                                log(t);
                            }
                            return null;
                        }
                    });
                    Class miniapp = load("com/tencent/mobileqq/mini/entry/MiniAppEntryAdapter");
                    if (miniapp == null)
                        miniapp = load("com/tencent/mobileqq/mini/entry/MiniAppEntryAdapter$1").getDeclaredField("this$0").getType();
                    XposedBridge.hookAllConstructors(miniapp, new XC_MethodHook(60) {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            String methodName = null;
                            StackTraceElement[] stacks = new Throwable().getStackTrace();
                            for (int i = 0; i < stacks.length; i++) {
                                if (stacks[i].getClassName().indexOf("Conversation") != -1) {
                                    methodName = stacks[i].getMethodName();
                                    break;
                                }
                            }
                            if (methodName == null)
                                throw new NullPointerException("Failed to get Conversation.?() to hide MiniApp!");
                            ConfigManager cfg = ConfigManager.getDefault();
                            cfg.putString("qn_hide_msg_list_miniapp_method_name", methodName);
                            cfg.getAllConfig().put("qn_hide_msg_list_miniapp_version_code", getQQVersionCode(getApplication()));
                            cfg.save();
                            param.setThrowable(new UnsupportedOperationException("MiniAppEntry disabled"));
                        }
                    });
                }
            }
        } catch (Exception e) {
            log(e);
        }

        log("will fuck setting2");
        XposedHelpers.findAndHookMethod(load("com.tencent.mobileqq.activity.QQSettingSettingActivity"), "doOnCreate", Bundle.class, new XC_MethodHook(47) {
            @Override
            protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                try {
                    View itemRef = (View) iget_object(param.thisObject, "a", load("com/tencent/mobileqq/widget/FormSimpleItem"));
                    View item = (View) new_instance(itemRef.getClass(), param.thisObject, Context.class);
                    invoke_virtual(item, "setLeftText", "QNotified", CharSequence.class);
                    invoke_virtual(item, "setRightText", Utils.QN_VERSION_NAME, CharSequence.class);
                    LinearLayout list = (LinearLayout) itemRef.getParent();
                    list.addView(item, 0, itemRef.getLayoutParams());
                    item.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startProxyActivity((Context) param.thisObject, ACTION_ADV_SETTINGS);
                        }
                    });
                } catch (Throwable e) {
                    log(e);
                    throw e;
                }
            }
        });

        try {
            Class cl_BaseBubbleBuilder = load("com.tencent.mobileqq.activity.aio.BaseBubbleBuilder");
            Class cl_ChatMessage = load("com.tencent.mobileqq.data.ChatMessage");
            Class cl_BaseChatItemLayout = load("com.tencent.mobileqq.activity.aio.BaseChatItemLayout");
            assert cl_BaseBubbleBuilder != null;
            assert cl_ChatMessage != null;
            assert cl_BaseChatItemLayout != null;
            Method[] ms = cl_BaseBubbleBuilder.getDeclaredMethods();
            Method m = null;
            Class[] argt;
            for (int i = 0; i < ms.length; i++) {
                argt = ms[i].getParameterTypes();
                if (argt.length != 6) continue;
                if (argt[0].equals(cl_ChatMessage) && argt[1].equals(Context.class)
                        && argt[2].equals(cl_BaseChatItemLayout) && argt[4].equals(int.class)
                        && argt[5].equals(int.class)) {
                    m = ms[i];
                }
            }
            XposedBridge.hookMethod(m, new XC_MethodHook(51) {
                private static final int R_ID_BB_LAYOUT = 0x300AFF41;
                private static final int R_ID_BB_TEXTVIEW = 0x300AFF42;

                @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    if (ConfigManager.getDefault().getBooleanOrFalse(qn_send_card_msg)) {
                        final Object msgObj = methodHookParam.args[0];
                        ViewGroup viewGroup = (ViewGroup) methodHookParam.args[2];
                        if (!load("com.tencent.mobileqq.data.MessageForStructing").isAssignableFrom(msgObj.getClass())
                                && !load("com.tencent.mobileqq.data.MessageForArkApp").isAssignableFrom(msgObj.getClass()))
                            return;
                        if (viewGroup.findViewById(R_ID_BB_LAYOUT) == null) {
                            Context context = viewGroup.getContext();
                            LinearLayout linearLayout = new LinearLayout(context);
                            linearLayout.setId(R_ID_BB_LAYOUT);
                            //linearLayout.setBackground(new DebugDrawable(context));//SimpleBgDrawable(0x00000000, Color.BLUE, dip2px(context, 1)));
                            linearLayout.setOrientation(LinearLayout.VERTICAL);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                            lp.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                            TextView textView = new TextView(context);
                            textView.setId(R_ID_BB_TEXTVIEW);
                            textView.setGravity(Gravity.CENTER);
                            textView.setTextColor(Color.BLUE);
                            textView.setText("长按复制");
                            linearLayout.addView(textView, lp);
                            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
                            rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            //rlp.addRule(RelativeLayout.ALIGN_PARENT_TO
                            int i = dip2px(context, 2);
                            rlp.setMargins(i, i, i, i);
                            linearLayout.hashCode();
                            viewGroup.addView(linearLayout, rlp);
                            //iput_object(viewGroup,"DEBUG_DRAW",true);
                        }
                        ((TextView) viewGroup.findViewById(R_ID_BB_TEXTVIEW)).setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                try {
                                    ClipboardManager clipboardManager = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                    if (load("com.tencent.mobileqq.data.MessageForStructing").isAssignableFrom(msgObj.getClass())) {
                                        clipboardManager.setText((String) invoke_virtual(iget_object(msgObj, "structingMsg"), "getXml", new Object[0]));
                                    } else if (load("com.tencent.mobileqq.data.MessageForArkApp").isAssignableFrom(msgObj.getClass())) {
                                        clipboardManager.setText((String) invoke_virtual(iget_object(msgObj, "ark_app_message"), "toAppXml", new Object[0]));
                                    }
                                    showToast(view.getContext(), TOAST_TYPE_INFO, "复制成功", Toast.LENGTH_SHORT);
                                } catch (Throwable th) {
                                }
                                return true;
                            }
                        });
                        viewGroup.setBackgroundDrawable(new DebugDrawable(viewGroup.getContext()));
                    }
                }
            });
        } catch (Throwable e) {
            log(e);
        }
		/*
        try {
            Method m = null;
            Method[] methods = load("com.tencent.mobileqq.activity.BaseChatPie").getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                if (method.getName().equals("e") && method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE) {
                    m = methods[i];
                    break;
                }
            }
            assert m != null;
            XposedBridge.hookMethod(m, new XC_MethodHook(51) {
                @Override
                public void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                    if (ConfigManager.get().getBooleanOrFalse(qn_send_card_msg)) {
                        final Object qqi = iget_object(methodHookParam.thisObject, "a", load("com.tencent.mobileqq.app.QQAppInterface"));
                        final Object session = iget_object(methodHookParam.thisObject, "a", load("com.tencent.mobileqq.activity.aio.SessionInfo"));
                        final ViewGroup viewGroup = (ViewGroup) iget_object(methodHookParam.thisObject, "d", Class.forName("android.view.ViewGroup"));
                        Resources res = viewGroup.getContext().getResources();
                        int id_btn = res.getIdentifier("fun_btn", "id", null);
                        final int id_et = res.getIdentifier("input", "id", null);
                        if (viewGroup != null)
                            ((Button) viewGroup.findViewById(id_btn).setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View view) {
                                    EditText edit = (EditText) viewGroup.findViewById(id_et);
                                    String input = edit.getText().toString();
                                    boolean success = false;
                                    Class cl_msgMgr = load((String) Hook.config.get("MessageManager"));
                                    try {
                                        Object msg = invoke_static(load((String) Hook.config.get("TestStructMsg")), "a", input, load("com.tencent.mobileqq.structmsg.AbsStructMsg"));
                                        if (msg != null) {
                                            invoke_static(cl_msgMgr, "a", qqi, session, msg);
                                            success = true;
                                        }
                                    } catch (Throwable th) {
                                        Toast.makeText(view.getContext(), th.toString(), Toast.LENGTH_SHORT).show();
                                        XposedBridge.log(th);
                                    }
                                    try {
                                        Object arkMsg = new_instance(load("com.tencent.mobileqq.data.ArkAppMessage"));
                                        if ((Boolean) invoke_virtual(arkMsg, "fromAppXml", input)) {
                                            invoke_static(cl_msgMgr, "a", qqi, session, arkMsg);
                                            success = true;
                                        }
                                    } catch (Throwable th2) {
                                        XposedBridge.log(th2);
                                    }
                                    if (success) edit.setText("");
                                    return false;
                                }
                            }));


                    }
                }
            });
        } catch (Throwable e) {
            log(e);
        }

		/*XposedBridge.hookAllMethods(load("aydf"),"a",invokeRecord);
		 XposedBridge.hookAllMethods(load("aydf"),"b",invokeRecord);
		 XposedBridge.hookAllMethods(load("aydu"),"a",invokeRecord);/*new XC_MethodHook(60){
		 *
		 @Override
		 protected void beforeHookedMethod(MethodHookParam param){

		 }
		 });



		 /*clazz=load("com.tencent.mobileqq.activity.Conversation");
		 if(clazz!=null){
		 try{
		 findAndHookMethod(clazz,"F",new XC_MethodHook(60){
		 @Override
		 protected void beforeHookedMethod(MethodHookParam param){
		 param.setResult(null);
		 }
		 });

		 }catch(Exception e){}
		 }

		 /*
		 findAndHookMethod(load("friendlist/DelFriendReq"),"writeTo",load("com/qq/taf/jce/JceOutputStream"),new XC_MethodHook(70){
		 @Override
		 protected void beforeHookedMethod(MethodHookParam param) throws Throwable{
		 Field f=param.thisObject.getClass().getDeclaredField("delType");
		 f.setAccessible(true);
		 f.set(param.thisObject,(byte)2);
		 }
		 });

		 /*
		 XposedBridge.hookMethod(XposedHelpers.findMethodBestMatch(load("com/tencent/mobileqq/activity/UncommonlyUsedContactsActivity"),"finish",new Class[]{}),invokeRecord);
		 //findAndHookMethod(load("friendlist/AddFriendReq"),"writeTo",load("com/qq/taf/jce/JceOutputStream"),invokeRecord);
		 /*findAndHookMethod(load("friendlist/AddFriendReq"),"writeTo",load("com/qq/taf/jce/JceOutputStream"),new XC_MethodHook(10){
		 @Override
		 protected void beforeHookedMethod(MethodHookParam param) throws Throwable{
		 Field f=param.thisObject.getClass().getDeclaredField("sourceSubID");
		 f.setAccessible(true);
		 f.set(param.thisObject,1);
		 f=param.thisObject.getClass().getDeclaredField("sourceID");
		 f.setAccessible(true);
		 f.set(param.thisObject,3071);
		 f=param.thisObject.getClass().getDeclaredField("myfriendgroupid");
		 f.setAccessible(true);
		 f.set(param.thisObject,(byte)0);
		 /*f=param.thisObject.getClass().getDeclaredField("adduinsetting");
		 f.setAccessible(true);
		 f.set(param.thisObject,4);*

		 }
		 });//*/
    }

    public XC_MethodHook invokeRecord = new XC_MethodHook(200) {
        @Override
        protected void afterHookedMethod(MethodHookParam param) throws IllegalAccessException, IllegalArgumentException {
            Member m = param.method;
            String ret = m.getDeclaringClass().getSimpleName() + "->" + ((m instanceof Method) ? m.getName() : "<init>") + "(";
            Class[] argt;
            if (m instanceof Method)
                argt = ((Method) m).getParameterTypes();
            else if (m instanceof Constructor)
                argt = ((Constructor) m).getParameterTypes();
            else argt = new Class[0];
            for (int i = 0; i < argt.length; i++) {
                if (i != 0) ret += ",\n";
                ret += param.args[i];
            }
            ret += ")=" + param.getResult();
            Utils.log(ret);
            ret = "↑dump object:" + m.getDeclaringClass().getCanonicalName() + "\n";
            Field[] fs = m.getDeclaringClass().getDeclaredFields();
            for (int i = 0; i < fs.length; i++) {
                fs[i].setAccessible(true);
                ret += (i < fs.length - 1 ? "├" : "↓") + fs[i].getName() + "=" + ClazzExplorer.en_toStr(fs[i].get(param.thisObject)) + "\n";
            }
            log(ret);
            Utils.dumpTrace();
        }
    };

    public static XC_MethodHook.Unhook findAndHookMethodIfExists(Class<?> clazz, String methodName, Object...
            parameterTypesAndCallback) {
        try {
            return findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
        } catch (Throwable e) {
            log(e.toString());
            return null;
        }
    }

    public static XC_MethodHook.Unhook findAndHookMethodIfExists(String clazzName, ClassLoader cl, String
            methodName, Object... parameterTypesAndCallback) {
        try {
            return findAndHookMethod(clazzName, cl, methodName, parameterTypesAndCallback);
        } catch (Throwable e) {
            log(e.toString());
            return null;
        }
    }

    public static void startProxyActivity(Context ctx, int action) {
        Intent intent = new Intent(ctx, load(ActProxyMgr.STUB_ACTIVITY));
        int id = ActProxyMgr.next();
        intent.putExtra(ACTIVITY_PROXY_ID_TAG, id);
        intent.putExtra(ACTIVITY_PROXY_ACTION, action);
        intent.putExtra("fling_action_key", 2);
        intent.putExtra("fling_code_key", ctx.hashCode());
        ctx.startActivity(intent);
    }

    public static void openProfileCard(Context ctx, long uin) {
        try {
            Parcelable allInOne = (Parcelable) new_instance(load("com/tencent/mobileqq/activity/ProfileActivity$AllInOne"), "" + uin, 35, String.class, int.class);
            Intent intent = new Intent(ctx, load("com/tencent/mobileqq/activity/FriendProfileCardActivity"));
            intent.putExtra("AllInOne", allInOne);
            ctx.startActivity(intent);
        } catch (Exception e) {
            log(e);
        }
    }

	/*
	 private XC_MethodHook proxyActivity_doOnCreate=new XC_MethodHook(200){
	 @Override
	 protected void beforeHookedMethod(MethodHookParam param) throws Throwable{
	 if(ActProxyMgr.isInfiniteLoop())return;
	 Activity self=(Activity)param.thisObject;

	 int id=self.getIntent().getExtras().getInt(ACTIVITY_PROXY_ID_TAG,-1);
	 if(id<=0)return;
	 ActProxyMgr.set(id,self);
	 Method m=self.getClass().getSuperclass().getDeclaredMethod("doOnCreate",Bundle.class);
	 m.setAccessible(true);
	 try{
	 ActProxyMgr.invokeSuper(self,m,param.args);
	 }catch(ActProxyMgr.BreakUnaughtException e){}
	 param.setResult(true);
	 //log("***doOnCreate");
	 try{
	 QThemeKit.initTheme(self);
	 LinearLayout ll=
	 QQViewBuilder.initCustomCommenTitleL(self,"返回","历史好友","清空");
	 //TextView tv=new TextView(self);
	 //QThemeKit.ThemeStruct theme=QThemeKit.getCurrentTheme(splashActivity);
	 /*tv.setText("Hello,QQ!");
	 tv.setTextColor(theme.skin_text_black);
	 tv.setBackgroundColor(theme.qq_setting_item_bg_pre);*
	 //ll.setBackgroundColor(0);
	 ll.setBackgroundColor(QThemeKit.qq_setting_item_bg_nor.getDefaultColor());
	 //ll.setBackgroundTintMode(PorterDuff.Mode.DARKEN);
	 //tv.setBackgroundColor(0xFF000000);
	 //ll.addView(tv);
	 //tv.setBackgroundResource(0);
	 SlideDetectListView sdlv=(SlideDetectListView)QConst.load("com.tencent.mobileqq.widget.SlideDetectListView").getConstructor(Context.class,AttributeSet.class).newInstance(self,null);
	 invoke_virtual(sdlv,"setCanSlide",true,boolean.class);
	 ViewGroup.LayoutParams lp=new ViewGroup.LayoutParams(MATCH_PARENT,MATCH_PARENT);
	 //sdlv.setBackgroundColor(0xFFAA0000);
	 ll.addView(sdlv,lp);
	 //invoke_virtual(sdlv,"addHeaderView",tv,null,false,View.class,Object.class,boolean.class);
	 invoke_virtual(sdlv,"setDivider",null,Drawable.class);
	 QQViewBuilder.listView_setAdapter(sdlv,new ExfriendListAdapter(sdlv));

	 }catch(Throwable e){
	 log(e);
	 }
	 }
	 };*/

    /*private XC_MethodHook pastEntry=new XC_MethodHook(1200){

     @Override
     protected void afterHookedMethod(MethodHookParam param) throws Throwable{
     try{

     */
    private XC_MethodHook exfriendEntryHook = new XC_MethodHook(1200) {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            try {
                boolean hide = false;
                try {
                    hide = ConfigManager.getDefault().getBooleanOrFalse("qn_hide_ex_entry_group");
                } catch (Throwable e) {
                    log(e);
                }
                if (hide) return;
                if (!param.thisObject.getClass().getName().contains("ContactsFPSPinnedHeaderExpandableListView"))
                    return;
                LinearLayout layout_entrance;
                android.widget.FrameLayout frameView;
                View lv = (View) param.thisObject;
                //frameView=Utils.getObject(,View.class,"b");
                final Activity splashActivity = (Activity) Utils.getContext(lv);
                QThemeKit.initTheme(splashActivity);
                //lv=(ContactsFPSPinnedHeaderExpandableListView) iget_object(param.thisObject,"a",load("com/tencent/mobileqq/activity/contacts/view/ContactsFPSPinnedHeaderExpandableListView"));
                //log("Fuckee:"+lv.getClass());
                TextView unusualContacts;
				/*if(frameView.getChildAt(0) instanceof LinearLayout){
				 if(frameView.getVisibility()==View.GONE){
				 /*兼容QQ净化->隐藏不常用联系人,上面的1200也是一样*
				 frameView.setVisibility(View.VISIBLE);
				 if(unusualContacts!=null)unusualContacts.setVisibility(View.GONE);
				 }
				 return;
				 }*/
                //unusualContacts=(TextView)frameView.getChildAt(0);

                layout_entrance = new LinearLayout(splashActivity);
                RelativeLayout rell = new RelativeLayout(splashActivity);
                //rell.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT,WRAP_CONTENT));

                //Object adapter=invoke_virtual(lv,"getAdapter",ListAdapter.class);
                //invoke_virtual(lv,"setAdapter",null,BaseAdapter.class);
				/*try{
				 invoke_virtual(lv,"removeFooterView",layout,View.class);
				 }catch(Exception e){log(e);}
				 */
                if (!addedListView.contains(lv)) {
                    //log("fucking it!");
                    invoke_virtual_original(lv, "addFooterView", layout_entrance, View.class);
                    addedListView.add(lv);
                    //invoke_static(XposedBridge.class,"dumpObjectNative",lv,Object.class);
                    //lv.setVisibility(View.GONE);
                }

                //invoke_virtual(lv,"setAdapter",adapter,BaseAdapter.class);

                layout_entrance.setOrientation(LinearLayout.VERTICAL);

                //StateListDrawable background=(StateListDrawable)unusualContacts.getBackground();

                exfriend = new TextView(splashActivity);
                exfriend.setTextColor(QThemeKit.skin_blue);//unusualContacts.getTextColors());//QThemeKit.skin_red);
                //exfriend.setBackground(Utils._obj_clone(background.mutate()));//damn! mutate() not working!
                exfriend.setTextSize(dip2sp(splashActivity, 17));//TypedValue.COMPLEX_UNIT_PX,unusualContacts.getTextSize());
                exfriend.setId(VIEW_ID_DELETED_FRIEND);
                exfriend.setText("历史好友");
                exfriend.setGravity(Gravity.CENTER);
                exfriend.setClickable(true);
                //exfriend.setTranslationY(-Utils.dip2px(splashActivity,1f));
                //unusualContacts.setVisibility(frameView.getVisibility()==View.GONE?View.GONE:View.VISIBLE);
                //frameView.setVisibility(View.VISIBLE);

                TextView redDot = new TextView(splashActivity);
                redDotRef = new WeakReference<>(redDot);
                redDot.setTextColor(0xFFFF0000);

                redDot.setGravity(Gravity.CENTER);
                //redDot.setBackground(QThemeKit.skin_tips_newmessage);
                redDot.getPaint().setFakeBoldText(true);
                //redDot.setTextAppearance(android.R.style.TextAppearance_Small);
                redDot.setTextSize(Utils.dip2sp(splashActivity, 10));
                //redDot.setPadding(4,0,4,0);
                try {
                    invoke_static(load("com/tencent/widget/CustomWidgetUtil"), "a", redDot, 3, 1, 0, TextView.class, int.class, int.class, int.class, void.class);
                } catch (NullPointerException e) {
                    redDot.setTextColor(Color.RED);
                }
                ExfriendManager.get(Utils.getLongAccountUin()).setRedDot();


                //frameView.removeAllViews();
                int height = dip2px(splashActivity, 48);//unusualContacts.getLayoutParams().height;
                //layout.addView(unusualContacts);
                RelativeLayout.LayoutParams exlp = new RelativeLayout.LayoutParams(MATCH_PARENT, height);
                exlp.topMargin = 0;
                exlp.leftMargin = 0;

                rell.addView(exfriend, exlp);
                RelativeLayout.LayoutParams dotlp = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                dotlp.topMargin = 0;
                dotlp.rightMargin = Utils.dip2px(splashActivity, 24);
                dotlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                dotlp.addRule(RelativeLayout.CENTER_VERTICAL);
                rell.addView(redDot, dotlp);
                layout_entrance.addView(rell);//,unusualContacts.getLayoutParams());
                ViewGroup.LayoutParams llp = new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
                layout_entrance.setPadding(0, (int) (height * 0.3f), 0, (int) (0.3f * height));
				/*frameView.addView(layout,llp);
				 ViewGroup.LayoutParams _lp=frameView.getLayoutParams();
				 _lp.height=WRAP_CONTENT;//(int)(unusual.getLayoutParams().height*());
				 final View.OnClickListener olds=Utils.getOnClickListener(frameView);
				 frameView.setOnTouchListener(null);
				 frameView.setClickable(false);
				 //unusualContacts_old.setOnTouchListener(null);*/
                exfriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(splashActivity, load(ActProxyMgr.STUB_ACTIVITY));
                        int id = ActProxyMgr.next();
                        intent.putExtra(ACTIVITY_PROXY_ID_TAG, id);
                        intent.putExtra(ACTIVITY_PROXY_ACTION, ACTION_EXFRIEND_LIST);
                        splashActivity.startActivity(intent);
                        //Toast.makeText(splashActivity,"Test",0).show();
                    }
                });
				/*unusualContacts.setOnClickListener(new View.OnClickListener(){
				 @Override
				 public void onClick(View v){
				 olds.onClick(frameView);
				 }
				 });
				 unusualContacts.invalidate();*/
                exfriend.postInvalidate();
				/*new Thread(new Runnable(){
				 @Override
				 public void run(){
				 try{
				 Thread.sleep(500);
				 }catch(InterruptedException e){}
				 exfriend.postInvalidate();
				 unusual.postInvalidate();
				 }
				 }).start();*/

                //log("[End of putting entrance]");
            } catch (Throwable e) {
                log(e);
                throw e;
            }
        }

    };


}
