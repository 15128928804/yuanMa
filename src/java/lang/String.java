package java.lang;

import sun.misc.ASCIICaseInsensitiveComparator;

import java.io.ObjectStreamField;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.lang.String.checkBounds;

/**
 * @Author：zhuangfei
 * @Description：String 源码重写
 * @Date：16:39 2017/11/8
 */
public final class String implements java.io.Serializable,Comparable<String>,CharSequence{

    /**
     * @Author：zhuangfei
     * @Description：该值用于字符存储
     * @Date：16:40 2017/11/8
     */
    private final char value[];

    /**
     * @Author：zhuangfei
     * @Description：缓存字符串的哈希代码
     * @Date：16:42 2017/11/8
     */
    private int hash;
    
    /**
     * @Author：zhuangfei
     * @Description：使用jdk1.0.2中的serialVersionUID来实现互操作性
     * @Date：16:43 2017/11/8
     */
    private static final long serialVersionUID = -6849794470754667710L;

    /**
     * @Author：zhuangfei
     * @Description：类字符串在序列化流协议中是特殊的。
     *  将字符串实例写入ObjectOutputStream
     * @Date：16:46 2017/11/8
     */
    private static final ObjectStreamField[] serialPersistenFields = new ObjectStreamField[0];

    /**
     * @Author：zhuangfei
     * @Description：初始化一个新的 String 对象用来表示一个空的字符序列。
     * 注意，这个构造方法是没有使用的必要的，因为字符串是不可变的
     * @Date：16:48 2017/11/8
     */
    public String() {
        this.value = "".value;
    }
    
    /**
     * @Author：zhuangfei
     * @Description：初始化一个新的 String对象用来表示相同的字符序列作为参数，
     *  换句话说，新创建的字符串是参数字符串的副本。除非一个‘需要的’显示拷贝，
     *  否则是不必使用此构造函数的，因为字符串是不可变的
     * @Date：16:53 2017/11/8
     */
    public String(String original) {
        this.value = original.value;
        this.hash = original.hash;
    }
    
    /**
     * @Author：zhuangfei
     * @Description：分配一个新的String用来表示序列字符数组参数中包含的字符。
     * 在该字符数组的内容被复制后的修改中，字符数组不影响新创建的字符串
     * @Date：16:58 2017/11/8
     */
    public String(char value[]) {
        this.value = Arrays.copyOf(value, value.length);
    }
    
    /**
     * @Author：zhuangfei
     * @Description：分配一个新的String对象用来包含来自子数组的字符
     * 字符数组参数。offset参数是子数组的第一个字符和count位置的索引指定的
     * 子数组的长度的内容。
     * 子数组被复制后对子数组进行修改不影响新创建的字符串
     * value：源数据
     * offset：初始偏移
     * count：偏移长度
     * PS：如果offset和count所得到的值不在源数组的下标内，会抛出
     * IndexOutOfBoundsException(数组下标越界) 异常
     * @Date：17:02 2017/11/8
     */
    public String (char value[], int offset, int count) {
        if(offset < 0) { // 没有偏移量
            throw new StringIndexOutOfBoundsException(offset); // 抛出索引越界异常
        }
        if(count <= 0) {
            if(count < 0) {
                throw new StringIndexOutOfBoundsException(count); // 抛出索引越界异常
            }
            if(offset <= value.length) { // 初始偏移量<=源数据长度时直接返回源数据
                this.value = "".value;
                return;
            }
        }
        // 源码解释：偏移量或计数可能在 -1 附近
        if(offset > value.length - count) { // 初始偏移量>源数据-初始位置的数据后的长度
            throw new StringIndexOutOfBoundsException(offset + count); // 抛出索引越界异常
        }
        this.value = Arrays.copyOfRange(value, offset,offset+count); // 执行方法
    }
    
    /**
     * @Author：zhuangfei
     * @Description：分配一个新的String对象，包含来自子数组的字符，
     * offset是第一个代码的索引。
     * 子数组的点和count指定了子数组的长度。
     * 子数组的内容被转换成了char形式，之后对数组的修改不会影响新创建的字符串
     * codePoints：源数据
     * offset：初始偏移
     * count：长度
     *PS：如果在codePoints中有任何无效的字符，会抛出：IllegalArgumentException(不合法的参数异常)
     * 如果offset和count所得到的值不在源数组的下标内，会抛出：IndexOutOfBoundsException(数组下标越界异常)
     *
     * @Date：17:20 2017/11/8
     */
    public String(int[] codePoints, int offset, int count) {
        if(offset < 0) {
            throw new StringIndexOutOfBoundsException(offset); // 抛出索引越界异常
        }
        if(count <= 0) {
            if(count < 0) {
                throw new StringIndexOutOfBoundsException(offset); // 抛出索引越界异常
            }
            if(offset <= codePoints.length) { // 初始偏移量<=源数据长度时直接返回源数据
                this.value = "".value;
                return;
            }
        }
        // 源码解释：偏移量或计数可能在 -1 附近
        if(offset > codePoints.length - count) { // 初始偏移量>源数据-初始位置的数据后的长度
            throw new StringIndexOutOfBoundsException(offset + count); // 抛出索引越界异常
        }

        final int end = offset + count;

        int n = count;
        for(int i = offset; i < end; i++) {
            int c = codePoints[i];
            if(Character.isBmpCodePoint(c)) // 确定指定的字符（c）是否位于Basic Multilingual Plane中。BmpCodePoint有效点在65535之间
                continue;                   // 这样的代码点可以用一个字符表示，返回是boolean值.true-继续
            else if(Character.isValidCodePoint(c)) // 确定指定的代码点是否是一个有效的Unicode代码点值。
                n++;
            else throw new IllegalArgumentException(Integer.toString(c)); // 抛出不合法参数异常
        }

        // 分配和填充char数组
        final char[] v = new char[n];

        for(int i = offset,j = 0; i < end; j++) {
            int c = codePoints[i];
            if(Character.isBmpCodePoint(c))  // 同上
                v[j] = (char) c;
            else
                Character.toSurrogates(c, v, j++);
        }

        this.value = v;
    }

