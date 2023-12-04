
(ns syntax-interpreter.interpreter.utils
    (:require [map.api     :as map :refer [update-by]]
              [string.api  :as string]
              [regex.api   :as regex]
              [seqable.api :as seqable :refer [last-dex]]
              [syntax-interpreter.core.config :as core.config]
              [vector.api  :as vector]))

;; -- Tag parameter functions -------------------------------------------------
;; ----------------------------------------------------------------------------

(defn tag-opening-pattern
  ; @ignore
  ;
  ; @description
  ; Returns the given tag's opening pattern.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (keyword) tag-name
  ;
  ; @return (regex pattern)
  [_ tags _ _ tag-name]
  (if-let [opening-pattern (-> tags tag-name first)]
          (if (regex/pattern? opening-pattern) opening-pattern)))

(defn tag-closing-pattern
  ; @ignore
  ;
  ; @description
  ; Returns the given tag's closing pattern (if provided).
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (keyword) tag-name
  ;
  ; @return (regex pattern)
  [_ tags _ _ tag-name]
  (if-let [closing-pattern (-> tags tag-name second)]
          (if (regex/pattern? closing-pattern) closing-pattern)))

(defn tag-options
  ; @ignore
  ;
  ; @description
  ; Returns the given tag's options map (if provided).
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (keyword) tag-name
  ;
  ; @return (map)
  [_ tags _ _ tag-name]
  (if-let [tag-options (-> tags tag-name last)]
          (if (map? tag-options) tag-options)))

(defn tag-omittag?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the given tag doesn't have a closing pattern (omittag).
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (keyword) tag-name
  ;
  ; @return (map)
  [n tags options state tag-name]
  (-> (tag-closing-pattern n tags options state tag-name) nil?))

;; -- Ancestor / parent tag functions -----------------------------------------
;; ----------------------------------------------------------------------------

(defn no-tags-opened?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if there is no opened tag at the actual cursor position.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ;
  ; @return (boolean)
  [_ _ _ state]
  (letfn [(f0 [{:keys [closed-at opened-at opens-at]}]
              ; Tags that are either already closed or not opened yet:
              (or closed-at (not (or opens-at opened-at))))]
         (vector/all-items-match? (:actual-tags @state) f0)))

(defn depth
  ; @ignore
  ;
  ; @description
  ; Returns the depth of the actual cursor position.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ;
  ; @return (integer)
  [_ _ _ state]
  (letfn [(f0 [{:keys [closed-at opened-at opens-at]}]
              ; Tags that are already opened and aren't closed yet:
              (and (or opens-at opened-at) (not closed-at)))]
         (vector/match-count (:actual-tags @state) f0)))

(defn tag-depth
  ; @ignore
  ;
  ; @description
  ; Returns the actual opened depth of a specific tag.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (keyword) tag-name
  ;
  ; @return (integer)
  [_ _ _ state tag-name]
  (letfn [(f0 [{:keys [closed-at name opened-at opens-at]}]
              ; Tags with a specific tag name that are already opened and aren't closed yet:
              (and (= name tag-name) (or opens-at opened-at) (not closed-at)))]
         (vector/match-count (:actual-tags @state) f0)))

(defn ancestor-tags
  ; @ignore
  ;
  ; @description
  ; Returns the ancestor tags of the actual cursor position.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ;
  ; @return (maps in vector)
  [_ _ _ state]
  (letfn [(f0 [{:keys [opened-at opens-at closed-at]}]
              ; Tags that are already opened and aren't closed yet:
              (and (or opens-at opened-at) (not closed-at)))]
         (vector/keep-items-by (:actual-tags @state) f0)))

(defn parent-tag
  ; @ignore
  ;
  ; @description
  ; Returns the parent tag of the actual cursor position.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ;
  ; @return (map)
  [_ _ _ state]
  (letfn [(f0 [{:keys [opened-at opens-at closed-at]}]
              ; Tags that are already opened and aren't closed yet:
              (and (or opens-at opened-at) (not closed-at)))]
         (vector/last-match (:actual-tags @state) f0)))

