Welcome
=============================================================================== 

Apache Camel ™ is a versatile open-source integration framework based on known Enterprise Integration Patterns.
    http://camel.apache.org/

Apache Commons Net™ library implements the client side of many basic Internet protocols.
    http://commons.apache.org/proper/commons-net/

Getting Started
=============================================================================== 

Apache Camel FTP endpoint (ftpext). Read data through a proxy.

FtpextComponent Options
   Name              Default value     Descriptions
ftpClient.proxyUrl       null          This proxy is used to consume/send messages from the target FTP host.

Proxy URL format
http://[user:password@]proxy.domain.lan:port/
ftp://[user:password@]proxy.domain.lan:port/
socks://[user:password@]proxy.domain.lan:port/
socks4://[user:password@]proxy.domain.lan:port/

Examples
  ftp://publicftpserver.com/download?ftpClient.proxyUrl=http://user:password@proxy.domain.lan:port/



Apache commons-net proxy componet 

Use proxy soket factory.

  String proxyUrl = "http://user:password@proxy.domain.lan/";
  SocketFactory socketFactory = new ProxySocketFactory(proxyUrl);
  FTPClient client = new FTPClient();

  client.setSocketFactory(socketFactory);
  client.connect(remotehost, remoteport);

