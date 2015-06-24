import java.util.ArrayList;
import java.util.List;

/*
 * Class to receive packets from given port and queue
 */

public class ReceivePoller {
	
	UnsafeAccess ua;
	long packet_all;
	long packet_all_size;
	long packet_interval;
	long packet_interval_size;
	int port_id;
	int queue_id;
	
	int get_size;
	
	UnsafeAccess unsafe;
	//List<Packet> packets;
	PacketList packets;
	
	long mem_pointer;
	int memory_size;
	
	private static final int DEFAULT_GET_SIZE = 32;

	public ReceivePoller(int port_id, int queue_id) {
		ua = new UnsafeAccess();
		packet_all = 0;
		packet_all_size = 0;
		packet_interval = 0;
		packet_interval_size = 0;
		get_size = DEFAULT_GET_SIZE;
		this.port_id = port_id;
		this.queue_id = queue_id;
		unsafe = new UnsafeAccess();
		//packets = new ArrayList<Packet>();
		packets = new PacketList();
		// each packet sends back mbuf and packet_header pointer -> therefore 2
		memory_size = ((Long.SIZE / Byte.SIZE) * get_size * 2) + Short.SIZE;
		mem_pointer = ua.allocateMemory(memory_size);
	}
	
	public int getGetSize() {
		return get_size;
	}
	
	public void setGetSize(int get_size) {
		this.get_size = get_size;
	}
	
	// gets burst of packets and create packet objects via dpdk library
	// also contains stats data collection
	public PacketList getBurst() {
		//long temp = mem_pointer;

		DpdkAccess.dpdk_receive_burst(mem_pointer, port_id, queue_id);
		
		ua.setCurrentPointer(mem_pointer);
		
		int packet_count = ua.getShort();
		//packet_all += packet_count; 
		//packet_interval += packet_count;
		
		
		
		if (packet_count > 0) {
			//packets.clear();
			packets.reset(packet_count, mem_pointer+2);
			return packets;
			/*temp += ua.getOffset();
			
			for (int i = 0; i < packet_count; i++) {

				long new_pointer = temp + (2*i*ua.longSize());
				ua.setCurrentPointer(new_pointer);
				long mbuf = ua.getLong();
				long packet = ua.getLong();
				
				Packet p = new Ipv4Packet(mbuf, packet, unsafe);
				//if (p.getVersion() == 6) {
					//p = new Ipv6Packet(mbuf, packet);
				//}
				
				//packet_all_size += p.getLength(); // plus ethernet header?
				//packet_interval_size += p.getLength();
				
				//packets.add(p);
			}*/
		}
		return null;
		//return packets;
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