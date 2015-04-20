__author__ = 'Akshay'
import sys
from os import listdir
from os.path import isfile, join
import os
import matplotlib.pyplot as plt
from sklearn import linear_model



def main(argv):
    mypath = os.path.abspath("result/")
    onlyfiles = [ f for f in listdir(mypath) if isfile(join(mypath,f)) ]
    data = []
    label = []
    for fl in onlyfiles:
        fl = mypath+"/"+fl
        with open(fl, "rb") as f:
            for line in f.readlines():
                line = line.split()
                temp = [0] * 70
                for element in line[:-1]:
                   temp[int(element)] = 1
                label.append(float(line[-1]))
                data.append(temp)
    regr = linear_model.LinearRegression()
    regr.fit(data[:-200], label[:-200])
    plt.scatter(data[-200:], label[-200:],  color='black')
    plt.plot(data[:-200], regr.predict(data[-200:]), color='blue',
         linewidth=3)

if __name__ == "__main__":
        main(sys.argv[1:])


