package com.ming.trade.track.util;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.regex.Pattern;

public class StringHelper
{
  public static boolean isNumber(String paramString)
  {
    if (isEmpty(paramString))
      return false;
    return Pattern.matches("^[-+0-9.]*$", paramString.trim());
  }

  public static void main(String[] paramArrayOfString)
    throws Exception
  {
   
  }

  public static String uuid()
  {
    UUID localUUID = UUID.randomUUID();
    String str = localUUID.toString();
    return replace(str, "-", "");
  }

  public static String encodeUtf8String(String paramString)
  {
    if (isEmpty(paramString))
      return "";
    try
    {
      String str = URLEncoder.encode(paramString, "utf-8");
      return str;
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      localUnsupportedEncodingException.printStackTrace();
    }
    return "";
  }

  public static String decodeUtf8String(String paramString)
  {
    if (isEmpty(paramString))
      return "";
    try
    {
      String str = new String(paramString.getBytes("iso8859_1"), "utf-8");
      str = URLDecoder.decode(str, "utf-8");
      return str;
    }
    catch (UnsupportedEncodingException localUnsupportedEncodingException)
    {
      localUnsupportedEncodingException.printStackTrace();
    }
    return "";
  }

  public static String toUtf8String(String paramString)
  {
    StringBuffer localStringBuffer = new StringBuffer();
    for (int i = 0; i < paramString.length(); ++i)
    {
      char c = paramString.charAt(i);
      if ((c >= 0) && (c <= 255))
      {
        localStringBuffer.append(c);
      }
      else
      {
        byte[] arrayOfByte;
        try
        {
          arrayOfByte = Character.toString(c).getBytes("utf-8");
        }
        catch (Exception localException)
        {
          System.out.println(localException);
          arrayOfByte = new byte[0];
        }
        for (int j = 0; j < arrayOfByte.length; ++j)
        {
          int k = arrayOfByte[j];
          if (k < 0)
            k += 256;
          localStringBuffer.append("%" + Integer.toHexString(k).toUpperCase());
        }
      }
    }
    return localStringBuffer.toString();
  }

  public static String toUTF(String paramString)
  {
    String str = new String("");
    if (paramString == null)
      paramString = "";
    for (int j = 0; j < paramString.length(); ++j)
    {
      char c = paramString.charAt(j);
      if ((c == '=') || (c == '/') || (c == '<') || (c == '>') || (c == '"') || (c == ' ') || (c + "".toString().getBytes().length == 1))
      {
        str = str + c;
      }
      else
      {
        int i = c + '\0';
        str = str + "&#x" + Integer.toHexString(i) + ";";
      }
    }
    return str;
  }

  public static String convertNull(String paramString)
  {
    if (paramString != null)
      return paramString.trim();
    return "";
  }

  public static String parseURLToFile(URL paramURL)
  {
    String str = paramURL.toString();
    if (str.startsWith("file://"))
      str = str.substring("file://".length(), str.length());
    else if (str.startsWith("file:/"))
      str = str.substring("file:/".length(), str.length());
    else if (str.startsWith("file:\\\\"))
      str = str.substring("file:\\\\".length(), str.length());
    else if (str.startsWith("file:\\"))
      str = str.substring("file:\\".length(), str.length());
    return str;
  }

  public static boolean isEmpty(String paramString)
  {
    return ((paramString == null) || (paramString.trim().length() == 0) || ("null".equals(paramString)));
  }

  public static boolean isNotEmpty(String paramString)
  {
    return ((paramString != null) && (paramString.trim().length() != 0) && (!("null".equals(paramString))));
  }

  public static String firstCharToLowercase(String paramString)
  {
    if (Character.isUpperCase(paramString.charAt(0)))
    {
      char c = Character.toLowerCase(paramString.charAt(0));
      paramString = c + paramString.substring(1, paramString.length());
    }
    return paramString;
  }

  public static String firstCharToUppercase(String paramString)
  {
    if (Character.isLowerCase(paramString.charAt(0)))
    {
      char c = Character.toUpperCase(paramString.charAt(0));
      paramString = c + paramString.substring(1, paramString.length());
    }
    return paramString;
  }

  public static String propertyNameToGetterName(String paramString)
  {
    paramString = firstCharToUppercase(paramString);
    return "get" + paramString + "()";
  }

  public static String getNestedPropertyGetterName(String paramString)
  {
    StringTokenizer localStringTokenizer = new StringTokenizer(paramString, ".", false);
    StringBuffer localStringBuffer = new StringBuffer();
    while (localStringTokenizer.hasMoreElements())
    {
      String str = (String)localStringTokenizer.nextElement();
      propertyNameToGetterName(str);
      localStringBuffer.append(propertyNameToGetterName(str) + ".");
    }
    paramString = localStringBuffer.toString();
    if (paramString.endsWith("."))
      return paramString.substring(0, paramString.length() - 1);
    return paramString;
  }

  public static String replace(String paramString1, String paramString2, String paramString3)
  {
    int i = 0;
    int j = 0;
    StringBuffer localStringBuffer = new StringBuffer();
    while ((j = paramString1.indexOf(paramString2, i)) >= 0)
    {
      localStringBuffer.append(paramString1.substring(i, j));
      localStringBuffer.append(paramString3);
      i = j + paramString2.length();
    }
    localStringBuffer.append(paramString1.substring(i));
    return localStringBuffer.toString();
  }

  public static String leftPad(String paramString, int paramInt, char paramChar)
  {
    int i = paramInt - paramString.length();
    if (i > 0)
    {
      for (int j = 0; j < i; ++j)
        paramString = paramChar + paramString;
      return paramString;
    }
    return paramString;
  }
}

/* Location:           D:\cmp\thunk\WebRoot\WEB-INF\lib\framework.jar
 * Qualified Name:     com.tzit.framework.util.StringHelper
 * JD-Core Version:    0.5.3
 */