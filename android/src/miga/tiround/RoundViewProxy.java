package com.miga.roundview;

import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.TiBlob;
import org.appcelerator.titanium.view.TiDrawableReference;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.io.TiBaseFile;
import org.appcelerator.titanium.io.TiFileFactory;
import org.appcelerator.titanium.view.TiUIView;
import org.appcelerator.titanium.proxy.TiViewProxy;
import android.app.Activity;
import org.appcelerator.titanium.TiApplication;
import android.view.LayoutInflater;
import android.view.View;
import java.io.IOException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.widget.ImageView;
import com.makeramen.roundedimageview.RoundedImageView;
import android.graphics.Color;
import android.webkit.URLUtil;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedInputStream;
import android.graphics.BitmapFactory;
import android.content.pm.PackageManager;
import android.os.Build;
import org.appcelerator.titanium.util.TiColorHelper;
import android.os.Message;

@Kroll.proxy(creatableInModule = TiRoundModule.class)
public class RoundViewProxy extends TiViewProxy {

    ImageView mVideoView;
    TiApplication appContext;
    Activity activity;
    String imageSrc;
    int borderWidth = 0;
    int cornerRadius = 0;
    String borderColor = "#000000";
    boolean isOval = false;
    boolean isMutated = true;
    private TiBlob imgObj = null;
    private RoundedImageView circularImageView;
    private String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
    

    public RoundViewProxy() {
        super();
        appContext = TiApplication.getInstance();
        activity   = appContext.getCurrentActivity();
    }

    @Override
    public TiUIView createView(Activity activity) {
        TiUIView view = new RoundView(this);
        return view;
    }
    
    @Kroll.setProperty @Kroll.method
    public void setImage(String url) {   
        imageSrc = url;
        openImage();                  
    }
    
    // Handle creation options
    @Override
    public void handleCreationArgs(KrollModule createdInModule, Object[] args) {        
        super.handleCreationArgs(createdInModule, args);
    }

    // Handle creation options
    @Override
    public void handleCreationDict(KrollDict options) {
        super.handleCreationDict(options);
        
        if (options.containsKey("image")) {
            imageSrc = options.getString("image");
        }
    
        if (options.containsKey("cornerradius")) {
            cornerRadius = options.getInt("cornerradius");
        }
        if (options.containsKey("borderwidth")) {
            borderWidth = options.getInt("borderwidth");
        }
        if (options.containsKey("bordercolor")) {
            borderColor = options.getString("bordercolor");
        }

        if (options.containsKey("isOval")) {
            isOval = options.getBoolean("isOval");
        }
        if (options.containsKey("isMutated")) {
            isMutated = options.getBoolean("isMutated");
        }
    }

    private void openImage(){
        Pattern p = Pattern.compile(URL_REGEX);
        if (imageSrc!=null && !imageSrc.isEmpty()){
            Matcher m = p.matcher(imageSrc);//replace with string to compare

            if(m.find()) {                
                
                Thread thread = new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try {                
                            imgObj = getRemoteImage(new URL(imageSrc));                              
                            if (imgObj!=null){
                                loadImage(); 
                            }
                        } catch (Exception e) {
                            Log.e("round","REMOTE error " + e);
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();        
            } else {
                imgObj = loadImageFromApplication(imageSrc);
            }

            if (imgObj!=null){
                loadImage();
            }
        } else {
            if(TiApplication.isUIThread()) {
                Bitmap.Config conf = Bitmap.Config.ARGB_8888; 
                Bitmap bmp = Bitmap.createBitmap(1,1,conf);
                circularImageView.setImageBitmap(bmp);
            } else {
                Message message = getMainHandler().obtainMessage(1);
			    message.sendToTarget();
            }
        }
    }
    
    @Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case 1:
                Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                Bitmap bmp = Bitmap.createBitmap(1,1,conf);
                circularImageView.setImageBitmap(bmp);
				return true;
		}
		return super.handleMessage(msg);
	}
    
    public void loadImage(){
        TiDrawableReference ref = TiDrawableReference.fromBlob(activity, imgObj);
        if (ref!=null){
            circularImageView.setImageBitmap(ref.getBitmap());
            circularImageView.setBorderColor(TiColorHelper.parseColor(borderColor));            
            if (borderWidth > 0) {
                circularImageView.setBorderWidth((float)borderWidth);
            }
            circularImageView.setCornerRadius((float)cornerRadius);
            circularImageView.mutateBackground(isMutated);
            circularImageView.setOval(isOval);
            // circularImageView.setScaleType(ScaleType.CENTER_CROP);
            // circularImageView.setImageDrawable(drawable);
            // circularImageView.setBackground(backgroundDrawable);
            // circularImageView.setTileModeX(Shader.TileMode.REPEAT);
            // circularImageView.setTileModeY(Shader.TileMode.REPEAT);
        }
    }
    
    public TiBlob loadImageFromApplication(String imageName) {
        TiBlob result = null;
        try {
            // Load the image from the application assets
            String url = resolveUrl(null, imageName);
            TiBaseFile file = TiFileFactory.createTitaniumFile(new String[] {url}, false);
            Bitmap bitmap = TiUIHelper.createBitmap(file.getInputStream());
            result = TiBlob.blobFromImage(bitmap);
        } catch (IOException e) {
            Log.e("RoundView", " EXCEPTION - IO");
        }
        return result;
    }

    public TiBlob getRemoteImage(final URL aURL) {
        
        if (Build.VERSION.SDK_INT >= 23) {
            int check = TiApplication.getInstance().getRootActivity().checkCallingOrSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE");
    		if (check == PackageManager.PERMISSION_GRANTED) {
                
    		} else {
    			Log.w("RoundView", "App doesn't have WRITE_EXTERNAL_STORAGE permission");			
    		}
        }
        
        try {
            final URLConnection conn = aURL.openConnection();
            conn.connect();
            final BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            final Bitmap bm = BitmapFactory.decodeStream(bis);
            bis.close();
            TiBlob result = TiBlob.blobFromImage(bm);
            return result;
        } catch (IOException e) {
            Log.e("round","Error fetching url " + e);
        }
        
        return null;
    }

    private class RoundView extends TiUIView {
        // create view
        public RoundView(final TiViewProxy proxy) {
            super(proxy);

            String packageName = proxy.getActivity().getPackageName();
            Resources resources = proxy.getActivity().getResources();
            View videoWrapper;
            int idLayout = -1;
            int idRoundview = -1;

            idLayout = resources.getIdentifier("layout", "layout", packageName);
            idRoundview = resources.getIdentifier("roundimage", "id", packageName);

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            videoWrapper = inflater.inflate(idLayout, null);
            circularImageView = (RoundedImageView)videoWrapper.findViewById(idRoundview);

            setNativeView(videoWrapper);
        }

    

        @Override
        public void processProperties(KrollDict d) {
            super.processProperties(d);
            
            if (d.containsKey("image")) {
    			imageSrc = d.getString("image");
            }
            
            if (d.containsKey("cornerradius")) {
                cornerRadius = d.getInt("cornerradius");
            }
            if (d.containsKey("borderwidth")) {
                borderWidth = d.getInt("borderwidth");
            }
            if (d.containsKey("bordercolor")) {
                borderColor = d.getString("bordercolor");
            }

            if (d.containsKey("isOval")) {
                isOval = d.getBoolean("isOval");
            }
            if (d.containsKey("isMutated")) {
                isMutated = d.getBoolean("isMutated");
            }
            openImage();

        }
        
    }
    
}
