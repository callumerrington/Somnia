package com.kingrunes.somnia.common.util;

public class TimePeriod
{
	public long start;
	public long end;
	
	public TimePeriod(long start, long end)
	{
		this.start = start;
		this.end = end;
	}
	
	public boolean isTimeWithin(long time)
	{
		return (
				time >= start
				&&
				time <= end
				);
	}
}
