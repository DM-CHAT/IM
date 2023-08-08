package com.mhhy.util;

import java.util.List;
import java.util.Random;

public class RandomUtil {

    //传入list数组后随机排列
    public static int[] getSequence(List<Integer> list) {
        int[] sequence = new int[list.size()];
        for(int i = 0; i < list.size(); i++){
            sequence[i] = list.get(i);
        }
        Random random = new Random();
        for(int i = 0; i < list.size(); i++){
            int p = random.nextInt(list.size());
            int tmp = sequence[i];
            sequence[i] = sequence[p];
            sequence[p] = tmp;
        }
        return sequence;
    }

    //获取某个范围内的随机数
    public static int getRandomArea(int i){
        Random random = new Random();
        return random.nextInt(i);
    }

    /**
     * 获取随机数
     * @param j 随机数的位数
     * @return
     */
    public static String getRandomToNum(int j){
        StringBuffer buffer = new StringBuffer();
        int str[] = {1,2,3,4,5,6,7,8,9,0};
        for (int i = 0; i < j; i ++){
            double num = Math.random();
            int a = (int) (num * 10);
            buffer.append(a);
        }
        return buffer.toString();
    }

    //获取随机数
    public static String getRandom(){
        Random random = new Random();
        int rand = 1000000 + random.nextInt(10000000);
        return "" + rand;
    }

    //获取邀请码 6位数
    public static String getRandomToInviteCode(int number){
        StringBuffer buffer = new StringBuffer();
        String str[] = {"1","2","3","4","5","6","7","8","9","0","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
        for (int i = 0; i < number; i ++){
            double num = Math.random();
            int a = (int) (num * str.length);
            buffer.append(str[a]);
        }
        return buffer.toString();
    }

    /**
     * 获取 邀请码
     * @param j 需要邀请码的位数
     * @return
     */
    public static String getRandomToInviteCodeToNum(int j){
        StringBuffer buffer = new StringBuffer();
        String str[] = {"1","2","3","4","5","6","7","8","9","0","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};
        for (int i = 0; i < j; i ++){
            double num = Math.random();
            int a = (int) (num * str.length);
            buffer.append(str[a]);
        }
        return buffer.toString();
    }

    public static String getRandomHeadUrl(){
        String str[] = {"https://www.cnqhcs.com/java/head/1.png", "https://www.cnqhcs.com/java/head/2.png"};
        double num = Math.random();
        Random random = new Random();
        int i = random.nextInt(str.length);
        return str[i];
    }
}
