package com.kingrunes.somnia.common.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;

public class ListUtils
{
	public static <T> WeakReference<T> getWeakRef(T obj, List<WeakReference<T>> refs)
	{
		Iterator<WeakReference<T>> iter = refs.iterator();
		while (iter.hasNext())
		{
			WeakReference<T> weakRef = iter.next();
			T o = weakRef.get();
			if (o == null)
				iter.remove();
			else if (o == obj)
				return weakRef;
		}
		
		return null;
	}
	
	public static <T> boolean containsRef(T obj, List<WeakReference<T>> refs)
	{
		Iterator<WeakReference<T>> iter = refs.iterator();
		while (iter.hasNext())
		{
			WeakReference<T> weakRef = iter.next();
			T o = weakRef.get();
			if (o == null)
				iter.remove();
			else if (o == obj)
				return true;
		}
		
		return false;
	}

	public static <T> List<T> extractRefs(List<WeakReference<T>> refs)
	{
		List<T> objects = new ArrayList<T>(refs.size());
		
		Iterator<WeakReference<T>> iter = refs.iterator();
		while (iter.hasNext())
		{
			WeakReference<T> weakRef = iter.next();
			T o = weakRef.get();
			if (o == null)
				iter.remove();
			else 
				objects.add(o);
		}
		
		return objects;
	}

	public static String[] playersToStringArray(List<EntityPlayerMP> players)
	{
		String[] astring = new String[players.size()];
		for (int i=0; i<astring.length; i++)
			astring[i] = players.get(i).getName();
		return astring;
	}
}
