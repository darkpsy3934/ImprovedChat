package wdmods.improvedchat;

import java.io.File;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import net.minecraft.src.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import wdmods.improvedchat.overrides.GuiImprovedChatNewChat;

import com.mumfrey.liteloader.ChatFilter;
import com.mumfrey.liteloader.InitCompleteListener;
import com.mumfrey.liteloader.LoginListener;
import com.mumfrey.liteloader.RenderListener;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.util.ModUtilities;

/**
 * LiteLoader adapter class for Improved Chat
 * 
 * @author Adam Mummery-Smith
 */
public class LiteModImprovedChat implements InitCompleteListener, RenderListener, LoginListener, ChatFilter
{
	/**
	 * Keypress mask used to determine when keys are released
	 */
	private boolean[] pressed = new boolean[400];
	
	/**
	 * New persistent chat GUI 
	 */
	private GuiImprovedChatNewChat persistentChatGui;
	
	@Override
	public String getName()
	{
		return "Improved Chat";
	}
	
	@Override
	public String getVersion()
	{
		return "3.1.0";
	}
	
	@Override
	public void init(File configPath)
	{
	}
	
	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath)
	{
	}
	
	/* (non-Javadoc)
	 * @see com.mumfrey.liteloader.InitCompleteListener#onInitCompleted(net.minecraft.client.Minecraft, com.mumfrey.liteloader.core.LiteLoader)
	 */
	@Override
	public void onInitCompleted(Minecraft minecraft, LiteLoader loader)
	{
		ImprovedChat.init(minecraft);
	}

	/* (non-Javadoc)
	 * @see com.mumfrey.liteloader.RenderListener#onRender()
	 */
	@Override
	public void onRender()
	{
		Minecraft minecraft = Minecraft.getMinecraft();
		
		// Replace persistent chat GUI
		if (minecraft.ingameGUI != null && minecraft.ingameGUI.getChatGUI() != null && !(minecraft.ingameGUI.getChatGUI() instanceof GuiImprovedChatNewChat))
		{
			try
			{
				// TODO Obfuscation - last update @ 1.6.4
				Field fChat = GuiIngame.class.getDeclaredField(ModUtilities.getObfuscatedFieldName("persistantChatGUI", "h", "field_73840_e"));
				fChat.setAccessible(true);
				if (persistentChatGui == null) persistentChatGui = new GuiImprovedChatNewChat(minecraft);
				fChat.set(minecraft.ingameGUI, persistentChatGui);
			}
			catch (Exception ex)
			{
				System.out.println("[ImprovedChat] ERROR OVER-RIDING CHAT GUI - improved chat probably won't function!");
			}
		}
		
		// Replace chat GUI
		if (minecraft.currentScreen != null && minecraft.currentScreen instanceof GuiChat && !(minecraft.currentScreen instanceof GuiImprovedChat) && !(minecraft.currentScreen instanceof GuiSleepMP))
		{
			minecraft.currentScreen = new GuiImprovedChat((GuiChat)minecraft.currentScreen);
		}

		// Replace sleep chat GUI
		if (minecraft.currentScreen != null && minecraft.currentScreen instanceof GuiSleepMP && !(minecraft.currentScreen instanceof GuiImprovedChatSleeping))
		{
			minecraft.currentScreen = new GuiImprovedChatSleeping((GuiSleepMP)minecraft.currentScreen);
		}
	}

	/* (non-Javadoc)
	 * @see com.mumfrey.liteloader.RenderListener#onRenderWorld()
	 */
	@Override
	public void onRenderWorld()
	{
	}
	
	@Override
	public boolean onChat(Packet3Chat chatPacket, ChatMessageComponent chat, String message)
	{
		return true;
	}
	
	@Override
	public void onLogin(NetHandler netHandler, Packet1Login loginPacket)
	{
		if (netHandler instanceof NetClientHandler)
		{
			SocketAddress socketAddress = ((NetClientHandler)netHandler).getNetManager().getSocketAddress();
			
			if (socketAddress instanceof InetSocketAddress)
			{
				InetSocketAddress inetAddr = (InetSocketAddress)socketAddress;
				
				String serverName = inetAddr.getHostName();
				int serverPort = inetAddr.getPort();
				
		        System.out.println("[ImprovedChat] Loading settings for " + serverName + ":" + serverPort);
		        ImprovedChat.setCurrent(serverName + "_" + serverPort);
			}
		}
	}
	
	@Override
	public void onTick(Minecraft minecraft, float partialTicks, boolean inGame, boolean clock)
	{
		for (int bindingIndex = -100; bindingIndex < 255; bindingIndex++)
		{
			boolean isPressed = getKeyPressed(bindingIndex);
			
			if (!isPressed && pressed[bindingIndex + 128])
			{
				if (minecraft.currentScreen == null || minecraft.currentScreen.allowUserInput)
					ImprovedChat.keyPressed(bindingIndex);
			}
			else if (isPressed && !pressed[bindingIndex + 128])
			{
		        if ((Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) && Keyboard.isKeyDown(Keyboard.KEY_TAB))
		        {
		            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
		            {
		                ImprovedChat.getCurrentServer().previousTab();
		            }
		            else
		            {
		                ImprovedChat.getCurrentServer().nextTab();
		            }
		        }
			}
			
			pressed[bindingIndex + 128] = isPressed;
		}
	}

	private boolean getKeyPressed(int bindingIndex)
	{
		if (bindingIndex < 0)
		{
			return Mouse.isButtonDown(bindingIndex + 100);
		}
		
		return Keyboard.isKeyDown(bindingIndex);
	}

	@Override
	public void onRenderGui(GuiScreen currentScreen)
	{
	}

	@Override
	public void onSetupCameraTransform()
	{
	}
	
}
