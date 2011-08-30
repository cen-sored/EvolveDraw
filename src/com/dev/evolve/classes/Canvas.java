package com.dev.evolve.classes;

import java.util.List;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import com.dev.evolve.EvolveDraw;

public class Canvas extends View
{

	public List<Point>	drawPoints;
	public List<Point>	desiredPoints;
	private Paint		ovalPaint, textPaint, desiredPaint;
	private EvolveDraw	parent;
	public boolean		showCoords;
	public boolean		showDesiredPoints;

	public Canvas(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		showDesiredPoints = false;
		parent = (EvolveDraw) context;
		ovalPaint = new Paint();
		ovalPaint.setColor(Color.WHITE);
		ovalPaint.setAntiAlias(true);
		
		desiredPaint = new Paint();
		desiredPaint.setColor(Color.YELLOW);
		desiredPaint.setAntiAlias(true);
		desiredPaint.setAlpha(120);

		textPaint = new Paint();
		textPaint.setTextSize(18);
		textPaint.setAntiAlias(true);
		textPaint.setColor(Color.RED);
		showCoords = false;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		parent.width = w;
		parent.height = h;
		super.onSizeChanged(w, h, oldw, oldh);
	};

	@Override
	protected void onDraw(android.graphics.Canvas canvas)
	{
		if (drawPoints == null || drawPoints.size() == 0)
			return;

		for (int i = 0; i < drawPoints.size(); i++)
		{
			Point p = drawPoints.get(i);
			RectF r = new RectF(p.x - 3, p.y - 3, p.x + 6, p.y + 6);
			canvas.drawOval(r, ovalPaint);
			
			if (showCoords)
			{
				String curPoint = "(" + p.x + "," + p.y + ")";
				canvas.drawText(curPoint, p.x - 35, p.y + 25, textPaint);
			}
			if (i < 1)
				continue;

			Point prevP = drawPoints.get(i - 1);
			canvas.drawLine(prevP.x + 3, prevP.y, p.x, p.y, ovalPaint);

		}
		
		if(showDesiredPoints)
		{
			if(desiredPoints == null || desiredPoints.size() == 0)
				return;
			
			for (int i = 0; i < desiredPoints.size(); i++)
			{
				Point p = desiredPoints.get(i);
				RectF r = new RectF(p.x - 3, p.y - 3, p.x + 6, p.y + 6);
				canvas.drawOval(r, desiredPaint);
				
				if (i < 1)
					continue;
				Point prevP = desiredPoints.get(i - 1);
				canvas.drawLine(prevP.x + 3, prevP.y, p.x, p.y, desiredPaint);
			}
		}

	}

}
