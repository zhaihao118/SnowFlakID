# SnowFlakID Generator: #

#### For Example:

~~~
new SFID: 	12600689748968350720
Binary Type: 	1010111011011110101001001001000010111100001010100010010000000000
Human Readable SFID Case: AXQ-N4J2Y-2M900
~~~

#### Human Readable ####

~~~
Map Rules:
0 - 00000 1 - 00001 2 - 00010 3 - 00011
4 - 00100 5 - 00101 6 - 00110 7 - 00111
8 - 01000 9 - 01001 A - 01010 B - 01011
C - 01100 D - 01101 E - 01110 F - 01111
G - 10000 H - 10001 I - (similar to L、1, unuse)
J - 10010 K - 10011 L - (similar to L、1, unuse)
M - 10100 N - 10101 O - (similar to 0, unuse)
P - 10110 Q - 10111 R - 11000 (S - similar to 5,unuse)
T - 11001 U - 11010 V - 11011 W - 11100
X - 11101 Y - 11110 Z - 11111

~~~


#### SnowFlakeId规则介绍

~~~
0                    A                    41    B    51   C   54    D   63
+------------------------------------------+----------+-------+---------+
|                 timestamp                |   hw id  |shardId| seq Id  |
|101011101101111010100100100100001011110000|1010100010|  010  |000000000|
+------------------------------------------+----------+-------+---------+
A. 前41位是timestamp，精确到千分之一秒，即mS。这样可以使ID生成变得有序。
B. 10位HW ID(表示主机或者docker)，这样可以有1024个节点, HwID由MAC地址最后7位+本实例进程号最后3位组成。
C. 3位分片id, 最大支持8个分片
D. 最后10位为了解决并发冲突问题，并发请求时以此10位作累加，这样在同一个毫秒最多可以生成1024个ID。
~~~

#### Usage:

~~~
BigInteger sfid = SnowFlakId.next();
String humanReadableSFID = SnowFlakId.toHumanReadable(sfid);
~~~

