import java.util.ArrayList;
import java.util.List;

/*
 * Class to aid in sending packets via certain port and queue
 */

public class PacketSender {
	
	private List<Packet> list;
	UnsafeAccess ua;
	long packet_all;
	long packet_all_size;
	long packet_interval;
	long packet_interval_size;
	long past_sent;
	int port_id;
	int queue_id;
	
	int send_burst;
	
	private static final int DEFAULT_SEND_BURST = 32;
	private static final long MILLI_SECOND = 1000;
	private static final int SHORT_SIZE = 2;
	
	public PacketSender(int port_id, int queue_id) {
		list = new ArrayList<Packet>();
		ua = new UnsafeAccess();
		packet_all = 0;
		packet_all_size = 0;
		packet_interval = 0;
		packet_interval_size = 0;
		past_sent = System.currentTimeMillis();
		send_burst = DEFAULT_SEND_BURST;
		this.port_id = port_id;
		this.queue_id = queue_id;
	}
	
	public int getSendBurst() {
		return send_burst;
	}
	
	public void setSendBurst(int send_burst) {
		this.send_burst = send_burst;
	}
	
	// checks if the given time period has occurred since last packet sending
	// used so packets are held in memory for too long for no reason
	private boolean isTimedOut() {
		return (System.currentTimeMillis() - past_sent) >= MILLI_SECOND;
	}
	
	// packet added to sends list and checks made for
	// timeout period and list size
	public void sendPacket(Packet p) {
		list.add(p);
		if (list.size() >= send_burst || isTimedOut()) {
			sendBurst();
			past_sent = System.currentTimeMillis();
		}
	}
	
	// send burst of packets and also frees them via dpdk library
	// also contains stats data collection
	private void sendBurst() {
		int num = 0;
		if (list.size() > send_burst) {
			num = send_burst;
		} else {
			num = list.size();
		}
		long memory_needed = (num * ua.longSize()) + SHORT_SIZE;
		long pointer = ua.allocateMemory(memory_needed);
		ua.setCurrentPointer(pointer);
		
		ua.putShort(num);
		
		for (int i = 0; i < num; i++) {
			ua.putLong(list.get(i).getMbuf_pointer());

			packet_all_size += ((Ipv4Packet)list.get(i)).getLength(); // plus ethernet header?
			packet_interval_size += ((Ipv4Packet)list.get(i)).getLength();
		}
		
		list.subList(0, num).clear();
		
		packet_all += num;
		packet_interval += num;
		
		DpdkAccess.dpdk_send_packets(pointer, port_id, queue_id);

		packet_all += num;
		packet_interval += num;
		
		ua.freeMemory(pointer);
	}
	
	public long getPacketAll() {
		return packet_all;
	}
	
	public long getPacketAllSize() {
		return packet_all_size;
	}
	
	public long getPacketInterval() {
		return packet_interval;
	}
	
	public long getPacketIntervalSize() {
		return packet_interval_size;
	}
	
	public void resetInterval() {
		packet_interval = 0;
		packet_interval_size = 0;
	}

}
