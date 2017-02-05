# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## [0.2.0](https://github.com/boot-clj/boot-cljs-devtools/compare/0.1.2...0.2.0)

- _\*API breaking\*_ Task split: now there is one task for `cljs-devtools` and one for `dirac` (thank you @pepe)
- Throw if required depedencies are missing for the called task

## [0.1.2](https://github.com/boot-clj/boot-cljs-devtools/compare/0.1.1...0.1.2)

- Support for Dirac 0.7.x

## [0.1.1](https://github.com/boot-clj/boot-cljs-devtools/compare/0.1.0...0.1.1)

- Use the `:preloads` compiler options (ClojureScript >= `1.9.89` is required)
- Require `boot-cljs-devtools` before any other require
- Add `nrepl-opts` for configuring the spawned nRepl instance
- Add `dirac-opts` for configuring `dirac`

## [0.1.0](https://github.com/boot-clj/boot-cljs-devtools/compare/af649ed...0.1.0)
### Initial Release
- First version of the jupl/boot-cljs-devtools.
