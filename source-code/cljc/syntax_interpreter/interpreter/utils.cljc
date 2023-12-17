
(ns syntax-interpreter.interpreter.utils
    (:require [fruits.map.api                 :as map :refer [update-by]]
              [fruits.regex.api               :as regex]
              [fruits.seqable.api             :as seqable :refer [last-dex]]
              [fruits.string.api              :as string]
              [fruits.vector.api              :as vector]
              [syntax-interpreter.core.config :as core.config]))

;; -- Tag details functions ---------------------------------------------------
;; ----------------------------------------------------------------------------

(defn tag-details->opening-pattern
  ; @ignore
  ;
  ; @description
  ; Derives the opening pattern from the given 'tag-details' vector.
  ;
  ; @param (vector) tag-details
  ;
  ; @return (regex pattern)
  [[_ opening-pattern & _]]
  (-> opening-pattern))

(defn tag-details->closing-pattern
  ; @ignore
  ;
  ; @description
  ; Derives the closing pattern (if any) from the given 'tag-details' vector.
  ;
  ; @param (vector) tag-details
  ;
  ; @return (regex pattern)
  [[_ _ closing-pattern & _]]
  (if (-> closing-pattern regex/pattern?)
      (-> closing-pattern)))

(defn tag-details->options
  ; @ignore
  ;
  ; @description
  ; Derives the tag options map (if any) from the given 'tag-details' vector.
  ;
  ; @param (vector) tag-details
  ;
  ; @return (map)
  [tag-details]
  (if-let [tag-options (-> tag-details last)]
          (if (map? tag-options) tag-options)))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn tag-details
  ; @ignore
  ;
  ; @description
  ; Returns the tag details of the given tag.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; @param (keyword) tag-name
  ;
  ; @usage
  ; (tag-details "..." [[:my-tag #"..." #"..." {...}]] {...} {...} :my-tag)
  ; =>
  ; [:my-tag #"..." #"..." {...}]
  ;
  ; @return (vector)
  [_ tags _ _ tag-name]
  (vector/first-match tags (fn [[% & _]] (= % tag-name))))

(defn tag-opening-pattern
  ; @ignore
  ;
  ; @description
  ; Returns the tag opening pattern of the given tag.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; @param (keyword) tag-name
  ;
  ; @usage
  ; (tag-opening-pattern "..." [[:my-tag #"..." #"..." {...}]] {...} {...} :my-tag)
  ; =>
  ; #"..."
  ;
  ; @return (regex pattern)
  [n tags options state tag-name]
  (if-let [tag-details (tag-details n tags options state tag-name)]
          (tag-details->opening-pattern tag-details)))

(defn tag-closing-pattern
  ; @ignore
  ;
  ; @description
  ; Returns the tag closing pattern of the given tag.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; @param (keyword) tag-name
  ;
  ; @usage
  ; (tag-closing-pattern "..." [[:my-tag #"..." #"..." {...}]] {...} {...} :my-tag)
  ; =>
  ; #"..."
  ;
  ; @return (regex pattern)
  [n tags options state tag-name]
  (if-let [tag-details (tag-details n tags options state tag-name)]
          (tag-details->closing-pattern tag-details)))

(defn tag-options
  ; @ignore
  ;
  ; @description
  ; Returns the tag options of the given tag.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; @param (keyword) tag-name
  ;
  ; @usage
  ; (tag-options "..." [[:my-tag #"..." #"..." {...}]] {...} {...} :my-tag)
  ; =>
  ; {...}
  ;
  ; @return (map)
  [n tags options state tag-name]
  (if-let [tag-details (tag-details n tags options state tag-name)]
          (tag-details->options tag-details)))

(defn tag-omittag?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the given tag doesn't have a closing pattern (omittag).
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
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
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (boolean)
  [_ _ _ {:keys [actual-tags]}]
  (letfn [(f0 [{:keys [closed-at opened-at opens-at]}]
              ; Tags that are either already closed or not opened yet:
              (or closed-at (not (or opens-at opened-at))))]
         (vector/all-items-match? actual-tags f0)))

(defn depth
  ; @ignore
  ;
  ; @description
  ; Returns the depth of the actual cursor position.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ;
  ; @return (integer)
  [_ _ _ {:keys [actual-tags]}]
  (letfn [(f0 [{:keys [closed-at opened-at opens-at]}]
              ; Tags that are already opened and aren't closed yet:
              (and (or opens-at opened-at) (not closed-at)))]
         (vector/match-count actual-tags f0)))

(defn tag-depth
  ; @ignore
  ;
  ; @description
  ; Returns the actual opened depth of a specific tag.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ; @param (keyword) tag-name
  ;
  ; @return (integer)
  [_ _ _ {:keys [actual-tags]} tag-name]
  (letfn [(f0 [{:keys [closed-at name opened-at opens-at]}]
              ; Tags with a specific tag name that are already opened and aren't closed yet:
              (and (= name tag-name) (or opens-at opened-at) (not closed-at)))]
         (vector/match-count actual-tags f0)))

(defn ancestor-tags
  ; @ignore
  ;
  ; @description
  ; Returns the ancestor tags of the actual cursor position.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ;
  ; @return (maps in vector)
  [_ _ _ {:keys [actual-tags]}]
  (letfn [(f0 [{:keys [closed-at opened-at opens-at]}]
              ; Tags that are already opened and aren't closed yet:
              (and (or opens-at opened-at) (not closed-at)))]
         (vector/keep-items-by actual-tags f0)))

(defn parent-tag
  ; @ignore
  ;
  ; @description
  ; Returns the parent tag of the actual cursor position.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ;
  ; @return (map)
  [_ _ _ {:keys [actual-tags]}]
  (letfn [(f0 [{:keys [closed-at opened-at opens-at]}]
              ; Tags that are already opened and aren't closed yet:
              (and (or opens-at opened-at) (not closed-at)))]
         (vector/last-match actual-tags f0)))

(defn tag-opened?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE the given tag is opened.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; @param (keyword) tag-name
  ;
  ; @return (integer)
  [_ _ _ {:keys [actual-tags]} tag-name]
  (letfn [(f0 [{:keys [closed-at name opened-at opens-at]}]
              ; Tags with a specific tag name that are already opened and aren't closed yet:
              (and (= name tag-name) (or opens-at opened-at) (not closed-at)))]
         (vector/any-item-matches? actual-tags f0)))

(defn tag-ancestor?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE the given tag is an opened ancestor tag of the actual cursor position.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; @param (keyword) tag-name
  ;
  ; @return (integer)
  [n tags options state tag-name]
  (tag-opened? n tags options state tag-name))

(defn tag-parent?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the given tag is the opened parent tag of the actual cursor position.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
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
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ;
  ; @return (integer)
  [n tags options {:keys [actual-tags] :as state}]
  (if-let [parent-tag (parent-tag n tags options state)]
          (letfn [(f0 [{:keys [started-at starts-at] :as %}]
                      ; Children of the parent tag:
                      (> (or starts-at started-at) (:started-at parent-tag)))]
                 (let [not-ended-children-count (-> state :actual-tags (vector/match-count f0))]
                      (-> parent-tag :child-met (- not-ended-children-count))))))

;; -- Iteration functions -----------------------------------------------------
;; ----------------------------------------------------------------------------

(defn offset-reached?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the actual cursor position reached the given 'offset' position.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; {:offset (integer)(opt)}
  ; @param (map) state
  ; {:cursor (integer)}
  ;
  ; @return (keyword)
  [n _ {:keys [offset] :or {offset 0}} {:keys [cursor]}]
  (let [offset (seqable/normalize-cursor n offset)]
       (>= cursor offset)))

(defn endpoint-reached?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the actual cursor position reached the given 'endpoint' position.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; {:endpoint (integer)(opt)}
  ; @param (map) state
  ; {:cursor (integer)}
  ;
  ; @return (keyword)
  [n _ {:keys [endpoint] :or {endpoint (count n)}} {:keys [cursor]}]
  (let [endpoint (seqable/normalize-cursor n endpoint)]
       (>= cursor endpoint)))

(defn iteration-ended?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the actual cursor position reached the last cursor position in the given 'n' string.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:cursor (integer)}
  ;
  ; @return (boolean)
  [n _ _ {:keys [cursor]}]
  (seqable/cursor-last? n cursor))

(defn iteration-stopped?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the 'stop' metafunction stopped the iteration.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:cursor (integer)}
  ;
  ; @return (boolean)
  [_ _ _ {:keys [cursor]}]
  (= cursor :iteration-stopped))

;; -- Interpreter functions ---------------------------------------------------
;; ----------------------------------------------------------------------------

(defn interpreter-disabled-by
  ; @ignore
  ;
  ; @description
  ; Returns the disabling tag's name if the interpreter is disabled by an opened tag.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ;
  ; @return (keyword)
  [n tags options {:keys [actual-tags] :as state}]
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
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (boolean)
  [n tags options state]
  (-> (interpreter-disabled-by n tags options state)))

(defn interpreter-enabled?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the interpreter is NOT disabled by an opened tag.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
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
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ;
  ; @return (boolean)
  [_ _ _ {:keys [actual-tags]}]
  (-> actual-tags last :will-open-at some?))

(defn reading-any-closing-match?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if any closing pattern's last found match is already started but not ended yet at the actual cursor position.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)}
  ;
  ; @return (boolean)
  [_ _ _ {:keys [actual-tags]}]
  (-> actual-tags last :will-end-at some?))

(defn reading-any-match?
  ; @ignore
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (boolean)
  [n tags options state]
  (or (reading-any-opening-match? n tags options state)
      (reading-any-closing-match? n tags options state)))

(defn not-reading-any-match?
  ; @ignore
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (boolean)
  [n tags options state]
  (-> (reading-any-match? n tags options state) not))

;; -- Regex functions ---------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn opening-match
  ; @ignore
  ;
  ; @description
  ; Returns the the tag name and the found match if the given tag's opening match starts at the actual cursor position.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:cursor (integer)}
  ; @param (vector) tag-details
  ;
  ; @return (map)
  ; {:match (integer)
  ;  :name (keyword)}
  [n tags options {:keys [cursor] :as state} [tag-name :as tag-details]]
  ; Merging regex actions into one function decreased the interpreter processing time.
  (if-let [opening-pattern (tag-details->opening-pattern tag-details)]
          (let [tag-options           (tag-details->options tag-details)
                max-lookbehind-length (or (get-in tag-options                     [:pattern-limits :opening/lookbehind])
                                          (get-in tag-options                     [:pattern-limits :lookbehind])
                                          (get-in core.config/DEFAULT-TAG-OPTIONS [:pattern-limits :lookbehind]))
                max-lookahead-length  (or (get-in tag-options                     [:pattern-limits :opening/lookahead])
                                          (get-in tag-options                     [:pattern-limits :lookahead])
                                          (get-in core.config/DEFAULT-TAG-OPTIONS [:pattern-limits :lookahead]))
                max-match-length      (or (get-in tag-options                     [:pattern-limits :opening/match])
                                          (get-in tag-options                     [:pattern-limits :match])
                                          (get-in core.config/DEFAULT-TAG-OPTIONS [:pattern-limits :match]))
                corrected-cursor      (min cursor max-lookbehind-length)
                observed-from         (max (->    0) (- cursor max-lookbehind-length))
                observed-to           (min (count n) (+ cursor max-match-length max-lookahead-length))
                observed-part         (subs n observed-from observed-to)]
               (if-let [opening-match (regex/re-from observed-part opening-pattern corrected-cursor)]
                       {:name tag-name :match opening-match}))))

(defn closing-match
  ; @ignore
  ;
  ; @description
  ; Returns the the tag name and the found match if the given tag's closing match starts at the actual cursor position.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:cursor (integer)}
  ; @param (vector) tag-details
  ;
  ; @return (map)
  ; {:match (integer)
  ;  :name (keyword)}
  [n tags options {:keys [cursor] :as state} [tag-name :as tag-details]]
  ; Merging regex actions into one function decreased the interpreter processing time.
  (if-let [closing-pattern (tag-details->closing-pattern tag-details)]
          (let [tag-options           (tag-details->options tag-details)
                max-lookbehind-length (or (get-in tag-options                     [:pattern-limits :closing/lookbehind])
                                          (get-in tag-options                     [:pattern-limits :lookbehind])
                                          (get-in core.config/DEFAULT-TAG-OPTIONS [:pattern-limits :lookbehind]))
                max-lookahead-length  (or (get-in tag-options                     [:pattern-limits :closing/lookahead])
                                          (get-in tag-options                     [:pattern-limits :lookahead])
                                          (get-in core.config/DEFAULT-TAG-OPTIONS [:pattern-limits :lookahead]))
                max-match-length      (or (get-in tag-options                     [:pattern-limits :closing/match])
                                          (get-in tag-options                     [:pattern-limits :match])
                                          (get-in core.config/DEFAULT-TAG-OPTIONS [:pattern-limits :match]))
                corrected-cursor      (min cursor max-lookbehind-length)
                observed-from         (max (->    0) (- cursor max-lookbehind-length))
                observed-to           (min (count n) (+ cursor max-match-length max-lookahead-length))
                observed-part         (subs n observed-from observed-to)]
               (if-let [closing-match (regex/re-from observed-part closing-pattern corrected-cursor)]
                       {:name tag-name :match closing-match}))))

;; -- Tag processing requirement functions ------------------------------------
;; ----------------------------------------------------------------------------

(defn tag-requires-no-ancestors?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the given tag requires no ancestor tags.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; @param (vector) tag-details
  ;
  ; @return (boolean)
  [n tags options state tag-details]
  (if-let [tag-options (tag-details->options tag-details)]
          (-> tag-options :accepted-ancestors (= []))))

(defn tag-requires-no-parents?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the given tag requires no parent tags.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; @param (vector) tag-details
  ;
  ; @return (boolean)
  [n tags options state tag-details]
  (if-let [tag-options (tag-details->options tag-details)]
          (-> tag-options :accepted-parents (= []))))

(defn tag-requires-accepted-ancestor?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the given tag requires any accepted ancestor tags.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; @param (vector) tag-details
  ;
  ; @return (boolean)
  [n tags options state tag-details]
  (if-let [tag-options (tag-details->options tag-details)]
          (-> tag-options :accepted-ancestors vector/nonempty?)))

(defn tag-requires-accepted-parent?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the given tag requires any accepted ancestor tags.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; @param (vector) tag-details
  ;
  ; @return (boolean)
  [n tags options state tag-details]
  (if-let [tag-options (tag-details->options tag-details)]
          (-> tag-options :accepted-parents vector/nonempty?)))

(defn tag-any-accepted-ancestor-opened?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if at least one of the accepted ancestor tags of the given tag is opened.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; @param (vector) tag-details
  ;
  ; @return (boolean)
  [n tags options state tag-details]
  (if-let [tag-options (tag-details->options tag-details)]
          (if-let [accepted-ancestors (:accepted-ancestors tag-options)]
                  (letfn [(f0 [accepted-ancestor] (tag-ancestor? n tags options state accepted-ancestor))]
                         (vector/any-item-matches? accepted-ancestors f0)))))

(defn tag-any-accepted-parent-opened?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if at least one of the accepted parent tags of the given tag is opened (as the the actual parent tag).
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; @param (vector) tag-details
  ;
  ; @return (boolean)
  [n tags options state tag-details]
  (if-let [tag-options (tag-details->options tag-details)]
          (if-let [accepted-parents (:accepted-parents tag-options)]
                  (letfn [(f0 [accepted-parent] (tag-parent? n tags options state accepted-parent))]
                         (vector/any-item-matches? accepted-parents f0)))))

(defn tag-ancestor-requirements-met?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the actual cursor position meets the given tag's ancestor requirements.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; @param (vector) tag-details
  ;
  ; @return (boolean)
  [n tags options state tag-details]
  (and (or (-> (tag-requires-no-ancestors?        n tags options state tag-details) not)
           (-> (no-tags-opened?                   n tags options state)))
       (or (-> (tag-requires-accepted-ancestor?   n tags options state tag-details) not)
           (-> (tag-any-accepted-ancestor-opened? n tags options state tag-details)))))

(defn tag-parent-requirements-met?
  ; @ignore
  ;
  ; @description
  ; Returns TRUE if the actual cursor position meets the given tag's parent requirements.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; @param (vector) tag-details
  ;
  ; @return (boolean)
  [n tags options state tag-details]
  (and (or (-> (tag-requires-no-parents?        n tags options state tag-details) not)
           (-> (no-tags-opened?                 n tags options state)))
       (or (-> (tag-requires-accepted-parent?   n tags options state tag-details) not)
           (-> (tag-any-accepted-parent-opened? n tags options state tag-details)))))

(defn tag-closes-instead?
  ; @ignore
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; @param (vector) tag-details
  ;
  ; @return (boolean)
  [n tags options state [tag-name & _ :as tag-details]]
  (and (tag-opened?   n tags options state tag-name)
       (closing-match n tags options state tag-details)))

(defn tag-not-closes-instead?
  ; @ignore
  ;
  ; @description
  ; - Returns TRUE if the given tag is not closing at the actual cursor position.
  ; - When an opening match is found at a cursor position, it must be checked whether
  ;   its closing match is also found at the same position in order to find the closing
  ;   matches for symmetric tags (= opening and closing patterns match the same).
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; @param (vector) tag-details
  ;
  ; @return (boolean)
  [n tags options state tag-details]
  (-> (tag-closes-instead? n tags options state tag-details) not))

;; -- Update child / parent tag functions -------------------------------------
;; ----------------------------------------------------------------------------

(defn start-child-tag
  ; @ignore
  ;
  ; @description
  ; Updates the given 'state' by adding a new depth for the given tag to the 'actual-tags' vector,
  ; and increasing the ':child-met' (how many children have been already reached by the cursor) value in the actual parent tag (if any).
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:cursor (integer)}
  ; @param (map) opening-match
  ; {:match (string)
  ;  :name (keyword)}
  ;
  ; @usage
  ; (start-child-tag "..." {...} {...}
  ;                  {:cursor 7 :actual-tags [{:name :paren :started-at 1 :opened-at 2 :child-met 1}
  ;                                           {:name :paren :started-at 4 :opened-at 5 :child-met 0}]}
  ;                  {:name :paren :match "("})
  ; =>
  ; {:cursor 7 :actual-tags [{:name :paren :started-at 1 :opened-at 2 :child-met 1}
  ;                          {:name :paren :started-at 4 :opened-at 5 :child-met 1}
  ;                          {:name :paren :starts-at  7 :will-open-at 8}]}
  ;
  ; @return (map)
  [n tags options {:keys [cursor] :as state} {:keys [match name]}]
  ; The ':child-met' value helps determine how many children of the actual parent tag have been reached by the interpreter at the actual cursor position.
  ; The ':left-tags' map contains that information as well but its highly performance heavy to derive in case the interpreted string is extremly long (over 100k char)
  ; and full of tags.
  (letfn [(f0 [{:keys [closed-at opened-at opens-at]}]
              ; Tags that are already opened and aren't closed yet:
              (and (or opens-at opened-at) (not closed-at)))]
         (if (tag-omittag? n tags options state name)
             (-> state (update :actual-tags vector/conj-item {:name name :starts-at cursor :will-end-at  (+ cursor (count match))})
                       (update :actual-tags vector/update-last-item-by f0 update :child-met inc))
             (-> state (update :actual-tags vector/conj-item {:name name :starts-at cursor :will-open-at (+ cursor (count match))})
                       (update :actual-tags vector/update-last-item-by f0 update :child-met inc)))))

(defn close-parent-tag
  ; @ignore
  ;
  ; @description
  ; Updates the given 'state' by closing the actual parent tag in the 'actual-tags' vector.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)
  ;  :cursor (integer)}
  ; @param (map) closing-match
  ; {:match (string)
  ;  :name (keyword)}
  ;
  ; @usage
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
  [n tags options {:keys [actual-tags cursor] :as state} {:keys [match name]}]
  (let [parent-tag     (parent-tag n tags options state)
        parent-tag-dex (vector/last-dex-of actual-tags parent-tag)]
       (update state :actual-tags vector/update-nth-item parent-tag-dex merge {:closes-at cursor :will-end-at (+ cursor (count match))})))

;; -- State functions ---------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn filter-provided-state
  ; @ignore
  ;
  ; @description
  ; Removes the ':result' from the actual state, because the result is provided to the applied function in a separate parameter.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (map)
  [_ _ _ state]
  (dissoc state :result))

;; -- Actual state functions --------------------------------------------------
;; ----------------------------------------------------------------------------

(defn actualize-previous-tags
  ; @ignore
  ;
  ; @description
  ; ...
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:cursor (integer)}
  ;
  ; @return (map)
  [_ _ _ {:keys [cursor] :as state}]
  (letfn [(f0 [%] (cond-> % (-> % :will-open-at (=      cursor))  (assoc    :child-met 0)
                            (-> % :will-open-at (=      cursor))  (map/move :will-open-at :opens-at)
                            (-> % :will-end-at  (=      cursor))  (map/move :will-end-at  :ends-at)
                            (-> % :starts-at    (= (dec cursor))) (map/move :starts-at    :started-at)
                            (-> % :opens-at     (= (dec cursor))) (map/move :opens-at     :opened-at)
                            (-> % :closes-at    (= (dec cursor))) (map/move :closes-at    :closed-at)))]
         (update state :actual-tags vector/->items f0)))

(defn actualize-updated-tags
  ; @ignore
  ;
  ; @description
  ; - Moves the currently ending tag (if any) from the 'actual-tags' tags vector into the 'left-tags' map.
  ; - Ensures that the tags in the 'left-tags' map is sorted by the starting positions of the tags.
  ;   By default, it would be sorted by the ending positions if the ended tags were simply appended to the end of the vector.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; {:actual-tags (maps in vector)
  ;  :left-tags (map)}
  ;
  ; @return (map)
  [_ _ _ {:keys [actual-tags left-tags] :as state}]
  (letfn [(f0 [a b] (> (:started-at a) (:started-at b)))]
         (if-let [ending-tag-dex (vector/last-dex-by actual-tags :ends-at)]
                 (let [ended-tag        (-> actual-tags (nth ending-tag-dex) (map/move :ends-at :ended-at))
                       ended-tag-name   (-> ended-tag :name)
                       ended-tag-bounds (-> ended-tag (dissoc :name))]
                      (if-let [insert-dex (vector/first-dex-by left-tags #(f0 % ended-tag))]
                              (-> state (update-in [:actual-tags]              vector/remove-nth-item ending-tag-dex)
                                        (update-in [:left-tags ended-tag-name] vector/insert-item insert-dex ended-tag-bounds))
                              (-> state (update-in [:actual-tags]              vector/remove-nth-item ending-tag-dex)
                                        (update-in [:left-tags ended-tag-name] vector/conj-item ended-tag-bounds))))
                 (-> state))))

(defn check-for-opening-match
  ; @ignore
  ;
  ; @description
  ; ...
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (map)
  ; {:match (integer)
  ;  :name (keyword)}
  [n tags options state]
  (letfn [(f0 [tag-details] (if-let [opening-match (opening-match n tags options state tag-details)]
                                    (and (tag-ancestor-requirements-met? n tags options state tag-details)
                                         (tag-parent-requirements-met?   n tags options state tag-details)
                                         (tag-not-closes-instead?        n tags options state tag-details)
                                         (-> opening-match))))]
         (and (interpreter-enabled?   n tags options state)
              (not-reading-any-match? n tags options state)
              (some f0 tags))))

(defn check-for-closing-match
  ; @ignore
  ;
  ; @description
  ; ...
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (map)
  ; {:match (integer)
  ;  :name (keyword)}
  [n tags options state]
  (and (not-reading-any-match? n tags options state)
       (if-let [parent-tag (parent-tag n tags options state)]
               (letfn [(f0 [[tag-name & _]] (= tag-name (:name parent-tag)))]
                      (let [tag-details (vector/last-match tags f0)]
                           (closing-match n tags options state tag-details))))))

(defn update-previous-state
  ; @ignore
  ;
  ; @description
  ; ...
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (map)
  [n tags options state]
  (let [state (actualize-previous-tags n tags options state)]
       (or (if-let [found-opening-match (check-for-opening-match n tags options state)]
                   (start-child-tag n tags options state found-opening-match))
           (if-let [found-closing-match (check-for-closing-match n tags options state)]
                   (close-parent-tag n tags options state found-closing-match))
           (-> state))))

(defn update-actual-state
  ; @ignore
  ;
  ; @description
  ; After the given 'f' function is applied, it stores the updated result in the actual 'state' map.
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ; @param (*) updated-result
  ;
  ; @return (map)
  ; {:cursor (integer or keyword)
  ;  :result (*)}
  [n tags options state updated-result]
  (cond (-> updated-result vector? not)              (-> state (assoc :result   (-> updated-result)))
        (-> updated-result first (= :$stop))         (-> state (assoc :result   (-> updated-result last) :cursor :iteration-stopped))
        (-> updated-result first (= :$use-metadata)) (-> state (assoc :metadata (-> updated-result second))
                                                               (assoc :result   (-> updated-result last)))
        :else                                        (-> state (assoc :result   (-> updated-result)))))

(defn prepare-next-state
  ; @ignore
  ;
  ; @description
  ; ...
  ;
  ; @param (string) n
  ; @param (vectors in vector) tags
  ; @param (map) options
  ; @param (map) state
  ;
  ; @return (map)
  [n tags options state]
  (let [state (actualize-updated-tags n tags options state)]
       (update state :cursor inc)))
