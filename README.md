# cljg

Result of playing with Clojure, ClojureScript and Om.

## Prerequisites

* [Bower](http://bower.io/)

* [Leiningen][1] 1.7.0 or above

[1]: https://github.com/technomancy/leiningen

## Running

Compile the assets

    bower install
    lein cljsbuild once

To start a web server for the application, run:

    lein ring server

OSX Quickstart

    brew install node leiningen
    bower install
    lein deps
    lein cljsbuild once
    lein ring server
