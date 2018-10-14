
import java.math.*;
public class Solution {



	public static int solution(int K,int L , int M, int N,int P,int Q, int R,int S){
		int count = 0;
		for (int i = 0; i<=10;i++){
			//number count
			
			//Area reduction Function
			K = K - i;
			N = N + i;
			L = L + i;
			M = M - i;
	
			P = P + i;
			S = S - i;
			R = R + i;
			Q = Q - i;

			//sum of Area
			int Area1 = (M-K)*(N-L);
			System.out.println("Sum of Area1 is "+ Area1);
			int Area2 = (R-P)*(Q-S);
			System.out.println("Sum of Area2 is "+ Area2);
			//integrate Area
			int Area3 = (M-P)*(S-L);
			System.out.println("Sum of Area3 is "+ Area3);
			int Area =Math.abs((M-K)*(N-L))+Math.abs(((R-P)*(Q-S)))-Math.abs((M-P)*(S-L));
			System.out.println("Sum of Area is "+ Area);
			System.out.println("count of number is " + count);
			
			count ++;
			if(Math.abs(Area2)==Math.abs(Area3))
			break;
			
		}
		
		return count;
	}
	public static void main(String proD[ ]){
		int x = -4;
		int y = 1;
		int z = 2;
		int g = 6;
		int j = 0;
		int h = -1;
		int t = 4;
		int u = 3;
		solution(x,y,z,g,j,h,t,u);
	}
	
}
