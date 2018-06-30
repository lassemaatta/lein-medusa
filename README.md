# lein-medusa

[![Clojars Project](https://img.shields.io/clojars/v/org.clojars.lassemaatta/lein-medusa.svg)](https://clojars.org/org.clojars.lassemaatta/lein-medusa)

A simple Leiningen plugin which parses and visualizes your [re-frame](https://github.com/Day8/re-frame) project.

At the moment supports graphing the subscription hierarchy into the [.dot](https://www.graphviz.org/) -format.

Supported output formats (either to file or stdout):
  * EDN map
  * plain [.dot](https://www.graphviz.org/)
  * [PlantUML](http://plantuml.com/)

## Usage

Use this for user-level plugins:

Put `[lein-medusa "0.1.1"]` into the `:plugins` vector of your `:user`
profile.

Use this for project-level plugins:

Put `[lein-medusa "0.1.1"]` into the `:plugins` vector of your project.clj.

Example usage (print result as a map to stdout):

    $ lein medusa

Example usage (write result in plantuml format to file):

    $ lein medusa --graph --depth 4 --plantuml --output my_graph.plantuml

## License

Copyright © 2018

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
