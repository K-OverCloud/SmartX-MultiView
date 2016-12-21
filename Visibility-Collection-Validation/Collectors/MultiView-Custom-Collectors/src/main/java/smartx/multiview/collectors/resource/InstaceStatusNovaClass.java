package smartx.multiview.collectors.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.StreamGobbler;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

public class InstaceStatusNovaClass implements Runnable{
	private Thread thread;
	private String ThreadName="vBox Status Thread";
	private String SmartXBox_USER, SmartXBox_PASSWORD;
	private String MGMTBox_IP, MGMTBox_USER, MGMTBox_PASSWORD;
	private String vboxMongoCollection, vboxMongoCollectionRT;
	private MongoClient mongoClient;
	private MongoDatabase db;
	private Document documentHistory, documentRT;
	private DeleteResult deleteResult;
	private Date timestamp;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
	//@SuppressWarnings("deprecation")
	
	public InstaceStatusNovaClass(String boxUser, String boxPassword, String dbHost, int dbPort, String dbName, String MGMT_IP, String MGMT_USER, String MGMT_PASSWORD, String OpenStackUSER, String OpenStackPASSWD, String OpenStackPROJECT, String OpenStackENDPOINT, String vboxhistory, String vboxrt) 
	{
		mongoClient           = new MongoClient(dbHost, dbPort);
		db                    = mongoClient.getDatabase(dbName);
		vboxMongoCollection   = vboxhistory;
		vboxMongoCollectionRT = vboxrt;
		SmartXBox_USER        = boxUser;
		SmartXBox_PASSWORD    = boxPassword;
		MGMTBox_IP            = MGMT_IP;
		MGMTBox_USER          = MGMT_USER;
		MGMTBox_PASSWORD      = MGMT_PASSWORD;
	}

	public void sshClient(String serverIp, String BoxName, String command)
    {
    	String instanceName, instanceID, instanceStatus, instancePower, instanceNetwork, instancetenantID;
    	timestamp = new Date();
    	try
        {
        	Connection conn = new Connection(serverIp);
            conn.connect();
            boolean isAuthenticated = conn.authenticateWithPassword(SmartXBox_USER, SmartXBox_PASSWORD);
            if (isAuthenticated == false)
                throw new IOException("Authentication failed.");        
            ch.ethz.ssh2.Session sess = conn.openSession();
            sess.execCommand(command);  
            InputStream stdout = new StreamGobbler(sess.getStdout());
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            
            while (true)
            {
            	String line = br.readLine();
            	if (line == null)
                    break;
                if (line!=null)
                {
                	if (!(line.contains("+-") || line.contains("Task State")))
                	{
                		instanceID       = line.substring(StringUtils.ordinalIndexOf(line, "|", 1)+1, StringUtils.ordinalIndexOf(line, "|", 2)-1).trim();
                		instanceName     = line.substring(StringUtils.ordinalIndexOf(line, "|", 2)+1, StringUtils.ordinalIndexOf(line, "|", 3)-1).trim();
                		instancetenantID = line.substring(StringUtils.ordinalIndexOf(line, "|", 3)+1, StringUtils.ordinalIndexOf(line, "|", 4)-1).trim();
                		instanceStatus   = line.substring(StringUtils.ordinalIndexOf(line, "|", 4)+1, StringUtils.ordinalIndexOf(line, "|", 5)-1).trim();
                		instancePower    = line.substring(StringUtils.ordinalIndexOf(line, "|", 6)+1, StringUtils.ordinalIndexOf(line, "|", 7)-1).trim();
                		instanceNetwork  = line.substring(StringUtils.ordinalIndexOf(line, "|", 7)+1, StringUtils.ordinalIndexOf(line, "|", 8)-1).trim();
                		
                		documentHistory = new Document();
    		        	documentRT      = new Document();
    		        	
    		        	documentHistory.put("timestamp"  , new Date());
    		        	documentHistory.put("box"        , BoxName);
    		        	documentHistory.put("tenantid"   , instancetenantID);
    		        	documentHistory.put("name"       , instanceName);
    		        	documentHistory.put("uuid"       , instanceID);
    		        	documentHistory.put("Powerstate" , instancePower);
    		        	documentHistory.put("Network"    , instanceNetwork);
    		    		
    		        	documentRT.put("box"             , BoxName);
    		        	documentRT.put("tenantid"        , instancetenantID);
    		    		documentRT.put("name"            , instanceName);
    		    		documentRT.put("uuid"            , instanceID);
    		    		
    		    		if (instanceStatus.equals("ACTIVE"))
    	            	{
    		    			documentHistory.put("state", "Running");
    	            		documentRT.put("state", "Running");
    	            	}
    	            	else
    	            	{
    	            		documentHistory.put("state", instanceStatus);
    	            		documentRT.put("state", instanceStatus);
    	            	}
    		    		//Insert New Documents to MongoDB
    		    		db.getCollection(vboxMongoCollectionRT).insertOne(documentRT);
    	            	db.getCollection(vboxMongoCollection).insertOne(documentHistory);
    		    		
    	            	System.out.println("["+dateFormat.format(timestamp)+"][INFO][NOVA][Box: "+BoxName+" Instance: "+instanceName+" State: "+instanceStatus+"]");
                	}
                }
            }
            //System.out.println("ExitCode: " + sess.getExitStatus());
            sess.close();
            conn.close();
        }
        catch (IOException e)
        {
        	System.out.println("[INFO][OVS-VM][Box : "+serverIp+" Failed");
            e.printStackTrace(System.err);
        }
    }
	
	public void getOSInstanceList() {
		//Delete Previous Documents from Real Time collection
    	deleteResult=db.getCollection(vboxMongoCollectionRT).deleteMany(new Document());
		
    	sshClient("210.125.84.68", "SaaS-Box1", "cat /opt/GetVMList/SaaSBox1VMs.list");
    	sshClient("210.125.84.68", "SaaS-Box2", "cat /opt/GetVMList/SaaSBox2VMs.list");
    	sshClient("210.125.84.68", "SaaS-Box3", "cat /opt/GetVMList/SaaSBox3VMs.list");
    	sshClient("210.125.84.68", "SaaS-Box4", "cat /opt/GetVMList/SaaSBox4VMs.list");
    	
	}
	public void run() 
	{
		while (true)
		{
			getOSInstanceList();
			try {
				//Sleep For 5 Minutes
				Thread.sleep(300000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	public void start() {
		System.out.println("Starting vBox Status Thread");
		if (thread==null){
			thread = new Thread(this, ThreadName);
			thread.start();
		}
	}
}

