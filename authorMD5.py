#!/usr/local/bin/python

import commands
import os
from datetime import datetime


signatures_file = open("apkSignatures.csv", "w")
apkDirectory = "/home/Dfosak/Desktop/apks"

startTime = datetime.now()

for dirname, dirnames, filenames in os.walk(apkDirectory):
	for filename in filenames:
		if ".apk" in filename:
			md5 = commands.getstatusoutput('unzip -p ' + os.path.join(dirname, filename) + ' META-INF/*.RSA *.DSA| keytool -printcert | grep MD5')
			startIdx = md5[1].find('MD5:')
			if startIdx != -1:
				signatures_file.write(filename.replace('.apk','') + ' ' + md5[1][startIdx+6:startIdx+53].replace(':','') + '\n')
			else:
				print "No signature found for " + filename


signatures_file.close()

print "Execution time"
print (datetime.now()-startTime)
