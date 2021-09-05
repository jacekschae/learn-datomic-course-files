![alt learn-datomic-logo](https://res.cloudinary.com/schae/image/upload/f_auto,q_80,r_12/v1625037435/learndatomic.com/1200x640.png)

# [LearnDatomic.com](https://www.learndatomic.com)

Video course about Datomic Cloud. Including Datomic dev-local, Datomic Ions, and AWS deployment.

## Course files

The code in this repo includes two folders - `increments` - code for the start of each video (if you get lost somewhere along the way just copy the content of the video you are starting and continue). `cheffy` this is the start of the project / course. It's the same code as in `increments/06-start`

### Clone

```shell
$ git clone git@github.com:jacekschae/learn-datomic-course-files.git

$ cd learn-datomic-course-files/increments/<step-you-want-to-check-out>
```

### Run REPL

Probably you will run your REPL from your editor, and thre is nothing stopping you to run it from the command line:

```shell
clj
```

### Run the app
Probably you will run your REPL from your editor, and thre is nothing stopping you to run it from the command line:

```shell
clj -M:dev src/main/cheffy/server.clj
```

## License

Copyright © 2021 Jacek Schae