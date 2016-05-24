/*
 * Copyright (C) 2013 4th Line GmbH, Switzerland
 *
 * The contents of this file are subject to the terms of either the GNU
 * Lesser General Public License Version 2 or later ("LGPL") or the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package org.fourthline.cling.demo.android.browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import org.fourthline.cling.android.AndroidUpnpService;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.android.FixedAndroidLogHandler;
import org.fourthline.cling.controlpoint.ActionCallback;
import org.fourthline.cling.controlpoint.SubscriptionCallback;
import org.fourthline.cling.model.UnsupportedDataException;
import org.fourthline.cling.model.action.ActionArgumentValue;
import org.fourthline.cling.model.action.ActionInvocation;
import org.fourthline.cling.model.gena.CancelReason;
import org.fourthline.cling.model.gena.GENASubscription;
import org.fourthline.cling.model.gena.RemoteGENASubscription;
import org.fourthline.cling.model.message.StreamRequestMessage;
import org.fourthline.cling.model.message.UpnpResponse;
import org.fourthline.cling.model.message.header.STAllHeader;
import org.fourthline.cling.model.message.header.UDNHeader;
import org.fourthline.cling.model.meta.*;
import org.fourthline.cling.model.state.StateVariableValue;
import org.fourthline.cling.model.types.BooleanDatatype;
import org.fourthline.cling.model.types.Datatype;
import org.fourthline.cling.model.types.UDAServiceId;
import org.fourthline.cling.registry.DefaultRegistryListener;
import org.fourthline.cling.registry.Registry;
import org.fourthline.cling.transport.Router;
import org.fourthline.cling.transport.RouterException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
//import static org.testng.Assert.assertEquals;

//import static org.testng.Assert.*;

/**
 * @author Christian Bauer
 */
// DOC:CLASS
public class BrowserActivity extends ListActivity {
//public class BrowserActivity extends Activity {

    // DOC:CLASS
    // DOC:SERVICE_BINDING
    private ArrayAdapter<DeviceDisplay> listAdapter;

    private BrowseRegistryListener registryListener = new BrowseRegistryListener();

    private AndroidUpnpService upnpService;

    private ImageView imgShow=null;

    private TextView imgPath=null;

    Service service = null;

    private Button playButton;
    private Button stopButton;
    private MediaPlayer mediaPlayer;

    private ServiceConnection serviceConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;

            // Clear the list
            listAdapter.clear();
            // Get ready for future device advertisements
            upnpService.getRegistry().addListener(registryListener);

            // Now add all devices to the list we already know about
            for (Device device : upnpService.getRegistry().getDevices()) {
                registryListener.deviceAdded(device);
                System.out.println(device.getDetails().getFriendlyName());
            }

            // Search asynchronously for all devices, they will respond soon
            upnpService.getControlPoint().search();
//            upnpService.getControlPoint().search(new STAllHeader());
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        imgPath=(TextView) findViewById(R.id.img_path);
//        imgShow=(ImageView) findViewById(R.id.imgShow);
//        setContentView(R.layout.music);

//        playButton=(Button)findViewById(R.id.playButton);
//        stopButton=(Button)findViewById(R.id.stopButton);
        // Fix the logging integration between java.util.logging and Android internal logging
        org.seamless.util.logging.LoggingUtil.resetRootHandler(
            new FixedAndroidLogHandler()
        );
        // Now you can enable logging as needed for various categories of Cling:
        // Logger.getLogger("org.fourthline.cling").setLevel(Level.FINEST);

        listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        setListAdapter(listAdapter);

        // This will start the UPnP service if it wasn't already started
        getApplicationContext().bindService(
            new Intent(this, AndroidUpnpServiceImpl.class),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (upnpService != null) {
            upnpService.getRegistry().removeListener(registryListener);
        }
        // This will stop the UPnP service if nobody else is bound to it
        getApplicationContext().unbindService(serviceConnection);
    }
    // DOC:SERVICE_BINDING

