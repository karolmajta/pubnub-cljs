(ns pubnub-cljs-example.macros)

(defmacro env [name] (System/getenv name))
