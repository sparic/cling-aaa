package org.fourthline.cling.demo.android.light;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import de.greenrobot.event.EventBus;
import org.fourthline.cling.binding.annotations.*;

import java.beans.PropertyChangeSupport;
import java.io.File;

/**
 * Created by Ray.Fu on 2016/5/17.
 */
@UpnpService(
        serviceId = @UpnpServiceId("MessageDisplay"),
        serviceType = @UpnpServiceType(value = "MessageDisplay", version = 1)
)
public class MessageDisplay {

    private final PropertyChangeSupport propertyChangeSupport;

    public MessageDisplay() {
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }

    @UpnpStateVariable(defaultValue = "", sendEvents = false)
    private String hello = null;

    @UpnpStateVariable(defaultValue = "")
    private String message = null;

    @UpnpStateVariable(defaultValue = "")
    private byte[] pic = null;

    @UpnpAction
    public void setHello(@UpnpInputArgument(name = "UserName") String userName) {
        String targetOldValue = hello;
        hello = userName;
        String statusOldValue = message;
        message = userName;
        EventBus.getDefault().post(new MessageEvent(userName));
        // These have no effect on the UPnP monitoring but it's JavaBean compliant
        getPropertyChangeSupport().firePropertyChange("hello", targetOldValue, hello);
        getPropertyChangeSupport().firePropertyChange("message", statusOldValue, message);

        // This will send a UPnP event, it's the name of a state variable that sends events
        getPropertyChangeSupport().firePropertyChange("message", statusOldValue, message);
    }

    /**
     * string转成bitmap
     *
     * @param st
     */
    public static Bitmap convertStringToIcon(String st)
    {
        // OutputStream out;
        Bitmap bitmap = null;
        try
        {
            // out = new FileOutputStream("/sdcard/aa.jpg");
            byte[] bitmapArray;
            bitmapArray = Base64.decode(st, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
            // bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            return bitmap;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @UpnpAction(name = "SetPic")
    public void setPic(@UpnpInputArgument(name = "pic") byte[] pic) {
        EventBus.getDefault().post(new FileEvent(Bytes2Bimap(pic)));
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "RetHelloValue"))
    public String getHello() {
        return hello;
    }

    @UpnpAction(out = @UpnpOutputArgument(name = "ResultStatus"))
    public String getMessage() {
        return message;
    }

    public Bitmap Bytes2Bimap(byte[] b) {
        if (b.length != 0) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        } else {
            return null;
        }
    }
}