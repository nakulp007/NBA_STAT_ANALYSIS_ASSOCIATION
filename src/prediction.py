__author__ = 'Akshay'
import sys
from os import listdir
from os.path import isfile, join
import os
import matplotlib.pyplot as plt
import numpy as np
from sklearn import linear_model



def main(argv):
    mypath = os.path.abspath("result/")
    onlyfiles = [ f for f in listdir(mypath) if isfile(join(mypath,f)) ]
    count = 0
    y = [0.0]*20
    value = int(argv[0])
    for fl in onlyfiles:
        fl = mypath+"/"+fl
        with open(fl, "rb") as f:
            for line in f.readlines():
                line = line.split()
                if len(line) == 2:
                    if int(line[0]) == value:
                        try:
                            y[count] = float(line[1])
                        except IndexError:
                            pass
        count += 1

    limit = int(argv[1])
    x =  xrange(0,limit,1)
    regr = linear_model.LinearRegression()
    data = np.asarray([x])
    label = np.asarray([y[:limit]])
    regr.fit(data, label)
    plt.scatter(np.asarray([xrange(limit,20,1)])[0], np.asarray([y[limit:]]),  color='black')
    plt.plot(np.asarray([xrange(20-limit,20,1)])[0], regr.predict(np.asarray([xrange(20-limit,20,1)]))[0], color='blue', linewidth=3)
    plt.show()

if __name__ == "__main__":
        main(sys.argv[1:])


