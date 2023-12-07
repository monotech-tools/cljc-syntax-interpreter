
(ns syntax-interpreter.interpreter.metafunctions
    (:require [fruits.string.api                    :as string]
              [fruits.vector.api                    :as vector]
              [syntax-interpreter.interpreter.utils :as interpreter.utils]))

;; -- Ancestor / parent tag metafunctions -------------------------------------
;; ----------------------------------------------------------------------------

(defn no-tags-opened-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'no-tags-opened?' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (boolean)
  [n tags options state]
  ; @description
  ; Returns TRUE if there is no opened tag at the actual cursor position.
  ;
  ; @return (boolean)
  (fn [] (interpreter.utils/no-tags-opened? n tags options state)))

(defn depth-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'depth' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (function)
  [n tags options state]
  ; @description
  ; Returns the depth of the actual cursor position.
  ;
  ; @return (integer)
  (fn [] (interpreter.utils/depth n tags options state)))

(defn tag-depth-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'tag-depth' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (function)
  [n tags options state]
  ; @description
  ; Returns the actual opened depth of a specific tag.
  ;
  ; @param (keyword) tag-name
  ;
  ; @return (integer)
  (fn [tag-name] (interpreter.utils/tag-depth n tags options state tag-name)))

(defn ancestor-tags-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'ancestor-tags' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (function)
  [n tags options state]
  ; @description
  ; Returns the ancestor tags of the actual cursor position.
  ;
  ; @param (keyword) tag-name
  ;
  ; @return (maps in vector)
  (fn [] (interpreter.utils/ancestor-tags n tags options state)))

(defn parent-tag-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'parent-tag' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (function)
  [n tags options state]
  ; @description
  ; Returns the parent tag name of the actual cursor position.
  ;
  ; @return (keyword)
  (fn [] (:name (interpreter.utils/parent-tag n tags options state))))

(defn tag-ancestor-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'tag-ancestor?' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (function)
  [n tags options state]
  ; @description
  ; Returns TRUE the given tag is an opened ancestor tag of the actual cursor position.
  ;
  ; @param (keyword) tag-name
  ;
  ; @return (boolean)
  (fn [tag-name] (interpreter.utils/tag-ancestor? n tags options state tag-name)))

(defn tag-parent-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'tag-parent?' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (function)
  [n tags options state]
  ; @description
  ; Returns TRUE if the given tag is the opened parent tag of the actual cursor position.
  ;
  ; @param (keyword) tag-name
  ;
  ; @return (boolean)
  (fn [tag-name] (interpreter.utils/tag-parent? n tags options state tag-name)))

(defn left-sibling-count-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'left-sibling-count' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (function)
  [n tags options state]
  ; @description
  ; Returns how many siblings have been already left behind by the interpreter within the actual parent tag.
  ;
  ; @return (integer)
  (fn [] (interpreter.utils/left-sibling-count n tags options state)))

;; -- Interpreter metafunctions -----------------------------------------------
;; ----------------------------------------------------------------------------

(defn interpreter-disabled-by-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'interpreter-disabled-by' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (function)
  [n tags options state]
  ; @description
  ; Returns the disabling tag's name if the interpreter is disabled by an opened tag.
  ;
  ; @return (keyword)
  (fn [] (interpreter.utils/interpreter-disabled-by n tags options state)))

(defn interpreter-disabled-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'interpreter-disabled?' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (function)
  [n tags options state]
  ; @description
  ; Returns TRUE if the interpreter is disabled by an opened tag.
  ;
  ; @return (boolean)
  (fn [] (interpreter.utils/interpreter-disabled? n tags options state)))

(defn interpreter-enabled-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'interpreter-enabled?' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (function)
  [n tags options state]
  ; @description
  ; Returns TRUE if the interpreter is NOT disabled by an opened tag.
  ;
  ; @return (boolean)
  (fn [] (interpreter.utils/interpreter-enabled? n tags options state)))

(defn reading-any-opening-match-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'reading-any-opening-match?' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (function)
  [n tags options state]
  ; @description
  ; Returns TRUE if any opening pattern's last found match is already started but not ended yet at the actual cursor position.
  ;
  ; @return (boolean)
  (fn [] (interpreter.utils/reading-any-opening-match? n tags options state)))

(defn reading-any-closing-match-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'reading-any-closing-match?' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (function)
  [n tags options state]
  ; @description
  ; Returns TRUE if any closing pattern's last found match is already started but not ended yet at the actual cursor position.
  ;
  ; @return (boolean)
  (fn [] (interpreter.utils/reading-any-closing-match? n tags options state)))

;; -- Operator metafunctions --------------------------------------------------
;; ----------------------------------------------------------------------------

(defn stop-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'stop' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (function)
  [_ _ _ _]
  ; @description
  ; Stops the interpreter immediatelly and the interpreter returns the parameter of this ('stop') function.
  ;
  ; @param (*) result
  ;
  ; @usage
  ; (stop "My output")
  ;
  ; @return (vector)
  ; [(keyword) stop-marker
  ;  (*) result]
  (fn [result] [:$stop result]))

