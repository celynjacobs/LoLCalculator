from flask import Flask

app = Flask(__name__)

@app.route("/")
def     ():
    return "Welcome to my Flask page!"

if __name__ == "__main__":
    app.run(debug=True, hoat="0.0.0.0", port = 80)