(defn tag-ancestor?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE the given tag is an opened ancestor tag of the actual cursor position.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (keyword) tag-name
  ;
  ; @return (integer)
  [n tags options state tag-name]
  (< 0 (tag-depth n tags options state tag-name)))

(defn tag-parent?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the given tag is the opened parent tag of the actual cursor position.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (keyword) tag-name
  ;
  ; @return (boolean)
  [n tags options state tag-name]
  (if-let [parent-tag (parent-tag n tags options state)]
          (-> parent-tag :name (= tag-name))))

(defn left-sibling-count
  ; @ignore
  ;
  ; @description
  ; Returns how many siblings have been already left behind by the interpreter within the actual parent tag.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ;
  ; @return (integer)
  [n tags options state]
  (letfn [(f0 [a b] (< (:started-at a) (or (:opened-at b) (:opens-at b))))     ; <- The parent tag (as 'b') can be actually opening at the actual cursor position!
          (f1 [a b] (and (:opened-at b) (>= (:started-at a) (:opened-at b))))] ; <- The potential ascendant tag (as 'b') can be an omittag without an opening position!
         (if-let [parent-tag (parent-tag n tags options state)]
                 (loop [dex 0 left-siblings []]
                       (if ; - Iterates over the 'left-tags' vector, and counts how many left tags are direct children (not descendants) of the actual parent tag.
                           ; - When the iteration is over it returns the count of the already left children within the actual parent tag.
                           (seqable/dex-out-of-bounds? (:left-tags @state) dex) (count left-siblings)
                           ; - If the observed 'left-tag' started before the parent tag opened, it means that the observed tag is not a child or even a descendant of the parent tag.
                           ; - If any other tag has been already collected into the 'left-siblings' vector during the previous iterations, it can be a potential ascendant of the observed 'left-tag'.
                           ; - If the observed 'left-tag' has ascendant(s) within the parent tag, it means that it is a descendant but not a child of the parent tag.
                           ; - If the last collected tag in the 'left-siblings' vector is an omittag, it means it cannot be an ascendant of the observed 'left-tag'.
                           ; - If a tag is currently ending at the actual cursor position, it can be a potential ascendant of any tags in the 'left-tags' vector
                           ;   and because it is not moved into the 'left-tags' vector from the 'actual-tags' vector yet, it has to be checked separatelly.
                           (let [left-tag   (vector/nth-item   (:left-tags   @state) dex)
                                 ending-tag (vector/last-match (:actual-tags @state) :ends-at)]
                                (cond (f0 left-tag parent-tag)           (recur (inc dex) (->   left-siblings))
                                      (f1 left-tag ending-tag)           (recur (inc dex) (->   left-siblings))
                                      (-> left-siblings last nil?)       (recur (inc dex) (conj left-siblings left-tag))
                                      (f1 left-tag (last left-siblings)) (recur (inc dex) (->   left-siblings))
                                      :else                              (recur (inc dex) (conj left-siblings left-tag)))))))))

;; -- Iteration functions -----------------------------------------------------
;; ----------------------------------------------------------------------------

(defn offset-reached?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the actual cursor position reached the given 'offset' position.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; {:offset (integer)(opt)}
  ; @param (atom) state
  ;
  ; @return (keyword)
  [n _ {:keys [offset] :or {offset 0}} state]
  (let [offset (seqable/normalize-cursor n offset)]
       (>= (:cursor @state) offset)))

(defn endpoint-reached?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the actual cursor position reached the given 'endpoint' position.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; {:endpoint (integer)(opt)}
  ; @param (atom) state
  ;
  ; @return (keyword)
  [n _ {:keys [endpoint] :or {endpoint (count n)}} state]
  (let [endpoint (seqable/normalize-cursor n endpoint)]
       (>= (:cursor @state) endpoint)))

(defn iteration-ended?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the actual cursor position reached the last cursor position in the given 'n' string.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ;
  ; @return (boolean)
  [n _ _ state]
  (seqable/cursor-last? n (:cursor @state)))

(defn iteration-stopped?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the 'stop' metafunction stopped the iteration.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ;
  ; @return (boolean)
  [_ _ _ state]
  (= (:cursor @state) :iteration-stopped))

