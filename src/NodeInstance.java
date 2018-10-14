import java.util.*;
//import java.net.*;
/*
* Node実体クラス
*/
public class NodeInstance {
	// フィールド
	public int HASH_MAX_VALUE; // ハッシュ空間の最大値(2のべき乗)
	public long Node_ID; // NodeのノードID
	public long Original_Node_ID; // 上位リングに入る前の元のID
	public NodeInstance Former_Node; // 前任ノードを指すポインタ
	public NodeInstance Successor_Node; // 後任ノードを指すポインタ
	private Vector<Long> fingerTable; // フィンガーテーブル
	private Vector manageNodes; // 管理する領域のノードを記憶する配列
	private Vector<Cache> Cache;
	static final float TIMEEXTE = 10000;
	int total_re;
	
	/////ГГГГГГ コンストラクタ ГГГГГГГ/////
	// 引数はハッシュ空間のサイズ
	public NodeInstance(int HASH_MAX, long nodeid){
		this.HASH_MAX_VALUE = HASH_MAX;
		fingerTable = new Vector<Long>();
		Node_ID = nodeid;
		Cache = new Vector<Cache>();
	}
	
	
	/////ГГГГГГ メソッド ГГГГГГГ/////
	////////
	
	// Cacheの追加、検索、削除メソッド
	public void add_cache(Cache c){
		
		this.Cache.addElement(c);
		
	}
	
	public boolean search_cache(long target){
		Cache c,c1;
		int j;
		long nodeid;
	//	System.out.println("search cache : target "+target+" in "+this.Node_ID);
		// cache_nodeがあれば、true
		for(int i=0;i<this.Cache.size();i++){
			c = this.Cache.elementAt(i);
			nodeid = c.cache_id;
			if(nodeid == target){
					// c.time_limit =  TIMEEXTE + c.time_limit;
					return true;
			}
		}
		return false;
	}
	
	public boolean del_cache(double time){
		Cache c1;
		for(int i=0;i<this.Cache.size();i++){
			c1= this.Cache.elementAt(i);
			if(c1.time_limit<=time)
			{
				this.Cache.remove(i);
				return true;
				
			}
			
		}
		return false;
	}
	
	// フィンガーテーブルの更新// ID:IPリストからフィンガーテーブルを生成
	public void refresh(Vector node_list) {
		this.fingerTable = (Vector<Long>)createFtable(node_list, Node_ID);
		Former_Node = getFormerNode(node_list, Node_ID);
		Successor_Node = getSuccessorNode(node_list, Node_ID);
		nodeJoin();
	}
	////////+++++ Proxy新規参加処理(前後のノードに参加を告知) +++++////////
	public void nodeJoin() {
		//        System.out.println("nodeRefresh");
		if(Successor_Node.Node_ID != Node_ID)
		Successor_Node.Former_Node = this;
		if(Former_Node.Node_ID != Node_ID)
		Former_Node.Successor_Node = this;
	}
	////////+++++ Proxy脱退処理(前任・後任ノードに脱退の旨を告知する) +++++////////
	public void nodeLeave() {
		System.out.println("ネットワークから脱退します");
		// ID:IPリストに脱退することを告知してリストを更新する
		// 後任ノードには、自分のIDと前任ノードのIDとIPを添える
		Successor_Node.Former_Node = this.Former_Node;
		// 前任ノードには、自分のIDと後任ノードのIDとIPを添える
		Former_Node.Successor_Node = this.Successor_Node;
	}
	
