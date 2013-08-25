vertx-mod-webjar
================

Allow to add and query webjar to your vertx.io module.

Configuration
-------------

<pre>
{
    "webjars":[<webjars>],
    
	"repos-file":<repos-files>, 
	"webjars-dir":<webjars-dir>,
	"pullin-on-startup":<pullin-on-startup>
}
<pre>

* `webjars` comma separated list of webjar maven coordinate, eg: `"org.webjars:angularjs:1.0.7", "org.webjars:bootstrap:3.0.0"`
* `repos-files` Path to the file containing maven repositories, default is vertx's `repos.txt`
* `webjars-dir` Path to the directory where to put webjar's jar, default is `webjars`
* `pullin-on-startup` Set to true if you want webjars to be populated at vertice startup, default is `false`

Usage
-----

```java
vertx.eventBus().send("module.webjar.resourceRequest", "webjars/angularjs/1.0.7/angular.js", new Handler<Message<String>>() {
    @Override public void handle(Message<String> event) {
        String content = event.body();
        // Do what you want with content...
    }
});
```

Remarks
-------

* Currently, all resources files from webjars are loaded in memory. 
* This is my first vertx module, any review would be greatly appreciated.