    /**
     * @Author：zhuangfei
     * @Description：从数组的子数组中分配一个新的String，8位长度的对象
     * offset是子数组的第一个字节的索引，count指定子数组的参数长度
     * 在子数组中调用下面的方法把字节转换成char
     *PS：如果offset或count的参数无效，会抛出 IndexOutOfBoundsExceprion (数组下标越界异常)
     * @Date：17:50 2017/11/8
     */
    public String(byte ascii[], int hibyte, int offset, int count) {
        checkBounds(ascii, offset, count);
        char value[] = new char[count];

        if(hibyte == 0) {
            for(int i = count; i-- > 0;) {
                value[i] = (char) (ascii[i + offset] & 0xff);
            }
        } else {
            hibyte <<= 8;
            for(int i = count; i-- > 0;) {
                value[i] = (char) (hibyte | (ascii[i + offset] & 0xff));
            }
        }
        this.value = value;
    }

    /**
     * @Author：zhuangfei
     * @Description：分配一个新的String对象，包含了一个8位整数的数组，每个字符产生产生的字符串由
     * 相应的组件构成在字节数组中
     * ascii：需要转换成字符的字节
     * hibyte：每个16位Unicode代码单元的前8位
     * @Date：18:01 2017/11/8
     */
    public String(byte ascii[], int hibyte) {
        this(ascii, hibyte, 0, ascii.length);
    }

    /**
     * @Author：zhuangfei
     * @Description：用于检查数组边界的公共方法
     * @Date：18:05 2017/11/8
     */
    public static void checkBounds(byte[] bytes, int offset, int length) {
        if(length < 0) {
            throw new StringIndexOutOfBoundsException(length); // 抛出索引越界异常
        }
        if(offset < 0) {
            throw new StringIndexOutOfBoundsException((offset)); // 抛出索引越界异常
        }
        if(offset > bytes.length - length) {
            throw new StringIndexOutOfBoundsException(offset + length); // 抛出索引越界异常
        }
    }

    /**
     * @Author：zhuangfei
     * @Description：通过解码指定的子数组和指定字符集的字节来构造一个新的String对象
     * 新String的长度是字符集的函数，因此可能不等于子数组的长度
     *bytes[]：被解码成字符的字节
     * offset：解码第一个字节的索引
     * length：解码的字节数
     * charsetName：一个支持的字符集的名称
     * PS：如果解码是不支持的字符集，会抛出 UnsupportedEncodingException(不支持的命名字符集)
     *   如果offset和length所得到的值不在源数组的下标内，会抛出：IndexOutOfBoundsException(数组下标越界异常)
     * @Date：18:09 2017/11/8
     */
    public String(byte bytes[], int offset, int length, String charsetName)
            throws UnsupportedEncodingException {

        if(charsetName == null) {
            throw new NullPointerException("charsetName"); // 空指针异常
        }
        checkBounds(bytes, offset, length); // 转码
        this.value = StringCoding.decode(charsetName, bytes, offset, length);
    }

    /**
     * @Author：zhuangfei
     * @Description：通过解码指定的子数组产生一个新的String对象，解码
     * 字符集使用的是charset的字节。
     * 新的String对象的长度是字符集的长度，因此可能不等于子数组的长度
     * bytes[]：被解码成字符的字节
     * offset：解码第一个字节的索引
     * length：解码的字节数
     * charset：解码字符集
     * PS ：如果offset和length所得到的值不在源数组的下标内，会抛出：IndexOutOfBoundsException(数组下标越界异常)
     * @Date：18:22 2017/11/8
     */
    public String(byte bytes[], int offset, int length, Charset charset) {
        if(charset == null) {
            throw new NullPointerException("charset"); // 空指针异常
        }
        
        checkBounds(bytes, offset, length); // 转码
        this.value = StringCoding.decode(charset,bytes, offset, length);
        
    }
    
    /**
     * @Author：zhuangfei
     * @Description：通过解码指定的字节数组构造一个新的String对象，解码字符集
     * 使用的是charsetName的字符集。
     * 新的String对象的长度是字符集解码后的函数，因此可能不等于原字节数组的长度
     * bytes[]：源数组
     * charsetName：命名字符集
     * PS：如果使用了不支持的字符集解码，会抛出：UnsupportedEncodingException(不支持的命名字符集异常)
     * @Date：18:29 2017/11/8
     */
    public String(byte bytes[], String charsetName) throws UnsupportedEncodingException{
        this(bytes, 0, bytes.length, charsetName); // 调用上面的方法来解码
    }

    /**
     * @Author：zhuangfei
     * @Description：通过解码指定的字节数组构造一个新的String对象，解码字符集
     * 使用的是charsetName的字符集。
     * 新的String对象的长度是字符集解码后的函数，因此可能不等于原字节数组的长度
     * bytes[]：源数组
     * charsetName：命名字符集
     * @Date：18:33 2017/11/8
     */
    public String(byte bytes[], Charset charset) {
        this(bytes, 0, bytes.length, charset); // 调用上面的方法来解码
    }

    /**
     * @Author：zhuangfei
     * @Description：使用默认的字符集创建一个新的String对象，
     * 因新字符串的长度使用的是默认字符集的编码，所以长度可能和源数组不一样
     * bytes ： 源数组
     * offset ：初始偏移量
     * length ：解码字节数
     * PS：如果offset或length的结果不在源数组下标内，会抛出 ：IndexOutOfBoundsException(数组下标越界异常)
     * @Date：10:50 2017/11/9
     */
    public String(byte[] bytes, int offset, int length) {
        checkBounds(bytes, offset, length); // 调用公共方法解码
        this.value = StringCoding.decode(bytes, offset, length); // decode方法如果没有指定编码格式会调用系统默认的编码格式
    }
    
    /**
     * @Author：zhuangfei
     * @Description：使用系统默认的字符集构造一个新的String对象，因新字符串的长度使用
     * 的是默认字符集编码，所以长度可能和源数组不一样
     * byte ：源数组
     * @Date：11:04 2017/11/9
     */
    public String(byte[] bytes) {
        this(bytes, 0, bytes.length); // 调用上面的方法解码
    }

