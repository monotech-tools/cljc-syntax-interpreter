
(ns syntax-interpreter.api
    (:require [syntax-interpreter.core.utils         :as core.utils]
              [syntax-interpreter.default-patterns   :as default-patterns]
              [syntax-interpreter.interpreter.engine :as interpreter.engine]))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @tutorial How to use the interpreter?
;
; The [syntax-interpreter.api/interpreter](#interpreter) function applies  the given 'f' function at each cursor position
; of the given 'n' string.
;
; It provides a state of the actual position and a set of metafunctions for the applied function.
;
; @code
; (interpreter ; Processed string:
;              "abcdef (ghijkl ())"
;
;              ; Processing function:
;              (fn [result state metafunctions]
;                  (cond ((:tag-starts? metafunctions) :paren) (update result :paren-starts-at conj (:cursor state))
;                        ((:tag-opens?  metafunctions) :paren) (update result :paren-opens-at  conj (:cursor state))
;                        ((:tag-closes? metafunctions) :paren) (update result :paren-closes-at conj (:cursor state))
;                        ((:tag-ends?   metafunctions) :paren) (update result :paren-ends-at   conj (:cursor state))
;                        :return result))
;
;              ; Initial output:
;              {:paren-starts-at [] :paren-opens-at [] :paren-closes-at [] :paren-ends-at []}
;
;              ; Processed tags:
;              [[:paren #"\(" #"\)"]])
; =>
; {:paren-starts-at [ 7 16]
;  :paren-opens-at  [ 8 17]
;  :paren-closes-at [17 18]
;  :paren-ends-at   [18 19]}
; @---



; @tutorial How to use metafunctions?
;
; @code
; (interpreter "..."
;              (fn [_ _ {:keys [tag-opened?] :as metafunctions}]
;                  ; A)
;                  (tag-opened? :my-tag)
;                  ; B)
;                  ((:tag-opened? metafunctions) :my-tag))
;              nil
;              [[:my-tag #"..."]])
; @---



; @tutorial Provided metafunctions
;
; Ancestor / parent tag metafunctions:
;
; @code
; (interpreter "..."
;              (fn [_ _ metafunctions]
;                  ((:ancestor-tags   metafunctions))
;                  ((:depth           metafunctions))
;                  ((:no-tags-opened? metafunctions))
;                  ((:parent-tag      metafunctions))
;                  ((:tag-ancestor?   metafunctions) :my-tag)
;                  ((:tag-depth       metafunctions) :my-tag)
;                  ((:tag-parent?     metafunctions) :my-tag))
;              nil
;              [[:my-tag #"..."]])
;
; Interpreter metafunctions:
;
; @code
; (interpreter "..."
;              (fn [_ _ metafunctions]
;                  ((:reading-any-closing-match? metafunctions))
;                  ((:reading-any-opening-match? metafunctions))
;                  ((:reading-any-match?         metafunctions)))
;              nil
;              [[:my-tag #"..."]])
;
; Operator metafunctions:
;
; @code
; (interpreter "..."
;              (fn [_ _ metafunctions]
;                  ; The "My metadata" string will be available in the actual state from the next cursor position.
;                  ((:use-metadata metafunctions) "My metadata" "My result")
;                  ; Immediatelly stops the interpreter at the actual cursor position.
;                  ((:stop metafunctions) "My result")
;              nil
;              [[:my-tag #"..."]])
;
; Tag boundary metafunctions:
;
; @code
; (interpreter "..."
;              (fn [_ _ metafunctions]
;                  ((:closing-tag    metafunctions))
;                  ((:ending-tag     metafunctions))
;                  ((:opening-tag    metafunctions))
;                  ((:starting-tag   metafunctions))
;                  ((:tag-closed-at  metafunctions) :my-tag)
;                  ((:tag-closed?    metafunctions) :my-tag)
;                  ((:tag-closes?    metafunctions) :my-tag)
;                  ((:tag-ends?      metafunctions) :my-tag)
;                  ((:tag-opened-at  metafunctions) :my-tag)
;                  ((:tag-opened?    metafunctions) :my-tag)
;                  ((:tag-opens?     metafunctions) :my-tag)
;                  ((:tag-started-at metafunctions) :my-tag)
;                  ((:tag-started?   metafunctions) :my-tag)
;                  ((:tag-starts?    metafunctions) :my-tag))
;              nil
;              [[:my-tag #"..."]])
;
; Tag body / content metafunctions:
;
; @code
; (interpreter "..."
;              (fn [_ _ metafunctions]
;                  ((:tag-body    metafunctions) :my-tag)
;                  ((:tag-content metafunctions) :my-tag))
;              nil
;              [[:my-tag #"..."]])
;
; Tag history metafunctions:
;
; @code
; (interpreter "..."
;              (fn [_ _ metafunctions]
;                  ((:tag-left-count metafunctions) :my-tag)
;                  ((:tag-met-count  metafunctions) :my-tag))
;              nil
;              [[:my-tag #"..."]])
;
; Tag details metafunctions:
;
; @code
; (interpreter "..."
;              (fn [_ _ metafunctions]
;                  ((:tag-details         metafunctions) :my-tag)
;                  ((:tag-opening-pattern metafunctions) :my-tag)
;                  ((:tag-closing-pattern metafunctions) :my-tag)
;                  ((:tag-options         metafunctions) :my-tag))
;              nil
;              [[:my-tag #"..."]])

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------

; @redirect (syntax-interpreter.interpreter.core.utils/*)
(def with-options core.utils/with-options)

; @redirect (syntax-interpreter.default-patterns/*)
(def CLJ-PATTERNS default-patterns/CLJ-PATTERNS)
(def CSS-PATTERNS default-patterns/CSS-PATTERNS)

; @redirect (syntax-interpreter.interpreter.engine/*)
(def interpreter interpreter.engine/interpreter)
