package com.kingrunes.somnia.common;

import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.network.PacketBuffer;

import com.google.common.base.Charsets;

public class StreamUtils
{

	public static String readString(DataInputStream in) throws IOException
	{
		int i = in.readInt();
		
		if (i < 0)
			throw new IOException("The received encoded string buffer length is less than zero! Weird string!");
		
		byte[] buffer = new byte[i];
		in.read(buffer);
		return new String(buffer, Charsets.UTF_8);
	}

	public static void writeString(String str, PacketBuffer pBuffer)
	{
		byte[] buffer = str.getBytes(Charsets.UTF_8);

		pBuffer.writeInt(buffer.length);
		pBuffer.writeBytes(buffer);
	}

	public static void writeObject(Object object, PacketBuffer buffer)
	{
		if (object instanceof String)
			writeString((String)object, buffer);
		else if (object instanceof Integer)
			buffer.writeInt((Integer)object);
		else if (object instanceof Long)
			buffer.writeLong((Long)object);
		else if (object instanceof Double)
			buffer.writeDouble((Double)object);
		else
			throw new IllegalArgumentException("unknown data type: " + object.getClass().getCanonicalName());
	}
}
