# juzraai's Proxy Manager [![Release](https://jitpack.io/v/juzraai/proxy-manager.svg)](https://jitpack.io/#juzraai/proxy-manager) [![Build status](https://travis-ci.org/juzraai/proxy-manager.svg)](https://travis-ci.org/juzraai/proxy-manager)

*Fetch, test &amp; use ip:port proxy lists* **[in development]**



## Features

* downloads IP:PORT HTTP proxy lists from **8 sites**
* stores proxy source information: crawler name (includes site name), date of first and last fetch
* stores all data in a single **SQLite database file**
* you can use it as a **stand-alone** tool
* you can use it as a **library**



## Crawled proxy lists

Site | Proxy count
-----|------------:
[freeproxylists.com](http://www.freeproxylists.com/anon.php) | 107
[gatherproxy.com](http://gatherproxy.com/proxylist/anonymity/?t=Elite) | 25
[hide-my-ip.com](https://www.hide-my-ip.com/proxylist.shtml) | *(limited to)* 100
[idcloak.com](http://www.idcloak.com/proxylist/free-proxy-ip-list.html) | ~ 90
[incloak.com](https://incloak.com/proxy-list/) | 64
[ip-adress.com](http://www.ip-adress.com/proxy_list/?k=time&d=desc) | 50
[proxy.moo.jp](http://proxy.moo.jp/?u=90) | ~ 290
[proxynova.com](http://www.proxynova.com/proxy-server-list/) | 35
**Total** | **~ 760**
**Unique** | **~ 710**

According to my tests there are an average of **10 new proxy comes in each minute**.



## Use as stand-alone tool

*(what it does)*

Accepted arguments:

* `--stdin` - reads IP:PORT list from standard input instead of sites listed above



## Use as library

First, add **Proxy Manager** as dependency from [JitPack.io](https://jitpack.io/#juzraai/toolbox). Follow the link to get information on how to do this, click on the green *"Get it"* button besides the latest version.

*(code samples)*