    /**
     * @Author：zhuangfei
     * @Description：创建一个包含当前缓冲区里参数的新字符串，
     * 原字符串缓冲区里的内容被复制，复制后原缓冲区里的修改不会影响新建的字符串。
     * 因为它加上了同步命令，所以是线程安全的
     * buffer ：源字符串
     * @Date：11:08 2017/11/9
     */
    public String(StringBuffer buffer) {
        synchronized(buffer) {
            this.value = Arrays.copyOf(buffer.getValue(), buffer.length()); // 调用数组复制方法
        }
    }
    
    /**
     * @Author：zhuangfei
     * @Description：创建一个包含当前缓冲区里参数的新字符串，
     * 原字符串缓冲区里的内容被复制，复制后原缓冲区里的修改不会影响新建的字符串。
     * 它没有同步命令，所以在执行速度上比StringBuffer要快，但它在线程中是不安全的
     * @Date：11:15 2017/11/9
     */
    public String(StringBuilder builder) {
        this.value = Arrays.copyOf(builder.getValue(), builder.length()); // 调用数组复制方法
    }
    
    /**
     * @Author：zhuangfei
     * @Description：它是一个用来保护类型的构造方法，虽然它比上面的String构造方法多了一个boolean值，
     * 但是也只支持true,所以加上share仅仅是为了能够重载。使用起来和上面的构造方法也不同，上面的方法是在创建
     * String的时候会被用到，使用Arrays的copyOf方法把value的内容逐一复制到String中，而下面这个方法是直接将value
     * 值赋给String.这个方法相比起来1、性能好，直接赋值和逐一复制在速度上就快很多，而且它能共享内部数组以节约内存。
     * @Date：11:20 2017/11/9
     */
    String(char[] value, boolean share) {
        this.value = value;
    }

    /**
     * @Author：zhuangfei
     * @Description：返回调用该方法的对象的长度
     * @Date：11:47 2017/11/9
     */
    public int length() {
        return value.length;
    }

    /**
     * @Author：zhuangfei
     * @Description：判断指定对象是否为空，是-true,否-false
     * @Date：11:52 2017/11/9
     */
    public boolean isEmpty() {
        return value.length == 0;
    }

    /**
     * @Author：zhuangfei
     * @Description：返回指定对象的第X下标的值
     * 注：数组是从0开始计数，如果要返回最后一个值需要对象.length - 1
     * index ：指定的下标
     * PS ： 如果传入的下标不在指定数组的范围之内，会抛出：IndexOutOfBoundsException(数组下标越界异常)
     * @Date：11:54 2017/11/9
     */
    public char charAt(int index) {
        if(index < 0 || index >= value.length) {
            throw new StringIndexOutOfBoundsException(index); // 抛出数组下标越界异常
        }
        return value[index];
    }

    /**
     * @Author：zhuangfei
     * @Description：返回指定索引处的字符(Unicode)，索引引用char值，范围从0~value.length - 1
     * index ：指定的索引值
     * PS ：如果指定的索引不在对象的范围内，会抛出：IndexOutOfBoundsException(数组下标越界异常)
     * @Date：11:59 2017/11/9
     */
    public int codePointAt(int index) {
        if(index < 0 || index >= value.length) {
            throw new StringIndexOutOfBoundsException(index); // 抛出数组下标越界异常
        }
        return Character.codePointAtImpl(value, index, value.length);
    }

    /**
     * @Author：zhuangfei
     * @Description：获取指定下标的前一个字符的值
     * index ：指定的下标
     * PS ：如果传入的下标不在源数据范围内，会抛出：IndexOutOfBoundsException(数组下标越界异常)
     * @Date：12:08 2017/11/9
     */
    public int codePointBefore(int index) {
        int i = index - 1;
        if(i < 0 || i >= value.length) {
            throw new StringIndexOutOfBoundsException(index); // 抛出数组下标越界异常
        }
        return Character.codePointBeforeImpl(value, index, 0); // 返回指定下标前一个的数据值
    }

    /**
     * @Author：zhuangfei
     * @Description：返回一个新的String对象，内容为指定字符串指定的开始位置到指定的结束位置，
     * 新String的长度为endIndex - beginInden的长度。
     * beginIndex ：开始位置
     * endIndex ：结束位置
     * 注 ：如果结束位置是原字符串的最后位置那么需要让 endIndex - 1，否则会抛出：IndexOutOfBoundsException(数组下标越界异常)
     * @Date：12:18 2017/11/9
     */
    public int codePointCount(int beginIndex, int endIndex) {
        if(beginIndex < 0 || endIndex > value.length || beginIndex > endIndex) {
            //throw new IndexOutOfBoundsException();  // 抛出数组下标越界异常  这是源码里抛出的异常
            throw new StringIndexOutOfBoundsException("begin:"+beginIndex+"-end:"+endIndex); // 这是我自己修改后的抛出异常，亲测可用
        }
        return Character.codePointCountImpl(value, beginIndex, endIndex - beginIndex); //
    }

    /**
     * @Author：zhuangfei
     * @Description：返回一个给定的字符序列，序列是从index处开始向codePointOffset处偏移
     * 比如 2,4，就是指从第2个开始向右偏移4个字节然后返回当前的序列
     * index ：开始序列点
     * codePointOffset ：偏移量
     * PS ：如果给定的序列或偏移量超出了原数组的范围，会抛出：IndexOutOfBoundsException(数组下标越界异常)
     * @Date：13:50 2017/11/9
     */
    public int offsetByCodePoints(int index, int codePointOffset) {
        if(index < 0 || index >= value.length) {
            throw new IndexOutOfBoundsException(); // 抛出数组下标越界异常
        }
        return Character.offsetByCodePointsImpl(value, 0, value.length, index, codePointOffset);
    }

    /**
     * @Author：zhuangfei
     * @Description：复制字符串到dst，从指定位置dstBegin开始，
     * 这个方法不会执行任何范围检查
     * dst ：源数组
     * dstBegin ：开始复制的位置
     * @Date：14:00 2017/11/9
     */
    void getChars(char dst[], int dstBegin) {
        System.arraycopy(value, 0, dst, dstBegin, value.length);
    }
    
