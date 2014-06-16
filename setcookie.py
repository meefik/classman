#!/usr/bin/python

import os
import sys
import sqlite

if len(sys.argv) != 2:
   print "There is not enough input parameters!"
   sys.exit(1)

HOME_DIR = os.path.expanduser('~')
COOKIE_FILE = (HOME_DIR+'/.mozilla/firefox/default/cookies.sqlite')

connection = sqlite.connect(COOKIE_FILE)
cursor = connection.cursor()

if sys.argv[1] == "list":
   cursor.execute('SELECT * FROM moz_cookies')
   for row in cursor:
      print row
else:
   cookie = "test"+sys.argv[1]
   print "set cookie: comp="+cookie
   cursor.execute("insert into moz_cookies (name, value, host, path, expiry, lastAccessed, isSecure, isHttpOnly) values ('comp', %s, '.ifmo.ru', '/', 2110000000, 0, 0, 0)",cookie)
   connection.commit()
