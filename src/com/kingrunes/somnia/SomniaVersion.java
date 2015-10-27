package com.kingrunes.somnia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.Gson;

import cpw.mods.fml.relauncher.FMLInjectionData;

public class SomniaVersion
{
							// Incremented when a significant change to the mod is made, never reset
	public static final int MAJOR_VERSION = 1,
							// Incremented for new features or significant rewrites, reset with every new MAJOR_VERSION
							MINOR_VERSION = 4,
							// Incremented when a bugfix is made and the mod can be considered 'stable', reset with every new MINOR_VERSION
							REVISION_VERSION = 7;
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
	private static final String UPDATE_URL_FORMAT = "https://" + "codeheist.net" + "/somnia-update.php" + "?v=%s";
	
	private static final String USER_AGENT = "Somnia-Updater (v1.0)";
	
	public static String getVersionString()
	{
		return String.format(FORMAT, MAJOR_VERSION, MINOR_VERSION, REVISION_VERSION, BUILD);
	}
	
	public static String getCoreVersionString()
	{
		return String.format(FORMAT, CORE_MAJOR_VERSION, CORE_MINOR_VERSION, CORE_REVISION_VERSION, BUILD);
	}
	
	private static int[] parseVersionString(String versionString)
	{
		String[] segments = versionString.split("\\.");
		if (segments.length < 3)
			return null;
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
				return null;
			}
		}
		
		return intArray;
	}
	
	private static boolean isHigher(String versionString)
	{
		return isHigher(versionString, MAJOR_VERSION, MINOR_VERSION, REVISION_VERSION, BUILD);
	}
	
	private static boolean isHigher(String baseVersion, String altVersion)
	{
		int[] intArray = parseVersionString(baseVersion);
		int build = intArray.length >= 4 ? intArray[3] : Integer.MAX_VALUE;
		return isHigher(altVersion, intArray[0], intArray[1], intArray[2], build);
	}
	
	private static boolean isHigher(String versionString, int major, int minor, int rev, int build)
	{
		int[] intArray = parseVersionString(versionString);
		if (intArray != null)
			return (major < intArray[0] ? true : (minor < intArray[1] ? true : (rev < intArray[2] ? true : intArray.length >= 4 && build < intArray[3])));
		else
			return false;
	}
	
	public static UpdateCheckerEntry checkForUpdates() throws IOException
	{
		HttpsURLConnection conn = null;
		BufferedReader br = null;
		try
		{
			URL url = new URL(String.format(UPDATE_URL_FORMAT, getVersionString()));
			conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("User-Agent", USER_AGENT);
			
			int responseCode = conn.getResponseCode();
			if (responseCode == 200) // Ignore anything else
			{
				br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line=br.readLine()) != null)
					sb.append(line);
				String str = sb.toString();
				sb.setLength(0);
				sb = null;
				
				Gson gson = new Gson();
				UpdateCheckerResponse response = gson.fromJson(str, UpdateCheckerResponse.class);
				if (response != null)
				{
					UpdateCheckerEntry[] entries = response.entriesFor(String.valueOf(FMLInjectionData.data()[4]));
					if (entries.length > 0)
					{
						UpdateCheckerEntry mostRecent = null;
						for (UpdateCheckerEntry e : entries)
						{
							if (mostRecent == null || isHigher(mostRecent.getVersionString(), e.getVersionString()))
								mostRecent = e;
						}
						if (isHigher(mostRecent.getVersionString()))
							return mostRecent;
					}
				}
			}
		}
		finally
		{
			if (conn != null)
			{
				conn.disconnect();
			}
			
			if (br != null)
			{
				try
				{
					br.close();
				}
				catch (IOException ioe) {}
			}
		}
		
		return null;
	}
	
	public static class UpdateCheckerEntry
	{
		private String versionString;
		private String url;
		private String comment;
		
		public UpdateCheckerEntry(String versionString, String url, String comment)
		{
			this.versionString = versionString;
			this.url = url;
			this.comment = comment;
		}

		public String getVersionString()
		{
			return versionString;
		}
		
		public String getUrl()
		{
			return url;
		}

		public String getComment()
		{
			return comment;
		}
	}
	
	public static class UpdateCheckerResponse
	{
		//				MC version -> Somnia versions
		private HashMap<String, UpdateCheckerEntry[]> versionMap;
		
		public UpdateCheckerResponse()
		{
			this(new HashMap<String, SomniaVersion.UpdateCheckerEntry[]>());
		}
		
		public UpdateCheckerResponse(HashMap<String, SomniaVersion.UpdateCheckerEntry[]> versionMap)
		{
			 this.versionMap = versionMap;
		}
		
		public UpdateCheckerEntry[] entriesFor(String mcVersion)
		{
			return versionMap.get(mcVersion);
		}
	}
}
