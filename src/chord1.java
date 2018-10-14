//
//  Chord1
//  フラットなChord
//  ノード毎にリフレッシュ間隔、参加脱退間隔、query間隔を設定できていない
//  hash_maxは大きな値とし、それより小さな値を参加ノード数とする
//    理由：参加・脱退をしたとき、hash_maxが狭いと、同じIDで参加するノードが
//          できる。それでは、refreshをしなくても、成功してしまう。
//          リフレッシュを高頻度でしないと、成功しないようにするには、join
//          するノードができるだけ異なるIDとなるようにする必要がある。
//          従って、hash_maxを大きくする必要がある。
//
//
//import java.io.*;
import java.util.*;
////////////////////////////////////////////////////////////////////////////
class Event{
	long node_id;   /* node ID */
	int event_type;   /* 1: join node, 2: delete node, 3: join_req, 4: delete_req,
	5: query_req, 6: reply, 7: ID list refresh, 8: ?*/
	int  seen;    /* seen query flag */
	int  hop;    /* Number of hops */
	long new_node_id;  /* sanka node id */
	long delete_node_id; /* dattai node id */
	long query_id;   /* Query code */
	long query_node_id;  /* initiator of Query */
	long reply_type;  /* 0: OK, 1: NG */
	long reply_node;  /* sender of reply */
	double etime;   /* occured time */
	long sender; /*define  a transformer*/
	
}
//////////////////////////////////////////////////////////////////////////
class  zipf{
	int cache_id;
	int cache_num;
}
////////////////////////////////////////////////////////////////////////
class Cache{
	long    cache_id;// cache id
	double time_limit;// time limit
	
}
/////////////////////////////////////////////////////////////////////////////
public class chord1{
	static final int HASH_MAX = 32768; // Hash space size 2^15
	static final int HOP_MAX = 20; // Max number of hops of query
	
	static final int E_JOINNODE = 1;
	static final int E_DELENODE = 2;
	static final int E_JOINNREQ = 3;
	static final int E_DELENREQ = 4;
	static final int E_QUERYREQ = 5;
	static final int E_REPLYREQ = 6;
	static final int E_REFRESH = 7;
	static final int E_CACHE = 8;
	
	static final float END_TIME = 180000.0F;
	static final float QUERY_INTERVAL = 200.0F ;
	static final float REFRESH_INTERVAL = 1000.0F;
	static final float JOIN_DEL_INTERVAL = 10000.0F;
	static final float DELAY = 0.01F;
	static final int INITIALNODE =512;
	static final int MAXQUERY = 100000;
	static final double CACHERATE = 0;
	static final double CACHELIMIT = 30;
	static final boolean CACHE_OF_CACHE = false;//create "cache of cache" or not
	
	static int node_cache_count[] = new int[HASH_MAX];
	static double time = 0.0;
	static double l_time;
	static long total_query = 0;
	static long total_success = 0;
	static long total_fail = 0;
	static long total_fail1 = 0;
	static long total_fail2 = 0;
	static long total_fail_timeover = 0;
	static long total_join = 0;
	static long total_delete = 0;
	static long total_hop = 0;
	static long total_hit = 0;
	static long total_refresh =0;
	static Vector<Event> sched;
	static Vector<NodeInstance> node;
	static Random r,r1,r2;
	static int queue = 0;
	static Event top, current;
	static float refresh_int[] = new float[HASH_MAX]; // 2048 is HASH_MAX
	static float query_int[] = new float[HASH_MAX];
	static float join_del_int[] = new float[HASH_MAX];
	static int[] nodequeue = new int[1000];
	static double[] nodeprob = new double[100];//target query id 
	static int Cache_total = 0;
	static int nodecachetotal;
	static int total_re = 0;
	// static double queryrate[] = new double[16];
	/////////////////////////////////////////////////////////////////////////
	public static void zipfnode()
	{
		int i,j;
		double total = 0;
		
		//////////////////////////////////////////////////////
		for(i= 0;i<100;i++){
			nodeprob[i]= (double)(1)/(i+1);//(1/n)
			total =(double)(1)/(i+1)+total;// sum of (1/n)
		}
		for(i=0;i<100;i++){
			nodeprob[i]= nodeprob[i]/total; // (1/n)/sum of (1/n)
			
		}
		for(i=1;i<100;i++){
			nodeprob[i]=nodeprob[i-1]+nodeprob[i];//
			if(i==99)
			nodeprob[i]=1.0;
			System.out.println("nodeprob["+i+"]="+ nodeprob[i]);
		}
		
	}
	