(defn set-metadata-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'set-metadata' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (function)
  [_ _ _ _]
  ; @description
  ; Associates the given 'metadata' value into the 'state' map before the next iteration.
  ;
  ; @param (*) metadata
  ; @param (*) result
  ;
  ; @usage
  ; (set-metadata "This metadata will be available in the actual state from the next iteration."
  ;               "This is the result of the current iteration")
  ;
  ; @return (vector)
  ; [(keyword) set-metadata-marker
  ;  (*) metadata
  ;  (*) result]
  (fn [metadata result] [:$set-metadata metadata result]))

;; -- Tag boundary metafunctions ----------------------------------------------
;; ----------------------------------------------------------------------------

(defn starting-tag-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'starting-tag' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ;
  ; @return (function)
  [_ _ _ {:keys [actual-tags]}]
  ; @description
  ; Returns the tag's name that starts at the actual cursor position (if any).
  ;
  ; @return (keyword)
  (fn [] (-> actual-tags (vector/last-match :starts-at) :name)))

(defn opening-tag-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'opening-tag' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ;
  ; @return (function)
  [_ _ _ {:keys [actual-tags]}]
  ; @description
  ; Returns the tag's name that opens at the actual cursor position (if any).
  ;
  ; @return (keyword)
  (fn [] (-> actual-tags (vector/last-match :opens-at) :name)))

(defn closing-tag-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'closing-tag' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ;
  ; @return (function)
  [_ _ _ {:keys [actual-tags]}]
  ; @description
  ; Returns the tag's name that closes at the actual cursor position (if any).
  ;
  ; @return (keyword)
  (fn [] (-> actual-tags (vector/last-match :closes-at) :name)))

(defn ending-tag-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'ending-tag' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ;
  ; @return (function)
  [_ _ _ {:keys [actual-tags]}]
  ; @description
  ; Returns the tag's name that ends at the actual cursor position (if any).
  ;
  ; @return (keyword)
  (fn [] (-> actual-tags (vector/last-match :ends-at) :name)))

(defn tag-starts-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'tag-starts?' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ;
  ; @return (function)
  [_ _ _ {:keys [actual-tags]}]
  ; @description
  ; Returns TRUE if the given tag's opening pattern's match starts at the actual cursor position.
  ;
  ; @param (keyword) tag-name
  ;
  ; @return (boolean)
  (fn [tag-name] (letfn [(f0 [%] (and (-> % :name (= tag-name))
                                      (-> % :starts-at)))]
                        (vector/any-item-matches? actual-tags f0))))

(defn tag-started-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'tag-started?' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ;
  ; @return (function)
  [_ _ _ {:keys [actual-tags]}]
  ; @description
  ; Returns TRUE if the given tag is started and not ended yet at the actual cursor position.
  ;
  ; @param (keyword) tag-name
  ;
  ; @return (boolean)
  (fn [tag-name] (letfn [(f0 [%] (and (-> % :name (= tag-name))
                                      (or (-> % :starts-at)
                                          (-> % :started-at))))]
                        (vector/any-item-matches? actual-tags f0))))

(defn tag-started-at-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'tag-started-at' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ;
  ; @return (function)
  [_ _ _ {:keys [actual-tags]}]
  ; @description
  ; Returns the starting position of the given tag.
  ;
  ; @param (keyword) tag-name
  ;
  ; @return (integer)
  (fn [tag-name] (letfn [(f0 [%] (and (-> % :name (= tag-name))
                                      (or (-> % :starts-at)
                                          (-> % :started-at))))]
                        (some f0 actual-tags))))

(defn tag-opens-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'tag-opens?' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ;
  ; @return (function)
  [_ _ _ {:keys [actual-tags]}]
  ; @description
  ; Returns TRUE if the given tag's opening pattern's match ends at the actual cursor position.
  ;
  ; @param (keyword) tag-name
  ;
  ; @return (boolean)
  (fn [tag-name] (letfn [(f0 [%] (and (-> % :name (= tag-name))
                                      (-> % :opens-at)))]
                        (vector/any-item-matches? actual-tags f0))))

(defn tag-opened-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'tag-opened?' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)
  ;  :cursor (integer)}
  ;
  ; @return (function)
  [_ _ _ {:keys [actual-tags cursor]}]
  ; @description
  ; Returns TRUE if the given tag is opened and not closed at the actual cursor position.
  ;
  ; @param (keyword) tag-name
  ;
  ; @return (boolean)
  (fn [tag-name] (letfn [(f0 [%] (and (-> % :name (= tag-name))
                                      (or (-> % :opens-at)
                                          (-> % :opened-at))
                                      (-> % :closed-at not)))]
                        (vector/any-item-matches? actual-tags f0))))

