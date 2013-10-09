Welcome
=============================================================================== 

Apache Camel ™ is a versatile open-source integration framework based on known Enterprise Integration Patterns.
    http://camel.apache.org/

Apache Commons Net™ library implements the client side of many basic Internet protocols.
    http://commons.apache.org/proper/commons-net/

Getting Started
=============================================================================== 

Apache Camel FTP endpoint (ftpext). Read data through a proxy.

Old camel component ftp sintax url without proxy:
  from("ftp://{{remoteurl}}/")
  .log("read file from ftp server");

New camel compoent ftpext sintax with proxy:
  // PropertiesComponent have property
  // proxyUrl=http://user:password@proxy.domain.lan/
  // or
  // proxyUrl=ftp://user:password@proxy.domain.lan/
  // or
  // proxyUrl=socks://user:password@proxy.domain.lan/
  // or
  // proxyUrl=socks4://user:password@proxy.domain.lan/
  
  from("ftpext://{{remoteurl}}/?ftpClient.proxyUrl={{proxyUrl}}")
  .log("read file from ftp server ");



Apache commons-net proxy componet 

Use proxy soket factory.

  String proxyUrl = "http://user:password@proxy.domain.lan/";
  SocketFactory socketFactory = new ProxySocketFactory(proxyUrl);
  FTPClient client = new FTPClient();

  client.setSocketFactory(socketFactory);
  client.connect(remotehost, remoteport);