	////////+++++ node探索処理 +++++////////
	// フィンガーテーブルを利用して次にアクセスすべきProxyのIPを求める
	// 引数は探すノードのID, 返り値は次にステータスコードと、アクセスすべきノードのIP
	public boolean query(long target) {
		// 自分が管理する領域内にあれば、true
		if(this.Former_Node.Node_ID < this.Node_ID){
			if ((target > this.Former_Node.Node_ID)
			&& (target <= this.Node_ID)){
				////                System.out.println("func query: true: nodeid = "+this.Node_ID+" target = "+target+" former = "+this.Former_Node.Node_ID);
				return true;
			}else{
				//                System.out.println("func query: false: nodeid = "+this.Node_ID+" target = "+target+" former = "+this.Former_Node.Node_ID);
				return false;
			}
		}else {
			if ((target > this.Former_Node.Node_ID)
			|| (target <= this.Node_ID)){
				////                System.out.println("func query: true: nodeid = "+this.Node_ID+" target = "+target+" former = "+this.Former_Node.Node_ID);
				return true;
			}else{
				//                System.out.println("func query: false: nodeid = "+this.Node_ID+" target = "+target+" former = "+this.Former_Node.Node_ID);
				return false;
			}
		}
	}
	
	////////+++++ Proxy探索処理 +++++////////
	// フィンガーテーブルを利用して次にアクセスすべきProxyのIPを求める
	// 引数は探すノードのID, 返り値は次にステータスコードと、アクセスすべきノードのIP
	public long nexthop(long target) {
		Long nodeid1, nodeid2;
		
		// 探すノードが自分と後任ノードの間にあれば、後任ノード
		/*        if(this.Successor_Node.Node_ID > this.Node_ID){
			if ((target <= this.Successor_Node.Node_ID)
			&& (target > this.Node_ID)){
				System.out.println("X:ノード"+target+"の探索をノード"+ this.Successor_Node.Node_ID+"に委託します");
				return this.Successor_Node.Node_ID;
			}
		}else if(this.Successor_Node.Node_ID < this.Node_ID){
			if ((target <= this.Former_Node.Node_ID)
			|| (target > this.Node_ID)){
				System.out.println("Y:ノード"+target+"の探索をノード"+ this.Successor_Node.Node_ID+"に委託します");
				return this.Successor_Node.Node_ID;
			}
		}
		*/
		nodeid1 = fingerTable.get(0);
		if(nodeid1.longValue() > this.Node_ID){
			if ((target <= nodeid1.longValue())
			&& (target > this.Node_ID)){
				//                System.out.println("X:ノード"+target+"の探索をノード"+ nodeid1.longValue()+"に委託します");
				return nodeid1.longValue();
			}
		}else if(nodeid1.longValue() < this.Node_ID){
			if ((target <= nodeid1.longValue())
			|| (target > this.Node_ID)){
				//                System.out.println("Y:ノード"+target+"の探索をノード"+ nodeid1.longValue()+"に委託します");
				return nodeid1.longValue();
			}
		}
		//        else{
			// さもなければ、フィンガー・テーブルの"開始ID"エントリ中から
			// 求めるキーの前任ノードとして最も近いものを探す
			for(int i=1; i<fingerTable.size(); i++){
				nodeid1 = fingerTable.get(i-1);
				nodeid2 = fingerTable.get(i);
				//                System.out.println(" nodeid1 = "+nodeid1.longValue()+" nodeid2 = "+nodeid2.longValue()+" target ="+target);
				if(nodeid1.longValue() <= nodeid2.longValue()){
					if ((target < nodeid2.longValue())
					&& (target >= nodeid1.longValue())){
						//                        System.out.println("A:ノード"+target+"の探索をノード"+nodeid1.longValue()+"に委託します");
						return nodeid1.longValue();
					}
				}else if(nodeid1.longValue() > nodeid2.longValue()){
					if ((target < nodeid2.longValue())
					|| (target >= nodeid1.longValue())){
						//                        System.out.println("B:ノード"+target+"の探索をノード"+nodeid1.longValue()+"に委託します");
						return nodeid1.longValue();
					}
				}
			}
			nodeid2 = fingerTable.get(fingerTable.size()-1);
			//            System.out.println("C:ノード"+target+"の探索をノード"+nodeid2.longValue()+"に委託します");
			return nodeid2.longValue();
		//        }
		//    return 0;
	}
	
