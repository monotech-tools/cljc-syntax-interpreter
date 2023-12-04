
(ns syntax-interpreter.api
    (:require [syntax-interpreter.default-patterns   :as default-patterns]
              [syntax-interpreter.interpreter.engine :as interpreter.engine]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; syntax-reader.default-patterns
(def CLJ-PATTERNS default-patterns/CLJ-PATTERNS)
(def CSS-PATTERNS default-patterns/CSS-PATTERNS)

; syntax-reader.interpreter.engine
(def interpreter interpreter.engine/interpreter)
