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
     *  PS：会抛出 NumberFormatException 数字格式异常
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
     * PS：会抛出 NumberFormatException 数字格式异常
     * @Date：16:10 2017/11/30
     */
    public static int parseInt(String s) throws NumberFormatException{
        return parseInt(s, 10);
    }

    /**
     * @Author：zhuangfei
     * @Description：将字符串参数解析为第二个参数指定的基数的无符号整数。无符号整数将通常与负数关联的值映射
     *                  到大于 @code 最大值的正数数字
     * s ：字符串参数
     * radix ：指定的基数
     * PS：会抛出 NumberFormatException 数字格式异常
     * @Date：13:52 2017/12/7
     */
    public static int parseUnsignedInt(String s, int radix) throws NumberFormatException {
        if(s == null)
            throw new NumberFormatException("null"); // 抛出，输入数据为空异常

        int len = s.length();
        if(len > 0) {
            char firstChar = s.charAt(0);
            if(firstChar == '-') {
                throw new NumberFormatException(String.format("Illegal leading minus sign"
                        + "on unsigned string %s.", s)); // 抛出，在无符号的字符串上非法引导'-'异常
            } else {
                if(len <= 5 || // integer的最大值在支持的最大进制下占6位字符
                        (radix == 10 && len <= 9))  // integer的最大值在十进制下占10位字符
                {
                   return parseInt(s, radix);
                } else {
                    long ell = Long.parseLong(s, radix);
                    if((ell & 0xffff_ffff_0000_0000L) == 0) {
                        return (int) ell;
                    } else {
                        throw new NumberFormatException(String.format("String value %s exceeds range of unsigned int.", s));
                        // 抛出，字符串的值超出整数范围异常
                    }
                }
            }

        } else {
            throw NumberFormatException.forInputString(s);
        }
    }

    /**
     * @Author：zhuangfei
     * @Description：将字符串参数解析为无符号的十进制整数。在字符串中的字符
     *              必须都是十进制数字，除了第一个字符可以为ASCII加好。
     *  s ：需要转换的字符串
     *  PS：会抛出 NumberFormatException 数字格式异常
     * @Date：14:14 2017/12/7
     */
    public static int parseUnsignedInt(String s) throws NumberFormatException {
        return parseInt(s, 10);
    }
    
    /**
     * @Author：zhuangfei
     * @Description：将指定字符串内容按照指定进制转换为Integer格式输出
     * s ：需要转换的字符串
     * PS：会抛出 NumberFormatException 数字格式异常
     * @Date：14:17 2017/12/7
     */
    public static Integer valueOf(String s, int radix) throws NumberFormatException {
        return Integer.valueOf(s, radix);
    }

    /**
     * @Author：zhuangfei
     * @Description：将指定的字符串按照10进制的格式转换为Integer输出
     * s ：需要转换的字符串
     * PS：会抛出 NumberFormatException 数字格式异常
     * @Date：14:21 2017/12/7
     */
    public static Integer valueOf(String s) throws NumberFormatException {
        return Integer.valueOf(s, 10);
    }

    /**
     * @Author：zhuangfei
     * @Description：缓存以支持自动装箱的对象标识语义
     * @Date：14:32 2017/12/7
     */
    public static class IntegerCache {
        static final int low = -128;
        static final int high;
        static final Integer cache[];

        static {
            // 高值可由属性配置
            int h = 127;
            String integerCacheHighPropValue = sun.misc.VM.getSavedProperty("java.lang.Integer.IntegerCache.high");
            if(integerCacheHighPropValue != null) {
                try {
                    int i = parseInt(integerCacheHighPropValue);
                    i = Math.min(i, 127);
                    h = Math.min(i, Integer.MAX_VALUE - (low) -1);
                } catch(NumberFormatException nfe) {

                }
            }
            high = h;

            cache = new Integer[(high - low) + 1];
            int j = low;
            for(int k = 0; k < cache.length; k++)
                cache[k] = new Integer(j++);

            assert IntegerCache.high >= 127;
        }
        private IntegerCache() {}
    }

    /**
     * @Author：zhuangfei
     * @Description：返回一个表示指定integer值的实例，如果不需要一个新的integer对象，那么这个方法
     *              通常被使用到构造方法中，因为这个方法很可能通过缓存经常请求的值来获得更大的空间和时间性能
     * i ：指定值
     * @Date：14:38 2017/12/7
     */
    public static Integer valueOf(int i) {
        if(i >= IntegerCache.low && i <= IntegerCache.high)
            return IntegerCache.cache[i + (-IntegerCache.low)];
        return new Integer(i);
    }

    private final int value;

    /**
     * @Author：zhuangfei
     * @Description：初始化Integer
     * value ：初始值
     * @Date：14:39 2017/12/7
     */
    public Integer (int value) {
        this.value = value;
    }

    /**
     * @Author：zhuangfei
     * @Description：将字符串值初始化为十进制的Integer类型
     * s ：初始值
     * PS ：会抛出 NumberFormatException 数字格式异常
     * @Date：14:50 2017/12/7
     */
    public Integer(String s) throws NumberFormatException {
        this.value = parseInt(s, 10);
    }

    /**
     * @Author：zhuangfei
     * @Description：将Integer的值作为byte值返回
     * @Date：14:52 2017/12/7
     */
    public byte byteValue() {
        return (byte) value;
    }
    
    /**
     * @Author：zhuangfei
     * @Description：将Integer的值作为short值返回
     * @Date：14:52 2017/12/7
     */
    public short shortValue() {
        return (short) value;
    }
    
    /**
     * @Author：zhuangfei
     * @Description：将Integer的值作为int值返回
     * @Date：14:53 2017/12/7
     */
    public int intValue() {
        return value;
    }
    
    /**
     * @Author：zhuangfei
     * @Description：将Integer的值作为long值返回
     * @Date：14:53 2017/12/7
     */
    public long longValue() {
        return (long) value;
    }

    /**
     * @Author：zhuangfei
     * @Description：将Integer的值作为float值返回
     * @Date：14:54 2017/12/7
     */
    public float floatValue() {
        
        return (float) value;
    }

    /**
     * @Author：zhuangfei
     * @Description：将Integer的值作为double值返回
     * @Date：14:54 2017/12/7
     */
    public double doubleValue() {
        return (double) value;
    }

    /**
     * @Author：zhuangfei
     * @Description：将使用此方法的对象转换为String格式返回
     * @Date：14:57 2017/12/7
     */
    public String toString() {
        return toString(value);
    }

    /**
     * @Author：zhuangfei
     * @Description：将调用此方法对象的哈希码返回
     * @Date：14:58 2017/12/7
     */
    public int hashCode() {
        return Integer.hashCode(value);
    }

    /**
     * @Author：zhuangfei
     * @Description：返回指定对象的哈希码
     * value ： 指定对象
     * @Date：15:00 2017/12/7
     */
    public static int hashCode(int value) {
        return value;
    }

    /**
     * @Author：zhuangfei
     * @Description：返回对象与指定对象比较的结果，相同：true，不同：false
     * @Date：15:02 2017/12/7
     */
    public boolean eqyals(Object obj) {
        if(obj instanceof Integer)
            return value == ((Integer)obj).intValue();
        return false;
    }

    /**
     * @Author：zhuangfei
     * @Description：确定带有指定名称的系统属性的整数值。第一个参数为系统属性的名称，系统属性通过
     *              System.getProperty()方法获得。然后将该属性的字符串值解析为一个使用deCode支持的语法和表示
     *              该值的Integer对象的整数值
     * nm ：系统属性
     * @Date：15:05 2017/12/7
     */
    public static Integer getInteger(String nm) {
        return getInteger(nm, null);
    }
    
    /**
     * @Author：zhuangfei
     * @Description：确定带有指定名称的系统属性的整数值。第一个参数被视为系统属性的名称。系统属性用过
     *              System.getProperty()方法获得。然后将该字符串值解析为一个使用deCode支持语法表示该值的整数值。
     *              第二个参数是默认值，如果没有指定名称的属性，则返回第二个参数值的对象，如果该属性没有正确的数字
     *              格式，或者指定的名称为空
     * nm ：系统属性
     * val ：默认值
     * @Date：15:07 2017/12/7
     */
    public static Integer getInteger(String nm, int val) {
        Integer result = getInteger(nm, null);
        return (result == null) ? Integer.valueOf(val) : result;
    }
    
    /**
     * @Author：zhuangfei
     * @Description：以指定的名称返回系统属性的整数值。第一个参数为系统属性的名称。系统属性通过
     *                 System.getProperty()方法获得，然后将该属性的字符串值解析为一个整数值。如果属性值以
     *                 两个ASCII字符 ‘0x’‘#’开头，而不是后面加‘-’，那么剩下的内容就会被解析为十六进制整数
     * nm ：指定获取的系统属性
     * val ：默认值
     * @Date：15:10 2017/12/7
     */
    public static Integer getInteger(String nm, Integer val) {
        String v = null;
        try {
            v = System.getProperty(nm);
        } catch (IllegalArgumentException | NullPointerException e) {
            
        }
        if(v != null) {
            try {
                return Integer.decode(v);
            } catch (NullPointerException e) {
                
            }
        }
        return val;
    }

    /**
     * @Author：zhuangfei
     * @Description：将指定的字符串解码为Integer整数，按照十进制，十六进制和八进制。
     *              在一个可选符号或说明符后面的字符会被解析为Integer格式，parseInt方法与指定
     *              的基数(10进制，16进制或8进制)，这个字符序列必须表示一个正值，否则会抛出
     *              NumberFormatException(数字格式异常)，如果指定的字符串的第一个字符是‘-’，
     *              结果将被否定。在字符串中不允许使用空格字符
     * PS：会抛出 NumberFormatException 数字格式异常
     * @Date：15:31 2017/12/7
     */
    public static Integer decode (String nm) throws NumberFormatException {
        int radix = 10;
        int index = 0;
        boolean negative = false;
        Integer result;

        if(nm.length() == 0)
            throw new NumberFormatException("Zero length string"); // 抛出 字符串长度为0异常
        char firstChar = nm.charAt(0);
        // 如果标志存在，处理标志
        if(firstChar == '-') {
            negative = true;
            index++;
        } else if(firstChar == '+')
            index++;
        // 如果存在基数和说明符，处理它们
        if(nm.startsWith("0x", index) || nm.startsWith("0X", index))  {
            index += 2;
            radix = 16;
        } else if (nm.startsWith("#", index)) {
            index++;
            radix = 16;
        } else if (nm.startsWith("0", index) && nm.length() > 1 + index) {
            index++;
            radix = 8;
        }

        if(nm.startsWith("-", index) || nm.startsWith("+", index)) {
            throw new NumberFormatException("Sign character in wrong position");
        }

        try {
            result = Integer.valueOf(nm.substring(index), radix);
            result = negative ? Integer.valueOf(-result.intValue()) : result;
        } catch (NumberFormatException e) {

            String constant = negative ? ("-" + nm.substring(index)) : nm.substring(index);
            result = Integer.valueOf(constant, radix);
        }
        return result;
    }

    // 1215

    @Override
    public int compareTo(Integer o) {
        return 0;
    }

}
