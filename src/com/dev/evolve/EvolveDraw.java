package com.dev.evolve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.dev.evolve.classes.Canvas;
import com.dev.evolve.classes.Chromosone;

public class EvolveDraw extends Activity implements OnTouchListener,
		OnClickListener, Runnable, OnCheckedChangeListener
{
	private Canvas				userCanvas, evolveCanvas;
	private List<Point>			drawPoints;
	private Map<String, Point>	targetMap;
	private List<Chromosone>	population;
	private final int			populationSize	= 200;
	public int					width;
	public int					height;
	private final double		crossOverRate	= 0.75;
	private final double		mutationRate	= 0.15;
	private Random				random;
	private Chromosone			leastFit1, leastFit2;
	private double				min1, min2;
	private double				previousFitness;
	private Button				btnStart, btnStop, btnClear;
	private Handler				uiUpdater;
	private TextView			txtGeneration, txtFitness, txtAccuracy;
	private boolean				isRunning		= false;
	private int					generation		= 0;
	private CheckBox			cbxToggleCoords, cbxToggleDesiredPoint;
	private double				startingFitness	= 0.0;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.evolvedraw);
		drawPoints = new ArrayList<Point>();
		userCanvas = (Canvas) findViewById(R.id.userCanvas);
		userCanvas.setOnTouchListener(this);
		userCanvas.drawPoints = drawPoints;

		setupHandler();
		evolveCanvas = (Canvas) findViewById(R.id.evolveCanvas);
		evolveCanvas.desiredPoints = drawPoints;

		txtGeneration = (TextView) findViewById(R.id.txtGeneration);

		txtFitness = (TextView) findViewById(R.id.txtFitness);
		txtAccuracy = (TextView) findViewById(R.id.txtAccuracy);

		btnStart = (Button) findViewById(R.id.btnStart);
		btnStart.setOnClickListener(this);

		btnStop = (Button) findViewById(R.id.btnStop);
		btnStop.setOnClickListener(this);

		btnClear = (Button) findViewById(R.id.btnClear);
		btnClear.setOnClickListener(this);

		cbxToggleCoords = (CheckBox) findViewById(R.id.cbxToggleCoords);
		cbxToggleCoords.setOnCheckedChangeListener(this);

		cbxToggleDesiredPoint = (CheckBox) findViewById(R.id.cbxToggleDesiredPoint);
		cbxToggleDesiredPoint.setOnCheckedChangeListener(this);

		random = new Random();

	}

	private void clear()
	{
		if (isRunning)
			return;
		drawPoints.clear();
		if (targetMap != null)
			targetMap.clear();
		userCanvas.invalidate();
		evolveCanvas.drawPoints = null;
		evolveCanvas.invalidate();
		txtGeneration.setText("0");
		txtFitness.setText("0");
		txtAccuracy.setText("0");
	}

	private void setupHandler()
	{
		uiUpdater = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				Chromosone fittest = fittestIndividual();
				if (generation == 0)
					startingFitness = fittest.fitness;

				if (previousFitness != fittest.fitness)
				{
					txtFitness.setText(String.valueOf(fittest.fitness));
					Point[] temp = new Point[drawPoints.size()];
					for (Map.Entry<String, Point> entry : fittest.generationMap.entrySet())
						temp[Integer.parseInt(entry.getKey())] = entry.getValue();
					evolveCanvas.drawPoints = new ArrayList<Point>(Arrays.asList(temp));

					double accuracy = 100 - ((fittest.fitness / startingFitness)) * 100;
					txtAccuracy.setText(String.valueOf(accuracy));
					evolveCanvas.invalidate();

				}
				previousFitness = fittest.fitness;
				txtGeneration.setText(String.valueOf(generation));
				if (fittest.fitness == 0)
				{
					isRunning = false;
					return;
				}

			}

		};
	}

	private void computeLeastFitParents()
	{
		min1 = Double.MIN_VALUE;
		min2 = Double.MIN_VALUE;
		for (Chromosone chromosone : population)
		{
			if (chromosone.fitness > min1)
			{
				min2 = min1;
				min1 = chromosone.fitness;
				leastFit2 = leastFit1;
				leastFit1 = chromosone;
			} else if (chromosone.fitness > min2)
			{
				min2 = chromosone.fitness;
				leastFit2 = chromosone;
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if (isRunning)
			return super.onTouchEvent(event);
		drawPoints.add(new Point((int) event.getX(), (int) event.getY()));
		userCanvas.invalidate();
		return false;
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.btnStart:
				if (drawPoints.size() == 0)
					return;

				if (isRunning)
					return;

				isRunning = true;
				targetMap = new HashMap<String, Point>(drawPoints.size());

				for (int i = 0; i < drawPoints.size(); i++)
					targetMap.put(String.valueOf(i), drawPoints.get(i));
				Chromosone.targetMap = targetMap;
				GenerateRandomPopulation();
				runEvolution();
			break;

			case R.id.btnStop:
				isRunning = false;
			break;

			case R.id.btnClear:
				clear();
			break;
		}

	}

	private void GenerateRandomPopulation()
	{
		population = new ArrayList<Chromosone>(populationSize);
		for (int i = 0; i < populationSize; i++)
		{
			Map<String, Point> generationMap = new HashMap<String, Point>(drawPoints.size());
			for (int x = 0; x < drawPoints.size(); x++)
				generationMap.put(String.valueOf(x), new Point(random.nextInt(width), random.nextInt(height)));

			Chromosone c = new Chromosone(generationMap);
			c.CalculateFitness();
			population.add(c);
		}
	}

	@Override
	public void run()
	{
		while (isRunning)
		{
			Evolve();
			uiUpdater.sendEmptyMessage(0);
			generation++;
		}
	}

	private void runEvolution()
	{
		generation = 0;
		Thread worker = new Thread(this);
		worker.start();
	}

	@Override
	protected void onDestroy()
	{
		isRunning = false;
		super.onDestroy();
	}

	@Override
	protected void onStop()
	{
		isRunning = false;
		super.onStop();
	}

	private double populationFitness()
	{
		double fitness = 0;
		for (Chromosone c : population)
			fitness += c.fitness;

		return fitness;
	}

	private int RouletteWheelIndex()
	{
		double popFitness = populationFitness();
		double RAND_NUM = random.nextDouble();
		double upperLimit = RAND_NUM * popFitness;
		double total = 0.0;
		for (int i = 0; i < population.size(); i++)
		{
			total += population.get(i).fitness;
			if (total >= upperLimit)
				return i;
		}
		return random.nextInt(population.size());

	}

	private void Evolve()
	{
		List<Chromosone> newPopulation = new ArrayList<Chromosone>();
		for (int i = 0; i < populationSize / 2; i++)
		{
			Chromosone p1 = population.get(RouletteWheelIndex());
			Chromosone p2 = population.get(RouletteWheelIndex());

			Chromosone[] child = CrossOver(p1, p2);
			Chromosone mut1 = Mutate(child[0]);
			Chromosone mut2 = Mutate(child[1]);

			newPopulation.add(mut1);
			newPopulation.add(mut2);

			computeLeastFitParents();
			population.remove(leastFit1);
			population.remove(leastFit2);

		}
		population = newPopulation;
	}

	private Chromosone[] CrossOver(Chromosone o1, Chromosone o2)
	{
		double RAND_NUM = random.nextDouble();

		if (RAND_NUM > crossOverRate)
			return new Chromosone[] { o1, o2 };
		int crossOverPoint = random.nextInt(drawPoints.size());
		// while (crossOverPoint == 0)
		// crossOverPoint = random.nextInt(drawPoints.size());
		Map<String, Point> c1 = new HashMap<String, Point>(drawPoints.size());
		Map<String, Point> c2 = new HashMap<String, Point>(drawPoints.size());

		for (int i = 0; i < drawPoints.size(); i++)
		{
			String key = String.valueOf(i);
			if (i < crossOverPoint)
			{
				c1.put(key, o1.generationMap.get(key));
				c2.put(key, o2.generationMap.get(key));
			} else
			{
				c1.put(key, o2.generationMap.get(key));
				c2.put(key, o1.generationMap.get(key));
			}
		}

		Chromosone child1 = new Chromosone(c1);
		child1.CalculateFitness();
		Chromosone child2 = new Chromosone(c2);
		child2.CalculateFitness();
		return new Chromosone[] { child1, child2 };
	}

	private Chromosone Mutate(Chromosone chromo)
	{
		double RAND_NUM = random.nextDouble();
		if (RAND_NUM > mutationRate)
			return chromo;
		int mutatePoint = random.nextInt(drawPoints.size());
		// while (mutatePoint == 0)
		// mutatePoint = random.nextInt(drawPoints.size());

		Map<String, Point> map = chromo.generationMap;
		map.put(String.valueOf(mutatePoint), new Point(random.nextInt(width), random.nextInt(height)));

		Chromosone mutated = new Chromosone(map);
		mutated.CalculateFitness();
		return mutated;
	}

	private Chromosone fittestIndividual()
	{
		double min = Double.MAX_VALUE;
		Chromosone fittest = null;
		for(int i=0; i<population.size();i++)
		{
			Chromosone chromo = population.get(i);
			if (chromo.fitness < min)
			{
				min = chromo.fitness;
				fittest = chromo;
			}
		}
		/*for (Chromosone chromo : population)
		{
			if (chromo.fitness < min)
			{
				min = chromo.fitness;
				fittest = chromo;
			}
		}*/
		return fittest;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		switch (buttonView.getId())
		{
			case R.id.cbxToggleCoords:
				evolveCanvas.showCoords = isChecked;
				userCanvas.showCoords = isChecked;
				evolveCanvas.invalidate();
				userCanvas.invalidate();
			break;
			case R.id.cbxToggleDesiredPoint:
				evolveCanvas.showDesiredPoints = isChecked;
				evolveCanvas.invalidate();
			break;
		}

	}

}