	//////////////////////////////////////////////////////////////////////////
	
	public static int zipfgetid(){
		//target特定値に指定します
		int target = 6721;
		int i,j;
	//	Random r = new Random();
		double p = r1.nextDouble();
		
		
		for(i=0;i<100;i++){
			if(p<nodeprob[i])
				break;

			target = ((target * 971+791)%(HASH_MAX));

		}
//		System.out.println("p="+p+" target = "+target);
		return target;
		
		
	}
	
	
	
	
	
	////////////////////////////////////////////////////////////////////////////
	public static void schedule(Event event0, float interval){
		Event event1;
		double d;
		int j;
		
		d = r1.nextDouble();
		d = (-1.0)*Math.log(d)*interval;
		event0.etime = time + d;
		if(time + d > END_TIME+100)
		return;
		for(j=0; j<sched.size(); j++){
			event1 = (Event)sched.elementAt(j);
			if(event0.etime < event1.etime){
				sched.insertElementAt(event0, j);
				break;
			}
		}
		if(j == sched.size())
		sched.addElement(event0);
		
	}
	
	////////////////////////////////////////////////////////////////////////////
	
	static void e_joinnode(Event e){
		int j;
		int i;
		long nodeid;
		NodeInstance node0, node1;
		Event event1;
		
		if(node.size() < HASH_MAX){
			
			total_join++;
			System.out.println("### join_node is processed!!");
			for(;;){ // exclude the used nodeID
				nodeid = r1.nextInt(HASH_MAX);
				
				for(j=0; j<node.size(); j++){
					node1 = (NodeInstance)node.elementAt(j);
					if(node1.Node_ID == nodeid){
						break;
					}
				}
				if(j == node.size())
				break;
			}
			// insert node
			node0 = new NodeInstance(HASH_MAX, nodeid);
			for(j=0; j<node.size(); j++){
				node1 = (NodeInstance)node.elementAt(j);
				if(node1.Node_ID > node0.Node_ID){
					node.insertElementAt(node0, j);
					node0.refresh(node);
					////     node0.showFinger();
					break;
				}
			}
			if(j == node.size()){
				node.addElement(node0);
				node0.refresh(node);
				////    node0.showFinger();
			}
			
			// new query event schedule
			event1 = new Event();
			event1.node_id = nodeid;
			event1.event_type = 5; // Query request
			event1.seen =0;
			event1.hop = 0;
			event1.query_id = zipfgetid();
			
			
			
			
			event1.query_node_id = nodeid; // query node is me.
			event1.sender = nodeid;
			schedule(event1, query_int[(int)e.node_id]);
			
			// new refresh event schedule
			event1 = new Event();
			event1.node_id = nodeid;
			event1.event_type = 7; // refresh
			//   schedule(event1,refresh_int[(int)e.node_id]);
			schedule(event1,REFRESH_INTERVAL);
			
			//   System.out.println("### joined node is " + nodeid);
		}
		
		
		/*
		for(j=0;j<node.size();j++){
			node1 = (NodeInstance)node.elementAt(j);
			node1.refresh(node);
		}
		*/
		// delete schedule
		/*  event1 = new Event();
		event1.event_type = 2; // delete node
		event1.delete_node_id = e.node_id;
		System.out.println("### delete node is scheduled");
		//  schedule(event1, join_del_int[(int)e.node_id]);
		schedule(event1, JOIN_DEL_INTERVAL);
		*/
	}
	/*=============================================================*/
	static void e_delnode(Event e){
		int j;
		Event event1;
		NodeInstance node1=null;
		// NodeInstance node2, node3;
		
		// leave and remove process
		//  System.out.println("### delete_node is processed!!");
		total_delete++;
		
		if(node.size() > 1){
			
			for(j=0; j<node.size(); j++){
				node1 = (NodeInstance)node.elementAt(j);
				if(node1.Node_ID == e.node_id){
					break;
				}
			}
			if(j != node.size()){
				node.remove(j);
			}
			
		}
		
		/*
		for(j=0;j<node.size();j++){
			node1 = (NodeInstance)node.elementAt(j);
			node1.refresh(node);
		}
		*/
		
		
		
		// join schedule
		/* event1 = new Event();
		event1.event_type = 1; // join node
		event1.node_id = e.node_id;
		System.out.println("### joined node is scheduled");
		//  schedule(event1, join_del_int[(int)e.node_id]);
		schedule(event1, JOIN_DEL_INTERVAL);
		*/
		//  System.out.println("### deleted node is "+e.node_id);
	}
	// /*=============================================================
	// static void e_joinreq(Event e){
		//  queue++;
		//
	// }
	// /*=============================================================
	// static void e_delreq(Event e){
		//
		//
		//
	// }
	
