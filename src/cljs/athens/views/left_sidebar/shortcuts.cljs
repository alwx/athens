(ns athens.views.left-sidebar.shortcuts
  (:require
    ["/components/SidebarShortcuts/List" :refer [List]]
    ["/components/Widget/Widget" :refer [Widget WidgetHeader WidgetBody WidgetToggle]]
    [athens.reactive :as reactive]
    [athens.router :as router]
    [athens.views.left-sidebar.subs :as left-sidebar-subs]
    [re-frame.core :as rf]))


(defn global-shortcuts
  []
  (let [shortcuts (reactive/get-reactive-shortcuts)
        is-open? (left-sidebar-subs/get-widget-open? "shortcuts")]
    [:> Widget
     {:pr            4
      :defaultIsOpen is-open?}
     [:> WidgetHeader {:title "Shortcuts"
                       :pl    6}
      [:> WidgetToggle {:onClick #(rf/dispatch [:left-sidebar.widgets/toggle-widget "shortcuts"])}]]

     [:> WidgetBody
      [:> List {:items              shortcuts
                :onOpenItem         (fn [e [_order page]]
                                      (let [shift? (.-shiftKey e)]
                                        (rf/dispatch [:reporting/navigation {:source :left-sidebar
                                                                             :target :page
                                                                             :pane   (if shift?
                                                                                       :right-pane
                                                                                       :main-pane)}])
                                        (router/navigate-page page e)))
                :onUpdateItemsOrder (fn [oldIndex newIndex]
                                      (cond
                                        (< oldIndex newIndex) (rf/dispatch [:left-sidebar/drop oldIndex newIndex :after])
                                        (> oldIndex newIndex) (rf/dispatch [:left-sidebar/drop oldIndex newIndex :before])))}]]]))
