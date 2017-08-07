import java.math.BigInteger;
public class SnowFlakeIdTest{
    public static void main(String[] args){
        BigInteger sfid = SnowFlakeId.next();
        System.out.println("new SFID: " + sfid.toString());
        System.out.println("Human Readable SFID Case: " + SnowFlakeId.toHumanReadable(sfid));
    }
}
