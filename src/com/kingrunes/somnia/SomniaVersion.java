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
							MINOR_VERSION = 5,
							// Incremented when a bugfix is made and the mod can be considered 'stable', reset with every new MINOR_VERSION
							REVISION_VERSION = 0;
							// Incremented automatically by the build system, never reset
	
							// Incremented when a significant change to the mod is made, never reset
	public static final int CORE_MAJOR_VERSION = 1,
							// Incremented for new features or significant rewrites, reset with every new MAJOR_VERSION
							CORE_MINOR_VERSION = 4,
							// Incremented when a bugfix is made and the mod can be considered 'stable', reset with every new MINOR_VERSION
							CORE_REVISION_VERSION = 1;
							// Incremented automatically by the build system, never reset

	public static final int BUILD = 0;
	
	private static final String FORMAT = "%s.%s.%s.%s";
	private static final String UPDATE_URL_FORMAT = "https://" + "codeheist.net" + "/somnia-update.php" + "?v=%s";
	
	private static final String USER_AGENT = "Somnia-Updater (v1.0)";
	
	public static int getBuild()
	{
		return BUILD;
	}
	
	public static String getVersionString()
	{
		return String.format(FORMAT, MAJOR_VERSION, MINOR_VERSION, REVISION_VERSION, BUILD);
	}
	
	public static String getCoreVersionString()
	{
		return String.format(FORMAT, CORE_MAJOR_VERSION, CORE_MINOR_VERSION, CORE_REVISION_VERSION, BUILD);
	}
	
	private static boolean isHigher(String versionString)
	{
		return isHigher(getVersionString(), versionString);
	}
	
	/*
	 * Implementation based off Alex Gitelman's answer from:
	 * 	http://stackoverflow.com/questions/6701948/efficient-way-to-compare-version-strings-in-java
	 */
	private static boolean isHigher(String baseVersion, String altVersion)
	{
	    String[] vals1 = altVersion.split("\\.");
	    String[] vals2 = baseVersion.split("\\.");
	    int i = 0;
	    // set index to first non-equal ordinal or length of shortest version string
	    while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i]))
	      i++;
	    // compare first non-equal ordinal number
	    if (i < vals1.length && i < vals2.length)
	    {
	        int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
	        return Integer.signum(diff) > 0;
	    }
	    return Integer.signum(vals1.length - vals2.length) > 0;
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
