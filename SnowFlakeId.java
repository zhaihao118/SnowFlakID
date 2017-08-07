import java.lang.management.ManagementFactory;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by zhaihao on 17/7/24.
 */
public class SnowFlakeId {
    private static long m_macId = 0;
    private static int m_pid = 0;

    /**
     * Human-readable
     * 让sfid变得可以阅读的解决思路：
     * 0 - 00000 1 - 00001 2 - 00010 3 - 00011
     * 4 - 00100 5 - 00101 6 - 00110 7 - 00111
     * 8 - 01000 9 - 01001 A - 01010 B - 01011
     * C - 01100 D - 01101 E - 01110 F - 01111
     * G - 10000 H - 10001 I - 与L、1相似，略去
     * J - 10010 K - 10011 L - 与I、1相似，略去
     * M - 10100 N - 10101 O - 与0相似，略去
     * P - 10110 Q - 10111 R - 11000 S - 与5相似，略去
     * T - 11001 U - 11010 V - 11011 W - 11100
     * X - 11101 Y - 11110 Z - 11111
     * 所以64位的SFID整形数，可以用64/5=12余4，一共13个符号既可以表示出来。可以用“012-34567-89ABC”的格式呈现优惠码，会员号等在系统用sfid表示的数据
     * 12491575082415873361 = 1010 11010 10110 10111 11101 01011 01010 11000 01000 01001 10111 01010 10001 = AUP-QXBAR-89QAH
     * 12491570832403129941 = 1010 11010 10110 10111 11001 01111 10100 10100 00000 01001 11001 10010 10101 = AUP-QTFMM-09TJN
     * 12491566990856848639 = 1010 11010 10110 10111 10101 11111 11010 11101 00000 01001 00001 00111 11111 = AUP-QNZUX-0917Z
     */
    private static final List<Integer> list = new ArrayList<Integer>() {{
        add((int) '0');
        add((int) '1');
        add((int) '2');
        add((int) '3');
        add((int) '4');
        add((int) '5');
        add((int) '6');
        add((int) '7');
        add((int) '8');
        add((int) '9');
        add((int) 'A');
        add((int) 'B');
        add((int) 'C');
        add((int) 'D');
        add((int) 'E');
        add((int) 'F');
        add((int) 'G');
        add((int) 'H');
        add((int) 'J');
        add((int) 'K');
        add((int) 'M');
        add((int) 'N');
        add((int) 'P');
        add((int) 'Q');
        add((int) 'R');
        add((int) 'T');
        add((int) 'U');
        add((int) 'V');
        add((int) 'W');
        add((int) 'X');
        add((int) 'Y');
        add((int) 'Z');
    }};
    private static final Map<Long, Integer> hash = new HashMap<Long, Integer>() {
        {
            long i = 0;
            for (int c : list) {
                put(i, c);
                i++;
            }
        }
    };

    private static int sequence;
    private static long timestamp;

//    public SnowFlakeId() {
//        synchronized (SnowFlakeId.class) {
//            this.sequence = 0;
//            this.timestamp = System.currentTimeMillis();
//        }
//    }
//
    public static BigInteger next() {
        BigInteger bigInteger;
        synchronized (SnowFlakeId.class) {
            bigInteger = getSfid();
            sequence++;
        }
        return bigInteger;
    }

    private static Long getLocalMac(InetAddress ia) throws SocketException {
        //获取网卡，获取地址
        if (m_macId == 0) {
            byte[] mac = NetworkInterface.getByInetAddress(ia).getHardwareAddress();
            long m = 0x00000000;
            for (byte aMac : mac) {
                m = m << 8;
                m = m | (aMac & 0xFF);
            }
            m_macId = m;
        }
        return m_macId;
    }

    private static Integer getPid() {
        if (m_pid == 0) {
            String name = ManagementFactory.getRuntimeMXBean().getName();
            m_pid = Integer.valueOf(name.split("@")[0]);
        }
        return m_pid;
    }

    private static BigInteger getSfid() {
        BigInteger result;
        /*
          SnowFlakeId规则介绍
           0     A     41    B     51    C   54  D  63
           +-----------+-----------+---------+--------+
           | timestamp |   hw id   | shardId | seq Id |
           +-----------+-----------+---------+--------+
           A. 前41位是timestamp，精确到千分之一秒，即mS。这样可以使ID生成变得有序。
           B. 10位HW ID(表示主机或者docker)，这样可以有1024个节点, HwID由MAC地址最后7位+本实例进程号最后3位组成。
           C. 3位分片id, 最大支持8个分片
           D. 最后10位为了解决并发冲突问题，并发请求时以此10位作累加，这样在同一个毫秒最多可以生成1024个ID。
         */

        long lMac = 0;
        long lPid = 0;


        Random random = new Random();

        try {
            lMac = getLocalMac(InetAddress.getLocalHost());
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
            System.out.println("获取mac地址失败，将使用默认的mac地址0作为sfid生成依据。");
        }
        lPid = getPid();


        long lTimestamp = System.currentTimeMillis();
        if (lTimestamp != timestamp) {
            timestamp = lTimestamp;
            sequence = 0;
        }

        //step1: 放置 timestamp
        long sfid = lTimestamp << 23;

        //step2: 放置hw、id
        sfid = sfid | ((lMac & 0b1111111) << 16);
        sfid = sfid | ((lPid & 0b111) << 13);

        //step3: 放置shardId
        long shardId = random.nextInt(8);
        sfid = sfid | (shardId << 10);

        //step4: 放置seqId
        long r = (long) sequence;
        sfid = sfid | r;

//        result = BigInteger.valueOf(sfid);
        result = new BigInteger(Long.toBinaryString(sfid), 2);
        return result;
    }

    public static BigInteger toSnowFlakId(String sfid) throws Exception {
        if (!testSfid(sfid)) {
            throw new Exception(new Throwable("非法的SFID：" + sfid));
        }
        Long result = 0xFFFFFFFFL;

        for (char c : sfid.toCharArray()) {
            if (c != '-') {

                int i = list.indexOf((int) c);
                result = result << 5 | (i & 0b11111);
            }
        }
        return new BigInteger(Long.toBinaryString(result), 2);
    }


    public static String toHumanReadable(final BigInteger bigSfid) {
        long sfid = bigSfid.longValue();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            if (i == 5 || i == 10)
                result.append("-");
            result.append((char) ((int) hash.get((long) (sfid >> (i * 5)) & 0b11111L)));
        }
        result.append((char) ((int) hash.get((long) ((sfid >> (12 * 5)) & 0b01111L) & 0b11111L)));
        result.reverse();
        return result.toString();
    }

    public static boolean testSfid(String sfid) {
        return true;
    }


//    public static void main(String[] args) {
//        for (int i=0;i<10000;i++) {
//
//            BigInteger sfid = next();
//            System.out.println(Long.toBinaryString(sfid.longValue()));
//        }
//        System.out.println(Long.toBinaryString(System.currentTimeMillis()));
//    }
}
