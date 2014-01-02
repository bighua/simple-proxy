#!/bin/bash
nohup ~/java/jdk1.7.0/bin/java -Xmx1024M \
-cp lib/*: com.jcm.proxy.ProxyServer \
main $@ -mcname ProxyServer -mcdesc ProxyServer\
> ns.out 2>&1 &
echo $! > pid
echo 'Start index proxy server ...' $!