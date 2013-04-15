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
//import android.util.Log;

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
	
	private static final int POSITIVE_COLOR = Color.GREEN;
	private static final int NEGATIVE_COLOR = Color.RED;
	private static final int FILLCOLOR_ALPHA = 60;
	
	private float firstMultiplier;
	private float secondMultiplier;
	private PointF p1 = new PointF();
	private PointF p2 = new PointF();
	private PointF p3 = new PointF();
	
	public class DataColorSet{
		private List<Float> mData;
		private int mStrokeColor;
		private int mFillColor;
		
		public DataColorSet(List<Float> lineData, int strokeColor, int fillColor){
			mData = lineData;
			mStrokeColor = strokeColor;
			mFillColor = fillColor;
		}
		
		public List<Float> getData(){
			return mData;
		}
		
		public int getStrokeColor()
		{
			return mStrokeColor;
		}
		
		public int getFillColor(){
			return mFillColor;
		}
	}
	
	ColoredLineChart(){
		firstMultiplier = 0.333f;
        secondMultiplier = 1 - firstMultiplier;
	}
	
	public ColoredLineChart(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer) {
		super(dataset, renderer);
		firstMultiplier = 0.333f;
		secondMultiplier = 1 - firstMultiplier;
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
	
	// first 2 values of points must start with position for (0,0) in order to get proper axisY position
	private List<DataColorSet> getDataColorSets(List<Float> points){
		List<DataColorSet> seps = new ArrayList<DataColorSet>();
		float zero = points.get(1); //first Y axis
		int startIndex = 0;
		int endIndex = points.size();
		int currColor;
		if (points.get(3) <= zero) currColor = POSITIVE_COLOR;
		else currColor = NEGATIVE_COLOR;
		
		PointF temp = new PointF(points.get(0), zero);
		for(int i=5; i<endIndex; i+=2){
			float prevY = points.get(i-2);
			float currY = points.get(i);
//			Log.v(TAG, "prevY: " + prevY + " currY: " + currY);
			// Only check the previous value and current value are on opposite side of axisY
			if (prevY >= zero && currY < zero || prevY <= zero && currY >zero){
				
				List<Float> tempList = new ArrayList<Float>(points.subList(startIndex, i-1));
				if (startIndex > 0){
					tempList.add(0, temp.y);
					tempList.add(0, temp.x);
				}
				if (prevY != zero){
					temp = getSepPoint(points.get(i-3), prevY, points.get(i-1), currY, zero);				
					tempList.add(temp.x);
					tempList.add(temp.y);
//					Log.v(TAG, "midX: " + temp.x + " midY: " + temp.y);

				}else{
					temp = new PointF(points.get(i-3), prevY);
				}
//				Log.v(TAG, "index: " + i + " tempList: " + tempList);
				seps.add(new DataColorSet(tempList, currColor, Color.argb(FILLCOLOR_ALPHA, Color.red(currColor), Color.green(currColor), Color.blue(currColor))));
				startIndex = i-1;
				if (currY <= zero) {
					currColor = POSITIVE_COLOR;
				}
				else {
					currColor = NEGATIVE_COLOR;
				}
			}
		}
		
		if (startIndex == 0)
			seps.add(new DataColorSet(points, currColor, Color.argb(FILLCOLOR_ALPHA, Color.red(currColor), Color.green(currColor), Color.blue(currColor))));
		else{
			List<Float> lastList = new ArrayList<Float>(points.subList(startIndex, endIndex));
			lastList.add(0, temp.y);
			lastList.add(0, temp.x);
			seps.add(new DataColorSet(lastList, currColor, Color.argb(FILLCOLOR_ALPHA, Color.red(currColor), Color.green(currColor), Color.blue(currColor))));
		}

		return seps;
	}
	
	private PointF getSepPoint(float prevX, float prevY, float currX, float currY, float fixedY){
		float diffY = Math.abs(prevY - currY);
		float diffX = Math.abs(prevX - currX);
		float deltaX = Math.abs(prevY-fixedY)/diffY*diffX; 
		return new PointF(prevX+deltaX, fixedY);
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
//	    float lineWidth = paint.getStrokeWidth();
	    paint.setStrokeWidth(renderer.getLineWidth());
	    final FillOutsideLine[] fillOutsideLine = renderer.getFillOutsideLine();
	    List<DataColorSet> seps = getDataColorSets(points);
	    for (int s=0; s< seps.size(); s++){
	    	DataColorSet dcSet = seps.get(s);	    	
		    for (FillOutsideLine fill : fillOutsideLine) {
		      if (fill.getType() != FillOutsideLine.Type.NONE) {
		        paint.setColor(dcSet.getFillColor());

		        List<Float> fillPoints = new ArrayList<Float>();
		        int[] range = fill.getFillRange();
		        if (range == null) {
		          fillPoints.addAll(dcSet.getData());
		        } else {
		          fillPoints.addAll(dcSet.getData().subList(range[0] * 2, range[1] * 2));
		        }
	
		        final float referencePoint;
		        switch (fill.getType()) {
		        case BOUNDS_ALL:
		          referencePoint = yAxisValue;
		          break;
		        case BOUNDS_BELOW:
		          referencePoint = yAxisValue;
		          break;
		        case BOUNDS_ABOVE:
		          referencePoint = yAxisValue;
		          break;
		        case BELOW:
		          referencePoint = canvas.getHeight();
		          break;
		        case ABOVE:
		          referencePoint = 0;
		          break;
		        default:
		          throw new RuntimeException(
		              "You have added a new type of filling but have not implemented.");
		        }
	       
		        int length = fillPoints.size();
		        fillPoints.set(0, fillPoints.get(0) + 1);
		        fillPoints.add(fillPoints.get(length - 2));
		        fillPoints.add(referencePoint);
		        fillPoints.add(fillPoints.get(0));
		        fillPoints.add(fillPoints.get(length + 1));
		        for (int i = 0; i < length + 4; i += 2) {
		          if (fillPoints.get(i + 1) < 0) {
		            fillPoints.set(i + 1, 0f);
		          }
		        }
	
		        paint.setStyle(Style.FILL);
		        drawPath(canvas, fillPoints, paint, true);
		      }
		    }
		    paint.setColor(seriesRenderer.getColor());
		    paint.setStyle(Style.STROKE);
		    paint.setStrokeWidth(renderer.getLineWidth());	   
		    drawPath(canvas, dcSet.getData(), paint, false);
		   
	    }
	    
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
	    Path p = new Path();
	    float x = points.get(0);
	    float y = points.get(1);
	    p.moveTo(x, y);

	    int length = points.size();
	    if (circular) {
	      length -= 4;
	    }

	    for (int i = 0; i < length; i += 2) {
	      int nextIndex = i + 2 < length ? i + 2 : i;
	      int nextNextIndex = i + 4 < length ? i + 4 : nextIndex;
	      calc(points, p1, i, nextIndex, secondMultiplier);
	      p2.set(points.get(nextIndex), points.get(nextIndex + 1));
	      calc(points, p3, nextIndex, nextNextIndex, firstMultiplier);
	      // From last point, approaching x1/y1 and x2/y2 and ends up at x3/y3
	      p.cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y);

	    }
	    if (circular) {
	      for (int i = length; i < length + 4; i += 2) {
	        p.lineTo(points.get(i), points.get(i + 1));
	      }
	      p.lineTo(points.get(0), points.get(1));
	    }
	    canvas.drawPath(p, paint);
	  }

	  private void calc(List<Float> points, PointF result, int index1, int index2, final float multiplier) {
	    float p1x = points.get(index1);
	    float p1y = points.get(index1 + 1);
	    float p2x = points.get(index2);
	    float p2y = points.get(index2 + 1);

	    float diffX = p2x - p1x; // p2.x - p1.x;
	    float diffY = p2y - p1y; // p2.y - p1.y;
	    result.set(p1x + (diffX * multiplier), p1y + (diffY * multiplier));
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
