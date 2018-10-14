import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Test3 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        int a[] = new int[1000000000];
        List<Integer> list = new ArrayList<>();
        //TODO:正常的TODO注释会被idea自动识别并显示
        //solution:自己设定的TODO
        int n = input.nextInt();
        int x = 0;
        for (int i = 0; i < n; i++) {
            x = input.nextInt();
            a[x]++;
            if (a[x] == 1)
                list.add(x);
        }
        Collections.reverse(list);
        int sum = 0;
        for (int i = 0; i < list.size(); i++) {
            int num = list.get(i);
            sum += a[num];
            for (int j = i + 1; j < list.size(); j++) {
                if (a[num] - a[list.get(j)] >= 0) {
                    a[list.get(j)] = a[num] - a[list.get(j)];
                } else {
                    a[list.get(j)] = 0;
                }
            }

            a[num] = 0;
        }
        System.out.println(sum);
    }
}