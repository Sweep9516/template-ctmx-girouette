# dtou/template-ctmx-girouette

Generate a simple project for a server with [ctmx](https://github.com/whamtet/ctmx) ([htmx](https://htmx.org/)) and [girouette](https://github.com/green-coder/girouette) (tailwindcss) included.

## Usage

Assuming you have installed `deps-new` as your `new`, create a new 'ctmx-girouette' project:

```bash
clj -Sdeps '{:deps {dtou/template-ctmx-girouette {:git/url "https://github.com/d-tou/template-ctmx-girouette.git" :git/sha "36b66439ccb2b44cf8be115e971b194a99555953"}}}' -Tnew create :template dtou/template-ctmx-girouette :name myusername/my-ctmx-girouette-project :target-dir my-ctmx-girouette-project
```

In the newly created project, launch girouette: 


    $ clojure -X:girouette


Find the guide on the [deps-new](https://github.com/seancorfield/deps-new) github page to install it. 

