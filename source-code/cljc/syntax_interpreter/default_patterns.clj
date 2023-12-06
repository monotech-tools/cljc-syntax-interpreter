
(ns syntax-interpreter.default-patterns)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @important
; Providing lookbehind, lookahead and match length limits helps decrease the processing time.
;
; @constant (vectors in map)
; {:tag-name (vector)
;   [(keyword) tag-name
;    (regex-pattern) pattern / opening-pattern
;    (regex-pattern)(opt) closing-pattern
;    (map)(opt) options]}
(def CLJ-PATTERNS
     {:symbol            [:symbol            #"[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&][a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{0,}(?=[\n\r\s\t\[\]\(\)\{\}\"\@\~])"     {:pattern-limits {:lookbehind 0 :lookahead 1}}]
      :meta-symbol       [:meta-symbol       #"\^[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&][a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{0,}(?=[\n\r\s\t\[\]\(\)\{\}\"\@\~])"   {:pattern-limits {:lookbehind 0 :lookahead 1}}]
      :derefed-symbol    [:derefed-symbol    #"\@[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&][a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{0,}(?=[\n\r\s\t\[\]\(\)\{\}\"\@\~])"   {:pattern-limits {:lookbehind 0 :lookahead 1}}]
      :unresolved-symbol [:unresolved-symbol #"\'[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&][a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{0,}(?=[\n\r\s\t\[\]\(\)\{\}\"\@\~])"   {:pattern-limits {:lookbehind 0 :lookahead 1}}]
      :var               [:var               #"\#\'[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&][a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{0,}(?=[\n\r\s\t\[\]\(\)\{\}\"\@\~])" {:pattern-limits {:lookbehind 0 :lookahead 1}}]
      :keyword           [:keyword           #"\:[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{1,}(?=[\n\r\s\t\[\]\(\)\{\}\"\@\~])"                                   {:pattern-limits {:lookbehind 0 :lookahead 1}}]
      :meta-keyword      [:meta-keyword      #"\^\:[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{1,}(?=[\n\r\s\t\[\]\(\)\{\}\"\@\~])"                                 {:pattern-limits {:lookbehind 0 :lookahead 1}}]
      :boolean           [:boolean           #"true|false(?=[\n\r\s\t\[\]\(\)\{\}\"\@\~])"          {:pattern-limits {:lookbehind 0 :match 5                          :lookahead 1}}]
      :conditional-form  [:conditional-form  #"\#\?\(\:clj[s]{0,}" #"\)"                            {:pattern-limits {:lookbehind 0 :opening/match 8 :closing/match 1 :lookahead 0}}]
      :list              [:list              #"\("                 #"\)"                            {:pattern-limits {:lookbehind 0 :match 1                          :lookahead 0}}]
      :map               [:map               #"\{"                 #"\}"                            {:pattern-limits {:lookbehind 0 :match 1                          :lookahead 0}}]
      :meta-map          [:meta-map          #"\^\{"               #"\}"                            {:pattern-limits {:lookbehind 0 :opening/match 2 :closing/match 1 :lookahead 0}}]
      :vector            [:vector            #"\["                 #"\]"                            {:pattern-limits {:lookbehind 0 :match 1                          :lookahead 0}}]
      :comment           [:comment           #";"                  #"\n" {:disable-interpreter? true :pattern-limits {:lookbehind 0 :match 1                          :lookahead 0}}]
      :regex-pattern     [:regex-pattern     #"\#\""               #"\"" {:disable-interpreter? true :pattern-limits {:lookbehind 0 :match 2                          :lookahead 0}}]
      :string            [:string            #"\""                 #"\"" {:disable-interpreter? true :pattern-limits {:lookbehind 0 :match 1                          :lookahead 0}}]
      :meta-string       [:meta-string       #"\^\""               #"\"" {:disable-interpreter? true :pattern-limits {:lookbehind 0 :opening/match 2 :closing/match 1 :lookahead 0}}]})

; @important
; Providing lookbehind, lookahead and match length limits helps decrease the processing time.
;
; @constant (vectors in map)
; {:tag-name (vector)
;   [(keyword) tag-name
;    (regex-pattern) pattern / opening-pattern
;    (regex-pattern)(opt) closing-pattern
;    (map)(opt) options]}
(def CSS-PATTERNS
     {:class [:class #"(?<=[\n\r\s\t\}\]\)\*\~\>\+a-zA-Z\d\_\-])\.[a-zA-Z\d\_][a-zA-Z\d\_\-]{0,}(?<=[\n\r\s\t\{\[\*\~\>\:\.\#])" {:pattern-limits {:lookbehind 1 :lookahead 1}}]
      :id    [:id    #"(?<=[\n\r\s\t\}\]\)\*\~\>\+a-zA-Z\d\_\-])\#[a-zA-Z\d\_][a-zA-Z\d\_\-]{0,}(?<=[\n\r\s\t\{\[\*\~\>\:\.\#])" {:pattern-limits {:lookbehind 1 :lookahead 1}}]
      :tag   [:tag   #"(?<=[\n\r\s\t\}\]\)\*\~\>\+a-zA-Z\d\_\-])[a-zA-Z]{1,}(?<=[\n\r\s\t\{\[\*\~\>\:\.\#])"                     {:pattern-limits {:lookbehind 1 :lookahead 1}}]})
