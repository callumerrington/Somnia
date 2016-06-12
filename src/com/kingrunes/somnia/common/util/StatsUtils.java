package com.kingrunes.somnia.common.util;

import java.util.List;

import java.util.Collections;
import java.util.Comparator;

public class StatsUtils
{
	public static double meanl(List<Long> data)
	{
		double avg = 0;
		double n = 1;
		for (Long l : data)
		{
			avg = avg + (l - avg) / n;
			n++;
		}
		return avg;
	}
	
	public static long rangel(List<Long> sortedData)
	{
		return (sortedData.isEmpty() ? 0l : (sortedData.size() == 1 ? sortedData.get(0) : sortedData.get(sortedData.size()-1)-sortedData.get(0)));
	}
	
	public static void sortl(List<Long> unsortedData)
	{
		Collections.sort(unsortedData,
				new Comparator<Long>() 
				{
					@Override
					public int compare(Long o1, Long o2) 
					{
						return o1.compareTo(o2);
					}
				});
	}
}
