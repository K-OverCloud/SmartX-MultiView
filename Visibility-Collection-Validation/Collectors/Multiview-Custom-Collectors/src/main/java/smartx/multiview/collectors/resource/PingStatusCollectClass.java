/**
 * @author Muhammad Usman
 * @version 0.1
 */

package smartx.multiview.collectors.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.result.DeleteResult;

import smartx.multiview.DataLake.MongoDB_Connector;

public class PingStatusCollectClass implements Runnable {
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	@SuppressWarnings("unused")
	private DeleteResult deleteResult;
	private float latency;
	private MongoDB_Connector mongoConnector;

	private Document NewDocument;
	private FindIterable<Document> pBoxList;
	private String pboxMongoCollection, pboxstatusMongoCollection, pboxstatusMongoCollectionRT;
	private Thread thread;

	private String ThreadName = "Physical Path Thread";

	private Date timestamp;
	private String VisibilityCenter, BoxIP, pingResult = "";

	// private static Logger logger =
	// Logger.getLogger(PingStatusCollectClass.class.getName());

	public PingStatusCollectClass(String visibilityCenter, MongoDB_Connector MongoConn, String pbox, String pboxstatus,
			String pboxstatusRT) {
		VisibilityCenter = visibilityCenter;
		mongoConnector = MongoConn;
		pboxMongoCollection = pbox;
		pboxstatusMongoCollection = pboxstatus;
		pboxstatusMongoCollectionRT = pboxstatusRT;
	}

	public void getPingStatus() {
		// Remove Previous records from Collection (For good performance)
		deleteResult = mongoConnector.deleteDataDB(pboxstatusMongoCollectionRT);

		// Get List of SmartX Boxes
		pBoxList = mongoConnector.getDataDB(pboxMongoCollection);

		pBoxList.forEach(new Block<Document>() {
			public void apply(final Document document) {
				pingResult = "";
				BoxIP = (String) document.get("management_ip");

				String pingCmd = "nmap -sP -R " + BoxIP;
				try {
					Runtime r = Runtime.getRuntime();
					Process p = r.exec(pingCmd);

					BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String inputLine;
					timestamp = new Date();

					while ((inputLine = in.readLine()) != null) {
						pingResult += inputLine;
					}

					if (pingResult.contains("Host seems down") == true) {
						writeDB("", new Date(), VisibilityCenter, BoxIP, "Down", 0);
						System.out.println("[" + dateFormat.format(timestamp) + "][INFO][PING][Box: " + BoxIP
								+ " Management Status: Down]");
					} else {
						latency = Float.parseFloat(pingResult.substring(pingResult.indexOf("Host is up (") + 12,
								pingResult.indexOf(" latency") - 1));
						writeDB("", new Date(), VisibilityCenter, BoxIP, "Up", latency);
						System.out.println("[" + dateFormat.format(timestamp) + "][INFO][PING][Box: " + BoxIP
								+ " Management Status: Up]");
					}
					in.close();
				} catch (IOException e) {
					System.out.println("[INFO][PING][Box : " + BoxIP + " Failed " + e);
				}
			}
		});
	}

	public void run() {
		while (true) {
			getPingStatus();
			try {
				// Sleep For 1 Minute
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void start() {
		System.out.println("Starting Nmap Ping Test");
		if (thread == null) {
			thread = new Thread(this, ThreadName);
			thread.start();
		}
	}

	public void writeDB(String json1, Date timestamp, String src, String dest, String stat, float latency) {
		NewDocument = new Document();
		NewDocument.put("timestamp", new Date());
		NewDocument.put("source", src);
		NewDocument.put("destination", dest);
		NewDocument.put("status", stat);
		NewDocument.put("latency", latency);

		mongoConnector.insertDataDB(pboxstatusMongoCollection, NewDocument);
		mongoConnector.insertDataDB(pboxstatusMongoCollectionRT, NewDocument);
	}
}
