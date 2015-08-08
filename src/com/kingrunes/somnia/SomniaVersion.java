package com.kingrunes.somnia;

public class SomniaVersion
{
							// Incremented when a significant change to the mod is made, never reset
	public static final int MAJOR_VERSION = 1,
							// Incremented for new features or significant rewrites, reset with every new MAJOR_VERSION
							MINOR_VERSION = 5,
							// Incremented when a bugfix is made and the mod can be considered 'stable', reset with every new MINOR_VERSION
							REVISION_VERSION = 0;
							// Incremented automatically by the build system, never reset
	
							// Incremented when a significant change to the mod is made, never reset
	public static final int CORE_MAJOR_VERSION = 1,
							// Incremented for new features or significant rewrites, reset with every new MAJOR_VERSION
							CORE_MINOR_VERSION = 5,
							// Incremented when a bugfix is made and the mod can be considered 'stable', reset with every new MINOR_VERSION
							CORE_REVISION_VERSION = 0;
							// Incremented automatically by the build system, never reset

	public static final int BUILD = 0;
	
	private static final String FORMAT = "%s.%s.%s.%s";
	
	public static String getVersionString()
	{
		return String.format(FORMAT, MAJOR_VERSION, MINOR_VERSION, REVISION_VERSION, BUILD);
	}
	
	public static String getCoreVersionString()
	{
		return String.format(FORMAT, CORE_MAJOR_VERSION, CORE_MINOR_VERSION, CORE_REVISION_VERSION, BUILD);
	}
	
	public static boolean isHigher(String versionString)
	{
		String[] segments = versionString.split("\\.");
		if (segments.length < 3)
			return false;
		int[] intArray = new int[segments.length];
		for (int i=0; i<segments.length; i++)
		{
			try
			{
				intArray[i] = Integer.parseInt(segments[i]);
			}
			catch (NumberFormatException nfe)
			{
				nfe.printStackTrace();
				return false;
			}
		}
		return (MAJOR_VERSION < intArray[0] ? true : (MINOR_VERSION < intArray[1] ? true : (REVISION_VERSION < intArray[2] ? true : intArray.length >= 4 && BUILD < intArray[3])));
	}
}
