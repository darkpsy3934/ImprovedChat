package wdmods.improvedchat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.src.ChatLine;

/**
 *
 * @author wd1966
 */
public class ImprovedChatLine extends ChatLine
{
    /** GUI Update Counter value this Line was created at */
    private final int updateCounterCreated;
    private final String lineString;

    /**
     * int value to refer to existing Chat Lines, can be 0 which means unreferrable
     */
    private final int chatLineID;
    private static Map<String, BufferedWriter> out = new HashMap<String, BufferedWriter>();
    private static SimpleDateFormat prefix = null;
    private static SimpleDateFormat sufix = null;

    public ImprovedChatLine(int updateCounterValue, String text, int id)
    {
    	super(updateCounterValue, text, id);
    	updateCounterCreated = updateCounterValue;
        lineString = text;
        chatLineID = id;
        writeLog();
    }

    public ImprovedChatLine(int updateCounterValue, String text)
    {
    	super(updateCounterValue, text, 0);
        lineString = text;
        updateCounterCreated = updateCounterValue;
        chatLineID = 0;
        writeLog();
    }

    @Override
	public String getChatLineString()
    {
        return lineString;
    }

    @Override
	public int getUpdatedCounter()
    {
        return updateCounterCreated;
    }

    @Override
	public int getChatLineID()
    {
        return chatLineID;
    }

    private void writeLog()
    {
        try
        {
            String cleaned = ImprovedChat.stripColors(lineString);
            BufferedWriter writer = getOut();
            Date time = Calendar.getInstance().getTime();
            writer.write(ImprovedChatLine.prefix.format(time) + cleaned + ImprovedChatLine.sufix.format(time));
            writer.flush();
        }
        catch (Exception ex)
        {
            System.err.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private BufferedWriter getOut()
    {
        String serverName = ImprovedChat.getCurrentServer().name;

        if (ImprovedChatLine.out != null && ImprovedChatLine.out.containsKey(serverName))
        {
            return ImprovedChatLine.out.get(serverName);
        }
        
		File chatLoggerCfg = new File(ImprovedChat.getModDir(), "ChatLogger.conf");
		String format = "EEE, d MMM yyyy HH:mm:ss \'-\' $line\'\n\'";
		BufferedWriter writer = null;
		File file = null;

		if (!ImprovedChat.getModDir().exists())
		{
		    ImprovedChat.getModDir().mkdirs();
		}

		try
		{
		    if (ImprovedChat.getModDir().exists())
		    {
		        if (chatLoggerCfg.exists())
		        {
		            BufferedReader reader = new BufferedReader(new FileReader(chatLoggerCfg));
		            String line;

		            while ((line = reader.readLine()) != null)
		            {
		                if (!line.startsWith("#"))
		                {
		                    String[] parts = line.split("=", 2);

		                    if (parts.length == 2)
		                    {
		                        if (parts[0].equalsIgnoreCase("format"))
		                        {
		                            format = unescape(parts[1]);
		                        }

		                        if (parts[0].equalsIgnoreCase("destination"))
		                        {
		                            String fileName = parts[1];
		                            fileName = fileName.replaceAll("%server%", serverName);
		                            file = new File(fileName);
		                        }
		                    }
		                }
		            }

		            reader.close();
		        }
		        else
		        {
		            file = new File(ImprovedChat.getModDir(), "chat-%server%.log");
		            PrintWriter printWriter = new PrintWriter(chatLoggerCfg);
		            printWriter.println("#Config file for chat logger");
		            printWriter.println("format=" + escape(format));
		            printWriter.println("destination=" + file.getAbsolutePath());
		            file = new File(file.getAbsolutePath().replaceAll("%server", serverName));
		            printWriter.close();
		        }

		        int contentPos = format.indexOf("$line");

		        if (contentPos < 0)
		        {
		            contentPos = 0;
		        }

		        writer = new BufferedWriter(new FileWriter(file, true));
		        ImprovedChatLine.prefix = new SimpleDateFormat(format.substring(0, contentPos));
		        ImprovedChatLine.sufix = new SimpleDateFormat(format.substring(contentPos + 5));
		    }
		    else
		    {
		        writer = new BufferedWriter(new FileWriter("chatlog.txt", true));
		        ImprovedChatLine.prefix = new SimpleDateFormat("");
		        ImprovedChatLine.sufix = new SimpleDateFormat("");
		    }
		}
		catch (IOException ex)
		{
		    ex.printStackTrace();
		}

		if (writer != null)
		{
		    ImprovedChatLine.out.put(ImprovedChat.getCurrentServer().name, writer);
		}

		return writer;
    }

    private String escape(String text)
    {
        StringBuffer buf = new StringBuffer();

        for (int pos = 0; pos < text.length(); ++pos)
        {
            char charAt = text.charAt(pos);

            if (charAt == 10)
            {
                buf.append("\\n");
            }
            else if (charAt == 13)
            {
                buf.append("\\r");
            }
            else if (charAt == 12)
            {
                buf.append("\\f");
            }
            else
            {
                buf.append(charAt);
            }
        }

        return buf.toString();
    }

    private String unescape(String string)
    {
        StringBuffer buf = new StringBuffer();

        for (int pos = 0; pos < string.length(); ++pos)
        {
            char charAt = string.charAt(pos);

            if (charAt == 92 && pos + 1 < string.length())
            {
                ++pos;
                charAt = string.charAt(pos);

                if (charAt == 110)
                {
                    buf.append("\n");
                }
                else if (charAt == 114)
                {
                    buf.append("\r");
                }
                else if (charAt == 102)
                {
                    buf.append("\f");
                }
                else
                {
                    buf.append("\\" + charAt);
                }
            }
            else
            {
                buf.append(charAt);
            }
        }

        return buf.toString();
    }
}
