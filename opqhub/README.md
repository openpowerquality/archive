OPQHub
======

OPQHUb is a cloud based power quality aggregation and analysis service.

Building OPQHub
========

### Installing the Play Framework
* OPQHub uses version 2.2.2 of the Play Framework and can be downloaded at [http://downloads.typesafe.com/play/2.2.2/play-2.2.2.zip](http://downloads.typesafe.com/play/2.2.2/play-2.2.2.zip)
* Follow the instructions at [https://www.playframework.com/documentation/2.2.x/Installing](https://www.playframework.com/documentation/2.2.x/Installing) to install the Play Framework on your development machine

### Cloning the OPQHub directory
* Clone the OPQHub repository with `git clone https://github.com/openpowerquality/OPQHub.git`

### Testing the Play Framework Installation
1. Open a terminal window and change to the cloned OPQHub directory
2. Type `play`

After running the above command, you should see

           _
     _ __ | | __ _ _  _
    | '_ \| |/ _' | || |
    |  __/|_|\____|\__ /
    |_|            |__/

    play 2.2.2 built with Scala 2.10.3 (running Java 1.8.0_20),    http://www.playframework.com

    > Type "help play" or "license" for more information.
    > Type "exit" or use Ctrl+D to leave this console.

    [OPQHub] $

If you see the above, congrats, you have installed the Play Framework correctly. If you don't see the above, please refer to the Play Framework documentation.

### Compiling OPQHub
1. Open a terminal window and change to the cloned OPQHub directory
2. Enter the Play environment with the `play` command
3. Once in the Play environment, issue the command `clean`
4. To download needed dependencies, issue the command `update`
5. To compile the project, issue the command `compile`
6. Test that the application compiled correctly by completing the following steps in the section, "_Running OPQHub_".

### Running OPQHub
1. Open a terminal window and change to the cloned OPQHub directory
2. Enter the Play environment with the `play` command
3. From within the Play environment, issue the command `~ run [port #]`
4. Navigate your web browser to http://localhost:9000/ (replace 9000 with a different port number if you didn't use the default port number)

The `~` before `run` tells Play to continuously compile when it sees source changes.

`[port #]` should be set to a port that you want OPQHub to run from (9000 by default).

### Configuring the database
The default OPQHub build comes with a built-in H2 in-memory database enabled. This is great for testing purposes, but not so great for long term storage. The Play framework can also be used to connect to MySQL databases. If you plan on using MySQL for data persistence, complete the following steps.

Note that familiarity with MySQL is assumed here. If you have any questions relating to MySQL, please refer to the MySQL [documentation](http://dev.mysql.com/doc/).

1. Download and install MySQL [http://www.mysql.com/](http://www.mysql.com/)
2. Create a database for use by OPQHub named `opqhub`.
3. Create a database user named `opquser`.
4. Edit `conf/application.conf` lines 54 - 57 to reflect the following changes


    db.default.driver=com.mysql.jdbc.Driver
    db.default.url="jdbc:mysql://localhost/opqhub"
    db.default.user="opquser"
    db.default.password="your_password_here"

For further information, see Play's guide on accessing a database from Java at [https://www.playframework.com/documentation/2.2.x/JavaDatabase](https://www.playframework.com/documentation/2.2.x/JavaDatabase).

### Configuring the mailer
OPQHub has a feature that will notify users of power quality events either by e-mail or by SMS. In order for this feature to work, you must have access to an SMTP e-mail server (for instance, Google's mail service).

If you wish to enable this feature in your build, you'll need to have access to an SMTP e-mail account. Edit the file `conf/application.conf` lines 60 - 65 to setup the alert account. The following shows a standard Google account.


    smtp.host="smtp.gmail.com"
    smtp.port=465
    smtp.ssl=true
    smtp.tls=true
    smtp.user="your_alert_email@gmail.com"
    smtp.pass="your_alert_email_password"

### Deploying to production
Once everything else is set up, it's possible to create a production OPQHub by using Play's `dist` command. This method makes it easy to transfer OPQHub to a server without the need of installing any of Play's dependencies. The only dependency required for this method is a configured database on the production server.

1. Open a terminal window and change to the cloned OPQHub directory
2. Issue the command `play` to enter the Play environment
3. Issue the command `clean`
4. Issue the command `dist` to rebuild from scratch and package your application

Once the application has been packaged, the resulting .zip file can be found in `OPQHub/target/universal/opqhub-1.0-SNAPSHOT.zip`.

Copy the above .zip file to your production server and unzip it into a directory called `opqhub`. This should give you `.../opqhub/opqhub-1.0-SNAPSHOT/`

To start the server
1. Open a terminal window and change to the location of the unzipped distribution's `bin` directory `.../opqhub/opqhub-1.0-SNAPSHOT/bin/`
2. On linux, make sure the script `opqhub` is executable by issueing the command `chmod u+x opqhub`
3. On Linux, to start the production distribution of OPQHub, issue the command `bash opqhub`. On Windows, issue the command `opqhub.bat`
4. If you wish to configure the port pass the option `-Dhttp.port=your_port` where `your_port` is replaced by the port number you wish to run OPQHub from

### Production server over SSH
If you need to SSH into your production environment, then the above method will work until your SSH connection is closed. Then, the process will die due to receiving a SIGTERM signal.

To overcome this, I recommend using [tmux](http://tmux.sourceforge.net/) on your production server which will manage processes even after the SSH connection has died. A great introduction to tmux can be found at [http://robots.thoughtbot.com/a-tmux-crash-course](http://robots.thoughtbot.com/a-tmux-crash-course).

In general though, if you have tmux installed, you can complete the following steps

1. Log into production server over ssh
2. Deploy to production as above
4. If you haven't created a tmux session before, do so now by issueing the command `tmux new -s opqhub-production`. This will create a new tmux session for you to run OPQHub in.
5. If you've already created the tmux session, switch to it by issueing the command `tmux attach -t opqhub-production`
6. Run OQPHub be switching to the OPQHub's `bin` directory
7. Run the correct script for your system and background the task (i.e. `bash opqhub&`)
8. Detach from the tmux session with `tmux detach`.
9. Now you can safely log off of SSH and OPQHub will continue running under tmux
