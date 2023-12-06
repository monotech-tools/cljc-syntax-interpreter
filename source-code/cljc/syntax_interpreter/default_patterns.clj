
(ns syntax-interpreter.default-patterns)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @important
; Providing lookbehind, lookahead and match length limits helps decrease the processing time.
;
; @constant (map)
; {:my-tag (vector)
;   [(regex-pattern) pattern / opening-pattern
;    (regex-pattern)(opt) closing-pattern
;    (map)(opt) options]}
(def CLJ-PATTERNS
     [{:symbol      [#""]}    ; <- order of priority
      {:meta-symbol [#""]}]
      



     {:symbol            [    #"[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&][a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{0,}(?=[\n\r\s\t\[\]\(\)\{\}\"\@\~])" {:pattern-limits {:lookbehind 0          :lookahead 1}}]
      :meta-symbol       [  #"\^[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&][a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{0,}(?=[\n\r\s\t\[\]\(\)\{\}\"\@\~])" {:pattern-limits {:lookbehind 0          :lookahead 1}}]
      :derefed-symbol    [  #"\@[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&][a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{0,}(?=[\n\r\s\t\[\]\(\)\{\}\"\@\~])" {:pattern-limits {:lookbehind 0          :lookahead 1}}]
      :unresolved-symbol [  #"\'[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&][a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{0,}(?=[\n\r\s\t\[\]\(\)\{\}\"\@\~])" {:pattern-limits {:lookbehind 0          :lookahead 1}}]
      :var               [#"\#\'[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&][a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{0,}(?=[\n\r\s\t\[\]\(\)\{\}\"\@\~])" {:pattern-limits {:lookbehind 0          :lookahead 1}}]
      :keyword           [  #"\:[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{1,}(?=[\n\r\s\t\[\]\(\)\{\}\"\@\~])"                                 {:pattern-limits {:lookbehind 0          :lookahead 1}}]
      :meta-keyword      [#"\^\:[a-zA-Z\d\+\-\_\<\>\=\*\!\?\%\&\/\#\:\.\']{1,}(?=[\n\r\s\t\[\]\(\)\{\}\"\@\~])"                                 {:pattern-limits {:lookbehind 0          :lookahead 1}}]
      :boolean           [#"true|false(?=[\n\r\s\t\[\]\(\)\{\}\"\@\~])" {:pattern-limits {:lookbehind 0 :match 5 :lookahead 1}}]
      :conditional-form  [#"\#\?\(\:clj[s]{0,}" #"\)"                            {:pattern-limits {:lookbehind 0 :opening/match 8 :closing/match 1 :lookahead 0}}]
      :list              [#"\("                 #"\)"                            {:pattern-limits {:lookbehind 0 :match 1                          :lookahead 0}}]
      :map               [#"\{"                 #"\}"                            {:pattern-limits {:lookbehind 0 :match 1                          :lookahead 0}}]
      :meta-map          [#"\^\{"               #"\}"                            {:pattern-limits {:lookbehind 0 :opening/match 2 :closing/match 1 :lookahead 0}}]
      :vector            [#"\["                 #"\]"                            {:pattern-limits {:lookbehind 0 :match 1                          :lookahead 0}}]
      :comment           [#";"                  #"\n" {:disable-interpreter? true :pattern-limits {:lookbehind 0 :match 1                          :lookahead 0}}]
      :regex-pattern     [#"\#\""               #"\"" {:disable-interpreter? true :pattern-limits {:lookbehind 0 :match 2                          :lookahead 0}}]
      :string            [#"\""                 #"\"" {:disable-interpreter? true :pattern-limits {:lookbehind 0 :match 1                          :lookahead 0}}]
      :meta-string       [#"\^\""               #"\"" {:disable-interpreter? true :pattern-limits {:lookbehind 0 :opening/match 2 :closing/match 1 :lookahead 0}}]})

; @important
; Providing lookbehind, lookahead and match length limits helps decrease the processing time.
;
; @constant (map)
; {:my-tag (vector)
;   [(regex-pattern) pattern / opening-pattern
;    (regex-pattern)(opt) closing-pattern
;    (map)(opt) options]}
(def CSS-PATTERNS
     {:class [#"(?<=[\n\r\s\t\}\]\)\*\~\>\+a-zA-Z\d\_\-])\.[a-zA-Z\d\_][a-zA-Z\d\_\-]{0,}(?<=[\n\r\s\t\{\[\*\~\>\:\.\#])" {:pattern-limits {:lookbehind 1 :lookahead 1}}]
      :id    [#"(?<=[\n\r\s\t\}\]\)\*\~\>\+a-zA-Z\d\_\-])\#[a-zA-Z\d\_][a-zA-Z\d\_\-]{0,}(?<=[\n\r\s\t\{\[\*\~\>\:\.\#])" {:pattern-limits {:lookbehind 1 :lookahead 1}}]
      :tag   [#"(?<=[\n\r\s\t\}\]\)\*\~\>\+a-zA-Z\d\_\-])[a-zA-Z]{1,}(?<=[\n\r\s\t\{\[\*\~\>\:\.\#])"                     {:pattern-limits {:lookbehind 1 :lookahead 1}}]})