	////////+++++ 現在のフィンガーテーブルを一覧表示 +++++////////
	public void showFinger() {
		System.out.println("++[ Finger Table ]++");
		System.out.println("My Node_ID = " + this.Node_ID+" former="+this.Former_Node.Node_ID+" successor="+this.Successor_Node.Node_ID);
		
		for (int i = 0; i < fingerTable.size(); i++) {
			Long nodeid = fingerTable.get(i);
			System.out.println(i+"番 :"+nodeid.longValue());
		}
	}
	
	
	// 現在のローカルID:IPリストから、フィンガーテーブルを自動生成。Vector型に直して返す
	public Vector<Long> createFtable(Vector node_list, long NodeID){
		int tableSize = 16; // テーブルのサイズはハッシュ空間のビットサイズ
		/* 底が2の対数計算はJ2SE1.5でないと使えないのでここでは指定する */
		NodeInstance succ;
		long nodeid;
		Vector<Long> data = new Vector<Long>();
		
		//        System.out.println("createFtable");
		
		for(int i = 0; i<node_list.size();i++){
			NodeInstance nd = (NodeInstance)node_list.elementAt(i);
			///            System.out.print(" "+ nd.Node_ID);
		}
		///        System.out.println();
		
		// Vectorの各要素を1つずつ決定し、格納する
		for(int i = 0; i < tableSize; i++){
			// start値はそれぞれ自分のIDから2のべき乗値を加えたもの
			long start = (long)(NodeID + Math.pow(2,i));
			int flag = 0;
			for(int j = 0; j < node_list.size(); j++){
				NodeInstance nd = (NodeInstance)node_list.elementAt(j);
				if(start == nd.Node_ID)
				flag = 1;
			}
			if(flag == 0){
				succ = getSuccessorNode(node_list, start);
				Long sucNodeID = new Long(succ.Node_ID);
				data.addElement(sucNodeID);
			}else{
				Long sucNodeID = new Long(start);
				data.addElement(sucNodeID);
			}
		}
		
		for(int i = 0; i<node_list.size();i++){
			NodeInstance nd = (NodeInstance)node_list.elementAt(i);
			///            System.out.print(" "+ nd.Node_ID);
		}
		///        System.out.println();
		
		return data;
	}
	
	// ID:IPリストから指定ハッシュIDに対する前任ノードを取り出す。戻り値はNode型で
	public NodeInstance getFormerNode(Vector node_list, long NodeID){
		// NodeIDがハッシュ空間の最大値を越えたら、剰余をとる
		//        System.out.println("getFormerNode");
		if(NodeID > HASH_MAX_VALUE)
		NodeID = NodeID % HASH_MAX_VALUE;
		
		NodeInstance nd = null;
		for(int i = node_list.size()-1;i>=0;i--){
			nd = (NodeInstance)node_list.elementAt(i);
			if(NodeID > nd.Node_ID){
				return nd;
			}
		}
		nd = (NodeInstance)node_list.lastElement();
		return nd;
	}
	// ID:IPリストから指定ハッシュIDを管理するノード(後任ノード)を取り出す。戻り値はNode型で
	public NodeInstance getSuccessorNode(Vector node_list, long NodeID){
		// NodeIDがハッシュ空間の最大値を越えたら、剰余をとる
		int i;
		
		//        System.out.println("getSuccessorNode");
		if(NodeID > HASH_MAX_VALUE)
		NodeID = NodeID % HASH_MAX_VALUE;
		
		NodeInstance referNode= (NodeInstance)node_list.elementAt(0);
		for(i=0; i<node_list.size(); i++){
			referNode = (NodeInstance)node_list.elementAt(i);
			// 指定IDが参照中のノードIDより大きく
			if(NodeID >= referNode.Node_ID)
			continue;
			else
			break;
		}
		if(i == node_list.size())
		referNode = (NodeInstance)node_list.elementAt(0);
		return referNode;
	}
}
