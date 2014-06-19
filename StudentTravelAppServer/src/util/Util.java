package util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import model.Constant;

import org.apache.commons.codec.binary.Hex;

import sun.misc.BASE64Decoder;

public class Util
{
	static PrintStream out;
	static int level;
	public static void init(PrintStream output, int l)
	{
		out = output;
		level = l;
	}
	
	protected static int getLevel(String sl)
	{
		if (sl.equals(Constant.LOG_LEVEL_INFO))
		{
			return Constant.LEVEL_INFO;
		}
		else if (sl.equals(Constant.LOG_LEVEL_DEBUG))
		{
			return Constant.LEVEL_DEBUG;
		}
		else if (sl.equals(Constant.LOG_LEVEL_WARNING))
		{
			return Constant.LEVEL_WARNING;
		}
		else if (sl.equals(Constant.LOG_LEVEL_ERROR))
		{
			return Constant.LEVEL_ERROR;
		}
		else if (sl.equals(Constant.LOG_LEVEL_FATAL))
		{
			return Constant.LEVEL_FATAL;
		}
		else
		{
			return Constant.LEVEL_INFO;
		}
	}
	
	
	public static void Log(String sl, String message, String callee)
	{
		if (level <= getLevel(sl))
		{
			String timeString = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
			out.println(sl + " " + timeString + " " + System.currentTimeMillis() + 
							   " at `" + callee + "` log : " + message);
		}
	}
	
	public static HashMap<String, String> getParams(String data)
	{
		HashMap<String, String> params = new HashMap<String, String>();
		try
		{
			String [] datas = data.split("&");
			for (String s : datas)
			{
				String [] values = s.split("=");
				params.put(values[0], URLDecoder.decode(URLDecoder.decode(values[1], "UTF-8"), "UTF-8"));
			}
		}
		catch (UnsupportedEncodingException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "Util getParams");
		}
		return params;
	}
	
	public static String genToken()
	{
		String charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random rand = new Random();
		StringBuffer buf = new StringBuffer();
		for(int i = 0 ;i < Constant.TOKEN_LENGTH ; i ++)
		{
			int num = rand.nextInt(62);
			buf.append(charset.charAt(num));
		}
		return buf.toString();
	}
	
	static MessageDigest SHA = null;
	static PrivateKey privateKey;
	//initialize the SHA algorithm instance
    static 
    {
        try 
        {
        	SHA = MessageDigest.getInstance("SHA");
        	
			File kprFile = new File(System.getProperty("user.dir") + "/id_rsa");
			DataInputStream dis = new DataInputStream(new FileInputStream(kprFile));
			byte[] keyBytes = new byte[(int)kprFile.length()];
			dis.readFully(keyBytes);
			dis.close();
			PKCS8EncodedKeySpec spec =new PKCS8EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			privateKey = kf.generatePrivate(spec);        	
        } 
        catch (NoSuchAlgorithmException e) 
        {
        	Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "Util Hash init");
        }
        catch (InvalidKeySpecException e) 
        {
        	Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "Util Hash init");
        }
		catch (FileNotFoundException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "Util Hash init");
		}
		catch (IOException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "Util Hash init");
		}
    }
	
	public static String getSHA1Value(String data)
	{
		SHA.reset();
		SHA.update(data.getBytes());
		String encrypt = Hex.encodeHexString(SHA.digest());
		return encrypt;
	}
	
	public static String RSADecrypt(String crypt)
	{
		String decrypt = null;
		try
		{
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.DECRYPT_MODE, privateKey);
			byte[] plain = cipher.doFinal(new BASE64Decoder().decodeBuffer(crypt));
			decrypt = new String(plain);
		}
		catch (NoSuchAlgorithmException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "Util RSADecrypt");
		}
		catch (NoSuchPaddingException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "Util RSADecrypt");
		}
		catch (InvalidKeyException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "Util RSADecrypt");
		}
		catch (IllegalBlockSizeException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "Util RSADecrypt");
		}
		catch (BadPaddingException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "Util RSADecrypt");
		}
		catch (IOException e)
		{
			Util.Log(Constant.LOG_LEVEL_ERROR, e.getMessage(), "Util RSADecrypt");
		}
		return decrypt;
	}
}
