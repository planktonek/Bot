package discordbot;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.tritonus.share.ArraySet;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * This represents a SUPER basic bot (literally all it does is login). This is
 * used as a base for all example bots.
 */
public class BaseBot implements IListener<MessageReceivedEvent> {

	public static BaseBot INSTANCE; // Singleton instance of the bot.
	public IDiscordClient client; // The instance of the discord client.
	public ArraySet<String> admins = new ArraySet<>();
	public IChannel output = null;
	public Gamble gamble = new Gamble(0, 0, output, null);
	public Output out;
	public static void main(String[] args) { // Main method
		if (args.length < 2) // Needs a bot token provided
		{
			throw new IllegalArgumentException("This bot needs at Token and admin ID");
		}

		INSTANCE = new BaseBot(createClient(args[0], true), args[1]);
	}

	public BaseBot(IDiscordClient client, String admin) {
		this.client = client; // Sets the client instance to the one provided
		EventDispatcher dispatcher = client.getDispatcher(); // Gets the client's event dispatcher
		dispatcher.registerListener(this); // Registers this bot as an event listener
		admins.add(admin);
		out = new Output(client);
	}

	public static IDiscordClient createClient(String token, boolean login) { // Returns a new instance of the Discord client
		ClientBuilder clientBuilder = new ClientBuilder(); // Creates the ClientBuilder instance
		clientBuilder.withToken(token); // Adds the login info to the builder
		try {
			if (login) {
				return clientBuilder.login(); // Creates the client instance and logs the client in
			} else {
				return clientBuilder.build(); // Creates the client instance but it doesn't log the client in yet, you would have to call client.login() yourself
			}
		} catch (DiscordException e) { // This is thrown if there was a problem building the client
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void handle(MessageReceivedEvent t) {
		IMessage message = t.getMessage(); // Gets the message from the event object NOTE: This is not the content of the message, but the object itself
		IChannel channel = message.getChannel();
		if (message.getGuild() == null) {
			System.out.println("Got Private MSG");
			privateMsgParesr(t);
		} else {
			if(gamble.isActive() && output.equals(channel)) {
				if(message.getContent().equalsIgnoreCase("!join")) {
					gamble.add(message);
				}
			}
		}
	}

	public void privateMsgParesr(MessageReceivedEvent t) {
		IMessage message = t.getMessage();
		IChannel channel = message.getChannel();
		System.out.println(message.getAuthor().getID());
		if (admins.contains(message.getAuthor().getID())) {
			String msg = message.getContent();

			String[] command = msg.split("/");

			switch (command[0]) {
				case "start":
					if (command.length != 3) {
						out.sendMsgChannel(channel, "start/<ilość osób do wylosowania>/<czas w sekundach>");
						return;
					}
					int num = 0;
					int time = 0;
					try {
						num = Integer.parseInt(command[1]);
						time = Integer.parseInt(command[2]);
					} catch (NumberFormatException e) {
						out.sendMsgChannel(channel, "start/<ilość osób do wylosowania/<czas w sekundach>");
						return;
					}
					if (output == null) {
						out.sendMsgChannel(channel, "set channel by using channel command");
						return;
					}
					if(gamble.isActive()) {
						out.sendMsgChannel(channel, "losowanie w toku");
						return;
					}
					gamble = new Gamble(time, num, output, out);
					gamble.start();
					return;
				case "channel":
					if (command.length != 3) {
						out.sendMsgChannel(channel, "channel/<serwer>/<kanal>");
						return;
					}
					for (IGuild g : client.getGuilds()) {
						if (g.getName().equals(command[1])) {
							for (IChannel c : g.getChannels()) {
								if (c.getName().equals(command[2])) {
									out.sendMsgChannel(channel, "guild/channel found");
									output = c;
									return;
								}
							}
							out.sendMsgChannel(channel, "no channel found");
							return;
						}
					}
					out.sendMsgChannel(channel, "no guild found");
					return;
			}
		} else {
			out.sendMsgChannel(channel, message.getAuthor().getName() + "#" + message.getAuthor().getDiscriminator() + "Nie jesteś adminem - Bana chcesz?");
		}
	}	
}