	/*=============================================================*/
	static void e_queryreq(Event e){
		int j;
		int i;
		NodeInstance node1=null;
		NodeInstance node2=null;
		NodeInstance node3=null;
		//  System.out.println("### query is processed!! nodeid = "+ e.node_id);
		// schedule next query event
		if(e.node_id == e.query_node_id && e.seen == 0){
			total_query++;
			//   System.out.println("### query is count up = "+ total_query);
			Event event1 = new Event();
			event1.node_id = e.node_id;
			event1.event_type = 5; // Query request
			event1.seen = 0;
			event1.hop = 0;
			event1.query_id = zipfgetid();
			event1.query_node_id = event1.node_id; // query node is me.
			event1.sender = e.node_id;
			//   schedule(event1, query_int[(int)e.node_id]);
			schedule(event1, QUERY_INTERVAL);
			
		}else if((e.node_id == e.query_node_id && e.seen != 0)
			|| (e.node_id != e.query_node_id && e.hop > HOP_MAX)){
			e.event_type = 6; // reply
			e.reply_node = e.node_id;
			e.node_id = e.query_node_id;
			e.reply_type = 1; // NG
			total_fail1++;
			if(e.node_id != e.query_node_id && e.hop > HOP_MAX)
			total_fail_timeover++;
			schedule(e, DELAY);
			return;
		}
		
		// processing query code
		// (1)cache deletion
		
		for(j=0; j<node.size(); j++){
			node1 = (NodeInstance)node.elementAt(j);
			
			if(node1.del_cache(time))
			{
				total_re++;
			};
		}
		
		//(2)node1 is node object that  Node_ID is e.node_id
		for(j=0; j<node.size(); j++){
			node1 = (NodeInstance)node.elementAt(j);
			if(node1.Node_ID == e.node_id){
				break;
			}
		}
		
		if(j < node.size()){//node_id is in the node list
		//	System.out.println("cache has been checked. "+e.query_id);
			if(node1.search_cache(e.query_id)){
				total_hit = total_hit + 1;
				Event event1 = new Event();
				event1.event_type = 6; // reply
				event1.reply_node = e.node_id;
				event1.node_id = e.query_node_id;
				event1.reply_type = 0; // OK
				total_hop = total_hop + e.hop;
				schedule(event1, DELAY);
				
				
				if(CACHE_OF_CACHE){
					//---------> cache of cache
					double  percent1;
				//	Random r = new Random();
					percent1 = r2.nextDouble();
					if(percent1<=CACHERATE)
					{
						Cache_total++;
						Event event2 = new Event();
						event2.query_id = e.query_id;
						event2.event_type = 8;
						event2.node_id = e.sender;
						event2.sender = e.node_id;
						schedule(event2,DELAY);
					
					//     System.out.println("cache has been created.");
					
					}
				}
			
				return;
			}
		}
		
		
		if(j == node.size() && e.seen != 0 ){   // query fail because node was gone.
			System.out.println("?????? fail2 ??????");
			System.out.println("node_id="+ e.node_id);
			System.out.println("query_node_id="+e.query_node_id);
			System.out.println("query_id="+e.query_id);
			System.out.println("seen="+e.seen);
			System.out.println("etime="+e.etime);
			Event event1 = new Event();
			event1.event_type = 6; // reply
			event1.reply_node = e.node_id;
			event1.node_id = e.query_node_id;
			event1.reply_type = 1; // NG
			total_fail2++;
			
			schedule(event1, DELAY);
		}else
		if(node1.query(e.query_id)){  // query success
			Event event1 = new Event();
			event1.event_type = 6; // reply
			event1.reply_node = e.node_id;
			event1.node_id = e.query_node_id;
			event1.reply_type = 0; // OK
			total_hop = total_hop + e.hop;
			schedule(event1, DELAY);
			
			double  percent1,pencent2;
//			Random r = new Random();
			percent1 = r2.nextDouble();

			if(percent1<=CACHERATE)
			{
				Cache_total++;
				Event event2 = new Event();
				event2.query_id = e.query_id;
				event2.event_type = 8;
				event2.node_id = e.sender;
				event2.sender = e.node_id;
				schedule(event2,DELAY);
				
				//     System.out.println("cache has been created.");
				
			}
			
		}
		
		else{
			//   System.out.println("### query nexthop search start !! nodeid = "+node1.Node_ID);
			Event event1 = new Event();
			event1.event_type = e.event_type;
			event1.node_id = (long)node1.nexthop(e.query_id); // trasfer query
			event1.seen = 1;
			event1.query_node_id = e.query_node_id;
			event1.query_id = e.query_id;
			event1.hop = e.hop+1;
			event1.sender = e.node_id;
			//   System.out.println("### query nexthop is "+e.node_id);
			schedule(event1, DELAY);
		}
		
	}
	
	
	/*=============================================================*/
	static void e_replyreq(Event e){
		//  System.out.println("### reply is processed!!");
		if(e.reply_type == 0){ // success
			total_success++;
			//   System.out.println("query completed at node "+e.reply_node);
		}else{
			total_fail++;
			//   System.out.println("query failed at node "+e.reply_node);
		}
	}
	/*=============================================================*/
	static void e_refresh(Event e){
		int j;
		
		NodeInstance node1=null;
		total_refresh++;
		
		//  System.out.println("### refresh is processed!!");
		for(j=0; j<node.size(); j++){
			node1 = (NodeInstance)node.elementAt(j);
			
			if(node1.Node_ID == e.node_id){
				break;
			}
		}
		
		if(j != node.size()){
			node1.refresh(node);
			
			// print finger table !!
			/*
			if(e.node_id == 0){
				System.out.println("Member of ring ");
				for(j=0; j<node.size(); j++){
					node2 = (NodeInstance)node.elementAt(j);
					System.out.print(j+"="+node2.Node_ID+"  ");
				}
				System.out.println(" ");
				for(j=0; j<node.size(); j++){
					node2 = (NodeInstance)node.elementAt(j);
					System.out.print(j+"="+node2.Former_Node.Node_ID+"  ");
				}
				System.out.println(" ");
				for(j=0; j<node.size(); j++){
					node2 = (NodeInstance)node.elementAt(j);
					System.out.print(j+"="+node2.Successor_Node.Node_ID+"  ");
				}
				System.out.println(" ");
				System.out.println("Finger table of node 0");
				node1.showFinger();
			}
			*/
			//   schedule(e, refresh_int[(int)e.node_id]);
			schedule(e, REFRESH_INTERVAL);
		}
	}
	