;; -- Interpreter functions ---------------------------------------------------
;; ----------------------------------------------------------------------------

(defn interpreter-disabled-by
  ; @ignore
  ;
  ; @description
  ; Returns the disabling tag's name if the interpreter is disabled by an opened tag.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ;
  ; @return (keyword)
  [n tags options state]
  (if-let [parent-tag (parent-tag n tags options state)]
          (if-let [tag-options (tag-options n tags options state (:name parent-tag))]
                  (if (:disable-interpreter? tag-options)
                      (:name parent-tag)))))

(defn interpreter-disabled?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the interpreter is disabled by an opened tag.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ;
  ; @return (boolean)
  [n tags options state]
  (-> (interpreter-disabled-by n tags options state) some?))

(defn interpreter-enabled?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the interpreter is NOT disabled by an opened tag.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ;
  ; @return (boolean)
  [n tags options state]
  (-> (interpreter-disabled-by n tags options state) not))

(defn reading-any-opening-match?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if any opening pattern's last found match is already started but not ended yet at the actual cursor position.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ;
  ; @return (boolean)
  [_ _ _ state]
  (-> @state :actual-tags last :will-open-at ))

(defn reading-any-closing-match?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if any closing pattern's last found match is already started but not ended yet at the actual cursor position.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ;
  ; @return (boolean)
  [_ _ _ state]
  (-> @state :actual-tags last :will-end-at))


;; -- Tag processing requirement functions ------------------------------------
;; ----------------------------------------------------------------------------

(defn tag-requires-no-ancestors?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the given tag requires no ancestor tags.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (keyword) tag-name
  ;
  ; @return (boolean)
  [n tags options state tag-name]
  (if-let [tag-options (tag-options n tags options state tag-name)]
          (-> tag-options :accepted-ancestors (= []))))

(defn tag-requires-no-parents?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the given tag requires no parent tags.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (keyword) tag-name
  ;
  ; @return (boolean)
  [n tags options state tag-name]
  (if-let [tag-options (tag-options n tags options state tag-name)]
          (-> tag-options :accepted-parents (= []))))

(defn tag-requires-accepted-ancestor?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the given tag requires any accepted ancestor tags.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (keyword) tag-name
  ;
  ; @return (boolean)
  [n tags options state tag-name]
  (if-let [tag-options (tag-options n tags options state tag-name)]
          (-> tag-options :accepted-ancestors vector/nonempty?)))

(defn tag-requires-accepted-parent?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the given tag requires any accepted ancestor tags.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (keyword) tag-name
  ;
  ; @return (boolean)
  [n tags options state tag-name]
  (if-let [tag-options (tag-options n tags options state tag-name)]
          (-> tag-options :accepted-parents vector/nonempty?)))

(defn tag-any-accepted-ancestor-opened?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if at least one of the accepted ancestor tags of the given tag is opened.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (keyword) tag-name
  ;
  ; @return (boolean)
  [n tags options state tag-name]
  (if-let [tag-options (tag-options n tags options state tag-name)]
          (if-let [accepted-ancestors (:accepted-ancestors tag-options)]
                  (letfn [(f0 [accepted-ancestor] (tag-ancestor? n tags options state accepted-ancestor))]
                         (some f0 accepted-ancestors)))))

(defn tag-any-accepted-parent-opened?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if at least one of the accepted parent tags of the given tag is opened (as the the actual parent tag).
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (keyword) tag-name
  ;
  ; @return (boolean)
  [n tags options state tag-name]
  (if-let [tag-options (tag-options n tags options state tag-name)]
          (if-let [accepted-parents (:accepted-parents tag-options)]
                  (letfn [(f0 [accepted-parent] (tag-parent? n tags options state accepted-parent))]
                         (some f0 accepted-parents)))))

(defn tag-ancestor-requirements-met?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the actual cursor position meets the given tag's ancestor requirements.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (keyword) tag-name
  ;
  ; @return (boolean)
  [n tags options state tag-name]
  (and (or (-> (tag-requires-no-ancestors?        n tags options state tag-name) not)
           (-> (no-tags-opened?                   n tags options state)))
       (or (-> (tag-requires-accepted-ancestor?   n tags options state tag-name) not)
           (-> (tag-any-accepted-ancestor-opened? n tags options state tag-name)))))