    // DOC:MENU
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, R.string.searchLAN).setIcon(android.R.drawable.ic_menu_search);
        // DOC:OPTIONAL
        menu.add(0, 1, 0, R.string.switchRouter).setIcon(android.R.drawable.ic_menu_revert);
        menu.add(0, 2, 0, R.string.toggleDebugLogging).setIcon(android.R.drawable.ic_menu_info_details);
        // DOC:OPTIONAL
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                if (upnpService == null)
                    break;
                Toast.makeText(this, R.string.searchingLAN, Toast.LENGTH_SHORT).show();
                upnpService.getRegistry().removeAllRemoteDevices();
                upnpService.getControlPoint().search();
                break;
            // DOC:OPTIONAL
            case 1:
                if (upnpService != null) {
                    Router router = upnpService.get().getRouter();
                    try {
                        if (router.isEnabled()) {
                            Toast.makeText(this, R.string.disablingRouter, Toast.LENGTH_SHORT).show();
                            router.disable();
                        } else {
                            Toast.makeText(this, R.string.enablingRouter, Toast.LENGTH_SHORT).show();
                            router.enable();
                        }
                    } catch (RouterException ex) {
                        Toast.makeText(this, getText(R.string.errorSwitchingRouter) + ex.toString(), Toast.LENGTH_LONG).show();
                        ex.printStackTrace(System.err);
                    }
                }
                break;
            case 2:
                Logger logger = Logger.getLogger("org.fourthline.cling");
                if (logger.getLevel() != null && !logger.getLevel().equals(Level.INFO)) {
                    Toast.makeText(this, R.string.disablingDebugLogging, Toast.LENGTH_SHORT).show();
                    logger.setLevel(Level.INFO);
                } else {
                    Toast.makeText(this, R.string.enablingDebugLogging, Toast.LENGTH_SHORT).show();
                    logger.setLevel(Level.FINEST);
                }
                break;
            // DOC:OPTIONAL
        }
        return false;
    }
    // DOC:MENU

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //打开选择图片
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("image/*");
        intent.setType("audio/mp3");
        intent.putExtra("crop", true);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, 2);

        //传音乐
//        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Music"+ File.separator+"Do What You Do-Cute Is What We Aim For.mp3";
//        File file = new File(path);
//        if(file.exists()) {
//            System.out.println("aaaaaaaaa");
//        }
//        ActionArgument[] a = null;
//        AlertDialog dialog = new AlertDialog.Builder(this).create();
//        dialog.setTitle(R.string.deviceDetails);
        final DeviceDisplay deviceDisplay = (DeviceDisplay)l.getItemAtPosition(position);
//        dialog.setMessage(deviceDisplay.getDetailsMessage());
//        dialog.setButton(
//                getString(R.string.OK),
//                new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                }
//        );
//        dialog.show();
////        upnpService.getControlPoint().search(new UDNHeader(deviceDisplay.getDevice().getIdentity().getUdn()));
//        Service[] serv = deviceDisplay.getDevice().findServices();
//        for (Service s : serv) {
//            System.out.println(s.getServiceId());
//        }

        service = deviceDisplay.getDevice().findService(new UDAServiceId("MessageDisplay"));

//        Action[] acts = service.getActions();
//        for (Action ac : acts) {
//            System.out.println(ac.getName());
//        }

//        Action action = service.getAction("SetHello");
//        ActionInvocation setTargetInvocation = new ActionInvocation(action);
//        setTargetInvocation.setInput(new ActionArgumentValue(action.getInputArgument("UserName"), "fuyuda")); // Can throw InvalidValueException
//
//// Alternative:
////
//// setTargetInvocation.setInput(
////         new ActionArgumentValue(
////                 action.getInputArgument("NewTargetValue"),
////                 true
////         )
//// );
//        ActionCallback setTargetCallback = new ActionCallback(setTargetInvocation) {
//
//            @Override
//            public void success(ActionInvocation invocation) {
//                ActionArgumentValue[] output = invocation.getOutput();
////                assertEquals(output.length, 0);
//            }
//
//            @Override
//            public void failure(ActionInvocation invocation,
//                                UpnpResponse operation,
//                                String defaultMsg) {
//                System.err.println(defaultMsg);
//            }
//        };
//
//        upnpService.getControlPoint().execute(setTargetCallback);

