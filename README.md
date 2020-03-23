# CraftTunnel

CraftTunnel is a layer 7 proxy to be used over BungeeCord or any minecraft server that (preferably) supports HAPRoxyProtocol.

CraftTunnel operates on two principal modes:

## PROXY_ONLY

On this mode, a ProxyServer instance will be created and forward any receiving connections to a configured remote server.

```
                                              
                external TCP                    external or internal
+----------+     connection     +-------------+    TCP connection  +----------+
|          |                    |             |                    |          |
|  Client  |      ------->      | ProxyServer |      ------->      |  Server  |
|          |      <-------      |             |      <-------      |          |
+----------+                    +-------------+                    +----------+

```

## MULTI_PROXY_TUNNEL

```
############           ######################
## CLIENT ##   --- >   ## PROXY ENTRYPOINT ##
##        ##   < --    ##                  ## 
############           ######################

                              /\   ||   
                              ||   ||    tunneled connection
                              ||   ||    initiated from proxy exit point
                              ||   \/
                              
                       ######################           ############
                       ## PROXY EXITPOINT  ##   --- >   ## SERVER ##
                       ##                  ##    < ---  ##        ##
                       ######################           ############
                              
```
