package java.lang;

import java.lang.annotation.Native;

import static java.lang.Integer.formatUnsignedInt;
import static java.lang.Integer.toUnsignedLong;

/**
 * @Author：zhuangfei
 * @Description：Integer 源码重写
 * @Date：11:03 2017/11/29
 */
public class Integer extends Number implements Comparable<Integer> {

    /** 保持int常量的最小值 **/
    @Native public static final int MIN_VALUE = 0x80000000;

    /** 保持int常量的最大值 **/
    @Native public static final int MAX_VALUE = 0x7fffffff;

    /** 保持原始类型的int接口 **/
    public static final Class<Integer> TYPE = (Class<Integer>) Class.getPrimitiveClass("int");

    /**
     * @Author：zhuangfei
     * @Description：所有可能的字符作为字符串表示为数字
     * @Date：11:09 2017/11/29
     */
    final static char[] digits = {
        '0' , '1' , '2' , '3' , '4' , '5' ,
        '6' , '7' , '8' , '9' , 'a' , 'b' ,
        'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
        'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
        'o' , 'p' , 'q' , 'r' , 's' , 't' ,
        'u' , 'v' , 'w' , 'x' , 'y' , 'z'    
    };

    /**
     * @Author：zhuangfei
     * @Description：返回由第二个参数指定基数转换为字符串格式的第一个参数
     *              如果基数超过了Character的最小(-2)或最大(36)区间，会指定为10
     *              如第一个参数为负，则会把它相应转换后的ASCII参数前加上 ‘-’
     * i ：需要转换的参数
     * radix ：指定的基数
     * @Date：11:13 2017/11/29
     */
    public static String toString(int i, int radix) {
        if(radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            radix = 10;
        }

        if(radix == 10)
            return toString(i);

        char buf[] = new char[33];
        boolean negative = (i < 0);
        int charPos = 32;

        if(!negative)
            i = -i;

        while (i <= -radix) {
            buf[charPos--] = digits[-(i % radix)];
            i = i / radix;
        }
        buf[charPos] = digits[-i];
        if(negative) {
            buf[--charPos] = '-';
        }

        return new String(buf, charPos, (33 - charPos));
    }

    /**
     * @Author：zhuangfei
     * @Description：返回指定参数的String格式，指定的整数参数转换为有符号的小数返回
     * i ：指定参数
     * @Date：11:28 2017/11/29
     */
    public static String toString(int i) {
        if(i == Integer.MIN_VALUE) {
            return "-2147483648";
        }
        int size = (i < 0)? stringSize(-i) + 1: stringSize(i);
        char[] buf = new char[size];
        getChars(i, size, buf);
        return new String(buf, true);
    }

    /**
     * @Author：zhuangfei
     * @Description：将整数放入数组中，字符被放置到缓冲区里，然后从指定索引处最不重要
     *                  的数开始向后遍历
     * i ：整数
     * index ：指定的索引
     * buf ：字符数组
     * @Date：11:33 2017/11/29
     */
    static void getChars(int i, int index, char[] buf) {
        int q, r;
        int charPos = index;
        char sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        while (i >= 65536) {
            q = i / 100;
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buf[--charPos] = DigitOnes[r];
            buf[--charPos] = DigitTens[r];
        }

        for(;;) {
            q = (i * 52429) >>> (16 + 3);
            r = i - ((q << 3) + (q << 1));
            buf[--charPos] = digits[r];
            i = q;
            if (i == 0) break;
        }
        if(sign != 0) {
            buf[--charPos] = sign;
        }
    }
    /**
     * @Author：zhuangfei
     * @Description：需要正数的参数
     * x ：正参
     * @Date：11:42 2017/11/29
     */
    static int stringSize(int x) {
        for(int i = 0; ; i++) {
            if(x <= sizeTable[i]) {
                return i + 1;
            }
        }
    }

    static final int[] sizeTable = {9, 99, 999, 9999, 99999, 999999, 9999999,
                                    99999999, 999999999, Integer.MAX_VALUE};

    final static char [] DigitTens = {
            '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
            '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
            '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
            '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
            '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
            '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
            '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
            '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
            '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
            '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
    } ;

