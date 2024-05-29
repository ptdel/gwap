# gwap

"The gwap service" is the backend for a financial simulation game a friend and I worked on for a period of time.  The service makes a securities "universe" of 2700+ ticker symbols, with each ticker being a random walk on it's initial "valuation".  Clients listen to individual ticker symbols that emit over a websocket on an open-close market cycle.  Maybe one day we'll see it through to the end.

Project created with [deps-new](https://github.com/seancorfield/deps-new) and the [practicalli/application template](https://github.com/practicalli/project-templates)

## License

Copyright © 2023 Patrick

[Creative Commons Attribution Share-Alike 4.0 International](http://creativecommons.org/licenses/by-sa/4.0/")
