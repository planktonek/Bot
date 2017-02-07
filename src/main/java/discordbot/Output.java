package discordbot;

import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 *
 * @author Plankton
 */
public class Output {

	IDiscordClient client; // The instance of the discord client.

	public Output(IDiscordClient client) {
		this.client = client;
	}

	public void sendMsgChannel(IChannel channel, String msg) {
		System.out.println("Send: " + msg + " to: " + channel.getName());
		boolean retry = true;
		while (retry) {
			try {
				retry = false;
				channel.sendMessage(msg);
			} catch (MissingPermissionsException | DiscordException ex) {
				Logger.getLogger(BaseBot.class.getName()).log(Level.SEVERE, null, ex);
			} catch (RateLimitException ex) {
				try {
					Thread.sleep(ex.getRetryDelay() + 100);
					retry = true;
				} catch (InterruptedException ex1) {
					Logger.getLogger(Output.class.getName()).log(Level.SEVERE, null, ex1);
				}
			}
		}
	}
	
	public void addReaction(IMessage msg, String reaction) {
		System.out.println("Add Raction to: " + msg.getContent());
		boolean retry = true;
		while (retry) {
			try {
				retry = false;
				msg.addReaction(reaction);
			} catch (MissingPermissionsException | DiscordException ex) {
				Logger.getLogger(BaseBot.class.getName()).log(Level.SEVERE, null, ex);
			} catch (RateLimitException ex) {
				try {
					Thread.sleep(ex.getRetryDelay() + 100);
					retry = true;
				} catch (InterruptedException ex1) {
					Logger.getLogger(Output.class.getName()).log(Level.SEVERE, null, ex1);
				}
			}
		}
	}
}
