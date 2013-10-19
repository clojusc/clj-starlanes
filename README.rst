##################
Clojure Star-Lanes
##################

*A Nostalgic Implementation*

.. image:: resources/screeshots/hy-early-stage-game-400.png


Background
==========

*Star Trader* was a BASIC game written by Dave Kaufman and published by the
"People's Computer Company" in 1974. It seems to have been written in 1973,
however.

Over the years, it was copied and then heavily modified, morphing into what
became *Star Lanes* by the late 70s and early 80s. (Note that some *Star Lanes*
versions of the game still used the ``TRADE.BAS`` filename, while others
switched to ``LANES.BAS``.)

The *Star Trader* lineage of games inspired a list of excellent games; the
`Wikipedia article`_ even claims that EVE Online traces its ancestry to
*Star Trader*. There is also a page that provides a `timeline`_ of
*Star Trader*, should you want to explore this further.

The differences of the original two variantes of the game are briefly
outlined below.


Star Trader
-----------

.. image:: resources/screeshots/BASIC-star-trader-map-400.png

In *Star Trader*, players travel about the star map buying and selling six types
of merchandise: uranium, metals, gems, software, heavy equipment, and medicine.


Star Lanes
-----------

.. image:: resources/screeshots/BASIC-star-lanes-map-400.png

In *Star Lanes*, players take turns building outposts, creating companies, and
purchasing stock.


Clojure Implementation
======================

This Clojure implementation follows the *Star Lanes* path, since that's the one
that I played as a kid on a CP\M Kaypro II back in '81. It does, however,
provide for a more diverse game play, with an extended map and greater number
of companies (both configurable).


Usage
-----

Instructions for play are provided in-game (copying the text of the original
almost word-for-word).

The easiest way to play the game is simply this:

.. code:: bash

    $ git clone https://github.com/oubiwann/clj-starlanes.git
    $ cd clj-starlanes
    $ make run

You will need to have Java, Clojure, and `Leiningen`_ installed. `lein` will
need to be in your `$PATH`. Also, note that doing `make run` for the first
time will download all the other dependencies automatically, so you will see
lots of text scrolling in your terminal while it does so.

Enjoy!


.. Links
.. -----
.. _Wikipedia article: http://en.wikipedia.org/wiki/Star_Trader
.. _timeline: http://wiki.classictw.com/index.php?title=Inside_TradeWars_-_History_-_Timeline
.. _Hy: hy/README.rst
.. _LFE: lfe/README.rst
.. _Clojure: clojure/README.rst
.. _Racket: racket/README.rst
.. _Leiningen: https://github.com/technomancy/leiningen