//        TextView textView = (TextView) dialog.findViewById(android.R.id.message);
//        textView.setTextSize(12);
//        super.onListItemClick(l, v, position, id);
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {        //此处的 RESULT_OK 是系统自定义得一个常量
            Log.e("TAG->onresult", "ActivityResult resultCode error");
            return;
        }
        Bitmap bm = null;

        //外界的程序访问ContentProvider所提供数据 可以通过ContentResolver接口

        ContentResolver resolver = getContentResolver();

        //此处的用于判断接收的Activity是不是你想要的那个
        if (requestCode == 2) {
            try {
                /**
                 * 发送消息
                 */
                /*Action action = service.getAction("SetHello");
                ActionInvocation setTargetInvocation = new ActionInvocation(action);
                setTargetInvocation.setInput("UserName", "fuyuda");*/
                Uri originalUri = data.getData();        //获得图片的uri
//                bm = MediaStore.Images.Media.getBitmap(resolver, originalUri);

//                saveMyBitmap("demoPic.jpg",bm);

                String path = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/Music" +File.separator+"Cry on my Shoulder.mp3";
                File file = new File(path);
                Action action = service.getAction("SetMusic");
                ActionInvocation setTargetInvocation = new ActionInvocation(action);
                setTargetInvocation.setInput("music", File2byte(file));


                ActionCallback setTargetCallback = new ActionCallback(setTargetInvocation) {

                    @Override
                    public void success(ActionInvocation invocation) {
                        ActionArgumentValue[] output = invocation.getOutput();
//                assertEquals(output.length, 0);
                    }

                    @Override
                    public void failure(ActionInvocation invocation,
                                        UpnpResponse operation,
                                        String defaultMsg) {
                        System.err.println(defaultMsg);
                    }
                };

                upnpService.getControlPoint().execute(setTargetCallback);

            } catch (Exception e) {
                Log.e("TAG-->Error", e.toString());
            }
        }
    }

    protected class BrowseRegistryListener extends DefaultRegistryListener {

        /* Discovery performance optimization for very slow Android devices! */
        @Override
        public void remoteDeviceDiscoveryStarted(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceDiscoveryFailed(Registry registry, final RemoteDevice device, final Exception ex) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(
                        BrowserActivity.this,
                        "Discovery failed of '" + device.getDisplayString() + "': "
                            + (ex != null ? ex.toString() : "Couldn't retrieve device/service descriptors"),
                        Toast.LENGTH_LONG
                    ).show();
                }
            });
            deviceRemoved(device);
        }
        /* End of optimization, you can remove the whole block if your Android handset is fast (>= 600 Mhz) */

        @Override
        public void remoteDeviceAdded(Registry registry, RemoteDevice device) {
            deviceAdded(device);
        }

        @Override
        public void remoteDeviceRemoved(Registry registry, RemoteDevice device) {
            deviceRemoved(device);
        }

        @Override
        public void localDeviceAdded(Registry registry, LocalDevice device) {
            deviceAdded(device);
        }

        @Override
        public void localDeviceRemoved(Registry registry, LocalDevice device) {
            deviceRemoved(device);
        }

        public void deviceAdded(final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    DeviceDisplay d = new DeviceDisplay(device);
                    int position = listAdapter.getPosition(d);
                    if (position >= 0) {
                        // Device already in the list, re-set new value at same position
                        listAdapter.remove(d);
                        listAdapter.insert(d, position);
                    } else {
                        listAdapter.add(d);
                    }
                }
            });
        }

        public void deviceRemoved(final Device device) {
            runOnUiThread(new Runnable() {
                public void run() {
                    listAdapter.remove(new DeviceDisplay(device));
                }
            });
        }
    }

    protected class DeviceDisplay {

        Device device;

        public DeviceDisplay(Device device) {
            this.device = device;
        }

        public Device getDevice() {
            return device;
        }

        // DOC:DETAILS
        public String getDetailsMessage() {
            StringBuilder sb = new StringBuilder();
            if (getDevice().isFullyHydrated()) {
                sb.append(getDevice().getDisplayString());
                sb.append("\n\n");
                for (Service service : getDevice().getServices()) {
                    sb.append(service.getServiceType()).append("\n");
                }
            } else {
                sb.append(getString(R.string.deviceDetailsNotYetAvailable));
            }
            return sb.toString();
        }
        // DOC:DETAILS

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeviceDisplay that = (DeviceDisplay) o;
            return device.equals(that.device);
        }

        @Override
        public int hashCode() {
            return device.hashCode();
        }

        @Override
        public String toString() {
            String name =
                getDevice().getDetails() != null && getDevice().getDetails().getFriendlyName() != null
                    ? getDevice().getDetails().getFriendlyName()
                    : getDevice().getDisplayString();
            // Display a little star while the device is being loaded (see performance optimization earlier)
            return device.isFullyHydrated() ? name : name + " *";
        }
    }

    /**
     * 图片转成string
     *
     * @param bitmap
     * @return
     */
    public static byte[] convertIconToString(Bitmap bitmap)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// outputstream
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] appicon = baos.toByteArray();// 转为byte数组
        return appicon;
//        return Base64.encodeToString(appicon, Base64.DEFAULT);

    }

    //将图像保存到SD卡中
//    public void saveMyBitmap(String bitName,Bitmap mBitmap){
//        File f = new File("/sdcard/" + bitName + ".png");
//        try {
//            f.createNewFile();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//        }
//        FileOutputStream fOut = null;
//        try {
//            fOut = new FileOutputStream(f);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
//        try {
//            fOut.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        try {
//            fOut.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public static byte[] File2byte(File file)
    {
        byte[] buffer = null;
        try
        {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1)
            {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return buffer;
    }

    // DOC:CLASS_END
    // ...
}
// DOC:CLASS_END
