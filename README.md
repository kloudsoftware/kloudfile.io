# kloudfile

This is a self-hosted all inclusive file and image sharing solution.

# Installation

To deploy kloudfile on your server you need only a few things:

Git, java8, a mysql server(and something to keep the session going like "screen" for linux)

I would also advise to get a URL and a SSL certificate (we used letsencrypt).

First, you need to clone the repo into a folder of your choice,

    git clone https://github.com/probE466/kloudfile.io.git
    
Then, navigate into src/main/resources and modify the application.properties with the correct login for your mysql user and change the password for your admin backend from

    security.user.password=secret
    
to something of your choice (use a strong password as this password allows to create api keys to use your server and manage users!).

if you have a ssl cert you may need to convert it into the correct file (https://www.drissamri.be/blog/java/enable-https-in-spring-boot/)
and also uncomment all lines at the bottom, fill them in with the correct values for your cert.

if you don't own a cert or want to use the service without one but do not want to access using the :8080, uncomment the server.port and change it to 80. You will need to run the server as admin if you do this in order to open the port.

Next, login into your mysql instance and create the "push" database.
Now you should be ready to start the server, navigate to the root folder of the git repo and execute the following command (linux): 
    
    sudo screen ./gradlew bootRun

Next, navigate to your domain and you should be greeted by the "logo" of kloudfile (https://kloudfile.io).

You can view your stats by going to http(s)://your_domain.ext/stats, create api keys by visiting http(s)://your_domain.ext/admin and from there also see a list of all api keys and revoke them, if you wish to.

To upload to the server you can use something like https://www.getpostman.com/ , write your own application to use the service(specifications not finished) or use our client(documentation still not complete): https://github.com/probE466/kloudfile.io-client
