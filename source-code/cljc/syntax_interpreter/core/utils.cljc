
(ns syntax-interpreter.core.utils
    (:require [fruits.map.api    :as map]
              [fruits.regex.api  :as regex]
              [fruits.vector.api :as vector]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn with-options
  ; @description
  ; Applies the given options on the given tag.
  ;
  ; @param (vector) tag
  ; [(keyword) tag-name
  ;  (regex pattern) pattern / opening-pattern
  ;  (regex pattern)(opt) closing-pattern
  ;  (map)(opt) options]
  ; @param (map) options
  ;
  ; @usage
  ; (with-options [:my-tag #"..."]
  ;               {:pattern-limits {:match 5}})
  ; =>
  ; [:my-tag #"..." {:pattern-limits {:match 5}}]
  ;
  ; @usage
  ; (with-options [:my-tag #"..." #"..."]
  ;               {:pattern-limits {:match 5}})
  ; =>
  ; [:my-tag #"..." #"..." {:pattern-limits {:match 5}}]
  ;
  ; @usage
  ; (with-options [:my-tag #"..." #"..." {:pattern-limits {:lookahead 3}}]
  ;               {:pattern-limits {:match 5}})
  ; =>
  ; [:my-tag #"..." #"..." {:pattern-limits {:match 5 :lookahead 3}}]
  ;
  ; @return (vector)
  [tag options]
  (if (and (vector? tag) (map? options))
      (if (-> tag last map?)
          (-> tag (vector/update-last-item map/deep-merge options))
          (-> tag (vector/conj-item                       options)))))