    /**
     * @Author：zhuangfei
     * @Description：从源字符串复制字符到目标字符数组
     * srcBegin ： 第一个被复制的字符的索引
     * srcEnd ： 最后一个被复制的字符的索引，如果是源字符串的最后一位需要 - 1
     * dst[] ： 复制出来的字符串存放的位置
     * dstBegin ：从索引开始的源数组到子数组之前的序列
     * PS ：如果开始序列和结束序列不在源数组的范围之内，会抛出：IndexOutOfBoundsException(数组下标越界异常)
     * @Date：17:14 2017/11/9
     */
    public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin) {
        if(srcBegin < 0) {
            throw new StringIndexOutOfBoundsException(srcBegin); // 抛出数组下标越界异常
        }
        if(srcEnd > value.length) {
            throw new StringIndexOutOfBoundsException(srcEnd); // 抛出数组下标越界异常
        }
        if(srcBegin > srcEnd) {
            throw new StringIndexOutOfBoundsException(srcEnd - srcBegin); // 抛出数组下标越界异常
        }
        
        System.arraycopy(value, srcBegin, dst, dstBegin, srcEnd - srcBegin);
    }

    /**
     * @Author：zhuangfei
     * @Description：获得指定对象的byte类型，每一个字节接受对应字符的8个低阶位。每个字符的8个高阶位
     * 不能复制。
     * srcBegin ：第一个被复制的字节
     * srcEnd ：最后一个被复制的字节
     * dst ：从dstBegin处开始，以索引结束
     * dstBegin ：从此处开始复制进dst数组中
     * @Date：17:26 2017/11/9
     */
    @Deprecated
    public void getBytes(int srcBegin, int srcEnd, byte dst[], int dstBegin) {
        if(srcBegin < 0){
            throw new StringIndexOutOfBoundsException(srcBegin); // 抛出数组下标越界异常
        }
        if(srcEnd > value.length) {
            throw new StringIndexOutOfBoundsException(srcEnd); // 抛出数组下标越界异常
        }
        if(srcBegin > srcEnd) {
            throw new StringIndexOutOfBoundsException(srcEnd - srcBegin); // 抛出数组下标越界异常
        }
        Objects.requireNonNull(dst);
        
        int j = dstBegin;
        int n = srcEnd;
        int i = srcBegin;
        char[] val = value;
        
        while(i < n) {
            dst[j++] = (byte) val[i++]; // 把字符串里的字符依次转换为byte数组
        }
    }

    /**
     * @Author：zhuangfei
     * @Description：根据指定的字符集把源数据转换成数组
     * cahrsetName ：指定的字符集
     * PS ：如果指定的字符集不存在，会抛出：UnsupportedEncodingException(不支持的字符编码异常)
     * @Date：13:30 2017/11/10
     */
    public byte[] getBytes(String charsetName) throws UnsupportedEncodingException{
        if(charsetName == null) {
            throw new NullPointerException(); // 抛出空指针异常
        }
        return StringCoding.encode(charsetName, value, 0, value.length);
    }
    
    /**
     * @Author：zhuangfei
     * @Description：根据指定的字符编码转换源数据为数组
     * charset　：指定的字符集
     * @Date：13:36 2017/11/10
     */
    public byte[] getBytes(Charset charset) {
        if(charset == null) {
            throw new NullPointerException(); // 抛出空指针异常
        }
        return StringCoding.encode(charset, value, 0, value.length);
    }

    /**
     * @Author：zhuangfei
     * @Description：如果没有指定字符编码，系统会使用工具
     * 默认的字符集来进行字符串到数组的转换
     * @Date：13:40 2017/11/10
     */
    public byte[] getBytes() {
        return StringCoding.encode(value, 0, value.length);
    }

    /**
     * @Author：zhuangfei
     * @Description：使用源数据对指定数据进行比较，相同-true，不同-false
     * anObject ：被比较的对象
     * @Date：13:46 2017/11/10
     */
    public boolean equals(Object anObject) {
        if(this == anObject) {
            return true;
        }
        // 源数据和指定数据进行逐一比较
        if(anObject instanceof String) {
            String anotherString = (String) anObject;
            int n = value.length;
            if(n == anotherString.value.length) {
                char v1[] = value;
                char v2[] = anotherString.value;
                int i = 0;
                while(n-- != 0) {
                    if(v1[i] != v2[i]) {
                        return false;
                    }
                    i++;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * @Author：zhuangfei
     * @Description：当且仅当此字符串与要比较的字符序列相同时，返回-true，不同-false
     * sb ：被比较的字符序列
     * @Date：13:53 2017/11/10
     */
    public boolean contentEquals(StringBuffer sb) {
        return contentEquals(sb);
    }
    
    /**
     * @Author：zhuangfei
     * @Description：这个方法是下面方法的部分实现
     * sb ：参数可以为StringBuffer ，StringBuilder
     * @Date：13:59 2017/11/10
     */
    private boolean nonSyncContentEquals(AbstractStringBuilder sb) {
        char v1[] = value;
        char v2[] = sb.getValue();
        int n = v1.length;
        if(n != sb.length()) {
            return false;
        }
        for(int i = 0; i < n; i++) {
            if(v1[i] != v2[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * @Author：zhuangfei
     * @Description：这个方法是直接把AbstractStringBuilder的value字段拿出来读取内容，如果是StringBuffer
     * 的话，会可能发生另一个线程在读取的同时对它做修改，那样value里的值就会发生变化，线程就不安全了。
     * 这个方法是为了保护StringBuffer的线程安全而写的。
     * @Date：14:09 2017/11/10
     */
    public boolean contentEquals(CharSequence cs) {
        // 参数可以为 StringBuffer ，StringBuilder
        if(cs instanceof AbstractStringBuilder) {
            if(cs instanceof StringBuffer) {
                synchronized (cs) {
                    return nonSyncContentEquals((AbstractStringBuilder) cs);
                }
            } else {
                return nonSyncContentEquals((AbstractStringBuilder) cs);
            }
        }
        // 参数为String
        if(cs instanceof String) {
            return equals(cs);
        }
        // 参数是一个泛型的 CharSequence
        char v1[] = value;
        int n = v1.length;
        if(n != cs.length()) {
            return false;
        }
        for(int i = 0; i < n; i++) {
            if(v1[i] != cs.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @Author：zhuangfei
     * @Description：比较两个字符串是否相等，忽略大小写的比较
     * anotherString ：指定被比较的字符串
     * @Date：14:20 2017/11/10
     */
    public boolean equalsIgnoreCase(String anotherString) {
        return (this == anotherString) ? true:(anotherString != null) && (anotherString.value.length == value.length)
                && regionMatches(true, 0, anotherString, 0, value.length);
    }

    /**
     * @Author：zhuangfei
     * @Description：比较两个字符串区域是否相等
     * 若该字符串的指定子区域精确匹配参数字符串中的指定子区域则返回 true ，否则返回 false
     * ignoreCase ：为true时，忽略大小写
     * toffset ：该分区域在此字符串中的起始偏移量
     * other ：字符串参数
     * ooffset ：该区域中字符串参数的起始偏移量
     * len ：比较的字符的数目
     * @Date：14:31 2017/11/10
     */
    public boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len) {
        char ta[] = value;
        int to = toffset;
        char pa[] = other.value;
        int po = ooffset;
        // toffset，ooffset和len可能为-1
        if((ooffset < 0) || (toffset < 0) || (toffset > (long) value.length - len)
            || (ooffset > (long) other.value.length - len)) {
            return false;
        }
        while(len-- > 0) {
            char c1 = ta[to++];
            char c2 = pa[po++];
            if(c1 == c2) {
                continue;
            }
            if(ignoreCase) {
                // 如果字符不匹配，情况可能会忽略
                // 尝试将两个字符转换大小写
                char u1 = Character.toUpperCase(c1);
                char u2 = Character.toUpperCase(c2);
                if(u1 == u2) {
                    continue;
                }
                // 转换到大写不能正常工作
                // 对于格鲁吉亚字符表，它有奇怪的规则，所以需要再做一次检查
                if(Character.toLowerCase(u1) == Character.toLowerCase(u2)) {
                    continue;
                }
            }
            return false;
        }
        return true;
        
    }
    
    /**
     * @Author：zhuangfei
     * @Description：正序对比两个字符串中的字符，
     * 由此返回第一个不相等的字符的差值或者两个字符串的长度差值；
     * anotherString ：被比较的字符串
     * @Date：14:43 2017/11/10
     */
    public int compareTo(String anotherString) {
        int len1 = value.length;
        int len2 = anotherString.value.length;
        int lim = Math.min(len1, len2);
        char v1[] = value;
        char v2[] = anotherString.value;
        
        int k = 0;
        while(k < lim) {
            char c1 = v1[k];
            char c2 = v2[k];
            if(c1 != c2) {
                return c1 - c2;
            }
            k++;
        }
        return len1 - len2;
    }


    /**
     * @Author：zhuangfei
     * @Description：比较器，用来对比两个字符串是否相等，然后返回它们之间不同的序列值
     * @Date：17:23 2017/11/10
     */
    public static final Comparator<String> CASE_INSENSIIIVE_ORDER = new ASCIICaseInsensitiveComparator();

    private static class CaseInsensitiveComparator implements Comparator<String>, java.io.Serializable {

        private static final long serialVersionUID = 8575799808933029326L;

        public int compare(String s1, String s2) {
            int n1 = s1.length();
            int n2 = s2.length();
            int min = Math.min(n1, n2);
            for (int i = 0; i < min; i++) {
                char c1 = s1.charAt(i);
                char c2 = s2.charAt(i);
                if (c1 != c2) {
                    c1 = Character.toUpperCase(c1); // 比较大写是否相同
                    c2 = Character.toUpperCase(c2);
                    if(c1 != c2) {
                        c1 = Character.toLowerCase(c1); // 比较小写是否相同
                        c2 = Character.toLowerCase(c2);
                        if (c1 != c2) {
                            // 没有因为数字提升而溢出
                            return c1 - c2;
                        }
                    }
                }
            }
            return n1 -n2;
        }

        // 取代了反序列化对象
        private Object readResolve() {
            return CASE_INSENSIIIVE_ORDER;
        }
    }

    /**
     * @Author：zhuangfei
     * @Description：比较两个字符串(不考虑大小写) ，返回一个整数。
     * 大小写的差异是通过调用每个字符的Character.toLowerCase(Character.toUpperCase(character)) 来消除
     * str ：被比较的字符串
     * @Date：17:27 2017/11/10
     */
    public int compareToIgnoreCase(String str) {
        return CASE_INSENSIIIVE_ORDER.compare(this, str);
    }
    
    /**
     * @Author：zhuangfei
     * @Description：比较两个字符串区域是否相同
     *若该字符串的指定子区域精确匹配参数字符串中的指定子区域则返回 true ，否则返回 false
     *toffset -- 该分区域在此字符串中的起始偏移量。
     *other -- 字符串参数。
     *ooffset -- 该次区域中的字符串参数的起始偏移量。
     *len -- 比较的字符的数目
     * @Date：17:37 2017/11/10
     */
    public boolean regionMatches(int toffset, String other, int ooffset, int len) {
        char ta[] = value;
        int to = toffset;
        char pa[] = other.value;
        int po = ooffset;
        // 注意：toffset，ooffset和len可能为 -1
        if((ooffset < 0) || (toffset < 0)
                || (toffset > (long)value.length - len)
                || (ooffset > (long)other.value.length - len)) {
            return false;
        }
        
        while(len-- > 0) {
            if(ta[to++] != pa[po++]) {
                return false;
            }
        }
        return true;
    }

    /**
     * @Author：zhuangfei
     * @Description：判断指定的字符串是否以传输的字符串的位置开始，是-true，否-false
     * prefix ：判断是否为前缀的字符串
     * toffset ：指定开始的位置
     * @Date：18:03 2017/11/13
     */
    public boolean startsWith(String prefix, int toffset) {
        char ta[] =  value;
        int to = toffset;
        char pa[] = prefix.value;
        int po = 0;
        int pc = prefix.value.length;
        // 注意：toffset有可能为 -1
        if((toffset < 0) || (toffset > value.length - pc)) {
            return false;
        }
        while(--pc >= 0) {
            if(ta[to++] != pa[po++]) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * @Author：zhuangfei
     * @Description：判断指定字符串是否以传输字符串的位置开始，是-true，否-false
     * prefix ：判断是否为前缀的字符串
     * 默认从第0位开始判断
     * @Date：18:11 2017/11/13
     */
    public boolean startsWith(String prefix) {
        return startsWith(prefix, 0);
    }
    
    /**
     * @Author：zhuangfei
     * @Description：判断指定的字符串是否以传输的字符串为结尾，是-true，否-false
     * @Date：18:13 2017/11/13
     */
    public boolean endWith(String suffix) {
        return startsWith(suffix, value.length - suffix.length());
    }

    /**
     * @Author：zhuangfei
     * @Description：返回指定对象的哈希码
     * @Date：18:15 2017/11/13
     */
    public int hashCode() {
        int h = hash;
        if(h == 0 && value.length > 0) {
            char val[] = value;
            for(int i = 0; i < value.length; i++) {
                h = 31 * h + val[i];
            }
            hash = h;
        }
        return h;
    }

    /**
     * @Author：zhuangfei
     * @Description：返回指定字符串里字符第一次出现的位置，如果该指定字符不存在，返回-1
     * ch ：指定字符
     * @Date：18:30 2017/11/13
     */
    public int indexOf(int ch) {
        return indexOf(ch, 0);
    }

    /**
     * @Author：zhuangfei
     * @Description：返回指定字符串里字符在指定位置之后第一次出现的位置，如果没有，返回-1
     * ch ：指定字符
     * fromIndex ：指定的位置
     * @Date：18:30 2017/11/13
     */
    public int indexOf(int ch, int fromIndex) {
        final int max = value.length;
        if(fromIndex < 0) {
            fromIndex = 0;
        } else if(fromIndex >= max) {
            // 注意：fromIndex可能为 -1
            return -1;
        }

        if(ch < Character.MIN_SUPPLEMENTARY_CODE_POINT) {  // 0-0x010000
            // 在这里处理大多数情况(ch 是BMP或负值(无效的值))
            final char[] value = this.value;
            for(int i = fromIndex; i < max; i++) {
                if(value[i] == ch) {
                    return i;
                }
            }
            return -1;
        } else {
            return indexOfSuplementary(ch, fromIndex);
        }
    }
    
    /**
     * @Author：zhuangfei
     * @Description：处理索引的一个补充方法(用的很少)
     * @Date：18:33 2017/11/13
     */
    public int indexOfSuplementary(int ch, int fromIndex) {
        if(Character.isValidCodePoint(ch)) {
            final char[] value = this.value;
            final char hi = Character.highSurrogate(ch);
            final char lo = Character.lowSurrogate(ch);
            final int max = value.length - 1;
            for(int i = fromIndex; i < max; i++) {
                if(value[i] == hi && value[i + 1] == lo) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * @Author：zhuangfei
     * @Description：返回指定字符串中指定字符最后一次出现的位置，如果没有返回 -1
     * ch ：指定的字符
     * @Date：18:45 2017/11/13
     */
    public int lastIndexOf(int ch) {
        return lastIndexOf(ch, value.length - 1);
    }

    /**
     * @Author：zhuangfei
     * @Description：返回指定字符串中指定字符在指定位置之后最后一次出现的位置，如果没有返回 -1
     * ch ：指定字符
     * lastIndex ：指定位置
     * @Date：18:46 2017/11/13
     */
    public int lastIndexOf(int ch, int lastIndex) {
        if(ch < Character.MIN_SUPPLEMENTARY_CODE_POINT) {
            // 在这里处理大多数情况(ch 是BMP或负值(无效的值))
            final char[] value = this.value;
            int i = Math.min(lastIndex, value.length - 1);
            for(; i >= 0; i--) {
                if(value[i] == ch) {
                    return i;
                }
            }
            return -1;
        } else {
            return lastIndexOfSupplementary(ch, lastIndex);
        }
    }

    /**
     * @Author：zhuangfei
     * @Description：指定字符最后出现的补充方法,(用的很少)
     * @Date：18:49 2017/11/13
     */
    public int lastIndexOfSupplementary(int ch, int lastIndex) {
        if(Character.isValidCodePoint(ch)) {
            final char[] value = this.value;
            char hi = Character.highSurrogate(ch);
            char lo = Character.lowSurrogate(ch);
            int i = Math.min(lastIndex, value.length - 2);
            for(; i >= 0; i--) {
                if(value[i] == hi && value[i + 1] == lo) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * @Author：zhuangfei
     * @Description：返回指定字符串指定字符第一次出现的位置，默认从0开始
     * str ：指定的字符
     * @Date：18:52 2017/11/13
     */
    public int indexOf(String str) {
        return indexOf(str, 0);
    }

    /**
     * @Author：zhuangfei
     * @Description：返回指定字符串指定字符在指定位置后第一次出现的位置
     * str ：指定的字符
     * fromIndex ：指定的位置
     * @Date：18:56 2017/11/13
     */
    public int indexOf(String str, int fromIndex) {

        return indexOf(value, 0, value.length, str.value, 0, str.value.length, fromIndex);
    }

    /**
     * @Author：zhuangfei
     * @Description：通过指定的字符在目标对象里进行搜索
     * source ：指定的对象
     * sourceOffset ：指定对象的初始偏移量
     * sourceCount ：对源数据的计数
     * target ：被搜索的字符
     * fromIndex ：从指定我位置开始搜索
     * @Date：19:00 2017/11/13
     */
    public int indexOf(char[] source, int sourceOffset, int sourceCount, String target, int fromIndex) {
        return indexOf(source, sourceOffset, sourceCount,target.value, 0, target.value.length,fromIndex);
    }

    /**
     * @Author：zhuangfei
     * @Description：通过指定的字符在目标对象里进行搜索
     *source ：指定的对象
     * sourceOffset ：指定对象的初始偏移量
     * sourceCount ：对源数据的计数
     * target ：被搜索的字符
     * targetOffset ：搜索字符的偏移量
     * targetCount ：搜索字符的计数
     * fromIndex ：从指定我位置开始搜索
     * @Date：19:12 2017/11/13
     */
    public int indexOf(char[] source, int sourceOffset, int sourceCount, char[] target, int targetOffset, int targetCount, int fromIndex) {
        if(fromIndex >= sourceCount) {
            return (targetCount == 0? sourceCount:-1);
        }
        if(fromIndex < 0) {
            fromIndex = 0;
        }
        if(targetCount == 0) {
            return fromIndex;
        }

        char first = target[targetOffset];
        int max = sourceOffset + (sourceCount - targetCount);
        for(int i = sourceOffset + fromIndex; i <= max; i++) {
            // 寻找第一个字符
            if(source[i] != first) {
                while(++i <= max && source[i] != first);
            }

            // 找到第一个字符，然后再看看v2剩余的地方
            if(i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                for(int k = targetOffset + 1; j < end && source[j] == target[k]; j++,k++);
                if(j == end) {
                 //  在此寻找第一个字符
                    return i - sourceOffset;
                }
            }
        }
        return -1;
    }

    /**
     * @Author：zhuangfei
     * @Description：返回指定对象中指定字符最后出现的位置，如果没有返回-1
     * str ：指定的字符
     * @Date：14:34 2017/11/14
     */
    public int lastIndexOf(String str) {
        return lastIndexOf(str, value.length);
    }

    /**
     * @Author：zhuangfei
     * @Description：返回指定对象中指定字符在指定位置后最后出现的位置，如果没有返回-1
     * str ：指定的字符
     * fromIndex ：指定的位置
     * @Date：14:36 2017/11/14
     */
    public int lastIndexOf(String str, int fromIndex) {
        return lastIndexOf(value, 0, value.length, str.value, 0, str.value.length, fromIndex);
    }

    /**
     * @Author：zhuangfei
     * @Description：返回指定字符串中指定偏移量之后的指定位置后的指定字符最后出现的位置，
     *                  如果没有返回-1
     * source ：源数据
     * sourceOffset  ：指定的偏移量
     * sourceCount ：源字符串的计数
     * target ：指定的字符
     * fromIndex ：指定搜索的位置
     * @Date：14:38 2017/11/14
     */
    public int lastIndexOf(char[] source, int sourceOffset, int sourceCount, String target, int fromIndex) {
        return lastIndexOf(source, sourceOffset, sourceCount, target.value, 0, value.length, fromIndex);
    }

    /**
     * @Author：zhuangfei
     * @Description：返回指定字符串中指定偏移量后指定字符的偏移量的最后出现的位置，如果没有返回-1
     * * source ：源数据
     * sourceOffset  ：指定的偏移量
     * sourceCount ：源字符串的计数
     * target ：指定的字符
     * targetOffset ：指定字符的偏移量
     * targetCount ：指定字符的计数
     * fromIndex ：指定搜索的位置
     * @Date：14:48 2017/11/14
     */
    public int lastIndexOf(char[] source, int sourceOffset, int sourceCount, char[] target, int targetOffset, int targetCount, int fromIndex) {

        // 检查参数后立即返回，对于一致性，不检查 null str
        int rightIndex = sourceCount - targetCount;
        if(fromIndex < 0) {
            return -1;
        }
        if(fromIndex > rightIndex) {
            fromIndex = rightIndex;
        }
        // 总是匹配空字符串
        if(targetCount == 0) {
            return fromIndex;
        }
        
        int strLastIndex = targetOffset + targetCount - 1;
        char strLastChar = target[strLastIndex];
        int min = sourceOffset + sourceCount - 1;
        int i = min + fromIndex;

        startSearchForLastChar:
            while(true) {
                while(i >= min && source[i] != strLastChar) {
                    i--;
                }
                if(i < min) {
                    return -1;
                }
                int j = i - 1;
                int start = j - (targetCount - 1);
                int k = strLastIndex - 1;
                
                while(j > start) {
                    if(source[j--] != target[k--]) {
                        i--;
                        continue startSearchForLastChar;
                    }
                }
                return start - sourceOffset + 1;
            }
    }

    /**
     * @Author：zhuangfei
     * @Description：返回从指定位置后的所有字符数据
     * beginIndex ：指定的位置
     * PS：如果指定的位置不存在会抛出 IndexOfBoundsException(数组下标越界异常)
     * @Date：14:59 2017/11/14
     */
    public String substring(int beginIndex) {
        if(beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(beginIndex); // 抛出字符下标越界异常
        }
        int subLen = value.length - beginIndex;
        if(subLen < 0) {
            throw new StringIndexOutOfBoundsException(subLen);  // 抛出字符下标越界异常
        }
        return (beginIndex == 0)? this:new String(value, beginIndex, subLen);
    }

    /**
     * @Author：zhuangfei
     * @Description：返回指定对象从指定开始位置到指定结束位置之间的字符内容，前截后不截
     * beginIndex ：开始截取的位置
     * endIndex ： 结束截取的位置
     * PS ：开始和结束的下标不能为负和超过字符串本身长度，否则会抛：IndexOutBoundsException(数组下标越界异常)
     * @Date：15:04 2017/11/14
     */
    public String substring(int beginIndex, int endIndex) {
        if(beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(beginIndex); // 抛出字符下标越界异常
        }
        if(endIndex > value.length) {
            throw new StringIndexOutOfBoundsException(endIndex); // 抛出字符下标越界异常
        }
        int subLen = endIndex - beginIndex;
        if(subLen < 0) {
            throw new StringIndexOutOfBoundsException(subLen); // 抛出字符下标越界异常
        }
        return ((beginIndex == 0) && (endIndex == value.length)) ?this:
                new String(value, beginIndex, subLen);
    }

    /**
     * @Author：zhuangfei
     * @Description：返回指定字符串从指定开始位置到指定结束位置的字符内容，如果没有返回-1
     * beginIndex ：开始截取的位置
     * endIndex ： 结束截取的位置
     * @Date：15:34 2017/11/14
     */
    public CharSequence subSequence(int beginIndex, int endIndex) {
        // 它调用的是substring方法，如果下标位置超出的话会抛异常 StringIndexOutOfBoundsException
        return this.substring(beginIndex, endIndex);
    }

    /**
     * @Author：zhuangfei
     * @Description：将指定字符串连接到此字符串的结尾
     * str ：连接的字符串
     * @Date：15:41 2017/11/14
     */
    public String concat(String str) {
        int otherLen = str.length();
        if(otherLen == 0) {
            return this;
        }
        int len = value.length;
        char buf[] = Arrays.copyOf(value, len + otherLen);
        str.getChars(buf, len) ;
        return new String(buf, true);
    }

    /**
     * @Author：zhuangfei
     * @Description：将指定对象里的指定字符替换为新字符
     * oldChar ：被替换的字符
     * newChar ：替换的字符
     * @Date：15:44 2017/11/14
     */
    public String replace(char oldChar, char newChar) {
        if(oldChar != newChar) {
            int len = value.length;
            int i = -1;
            char[] val = value;

            while(++i < len) {
                if(val[i] == oldChar) {
                    break;
                }
            }
            if(i < len) {
                char[] buf = new char[len];
                for(int j = 0; j < i; j++) {
                    buf[j] = val[j];
                }
                while(i < len) {
                    char c = val[i];
                    buf[i] = (c == oldChar) ? newChar:c;
                    i++;
                }
                return new String(buf, true);
            }
        }
        return this;
    }

    /**
     * @Author：zhuangfei
     * @Description：判断指定对象里的指定字符是否被正则所匹配，是-true，否-false
     * regex ：判断匹配的字符
     * @Date：15:49 2017/11/14
     */
    public boolean matches(String regex) {
        return Pattern.matches(regex, this);
    }
    
    /**
     * @Author：zhuangfei
     * @Description：判断传入的对象是否存在于指定对象中，是-true，否-false
     * s ：传入值
     * @Date：15:53 2017/11/14
     */
    public boolean contains(CharSequence s) {
        return indexOf(s.toString()) > -1;
    }

    /**
     * @Author：zhuangfei
     * @Description：替换指定字符串中与指定正则匹配的第一个字符串
     * regex ：用于正则匹配的字符
     * replacement ：进行替换的字符串
     * @Date：15:56 2017/11/14
     */
    public String replaceFirst(String regex, String replacement) {
        return Pattern.compile(regex).matcher(this).replaceFirst(replacement);
    }

    /**
     * @Author：zhuangfei
     * @Description：替换指定对象中所有与指定正则相匹配的字符
     * regex ：用于正则的字符
     * replacement ：进行替换的字符串
     * @Date：16:01 2017/11/14
     */
    public String replaceAll(String regex, String replacement) {
        return Pattern.compile(regex).matcher(this).replaceAll(replacement);
    }

    /**
     * @Author：zhuangfei
     * @Description：替换指定对象所有子对象的被匹配的字符为指定字符
     * target ：被替换的值
     * replacement ：替换的值
     * @Date：16:52 2017/11/14
     */
    public String replace(CharSequence target, CharSequence replacement) {
        return Pattern.compile(target.toString(), Pattern.LITERAL).matcher(this)
                .replaceAll(Matcher.quoteReplacement(replacement.toString()));
    }

    /**
     * @Author：zhuangfei
     * @Description：根据指定的正则匹配拆分指定的对象，返回的数组由子正则匹配，
     *  如果表达式不匹配任何字符，则返回整个字符串
     *  regex ：指定匹配的表达式
     *  limit ：控制模式施加的数的计数
     *  PS ：如果出入的正则不规范的话，会抛出 PatternSyntaxException(正则表达式异常)
     * @Date：16:58 2017/11/14
     */
    public String[] split(String regex, int limit) {

        char ch = 0;
        if(((regex.value.length == 1 &&
             ".$|()[{^?*+\\".indexOf(ch = regex.charAt(0)) == -1) ||
                (regex.length() == 2 &&
                 regex.charAt(0) == '\\' &&
                        (((ch = regex.charAt(1))-'0')|('9'-ch)) < 0 &&
                        ((ch-'a')|('z'-ch)) < 0 &&
                        ((ch-'A')|('Z'-ch)) < 0)) &&
                (ch < Character.MIN_HIGH_SURROGATE || ch > Character.MAX_LOW_SURROGATE)
           ) {
            int off = 0;
            int next = 0;
            boolean limited = limit > 0;
            ArrayList<String> list = new ArrayList<>();
            while((next = indexOf(ch, off)) != -1) {
                if(!limited || list.size() < limit -1) {
                    list.add(substring(off, next));
                    off = next + 1;
                } else {
                    //
                    list.add(substring(off, value.length));
                }
            }
            // 如果没有匹配的字符，返回整个对象
            if(off == 0)
                return new String[]{this};

            // 把匹配最后剩余的部分加上去
            int resultSize = list.size();
            if(limit == 0) {
                while(resultSize > 0 && list.get(resultSize - 1).length() == 0) {
                    resultSize--;
                }
            }
            String[] result = new String[resultSize];
            return list.subList(0, resultSize).toArray(result);
        }
        return Pattern.compile(regex).split(this, limit);
    }

    /**
     * @Author：zhuangfei
     * @Description：根据指定的字符分割指定的对象，默认从第一位开始匹配
     * regex ：匹配的字符
     * @Date：17:22 2017/11/14
     */
    public String[] aplit(String regex) {
        return split(regex, 0);
    }

    /**
     * @Author：zhuangfei
     * @Description：通过指定的字符把传入的字符全部链接起来，第一次时会新建一个
     * StringBuilder之后每次都会在StringBuilder上进行拼接
     * delimiter ：指定的链接字符
     * elements ：被连接的数据
     * PS ：delimiter和elements为空时将会抛出 NullPointerException(空指针异常)
     * @Date：17:29 2017/11/14
     */
    public static String join(CharSequence delimiter, CharSequence... elements) {
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(elements);
        //
        StringJoiner joiner = new StringJoiner(delimiter);
        for(CharSequence cs : elements) {
            joiner.add(cs);
        }
        return joiner.toString();
    }

    /**
     * @Author：zhuangfei
     * @Description：返回一个由指定字符拼接的对象
     * delimiter ：指定的拼接字符
     * elements ：被拼接的数据
     * PS ：delimiter和elements为空时将会抛出 NullPointerException(空指针异常)
     * @Date：17:38 2017/11/14
     */
    public static String join(CharSequence delimiter, Iterable<? extends CharSequence> elements) {
        Objects.requireNonNull(delimiter);
        Objects.requireNonNull(elements);
        StringJoiner joiner = new StringJoiner(delimiter);
        for(CharSequence cs:elements) {
            joiner.add(cs);
        }
        return joiner.toString();
    }

    // 2561
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    
    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    @Override
    public IntStream chars() {
        return null;
    }

    @Override
    public IntStream codePoints() {
        return null;
    }
    
}
