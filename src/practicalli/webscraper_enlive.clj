(ns practicalli.webscraper-enlive
  (:gen-class)
  (:require [net.cgrand.enlive-html :as html]))

;; Website details
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def hacker-news-url "https://news.ycombinator.com/")


;; Big output, don't pretty print in CIDER :)
(slurp hacker-news-url)


;; generic function to get any website
;; will hit the website each time the function is called.

(defn website-content
  "Get website content from a given URL

  Arguments: web address as a string
  Returns: list of html content as hash-maps"

  [website-url]

  (html/html-resource (java.net.URL. website-url)))

#_(website-content hacker-news-url)


;; html-resource function takes raw HTML and converts it into a nested Clojure data structure
;; a bit like a simplfied Document Object Model (DOM).


;; Caching the website in a def
;; website only called once,
;; the first time the def name is evaluated

(def website-content-hacker-news
  "Get website content from Hacker News
  Returns: list of html content as hash-maps"

  (html/html-resource (java.net.URL. hacker-news-url)))

(clojure.pprint/pprint website-content-hacker-news)

;; website call should be cached on subsequence evaluations


;; Getting the headlines
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(html/select
  website-content-hacker-news
  [:td.title :a])


(take 1
      (html/select
        website-content-hacker-news
        [:td.title :a]))
;; => ({:tag :a,
;;     :attrs {:href "https://www.roadandtrack.com/new-cars/car-technology/a31451281/koenigsegg-gemera-engine-specs-analysis/", :class "storylink"},
;;     :content ("Koenigsegg’s 2.0-Liter No-Camshaft Engine Makes 600 Horsepower")})


;; the Enlive `text` function extracts the value from the `:content` key
;; Mapping the `text` function over our selected keys returns just the content we want


(map html/text
     (html/select
       website-content-hacker-news
       [:td.title :a]))


(take 4
      (map html/text
           (html/select
             website-content-hacker-news
             [:td.title :a])))
;; => ("Koenigsegg’s 2.0-Liter No-Camshaft Engine Makes 600 Horsepower" "roadandtrack.com" "Git Partial Clone lets you fetch only the large file you need" "gitlab.com")


(defn headlines
  "Headlines of the currently published stories

  Arguments: web address as a string
  Returns: list of headlines as strings"

  [website-url]

  (map html/text
       (html/select
         #_(website-content website-url)
         ;; DEV: cached website
         website-content-hacker-news
         [:td.title :a])))

;; the selector path matches the html tags path
;; <td class="title"> <a ,,,> ___ </a> </td>

(headlines hacker-news-url)


(take 10 (headlines hacker-news-url))
;; => ("Koenigsegg’s 2.0-Liter No-Camshaft Engine Makes 600 Horsepower" "roadandtrack.com" "Git Partial Clone lets you fetch only the large file you need" "gitlab.com" "Pence says European travel ban will extend to U.K. and Ireland" "axios.com" "Coronavirus has caused a bicycling boom in New York City" "grist.org" "Iceland radically cut teenage drug use" "weforum.org")

;; the html/select function takes parsed html content from html-resource
;; and selects the nodes specified in the Clojure vector,
;; that looks very similar to a CSS selector.
;; [:td.title :a]
;; This vector in Clojure is the same as `td.title a` in CSS
;; html/select returns a collection of the matching nodes

;; html/text extracts the text from each of the nodes returned by html/select


(html/select
  website-content-hacker-news
  [:td.subtext html/first-child])


(take 2
      (html/select
        website-content-hacker-news
        [:td.subtext html/first-child]))
;; => ({:tag :span, :attrs {:class "score", :id "score_22575931"}, :content ("148 points")}
;;     {:tag :a, :attrs {:href "item?id=22575931"}, :content ("3 hours ago")})


(map html/text
     (html/select
       website-content-hacker-news
       [:td.subtext html/first-child]))


(defn article-scoring
  "Points of the currently published stories

  Arguments: web address as a string
  Returns: headlines in hiccup format "

  [website-url]

  (map html/text
       (html/select
         ;; (website-content website-url)
         ;; DEV: cached website
         website-content-hacker-news
         [:td.subtext html/first-child])))

;; the selector path matches the html tags path
;; <td class="subtext"> ,,, </td>

(take 10
(article-scoring hacker-news-url))


(defn headlines-and-scoring
  "Putting it all together..."
  [website-url]
  (doseq [line (map #(str %1 " (" %2 ")")
                    (headlines website-url)
                    (article-scoring website-url))]
    (println line)))


(headlines-and-scoring hacker-news-url)

;; example output

;; Koenigsegg’s 2.0-Liter No-Camshaft Engine Makes 600 Horsepower (148 points)
;; roadandtrack.com (3 hours ago)
;; Git Partial Clone lets you fetch only the large file you need (119 points)
;; gitlab.com (3 hours ago)
;; Pence says European travel ban will extend to U.K. and Ireland (40 points)
;; axios.com (1 hour ago)
;; Coronavirus has caused a bicycling boom in New York City (96 points)
;; grist.org (1 hour ago)


(defn hn-headlines-and-scoring []
  (map html/text
       (html/select website-content-hacker-news
                    #{[:td.title :a] [:td.subtext html/first-child]})))

(take 8
      (hn-headlines-and-scoring))


(defn print-headlines-and-scoring []
  (doseq [line (map (fn [[h s]] (str h " (" s ")"))
                    (partition 2 (hn-headlines-and-scoring)))]
    (println line)))

(partition 2 (hn-headlines-and-scoring))
