(ns athens.common-events.resolver.undo
  (:require
    [athens.common-db                     :as common-db]
    [athens.common-events                 :as common-events]
    [athens.common-events.graph.composite :as composite]
    [athens.common-events.graph.ops       :as graph-ops]))


(defn is-undo?
  [event]
  (-> event :op/trigger :op/undo?))


(defmulti resolve-atomic-op-to-undo-op
  #(:op/type %3))


(defmethod resolve-atomic-op-to-undo-op :block/save
  [db evt-db {:op/keys [args]}]
  (let [{:block/keys [uid]}    args
        {:block/keys [string]} (common-db/get-block evt-db [:block/uid uid])]
    (graph-ops/build-block-save-op db uid string)))


(defmethod resolve-atomic-op-to-undo-op :composite/consequence
  [db evt-db {:op/keys [consequences] :as op}]
  (assoc op :op/consequences (map (partial resolve-atomic-op-to-undo-op db evt-db)
                                  consequences)))


;; should there be a distinction between undo and redo?
(defn build-undo-event
  [db evt-db {:event/keys [type op] :as event}]
  (if-not (contains? #{:op/atomic} type)
    (throw (ex-info "Cannot undo non-atomic event" event))
    (->> op
         (resolve-atomic-op-to-undo-op db evt-db)
         vector
         (composite/make-consequence-op {:op/type type :op/undo? true})
         common-events/build-atomic-event)))

