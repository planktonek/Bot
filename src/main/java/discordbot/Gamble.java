package discordbot;

import java.util.HashSet;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;

/**
 *
 * @author Plankton
 */
public class Gamble extends TimerTask {

	int time;
	int num;
	IChannel channel;
	Output out;
	boolean active = false;
	HashSet<Entry> list = new HashSet<>();

	public Gamble(int time, int num, IChannel channel, Output output) {
		this.time = time;
		this.num = num;
		this.channel = channel;
		this.out = output;
	}

	public boolean isActive() {
		return active;
	}

	public void start() {
		active = true;
		out.sendMsgChannel(channel, "losowanie rozpoczęte\n napisz !join by dołączyć (ilość zwycięzców:" + String.valueOf(num) + ", czas:" + String.valueOf(time) + "s)");
		new Timer().schedule(this, time * 1000);
	}

	@Override
	public void run() {
		active = false;
		out.sendMsgChannel(channel, "losowanie zakończone");
		if (num > list.size()) {
			out.sendMsgChannel(channel, "za mało uczestników");
		}

		Entry ret = (Entry) list.toArray()[new Random().nextInt(list.size())];
		HashSet<Entry> win = new HashSet<>();
		for (int i = 0; i < num; i++) {
			while (win.add((Entry) list.toArray()[new Random().nextInt(list.size())]) == false);
		}

		StringBuilder build = new StringBuilder();
		for (Entry r : win) {
			build.append("<@" + r.message.getAuthor().getID() + "> ");
			out.addReaction(r.message, "👍");			
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
			Logger.getLogger(Gamble.class.getName()).log(Level.SEVERE, null, ex);
		}
		out.sendMsgChannel(channel, "Wygrał: " + build.toString());
	}

	void add(IMessage message) {
		if (list.add(new Entry(message))) {			
				out.addReaction(message, "😄");			
		}
	}

	class Entry {

		IMessage message;

		public Entry(IMessage message) {
			this.message = message;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Entry)) {
				return false;
			}
			Entry t = (Entry) obj;
			return message.getAuthor().equals(t.message.getAuthor());
		}

		@Override
		public int hashCode() {
			return message.getAuthor().hashCode();
		}

	}
}
