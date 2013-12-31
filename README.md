# About

This is a prototype of a service to allow people to eat with people
nearby, steared by a map-view. It is designed to be integrated in the
flow of social-media apps as well as being used as an independant
web-site or in other contexts for different purposes (read: it is very
modular due to a functional design with Clojure and
[Pedestal](http://pedestal.io) in particular). The prototype is still
simple and not yet ready for any end-users.

WeFeedUs is the bootstrapping application of a
[general open-source and open-data community](https://github.com/functional-nomads)
around open web-development. We are trying to build a community around
open-source to finally open up data (after all, data is more important
than the code that runs on it) and allow to cooperatively compute on it
with distributed applications like this. The general purpose is to make
people literate in programming by allowing them to tap their own
data. While this service currently uses [Datomic](http://datomic.com) in
its free-as-in-beer version, work is done towards a free software,
distributed version control system for data on the web with
[geschichte](http://github.com/ghubber/geschichte). Due to the
"high-level", functional design of Datomic, it should be fairly easy to
replace it once the application data stabilizes.

Application data will be released under an open license and development
will be aligned around strong copyleft products. If you are interested,
ping me.

# wefeedus-client

The server side part can be found [here](http://github.com/ghubber/wefeedus-service).

Start working on this application by inspecting its behavior in the file
`app/src/wefeedus_client/behavior.clj`

## Usage

`cd` into any directory and execute the following:

```bash
lein repl
```

The `io.pedestal.app-tools.dev` namespace is loaded by default. It contains
several useful functions. To see a list of some of these functions, type:

```clj
(tools-help)
```

To begin working on an application, execute:

```clj
(start)
```

and then visit `http://localhost:3000`.

Alternatively, start the app server from the command line: `lein run`.

During development of an application, sources will be compiled
on-demand. Sources include everything located in the `app`
directory. All compiled output goes to `out/public`. The contents of
`out/public` are transient and the `out` directory can be deleted at
any time to trigger a complete re-build.

The contents of `out/public` are the deployment artifacts for this
project.

If you would like to serve the contents of `out/public` from another
server and not run the development server. Run:

```clj
(watch :development)
```

from the application project to automatically build the `:development`
environment when sources change.


## Links
* [Overview of how pedestal-app works](http://pedestal.io/documentation/application-overview/)
* [Comprehensive tutorial for pedestal-app](https://github.com/pedestal/app-tutorial)


## License

Copyright © 2013 Christian Weilbach

Distributed under the AGPL version 3.0.
