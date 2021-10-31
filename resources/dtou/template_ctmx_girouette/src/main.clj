(ns {{top/ns}}.{{main/ns}}
    (:require
     [ctmx.core :as ctmx]
     [ctmx.render :as render]
     [girouette.core :refer [css]]
     [org.httpkit.server :as http-kit]
     [reitit.ring :as ring]
     [ring.middleware.anti-forgery :as anti-forgery]
     [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
     [ring.middleware.reload :as ring-reload]
     [ring.util.response :as ring-response]))

(defonce    web-server_ (atom nil)) ; (fn stop [])
(defn  stop-web-server! [] (when-let [stop-fn @web-server_]
                             (stop-fn)))

(ctmx/defcomponent ^:endpoint hello [req my-name]
  [:div {:id "hello"} "Hello " my-name])

(ctmx/defcomponent ^:endpoint click-count [req ^:int num-clicks]
  [:div {:hx-post "click-count"
         :hx-swap "outerHTML"
         :hx-vals {:num-clicks (inc num-clicks)}}
   "You have clicked me " num-clicks " times."])

(defn ctmx-page []
  (ctmx/make-routes
   "/"
   (fn [request]
     (-> (render/html
          [:html
           [:head
            [:meta {:name "viewport"
                    :content "width=device-width, initial-scale=1, shrink-to-fit=no"}]
            [:link {:rel "stylesheet" :type "text/css" :href "girouette.css"}]]
           [:body
            [:div.bg-green-200
             [:h1 "Minimal ctmx example style with ornament"]
             [:div.content
              [:input {:name "my-name" :hx-patch "hello" :hx-target "#hello" :hx-trigger "keyup changed delay:100ms"}]
              [:br]
              (hello request "")
              [:br]
              (click-count request 0)
              [:br]

             ; don't do this:
              [:button {:hx-post "/kill"
                        :style {:margin-top 100}} "Stop server"]]]]

           ; JS scripts
           ;; htmx
           [:script {:src "https://unpkg.com/htmx.org@1.5.0"}]

           ;; js code to inject CSRF Token into HTMX
           ;; info: https://github.com/bigskysoftware/htmx/issues/70
           (let [csrf-token (force anti-forgery/*anti-forgery-token*)]
             [:script (format "document.body.addEventListener('htmx:configRequest', (event) => {event.detail.headers['X-CSRF-Token'] = '%s';});"
                              csrf-token)])])
         #_css
         (ring-response/response)
         (ring-response/header "content-type" "text/html")))))

(def http-handler
  (ring/ring-handler
   (ring/router
    [(ctmx-page)

     ; don't do this:
     ["/kill" {:post {:handler (fn [_] (stop-web-server!))}}]])
   (ring/routes
    (ring/create-resource-handler
     {:path "/"}))))

; todo: refresh page when source changes. 
; ring-refresh does not work... 

(defn start-web-server! [& [{:keys [port dev?]}]]
  (stop-web-server!)
  (let [port (or port 3000) ; Choose any available port
        handler (cond-> #'http-handler
                  dev? ring-reload/wrap-reload ; auto detect changes and loads it into ring
                  true (wrap-defaults (-> (assoc-in site-defaults [:security :anti-forgery] false))))
        [port stop-fn] (let [stop-fn (http-kit/run-server handler {:port port})]
                         [(:local-port (meta stop-fn)) (fn [] (stop-fn :timeout 100))])
        uri (format "http://localhost:%s/" port)]

    (println (str "Web server is running at " uri))
    (try
      (.browse (java.awt.Desktop/getDesktop) (java.net.URI. uri))
      (catch java.awt.HeadlessException _))

    (reset! web-server_ stop-fn)))

(comment
  (start-web-server! {:dev? true})
  (stop-web-server!))

(defn -main
  [& args]
  (if args
    (start-web-server! {:port (Integer/parseInt (first args))})
    (start-web-server!)))
; apply