(defn tag-parent-requirements-met?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the actual cursor position meets the given tag's parent requirements.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (keyword) tag-name
  ;
  ; @return (boolean)
  [n tags options state tag-name]
  (and (or (-> (tag-requires-no-parents?        n tags options state tag-name) not)
           (-> (no-tags-opened?                 n tags options state)))
       (or (-> (tag-requires-accepted-parent?   n tags options state tag-name) not)
           (-> (tag-any-accepted-parent-opened? n tags options state tag-name)))))

;; -- Regex functions ---------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn opening-match
  ; @ignore
  ;
  ; @description
  ; Returns the the tag name and the found match if any opening pattern's match starts at the actual cursor position.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (keyword) tag-name
  ;
  ; @return (map)
  ; {:match (integer)
  ;  :name (keyword)}
  [n tags options state tag-name]
  ; Merging regex actions into one function decreases the interpreter processing time.
  (if-let [opening-pattern (tag-opening-pattern n tags options state tag-name)]
          (let [tag-options           (tag-options n tags options state tag-name)
                max-lookbehind-length (or (get-in tag-options                     [:pattern-limits :opening/lookbehind])
                                          (get-in tag-options                     [:pattern-limits :lookbehind])
                                          (get-in core.config/DEFAULT-TAG-OPTIONS [:pattern-limits :lookbehind]))
                max-lookahead-length  (or (get-in tag-options                     [:pattern-limits :opening/lookahead])
                                          (get-in tag-options                     [:pattern-limits :lookahead])
                                          (get-in core.config/DEFAULT-TAG-OPTIONS [:pattern-limits :lookahead]))
                max-match-length      (or (get-in tag-options                     [:pattern-limits :opening/match])
                                          (get-in tag-options                     [:pattern-limits :match])
                                          (get-in core.config/DEFAULT-TAG-OPTIONS [:pattern-limits :match]))
                corrected-cursor      (min              (:cursor @state) max-lookbehind-length)
                observed-from         (max (->    0) (- (:cursor @state) max-lookbehind-length))
                observed-to           (min (count n) (+ (:cursor @state) max-match-length max-lookahead-length))
                observed-part         (subs n observed-from observed-to)]
               (if-let [opening-match (regex/re-from observed-part opening-pattern corrected-cursor)]
                       {:name tag-name :match opening-match}))))

(defn closing-match
  ; @ignore
  ;
  ; @description
  ; Returns the the tag name and the found match if any closing pattern's match starts at the actual cursor position.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (keyword) tag-name
  ;
  ; @return (map)
  ; {:match (integer)
  ;  :name (keyword)}
  [n tags options state tag-name]
  ; Merging regex actions into one function decreases the interpreter processing time.
  (if-let [closing-pattern (tag-closing-pattern n tags options state tag-name)]
          (let [tag-options           (tag-options n tags options state tag-name)
                max-lookbehind-length (or (get-in tag-options                     [:pattern-limits :closing/lookbehind])
                                          (get-in tag-options                     [:pattern-limits :lookbehind])
                                          (get-in core.config/DEFAULT-TAG-OPTIONS [:pattern-limits :lookbehind]))
                max-lookahead-length  (or (get-in tag-options                     [:pattern-limits :closing/lookahead])
                                          (get-in tag-options                     [:pattern-limits :lookahead])
                                          (get-in core.config/DEFAULT-TAG-OPTIONS [:pattern-limits :lookahead]))
                max-match-length      (or (get-in tag-options                     [:pattern-limits :closing/match])
                                          (get-in tag-options                     [:pattern-limits :match])
                                          (get-in core.config/DEFAULT-TAG-OPTIONS [:pattern-limits :match]))
                corrected-cursor      (min              (:cursor @state) max-lookbehind-length)
                observed-from         (max (->    0) (- (:cursor @state) max-lookbehind-length))
                observed-to           (min (count n) (+ (:cursor @state) max-match-length max-lookahead-length))
                observed-part         (subs n observed-from observed-to)]
               (if-let [closing-match (regex/re-from observed-part closing-pattern corrected-cursor)]
                       {:name tag-name :match closing-match}))))

