import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/*
 * Starter class for Packet capture application
 */

public class PacketCap {
	
	public static void main(String[] args) {
		
		ApplicationStarter as = new ApplicationStarter();
		
		//as.setupGui(false);
		
		//List<ReceivePoller> rps = new ArrayList<ReceivePoller>();
		//List<PacketSender> pss = new ArrayList<PacketSender>();
		
		try {
			as.readConfig(new FileInputStream("config.properties"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		as.sendDPDKInformation();
		
		List<CoreThread> threads = new ArrayList<CoreThread>();
		
		ReceivePoller rp1 = new ReceivePoller(0, 0);
//		ReceivePoller rp2 = new ReceivePoller(0, 1);
		PacketSender ps = new PacketSender(0, 0);
		PacketFreeer pf = new PacketFreeer();
		
		//rps.add(rp);
		//pss.add(ps);
		
		threads.add(new CaptureProcessor(ps, pf, rp1));
//		threads.add(new CaptureProcessor(ps, pf, rp2));
		
		as.createAffinityThreads(threads);
		
		as.dpdk_init_eal();
		as.dpdk_create_mempool("mbufs", 8192, 32);
		as.dpdk_check_ports();
		as.dpdk_configure_dev(0, 2, 1);
		as.dpdk_configure_rx_queue(0, 0);
		as.dpdk_configure_rx_queue(0, 1);
		as.dpdk_configure_tx_queue(0, 0);
		as.dpdk_dev_start(0);
		as.dpdk_check_ports_link_status();

		as.dpdk_enable_pro();

		as.start_native_stats();
		
		//as.updateStatsInfo(rps, pss);
		
		as.startAll();
		
	}
	
}
