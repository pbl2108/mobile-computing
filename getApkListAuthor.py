#!/usr/local/bin/python

import commands
import os
import fileinput
import time
from datetime import datetime

#unzip -p APKNAME META-INF/*.RSA *.DSA| keytool -printcert | grep MD5

startTime = datetime.now()
signatures_file = open("apkSignatures_d78910.txt", "w")
for pathName in fileinput.input():
	#print 	'unzip -p ' + pathName.rstrip('\r\n') + ' META-INF/*.RSA *.DSA| keytool -printcert | grep MD5'
	md5 = commands.getstatusoutput('unzip -p ' + '/proj/ds/encos/apk/' + pathName.rstrip('\r\n') + '.apk' + ' META-INF/*.RSA *.DSA| keytool -printcert | grep MD5')
	startIdx = md5[1].find('MD5:')
	if startIdx != -1:
		signatures_file.write(pathName.rstrip('\r\n') + ' ' + md5[1][startIdx+6:startIdx+53].replace(':','') + '\n')
	else:
		time.sleep(.1)
		md5 = commands.getstatusoutput('unzip -p ' + '/proj/ds/encos/apk/' + pathName.rstrip('\r\n') + '.apk' + ' META-INF/*.RSA *.DSA| keytool -printcert | grep MD5')
		startIdx = md5[1].find('MD5:')
		if startIdx != -1:
			signatures_file.write(pathName.rstrip('\r\n') + ' ' + md5[1][startIdx+6:startIdx+53].replace(':','') + '\n')
		else:
			time.sleep(.1)
			md5 = commands.getstatusoutput('unzip -p ' + '/proj/ds/encos/apk/' + pathName.rstrip('\r\n') + '.apk' + ' META-INF/*.RSA *.DSA| keytool -printcert | grep MD5')
			startIdx = md5[1].find('MD5:')
			if startIdx != -1:
				signatures_file.write(pathName.rstrip('\r\n') + ' ' + md5[1][startIdx+6:startIdx+53].replace(':','') + '\n')
			else:
				print
				print md5
				print 'unzip -p ' + '/proj/ds/encos/apk/' + pathName.rstrip('\r\n') + '.apk' + ' META-INF/*.RSA *.DSA| keytool -printcert | grep MD5'
				print "No signature found for " + pathName.rstrip('\r\n')


#signatures_file.close()

print "Execution time"
print (datetime.now()-startTime)
