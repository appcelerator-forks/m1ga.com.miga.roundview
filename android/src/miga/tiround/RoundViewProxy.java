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
    private static TiBlob imgObj = null;
    private static RoundedImageView circularImageView;
    public static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
    

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
        if (imageSrc!=null){
            Matcher m = p.matcher(imageSrc);//replace with string to compare

            if(m.find()) {                
                
                Thread thread = new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            
                            imgObj = getRemoteImage(new URL(imageSrc));  
                            loadImage(); 
                        } catch (Exception e) {
                            Log.e("round","REMOTE error");
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
        }
    }
    
    public void loadImage(){
        TiDrawableReference ref = TiDrawableReference.fromBlob(activity, imgObj);
        circularImageView.setImageBitmap(ref.getBitmap());
        circularImageView.setBorderColor(Color.parseColor(borderColor));
        if (borderWidth > 0) {
            circularImageView.setBorderWidth((float)borderWidth);
        }
        //circularImageView.setScaleType(ScaleType.CENTER_CROP);
        circularImageView.setCornerRadius((float)cornerRadius);
        circularImageView.mutateBackground(isMutated);
        // circularImageView.setImageDrawable(drawable);
        // circularImageView.setBackground(backgroundDrawable);
        circularImageView.setOval(isOval);
        // circularImageView.setTileModeX(Shader.TileMode.REPEAT);
        // circularImageView.setTileModeY(Shader.TileMode.REPEAT);
    }
    
    public TiBlob loadImageFromApplication(String imageName) {
        TiBlob result = null;
        try {
            // Load the image from the application assets
            String url = getPathToApplicationAsset(imageName);
            TiBaseFile file = TiFileFactory.createTitaniumFile(new String[] {url}, false);
            Bitmap bitmap = TiUIHelper.createBitmap(file.getInputStream());
            result = TiBlob.blobFromImage(bitmap);
        } catch (IOException e) {
            Log.e("RoundView", " EXCEPTION - IO");
        }
        return result;
    }
    
    
    private String getPathToApplicationAsset(String assetName) {
        String result = resolveUrl(null, assetName);
        return result;
    }
    
    public TiBlob getRemoteImage(final URL aURL) {
        try {
            final URLConnection conn = aURL.openConnection();
            conn.connect();
            final BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
            final Bitmap bm = BitmapFactory.decodeStream(bis);
            bis.close();
            TiBlob result = TiBlob.blobFromImage(bm);
            return result;
        } catch (IOException e) {
            Log.e("round","Error fetching url");
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
            int resId_videoHolder = -1;
            int resId_video       = -1;

            resId_videoHolder = resources.getIdentifier("layout", "layout", packageName);
            resId_video       = resources.getIdentifier("roundimage", "id", packageName);

            LayoutInflater inflater     = LayoutInflater.from(getActivity());
            videoWrapper = inflater.inflate(resId_videoHolder, null);
            circularImageView = (RoundedImageView)videoWrapper.findViewById(resId_video);
            
            openImage();
            
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
