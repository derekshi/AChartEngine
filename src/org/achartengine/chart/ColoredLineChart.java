package org.achartengine.chart;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer.FillOutsideLine;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

/**
 * ColoredLineChart is similar to CubicLineChart 
 * except it allows to color the line with multiple colors based on pos/neg values
 * i.e. You can set positive values to use show as Green vs. Red for negative values
 * @author DerekShi
 *
 */
public class ColoredLineChart extends XYChart {
//	private static final String TAG = "ColoredLineChart";
	/** The constant to identify this chart type. */
	public static final String TYPE = "ColoredLine";
	/** The legend shape width. */
	private static final int SHAPE_WIDTH = 30;
	
	private static final int POSITIVE_COLOR = Color.argb(100, 87, 181, 230);
	private static final int NEGATIVE_COLOR = Color.argb(100, 219, 93, 105);

	private PointF p2 = new PointF();
	private Path p;
	
	ColoredLineChart(){
	}
	
	public ColoredLineChart(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer) {
		super(dataset, renderer);
	}
	
	/**
	   * Sets the series and the renderer.
	   * 
	   * @param dataset the series dataset
	   * @param renderer the series renderer
	   */
	protected void setDatasetRenderer(XYMultipleSeriesDataset dataset,
			XYMultipleSeriesRenderer renderer) {
			super.setDatasetRenderer(dataset, renderer);
			
	}
	
	
	/**
	   * The graphical representation of a series.
	   * 
	   * @param canvas the canvas to paint to
	   * @param paint the paint to be used for drawing
	   * @param points the array of points to be used for drawing the series
	   * @param seriesRenderer the series renderer
	   * @param yAxisValue the minimum value of the y axis
	   * @param seriesIndex the index of the series currently being drawn
	   * @param startIndex the start index of the rendering points
	   */
	@Override
	public void drawSeries(Canvas canvas, Paint paint, List<Float> points,
	      SimpleSeriesRenderer seriesRenderer, float yAxisValue, int seriesIndex, int startIndex) {
	    XYSeriesRenderer renderer = (XYSeriesRenderer) seriesRenderer;
	    paint.setStrokeWidth(renderer.getLineWidth());

	    drawPath(canvas, points, paint, false);

  	    paint.setColor(seriesRenderer.getColor());
  	    paint.setStyle(Style.STROKE);
  	    paint.setStrokeWidth(renderer.getLineWidth());
  	    outlinePath(canvas, paint);
	    	    
        
	    if (renderer.isPointed()){
	        int l = points.size();
	        paint.setStyle(Style.FILL);
	        paint.setColor(renderer.getPointFillColor());
	        canvas.drawCircle(points.get(l-2), points.get(l-1), renderer.getPointSize(), paint);
	        paint.setStyle(Style.STROKE);
	        paint.setStrokeWidth(3f);
	        paint.setColor(seriesRenderer.getColor());
	        canvas.drawCircle(points.get(l-2), points.get(l-1), renderer.getPointSize(), paint);
	    }
	    
//	    if (renderer instanceof OverlayRenderer){
//	      OverlayRenderer xrenderer = (OverlayRenderer)renderer;
//	      int[] overlay = xrenderer.getOverlayData();
//	      int[] resources = xrenderer.getResources();
//	      for(int i=0; i<overlay.length; i++){
//	        int val = overlay[i];
//	        if (val > 0){
//	          Resources r = xrenderer.getContext().getResources();
//	          Bitmap res = BitmapFactory.decodeResource(r, resources[val]);
//	          float pointx = points.get(i*2)-20;
//	          float pointy = points.get(i*2+1)-20;
//	          canvas.drawBitmap(res, pointx, pointy, null);
//	        }
//	      }
//	    }
	}


	@Override
	  protected ClickableArea[] clickableAreasForPoints(List<Float> points, List<Double> values,
	      float yAxisValue, int seriesIndex, int startIndex) {
	    int length = points.size();
	    ClickableArea[] ret = new ClickableArea[length / 2];
	    for (int i = 0; i < length; i += 2) {
	      int selectableBuffer = mRenderer.getSelectableBuffer();
	      ret[i / 2] = new ClickableArea(new RectF(points.get(i) - selectableBuffer, points.get(i + 1)
	          - selectableBuffer, points.get(i) + selectableBuffer, points.get(i + 1)
	          + selectableBuffer), values.get(i), values.get(i + 1));
	    }
	    return ret;
	  }

	  /**
	   * Returns the legend shape width.
	   * 
	   * @param seriesIndex the series index
	   * @return the legend shape width
	   */
	  public int getLegendShapeWidth(int seriesIndex) {
	    return SHAPE_WIDTH;
	  }

