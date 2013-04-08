#!/usr/local/bin/python

import commands
import os
import fileinput
from datetime import datetime



startTime = datetime.now()
signatures_file = open("apkSignatures.txt", "w")
for pathName in fileinput.input():
	md5 = commands.getstatusoutput('unzip -p ' + pathName.rstrip('\r\n') + ' META-INF/*.RSA *.DSA| keytool -printcert | grep MD5')
	startIdx = md5[1].find('MD5:')
	if startIdx != -1:
		signatures_file.write(pathName.rstrip('\r\n') + ' ' + md5[1][startIdx+6:startIdx+53].replace(':','') + '\n')
	else:
		print "No signature found for " + pathName.rstrip('\r\n')


signatures_file.close()

print "Execution time"
print (datetime.now()-startTime)
