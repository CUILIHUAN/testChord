

public class MyTime {
private int second;
private int minute;
private int hour;

public void setSecond(int second){
	this.second=second;
}
public int getSecond(){
	return second;
}
public void setMinute(int minute){
	this.minute=minute;
}
public int getMinute(){
	return minute;
}
public void addMinute(int b){
	minute=minute+b;
}
public void addHour(int c){
	hour=hour+c;
}
public void subSecond(int a){
	second=second-a;
}
public void subMinute(int b){
	minute=minute-b;
}
public void subHour(int c){
	hour=hour-c;
}

public void addtime(){
	if(second>=60){
		minute++;
		second-=60;
	}
	if(minute>=60){
		hour++;
		minute-=60;
	}
	if(hour>=24){
		hour-=24;
	}
	System.out.println("完成的时间是："+hour+"时"+minute+"分"+second+"秒");
}
public void subtime(){
	if(second<0){
		minute--;
		second+=60;
	}
	if(minute<=0){
		hour--;
		minute+=60;
	}
	if(hour<=0){
		hour+=24;
	}
	System.out.println("开始的时间是："+hour+"时"+minute+"分"+second+"秒");
}
}
