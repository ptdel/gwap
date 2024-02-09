(ns gwap.user
  (:require [integrant.core :as ig]
            [integrant.repl :refer [go halt]]
            [gwap.system :refer [universe]]
            [clojure.pprint :as pp]))

(integrant.repl/set-prep! #(ig/prep universe))

(defn pretty-spit
  [file-name collection]
  (spit (java.io.File. file-name)
        (with-out-str (pp/write collection :dispatch pp/code-dispatch))))

(comment
  pretty-spit
  (go)
  (halt))
