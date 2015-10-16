# pubnub-cljs

Idiomatic(?) ClojureScript wrapper for `cljsjs/pubnub` using `core.async`

## Why?

I just needed a clojurescript library for pubnub, and that's the proof of
concept. Current clients are either strictly clojure or just a simple wrapper
for pubnub.js. This is a wrapper for `cljsjs/pubnub` that uses channels.

## What's in the box

- Connecting to pubnub (wrapper around `PUBNUB({...})`)
- Joining pubnub channels (only single channels for now, channel groups are
  not supported). You still can join multiple channels, but separately, not
  as group. (wrapper around `pubnub.subscribe`)
- Leaving channels (wrapper around `pubnub.unsubscribe`)
- Presence API
- History API (currently only supports LIFO-order), because that's what
  I need.

## What's not in the box

- All other pubnub APIs (state, auth, etc.)
- Currently only "happy path" is handled. While the library is not likely
  to crash on message delivery errors etc. it may remain painfuly silent
  about them.

## Installation

In your `project.clj` add to dependencies:

    [pubnub-cljs "0.1.0-SNAPSHOT"]

## Usage

Whole public api is in `pubnub-cljs.core` namespace:

    (:require [cljs.core.async :refer [chan <! >!]]
              [pubnub-cljs.core :as pubnub]
              [promesa.core :as p])

Obtaining a configured pubnub instance is simple:

    (def pubnub-client
      (pubnub/pubnub :publish-key "<your pubnub publish key>"
                     :subscribe-key "<your pubnub subscribe key")

You can also pass any keyword argument as described in the docs:
http://www.pubnub.com/docs/web-javascript/api-reference#init
Just remember to use hyphens instead of underscores.

To subscribe to a pubnub channel you need ot call `pubnub/channel`.

    (def messages-tx (chan))
    (def messages-rx (chan))
    (def presence-rx (chan))

    (def pn (pubnub/channel pubnub-client "demoroom"
      :messages-rx messages-rx
      :messages-tx messages-tx
      :presence-rx presence-rx))

Once the room is joined you will be able to take messages from `messages-rx`
chan and put them into `messages-tx` chan. You can take presence events from
`presence-rx` chan.

`pubnub/channel` returns a promise (as in `promesa.core`) of a pubnub channel
(please do not confuse it with clojure's chans, it's just a protocol). Once
connected, the promise will resolve with an instance of `IPubNubChannel`.

You can check it's history:

    (p/then pn (fn [pubnub-channel]
      (let [history-channel (pubnub/history pubnub-channel)]
        (go-loop []
          (if-let [message (<! history-channel)]
            (do
              (println message)
              (recur))
            (pubnub/disconnect pubnub-channel)))))

`pubnub/history` returns a channel that historical messages can be taken from.
It will close, once all messages are taken.

You can also close the channel:

(p/then pn (fn [pubnub-channel]
  (pubnub/close pubnub-channel))

**`pubnub/close` will close all chan's you have passed to `pubnub/channel`.
I don't know if it's a good thing, maybe not (as they are created externaly),
maybe it should be callers responsibility to close them?**


## Running example & development

To get an interactive development environment run:

    lein figwheel

and open your browser at [localhost:3449](http://localhost:3449/).
This will auto compile and send all changes to the browser without the
need to reload. The browser contains.

To clean all compiled files:

    lein clean

To create a production build run:

    lein with-profile -dev jar

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
