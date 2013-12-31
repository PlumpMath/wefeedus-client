(ns wefeedus-client.html-templates
  (:use [io.pedestal.app.templates :only [tfn dtfn tnodes]]))

(defmacro wefeedus-client-templates
  {:wefeedus (dtfn (tnodes "wefeedus-client.html" "wefeedus") #{:id})
   :date-stepper (dtfn (tnodes "wefeedus-client.html" "date-stepper") #{:id})
   :add (dtfn (tnodes "add.html" "add"))})

;; Note: this file will not be reloaded automatically when it is changed.
