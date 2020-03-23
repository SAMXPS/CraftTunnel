# CraftTunnel

CraftTunnel is a layer 7 proxy to be used over BungeeCord or any minecraft server that (preferably) supports HAPRoxyProtocol.

## Simple Proxy and Tunneled Proxy

CraftTunnel operates on two principal modes:

### Simple Proxy / PROXY_ONLY

On proxy only mode, a ProxyServer instance will be created to forward any receiving connections to a configured remote server.


```                                             
                          external TCP                     external or internal
        +------------+     connection     +-------------+     TCP connection     +------------+
        |            |                    |             |                        |            |
        |   Client   |      ------->      | ProxyServer |        ------->        |   Server   |
        |            |      <-------      |             |        <-------        |            |
        +------------+                    +-------------+                        +------------+
        
```

### Tunnel Proxy / MULTI_PROXY_TUNNEL

On multi proxy tunnel, a ProxyEntryPoint (or Master ProxyServer) instance will be bound to a public port on a public server. Then, one or more ProxyExitPoint instances must connect to the master server. Once at least one ExitPoint is connected to the EntryPoint, the latter will forward incoming client connections to the former, and the ExitPoint will be responsible for connecting to configured remote servers and linking data with the client trough the tunnel.

If more than one ExitPoints are connected to the EntryPoint, the EntryPointHandler will choose, once a client tries to connect, the ExitPoint with fewer client active connections to handle the new connection. On this way, a simple LoadBalancing can be made using multiple ExitPoints connected to the EntryPoint.

```

     +------------+                    +-------------------+
     |            |                    |                   |
     |   Client   |      ------->      |  ProxyEntryPoint  |
     |            |      <-------      |                   |
     +------------+                    +-------------------+
     
                                               /\   ||   
                                               ||   ||    tunneled connection
                                               ||   ||    initiated from proxy exit point
                                               ||   \/
                                  
                                        +------------------+                 +------------+
                                        |                  |                 |            |
                                        |  ProxyExitPoint  |     ------->    |   Server   |
                                        |                  |     <-------    |            |
                                        +------------------+                 +------------+
                              
```