    final static char [] DigitOnes = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    } ;

    /**
     * @Author：zhuangfei
     * @Description：返回第一个参数的字符串表示，作为第二个参数
     *              指定基数的无符号整数值。如果radix的值不在Integer的
     *              最小和最大区域之间，radix的值为10
     *  i ：第一个参数
     *  radix ：第二个参数
     * @Date：14:21 2017/11/30
     */
    public static String toUnsignedString(int i, int radix) {
        return Long.toUnsignedString(toUnsignedLong(i), radix);
    }

    /**
     * @Author：zhuangfei
     * @Description：将整数转换为未签名的数字
     * @Date：14:29 2017/11/30
     */
    public static String toUnsignedString0(int val, int shift) {
        int mag = Integer.SIZE - Integer.numberOfLeadingZeros(val);
        int chars = Math.max((mag + (shift - 1) / shift), 1);
        char[] buf = new char[chars];

        formatUnsignedInt(val, shift, buf, 0, chars);

        return new String(buf, true);
    }

    @Native public static final int SIZE = 32;

    /**
     * @Author：zhuangfei
     * @Description：返回指定的输入值的补充二进制中的最高阶(最左边)的0。如果指定的
     *                 值为0，返回32
     * i ：需要补位的输入值
     * @Date：15:18 2017/11/30
     */
    public static int numberOfLeadingZeros(int i) {
        if(i == 0) {
            return 32;
        }
        int n = 1;
        if(i >>> 16 == 0) {n += 16; i <<= 16;}
        if(i >>> 24 == 0) {n += 8; i <<= 8;}
        if(i >>> 28 == 0) {n += 4; i <<= 4;}
        if(i >>> 30 == 0) {n += 2; i <<= 2;}
        n -= i >>> 31;
        return n;
    }
    
    /**
     * @Author：zhuangfei
     * @Description：返回整数参数的字符串表现形式 16进制
     * i ：整数
     * @Date：14:34 2017/11/30
     */
    public static String toHexString(int i) {
        return toUnsignedString0(i, 4);
    }
    
    /**
     * @Author：zhuangfei
     * @Description：返回整数参数的字符串表现形式 8进制
     * i ：整数
     * @Date：14:54 2017/11/30
     */
    public static String toOctalString(int i) {
        return toUnsignedString0(i, 3);
    }

    /**
     * @Author：zhuangfei
     * @Description：返回整数参数的字符串表现形式 2进制
     * i ：整数
     * @Date：14:56 2017/11/30
     */
    public static String toBinaryString(int i) {
        return toUnsignedString0(i , 1);
    }

    /**
     * @Author：zhuangfei
     * @Description：将未签名的格式转换为字符串缓冲区
     * val ：未签名的int格式
     * shift ：将基础的log2转换为指定格式(4:16进制，3:8进制， 1：二进制)
     * buf ：字符缓冲区写入
     * offset ：抵消目标缓冲区中的偏移量
     * len ：需要写入的字符数量
     * @Date：15:00 2017/11/30
     */
    static int formatUnsignedInt(int val, int shift, char[] buf, int offset, int len) {
        int charPos = len;
        int radix = 1 << shift;
        int mask = radix - 1;
        do {
            buf[offset + --charPos] = Integer.digits[val & mask];
            val >>>= shift;
        } while(val != 0 && charPos > 0);

        return charPos;
    }

    /**
     * @Author：zhuangfei
     * @Description：返回参数的字符串表现形式，作为无符号的十进制值。参数转换为无符号
     *              的十进制表示，并作为字符串返回。好比上面方法toUnsignedString(int,int)中第二
     *              个参数的值超过区间后为10一样
     *  i ：参数
     * @Date：15:06 2017/11/30
     */
    public static String toUnsignedString(int i) {
        return Long.toString(toUnsignedLong(i));
    }
    
    /**
     * @Author：zhuangfei
     * @Description：该方法返回输入指定字符串的指定进制的返回，
     *              例：paseInt("110",2) 即为输出2进制下110的结果
     *              平时使用的paseInt("110")默认返回110在十进制下的结果
     *              PS：s 的长度不能超过7位，否则也会抛出异常
     *  s ：指定转化的参数
     *  radix ：转换为多少进制
     * @Date：15:25 2017/11/30
     */
    public static int parseInt(String s, int radix) throws NumberFormatException {
        // 警告：此方法可能在初始化时再VM初始化时调用，必须注意不要使用价值的方法
        if(s == null) {
            throw new NumberFormatException("null"); // 输入值为空会抛出 数据转换指定值不能为空的异常
        }
        if(radix < Character.MIN_RADIX) {
            // 如果解析的基数小于指定的最小基数(-2)，会抛出 基数小于指定基数的异常
            throw new NumberFormatException("radix " + radix + "less than Character.MIN_RADIX");
        }
        if(radix > Character.MAX_RADIX) {
            // 如果解析的基数大于指定的最大基数(36)，会抛出 基数大于指定基数的异常
            throw new NumberFormatException("radix " + radix + "greater than Character.MAX_RADIX");
        }

        int result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;

        if( len > 0) {
            char firstChar = s.charAt(0);
            if(firstChar < '0') {
                if(firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if(firstChar != '+') {
                    // 抛出，输入的数组有误异常
                    throw NumberFormatException.forInputString(s);
                }
                if(len == 1) {
                    // 抛出，输入的数组有误异常
                    throw NumberFormatException.forInputString(s);
                }
                i++;
            }
            multmin = limit / radix;
            while(i < len) {
                digit = Character.digit(s.charAt(i++), radix);
                if(digit < 0) {
                    // 抛出，输入的数组有误异常
                    throw NumberFormatException.forInputString(s);
                }
                if(result < multmin) {
                    // 抛出，输入的数组有误异常
                    throw NumberFormatException.forInputString(s);
                }
                result *= radix;
                if(result < limit + digit) {
                    // 抛出，输入的数组有误异常
                    throw NumberFormatException.forInputString(s);
                }
                result -= digit;
            }
        } else {
            // 抛出，输入的数组有误异常
            throw NumberFormatException.forInputString(s);
        }
        return negative ? result : -result;
    }

    /**
     * @Author：zhuangfei
     * @Description：默认返回指定对象的十进制结果
     * s ：指定转换的参数
     * @Date：16:10 2017/11/30
     */
    public static int parseInt(String s) throws NumberFormatException{
        return parseInt(s, 10);
    }

    // 661

    @Override
    public int compareTo(Integer o) {
        return 0;
    }

    @Override
    public int intValue() {
        return 0;
    }

    @Override
    public long longValue() {
        return 0;
    }

    @Override
    public float floatValue() {
        return 0;
    }

    @Override
    public double doubleValue() {
        return 0;
    }
}