;; -- Update child / parent tag functions -------------------------------------
;; ----------------------------------------------------------------------------

(defn start-child-tag
  ; @ignore
  ;
  ; @description
  ; Updates the given 'state' by adding a new depth for the given tag to the 'actual-tags' vector.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (map) opening-match
  ; {:match (string)
  ;  :name (keyword)}
  ;
  ; @example
  ; (start-child-tag "..." {...} {...}
  ;                  {:cursor 7 :actual-tags [{:name :paren :started-at 1 :opened-at 2}
  ;                                           {:name :paren :started-at 4 :opened-at 5}]}
  ;                  {:name :paren :match "("})
  ; =>
  ; {:cursor 7 :actual-tags [{:name :paren :started-at 1 :opened-at 2}
  ;                          {:name :paren :started-at 4 :opened-at 5}
  ;                          {:name :paren :starts-at  7 :will-open-at 8}]}
  ;
  ; @return (map)
  [n tags options state {:keys [match name]}]
  (letfn [(f [{:keys [closed-at opened-at opens-at]}]
             ; Tags that are already opened and aren't closed yet:
             (and (or opens-at opened-at) (not closed-at)))]
         (if (tag-omittag? n tags options state name)
             (swap! state update :actual-tags vector/conj-item {:name name :starts-at (:cursor @state) :will-end-at  (+ (:cursor @state) (count match))})
             (swap! state update :actual-tags vector/conj-item {:name name :starts-at (:cursor @state) :will-open-at (+ (:cursor @state) (count match))}))))

(defn close-parent-tag
  ; @ignore
  ;
  ; @description
  ; Updates the given 'state' by closing the actual parent tag in the 'actual-tags' vector.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (map) closing-match
  ; {:match (string)
  ;  :name (keyword)}
  ;
  ; @example
  ; (close-parent-tag "..." {...} {...}
  ;                   {:cursor 10 :actual-tags [{:name :paren :started-at 1 :opened-at 2}
  ;                                             {:name :paren :started-at 4 :opened-at 5}
  ;                                             {:name :paren :started-at 7 :opened-at 8}]}
  ;                   {:name :paren :match ")"})
  ; =>
  ; {:cursor 10 :actual-tags [{:name :paren :started-at 1 :opened-at 2}
  ;                           {:name :paren :started-at 4 :opened-at 5}
  ;                           {:name :paren :started-at 7 :opened-at 8 :closes-at 10 :will-end-at 11}]}
  ;
  ; @return (map)
  [n tags options state {:keys [match name]}]
  (let [parent-tag     (parent-tag n tags options state)
        parent-tag-dex (vector/last-dex-of (:actual-tags @state) parent-tag)]
       (swap! state update :actual-tags vector/update-nth-item parent-tag-dex merge {:closes-at (:cursor @state) :will-end-at (+ (:cursor @state) (count match))})))

;; -- Actual state functions --------------------------------------------------
;; ----------------------------------------------------------------------------

(defn actualize-previous-tags
  ; @ignore
  ;
  ; @description
  ; ...
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; {:cursor (integer)}
  ;
  ; @return (map)
  [_ _ _ state]
  (letfn [(f0 [%] (cond-> % (-> % :will-open-at (=      (:cursor @state)))  (map/move :will-open-at :opens-at)
                            (-> % :will-end-at  (=      (:cursor @state)))  (map/move :will-end-at  :ends-at)
                            (-> % :starts-at    (= (dec (:cursor @state)))) (map/move :starts-at    :started-at)
                            (-> % :opens-at     (= (dec (:cursor @state)))) (map/move :opens-at     :opened-at)
                            (-> % :closes-at    (= (dec (:cursor @state)))) (map/move :closes-at    :closed-at)))]
         (swap! state update :actual-tags vector/->items f0)))

