package org.achartengine.renderer;

import android.content.Context;

public class OverlayRenderer extends XYSeriesRenderer {
    private int[] overlayData;
    private int[] resourceData;
    private Context context;
    
    public void setOverlayData(Context ctx, int[] data, int[] resources){
      context = ctx;
      overlayData = data;
      resourceData = resources;
    }
    
    public Context getContext(){
      return context;
    }
    
    public int[] getOverlayData(){
      return overlayData;
    }
    
    public int[] getResources(){
      return resourceData;
    }
}