	/*=============================================================*/
	static void e_cache(Event e){
		
		Cache c = new Cache();
		
		NodeInstance node1;
		for(int i = 0;i<node.size();i++){
			node1 = node.elementAt(i);
			if(node1.Node_ID ==e.node_id)
			{
				c.cache_id =e.query_id;
				c.time_limit = time + CACHELIMIT;
				node1.add_cache(c);
			}
			
		}
		
	}
	
	
	
	
	
	///////////////////////////////////////////////////////////////////////
	//
	//    MAIN FUNCTION
	//
	///////////////////////////////////////////////////////////////////////
	public static void main(String proD[ ]){
		//
		int i,j;
		int itime;
		long total_event;
		double error_rate;
		int flag;
		long nodeid=0;
		NodeInstance node0, node1;
		Event event0, event1;
		
		sched = new Vector<Event>();
		node = new Vector<NodeInstance>();
		r1 = new Random();
		r2 = new Random();
		
		// getID();
		System.out.println("Zipf function called");
		
		
		
		// Interval setting
		
		for (i=0; i<HASH_MAX; i++){ // Interval setting
			refresh_int[i] = REFRESH_INTERVAL;
			query_int[i] = QUERY_INTERVAL;
			join_del_int[i] = JOIN_DEL_INTERVAL;
		}
		
		
		
		// NODE initialize
		for (i=0; i<INITIALNODE; i++){
			
			for(;;){ // exclude the used nodeID
				nodeid = r1.nextInt(HASH_MAX);
				flag = 0;
				for(j=0; j<node.size(); j++){
					node1 = (NodeInstance)node.elementAt(j);
					if(node1.Node_ID == nodeid){
						flag = 1;
					}
				}
				if(flag == 0)
				break;
			}
			node0 = new NodeInstance(HASH_MAX, nodeid);
			flag = 0;
			for(j=0; j < i; j++){
				node1 = (NodeInstance)node.elementAt(j);
				if(node1.Node_ID > node0.Node_ID){
					node.insertElementAt(node0, j);
					System.out.println("Node_ID ="+node0.Node_ID+" j = "+j);
					//     node0.refresh(node);
					//     node0.showFinger();
					flag = 1;
					break;
				}
			}
			if(flag == 0){
				node.addElement(node0);
				System.out.println("Node_ID2 ="+node0.Node_ID+" j = "+j);
				//    node0.refresh(node);
				//    node0.showFinger();
			}
		}
		System.out.println("--------nodelist-----------");
		for(i=0; i<node.size(); i++){ // refresh node info.
			node1 = (NodeInstance)node.elementAt(i);
			System.out.println("node"+i+" th is " + node1.Node_ID);
		}
		System.out.println("--------shokika-----------");
		for(i=0; i<INITIALNODE; i++){ // refresh node info.
			node1 = (NodeInstance)node.elementAt(i);
			node1.refresh(node);
			////   node1.showFinger();
		}
		
		//Zipf function  initial
		/////////////////////////////////////
		zipfnode();
		////////////////////////////////////////

		//for comparison --- every target key is cached !!!
		
		for(i=0;i<INITIALNODE;i++){
			node1 = (NodeInstance)node.elementAt(i);
		
			for(j=1; j<=100; j++){
				int target = 6721;

				for(int k=0;k<j;k++){
					target = ((target * 971+791)%(HASH_MAX));
				}
				
				if (node1.query(target)){
					System.out.println("initial cache "+target+" in "+node1.Node_ID+" is cached at "+node1.Former_Node.Node_ID);
					Cache_total++;
					Cache c = new Cache();
					NodeInstance node2 = node1.Former_Node;
					
					c.cache_id =target;
					c.time_limit = END_TIME;
					node2.add_cache(c);
				}
			}
		}
		//-----------for comaprison ------------------------------
	
		
		//Initial query events are scheduled
		for(i=0; i<node.size(); i++){

			event1 = new Event();
			node1 = (NodeInstance)node.elementAt(i);
			event1.node_id = node1.Node_ID;
			event1.event_type = 5; // Query request
			event1.seen = 0;
			event1.hop = 0;
			event1.query_id = zipfgetid();
			event1.query_node_id = event1.node_id; // query node is me.
			event1.sender = event1.node_id;
	//		System.out.println("Query initiation node ="+node1.Node_ID+" query node = "+event1.query_id);
					
					
					//   schedule(event1, query_int[(int)node1.Node_ID]);
			schedule(event1, QUERY_INTERVAL);
		}
		// schedule refresh
		for(i=0; i<node.size(); i++){
			event1 = new Event();
			node1 = (NodeInstance)node.elementAt(i);
			event1.node_id = node1.Node_ID;
			event1.event_type = 7; // refresh
			schedule(event1,REFRESH_INTERVAL);
		}
		// main loop
		while(total_query < MAXQUERY){
			current = (Event)sched.remove(0);
					
			l_time = time;
			time = current.etime;
			itime = (int) time;
			if(itime % 5000 == 0){
				System.out.println("Simulation time(" + time + ")is complete !! ");
			}
					
			switch (current.event_type){
			case 1:
				e_joinnode(current);
				break;
			case 2:
				e_delnode(current);
				break;

			case 5:
				e_queryreq(current);
				break;
			case 6:
				e_replyreq(current);
				break;
			case 7:
				e_refresh(current);
				break;
			case 8:
				e_cache(current);
				break;
			default:
				/* error */
				break;
			}
		}


		System.out.println("Simulation is complete !! ");
		System.out.println("total_query   = "+ total_query);
		System.out.println("total_success = "+ total_success);
	//	System.out.println("total_fail    = "+ total_fail);
	//	System.out.println("total_fail1   = "+ total_fail1);
	//	System.out.println("total_fail2   = "+ total_fail2);
	//	System.out.println("total_fail_timeover = "+ total_fail_timeover);
	//	System.out.println("total_join    = "+ total_join);
	//	System.out.println("total_delete  = "+ total_delete);
		System.out.println("total_hop     = "+ total_hop);
		System.out.println("Cache_total   = "+ Cache_total);
		System.out.println("total_re      = "+ total_re);
		System.out.println("total_hit     = "+ total_hit);
		System.out.println("average cache = "+ (int)(Cache_total*CACHELIMIT/time));
	//	System.out.println("total_refresh = "+ total_refresh);
		total_event = (long)total_success + (long)total_fail;
		error_rate = (double)total_fail/total_event*100;
	//	System.out.println("join interval = "+ JOIN_DEL_INTERVAL+" refresh_interval = "+REFRESH_INTERVAL);
	//	System.out.println("error rate = "+ error_rate+"%");
	}
}