(defn actualize-updated-tags
  ; @ignore
  ;
  ; @description
  ; - Moves the currently ending tag (if any) from the 'actual-tags' tags vector into the 'left-tags' vector.
  ; - Ensures that the 'left-tags' vector is sorted by the starting positions of the left tags.
  ;   By default, it would be sorted by the ending positions if the ended tags were simply appended to the end of the vector.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ;
  ; @return (map)
  [_ _ _ state]
  (letfn [(f0 [a b] (> (:started-at a) (:started-at b)))]
         (if-let [ending-tag-dex (vector/last-dex-by (:actual-tags @state) :ends-at)]
                 (let [ended-tag (-> (:actual-tags @state) (nth ending-tag-dex) (map/move :ends-at :ended-at))]
                      (swap! state update :actual-tags vector/remove-nth-item ending-tag-dex)
                      (if-let [insert-dex (vector/first-dex-by (:left-tags @state) #(f0 % ended-tag))]
                              (swap! state update :left-tags vector/insert-item insert-dex ended-tag)
                              (swap! state update :left-tags vector/conj-item              ended-tag))))))

(defn check-for-opening-match
  ; @ignore
  ;
  ; @description
  ; ...
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ;
  ; @return (map)
  ; {:match (integer)
  ;  :name (keyword)}
  [n tags options state]
  (letfn [(f0 [tag-name] (if-let [opening-match (opening-match n tags options state tag-name)]
                                 (and (tag-ancestor-requirements-met? n tags options state tag-name)
                                      (tag-parent-requirements-met?   n tags options state tag-name)
                                      (-> opening-match))))]
         (and (-> (interpreter-enabled?       n tags options state))
              (-> (reading-any-opening-match? n tags options state) not)
              (-> (reading-any-closing-match? n tags options state) not)
              (or (some (fn [[tag-name _]] (f0 tag-name)) (map/filter-values tags (fn [[_ _ {:keys [priority] :or {priority :default}}]] (= priority :high))))
                  (some (fn [[tag-name _]] (f0 tag-name)) (map/filter-values tags (fn [[_ _ {:keys [priority] :or {priority :default}}]] (= priority :default))))
                  (some (fn [[tag-name _]] (f0 tag-name)) (map/filter-values tags (fn [[_ _ {:keys [priority] :or {priority :default}}]] (= priority :low))))))))

(defn check-for-closing-match
  ; @ignore
  ;
  ; @description
  ; ...
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ;
  ; @return (map)
  ; {:match (integer)
  ;  :name (keyword)}
  [n tags options state]
  (and (-> (reading-any-opening-match? n tags options state) not)
       (-> (reading-any-closing-match? n tags options state) not)
       (if-let [parent-tag (parent-tag n tags options state)]
               (closing-match n tags options state (:name parent-tag)))))

(defn update-previous-state
  ; @ignore
  ;
  ; @description
  ; ...
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ;
  ; @return (map)
  [n tags options state]
  (let [_ (actualize-previous-tags n tags options state)]
       (or (if-let [found-opening-match (check-for-opening-match n tags options state)]
                   (start-child-tag n tags options state found-opening-match))
           (if-let [found-closing-match (check-for-closing-match n tags options state)]
                   (close-parent-tag n tags options state found-closing-match)))))

(defn update-actual-state
  ; @ignore
  ;
  ; @description
  ; After the 'f' function is applied, it stores the updated result in the actual 'state' map.
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ; @param (*) updated-result
  ;
  ; @return (map)
  ; {:cursor (integer or keyword)
  ;  :result (*)}
  [n tags options state updated-result]
  (cond (-> updated-result vector? not)              (swap! state assoc :result    (-> updated-result))
        (-> updated-result first (= :$stop))         (swap! state assoc :result    (-> updated-result last) :cursor :iteration-stopped)
        (-> updated-result first (= :$set-metadata)) (swap! state merge {:metadata (-> updated-result second)
                                                                         :result   (-> updated-result last)})
        :else                                        (swap! state assoc :result    (-> updated-result))))

(defn prepare-next-state
  ; @ignore
  ;
  ; @description
  ; ...
  ;
  ; @param (string) n
  ; @param (map) tags
  ; @param (map) options
  ; @param (atom) state
  ;
  ; @return (map)
  [n tags options state]
  (actualize-updated-tags n tags options state)
  (swap! state update :cursor inc))