(defn tag-opened-at-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'tag-opened-at' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ;
  ; @return (function)
  [_ _ _ {:keys [actual-tags]}]
  ; @description
  ; Returns the opening position of the given tag.
  ;
  ; @param (keyword) tag-name
  ;
  ; @return (integer)
  (fn [tag-name] (letfn [(f0 [%] (and (-> % :name (= tag-name))
                                      (or (-> % :opens-at)
                                          (-> % :opened-at))))]
                        (some f0 actual-tags))))

(defn tag-closes-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'tag-closes?' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ;
  ; @return (function)
  [_ _ _ {:keys [actual-tags]}]
  ; @description
  ; Returns TRUE if the given tag's closing pattern's match starts at the actual cursor position.
  ;
  ; @param (keyword) tag-name
  ;
  ; @return (boolean)
  (fn [tag-name] (letfn [(f0 [%] (and (-> % :name (= tag-name))
                                      (-> % :closes-at)))]
                        (vector/any-item-matches? actual-tags f0))))

(defn tag-closed-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'tag-closed?' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)
  ;  :cursor (integer)}
  ;
  ; @return (function)
  [_ _ _ {:keys [actual-tags cursor]}]
  ; @description
  ; Returns TRUE if the given tag is closed.
  ;
  ; @param (keyword) tag-name
  ;
  ; @return (boolean)
  (fn [tag-name] (letfn [(f0 [%] (and (-> % :name (= tag-name))
                                      (or (-> % :closes-at)
                                          (-> % :closed-at))))]
                        (vector/any-item-matches? actual-tags f0))))

(defn tag-closed-at-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'tag-closed-at' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ;
  ; @return (function)
  [_ _ _ {:keys [actual-tags]}]
  ; @description
  ; Returns the closing position of the given tag.
  ;
  ; @param (keyword) tag-name
  ;
  ; @return (integer)
  (fn [tag-name] (letfn [(f0 [%] (and (-> % :name (= tag-name))
                                      (or (-> % :closes-at)
                                          (-> % :closed-at))))]
                        (some f0 actual-tags))))

(defn tag-ends-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'tag-ends?' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ;
  ; @return (function)
  [_ _ _ {:keys [actual-tags]}]
  ; @description
  ; Returns TRUE if the given tag's closing pattern's match ends at the actual cursor position.
  ;
  ; @param (keyword) tag-name
  ;
  ; @return (boolean)
  (fn [tag-name] (letfn [(f0 [%] (and (-> % :name (= tag-name))
                                      (-> % :ends-at)))]
                        (vector/any-item-matches? actual-tags f0))))

;; -- Tag body / content metafunctions ----------------------------------------
;; ----------------------------------------------------------------------------

(defn tag-body-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'tag-body' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:cursor (integer)}
  ;
  ; @return (function)
  [n tags options {:keys [cursor] :as state}]
  ; @description
  ; Returns a specific part of the 'n' string from the starting position of the given tag to the actual cursor position.
  ;
  ; @param (keyword) tag-name
  ;
  ; @return (string)
  (fn [tag-name] (if-let [tag-started-at ((tag-started-at-f n tags options state) tag-name)]
                         (string/keep-range n tag-started-at cursor))))

(defn tag-content-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'tag-content' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:cursor (integer)}
  ;
  ; @return (function)
  [n tags options {:keys [cursor] :as state}]
  ; @description
  ; Returns a specific part of the 'n' string from the opening position of the given tag to the actual cursor position.
  ;
  ; @param (keyword) tag-name
  ;
  ; @return (string)
  (fn [tag-name] (if-let [tag-opened-at ((tag-opened-at-f n tags options state) tag-name)]
                         (string/keep-range n tag-opened-at cursor))))

;; -- Tag history metafunctions -----------------------------------------------
;; ----------------------------------------------------------------------------

(defn tag-left-count-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'tag-left-count' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:left-tags (map)}
  ;
  ; @return (function)
  [_ _ _ {:keys [left-tags]}]
  ; @description
  ; Returns how many occurences of a specific tag has been ended and left behind by the interpreter until the actual cursor position.
  ;
  ; @param (keyword) tag-name
  ;
  ; @return (integer)
  (fn [tag-name] (-> left-tags tag-name count)))

(defn tag-met-count-f
  ; @ignore
  ;
  ; @description
  ; Returns the 'tag-met-count' metafunction.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)
  ;  :left-tags (map)}
  ;
  ; @return (function)
  [_ _ _ {:keys [actual-tags left-tags]}]
  ; @description
  ; Returns how many occurences of a specific tag has been reached by the interpreter until the actual cursor position.
  ;
  ; @param (keyword) tag-name
  ;
  ; @return (integer)
  (fn [tag-name] (letfn [(f0 [%] (-> % :name (= tag-name)))]
                        (+ (vector/match-count actual-tags f0)
                           (-> left-tags tag-name count)))))
