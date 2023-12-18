
(ns syntax-interpreter.default-patterns)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @note
; Lookbehind, lookahead and match length limits help decrease the processing time,
; and help create more accurate matches for tag patterns with lookaround assertions.
;
; @note
; Strings and regex patterns can include escaped quote characters. Therefore, their
; closing pattern must exclude escape characters that precede the closing quote character.
;
; @constant (vectors in map)
; {:tag-name (vector)
;   [(keyword) tag-name
;    (regex pattern) pattern / opening-pattern
;    (regex pattern)(opt) closing-pattern
;    (map)(opt) options]}
;
; @usage
; (interpreter "..." (fn [_ _ _] ...) nil [(:boolean           CLJ-PATTERNS)
;                                          (:comment           CLJ-PATTERNS)
;                                          (:conditional-form  CLJ-PATTERNS)
;                                          (:derefed-symbol    CLJ-PATTERNS)
;                                          (:keyword           CLJ-PATTERNS)
;                                          (:list              CLJ-PATTERNS)
;                                          (:map               CLJ-PATTERNS)
;                                          (:meta-map          CLJ-PATTERNS)
;                                          (:meta-string       CLJ-PATTERNS)
;                                          (:meta-symbol       CLJ-PATTERNS)
;                                          (:regex-pattern     CLJ-PATTERNS)
;                                          (:string            CLJ-PATTERNS)
;                                          (:symbol            CLJ-PATTERNS)
;                                          (:unresolved-symbol CLJ-PATTERNS)
;                                          (:var               CLJ-PATTERNS)
;                                          (:vector            CLJ-PATTERNS)])
(def CLJ-PATTERNS
     {:symbol            [:symbol            #"[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&][a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{0,}(?=[\s\[\]\(\)\{\}\"\@\~])"     {:pattern-limits {:lookahead 1}}]
      :meta-symbol       [:meta-symbol       #"\^[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&][a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{0,}(?=[\s\[\]\(\)\{\}\"\@\~])"   {:pattern-limits {:lookahead 1}}]
      :derefed-symbol    [:derefed-symbol    #"\@[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&][a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{0,}(?=[\s\[\]\(\)\{\}\"\@\~])"   {:pattern-limits {:lookahead 1}}]
      :unresolved-symbol [:unresolved-symbol #"\'[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&][a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{0,}(?=[\s\[\]\(\)\{\}\"\@\~])"   {:pattern-limits {:lookahead 1}}]
      :var               [:var               #"\#\'[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&][a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{0,}(?=[\s\[\]\(\)\{\}\"\@\~])" {:pattern-limits {:lookahead 1}}]
      :keyword           [:keyword           #"\:[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{1,}(?=[\s\[\]\(\)\{\}\"\@\~])"                                   {:pattern-limits {:lookahead 1}}]
      :meta-keyword      [:meta-keyword      #"\^\:[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{1,}(?=[\s\[\]\(\)\{\}\"\@\~])"                                 {:pattern-limits {:lookahead 1}}]
      :boolean           [:boolean           #"true|false(?=[\s\[\]\(\)\{\}\"\@\~])"                          {:pattern-limits {:match 5 :lookahead 1}}]
      :conditional-form  [:conditional-form  #"\#\?\(\:clj[s]{0,}" #"\)"                                      {:pattern-limits {:opening/match 8 :closing/match 1}}]
      :list              [:list              #"\("                 #"\)"                                      {:pattern-limits {:opening/match 1 :closing/match 1}}]
      :map               [:map               #"\{"                 #"\}"                                      {:pattern-limits {:opening/match 1 :closing/match 1}}]
      :meta-map          [:meta-map          #"\^\{"               #"\}"                                      {:pattern-limits {:opening/match 2 :closing/match 1}}]
      :vector            [:vector            #"\["                 #"\]"                                      {:pattern-limits {:opening/match 1 :closing/match 1}}]
      :comment           [:comment           #";"                  #"\n"           {:disable-interpreter? true :pattern-limits {:opening/match 1 :closing/match 1}}]
      :regex-pattern     [:regex-pattern     #"\#\""               #"(?<=[^\\])\"" {:disable-interpreter? true :pattern-limits {:opening/match 2 :closing/match 1 :closing/lookbehind 1}}]
      :string            [:string            #"\""                 #"(?<=[^\\])\"" {:disable-interpreter? true :pattern-limits {:opening/match 1 :closing/match 1 :closing/lookbehind 1}}]
      :meta-string       [:meta-string       #"\^\""               #"(?<=[^\\])\"" {:disable-interpreter? true :pattern-limits {:opening/match 2 :closing/match 1 :closing/lookbehind 1}}]})

; @note
; Lookbehind, lookahead and match length limits help decrease the processing time,
; and help create more accurate matches for tag patterns with lookaround assertions.
;
; @constant (vectors in map)
; {:tag-name (vector)
;   [(keyword) tag-name
;    (regex pattern) pattern / opening-pattern
;    (regex pattern)(opt) closing-pattern
;    (map)(opt) options]}
;
; @usage
; (interpreter "..." (fn [_ _ _] ...) nil [(:class CSS-PATTERNS)
;                                          (:id    CSS-PATTERNS)
;                                          (:tag   CSS-PATTERNS)])
(def CSS-PATTERNS
     {:class [:class #"(?<=[\s\}\]\)\*\~\>\+a-zA-Z\d\_\-])\.[a-zA-Z\d\_][a-zA-Z\d\_\-]{0,}(?<=[\s\{\[\*\~\>\:\.\#])" {:pattern-limits {:lookbehind 1 :lookahead 1}}]
      :id    [:id    #"(?<=[\s\}\]\)\*\~\>\+a-zA-Z\d\_\-])\#[a-zA-Z\d\_][a-zA-Z\d\_\-]{0,}(?<=[\s\{\[\*\~\>\:\.\#])" {:pattern-limits {:lookbehind 1 :lookahead 1}}]
      :tag   [:tag   #"(?<=[\s\}\]\)\*\~\>\+a-zA-Z\d\_\-])[a-zA-Z]{1,}(?<=[\s\{\[\*\~\>\:\.\#])"                     {:pattern-limits {:lookbehind 1 :lookahead 1}}]})
