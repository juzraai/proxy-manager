# juzraai's Proxy Manager [![Release](https://jitpack.io/v/juzraai/proxy-manager.svg)](https://jitpack.io/#juzraai/proxy-manager) [![Build status](https://travis-ci.org/juzraai/proxy-manager.svg)](https://travis-ci.org/juzraai/proxy-manager)

*Fetch, test &amp; use ip:port proxy lists* **[in development]**



## Features



* can download IP:PORT HTTP proxy lists from **8 sites**
* can accept proxy list from standard input
* stores proxy source information: crawler name (includes site name), date of first and last fetch
* tests proxies and determines if they are working and whether they are anonymous
* stores all data in a single **SQLite database file**
* you can use it as a **stand-alone** tool
* you can use it as a **library**



## Proxy lists it can crawl



The following table shows **average** statistics:

Site | Proxy count | Working count | Anon count
-----|------------:|--------------:|-----------:
[freeproxylists.com](http://www.freeproxylists.com/anon.php)              | ~ 115 | ~ 65 | ~ 50
[gatherproxy.com](http://gatherproxy.com/proxylist/anonymity/?t=Elite)    |    25 |   14 |   14
[hide-my-ip.com](https://www.hide-my-ip.com/proxylist.shtml) | *(limited to)* 100 |   18 |    9
[idcloak.com](http://www.idcloak.com/proxylist/free-proxy-ip-list.html)   |  ~ 90 | ~ 59 | ~ 14
[incloak.com](https://incloak.com/proxy-list/)                            |    64 |    5 |    2
[ip-adress.com](http://www.ip-adress.com/proxy_list/?k=time&d=desc)       |    50 |   37 |   18
[proxy.moo.jp](http://proxy.moo.jp/?u=90)                                 | ~ 260 | ~ 70 | ~ 35
[proxynova.com](http://www.proxynova.com/proxy-server-list/)              |    35 |   17 |    8
**Total**  | **~ 740** | **~ 285** | **~ 150**
**Unique** | **~ 695** | **~ 250** | **~ 135**

Testing ~700 proxy with 10 worker threads can take up to 20 minutes. (Maybe if we decrease retries from 2 to 1, we can make it faster.)

According to my tests there are an average of **10 new proxy comes in each minute**.



## Use as stand-alone tool



**Syntax:**

`$ java -jar proxy-manager-VERSION.jar [global options] [command] [command options]`



**Global options:**

Short            | Long                    | Description
-----------------|-------------------------|------------
`-db <filename>` | `--database <filename>` | Set SQLite database filename. Default is `proxies.db`.



 **Commands:**

 Name   | Description
 -------|------------
 `get`  | Prints working proxies to standard output
 `stat` | Prints statistics of proxy lists to standard output



 **Options of `get`:**

 Short            | Long                    | Description
 -----------------|-------------------------|------------
`-i <mode>`       | `--input <mode>`        | **Required.** Selects input mode. Possible values: `stdin`, `crawl`, `db`.
`-t <mode>`       | `--test <mode>`         | Selects test mode. Possible values are: `none`, `auto`, `all`. Default is `auto`.
`-a`              | `--anon`                | Prints out only anonymous proxies.
`-w`              | `--workers`             | Number of worker threads to download or test proxies. Default is `10`.



## Use as library



**Dependency**

You can add **Proxy Manager** as dependency using [JitPack.io](https://jitpack.io/#juzraai/toolbox). Follow the link to get information on how to do this, click on the green *"Get it"* button besides the latest version and scroll down to code snippets.

*(TODO code samples)*



## Contribute



You can contribute to this project by:

* adding fresh and refreshing proxy lists to [#1](https://github.com/juzraai/proxy-manager/issues/1) as comment
* adding proxy list downloaders which extend [ProxyListDownloaderTask](https://github.com/juzraai/proxy-manager/blob/master/src/main/java/hu/juzraai/proxymanager/fetch/ProxyListDownloaderTask.java) via pull request
* providing feedback about bugs or ideas by creating new [issues](https://github.com/juzraai/proxy-manager/issues/)