	  /**
	   * The graphical representation of the legend shape.
	   * 
	   * @param canvas the canvas to paint to
	   * @param renderer the series renderer
	   * @param x the x value of the point the shape should be drawn at
	   * @param y the y value of the point the shape should be drawn at
	   * @param seriesIndex the series index
	   * @param paint the paint to be used for drawing
	   */
	  public void drawLegendShape(Canvas canvas, SimpleSeriesRenderer renderer, float x, float y,
	      int seriesIndex, Paint paint) {
	    canvas.drawLine(x, y, x + SHAPE_WIDTH, y, paint);
	  }
	  
	  @Override
	  protected void drawPath(Canvas canvas, List<Float> points, Paint paint, boolean circular) {
	    p = new Path();
	    float x = points.get(0);
	    float y = points.get(1);
	    p.moveTo(x, y);

	    int length = points.size();
	    if (circular) {
	      length -= 4;
	    }
	    List<Float> controllers1 = new ArrayList<Float>();
	    List<Float> controllers2 = new ArrayList<Float>();
	    getControlPoints(points, controllers1, controllers2);
	    
	    for (int i = 0; i < length-2; i += 2) {
	      int nextIndex = i + 2 < length ? i + 2 : i;
	      p2.set(points.get(nextIndex), points.get(nextIndex + 1));
	      p.cubicTo(controllers1.get(i), controllers1.get(i+1), controllers2.get(i), controllers2.get(i+1), p2.x, p2.y);

	    }
	    if (circular) {
	      for (int i = length; i < length + 4; i += 2) {
	        p.lineTo(points.get(i), points.get(i + 1));
	      }
	      p.lineTo(points.get(0), points.get(1));
	    }
	    canvas.save();
	    
	    fillPath(canvas, paint, points.get(1), points.get(length-2));
	  }
	  
	  private void fillPath(Canvas canvas, Paint paint, float yAxisValue, float right){
	  //add fills
        Path fillPath = new Path(p);
        fillPath.lineTo(right, yAxisValue);

        //add positive color
        canvas.clipRect(0, 0, right, yAxisValue);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(POSITIVE_COLOR);
        canvas.drawPath(fillPath, paint);
        canvas.restore();
        canvas.save();
        
        //add negative color
        canvas.clipRect(0, yAxisValue, right, yAxisValue*2);
        paint.setColor(NEGATIVE_COLOR);
        canvas.drawPath(fillPath, paint);
        canvas.restore();
        canvas.save();
	  }
	  
	  private void outlinePath(Canvas canvas, Paint paint){
	    canvas.drawPath(p, paint);
	    canvas.restore();
	  }
	  
	  private float[] buildControlPoints(float[] rhs, int n){
	    float[] x = new float[n];
	    float[] tmp = new float[n];
	    float b = 2f;
	    x[0] = rhs[0]/b;
	    for(int i=1; i<n; i++){ //decomposition and forward subsitition
	      tmp[i] = 1/b;
	      b = (i < n-1 ? 4f : 3.5f) - tmp[i];
	      x[i] = (rhs[i] - x[i-1])/b;
	    }
	    
	    for(int i=1; i<n; i++){
	      x[n-i-1] -= tmp[n-i]*x[n-i]; //backsubstitution
	    }
	    return x;
	  }
	  
	  private void getControlPoints(List<Float> points, List<Float> controller1, List<Float> controller2){
	    int n = points.size()-2;
	    if (n<=2) return;
	    float[] knots = new float[points.size()];
	    int k=0;
	    for (Float val: points){
	      knots[k++] = val;
	    }
	    //x value
	    float[] rhs = new float[n/2];
	    for(int i=1; i<n/2-1; ++i){
	      rhs[i] = 4*knots[i*2] + 2*knots[i*2+2];	    
	    }
	    rhs[0] = knots[0] + 2*knots[2];
	    rhs[n/2-1] = (8*knots[n-2] + knots[n])/2;
	    float[] x = buildControlPoints(rhs, n/2);
	    //y value
	    for(int i=1; i<n/2-1; ++i){
	      rhs[i] = 4*knots[i*2+1] + 2*knots[i*2+3];
	    }
	    rhs[0] = knots[1] + 2*knots[3];
	    rhs[n/2-1] = (8*knots[n-1]+knots[n+1])/2;
	    float[] y = buildControlPoints(rhs, n/2);
	    
	    for(int i=0; i<n/2; ++i){
	      controller1.add(x[i]);
	      controller1.add(y[i]);
	      if (i<n/2-1){
	        controller2.add(2*knots[2*i+2]-x[i+1]);
	        controller2.add(2*knots[2*i+3]-y[i+1]);
	      }else{
	        controller2.add((knots[n]+x[n/2-1])/2);
	        controller2.add((knots[n+1]+y[n/2-1])/2);
	      }
	    }
	  }

	  /**
	   * Returns the chart type identifier.
	   * 
	   * @return the chart type
	   */
	  public String getChartType() {
	    return TYPE;
	  }

}
