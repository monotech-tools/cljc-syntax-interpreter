
(ns syntax-interpreter.core.check)

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(defn position-escaped?
  ; @description
  ; Returns whether a specific cursor position in the given 'n' string is preceded by an escape character.
  ;
  ; @param (string) n
  ; @param (integer) cursor
  ;
  ; @usage
  ; (position-escaped? "My string\n" 10)
  ; =>
  ; true
  ;
  ; @usage
  ; (position-escaped? "My string\n" 9)
  ; =>
  ; false
  ;
  ; @return (boolean)
  [n cursor]
  ; A cursor position is not escaped in case of even number of escape characters precede it.
  ; E.g., Escaped escape character: \\
  (and (-> cursor zero? not)
       (= "\\" (subs n (dec cursor) cursor))))
