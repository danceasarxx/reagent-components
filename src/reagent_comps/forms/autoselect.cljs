(ns reagent-comps.forms.autoselect
  (:require [beicon.core :as rx]
            [reagent.core :as r]
            [reagent-comps.beiconx :as beiconx]
            [reagent-comps.forms.core :as forms]))


(defn model [{:keys [suggest-fn async? on-select] :or {async? false}}]
  (let [hint        (rx/subject)
        selection   (rx/subject)
        _           (rx/on-value selection on-select)
        suggestions (if async?
                      (rx/flat-map suggest-fn hint)
                      (rx/map suggest-fn hint))
        dirty       (rx/merge
                     (rx/map (constantly true) hint)
                     (rx/map (constantly false) selection))
        suggest?    (rx/merge
                     (rx/map (constantly false) selection)
                     (rx/map seq suggestions))]
    {:hint        hint
     :selection   selection
     :suggestions suggestions
     :dirty       dirty
     :suggest?    suggest?}))


(defn- option-fn [render on-select]
  (fn [s]
    [:li.option-item {:on-click #(on-select s)}
     [render s]]))


(defn view [{:keys [suggestions dirty suggest? selection hint]}
            {:keys [show render class-name] :or {class-name ""}}]
  (let [buffer  (beiconx/to-ratom "" (rx/merge hint (rx/map show selection)))
        options (beiconx/to-ratom [] suggestions)
        show?   (beiconx/to-ratom false suggest?)]
    (fn []
      [:div.autoselect
       [:input.input {:type       "text"
                      :class-name class-name
                      :on-change  (forms/input hint)
                      :value      @buffer}]
       (into [:ul.options {:class-name (if @show? "show" "")}]
             (map (option-fn render selection)) @options)])))
