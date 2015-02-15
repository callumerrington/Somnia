package com.kingrunes.somnia;

public class SomniaVersion
{
							// Incremented when a significant change to the mod is made, never reset
	public static final int MAJOR_VERSION = 1,
							// Incremented for new features or significant rewrites, reset with every new MAJOR_VERSION
							MINOR_VERSION = 4,
							// Incremented when a bugfix is made and the mod can be considered 'stable', reset with every new MINOR_VERSION
							REVISION_VERSION = 1;
							// Incremented automatically by the build system, never reset
	
							// Incremented when a significant change to the mod is made, never reset
	public static final int CORE_MAJOR_VERSION = 1,
							// Incremented for new features or significant rewrites, reset with every new MAJOR_VERSION
							CORE_MINOR_VERSION = 3,
							// Incremented when a bugfix is made and the mod can be considered 'stable', reset with every new MINOR_VERSION
							CORE_REVISION_VERSION = 1;
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
}
