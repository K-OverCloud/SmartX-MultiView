var MongoClient = require('mongodb').MongoClient;
var Db = require('mongodb').Db;
var Connection = require('mongodb').Connection;
var Server = require('mongodb').Server;
var BSON = require('mongodb').BSON;
var ObjectID = require('mongodb').ObjectID;
var dateFormat  = require('dateformat');
var mongourl = "mongodb://103.22.221.56:27017/overclouddb";

ResourceProvider = function() {};
//UserProvider = function() {};

//Get MultiView Users
ResourceProvider.prototype.getUsers = function(callback)
{
    MongoClient.connect(mongourl, function(err, db)
    {
        var collection = db.collection("configuration-multiview-users");
        collection.find().toArray(function(err, users){
            callback(null,users);
		});
    });
};

//Get Controllers List From MongoDB New
ResourceProvider.prototype.getControllerList = function(callback) 
{
	MongoClient.connect(mongourl, function(err, db)
    {
        var collection = db.collection("playground-controllers-list");
        collection.find({},{controllerIP: true, controllerName: true, controllerStatus: true, controllerSoftware: true, _id: false}).sort({controllerName: -1}).toArray(function(err, controllers){
	     	callback(null, controllers);
		    db.close();
		});
	 });
};

//Get pBoxes List From MongoDB
ResourceProvider.prototype.getpBoxList = function(callback) 
{
    MongoClient.connect(mongourl, function(err, db)
    {
        console.log('Physical Boxes List: ');
		// Locate all the entries using find
        var collection = db.collection("configuration-pbox-list");
        //collection.find({type: 'B**'},{box: true, host: true, management_ip: true, management_ip_status: true, data_ip: true, data_ip_status: true, control_ip: true, control_ip_status: true, _id: false}).sort({host: -1}).toArray(function(err, boxes){
		//collection.find({$or:[{type: 'B**'},{type: 'C**'}]},{box: true, boxID: true, management_ip: true, management_ip_status: true, data_ip: true, data_ip_status: true, control_ip: true, control_ip_status: true, _id: false}).sort({host: -1}).toArray(function(err, boxes){
		collection.find( {}, {box: true, boxID: true, management_ip: true, management_ip_status: true, data_ip: true, data_ip_status: true, control_ip: true, control_ip_status: true, _id: false}).sort({host: -1}).toArray(function(err, boxes){
			//	db.close();
			callback(null,boxes);
	});
	//console.log (db.boxes);
    });
};

//Get vSwitches List From MongoDB
ResourceProvider.prototype.getvSwitchList = function(callback)
{
    MongoClient.connect(mongourl, function(err, db)
    {
        console.log('OVS bridges List: ');
        var collection = db.collection("configuration-vswitch-list");
        collection.find({},{type: true, bridge: true, topologyorder: true, _id: false}).sort({topologyorder: 1}).toArray(function(err, switchList){
			//db.close();
			callback(null,switchList);
        });
    });
};

//Get OpenStack Instances List From MongoDB
ResourceProvider.prototype.getvBoxList = function(callback)
{
    MongoClient.connect(mongourl, function(err, db)
    {
        console.log('OpenStack instances List: ');
        var collection = db.collection("configuration-vbox-list");
        collection.find({},{box: true, name: true, osusername: true, ostenantname: true, vlanid: true, state: true, _id: false}).sort({box: -1}).toArray(function(err, vBoxList){
                //db.close();
                callback(null,vBoxList);
        });
    });
};

//Get Services List From MongoDB
ResourceProvider.prototype.getServicesList = function(callback)
{
    MongoClient.connect(mongourl, function(err, db)
    {
        console.log('Services List: ');
        var collection = db.collection("configuration-service-list");
        collection.find({type: 'B**'},{box: true, name: true, osusername: true, ostenantname: true, vlanid: true, state: true, _id: false}).sort({box: -1}).toArray(function(err, vmList){
                //db.close();
                callback(null,vmList);
        });
    });
};

//Get pPath Status From MongoDB
/*ResourceProvider.prototype.getpPathStatus = function(callback)
{
    MongoClient.connect('mongodb://210.125.84.69:27017/smartxdb', function(err, db)
    {
        console.log('Physical Path Status: ');
        var collection = db.collection("configuration-pbox-path-status");
        collection.find({},{box: true, m_ip: true, d_ip: true, c_ip: true, _id: false}).sort({box: -1}).toArray(function(err, pPathStatus){
                //db.close();
                callback(null,pPathStatus);
        });
    });
};*/

//Get OVS Bridge Status From MongoDB
ResourceProvider.prototype.getovsBridgeStatus = function(callback)
{
    MongoClient.connect(mongourl, function(err, db)
    {
        console.log('OVS Bridge Status: ');
        var collection = db.collection("configuration-vswitch-status");
        collection.find({},{boxID: true, bridge: true, status: true, _id: false}).sort({boxID: -1}).toArray(function(err, ovsBridgeStatus){
                //db.close();
                callback(null,ovsBridgeStatus);
        });
    });
};

//Get Operator Controller Flow Rules
ResourceProvider.prototype.getOpsSDNConfigList = function(boxID, callback)
{
    MongoClient.connect(mongourl, function(err, db)
    {
	console.log('Flow Rules List: '+boxID);
	var currentTime = new Date();
	console.log('System Time: '+currentTime);
	dateFormat(currentTime.setMinutes(currentTime.getMinutes() - 59));
	console.log('Updated Time: '+currentTime);
       	var collection = db.collection("flow-configuration-sdn-controller-rt");
        collection.find({controllerIP: '103.22.221.152', boxID: boxID},{_id: false}).sort({name: -1}).toArray(function(err, rulesList){
                //db.close();
                callback(null,rulesList);
        });
    });
};

//Get Operator Controller Flow Statistics
ResourceProvider.prototype.getOpsSDNStatList = function(boxID, callback)
{
    MongoClient.connect(mongourl, function(err, db)
    {
        console.log('Flow Statistics List: ');
        var currentTime = new Date();
        console.log('System Time: '+currentTime);
        dateFormat(currentTime.setMinutes(currentTime.getMinutes() - 5));
        console.log('Updated Time: '+currentTime);
        var collection = db.collection("flow-stats-sdn-controller-rt");
        collection.find({controllerIP: '103.22.221.152', boxID: boxID},{_id: false}).sort({name: -1}).toArray(function(err, statList){
            //db.close();
            callback(null,statList);
        });
    });
};

exports.ResourceProvider = ResourceProvider;
//exports.UserProvider = UserProvider;
