package com.dev.evolve.classes;

import java.util.Map;

import android.graphics.Point;

public class Chromosone
{
	public Map<String, Point>			generationMap;
	public static Map<String, Point>	targetMap;
	public double						fitness;

	public Chromosone(Map<String, Point> generationMap)
	{
		this.generationMap = generationMap;
	}

	public void CalculateFitness()
	{
		double fit = 0;
		for (Map.Entry<String, Point> target : targetMap.entrySet())
		{
			Point generationPoint = generationMap.get(target.getKey());
			double res = Math.pow(generationPoint.x - target.getValue().x, 2) + Math.pow(generationPoint.y - target.getValue().y, 2);
			fit += Math.sqrt(res);
		}
		fitness = fit;
	}
}
