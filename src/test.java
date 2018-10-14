import java.util.Scanner;

public class test {
    /**
     *
     * 功能描述: 
     *
     * @param: 
     * @return: 
     * @auther: cui
     * @date: 2018/10/13 18:42
     */
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in

        );
        System.out.println("username");
        String a = scan.next();
        System.out.println("password");
        int b = scan.nextInt();
        int i = 0;

        for (i = 0; i <= 2; i++) {
            if ("admin".equals(a)&&b==123) {
                System.out.println("success！");
            } else {
                System.out.println("fail！");
                System.out.println("username");
                a = scan.next();
                System.out.println("passwrod");
                b = scan.nextInt();
            }
        }
    }
}
